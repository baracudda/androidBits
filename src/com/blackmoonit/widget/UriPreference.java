package com.blackmoonit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class UriPreference extends TextDialogPreference {

	public UriPreference(Context aContext, AttributeSet attrs) {
		super(aContext, attrs);
	}

	@Override
	protected EditText createEditText(Context aContext) {
		super.createEditText(aContext);
		mEditText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_URI);
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
