package com.blackmoonit.androidbits.app;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.CursorAdapter;

import com.blackmoonit.androidbits.concurrent.ThreadTask;

/**
 * Class that can manage all the cursor loaders for spinner/list adapters in the app.
 *
 * @author Ryan Fischbach
 */
public class AppCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {
	/**
	 * URI the Cursor will use. A default URI will be used if this is null. OPTIONAL.
	 */
	public static final String KEY_CURSOR_URI = "bundle_key_cursor_uri";
	/**
	 * Cursor projection. OPTIONAL.
	 */
	public static final String KEY_PROJECTION = "bundle_key_projection";
	/**
	 * Cursor selection clause. OPTIONAL.
	 */
	public static final String KEY_SELECTION = "bundle_key_selection";
	/**
	 * Cursor selection arguements. OPTIONAL.
	 */
	public static final String KEY_SELECTION_ARGS = "bundle_selection_args";
	/**
	 * Cursor sort order. OPTIONAL.
	 */
	public static final String KEY_SORT_ORDER = "bundle_key_sort_order";

	/**
	 * Called after the loader finishes loading and swapping the cursor into the adapter.
	 */
	static public interface OnLoadFinishedListener {
		void onLoadFinished(AppCursorLoader anAppLoader, int aCursorId, Cursor aCursor);
	}

	/**
	 * Called after the loader finishes swapping the cursor to NULL in the adapter.
	 */
	static public interface OnLoadResetListener {
		void onLoadReset(AppCursorLoader anAppLoader, int aCursorId);
	}

	/**
	 * Called when using {@link #createCursorInfo(int)} method.
	 */
	static public interface OnCreateCursorInfo {
		Bundle onCreateCursorInfo(AppCursorLoader anAppLoader, int aCursorId);
	}

	protected class WrCursorAdapter extends WeakReference<CursorAdapter> {
		public WrCursorAdapter(CursorAdapter aRef) {
			super(aRef);
		}
	}

	protected SparseArray<WrCursorAdapter> mAdapterMap = new SparseArray<WrCursorAdapter>();
	protected SparseArray<OnCreateCursorInfo> mCreateCursorInfoMap = new SparseArray<OnCreateCursorInfo>();
	protected ArrayList<OnLoadFinishedListener> mLoadFinishedHandlers = new ArrayList<OnLoadFinishedListener>();
	protected ArrayList<OnLoadResetListener> mLoadResetHandlers = new ArrayList<OnLoadResetListener>();
	protected final WeakReference<Context> mContext;

	public AppCursorLoader(Context aContext) {
		mContext = new WeakReference<Context>(aContext);
		if (aContext instanceof OnLoadFinishedListener) {
			addLoadFinishedListener((OnLoadFinishedListener)aContext);
		}
		if (aContext instanceof OnLoadResetListener) {
			addLoadResetListener((OnLoadResetListener)aContext);
		}
	}

	public Context getContext() {
		return mContext.get();
	}

	public AppCursorLoader addLoadFinishedListener(OnLoadFinishedListener aListener) {
		mLoadFinishedHandlers.add(aListener);
		return this;
	}

	public AppCursorLoader removeLoadFinishedListener(OnLoadFinishedListener aListener) {
		mLoadFinishedHandlers.remove(aListener);
		return this;
	}

	public AppCursorLoader addLoadResetListener(OnLoadResetListener aListener) {
		mLoadResetHandlers.add(aListener);
		return this;
	}

	public AppCursorLoader removeLoadResetListener(OnLoadResetListener aListener) {
		mLoadResetHandlers.remove(aListener);
		return this;
	}

	public AppCursorLoader addCreateCursorInfoListener(int aCursorId, OnCreateCursorInfo aListener) {
		mCreateCursorInfoMap.put(aCursorId,aListener);
		return this;
	}

	public AppCursorLoader removeCreateCursorInfoListener(int aCursorId) {
		mCreateCursorInfoMap.remove(aCursorId);
		return this;
	}

	protected Uri getCursorUri(int aCursorId) {
		return null;
	}

	public AppCursorLoader addAdapter(int aCursorId, CursorAdapter aAdapter) {
		if (aAdapter==null)
			throw new IllegalArgumentException("aAdapter is NULL.");
		mAdapterMap.put(aCursorId,new WrCursorAdapter(aAdapter));
		if (aAdapter instanceof OnCreateCursorInfo) {
			addCreateCursorInfoListener(aCursorId,(OnCreateCursorInfo)aAdapter);
		}
		return this;
	}

	public CursorAdapter getAdapter(int aCursorId) {
		CursorAdapter theAdapter = mAdapterMap.get(aCursorId).get();
		if (theAdapter==null)
			throw new IllegalStateException("No CursorAdapter found with ID of "+aCursorId+". Please use addAdapter first.");
		return theAdapter;
	}

	public AppCursorLoader restartLoader(LoaderManager aMgr, int aCursorId) {
		if (aMgr!=null) {
			aMgr.restartLoader(aCursorId,createCursorInfo(aCursorId),this);
		} else {
			Loader<Cursor> theCursorLoader = onCreateLoader(aCursorId,createCursorInfo(aCursorId));
			theCursorLoader.startLoading();
		}
		return this;
	}

	/**
	 * Helper for {@link #restartLoader()} so that adapters with OnCreateCursorInfo implemented
	 * actually define the cursor to be created.
	 * @param aCursorId - cursor's ID
	 * @return - Bundle to be used during {@link #onCreateLoader()}.
	 */
	public Bundle createCursorInfo(int aCursorId) {
		OnCreateCursorInfo doCreateCursorInfo = mCreateCursorInfoMap.get(aCursorId);
		if (doCreateCursorInfo!=null) {
			return doCreateCursorInfo.onCreateCursorInfo(this,aCursorId);
		} else {
			return new Bundle();
		}
	}

	/**
	 * Helper function to create a bundle with the Uri as the only info needed for the cursor.
	 * This info gets passed into the {@link #onCreateLoader()} method so that a cursor is created from it.
	 * @param aCursorUri - cursor's Uri
	 * @return Returns the bundle needed by the {@link android.app.LoaderManager#initLoader()} method.
	 */
	public Bundle createCursorInfo(Uri aCursorUri) {
		Bundle theResult = new Bundle();
		theResult.putString(KEY_CURSOR_URI,aCursorUri.toString());
		return theResult;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int aCursorId, Bundle aCursorInfo) {
		if (aCursorInfo==null)
			throw new IllegalArgumentException("aCursorInfo is NULL.");
		Uri theUri;
		try {
			theUri = Uri.parse(aCursorInfo.getString(KEY_CURSOR_URI));
		} catch (NullPointerException npe) {
			theUri = getCursorUri(aCursorId);
		}
		//if aCursorInfo did not have Uri defined and descendant class also does not... we have a problem.
		if (theUri==null)
			throw new IllegalStateException("No URI defined.");
		return new CursorLoader(getContext(),theUri,aCursorInfo.getStringArray(KEY_PROJECTION),
				aCursorInfo.getString(KEY_SELECTION),aCursorInfo.getStringArray(KEY_SELECTION_ARGS),
				aCursorInfo.getString(KEY_SORT_ORDER));
	}

	protected void swapAdapterCursor(int aAdapterId, Cursor aCursor) {
		CursorAdapter theAdapter = mAdapterMap.get(aAdapterId).get();
		if (theAdapter!=null) {
			theAdapter.swapCursor(aCursor);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> aLoader, final Cursor aCursor) {
		final int theLoaderId = aLoader.getId();
		// Swap the new cursor in.  (The framework will take care of closing the old cursor once we return.)
		swapAdapterCursor(theLoaderId,aCursor);
		//notify our listeners
		ThreadTask t = new ThreadTask(new Runnable() {
			@Override
			public void run() {
				Iterator<OnLoadFinishedListener> theHandlers = mLoadFinishedHandlers.iterator();
				while (theHandlers!=null && theHandlers.hasNext()) {
					theHandlers.next().onLoadFinished(AppCursorLoader.this,theLoaderId,aCursor);
				}
			}
		},"onLoadFinished-"+theLoaderId,Thread.NORM_PRIORITY);
		t.executeDelayed(10);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> aLoader) {
		final int theLoaderId = aLoader.getId();
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed.  We need to make sure we are no longer using it.
		swapAdapterCursor(aLoader.getId(),null);
		//notify our listeners
		ThreadTask t = new ThreadTask(new Runnable() {
			@Override
			public void run() {
				Iterator<OnLoadResetListener> theHandlers = mLoadResetHandlers.iterator();
				while (theHandlers!=null && theHandlers.hasNext()) {
					theHandlers.next().onLoadReset(AppCursorLoader.this,theLoaderId);
				}
			}
		},"onLoadReset-"+theLoaderId,Thread.NORM_PRIORITY);
		t.executeDelayed(10);
	}


}
