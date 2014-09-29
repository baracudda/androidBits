package com.blackmoonit.app;

import android.util.Log;

/**
 * Adds static bDebugMode var along with some methods related to it.
 * @see AppPreferenceBase
 */
public class AppPrefsDebug extends AppPreferenceBase {
	static protected Boolean bDebugMode = null;

	@Override
	protected void setup() {
		//nothing to do here
	}
	
	static public boolean isDebugMode() {
		return (bDebugMode != null && bDebugMode == true);
	}
	
	static public void setDebugMode(Boolean value) {
		bDebugMode = value;
	}
	
	static public void debugLog(String tag, String message) {
		if (isDebugMode()) {
			Log.i(tag, message);
		}
	}

}
