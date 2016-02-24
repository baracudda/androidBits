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
import android.preference.EditTextPreference;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Extend the default EditTextPreference to show the current value in the summary.
 * @author baracudda
 */
public class StringPreference extends EditTextPreference {

	public StringPreference(Context aContext, AttributeSet attrs) {
		super(aContext, attrs);
	}

	protected String getEntry() {
		String theText = getText();
		if (theText==null || theText.equals("")) {
			theText = "________";
        } else if (getEditText().getTransformationMethod() instanceof PasswordTransformationMethod) {
        	theText = "••••••••";
        }
		return theText;
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		setSummary(getEntry());
		return super.onCreateView(parent);
	}

	@Override
	protected boolean persistString(String aNewValue) {
		if (super.persistString(aNewValue)) {
			setSummary(getEntry());
			notifyChanged();
			return true;
		} else {
			return false;
		}
	}

}
