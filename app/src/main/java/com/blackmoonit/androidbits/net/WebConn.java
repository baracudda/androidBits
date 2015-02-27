package com.blackmoonit.androidbits.net;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.blackmoonit.androidbits.io.StreamUtils;
import com.blackmoonit.androidbits.net.ssl.CustomSSLTrustManager;
import com.blackmoonit.androidbits.net.ssl.CustomSSLTrustManager.SSLContextException;

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
	protected URL mUrl = null;
	protected String mUrlKey = null;
	protected Uri mCAResource = null;
	protected SSLContext mSSLContext = null;

	public String myUserAgent = null;
	public Integer expect_spontaneous_packages = null; //should poll URL or not for incoming data.

	static public class WebConnInfo {
		static public final String EXTRA_URL = "webconn.url";
		static public final String EXTRA_URL_BASIC_AUTH = "webconn.auth_key";
		static public final String EXTRA_CUSTOM_CA_URI = "webconn.custom_ca_uri";
		static public final String EXTRA_EXPECT_SPONTANEOUS_PACKGES = "webconn.expect_spontaneous_packages";

		static public int EXPECT_SPONTANEOUS_PACKGES_NEVER = 0;
		static public int EXPECT_SPONTANEOUS_PACKGES_APP_SETTING = 1;

		public URL url = null;
		public String auth_key = null;
		public Uri custom_ca_uri = null;
		public Integer expect_spontaneous_packages = null;

		public void setUrl(String aUrl) {
			try {
				url = new URL(aUrl);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		public void setCustomCaUri(String aCCU) {
			custom_ca_uri = (aCCU!=null && !"".equals(aCCU)) ? Uri.parse(aCCU) : null;
		}

		public void setAuth(String aUser, String aPass) {
			auth_key = aUser+":"+aPass;
		}

		public WebConnInfo setFromBundle(Bundle aBundle) {
			if (aBundle!=null && aBundle.containsKey(EXTRA_URL)) {
				Object theValue;

				theValue = aBundle.get(EXTRA_URL);
				if (theValue!=null)
					setUrl((String)theValue);

				theValue = aBundle.get(EXTRA_URL_BASIC_AUTH);
				if (theValue!=null)
					auth_key = (String)theValue;

				theValue = aBundle.get(EXTRA_CUSTOM_CA_URI);
				if (theValue!=null)
					setCustomCaUri((String)theValue);

				theValue = aBundle.get(EXTRA_EXPECT_SPONTANEOUS_PACKGES);
				if (theValue!=null)
					expect_spontaneous_packages = (Integer)theValue;
			}
			return this;
		}

		public Bundle toBundle(Bundle aBundle) {
			Bundle theResults = (aBundle!=null) ? aBundle : new Bundle();
			if (url!=null) {
				theResults.putString(EXTRA_URL, url.toString());
				theResults.putString(EXTRA_URL_BASIC_AUTH, auth_key);
				if (custom_ca_uri!=null)
					theResults.putString(EXTRA_CUSTOM_CA_URI, custom_ca_uri.toString());
				if (expect_spontaneous_packages!=null)
					theResults.putInt(EXTRA_EXPECT_SPONTANEOUS_PACKGES, expect_spontaneous_packages);
			}
			return theResults;
		}

		public WebConnInfo setFromIntent(Intent aIntent) {
			if (aIntent!=null)
				setFromBundle(aIntent.getExtras());
			return this;
		}

		static public WebConnInfo fromBundle(Bundle aBundle) {
			return new WebConnInfo().setFromBundle(aBundle);
		}

		public boolean isEmpty() {
			return (url==null || "".equals(url));
		}
	}

	public WebConn(Context aContext) {
		mContext = new WeakReference<Context>(aContext);
	}

	public WebConn(Context aContext, URL aUrl) {
		mContext = new WeakReference<Context>(aContext);
		setUrl(aUrl);
	}

	public WebConn(Context aContext, WebConnInfo aInfo) {
		mContext = new WeakReference<Context>(aContext);
		setUrl(aInfo.url);
		setAuthKey(aInfo.auth_key);
		setCertAuthResource(aInfo.custom_ca_uri);
		expect_spontaneous_packages = aInfo.expect_spontaneous_packages;
	}

	public Context getContext() {
		return (mContext!=null) ? mContext.get() : null;
	}

	public URL getUrl() {
		return mUrl;
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
	 * @throws java.net.MalformedURLException
	 */
	public WebConn setUrl(String aUrlStr) throws MalformedURLException {
		return setUrl(new URL(aUrlStr));
	}

	public String getAuthKey() {
		return mUrlKey;
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

	public Uri getCertAuthResource() {
		return mCAResource;
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
	 * Return web connection info via WebConnInfo class.
	 * @return WebConnInfo Returns the information about this connection.
	 * Returns NULL if the URL is null.
	 */
	public WebConnInfo getWebConnInfo() {
		if (mUrl!=null) {
			WebConnInfo theInfo = new WebConnInfo();
			theInfo.url = mUrl;
			theInfo.auth_key = mUrlKey;
			theInfo.custom_ca_uri = mCAResource;
			return theInfo;
		} else
			return null;
	}

	/**
	 * When using custom CAs, the custom CA chain needs to be loaded and used.
	 * @return Returns the SSLContext with all the certificate magic already loaded.
	 * @throws com.blackmoonit.androidbits.net.ssl.CustomSSLTrustManager.SSLContextException
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
			if (mCAResource!=null && (theUrlConn instanceof HttpsURLConnection)) {
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
	 * @throws java.io.IOException
	 */
	public void writeToUrl(URLConnection aUrlConn, String aDataToSend) throws IOException {
		URLConnection theUrlConn = aUrlConn;
		try {
			theUrlConn.setDoInput(true);
			theUrlConn.setDoOutput(true);
			if (theUrlConn instanceof HttpsURLConnection) {
				((HttpsURLConnection)theUrlConn).setRequestMethod("POST");
			} else if (theUrlConn instanceof HttpURLConnection) {
				((HttpURLConnection)theUrlConn).setRequestMethod("POST");
			}
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
			Log.e(TAG,"writeToUrl",pe);
			/* since we want min SDK as 7, cannot use the preferred constructor.
			if (Build.VERSION.SDK_INT>=9)
				throw new IOException(pe);
			else
				throw new IOException(pe.getMessage());
			*/
			IOException ioe = new IOException();
			ioe.initCause(pe);
		}
	}

	/**
	 * Simple method used to read the URL response and return the text as String.
	 * @param aUrlConn - open URLConnection.
	 * @return Returns the response text as a String.
	 * @throws java.io.IOException
	 */
	public String readResponseFromUrl(URLConnection aUrlConn) throws IOException {
		try {
			InputStream theInStream = aUrlConn.getInputStream();
			try {
				return StreamUtils.inputStreamToString(theInStream);
			} finally {
				theInStream.close();
			}
		} catch (FileNotFoundException fnfe) {
			Integer theResponseCode = null;
			String theResponseMsg = null;
			if (aUrlConn instanceof HttpsURLConnection) {
				theResponseCode = ((HttpsURLConnection) aUrlConn).getResponseCode();
				theResponseMsg = ((HttpsURLConnection) aUrlConn).getResponseMessage();
			} else if (aUrlConn instanceof HttpURLConnection) {
				theResponseCode = ((HttpURLConnection) aUrlConn).getResponseCode();
				theResponseMsg = ((HttpURLConnection) aUrlConn).getResponseMessage();
			}
		    throw new IOException("Bad response: ("+theResponseCode+") "+theResponseMsg);
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
	 * @see com.blackmoonit.androidbits.net.WebConn#uploadJson(java.net.URLConnection, Object, java.lang.reflect.Type)
	 * @see com.blackmoonit.androidbits.net.WebConn#downloadJson(java.net.URLConnection, Class)
	 */
	public <T> T jsonParley(URLConnection aUrlConn, Object aObject, Type aTypeOfObject, Class<T> aResultClass) {
		jsonUpload(aUrlConn, aObject, aTypeOfObject);
		return jsonDownload(aUrlConn, aResultClass);
	}

}
