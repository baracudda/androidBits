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
