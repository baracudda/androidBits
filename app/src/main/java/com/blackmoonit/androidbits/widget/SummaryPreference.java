package com.blackmoonit.androidbits.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Non-editable preference that exists solely to show the current value in the summary.
 * @author Ryan Fischbach
 */
public class SummaryPreference extends StringPreference {

	public SummaryPreference(Context aContext, AttributeSet attrs) {
		super(aContext, attrs);
		this.setEnabled(false);
	}

}
