package com.blackmoonit.androidbits.app;
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

import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Automatically map and manage startActivityForResult calls and returned results.
 * It would be nice to write library code that requires Activity results but does not force
 * specific constants to be created in the app Activity. It would also be nice to create library
 * dialogs as actual dialogs instead of Activities and the only way to use startActivityForResult
 * would be with some kind of manager that can dynamically create result codes that do not conflict
 * with any defined constants in the Activity and manage those results as needed.<br>
 * <br>
 * Create an instance of this class and then override {@link Activity#onActivityResult(int, int, Intent)}
 * so that it will call the same method of the {@link ActivityResult.Manager} class.
 * Example usage after such a setup:<br>
 * <pre class="prettyprint">
 * Intent theOtherActivityIntent = … {create as needed} …
 * int theRequestCode = mActResultMgr.registerResultHandler(new ActivityResult.Handler() {
 *   &#64;Override
 *   public void handleResultOk(Intent aData) {
 *     // code that would handle the RESULT_OK return code in onActivityResult
 *   }
 * });
 * try {
 *   mActResultMgr.startActivityForResult(theOtherActivityIntent,theRequestCode);
 * } catch (ActivityNotFoundException anfe) {
 *   mActResultMgr.unregisterResultHandler(theRequestCode);
 * }
 * </pre><br>
 *
 * @author baracudda
 */
public class ActivityResult {

	/**
	 * Handler class to simplify writing handlers for {@link ActivityResult.Manager}.
	 */
	static public abstract class Handler extends android.os.Handler {

		@Override
		public void handleMessage(Message aMsg) {
			handleResultCode(aMsg.what,(Intent)aMsg.obj);
		}

		public void handleResultCode(int aResultCode, Intent aData) {
			switch (aResultCode) {
				case Activity.RESULT_OK:
					handleResultOk(aData);
					break;
				case Activity.RESULT_CANCELED:
					handleResultCanceled(aData);
					break;
				default:
					handleResultCustom(aData);
					break;
			}
		}

		/**
		 * Called when the Activity result code is Activity.RESULT_OK.
		 * @param aData - the returned Intent data
		 */
		public abstract void handleResultOk(Intent aData);
		//most common use, abstract for ease of implementation (forces you to write one)

		/**
		 * Called when the Activity result code is Activity.RESULT_CANCELED.
		 * @param aData - the returned Intent data
		 */
		@SuppressWarnings("unused")
		public void handleResultCanceled(Intent aData) {
			//default is to do nothing
		}

		/**
		 * Called when the Activity result code is something other than OK or CANCELED.
		 * @param aData - the returned Intent data
		 */
		@SuppressWarnings("unused")
		public void handleResultCustom(Intent aData) {
			//default is to do nothing
		}
	}

	static public class Manager {
		protected static final AtomicInteger mRequestCode = new AtomicInteger();
		protected final WeakReference<Activity> mAct;
		protected final SparseArray<Handler> mHandlerMap;
		protected int[] mRequestCodeExcludes = null;

		/**
		 * A RequestCode manager for dynamic startActivityForResult calls so that static
		 * requestCodes do not need to be defined.
		 * @param anAct - the Activity waiting for a result.
		 */
		public Manager(Activity anAct) {
			mAct = new WeakReference<Activity>(anAct);
			mHandlerMap = new SparseArray<Handler>();
		}

		public Activity getActivity() {
			return mAct.get();
		}

		/**
		 * Existing constants being used by the Activity need to be known to avoid conflicts.
		 * Used in cases where this class is being introduced into existing Activities.
		 * @param aExclusionMap - array of integers that are already in use
		 */
		@SuppressWarnings("unused")
		public void setRequestCodeExclusionMap(int[] aExclusionMap) {
			mRequestCodeExcludes = aExclusionMap;
			Arrays.sort(mRequestCodeExcludes);
		}

		/**
		 * Check to see if a request code is excluded or not.
		 * @param aRequestCode - the code to check.
		 * @return Returns TRUE if excluded, FALSE otherwise.
		 */
		protected boolean isRequestCodeExcluded(int aRequestCode) {
			return mRequestCodeExcludes != null &&
					(Arrays.binarySearch(mRequestCodeExcludes, aRequestCode) >= 0);
		}

		/**
		 * Register a result handler and return the Request Code that should be passed to the called activity.
		 * @param aResultHandler - user defined request handler
		 * @return Returns the request code that must be used in calling {@link #startActivityForResult(Intent,int)} or
		 * {@link Activity#startActivityForResult(Intent,int)}
		 */
		public int registerResultHandler(Handler aResultHandler) {
			int theRequestCode;
			synchronized (mHandlerMap) {
				do {
					theRequestCode = mRequestCode.incrementAndGet();
					if (theRequestCode<1) //handle int overflow
						theRequestCode = 1;
				} while (isRequestCodeExcluded(theRequestCode) ||
						mHandlerMap.indexOfKey(theRequestCode)>=0);
				mHandlerMap.put(theRequestCode,aResultHandler);
			}
			return theRequestCode;
		}

		/**
		 * Given the request code, unregister the handler associated with it.
		 * @param aRequestCode - request code generated by {@link #registerResultHandler}
		 * @return Returns the handler associated with aRequestCode or NULL if it was not registered.
		 */
		public Handler unregisterResultHandler(int aRequestCode) {
			synchronized (mHandlerMap) {
				Handler theResult = mHandlerMap.get(aRequestCode);
				mHandlerMap.remove(aRequestCode);
				return theResult;
			}
		}

		/**
		 * Performs the same function as {@link android.app.Activity#startActivityForResult(android.content.Intent,int)}.
		 * @param aIntent - intent of Activity to be started
		 * @param aRequestCode - request code to use, returned from {@link #registerResultHandler(com.blackmoonit.androidbits.app.ActivityResult.Handler)}
		 */
		public void startActivityForResult(Intent aIntent, int aRequestCode) {
			getActivity().startActivityForResult(aIntent,aRequestCode);
		}

		/**
		 * Called by the Activity's onActivityResult event to handle any unhandled requestCodes.
		 * @param aRequestCode - requestCode of the result, determines which Handler will be called
		 * @param aResultCode - the resultCode of the Activity that returned the data
		 * @param aData - the data returned by the called Activity, may be NULL
		 */
		public void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
			Handler theHandler = mHandlerMap.get(aRequestCode);
			if (theHandler!=null)
				theHandler.dispatchMessage(theHandler.obtainMessage(aResultCode,aData));
			unregisterResultHandler(aRequestCode);
		}

	}

}
