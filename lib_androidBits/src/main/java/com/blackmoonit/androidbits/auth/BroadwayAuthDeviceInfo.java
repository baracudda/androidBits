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

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.location.Location;
import android.text.TextUtils;

import com.blackmoonit.androidbits.R;
import com.blackmoonit.androidbits.utils.BitsAppUtils;

import java.util.Date;

public class BroadwayAuthDeviceInfo implements IBroadwayAuthDeviceInfo
{
	/**
	 * Fingerprint separator can be custom. ", " is the default expected by server.
	 */
	private static final String FINGERPRINT_SEPARATOR = ", ";
	/**
	 * Circumstance separator can be custom. ", " is the default expected by server.
	 */
	private static final String CIRCUMSTANCE_SEPARATOR = ", ";

	/** the cached context to use */
	protected Context mContext;
	/** cache for non-volatile device fingerprint into */
	protected String[] mFingerprints = null;

	/**
	 * Gather various device information to send to the server for use in BroadwayAuth mechanism.
	 * @param aContext - context used for getting app and device info.
	 */
	public BroadwayAuthDeviceInfo(Context aContext) {
		mContext = aContext;
	}

	/**
	 * Standard device information sent to the server to determine auth status.
	 * Non-volatile information that should not change between API calls.
	 * @return Returns the various data collected to present to the server.
	 */
	@SuppressWarnings("deprecation")
	public String[] getMyDeviceFingerprints() {
		Context theContext = mContext;
		//app environment
		PackageInfo pi = BitsAppUtils.getAppPackageInfo(theContext, PackageManager.GET_SIGNATURES);
		//app resources
		Resources theResources = theContext.getResources();
		//device info
		ActivityManager theActMgr = (ActivityManager) theContext.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo myDeviceMemInfo = new ActivityManager.MemoryInfo();
		theActMgr.getMemoryInfo(myDeviceMemInfo);

		int theSigHash;
		if (pi.signatures.length > 1) {
			StringBuilder theSigsBuilder = new StringBuilder();

			for (Signature theSig : pi.signatures) {
				theSigsBuilder.append(theSig.toCharsString()).append(",");
			}
			theSigsBuilder.deleteCharAt(theSigsBuilder.length() - 1);
			theSigHash = theSigsBuilder.toString().hashCode();
		} else {
			theSigHash = pi.signatures[0].toCharsString().hashCode();
		}

		//.locale.toString() deprecated in API24, but still useful API14+
		return new String[]{
				String.valueOf(theSigHash),
				BitsAppUtils.getAndroidID(theContext),
				BitsAppUtils.getDeviceID(theContext),
				theResources.getConfiguration().locale.toString(),
				String.valueOf(myDeviceMemInfo.threshold), //API 1+, whereas totalMem is 16+
		};
	}

	/**
	 * Volatile device information sent to the server to determine auth status.
	 * This covers information such as GPS location and timestamp.
	 * @return Returns the various data collected to present to the server.
	 */
	public String[] getMyDeviceCircumstances() {
		Context theContext = mContext;
		Location theLoc = null;
		String theName = BitsAppUtils.getAppVersionName(theContext);
		if (theContext!=null) {
			theName = theContext.getString(R.string.app_name)+" "+theName;
			//gps, if available, but only if it is less than a week old
			long theRememberMeUntilDate = (new Date()).getTime() - (1000 * 60 * 60 * 24 * 7); // 7 day old data
			theLoc = BitsAppUtils.getLastKnownLocation(theContext, theRememberMeUntilDate);
		}
		return new String[]{
				String.valueOf(System.currentTimeMillis()),
				theName,
				((theLoc!=null) ? String.valueOf(theLoc.getLatitude()) : "?"),
				((theLoc!=null) ? String.valueOf(theLoc.getLongitude()) : "?"),
		};
	}

	/**
	 * Standard device information sent to the server to determine auth status.
	 * Non-volatile information that should not change between API calls.
	 * @return Returns the various data collected to present to the server.
	 */
	@Override
	public String[] getDeviceFingerprints() {
		if (mFingerprints==null) {
			mFingerprints = getMyDeviceFingerprints();
		}
		return mFingerprints;
	}

	/**
	 * Volatile device information sent to the server to determine auth status.
	 * This covers information such as GPS location and timestamp.
	 * @return Returns the various data collected to present to the server.
	 */
	@Override
	public String[] getDeviceCircumstances() {
		return getMyDeviceCircumstances();
	}

	@Override
	public StringBuilder composeBroadwayAuthData(StringBuilder aStrBldr)
	{
		final String theFingerprintSeparator = this.getFingerprintSeparator() ;
		final String theCircumstanceSeparator = this.getCircumstanceSeparator();
		if (aStrBldr==null)
			aStrBldr = new StringBuilder() ;
		//if using the default separator of ", ", then we do not need to specify it
		if (!", ".equals( theFingerprintSeparator ))
			aStrBldr.append( "fsep=\"" ).append( theFingerprintSeparator ).append( "\"," );
		aStrBldr.append( "fingerprints=\"" )
			.append( TextUtils.join( theFingerprintSeparator, getDeviceFingerprints() ) )
			.append( "\"," )
			;
		if (!", ".equals( theCircumstanceSeparator ))
			aStrBldr.append( "csep=\"" ).append( theCircumstanceSeparator ).append( "\"," );
		aStrBldr.append( "circumstances=\"" )
			.append( TextUtils.join( theCircumstanceSeparator, getDeviceCircumstances() ) )
			.append( "\"" )
			;
		return aStrBldr ;
	}

	/**
	 * Descendants may override this method with a custom separator.
	 * @return the separator string for fields in the "circumstances".
     */
	protected String getCircumstanceSeparator()
	{ return CIRCUMSTANCE_SEPARATOR ; }

	/**
	 * Descendants may override this method with a custom separator.
	 * @return the separator string for fields in the "fingerprints".
     */
	protected String getFingerprintSeparator()
	{ return FINGERPRINT_SEPARATOR ; }
}
