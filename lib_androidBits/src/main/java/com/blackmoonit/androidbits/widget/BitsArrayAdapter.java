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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * BITS framework ideas around the ArrayAdapter.
 *
 * @author Ryan Fischbach
 */
public abstract class BitsArrayAdapter<T> extends ArrayAdapter<T> {
	protected int mItemLayoutResId = 0;
	protected int mFontSize = 0;
	protected float mCompareFontSize = 0;
	protected int[] mResizableTextViews = null;

	public BitsArrayAdapter(Context aContext, int aItemLayoutResId, List<T> aList) {
		super(aContext,aItemLayoutResId,aList);
		mItemLayoutResId = aItemLayoutResId;
	}

	/**
	 * Descendants overwrite this method to supply an initial capacity, if desired.
	 * @return Returns the ViewCacher constructed.
	 */
	protected ViewCacher getNewViewCacher() {
		return new ViewCacher();
	}

	/**
	 * Convenience method to find/store the handle to a subview for speedy retrieval later.
	 *
	 * @param aParentView - container view
	 * @param aViewToFind - Resource ID of the subview to find
	 * @return Returns the cached view, finding and caching if necessary.
	 */
	public View getViewHandle(View aParentView, int aViewToFind) {
		ViewCacher vc = (ViewCacher)aParentView.getTag();
		if (vc==null) {
			vc = getNewViewCacher();
			aParentView.setTag(vc);
		}
		return vc.getViewHandle(aParentView, aViewToFind);
	}

	/**
	 * Set the font size of various item elements.
	 *
	 * @param aItemView - the View used inside {@link #getView(int, android.view.View, android.view.ViewGroup)}
	 */
	public void applyFontSize(View aItemView) {
		if (mFontSize>0 && aItemView!=null && mResizableTextViews!=null && mResizableTextViews.length>0) {
			TextView v = (TextView)getViewHandle(aItemView,mResizableTextViews[0]);
			if (Float.compare(mCompareFontSize,v.getTextSize())!=0) {
				v.setTextSize(mFontSize);
				for (int i=1; i<mResizableTextViews.length; i++) {
					v = (TextView)getViewHandle(aItemView,mResizableTextViews[i]);
					v.setTextSize(mFontSize);
				}
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup aParent) {
		if (position<0 || position>=getCount())
			return convertView;

		View theItemView = convertView;
		if (theItemView==null) {
			theItemView = LayoutInflater.from(getContext()).inflate(mItemLayoutResId,null);
		}
		applyFontSize(theItemView);
		return applyItemView(getItem(position),theItemView);
	}

	/**
	 * Set all the various pieces of the item view.
	 * @param anItemView - View containing all views needed to display an item
	 * @return Returns the View passed in.
	 */
	abstract public View applyItemView(T anItem, View anItemView);

	/**
	 * Set the text size used for it's item views.
	 *
	 * @param aSize - font size to use (in dips)
	 */
	public void setTextSize(int aSize) {
		if (aSize!=mFontSize && aSize>0) {
			mFontSize = aSize;
			mCompareFontSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
					mFontSize,getContext().getResources().getDisplayMetrics()));
		}
	}

}
