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

import android.content.Context;

import com.blackmoonit.androidbits.R;
import com.blackmoonit.androidbits.utils.BitsAppUtils;

public class FactoriesForBroadwayAuth
{
	static private final String TAG = FactoriesForBroadwayAuth.class.getSimpleName();

	/**
	 * Discover and then create an object that implements the {@link IBroadwayAuthDeviceInfo}
	 * interface.  The resource string that defines what class to use is
	 * <b><i>R.string.class_for_auth_device_info</i></b>.
	 * @param aContext - the context to use.
	 * @return Returns the newly instantiated object.
	 */
	static public IBroadwayAuthDeviceInfo obtainDeviceInfo(Context aContext)
	{
		return (IBroadwayAuthDeviceInfo) BitsAppUtils.obtainInstanceOfClassName( aContext,
				aContext.getString(R.string.class_for_auth_device_info),
				IBroadwayAuthDeviceInfo.class,
				TAG,
				aContext
		);
	}

	/**
	 * Discover and return the class to use that fills the role of Android's Account Manager
	 * login screen activity.  The resource string that defined what class to use is
	 * <b><i>R.string.class_for_activity_login</i></b>.
	 * @param aContext - the context to use.
	 * @return Returns the class to use in an Intent.
	 */
	static public Class<?> obtainLoginActivityClass(Context aContext)
	{
		return BitsAppUtils.obtainClassForName( aContext,
				aContext.getString(R.string.class_for_activity_login),
				TAG
		);
	}
}
