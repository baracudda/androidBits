package com.blackmoonit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class UriPreference extends StringPreference {

	public UriPreference(Context aContext, AttributeSet attrs) {
		super(aContext, attrs);
	}

	@Override
	protected void onAddEditTextToDialogView(View aDialogView, EditText aEditText) {
		super.onAddEditTextToDialogView(aDialogView, aEditText);
		aEditText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_URI);
	}
	
}
