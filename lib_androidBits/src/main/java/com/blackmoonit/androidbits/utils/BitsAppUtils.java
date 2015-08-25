package com.blackmoonit.androidbits.utils;
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

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Utility functions that deal mainly with app-wide settings or values.
 * @author Ryan Fischbach
 */
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
	static public String getSerialNumber() {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
			return Build.SERIAL;
		} else
			return null;
	}

	/**
	 * Based on information available for the device, return as unique of a device ID as possible.
	 * @param aContext - the context to use.
	 * @return Returns as unique a string per device as possible.
	 */
	static public String getDeviceID(Context aContext) {
		String theResult = null;

		//IMEI or similar cellular ID, if available
		if (theResult==null &&
			aContext.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
			TelephonyManager thePhoneMgr = (TelephonyManager) aContext.getSystemService(Context.TELEPHONY_SERVICE);
			theResult = thePhoneMgr.getDeviceId()+"1";
		}
		//ANDROID_ID, if available
		if (Build.VERSION.SDK_INT >= 9 &&
				( theResult==null || theResult.contains("*") || theResult.contains("000000000000000")) ) {
			//API 9+: ANDROID_ID (may be NULL or may be non-unique across a mfg model (known bugs))
			theResult = getAndroidID(aContext)+"2";
		}
		//SERIAL#, if available
		if (theResult==null && Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
			theResult = Build.SERIAL+"3";
		}

		//fallback
		if (theResult==null) {
			theResult = Build.FINGERPRINT;
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
	 *      Returns the version name as defined by the application. Appends an "*" if the
	 *      application is also debuggable.
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
	static public Location getLastKnownLocation(Context aContext, Long aForgetBeforeThisTimestamp) {
		Location theResult = null;
		LocationManager theLocMgr = null;
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
}
