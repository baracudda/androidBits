package com.blackmoonit.androidbits.utils;
/*
 * Copyright (C) 2014 Blackmoon Info Tech Services
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

import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * String Utilities.
 *
 * @author baracudda
 */
public final class BitsStringUtils {

	private BitsStringUtils() {} //do not instanciate this class

	/**
	 * Trim function that converts null string into "".
	 *
	 * @param s - string to trim, null trims to "".
	 * @return Return "" if null, else s.trim()
	 */
	static public String trim(String s) {
		return (s!=null)?s.trim():"";
	}

	/**
	 * Get the MD5 checksum, which is a 32 char string.
	 * @param s - string requiring the checksum
	 * @return Returns the 32 character MD5 checksum.
	 */
	static public String md5sum(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] theHashValue = md.digest(s.getBytes());
			BigInteger theHashNum = new BigInteger(1,theHashValue);
			String theMD5str = theHashNum.toString(16);
			int theLenDiff = 32-theMD5str.length();
			if (theLenDiff>0) {
				char[] thePad = new char[theLenDiff];
				Arrays.fill(thePad,'0');
				theMD5str = new String(thePad)+theMD5str;
			}
			return theMD5str;
		} catch (NoSuchAlgorithmException e) {
			Log.e("MD5", e.getMessage());
			return null;
		}
	}

	/**
	 * Null-safe equal check. Similar to the proposed === operator.
	 * @param s1 - string to test
	 * @param s2 - string to test
	 * @return Returns true if both strings are equal or both null.
	 */
	static public boolean isEqual(String s1, String s2) {
		//TRUE if both null or if s1 isn't null and checked against s2 (which may be null)
		return (s1==s2 || (s1!=null && s1.equals(s2)));
	}

	/**
	 * Null-safe empty check. Not all Android versions included this routine.
	 * @param aString - string to test for null or ""
	 * @return Returns TRUE if null or "".
	 */
	static public boolean isEmpty(String aString) {
		return (aString==null || aString.equals(""));
	}

	/**
	 * Java does not have a built-in PHP-implode-like function until Java 8.
	 * @param separator - the separator string to use.
	 * @param data - the String[] or data to implode.
	 * @return Returns the data imploded as one string.
	 */
	static public String implode(String separator, String... data) {
		StringBuilder sb = new StringBuilder();
		//data.length - 1 => to not add separator at the end
		for (int i = 0; i < data.length - 1; i++) {
			if (!data[i].matches(" *")) {//empty string are ""; " "; "  "; and so on
				sb.append(data[i]);
				sb.append(separator);
			}
		}
		sb.append(data[data.length - 1].trim());
		return sb.toString();
	}

	/**
	 * Java does not have a built-in PHP-implode-like function until Java 8.
	 * @param separator - the separator string to use.
	 * @param data - the String[] or data to implode.
	 * @return Returns the data imploded as one string.
	 */
	static public String implode(String separator, List<String> data) {
		if (data==null || data.size()<1) return "";
		StringBuilder sb = new StringBuilder();
		//data.size() - 1 => to not add separator at the end
		for (int i = 0; i < data.size() - 1; i++) {
			String theStrSegment = data.get(i);
			if (!theStrSegment.matches(" *")) {//empty string are ""; " "; "  "; and so on
				sb.append(theStrSegment);
				sb.append(separator);
			}
		}
		sb.append(data.get(data.size() - 1).trim());
		return sb.toString();
	}

	/**
	 * Construct the parameter list for use in a query's IN clause.
	 * @param data - the String[] or data to implode.
	 * @return Returns the placeholders created as one string.
	 */
	static public String implodeAsQueryPlaceholders(List<String> data) {
		if (data==null || data.size()<1) return "";
		StringBuilder sb = new StringBuilder();
		//data.size() - 1 => to not add separator at the end
		for (int i = 0; i < data.size() - 1; i++) {
			sb.append("?, ");
		}
		sb.append("?");
		return sb.toString();
	}

}
