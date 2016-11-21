package com.blackmoonit.androidbits.app;
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
		return (bDebugMode != null && bDebugMode);
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
