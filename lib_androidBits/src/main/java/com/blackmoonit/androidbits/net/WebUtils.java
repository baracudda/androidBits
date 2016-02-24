package com.blackmoonit.androidbits.net;
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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.google.bits_gson.Gson;
import com.google.bits_gson.JsonSyntaxException;

/**
 * JSON utility functions utilizing GSON library (Google's JSON library).
 * @author baracudda
 */
public final class WebUtils {
	static private final String TAG = "androidBits."+WebUtils.class.getSimpleName();

	static public final String WEB_CHARSET = "UTF-8";

	private WebUtils() {}

	/**
	 * Convert an instanced object to a JSON string.
	 * @param aObject - object instance with data to convert to string.
	 * @param aTypeOfObject - type of aObject (usually aObject's class).
	 * @return Returns aObject converted to a JSON string.
	 */
	static public String toJson(Object aObject, Type aTypeOfObject) {
		Gson gson = new Gson();
		return gson.toJson(aObject,aTypeOfObject);
	}

	/**
	 * Convert an instanced object to a JSON string.
	 * @param aObject - object instance with data to convert to string.
	 * @return Returns aObject converted to a JSON string.
	 */
	static public String toJson(Object aObject) {
		Gson gson = new Gson();
		return gson.toJson(aObject,aObject.getClass());
	}

	/**
	 * Convert a JSON string to a particular Java class.
	 * @param aJsonStr - string used as the JSON source.
	 * @param aResultClass - resulting object class.
	 * @return Returns the result object class with data filled in from JSON string.
	 * Returns NULL on parsing failure.
	 */
	static public <T> T fromJson(String aJsonStr, Class<T> aResultClass) {
		Gson gson = new Gson();
		try {
			return gson.fromJson(aJsonStr,aResultClass);
		} catch (JsonSyntaxException jse) {
			Log.e(TAG,"fromJson",jse);
		}
		return null;
	}

	/**
	 * In order to send up POST variables, you need to create a list of name=value pairs.
	 * This helper method lets you chain calls together to create a simple POST var list.
	 * @param aParamList - if NULL, will create a new list.
	 * @param aName - POST variable name.
	 * @param aValue - POST variable value.
	 * @return Returns the aParamList for chaining purposes.
	 */
	static public List<NameValuePair> addParam(List<NameValuePair> aParamList, String aName, String aValue) {
		List<NameValuePair> theParams = (aParamList!=null) ? aParamList : new ArrayList<NameValuePair>();
		theParams.add(new BasicNameValuePair(aName,aValue));
		return theParams;
	}

	/**
	 * Convert a parameter list of NameValuePairs into the string used to POST to a URL.
	 * @param aParamList - POST variable param list.
	 * @return
	 */
	static public String cnvPostParamsToString(List<NameValuePair> aParamList) {
		String theResult = "";
		if (aParamList.size()>0) try {
			for (NameValuePair thePair : aParamList) {
				theResult = theResult + URLEncoder.encode(thePair.getName(),WEB_CHARSET)+"="+
						URLEncoder.encode(thePair.getValue(),WEB_CHARSET)+"&";
			}
			theResult = theResult.substring(0,theResult.length()-1);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,"cnvPostParamsToString",e);
		}
		return theResult;
	}

	/**
	 * Get the standard "Basic" http auth string used with the standard "Authorization" header.
	 * @param aUserPwString - "user:pw" style string.
	 * @return Returns the Base64 encoded string to be used with the "Authorization" header.
	 */
	static public String getBasicAuthString(String aUserPwString) {
		return "Basic "+Base64.encodeToString(aUserPwString.getBytes(), Base64.NO_WRAP);
	}

	/**
	 * Get the standard "Basic" http auth string used with the standard "Authorization" header.
	 * @param aUserName - the user name
	 * @param aPasswordEntry - the password to use
	 * @return Returns the Base64 encoded string to be used with the "Authorization" header.
	 * @see WebUtils#getBasicAuthString(String)
	 */
	static public String getBasicAuthString(String aUserName, String aPasswordEntry) {
		return getBasicAuthString(aUserName+":"+aPasswordEntry);
	}

	/**
	 * Create a new email Intent based on single recipient vs multiple recipients.
	 * @param aToLine - single string of emails separated by "," or ";" or " ".
	 * @return Returns the Intent to use.
	 */
	static public Intent newEmailIntent(String aToLine) {
		String[] theRecipients = null;
		String theToLine = aToLine+"";
		String[] theSplitters = { ", ", "; ", " ", "," };
		for (String theSplitter : theSplitters) {
			if (theToLine.contains(theSplitter)) {
				theRecipients = theToLine.split(theSplitter);
				break;
			}
		}
		if (theRecipients==null) {
			theRecipients = theToLine.split(";");
		}
		Intent theIntent;
		if (theRecipients.length==1 && !theRecipients[0].equals("")) {
			theIntent = new Intent(Intent.ACTION_SENDTO);
			theIntent.setData(Uri.fromParts("mailto", theRecipients[0], null));
		} else {
			theIntent = new Intent(Intent.ACTION_SEND);
			theIntent.setType("message/rfc822");
			theIntent.putExtra(Intent.EXTRA_EMAIL,theRecipients);
		}
		return theIntent;
	}



}
