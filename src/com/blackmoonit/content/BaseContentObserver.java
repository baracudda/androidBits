package com.blackmoonit.content;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;

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
	 * Default Uri used by {@link #getNewCursor()}.
	 * @return Returns the default query Uri.
	 */
	protected abstract Uri getUri();

	/**
	 * Perform the query, default method calls query(getUri(),null,null,null,null);<br>
	 * Do not bother calling super.getNewCursor if you override this method.
	 * @return Returns the query cursor result.
	 */
	protected Cursor getNewCursor() {
		return mCR.query(getUri(),null,null,null,null);
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
		onChange(bSelfChange,getUri());
	}
	
	@Override
	public void onChange(boolean bSelfChange, Uri aUri) {
		swapCursor(getNewCursor());
		if (mOnChangeListener!=null) {
			mOnChangeListener.onChange(mCursor,mObserverId,bSelfChange,aUri);
		}
	}

	@SuppressLint("NewApi")
	public void start() {
		mCR.registerContentObserver(getUri(),getIncludeDescendants(),this);
		onChange(true,getUri());
	}

}
