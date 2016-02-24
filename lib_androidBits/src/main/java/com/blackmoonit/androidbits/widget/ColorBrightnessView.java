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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;

import com.blackmoonit.androidbits.utils.BitsColorUtils;


/**
 * View that adjusts the brightness of a given color rather than the hue.
 *
 * @author baracudda
 */
public class ColorBrightnessView extends View {
	private DisplayMetrics currDisplayMetrics = null;
	private int GRADIENT_WIDTH = 32;
	private int GRADIENT_HEIGHT = 200;
	private int THUMB_WIDTH = 12;
	private int VIEW_MARGIN = 4;
	private final int[] mColorSet = new int[] { 0xFFFFFFFF, Color.WHITE, 0xFF000000 };
	private float mBrightnessPos = 0.5f; //value [0-1]
	private Paint mGradientPainter;
	private Paint mThumbPainter;
	private Path mThumbWidget;
	private OnBrightnessChangedListener mChangeListener;

	public interface OnBrightnessChangedListener {
		void onBrightnessChanged(int aAdjustedColor);
	}

	public void setOnBrightnessChangedListener(OnBrightnessChangedListener aChangeListener) {
		mChangeListener = aChangeListener;
	}

	protected int dipsToPixels(int aDip) {
		float px;
		px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, aDip, currDisplayMetrics);
		return Math.round(px);
	}

	public ColorBrightnessView(Context aContext, OnBrightnessChangedListener aChangeListener) {
		this(aContext,aChangeListener,Color.WHITE);
	}

	public ColorBrightnessView(Context aContext, OnBrightnessChangedListener aChangeListener, int aColor) {
		super(aContext);
		initalize(aColor);
		setOnBrightnessChangedListener(aChangeListener);
	}

	public ColorBrightnessView(Context aContext, AttributeSet aAttrSet) {
		super(aContext, aAttrSet);
		initalize(Color.WHITE);
	}

	private void initalize(int aColor) {
		currDisplayMetrics = getContext().getResources().getDisplayMetrics();
		GRADIENT_WIDTH = dipsToPixels(GRADIENT_WIDTH);
		GRADIENT_HEIGHT = dipsToPixels(GRADIENT_HEIGHT);
		THUMB_WIDTH = dipsToPixels(THUMB_WIDTH);

		mGradientPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
		mGradientPainter.setStyle(Paint.Style.FILL_AND_STROKE);
		mGradientPainter.setStrokeWidth(1);

		mThumbPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
		mThumbPainter.setStyle(Paint.Style.FILL_AND_STROKE);
		mThumbPainter.setColor(Color.WHITE);
		mThumbPainter.setStrokeWidth(2);

		mGestureDetector = new GestureDetector(new MyGestureDetector());
		setColor(aColor);
		setBrightness(mBrightnessPos);
	}

	protected void updateThumbWidget() {
		float theThumbPos = GRADIENT_HEIGHT*mBrightnessPos;
		int dY = THUMB_WIDTH/2;
		mThumbWidget = new Path();
		mThumbWidget.moveTo(THUMB_WIDTH,theThumbPos);
		mThumbWidget.lineTo(1,theThumbPos-dY);
		mThumbWidget.lineTo(1,theThumbPos+dY);
		mThumbWidget.lineTo(THUMB_WIDTH,theThumbPos);
		mThumbWidget.setLastPoint(THUMB_WIDTH,theThumbPos);
	}

	public int getColor() {
		return mColorSet[1];
	}

	public int setColor(int aColor) {
		mColorSet[1] = aColor;
		mGradientPainter.setColor(aColor);
		Shader theShader = new LinearGradient(0,0,0,GRADIENT_HEIGHT,mColorSet,null,Shader.TileMode.REPEAT);
		mGradientPainter.setShader(theShader);
		invalidate();
		return getBrightness();
	}

	public int getBrightness() {
		return BitsColorUtils.interpolateColor(mColorSet, mBrightnessPos);
	}

	public void setBrightness(float aBrightnessValue) {
		if (aBrightnessValue>=0 && aBrightnessValue<=1) {
			mBrightnessPos = aBrightnessValue;
			updateThumbWidget();
			invalidate();
			if (mChangeListener!=null) {
				mChangeListener.onBrightnessChanged(getBrightness());
			}
		}
	};

	@Override
	protected void onDraw(Canvas aCanvas) {
		aCanvas.drawRect(THUMB_WIDTH,0,THUMB_WIDTH+GRADIENT_WIDTH,GRADIENT_HEIGHT,mGradientPainter);
		aCanvas.drawPath(mThumbWidget,mThumbPainter);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(THUMB_WIDTH+GRADIENT_WIDTH+VIEW_MARGIN,GRADIENT_HEIGHT+VIEW_MARGIN);
	}

	@Override
	public boolean onTouchEvent(MotionEvent aEvent) {
		return (mGestureDetector.onTouchEvent(aEvent));
	}

	/*
	 * Gesture Handling
	 */
	GestureDetector mGestureDetector = null;

	private class MyGestureDetector extends SimpleOnGestureListener {

		@Override
		public boolean onSingleTapConfirmed(MotionEvent aEvent) {
			float theNewPos = aEvent.getY()/getHeight();
			//if tap is "near center" just auto-center it (can scroll for fine tuning)
			if (theNewPos>0.4f && theNewPos<0.6f)
				theNewPos = 0.5f;
			setBrightness(theNewPos);
			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			//needed for anything else to work
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			handleSwipe(distanceY,Math.abs(distanceY));
			return true;
		}

	}

	public void handleSwipe(float aDelta, float aVelocity) {
		float theBrightnessDelta = 0f;
		if (aVelocity<50)
			theBrightnessDelta = 0.01f;
		else if (aVelocity<100)
			theBrightnessDelta = 0.05f;
		else if (aVelocity<200)
			theBrightnessDelta = 0.10f;
		if (aDelta>0 && theBrightnessDelta!=0f)
			theBrightnessDelta = -theBrightnessDelta;
		if (theBrightnessDelta!=0f) {
			setBrightness(mBrightnessPos+theBrightnessDelta);
		}
	}

}
