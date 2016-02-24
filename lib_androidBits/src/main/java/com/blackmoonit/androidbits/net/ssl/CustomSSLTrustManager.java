package com.blackmoonit.androidbits.net.ssl;
/*
 * Copyright (C) 2013 Blackmoon Info Tech Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

/**
 * SSL certs are not always known or they are self signed.
 * In order to load certs for these kinds of connections,
 * you need to use a class like this one that will load
 * the cert and create special TrustManagers with it.
 * @author baracudda
 */
public class CustomSSLTrustManager {
	static private final String TAG = "androidBits."+CustomSSLTrustManager.class.getSimpleName();
	protected Context mContext = null;
	protected Uri mCrtResource = null;
	protected SSLContext mSSLContext = null;

	/**
	 * Exception class thrown if {@link CustomSSLTrustManager#getSSLContext()} fails.
	 */
	static public class SSLContextException extends Exception {
		private static final long serialVersionUID = -2562981353329324044L;

		public SSLContextException(Throwable anException) {
			super("No SSL connection created. msg: "+anException.getMessage());
		}
	}

	public CustomSSLTrustManager(Context aContext, Uri aCrtResource) {
		mContext = aContext;
		mCrtResource = aCrtResource;
	}

	/**
	 * Open the asset or file (based on scheme in Uri) and return stream.
	 * @return Returns the InputStream of the resource/file.
	 * @throws java.io.IOException
	 */
	protected InputStream getCertAuthInputStream() throws IOException {
		InputStream theResult = null;
		String theScheme = mCrtResource.getScheme();
		if ("asset".equals(theScheme)) {
			String theAssetName = mCrtResource.getLastPathSegment();
			theResult = mContext.getAssets().open(theAssetName);
		} else if ("file".equals(theScheme)) {
			theResult = new FileInputStream(mCrtResource.getPath());
		}
		return new BufferedInputStream(theResult);
	}

	/**
	 * Self-signed or unknown CAs need to be loaded in order for SSL to work.
	 * @return Returns an SSLContext that uses our custom TrustManager.
	 * @throws com.blackmoonit.androidbits.net.ssl.CustomSSLTrustManager.SSLContextException
	 * @see <a href="http://developer.android.com/training/articles/security-ssl.html"
	 * >Google Article: Security with HTTPS and SSL</a>
	 */
	public SSLContext getSSLContext() throws SSLContextException {
		if (mSSLContext==null) try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			// Load CAs from an InputStream
			// (could be from a resource or ByteArrayInputStream or ...)
			InputStream theCAinput = getCertAuthInputStream();
			Certificate theCertAuth;
			try {
				theCertAuth = cf.generateCertificate(theCAinput);
			    Log.d(TAG,"loaded CA=" + ((X509Certificate) theCertAuth).getSubjectDN());
			} finally {
				theCAinput.close();
			}

			// Create a KeyStore containing our trusted CAs
			String theKeyStoreType = KeyStore.getDefaultType();
			KeyStore theKeyStore = KeyStore.getInstance(theKeyStoreType);
			theKeyStore.load(null, null);
			theKeyStore.setCertificateEntry("ca", theCertAuth);

			// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(theKeyStore);

			// Create an SSLContext that uses our TrustManager
			mSSLContext = SSLContext.getInstance("TLS");
			mSSLContext.init(null, tmf.getTrustManagers(), null);
		} catch (NoSuchAlgorithmException nsae) {
			throw new SSLContextException(nsae);
		} catch (CertificateException ce) {
			throw new SSLContextException(ce);
		} catch (KeyStoreException kse) {
			throw new SSLContextException(kse);
		} catch (KeyManagementException kme) {
			throw new SSLContextException(kme);
		} catch (IOException ioe) {
			throw new SSLContextException(ioe);
		}
		return mSSLContext;
	}

	public URLConnection getUrlConnection(URL aUrl) throws SSLContextException {
		HttpsURLConnection theUrlConnection = null;
		try {
			theUrlConnection = (HttpsURLConnection)aUrl.openConnection();
			theUrlConnection.setSSLSocketFactory(getSSLContext().getSocketFactory());
		} catch (IOException ioe) {
			throw new SSLContextException(ioe);
		}
		return theUrlConnection;
	}

	public InputStream getInputStream(URL aUrl) throws SSLContextException {
		try {
			URLConnection theUrlConnection = getUrlConnection(aUrl);
			return theUrlConnection.getInputStream();
		} catch (IOException ioe) {
			throw new SSLContextException(ioe);
		}
	}

	static public InputStream getInputStream(Context aContext, Uri aCrtResource, URL aUrl) throws SSLContextException {
		CustomSSLTrustManager theManager = new CustomSSLTrustManager(aContext, aCrtResource);
		return theManager.getInputStream(aUrl);
	}

}
