package com.blackmoonit.androidbits.app;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.blackmoonit.androidbits.R;

/**
 * Standard application preference class. v11+ defines a preference-headers xml file that references the
 * other preference xml files.<br>
 * <br>
 * REQUIRED RESOURCES:<ul>
 * <li>R.array.pref_screen_definitions - an array list of PreferenceScreen definitions to use. </li>
 * <li>layout.app_pref_headers | xml.app_pref_headers | override
 * getmAppPrefHeaderDefinitionResourceId() - Preference Header definition xml for
 * Android v11+ on large screens (tablets, TVs, widescreen big phones, etc.)</li>
 * <li>string.prefs_name - filename of prefs (optional, uses getPackageName()+".prefs" otherwise).</li>
 * </ul>
 * @author Ryan Fischbach
 */
public abstract class AppPreferenceBase extends PreferenceActivity {
	protected int[] mAppPrefDefinitionsResourceIds;
	protected int mAppPrefHeaderDefinitionResourceId = 0;
	protected Method mLoadHeaders = null;
	protected Method mHasHeaders = null;
	protected Method mRecreate = null;
	static protected ArrayList<String> mAllowedFragments = new ArrayList<String>();

	protected static int getResId(Context aContext, String aResType, String aResName) {
		return aContext.getApplicationContext().getResources().getIdentifier(aResName,
				aResType, aContext.getPackageName());
	}

	protected int getResId(String aResType, String aResName) {
		return getResId(this,aResType, aResName);
	}

	/**
	 * Checks to see if using new v11+ way of handling PrefFragments.
	 * @return Returns false pre-v11, else checks to see if using headers.
	 */
	public boolean isNewV11Prefs() {
		if (mHasHeaders!=null && mLoadHeaders!=null) {
			try {
				return (Boolean)mHasHeaders.invoke(this);
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
		return false;
	}

	/**
	 * Wrapper for {@link #invalidateHeaders()}.
	 */
	public void recreatePrefsView() {
		if (mRecreate!=null) {
			try {
				mRecreate.invoke(this);
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
	}

	/**
	 * App preference header definition resource id used to display the headers for settings.
	 * Primarily used for tablet layouts or widescreen usage.
	 * @return Returns the int containing all the R ID required to load the XML definition.
	 */
	public int getPreferenceHeaderDefinitionResourceId() {
		int theRes = getResId("layout","app_pref_headers");
		if (theRes==0) {
			theRes = getResId("xml","app_pref_headers");
		}
		return theRes;
	}

	@Override
	public void onCreate(Bundle aSavedState) {
		mAppPrefDefinitionsResourceIds = getResourceArray(getApplicationContext(), R.array.pref_screen_definitions);
		if (mAppPrefDefinitionsResourceIds!=null)
		mAppPrefHeaderDefinitionResourceId = getPreferenceHeaderDefinitionResourceId();
		try {
			mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class);
			mHasHeaders = getClass().getMethod("hasHeaders");
			mRecreate = getClass().getMethod("recreate");
		} catch (NoSuchMethodException e) {
		}
		super.onCreate(aSavedState);
		convertOldPrefs();
		if (!isNewV11Prefs()) {
			loadPrefLayouts();
		}
		setDefaultPrefs(this);
		setup();
	}

	/**
	 * Called before the prefs are loaded from resource.
	 */
	protected void convertOldPrefs() {
		//default no-op
	}

	protected int[] getPrefResources() {
		return mAppPrefDefinitionsResourceIds;
	}

	@SuppressWarnings("deprecation")
	protected void loadPrefLayouts() {
		for (int thePrefResId:getPrefResources()) {
			if (thePrefResId!=0) {
				addPreferencesFromResource(thePrefResId);
			}
		}
	}

	/**
	 * v11+ way to loadPrefLayouts via an xml file with preference-header tags.
	 */
	@Override
	public void onBuildHeaders(List<Header> aTarget) {
		if (mAppPrefHeaderDefinitionResourceId ==0 || mLoadHeaders==null)
			return;
		try {
			mLoadHeaders.invoke(this,new Object[]{mAppPrefHeaderDefinitionResourceId,aTarget});
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
	}

	protected abstract void setup();

	/**
	 * Loads the prefs file.
	 * @return Returns the SharedPreferences class.
	 */
	public SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(this);
	}

	/**
	 * Static method used to loads the prefs file.
	 *
	 * @param aContext - the Context
	 * @return Returns the SharedPreferences class.
	 */
	static public SharedPreferences getPrefs(Context aContext) {
		return PreferenceManager.getDefaultSharedPreferences(aContext);
	}

	/**
	 * Create pref entries and fill them with defined default values.<br>
	 * NOTE: does not work if using a boolean, the default entry will not
	 * be created in Android 2.x. A call to ".getBoolean(KEY, true)" will
	 * always return true. For Android 4.x it works.
	 * @param aContext - context to use.
	 * @param aPrefResourceIds - R.xml pref layout file IDs.
	 * @param bResetPrefs - Use FALSE to only perform this if the entry does not exist yet.
	 */
	static public void setDefaultPrefs(Context aContext, int[] aPrefResourceIds, boolean bResetPrefs) {
		for (int thePrefResId:aPrefResourceIds) {
			PreferenceManager.setDefaultValues(aContext,thePrefResId,bResetPrefs);
		}
	}

	/**
	 * Call this method in main activity's onCreate (requires preference layouts defined in pref_layouts int-array).
	 * @param aContext - main activity
	 */
	static public void setDefaultPrefs(Context aContext) {
		setDefaultPrefs(aContext,getResourceArray(aContext, R.array.pref_screen_definitions),false);
	}

	/**
	 * Erases all preferences, thus causing all references to use their default values.
	 */
	static public void clearPrefs(Context aContext, int[] aPrefResIds) {
		SharedPreferences.Editor theEditor = getPrefs(aContext).edit().clear();
		if (aPrefResIds!=null) {
			setDefaultPrefs(aContext,aPrefResIds,true);
		}
		theEditor.commit();
	}

	@SuppressWarnings("deprecation")
	protected void resetPrefScreens() {
		if (isNewV11Prefs()) {
			recreatePrefsView();
		} else {
			setPreferenceScreen(null);
			loadPrefLayouts();
		}
	}

	public void resetPrefs() {
		clearPrefs(this,getPrefResources());
		resetPrefScreens();
		setup();
	}

	/**
	 * PreferencePane (or its like) push what needs to be allowed.
	 * @param aFragmentName - name of fragment
	 */
	static public synchronized void addAllowedFragment(String aFragmentName) {
		mAllowedFragments.add(aFragmentName);
	}

	/**
	 * Get all currently allowed fragments.
	 * @return Returns the list of allowed fragment names.
	 */
	static public synchronized ArrayList<String> getAllowedFragments() {
		return mAllowedFragments;
	}

	/**
	 * Remove a fragment from the list of allowed fragments.
	 * @param aFragmentName - name of fragment
	 */
	static public synchronized void removeAllowedFragment(String aFragmentName) {
		int i = mAllowedFragments.indexOf(aFragmentName);
		if (i>=0)
			mAllowedFragments.remove(i);
	}

	/**
	 * Determine if the fragment name is allowed based on the internal allowed list.
	 * @param aFragmentName - name of fragment.
	 * @return Returns TRUE if the fragment name is in the allowed list.
	 */
	static public synchronized boolean isFragmentAllowed(String aFragmentName) {
		return (mAllowedFragments.indexOf(aFragmentName)>=0);
	}

/**
	 * Introduced in API 19, either override this method or
	 * use the PreferencePane class defined below as PrefHeaders.
	 * REASON: Security fix for Android.<br>
	 * Subclasses should override this method and verify that the given fragment
	 * is a valid type to be attached to this activity. The default implementation
	 * returns true for apps built for android:targetSdkVersion older than KITKAT.
	 * For later versions, it will throw an exception.
	 * @param aFragmentName - fragment about to be loaded
	 * @return Returns TRUE if allowed.
	 */
	@Override
	protected boolean isValidFragment(String aFragmentName) {
		//no need to call the super method as it will throw an exception.
		boolean b;
		//if you are using the built-in PreferencePane, it only loads app defined
		//  fragments; anything defined in our own app is trusted by default.
		b = "AppPreferenceBase$PreferencePane".equals(aFragmentName);
		//if the fragment name is not our built-in PreferenceFragment, check
		//  the static list of allowed fragments.
		return b || isFragmentAllowed(aFragmentName);
	}

	/**
	 * Generic preference fragment that will load up layouts defined in
	 * "layout" or "XML" res folders. e.g. <pre>
	 * &lt;header android:fragment="AppPreferenceBase$PreferencePane"
	 * 	 android:title="@string/prefs_category_area1"&gt;
	 *     &lt;extra android:name="layout-res" android:value="prefs_settings_area_1" /&gt;
	 * &lt;/header&gt;
	 * </pre> will load up the resource named "prefs_settings_area_1" from R.layout or
	 * R.xml if it fails to find it there.<br>
	 * <br>
	 * Added benefit is that these PreferenceFragments will be auto-allowed when using a
	 * descendent of AppPreferenceBase and targetting API 19+.
	 */
	/* supress Unused */
	@TargetApi(11)
	static public class PreferencePane extends PreferenceFragment {
		/**
		 * Fragment argument used to load the layout named by it as a PreferenceFragment.
		 */
		static public String ARG_LAYOUT_RESOURCE_ID = "layout-res";

		@Override
		public void onCreate(Bundle aSavedState) {
			super.onCreate(aSavedState);
			Context anAct = getActivity();
			Bundle args = getArguments();
			String thePrefScreenLayoutName = (args!=null) ? args.getString(ARG_LAYOUT_RESOURCE_ID) : null;
			if (thePrefScreenLayoutName!=null && !thePrefScreenLayoutName.equals("")) {
				int thePrefRes = anAct.getResources().getIdentifier(thePrefScreenLayoutName,
						"layout",anAct.getPackageName());
				//check xml folder if not in the layout folder
				if (thePrefRes==0) {
					thePrefRes = anAct.getResources().getIdentifier(thePrefScreenLayoutName,
							"xml",anAct.getPackageName());
				}
				if (thePrefRes!=0) {
					addPreferencesFromResource(thePrefRes);
				}
			}
		}

		/**
		 * Check aPrefKey for NULL or if equal to the string given by the resource ID.
		 * @param aPrefKey - the Preference Key to check.
		 * @param aPrefKeyResourceId - the resource ID of the string to compare against.
		 * @return Returns TRUE of we should process this preference key during onChanged.
		 */
		public boolean isPrefKey(String aPrefKey, int aPrefKeyResourceId) {
			return (aPrefKey==null || aPrefKey.equals(getActivity().getString(aPrefKeyResourceId)));
		}
	}

	/**
	 * Returns the current application's friendly version name.
	 *
	 * @param aContext - application context
	 * @return
	 *      Returns the version name as defined by the application. Appends an "*" if the
	 *      application is also debuggable.
	 */
	static public String getAppVersionName(Context aContext) {
		String theResult = "e^πi-1";
		if (aContext!=null) {
			PackageManager pm = aContext.getPackageManager();
			PackageInfo pi;
			try {
				pi = pm.getPackageInfo(aContext.getPackageName(),0);
				theResult = pi.versionName;
				if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE)!=0) {
					theResult += "*";
				}
			} catch (NameNotFoundException e) {
				//use default result if there was a nnfe
			}
		}
		return theResult;
	}

	/**
	 * Loads an int-array resource and creates the array of
	 * integer resources they represent. Android did not
	 * historically handle this use-case automatically.
	 * @param aContext - context used to getResources().
	 * @return Returns a new int[] with the proper resource IDs.
	 */
	static public int[] getResourceArray(Context aContext, int aResArray) {
		TypedArray theResArray = aContext.getResources().obtainTypedArray(aResArray);
		int iMax = theResArray.length();
		int[] theResIds = new int[iMax];
		for (int i=0; i<iMax; i++) {
			theResIds[i] = theResArray.getResourceId(i,0);
		}
		theResArray.recycle();
		return theResIds;
	}


}
