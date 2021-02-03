package com.blackmoonit.androidbits.utils;
/*
 * Copyright (C) 2015 Blackmoon Info Tech Services
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

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility functions that deal mainly with app-wide settings or values.
 * @author baracudda
 */
@SuppressWarnings("unused")
public final class BitsAppUtils {

	private BitsAppUtils() {} //do not instantiate this class

	/**
	 * API 9+, ANDROID_ID, may be NULL or may be non-unique across a mfg model (known bugs).
	 * A 64-bit number (as a hex string) that is randomly generated when the user first
	 * sets up the device and should remain constant for the lifetime of the user's device.
	 * The value may change if a factory reset is performed on the device.
	 * Note: When a device has multiple users (available on certain devices running Android 4.2
	 * or higher), each user appears as a completely separate device, so the ANDROID_ID value is
	 * unique to each user.
	 * @see Settings.Secure#ANDROID_ID
	 * @param aContext - context to use to aquire said value.
	 * @return Returns the 64 bit number as a hex string (16 chars)
	 */
	@SuppressLint("HardwareIds")
	static public String getAndroidID(Context aContext) {
		if (Build.VERSION.SDK_INT >= 9) {
			//API 9+: ANDROID_ID (may be NULL or may be non-unique across a mfg model (known bugs))
			return Settings.Secure.getString(aContext.getContentResolver(), Settings.Secure.ANDROID_ID);
		} else
			return null;
	}

	/**
	 * API 8+ A hardware serial number, if available. Alphanumeric only, case-insensitive.
	 * @return Returns the defined serial number or NULL if not available.
	 */
	@SuppressLint("HardwareIds")
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	static public String getSerialNumber() {
		if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO && Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1 ) {
			return Build.SERIAL;
		} else
			return null;
	}

	/**
	 * Based on information available for the device, return as unique of a device ID as possible.
	 * @param aContext - the context to use.
	 * @return Returns as unique a string per device as possible.
	 */
	@SuppressLint({"HardwareIds", "MissingPermission"})
	static public String getDeviceID(Context aContext) {
		String theResult = null;
		try {
			//IMEI or similar cellular ID, if available
			int thePermState = aContext.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE);
			if ( thePermState == PackageManager.PERMISSION_GRANTED ) {
				TelephonyManager thePhoneMgr = (TelephonyManager) aContext.getSystemService(Context.TELEPHONY_SERVICE);
				theResult = ( thePhoneMgr != null ) ? thePhoneMgr.getDeviceId() : null;
				if ( !TextUtils.isEmpty(theResult) ) {
					theResult += "1";
				}
			}
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ) {
				//ANDROID_ID, if available
				if ( theResult == null || theResult.contains("*") || theResult.contains("000000000000000") ) {
					//API 9+: ANDROID_ID (may be NULL or may be non-unique across a mfg model (known bugs))
					theResult = getAndroidID(aContext);
					if ( !TextUtils.isEmpty(theResult) ) {
						theResult += "2";
					}
				}
				//SERIAL#, if available
				if ( TextUtils.isEmpty(theResult) ) {
					theResult = getSerialNumber();
					if ( !TextUtils.isEmpty(theResult) ) {
						theResult += "3";
					}
				}
			}
			//fallback
			if (theResult==null) {
				theResult = Build.FINGERPRINT+"F";
			}
		}
		catch ( SecurityException sx ) {
			theResult = getAndroidID(aContext);
			if ( !TextUtils.isEmpty(theResult) ) {
				theResult += "2";
			}
		}

		return theResult;
	}

	static private Method mActMgr_GetMemoryClass = null;
	static private int myMemoryClassValue = 0;
	/**
	 * Wrapper for {@link android.app.ActivityManager#getMemoryClass()}.
	 * @param aContext - an Activity
	 * @return Returns the number of megs available per app.
	 */
	static public int getMemoryClass(Context aContext) {
		if (myMemoryClassValue==0) {
			myMemoryClassValue = 16; //16 megs is the Java heap size in older phones
			ActivityManager theActMgr = (ActivityManager)aContext.getSystemService(Context.ACTIVITY_SERVICE);
			if (mActMgr_GetMemoryClass!=null) {
				try {
					myMemoryClassValue = (Integer)mActMgr_GetMemoryClass.invoke(theActMgr, (Object[])null);
				} catch (IllegalArgumentException e) {
					//leave default value
				} catch (IllegalAccessException e) {
					//leave default value
				} catch (InvocationTargetException e) {
					//leave default value
				}
			}
		}
		return myMemoryClassValue;
	}

	/**
	 * Returns the current app's package information (package name, version info, etc.)
	 * @param aContext - context to use.
	 * @param flags - PackageManager.GET_* flags. 0 gets basic info like name and version.
	 * @return Returns the PackageInfo class from the PackageManager.
	 */
	static public PackageInfo getAppPackageInfo(Context aContext, int flags) {
		PackageInfo pi;
		try {
			pi = aContext.getPackageManager().getPackageInfo(aContext.getPackageName(), flags);
		} catch (Exception e) {
			//doubt this will ever run since we want info about our own package
			pi = new PackageInfo();
			pi.packageName = "com.unknown.404";
			pi.versionName = "e^πi-1";
			pi.versionCode = 69;
		}
		return pi;
	}

	/**
	 * Returns the current application's friendly version name.
	 *
	 * @param aContext - application context
	 * @return
	 *	  Returns the version name as defined by the application. Appends an "*" if the
	 *	  application is also debuggable.
	 */
	static public String getAppVersionName(Context aContext) {
		PackageInfo pi = getAppPackageInfo(aContext,0);
		String theResult = pi.versionName;
		if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE)!=0) {
			theResult += "*";
		}
		return theResult;
	}

	/**
	 * Quickly retrieve the last known gps coordinates based on available sensor permissions.
	 * @param aContext - the context to use.
	 * @param aForgetBeforeThisTimestamp - forget data older than this timestamp in milliseconds.
	 * @return Returns the Location object if its found recent enough to use.
	 */
	@SuppressWarnings("MissingPermission")
	static public Location getLastKnownLocation(Context aContext, Long aForgetBeforeThisTimestamp) {
		Location theResult = null;
		LocationManager theLocMgr;
		String thePermission = Manifest.permission.ACCESS_FINE_LOCATION;
		if (aContext.checkCallingOrSelfPermission(thePermission) == PackageManager.PERMISSION_GRANTED) {
			theLocMgr = (LocationManager) aContext.getSystemService(Context.LOCATION_SERVICE);
			theResult = theLocMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		if (theResult==null) {
			thePermission = Manifest.permission.ACCESS_COARSE_LOCATION;
			if (aContext.checkCallingOrSelfPermission(thePermission) == PackageManager.PERMISSION_GRANTED) {
				theLocMgr = (LocationManager) aContext.getSystemService(Context.LOCATION_SERVICE);
				theResult = theLocMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
		}
		if (theResult!=null && aForgetBeforeThisTimestamp!=null) {
			if (theResult.getTime() < aForgetBeforeThisTimestamp)
				theResult = null;
		}
		return theResult;
	}

	/**
	 * Return a list of the preferred activities for the given package name (or all if NULL).
	 * @param aContext - the context to use.
	 * @param aPackageName - (optional) the name of a specific package to filter by.
	 * @return Returns the preferred activity list for the given package, or all if NULL.
	 */
	static public List<ComponentName> getPreferredActivities(Context aContext,
			String aPackageName) {
		IntentFilter theFilter = new IntentFilter(Intent.ACTION_MAIN);
		theFilter.addCategory(Intent.CATEGORY_HOME);

		List<IntentFilter> theFilterList = new ArrayList<IntentFilter>();
		theFilterList.add(theFilter);

		List<ComponentName> thePreferredActivities = new ArrayList<ComponentName>();
		final PackageManager thePackageMgr = aContext.getPackageManager();
		thePackageMgr.getPreferredActivities(theFilterList, thePreferredActivities, aPackageName);
		return thePreferredActivities;
	}

	/**
	 * Return the SIM's serial number, if it exists.
	 * @param aContext - the context to use.
	 * @return Returns the serial number of the SIM, if any.
	 */
	@SuppressLint({"HardwareIds", "MissingPermission"})
	static public String getSimSerialNumber(Context aContext) {
		TelephonyManager theTelephonyMgr = (TelephonyManager) aContext.getSystemService(Context.TELEPHONY_SERVICE);
		if ( theTelephonyMgr != null && theTelephonyMgr.getSimState() == TelephonyManager.SIM_STATE_READY) {
			return theTelephonyMgr.getSimSerialNumber();
		} else {
			return null;
		}
	}

	static public Date apkUpdateTime(PackageInfo pi) {
		File apkFile = new File(pi.applicationInfo.sourceDir);
		return apkFile.exists() ? new Date(apkFile.lastModified()) : null;
	}

	/**
	 * Returns an {@link Intent} targeting the launch activity for the current
	 * application. A library that needs to launch the app's primary activity
	 * will not necessarily know this information.
	 * @param aContext the context to use
	 * @return an intent that targets the current app's launch activity
	 */
	static public Intent getLaunchIntentForPackage( Context aContext )
	{
		final Context ctxApp = aContext.getApplicationContext() ;
		final String sAppPkg = ctxApp.getPackageName() ;
		return ctxApp.getPackageManager().getLaunchIntentForPackage(sAppPkg) ;
	}

	static public Class<?> obtainClassForName(Context aContext, String aClassName, String aTAG)
	{
		try {
			return Class.forName(aClassName, true, aContext.getClassLoader());
		} catch (ClassNotFoundException e) {
			Log.e(aTAG, "class not found: " + aClassName, e);
		}
		return null;
	}

	static public Object obtainInstanceOfClassName(Context aContext, String aClassName,
			Class<?> anAncestorClassOrInterface, String aTAG, Object... aConstructorParams)
	{
		Class[] theConParamClasses = null;
		try {
			Class<?> theClass = obtainClassForName(aContext, aClassName, aTAG);
			if (theClass!=null) {
				if (anAncestorClassOrInterface != null)
					anAncestorClassOrInterface.isAssignableFrom(theClass);
				if (aConstructorParams!=null && aConstructorParams.length>0) {
					//get the classes of each construction param
					theConParamClasses = new Class[aConstructorParams.length];
					for (int i = 0; i < aConstructorParams.length; i++) {
						theConParamClasses[i] = aConstructorParams[i].getClass();
					}
					//cannot just use getConstructor(theConParamClasses) as that will
					//  only find a constructor with class declared as the type
					//  given to us rather than also any ancestor class or interface
					for (Constructor theCon : theClass.getConstructors()) {
						Class<?>[] theDeclaredParams = theCon.getParameterTypes();
						if (theDeclaredParams.length == theConParamClasses.length) {
							boolean bMatchesParams = true;
							for (int i=0; i<theDeclaredParams.length; i++) {
								bMatchesParams = bMatchesParams && (
										theDeclaredParams[i].isAssignableFrom(
												theConParamClasses[i]
										)
								);
							}
							if (bMatchesParams)
								return theCon.newInstance(aConstructorParams);
						}
					}
					//if we get here... error
					throw new NoSuchMethodException();
				}
				else
					return theClass.newInstance();
			}
		} catch (InstantiationException e) {
			Log.e(aTAG, "cannot instantiate: " + aClassName, e);
		} catch (IllegalAccessException e) {
			Log.e(aTAG, "cannot access: " + aClassName, e);
		} catch (NoSuchMethodException e) {
			StringBuilder theMsg = new StringBuilder()
				.append("no public constructor for: ").append(aClassName)
				.append(" matching (")
				;
			if (theConParamClasses.length>0) {
				for (Class theClass : theConParamClasses)
					theMsg.append(theClass.getSimpleName()).append(',');
				theMsg.deleteCharAt(theMsg.length()-1);
			}
			theMsg.append(")");
			Log.e(aTAG, theMsg.toString(), e);
		} catch (InvocationTargetException e) {
			Log.e(aTAG, "consructor failed for: " + aClassName, e);
		}
		return null;
	}

}
