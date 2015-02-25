package com.blackmoonit.androidbits.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class MultiLineTextPreference extends TextDialogPreference {

	public MultiLineTextPreference(Context aContext, AttributeSet attrs) {
		super(aContext, attrs);
	}

	@Override
	protected EditText createEditText(Context aContext) {
		super.createEditText(aContext);
		mEditText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_FILTER |
				EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		mEditText.setLines(8);

		return mEditText;
	}

}
