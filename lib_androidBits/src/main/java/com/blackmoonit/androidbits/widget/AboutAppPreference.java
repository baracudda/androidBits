package com.blackmoonit.androidbits.widget;
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

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;

import com.blackmoonit.androidbits.app.AppPreferenceBase;

/**
 * About App preference that will display version info in the summary.
 * <br><br>
 * {@code android:title} - required format string {@literal "About %s"} which becomes the app name.<br>
 * {@code android:summary} - required attribute, usually {@literal "@string/version_name"}<br>
 * @author baracudda
 */
public class AboutAppPreference extends Preference {
	private int mResIdAppName = 0;

	public AboutAppPreference(Context aContext, AttributeSet attrs) {
		super(aContext, attrs);
		setup(aContext,attrs);
	}

	public AboutAppPreference(Context aContext, AttributeSet attrs, int defStyle) {
		super(aContext, attrs, defStyle);
		setup(aContext,attrs);
	}

	protected int getResId(String aResType, String aResName) {
		return getContext().getResources().getIdentifier(aResName,aResType,getContext().getPackageName());
	}

	protected void getResIds(Resources aResources, String aPackageName) {
		Resources r = getContext().getResources();
		String pkgName = getContext().getPackageName();
		mResIdAppName = r.getIdentifier("app_name","string",pkgName);
	}

	protected int[] getAttrsWanted() {
		return new int[] {
				//android.R.attr.dialogLayout
			};
	}

	protected void setup(Context aContext, AttributeSet attrs) {
		int[] attrsWanted = getAttrsWanted();
		final TypedArray theAttrs = aContext.obtainStyledAttributes(attrs,attrsWanted);
		processWantedAttributes(theAttrs);
		theAttrs.recycle();
		getResIds(aContext.getResources(),aContext.getPackageName());
	}

	/**
	 * Descendants would override this method to handle any expanded list of attributes.
	 * The parameter is a 0 based array containing all values defined by {@link #getAttrsWanted}.
	 *
	 * @param aAttrs - the attributes as defined in the xml file
	 */
	protected int processWantedAttributes(final TypedArray aAttrs) {
		int i = 0; //skip first one since it's only there as a workaround to an Android bug
		//setDialogLayoutResource(aAttrs.getResourceId(i++,getResId("layout","dialog_about")));
		return i;
	}

	@Override
	public CharSequence getTitle() {
		String s = (String)super.getTitle();
		return String.format(s,getContext().getString(mResIdAppName));
	}

	@Override
	public CharSequence getSummary() {
		String s = (String)super.getSummary();
		return String.format(s,AppPreferenceBase.getAppVersionName(getContext()));
	}

}
