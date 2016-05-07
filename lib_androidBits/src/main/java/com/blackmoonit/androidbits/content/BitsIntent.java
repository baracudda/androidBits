package com.blackmoonit.androidbits.content;
/*
 * Copyright (C) 2015 Blackmoon Info Tech Services
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

import java.io.File;
import java.lang.reflect.Field;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Android Intent related functions and definitions backward compatible with 1.5+
 */
public class BitsIntent extends Intent {
	private static Field mActionSendMultipleField = null;
	private static final String cIntent_ACTION_SEND_MULTIPLE = "android.intent.action.SEND_MULTIPLE";
	private static final String cIntent_EXTRA_FROM_COMPONENT_NAME = "android.intent.extra.from_component_name";
	private static final String cIntent_EXTRA_REFERRER_NAME = "android.intent.extra.REFERRER_NAME";
	/**
	 * String extra containing the path to a folder.
	 */
	static public final String EXTRA_DIRECTORY = "com.blackmoonit.intent.extra.DIRECTORY";
	/**
	 * Boolean extra indicating the data itself must be returned, usually via {@link #putExtra(String, android.os.Parcelable)}.
	 * Do not rely on it's presence to mean true or false and be sure to read it's value to be certain of
	 * what is intended.
	 */
	static public final String EXTRA_RETURN_DATA = "return-data";
	/**
	 * Parcelable extra returning the actual data, usually requested with {@link #EXTRA_RETURN_DATA}.
	 */
	static public final String EXTRA_DATA = "data";
	/**
	 * Used to indicate that image data requested needs to be returned with an X value of this extra.
	 */
	static public final String EXTRA_OUTPUTX = "outputX";
	/**
	 * Used to indicate that image data requested needs to be returned with a Y value of this extra.
	 */
	static public final String EXTRA_OUTPUTY = "outputY";
	/**
	 * Used by Android 4.0 devices instead of {@link #EXTRA_RETURN_DATA}.
	 */
	static public final String EXTRA_OUTPUT = "output";
	/**
	 * Open the Quick Search Box settings.
	 */
	static public final String ACTION_QSB_SETTINGS = "android.search.action.SEARCH_SETTINGS";
	/**
	 * OpenIntents action for picking a folder
	 */
	static public final String ACTION_PICK_DIRECTORY = "org.openintents.action.PICK_DIRECTORY";

	//Intent scheme possibilities
	static public final String SCHEME_FILE = "file";
	static public final String SCHEME_FOLDER = "folder";
	static public final String SCHEME_DIRECTORY = "directory";
	static public final String SCHEME_CONTENT = "content";
	/**
	 * Boolean extra indicating if multiples are allowed.  e.g. pick multiple files
	 */
	static public final String EXTRA_ALLOW_MULTIPLE = "com.blackmoonit.extra.ALLOW_MULTIPLE";
	static public final String ACTION_OI_PICK_FILE = "org.openintents.action.PICK_FILE";
	static public final String ACTION_OI_PICK_FOLDER = "org.openintents.action.PICK_DIRECTORY";
	static public final String EXTRA_OI_TITLE = "org.openintents.extra.TITLE";
	static public final String EXTRA_OI_BUTTON_TEXT = "org.openintents.extra.BUTTON_TEXT";
	/**
	 * String extra indicating a default URI to use for whatever.
	 */
	static public final String EXTRA_DEFAULT_URI = "com.blackmoonit.extra.DEFAULT_URI";

	static {
		initCompatibility();
	};

	private static void initCompatibility() {
		/* example method
		try {
			mDebug_dumpHprofData = Debug.class.getMethod("dumpHprofData", new Class[] { String.class } );
		} catch (NoSuchMethodException nsme) {
			//failure, must be older device
		}
		*/
		try {
			mActionSendMultipleField = BitsIntent.class.getField("cIntent_ACTION_SEND_MULTIPLE");
			mActionSendMultipleField = Intent.class.getField("ACTION_SEND_MULTIPLE");
		} catch (NoSuchFieldException e) {
			//leave the fields set to their initial values
		}
	}

	/**
	 * Used for backward compatibility with Android 1.5
	 *
	 * @return Returns value of ACTION_SEND_MULTIPLE.
	 */
	static public String Intent_ACTION_SEND_MULTIPLE() {
		try {
			return (String)mActionSendMultipleField.get(null);
		} catch (Exception e) {
			return cIntent_ACTION_SEND_MULTIPLE;
		}
	}

	/**
	 * Used for backward compatibility with Android 1.5
	 *
	 * @return Returns value of EXTRA_FROM_COMPONENT_NAME.
	 */
	static public String Intent_EXTRA_FROM_COMPONENT_NAME() {
		return cIntent_EXTRA_FROM_COMPONENT_NAME;
	}

	/**
	 * Used for backward compatibility with Android 1.5
	 *
	 * @return Returns value of EXTRA_REFERRER_NAME.
	 */
	static public String Intent_EXTRA_REFERRER_NAME() {
		return cIntent_EXTRA_REFERRER_NAME;
	}

	/**
	 * Convenience function to set who the intent was from.
	 *
	 * @param aIntent - the intent to place the data
	 * @param aActName - the activity providing the data
	 */
	static public void setFromExtra(Intent aIntent, String aActName) {
		if (aIntent!=null) {
			aIntent.putExtra(Intent_EXTRA_FROM_COMPONENT_NAME(),aActName);
		}
	}

	/**
	 * Get the intent sender component information if it exists.
	 *
	 * @param aIntent - the intent to extract the information from.
	 * @return Returns the "from" information, if present, null otherwise.
	 */
	static public String getFromExtra(Intent aIntent) {
		if (aIntent!=null)
			return aIntent.getStringExtra(Intent_EXTRA_FROM_COMPONENT_NAME());
		else
			return null;
	}

	/**
	 * Get the intent's action, if any
	 *
	 * @param aIntent - the intent to extract the information from.
	 * @return Returns the action defined in the intent
	 */
	static public String getIntentAction(Intent aIntent) {
		if (aIntent!=null)
			return aIntent.getAction();
		else
			return null;
	}

	/**
	 * Convenience function to test the sender component information with aActName.
	 *
	 * @param aIntent - the intent which may contain sender information
	 * @param aActName - the string we wish to test against
	 * @return Returns true if the intent contains sender information and it equals aActName.
	 * (case sensitive)
	 */
	static public boolean isIntentFrom(Intent aIntent, String aActName) {
		String theFrom = getFromExtra(aIntent);
		return (aIntent!=null) && (aActName!=null) && (theFrom!=null) && aActName.startsWith(theFrom);
	}

	/**
	 * Check to see if  Quick Search Box settings activity exists.
	 *
	 * @param aContext - context used to {@link android.content.Context#startActivity(android.content.Intent)}
	 */
	static public boolean existsQSBsettings(Context aContext) {
		Intent theIntent = new Intent(ACTION_QSB_SETTINGS);
		return (aContext.getPackageManager().queryIntentActivities(theIntent,PackageManager.MATCH_DEFAULT_ONLY).size()>0);
	}

	/**
	 * Launch the Quick Search Box settings activity.
	 *
	 * @param aContext - context used to {@link android.content.Context#startActivity(android.content.Intent)}
	 */
	static public void launchQSBsettings(Context aContext) {
		if (existsQSBsettings(aContext)) {
			Intent theIntent = new Intent(ACTION_QSB_SETTINGS);
			aContext.startActivity(theIntent);
		}
	}

	/**
	 * Checks to see if the passed in scheme is for a file.
	 *
	 * @param aScheme - Scheme part of a Uri/Intent
	 * @return Returns true if the scheme is a file intent. If null, it will return false.
	 */
	static public boolean isSchemeFile(String aScheme) {
		return (aScheme!=null && aScheme.equalsIgnoreCase(SCHEME_FILE));
	}

	/**
	 * Checks to see if the passed in Intent contains a scheme for a file.
	 *
	 * @param aIntent - the intent to check
	 * @return Returns true if the scheme is a file intent. If null, it will return false.
	 */
	static public boolean isSchemeFile(Intent aIntent) {
		return (aIntent!=null && isSchemeFile(aIntent.getScheme()));
	}

	/**
	 * Checks to see if the passed in scheme is for a folder.
	 *
	 * @param aScheme - Scheme part of a Uri/Intent
	 * @return Returns true if the scheme is a folder intent. If null, it will return false.
	 */
	static public boolean isSchemeFolder(String aScheme) {
		return (aScheme!=null &&
				(aScheme.equalsIgnoreCase(SCHEME_FOLDER) ||
				 aScheme.equalsIgnoreCase(SCHEME_DIRECTORY)));
	}

	/**
	 * Checks to see if the passed in Intent contains a scheme for a folder.
	 *
	 * @param aIntent - the intent to check
	 * @return Returns true if the scheme is a folder intent. If null, it will return false.
	 */
	static public boolean isSchemeFolder(Intent aIntent) {
		return (aIntent!=null && isSchemeFolder(aIntent.getScheme()));
	}

	/**
	 * Return the Uri to use to view a file, special handling for folders
	 * @param aFile - a file or folder
	 * @return Returns the Uri used by the Intent mechanism to view the contents of a file.
	 * Due to how some apps abuse the intent-filter mechanism, I have had to insist on using
	 * a separate schema for folders so that my browser will continue to try to open them in
	 * order to see their contents (rather than try to zip them up or perform PGP on them or w/e).
	 * I still maintain that ACTION_SEND should be used for manipulation like that instead of VIEW.
	 */
	static public Uri getViewFileUri(File aFile) {
		if (aFile!=null)
			return Uri.parse(((aFile.isDirectory())?SCHEME_DIRECTORY:SCHEME_FILE)+"://"+aFile.getPath());
		else
			return null;
	}

	/**
	 * Same as the File param version.
	 * @param aPath - string representing path to file/folder
	 * @return Returns file Uri for files, folder Uri for folders.
	 * @see #getViewFileUri(java.io.File)
	 */
	static public Uri getViewFileUri(String aPath) {
		return getViewFileUri(new File(aPath));
	}


}
