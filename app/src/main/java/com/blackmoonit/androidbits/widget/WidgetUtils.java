package com.blackmoonit.androidbits.widget;

import android.graphics.Paint;
import android.widget.TextView;

public final class WidgetUtils {

	private WidgetUtils() {}; //do not instantiate

	static public void setTextUnderline(TextView v, boolean bUnderline) {
		if (v!=null) {
			if (bUnderline)
				v.setPaintFlags(v.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			else
				v.setPaintFlags(v.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
		}

	}

	static public String getClassPackageName(Class<?> aClass) {
		String theFullyQualifiedName = aClass.getName();
		int theFinalPeriod = theFullyQualifiedName.lastIndexOf('.');
		if (theFinalPeriod>=0) {
			return theFullyQualifiedName.substring(0,theFinalPeriod);
		} else {
			return "";
		}
	}

}
