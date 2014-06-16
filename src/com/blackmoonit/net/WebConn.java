package com.blackmoonit.net;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.blackmoonit.io.StreamUtils;
import com.blackmoonit.net.ssl.CustomSSLTrustManager;
import com.blackmoonit.net.ssl.CustomSSLTrustManager.SSLContextException;

/**
 * A class to attempt a more generic solution to trying to open an http/https
 * web connection that may or may not require custom Certificate Authority chains
 * to authorize SSL connections with unknown or self-signed hosts.
 *
 * @author Ryan Fischbach
 */
public class WebConn {
	static private final String TAG = "androidBits."+WebConn.class.getSimpleName();
	protected WeakReference<Context> mContext = null;
	public URL mUrl = null;
	public String mUrlKey = null;
	protected Uri mCAResource = null;
	protected SSLContext mSSLContext = null;
	public String myUserAgent = null;
	
	public WebConn(Context aContext) {
		mContext = new WeakReference<Context>(aContext);
	}
	
	public WebConn(Context aContext, URL aUrl) {
		mContext = new WeakReference<Context>(aContext);
		setUrl(aUrl);
	}
	
	public Context getContext() {
		return (mContext!=null) ? mContext.get() : null;
	}
	
	/**
	 * Specify the URL to use to connect.
	 * @param aUrl - either an HTTP or HTTPS connection.
	 * @return Returns THIS for chaining purposes.
	 */
	public WebConn setUrl(URL aUrl) {
		mUrl = aUrl;
		return this;
	}
	
	/**
	 * Specify the URL string to use to connect.
	 * @param aUrlStr - either an HTTP or HTTPS connection.
	 * @return Returns THIS for chaining purposes.
	 * @throws MalformedURLException
	 */
	public WebConn setUrl(String aUrlStr) throws MalformedURLException {
		return setUrl(new URL(aUrlStr));
	}
	
	/**
	 * Basically a password needed to connect to the URL.
	 * @param aAuthKey - string
	 * @return Returns THIS for chaining purposes.
	 */
	public WebConn setAuthKey(String aAuthKey) {
		mUrlKey = aAuthKey;
		return this;
	}
	
	/**
	 * When using PROTOCOL_HTTPS_CUSTOM, the custom CA needs to be supplied,
	 * this is how to supply it.
	 * @param aCAResource - either "asset://filename" or "file://filepath".
	 * @return Returns THIS for chaining purposes.
	 */
	public WebConn setCertAuthResource(Uri aCAResource) {
		mCAResource = aCAResource;
		mSSLContext = null;
		return this;
	}
	
	/**
	 * When using custom CAs, the custom CA chain needs to be loaded and used.
	 * @return Returns the SSLContext with all the certificate magic already loaded.
	 * @throws SSLContextException
	 */
	protected SSLContext getCustomSSLContext() throws SSLContextException {
		Context theContext = getContext();
		if (mSSLContext==null && theContext!=null && mCAResource!=null) {
			CustomSSLTrustManager theTm = new CustomSSLTrustManager(theContext,mCAResource);
			mSSLContext = theTm.getSSLContext();
		}
		return mSSLContext;
	}
	
	/**
	 * Opens the web connection and provides the custom SSLContext, if necessary.
	 * @return Returns the URLConnection or NULL if it failed to open.
	 */
	public URLConnection openConnection() {
		URLConnection theUrlConn = null;
		try {
			theUrlConn = mUrl.openConnection();
			if (mCAResource!=null) {
				//use custom CA
				SSLContext theSSLContext = getCustomSSLContext();
				((HttpsURLConnection)theUrlConn).setSSLSocketFactory(theSSLContext.getSocketFactory());
			}
			if (mUrlKey!=null) {
				theUrlConn.setRequestProperty("Authorization", WebUtils.getBasicAuthString(mUrlKey));
			}
			if (myUserAgent!=null) {
				theUrlConn.setRequestProperty("User-Agent",myUserAgent);
			}
		} catch (IOException ioe) {
			Log.e(TAG,"openConnection",ioe);
		} catch (SSLContextException sce) {
			Log.e(TAG,"openConn (custom-ssl)",sce);
		}
		return theUrlConn;
	}
	
	/**
	 * Given an open connection, upload the data string.
	 * @param aUrlConn - open URLConnection.
	 * @param aDataToSend - string data.
	 * @throws IOException
	 */
	public void writeToUrl(URLConnection aUrlConn, String aDataToSend) throws IOException {
		URLConnection theUrlConn = aUrlConn;
		try {
			theUrlConn.setDoInput(true);
			theUrlConn.setDoOutput(true);
			((HttpURLConnection)theUrlConn).setRequestMethod("POST");
			OutputStream theOutStream = null;
			try {
				theOutStream = theUrlConn.getOutputStream();
				BufferedWriter theWriter = null;
				try {
					theWriter = new BufferedWriter(new OutputStreamWriter(theOutStream,WebUtils.WEB_CHARSET));
					theWriter.write(aDataToSend);
					theWriter.flush();
				} finally {
					if (theWriter!=null)
						theWriter.close();
				}
			} finally {
				if (theOutStream!=null)
					theOutStream.close();
			}
		} catch (ProtocolException pe) {
			Log.e(TAG,"writeToUri",pe);
			throw new IOException(pe);
		}
	}
	
	/**
	 * Simple method used to read the URL response and return the text as String.
	 * @param aUrlConn - open URLConnection.
	 * @return Returns the response text as a String.
	 * @throws IOException
	 */
	public String readResponseFromUrl(URLConnection aUrlConn) throws IOException {
		InputStream theInStream = aUrlConn.getInputStream();
		try {
			return StreamUtils.inputStreamToString(theInStream);
		} finally {
			theInStream.close();
		}
	}
	
	/**
	 * Given an open connection, upload the data string and return the response.
	 * @param aUrlConn - open URLConnection.
	 * @param aDataToSend - string data.
	 * @return Returns the response text as a String.
	 */
	public String parleyWithUrl(URLConnection aUrlConn, String aDataToSend) {
		try {
			writeToUrl(aUrlConn, aDataToSend);
			return readResponseFromUrl(aUrlConn);
		} catch (IOException e) {
			Log.e(TAG,"parleyWithUrl",e);
		}
		return null;
	}

	/**
	 * Given an open connection, return the downloaded text as a JSON-converted object.
	 * @param aUrlConn - open URLConnection.
	 * @param aResultClass - resulting class JSON data is converted into.
	 * @return Returns the class filled in with appropriate data or NULL on failure.
	 */
	public <T> T jsonDownload(URLConnection aUrlConn, Class<T> aResultClass) {
		URLConnection theUrlConn = aUrlConn;
		try {
			String theResponse = readResponseFromUrl(theUrlConn);
			return WebUtils.fromJson(theResponse, aResultClass);
		} catch (IOException e) {
			Log.e(TAG,"downloadJson",e);
		}
		return null;
	}
	
	/**
	 * Given an open connection, upload the object converted to a JSON string.
	 * @param aUrlConn - open URLConnection.
	 * @param aObject - object instance with data to convert to string.
	 * @param aTypeOfObject - type of aObject so JSON conversion works.
	 */
	public void jsonUpload(URLConnection aUrlConn, Object aObject, Type aTypeOfObject) {
		URLConnection theUrlConn = aUrlConn;
		try {
			//theUrlConn.setRequestProperty("Content-Type", "application/json; charset="+CHARSET);
			//theUrlConn.setRequestProperty("Content-length", aDataToSend.length()+"");
			writeToUrl(theUrlConn, WebUtils.toJson(aObject,aTypeOfObject));
		} catch (IOException e) {
			Log.e(TAG,"uploadJson",e);
		}
	}
	
	/**
	 * Given an open connection, supply some string data to send and convert the text
	 * response from JSON into the aResultClass.
	 * @param aUrlConn - open URLConnection.
	 * @param aObject - object instance with data to convert to string.
	 * @param aTypeOfObject - type of aObject so JSON conversion works.
	 * @param aResultClass - resulting class JSON data is converted into.
	 * @return Returns the class filled in with appropriate data or NULL on failure.
	 * @see WebConn#uploadJson(URLConnection, Object, Type)
	 * @see WebConn#downloadJson(URLConnection, Class)
	 */
	public <T> T jsonParley(URLConnection aUrlConn, Object aObject, Type aTypeOfObject, Class<T> aResultClass) {
		jsonUpload(aUrlConn, aObject, aTypeOfObject);
		return jsonDownload(aUrlConn, aResultClass);
	}
	
}
