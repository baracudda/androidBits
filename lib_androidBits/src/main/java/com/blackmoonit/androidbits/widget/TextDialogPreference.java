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

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class TextDialogPreference extends DialogPreference {
	protected String mDefaultValue; //set in onGetDefaultValue
	protected EditText mEditText;

	public TextDialogPreference(Context aContext, AttributeSet attrs) {
		super(aContext,attrs);
		setup(aContext,attrs);
	}

	public TextDialogPreference(Context aContext, AttributeSet attrs, int defStyle) {
		super(aContext,attrs,defStyle);
		setup(aContext,attrs);
	}

	protected void setup(Context aContext, AttributeSet attrs) {
		int[] attrsWanted = getAttrsWanted();
		final TypedArray theAttrs = aContext.obtainStyledAttributes(attrs,attrsWanted);
		processWantedAttributes(theAttrs);
		theAttrs.recycle();
	}

	/**
	 * Descendants would override this method to expand the list of interested attributes.
	 * Call super and expand on what it returns. Current list contains the entries:<br>
	 * android.R.attr.defaultValue<br>
	 *
	 * @return Complete list of attributes the class is interested in.
	 */
	protected int[] getAttrsWanted() {
		return new int[] {
				android.R.attr.defaultValue,
			};
	}

	/**
	 * Descendants would override this method to handle any expanded list of attributes.
	 * The parameter is a 0 based array containing all values defined by {@link #getAttrsWanted}.
	 *
	 * @param aAttrs - the attributes as defined in the xml file
	 */
	protected int processWantedAttributes(final TypedArray aAttrs) {
		int i = 0;
		if (mDefaultValue==null && aAttrs.peekValue(i)!=null)
			mDefaultValue = aAttrs.getString(i++);
		else
			i += 1;
		return i;
	}

	@Override
	protected Object onGetDefaultValue(TypedArray attrs, int aIndex) {
		TypedValue tv = attrs.peekValue(aIndex);
		if (tv!=null)
			mDefaultValue = tv.coerceToString().toString();
		else
			mDefaultValue = null;
		return mDefaultValue;
	}

	public String getEntry() {
		String theResult = getPersistedString(mDefaultValue);
		return (theResult!=null) ? theResult : "";
	}

	public String getNewValue() {
		return mEditText.getText().toString();
	}

	public boolean saveNewValue(String aNewValue) {
		return persistString(aNewValue);
	}

	@Override
	protected void onDialogClosed(boolean bPositiveResult) {
		String theNewValue = getNewValue();
		if (bPositiveResult && callChangeListener(theNewValue)) {
			saveNewValue(theNewValue);
		}
		super.onDialogClosed(bPositiveResult);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder aBuilder) {
		super.onPrepareDialogBuilder(aBuilder);
		createEditText(getContext());
		mEditText.setText(getEntry());
		mEditText.setSelection(getEntry().length());
		aBuilder.setView(mEditText);
	}

	protected EditText createEditText(Context aContext) {
		mEditText = new EditText(getContext());
		mEditText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_NORMAL);
		return mEditText;
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
