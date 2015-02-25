package com.blackmoonit.androidbits.app;

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
