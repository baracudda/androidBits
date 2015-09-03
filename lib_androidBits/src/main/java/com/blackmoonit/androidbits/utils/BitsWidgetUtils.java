package com.blackmoonit.androidbits.utils;
/*
 * Copyright (C) 2015 Blackmoon Info Tech Services
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

import android.graphics.Paint;
import android.widget.TextView;

public class BitsWidgetUtils {

	protected BitsWidgetUtils() {}; //do not instantiate

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
