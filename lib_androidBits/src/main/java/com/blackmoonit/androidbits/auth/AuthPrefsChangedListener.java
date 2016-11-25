package com.blackmoonit.androidbits.auth;
/*
 * Copyright (C) 2016 Blackmoon Info Tech Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import com.blackmoonit.androidbits.R;
import com.blackmoonit.androidbits.app.UITaskRunner;

/**
 * Apps that use the Broadway Auth mechanism may need to use their own AppPrefs object that
 * is not descended from AuthPrefs/_v11 classes.  They can register to use this listener so
 * that all AppPref classes listen/react to the same change the same way.
 */
public class AuthPrefsChangedListener
	implements SharedPreferences.OnSharedPreferenceChangeListener
{
	//static private final String TAG = AuthPrefsChangedListener.class.getSimpleName();

	protected final String SETTINGS_CHANGED_INTENT;
	protected final Context mContext;

	public interface TaskToValidateURL
	{
		boolean onURLChange(Context aContext, SharedPreferences aSettings);
	}
	protected final TaskToValidateURL mTaskToValidateURL;

	public AuthPrefsChangedListener( Context aContext, TaskToValidateURL aTask ) {
		mContext = aContext;
		SETTINGS_CHANGED_INTENT = aContext.getString(R.string.ACTION_prefs_auth_settings_changed);
		mTaskToValidateURL = aTask;
	}

	/**
	 * Update the URL Validated setting.
	 * @param aSettings - the settings handle.
	 * @param bUrlValidated - NULL means "in progress" (entryValue=2).
	 */
	public void updateUrlValidated(SharedPreferences aSettings, Boolean bUrlValidated) {
		Context theContext = mContext;
		String[] theValidatedEntryValues = theContext.getResources().getStringArray(
				R.array.pref_entryvalues_server_url_validated);
		int idx = 0;
		if (bUrlValidated==null)
			idx = 2;
		else if (bUrlValidated)
			idx = 1;
		String theSettingValue = theValidatedEntryValues[idx];
		String thePrefKey = theContext.getString(R.string.pref_key_server_url_validated);
		aSettings.edit().putString(thePrefKey, theSettingValue).commit();
	}

	/**
	 * Called when settings have either changed or are being initialized on start.
	 * @param aSettings - the settings handle.
	 * @param aPrefKey - the key name of what changed, or NULL if all need to be checked.
	 */
	public void onSharedPreferenceChanged(final SharedPreferences aSettings, final String aPrefKey) {
		//Log.d(TAG, "onSharedPreferenceChanged! key=" + aPrefKey);
		final Context theContext = mContext;
		if (aPrefKey==null || aPrefKey.equals(theContext.getString(R.string.pref_key_server_url))) {
			updateUrlValidated(aSettings, null);
			//validate the new url setting and display toast on succeed/fail
			(new AsyncTask<SharedPreferences, Object, Boolean>(){

				@Override
				protected Boolean doInBackground(SharedPreferences... params) {
					SharedPreferences theSettings = null;
					if (params!=null && params.length>0)
						theSettings = params[0];
					return (mTaskToValidateURL!=null) &&
						mTaskToValidateURL.onURLChange( theContext, theSettings );
				}

				@SuppressLint("ShowToast")
				@Override
				protected void onPostExecute(Boolean bUrlValidated) {
					if (theContext!=null) {
						updateUrlValidated(aSettings, bUrlValidated);
						if (bUrlValidated) {
							UITaskRunner.showToast(Toast.makeText(theContext,
									R.string.msg_url_validated, Toast.LENGTH_SHORT));
						} else {
							UITaskRunner.showToast(Toast.makeText(theContext,
									R.string.msg_url_validate_failed, Toast.LENGTH_LONG));
						}
					}
				}

			}).execute(aSettings);
		}
		theContext.sendBroadcast(new Intent(SETTINGS_CHANGED_INTENT));
	}

}
