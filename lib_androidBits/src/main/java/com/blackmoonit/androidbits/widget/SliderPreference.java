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
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Custom preference that will toss up a slider dialog and store the result.<br>
 * <br>
 * {@code android:entries} &amp; {@code android:entryValues} - Define these resourceId attributes
 * if you intend on the slider equating to the index of arbitrary String values in entryValues. If
 * these values are set, the persisted value is a string from entryValues and not the integer
 * slider value.<br>
 * <br>
 * {@code android:defaultValue} - default value of the slider (use the entry desired from entryValues if you
 * define that attribute).<br>
 * <br>
 * {@code android:positiveButtonText} &amp; {@code android:negativeButtonText} - optional,
 * {@literal @null} to hide the button, the resId if you want something other than
 * the standard "OK" and "CANCEL".<br>
 * <br>
 * {@code android:max} - optional max slider value. Default is 100. If entryValues are defined,
 * then the max is set to the number of entrys.<br>
 * <br>
 * {@code android:dialogLayout} - optional in that if it is missing,
 * {@literal "@layout/dialog_pref_fontsize"} will be used.<br>
 * <br>
 * {@code android:dialogMessage} &amp; {@code android:summary} &mdash;
 *  optional; if missing, nothing will be displayed in the dialog above the
 *  slider control.<br>
 * <br>
 * @author baracudda
 */
public class SliderPreference extends DialogPreference implements OnSeekBarChangeListener {
	protected int mSliderValue = 0;
	protected int mSliderMaxValue = 100;
	protected Integer mSliderDefault = null;
	protected String mDefaultValue; //set in onGetDefaultValue
	protected List<String> mPrefEntries = null;
	protected List<String> mPrefValues = null;
	protected SeekBar mSeekBar = null;
	protected TextView mSummary = null;
	protected int R_id_dialog_pref_slider_SeekBar = 0;
	protected int R_id_dialog_pref_slider_Summary = 0;
	protected int R_array_pref_entries = 0;
	protected int R_array_pref_entryvalues = 0;
	private String mPrefEntry; //frequently used temp var

	public SliderPreference(Context aContext, AttributeSet attrs) {
		super(aContext,attrs);
		setup(aContext,attrs);
	}

	public SliderPreference(Context aContext, AttributeSet attrs, int defStyle) {
		super(aContext,attrs,defStyle);
		setup(aContext,attrs);
	}

	protected void setup(Context aContext, AttributeSet attrs) {
		int[] attrsWanted = getAttrsWanted();
		final TypedArray theAttrs = aContext.obtainStyledAttributes(attrs,attrsWanted);
		processWantedAttributes(theAttrs);
		theAttrs.recycle();
		getResIds(aContext.getResources(),aContext.getPackageName());
	}

	/**
	 * Descendants would override this method to expand the list of interested
     * attributes. Call {@code super.getAttrsWanted()} and expand on what it
     * returns. Currently this list includes:
     * <ul style="font-family:monospace">
     * <li>android.R.attr.entries</li>
     * <li>android.R.attr.entryValues</li>
     * <li>android.R.attr.defaultValue</li>
     * <li>android.R.attr.positiveButtonText</li>
     * <li>android.R.attr.negativeButtonText</li>
     * <li>android.R.attr.max</li>
     * <li>android.R.attr.dialogLayout</li>
     * </ul>
	 * Note that there is an unresolved issue with some of the later items in
     * this array, in that the resource IDs returned by the system are always
     * the "default" (not found), even when those attributes are indeed supplied
     * in the XML. One of these items, {@code dialogMessage}, has been removed
     * as of 2016-01-15.
	 * @return Complete list of attributes the class is interested in.
	 */
	protected int[] getAttrsWanted() {
		return new int[] {
				android.R.attr.entries,
				android.R.attr.entryValues,
				android.R.attr.defaultValue,
				android.R.attr.positiveButtonText,
				android.R.attr.negativeButtonText,
				android.R.attr.max,
				android.R.attr.dialogLayout
			};
	}

	/**
	 * Descendants would override this method to handle any expanded list of attributes.
	 * The parameter is a 0 based array containing all values defined by {@link #getAttrsWanted}.
	 *
	 * @param aAttrs - the attributes as defined in the xml file
	 */
	protected int processWantedAttributes(final TypedArray aAttrs)
    {
        int i = 0;
		R_array_pref_entries = aAttrs.getResourceId(i++,R_array_pref_entries);
		R_array_pref_entryvalues = aAttrs.getResourceId(i++,R_array_pref_entryvalues);
		if (mDefaultValue==null && aAttrs.peekValue(i)!=null)
			mDefaultValue = aAttrs.getString(i++);
		else
			i += 1;
		setPositiveButtonText(aAttrs.getResourceId(i++,android.R.string.ok));
		setNegativeButtonText(aAttrs.getResourceId(i++,android.R.string.cancel));
		mSliderMaxValue = aAttrs.getInteger(i++,mSliderMaxValue);
		setDialogLayoutResource( aAttrs.getResourceId( i++,
            getResId( "layout", "dialog_pref_slider" ) ) );
        /*
         * This method formerly tried to get things like the "dialogMessage" and
         * "summary" but apparently those are intercepted and unavailable. Even
         * the other attributes above, such as default value, are suspicious;
         * "defaultValue" was also found to be unobtained.
         */
		return i;
	}

	protected int getResId(String aResType, String aResName) {
		return getContext().getResources().getIdentifier(aResName,aResType,getContext().getPackageName());
	}

	protected void getResIds(Resources aResources, String aPackageName) {
		R_id_dialog_pref_slider_SeekBar = aResources.getIdentifier("dialog_pref_slider_SeekBar","id",aPackageName);
		R_id_dialog_pref_slider_Summary = aResources.getIdentifier("dialog_pref_slider_Summary","id",aPackageName);

		if (R_array_pref_entries!=0 && R_array_pref_entryvalues!=0) {
			mPrefEntries = Arrays.asList(aResources.getStringArray(R_array_pref_entries));
			mPrefValues = Arrays.asList(aResources.getStringArray(R_array_pref_entryvalues));
			mSliderMaxValue = mPrefValues.size()-1;
		}
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

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (defaultValue==null)
			defaultValue = mDefaultValue;
		Integer theDefaultValue;
		if (defaultValue instanceof String) {
			try {
				if (mPrefValues!=null) {
					theDefaultValue = mPrefValues.indexOf((String)defaultValue);
				} else {
					theDefaultValue = Integer.valueOf((String)defaultValue);
				}
			} catch (Exception e) {
				theDefaultValue = null;
			}
		} else if (defaultValue instanceof Integer) {
			theDefaultValue = (Integer)defaultValue;
		} else {
			theDefaultValue = null;
		}
		mSliderDefault = (theDefaultValue!=null)?theDefaultValue:mSliderValue;
		if (restorePersistedValue)
			mSliderValue = getSavedValue(mSliderDefault);
		else
			mSliderValue = mSliderDefault;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mSeekBar = (SeekBar)v.findViewById(R_id_dialog_pref_slider_SeekBar);
		mSummary = (TextView)v.findViewById(R_id_dialog_pref_slider_Summary);
		mSeekBar.setMax(mSliderMaxValue);
		mSeekBar.setOnSeekBarChangeListener(this);
		setProgress(mSliderValue);
		onProgressChanged(mSeekBar,mSliderValue,false);
	}

	protected String getEntry() {
		int theValue = getSavedValue(mSliderDefault);
		if (mPrefEntries!=null) {
			if (theValue<0 || theValue>=mPrefEntries.size()) {
				theValue = mPrefValues.indexOf(mDefaultValue);
			}
			return mPrefEntries.get(theValue);
		} else {
			return String.valueOf(theValue);
		}
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

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult && callChangeListener(getValueToSave(mSliderValue))) {
			putSavedValue(mSliderValue);
		} else {
			mSliderValue = getSavedValue(mSliderDefault);
		}
		super.onDialogClosed(positiveResult);
	}

	/**
	 * Given a slider value, return the value that will be persisted.
	 *
	 * @param aSliderValue - the slider value
	 * @return Returns the slider value or the entryValues[slidervalue] if defined.
	 */
	public Object getValueToSave(int aSliderValue) {
		if (mPrefValues!=null) {
			if (aSliderValue>=0 && aSliderValue<mPrefValues.size()) {
				return mPrefValues.get(aSliderValue);
			} else {
				throw new IndexOutOfBoundsException("Slider Value outside entryValues index range.");
			}
		} else {
			return (Integer)aSliderValue;
		}
	}

	/**
	 * Persist mSliderValue as int unless entryValues are defined. If entryValues are defined
	 * then persist mSliderValue as a string of entryValues[mSliderValue].
	 */
	public void putSavedValue(int aSliderValue) {
		if (shouldPersist()) {
			Object saveValue = getValueToSave(aSliderValue);
			if (saveValue instanceof String) {
				persistString((String)saveValue);
			} else {
				persistInt((Integer)saveValue);
			}
		}
	}

	/**
	 * Retrieve the saved value and convert it to a raw slider value if entryValues are defined.
	 *
	 * @param aDefaultValue - default slider value
	 * @return Returns saved slider value. If entryValues are defined, the index of the
	 * entryValue is returned. If there is no value or any kind of exception, aDefaultValue is returned.
	 */
	public int getSavedValue(int aDefaultValue) {
		if (shouldPersist()) {
			if (mPrefValues!=null) {
				try {
					return mPrefValues.indexOf(getPersistedString(Integer.toString(aDefaultValue)));
				} catch (Exception e) {
					return aDefaultValue;
				}
			} else {
				return getPersistedInt(aDefaultValue);
			}
		} else {
			return aDefaultValue;
		}
	}

	public void setProgress(int aValue) {
		mSliderValue = aValue;
		if (mSeekBar!=null && aValue>=0 && aValue<=mSeekBar.getMax()) {
			mSeekBar.setProgress(aValue);
		}
	}

	public int getProgress() {
		return mSliderValue;
	}

	public void setDialogSummary(String aSummary) {
		if (mSummary!=null)
			mSummary.setText(aSummary);
	}

	public void onProgressChanged(SeekBar aSeekBar, int aValue, boolean fromUser) {
		mSliderValue = aValue;
		if (mPrefEntries!=null) {
			if (aValue>=0 && aValue<mPrefEntries.size()) {
				mPrefEntry = mPrefEntries.get(aValue);
				setDialogSummary(mPrefEntry);
			}
		} else {
			setDialogSummary(""+aValue);
		}
		if (fromUser && getPositiveButtonText()==null) {
			if (callChangeListener(getValueToSave(mSliderValue)))
				putSavedValue(mSliderValue);
		}
	}

	public void onStartTrackingTouch(SeekBar aSeekBar) {

	}

	public void onStopTrackingTouch(SeekBar aSeekBar) {

	}

}
