package com.blackmoonit.androidbits.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
 * Delegates the Checkable interface to the desired Checkable subview.
 * ListView uses the Checkable interface of its root ItemView to handle selected functionality.
 * @author Ryan Fischbach
 */
public class RelativeListViewLayout extends RelativeLayout implements Checkable {
	protected int mDelegatedCheckableViewId = 0;
	protected Checkable mCheckableChild = null;
	protected boolean mIchecked = false;

	public RelativeListViewLayout(Context aContext, AttributeSet attrs) {
		super(aContext,attrs);
		setup(aContext,attrs);
	}

	public RelativeListViewLayout(Context aContext, AttributeSet attrs, int defStyle) {
		super(aContext,attrs,defStyle);
		setup(aContext,attrs);
	}

	protected void setup(Context aContext, AttributeSet attrs) {
		int[] theAttrsWanted = getAttrsWanted();
		final TypedArray theAttrs = aContext.obtainStyledAttributes(attrs,theAttrsWanted);
		processWantedAttributes(theAttrs);
		theAttrs.recycle();
	}

	/**
	 * Descendants would override this method to expand the list of interested attributes.
	 * Call super and expand on what it returns.
	 * @return Complete list of attributes the class is interested in.
	 */
	protected int[] getAttrsWanted() {
		return new int[] {
				android.R.attr.autoAdvanceViewId, //not a perfect fit for attr name, but will do for now
			};
	}

	/**
	 * Descendants would override this method to handle any expanded list of attributes.
	 * The parameter is a 0 based array containing all values defined by {@link #getAttrsWanted}.
	 * @param aAttrs - the attributes as defined in the xml file
	 */
	protected int processWantedAttributes(final TypedArray aAttrs) {
		int i = 0;
		mDelegatedCheckableViewId = aAttrs.getResourceId(i++,0);
		return i;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mCheckableChild = getCheckableChild();
	}

	protected Checkable getCheckableChild() {
		for (int i=getChildCount()-1; i>=0; i--) {
			View v = getChildAt(i);
			if (v instanceof Checkable) {
				if (mDelegatedCheckableViewId!=0) {
					if (mDelegatedCheckableViewId==v.getId())
						return (Checkable)v;
				} else {
					return (Checkable)v;
				}
			}
		}
		return null;
	}

	@Override
	public boolean isChecked() {
		if (mCheckableChild!=null) {
			return mCheckableChild.isChecked();
		} else
			return mIchecked;
	}

	@Override
	public void setChecked(boolean aNewValue) {
		if (mCheckableChild!=null) {
			mCheckableChild.setChecked(aNewValue);
		} else
			mIchecked = aNewValue;
	}

	@Override
	public void toggle() {
		if (mCheckableChild!=null) {
			mCheckableChild.toggle();
		} else
			mIchecked = !mIchecked;
	}

}
