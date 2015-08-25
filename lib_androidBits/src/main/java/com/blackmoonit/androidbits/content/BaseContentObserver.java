package com.blackmoonit.androidbits.content;
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

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;

/**
 * Self-data-change-registering class used for auto-populating display widgets when
 * data changes.
 *
 * @author Ryan Fischbach
 */
public abstract class BaseContentObserver extends ContentObserver {
	private Context mContext = null; //force descendants to use get and swap methods
	protected ContentResolver mCR = null;
	protected Cursor mCursor = null;
	protected int mObserverId = 0;

	static public interface OnDataChangeListener {
		public void onChange(Cursor aCursor, int aObserverId, boolean bSelfChange, Uri aUri);
	}

	protected OnDataChangeListener mOnChangeListener = null;

	public BaseContentObserver(Context aContext, int aObserverId, OnDataChangeListener aListener) {
		super(null);
		mContext = aContext;
		mCR = mContext.getContentResolver();
		mObserverId = aObserverId;
		mOnChangeListener = aListener;
	}

	public BaseContentObserver(Context aContext, int aObserverId) {
		this(aContext,aObserverId,null);
	}

	public BaseContentObserver(Context aContext) {
		this(aContext,0,null);
	}

	@Override
	protected void finalize() throws Throwable {
		if (mCR!=null) {
			mCR.unregisterContentObserver(this);
		}
		swapCursor(null);
		super.finalize();
	}

	public BaseContentObserver setDataChangeListener(OnDataChangeListener aDataChangeListener) {
		mOnChangeListener = aDataChangeListener;
		return this;
	}

	protected boolean getIncludeDescendants() {
		return false;
	}

	/**
	 * Default Uri used by {@link #getNewCursor()}. If you don't know what to return here, just
	 * return the parameter aUri.
	 * @param Uri aUri - modern Android may include the onChange Uri.
	 * @return Returns the default query Uri.
	 */
	protected abstract Uri getUri(Uri aUri);

	/**
	 * Perform the query, default method calls query(getUri(),null,null,null,null);<br>
	 * Do not bother calling super.getNewCursor if you override this method.
	 * @param Uri aUri - modern Android may include the onChange Uri.
	 * @return Returns the query cursor result.
	 */
	protected Cursor getNewCursor(Uri aUri) {
		return mCR.query(getUri(aUri),null,null,null,null);
	}

	public Cursor getCursor() {
		return mCursor;
	}

	public void swapCursor(Cursor aCursor) {
		if (mCursor!=null && !mCursor.isClosed())
			mCursor.close();
		mCursor = aCursor;
	}

	@Override
	public boolean deliverSelfNotifications() {
		return true;
	}

	@SuppressLint("NewApi")
	@Override
	public void onChange(boolean bSelfChange) {
		onChange(bSelfChange,getUri(null));
	}

	@Override
	public void onChange(boolean bSelfChange, Uri aUri) {
		swapCursor(getNewCursor(aUri));
		if (mOnChangeListener!=null) {
			mOnChangeListener.onChange(mCursor,mObserverId,bSelfChange,aUri);
		}
	}

	@SuppressLint("NewApi")
	public void start() {
		mCR.registerContentObserver(getUri(null),getIncludeDescendants(),this);
		onChange(true,getUri(null));
	}

}
