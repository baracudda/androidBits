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
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.blackmoonit.androidbits.utils.BitsColorUtils;

/**
 * ColorHueView is a view used to pick different colors based on a color wheel concept.
 * The code is influenced from the Android API demo ColorPickerDialog, but made into a View.
 *
 * @author Ryan Fischbach
 */
public class ColorHueView extends View {
	private DisplayMetrics currDisplayMetrics = null;
	private int CENTER_X = 100;
	private int CENTER_Y = 100;
	private int CENTER_RADIUS = 32;
	private int VIEW_MARGIN = 4;
	private final int[] mColorSet = new int[] {
			0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
			0xFFFFFF00, 0xFFFF0000
	};
    private Paint mCenterButton;
    private Paint mOuterContainer;
    private OnColorHueSelectedListener mSelectedListener;
    private OnColorHueChangedListener mChangedListener;
    private boolean mTrackingCenter = false;
    private boolean mHighlightCenter = false;

    public interface OnColorHueChangedListener {
        void onColorHueChanged(int aColor);
    }

    public interface OnColorHueSelectedListener {
        void onColorHueSelected(int aColor);
    }

    public void setOnHueChangedListener(OnColorHueChangedListener aListener) {
        mChangedListener = aListener;
    }

    public void setOnHueSelectedListener(OnColorHueSelectedListener aListener) {
        mSelectedListener = aListener;
    }

	protected int dipsToPixels(int aDip) {
		float px;
		px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, aDip, currDisplayMetrics);
		return Math.round(px);
	}

    public ColorHueView(Context aContext, AttributeSet aAttrSet) {
    	super(aContext, aAttrSet);
    	initialize(Color.WHITE);
    }

	public ColorHueView(Context aContext, OnColorHueChangedListener aHueChangedListener) {
        super(aContext);
        initialize(Color.WHITE);
        mChangedListener = aHueChangedListener;
    }

	public ColorHueView(Context aContext, OnColorHueChangedListener aHueChangedListener, int aColor) {
        super(aContext);
        initialize(aColor);
        mChangedListener = aHueChangedListener;
    }

	protected void initialize(int aInitialColor) {
		currDisplayMetrics = getContext().getResources().getDisplayMetrics();
		CENTER_X = dipsToPixels(CENTER_X);
		CENTER_Y = dipsToPixels(CENTER_Y);
		CENTER_RADIUS = dipsToPixels(CENTER_RADIUS);

		mOuterContainer = new Paint(Paint.ANTI_ALIAS_FLAG);
		Shader s = new SweepGradient(0,0,mColorSet,null);
		mOuterContainer.setShader(s);
		mOuterContainer.setStyle(Paint.Style.STROKE);
		mOuterContainer.setStrokeWidth(dipsToPixels(48));

		mCenterButton = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterButton.setStrokeWidth(5);
		mCenterButton.setStyle(Paint.Style.FILL);
		setColor(aInitialColor);
	}

    public void setColor(int aColor) {
        mCenterButton.setColor(aColor);
        invalidate();
    }

    private RectF mOnDrawTempCenterPoint = new RectF();

    @Override
    protected void onDraw(Canvas aCanvas) {
        float theRadius = CENTER_X - (mOuterContainer.getStrokeWidth()/2);
        aCanvas.translate(CENTER_X, CENTER_Y);
        mOnDrawTempCenterPoint.set(-theRadius,-theRadius,theRadius,theRadius);
        aCanvas.drawOval(mOnDrawTempCenterPoint,mOuterContainer);
        aCanvas.drawCircle(0,0,CENTER_RADIUS,mCenterButton);

        if (mTrackingCenter) {
            int saveColor = mCenterButton.getColor(); //setting Alpha will change the color
            mCenterButton.setStyle(Paint.Style.STROKE);
            mCenterButton.setAlpha((mHighlightCenter)?0xFF:0x80);
            aCanvas.drawCircle(0,0,CENTER_RADIUS+mCenterButton.getStrokeWidth(),mCenterButton);
            mCenterButton.setStyle(Paint.Style.FILL);
            mCenterButton.setColor(saveColor);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int theSize = Math.round(CENTER_X*2)+VIEW_MARGIN;
        setMeasuredDimension(theSize,theSize);
    }

    @Override
    public boolean onTouchEvent(MotionEvent aEvent) {
        float x = aEvent.getX()-CENTER_X;
        float y = aEvent.getY()-CENTER_Y;
        boolean isInCenter = (Math.sqrt(x*x + y*y) <= CENTER_RADIUS);

        switch (aEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTrackingCenter = isInCenter;
                if (isInCenter) {
                    mHighlightCenter = true;
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                if (mTrackingCenter) {
                    if (mHighlightCenter != isInCenter) {
                        mHighlightCenter = isInCenter;
                        invalidate();
                    }
                } else {
                    float theAngle = (float) Math.atan2(y,x);
                    // need to turn angle [-PI … PI] into unit [0….1]
                    float theColorPoint = theAngle/(2*(float)Math.PI);
                    if (theColorPoint<0) {
                    	theColorPoint += 1;
                    }
                    int theColor = BitsColorUtils.interpolateColor(mColorSet,theColorPoint);
                    mCenterButton.setColor(theColor);
                    if (mChangedListener!=null)
                    	mChangedListener.onColorHueChanged(theColor);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTrackingCenter) {
                    if (isInCenter && mSelectedListener!=null) {
                        mSelectedListener.onColorHueSelected(mCenterButton.getColor());
                    }
                    mTrackingCenter = false;
                    invalidate();
                }
                break;
        }
        return true;
    }
}
