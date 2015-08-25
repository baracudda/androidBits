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

import android.annotation.TargetApi;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * AppPreferenceBase as a Fragment loader Activity, basically
 * uses a stub layout to change out with PreferenceFragments.
 */
@TargetApi(11)
abstract public class AppPrefsAsFragment extends AppPreferenceBase {

	@Override
	protected void loadPrefLayouts() {
		//fragments will replace the default content view, no setContentView() needed.
		//also, fragments will now call addPreferencesFromResource(), not us.
	}

	/**
	 * Create a Bundle and return the name of the layout to load by using the key
	 * {@link com.blackmoonit.androidbits.app.AppPreferenceBase.PreferencePane#ARG_LAYOUT_RESOURCE_ID ARG_LAYOUT_RESOURCE_ID}
	 * @return Returns a bundle filled with PreferenceFragment arguments.
	 */
	abstract protected Bundle getFragmentArguments();

	/**
	 * Sets the arguments for the parameter and then loads the fragment.
	 * @param aPrefsFragment - the preference fragment object (usually newly created).
	 */
	protected void setFragmentContent(PreferenceFragment aPrefsFragment) {
		if (aPrefsFragment!=null) {
			aPrefsFragment.setArguments(getFragmentArguments());
			// Display the fragment as the main content.
			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, aPrefsFragment)
					.commit();
		}
	}

}
