package com.blackmoonit.androidbits.widget;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Extend the default EditTextPreference to show the current value in the summary.
 * @author Ryan Fischbach
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
