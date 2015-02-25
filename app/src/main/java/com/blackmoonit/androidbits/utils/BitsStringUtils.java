package com.blackmoonit.androidbits.utils;

import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * String Utilities.
 *
 * @author Ryan Fischbach
 */
public final class BitsStringUtils {

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


}
