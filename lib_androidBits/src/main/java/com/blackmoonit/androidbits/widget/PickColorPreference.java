package com.blackmoonit.androidbits.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;

import com.blackmoonit.androidbits.dialog.PickColorDialog;
import com.blackmoonit.androidbits.dialog.PickColorDialog.OnColorPickedListener;

/**
 * Custom preference that will toss up a color picker dialog and store the result.
 *
 * @author Ryan Fischbach
 */
public class PickColorPreference extends Preference implements OnColorPickedListener {
	//private static final String androidns = "android";
	//private static final String attr_dialogTitle = "dialogTitle";
	private int mValue = Color.WHITE;
	private int mDialogTitleResId = 0;

	public PickColorPreference(Context context) {
		super(context);
	}

	public PickColorPreference(Context aContext, AttributeSet attrs) {
		super(aContext, attrs);
		getDialogTitle(aContext,attrs);
	}

	public PickColorPreference(Context aContext, AttributeSet attrs, int defStyle) {
		super(aContext, attrs, defStyle);
		getDialogTitle(aContext,attrs);
	}

	private void getDialogTitle(Context aContext, AttributeSet attrs) {
		//Google engineer suggested this way is preferred over the commented out code below
		int attrsWanted[] = new int[] {android.R.attr.dialogTitle};
		TypedArray theAttrs = aContext.obtainStyledAttributes(attrs,attrsWanted);
		mDialogTitleResId = theAttrs.getResourceId(0,mDialogTitleResId);
		theAttrs.recycle();

		/*
		int theIdx = BitsLegacy.getAttributeIndex(aContext,attrs,attr_dialogTitle);
		if (theIdx>=0) {
			mDialogTitleResId = attrs.getAttributeResourceValue(theIdx,mDialogTitleResId);
		}
		*/
	}

	public void onColorPicked(int aColor) {
		mValue = aColor;
		PickColorPreference.this.persistInt(mValue);
	}

	@Override
	protected void onClick() {
		PickColorDialog d = new PickColorDialog(getContext(),this,getPersistedInt(mValue));
		if (mDialogTitleResId!=0)
			d.setTitle(mDialogTitleResId);
		d.setCanceledOnTouchOutside(true);
		d.show();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index,mValue);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		int theDefaultValue = mValue;
		if (defaultValue!=null) {
			theDefaultValue = (Integer)defaultValue;
		}
		if (restorePersistedValue && shouldPersist()) {
			mValue = getPersistedInt(theDefaultValue);
		} else {
			mValue = theDefaultValue;
		}
	}

}
