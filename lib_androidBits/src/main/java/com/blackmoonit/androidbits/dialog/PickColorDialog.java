package com.blackmoonit.androidbits.dialog;
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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.blackmoonit.androidbits.utils.BitsGraphicsUtils;
import com.blackmoonit.androidbits.widget.ColorBrightnessView;
import com.blackmoonit.androidbits.widget.ColorBrightnessView.OnBrightnessChangedListener;
import com.blackmoonit.androidbits.widget.ColorHueView;
import com.blackmoonit.androidbits.widget.ColorHueView.OnColorHueChangedListener;
import com.blackmoonit.androidbits.widget.ColorHueView.OnColorHueSelectedListener;

/**
 * Color picker dialog.
 *
 * @author Ryan Fischbach
 */
public class PickColorDialog extends Dialog implements OnColorHueSelectedListener,
		OnColorHueChangedListener, OnBrightnessChangedListener {

	public interface OnColorPickedListener {
		public void onColorPicked(int aColor);
	}

	ColorHueView mColorPickerView;
	ColorBrightnessView mBrightnessSliderView;
	OnColorPickedListener mColorPicked;
	int mInitColor = Color.WHITE;

	public PickColorDialog(Context aContext, OnColorPickedListener aColorPickedListener, int aInitColor) {
		super(aContext);
		mColorPicked = aColorPickedListener;
		mInitColor = aInitColor;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//construct the dialog Views
		Context theContext = getContext();
		RelativeLayout vLayout = new RelativeLayout(theContext);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		int pxlMargin = BitsGraphicsUtils.dipsToPixels(theContext,4);
		lp.setMargins(pxlMargin,pxlMargin,pxlMargin,pxlMargin);
		vLayout.setLayoutParams(lp);

		//hue view
		mColorPickerView = new ColorHueView(theContext,this,mInitColor);
		mColorPickerView.setOnHueSelectedListener(this);
		lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.setMargins(pxlMargin, pxlMargin, pxlMargin, pxlMargin);
		mColorPickerView.setLayoutParams(lp);
		mColorPickerView.setId(1);
		vLayout.addView(mColorPickerView);

		//brightness view
		mBrightnessSliderView = new ColorBrightnessView(theContext,this,mInitColor);
		lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.setMargins(pxlMargin,pxlMargin,pxlMargin,pxlMargin);
		lp.addRule(RelativeLayout.RIGHT_OF,1);
		mBrightnessSliderView.setLayoutParams(lp);
		vLayout.addView(mBrightnessSliderView);

		setContentView(vLayout);
	}

	@Override
	public void onColorHueSelected(int aColor) {
		if (mColorPicked!=null)
			mColorPicked.onColorPicked(aColor);
		dismiss();
	}

	@Override
	public void onColorHueChanged(int aColor) {
		mColorPickerView.setColor(mBrightnessSliderView.setColor(aColor));
	}

	@Override
	public void onBrightnessChanged(int aAdjustedColor) {
		mColorPickerView.setColor(aAdjustedColor);
	}

}
