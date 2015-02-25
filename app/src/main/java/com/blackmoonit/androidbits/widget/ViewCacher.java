package com.blackmoonit.androidbits.widget;

import android.util.SparseArray;
import android.view.View;

/**
 * Storage holder for findViewById() calls so that it can be conveniently placed in setTag().
 *
 * @author Ryan Fischbach
 */
public class ViewCacher {

	protected final SparseArray<View> mCache;

	/**
	 * Constructs a new, empty cacher.
	 */
	public ViewCacher() {
		mCache = new SparseArray<View>();
	}

	/**
	 * Constructs a new cacher with an initial capacity. The size will grow as needed.
	 * @param aInitialCapacity - starting capacity of cache
	 */
	public ViewCacher(int aInitialCapacity) {
		mCache = new SparseArray<View>(aInitialCapacity);
	}

	/**
	 * Get the cached View, finding and caching it if not cached yet.
	 *
	 * @param aParentView - view container
	 * @param aViewToFind - subview to find within the container
	 * @return Returns the subview handle requested.
	 */
	public View getViewHandle(View aParentView, Integer aViewToFind) {
		View v = mCache.get(aViewToFind);
		if (v==null) {
			v = aParentView.findViewById(aViewToFind);
			mCache.put(aViewToFind,v);
		}
		return v;
	}

}
