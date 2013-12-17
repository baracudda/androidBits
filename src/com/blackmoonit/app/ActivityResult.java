package com.blackmoonit.app;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;

/**
 * Automatically map and manage startActivityForResult calls and returned results.
 * It would be nice to write library code that requires Activity results but does not force
 * specific constants to be created in the app Activity. It would also be nice to create library
 * dialogs as actual dialogs instead of Activities and the only way to use startActivityForResult
 * would be with some kind of manager that can dynamically create result codes that do not conflict
 * with any defined constants in the Activity and manage those results as needed.<br>
 * <br>
 * Create an instance of this class and then override {@link Activity.onActivityResult(int, int, Intent)}
 * so that it will call the same method of the {@link ActivityResult#Manager} class. 
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
 * @author Ryan Fischbach
 */
public class ActivityResult {
	
	/**
	 * Handler class to simplify writing handlers for {@link ActivityResult#Manager}.
	 */
	static public abstract class Handler extends android.os.Handler {
		
		@Override
		public void handleMessage(Message aMsg) {
			handleResultCode(aMsg.what,(Intent)aMsg.obj);
		};
		
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
		public void handleResultCanceled(Intent aData) {
			//default is to do nothing
		}
		
		/**
		 * Called when the Activity result code is something other than OK or CANCELED.
		 * @param aData - the returned Intent data
		 */
		public void handleResultCustom(Intent aData) {
			//default is to do nothing
		}		
	}

	static public class Manager {
		protected static final AtomicInteger mRequestCode = new AtomicInteger();
		protected final WeakReference<Activity> mAct;
		protected int[] mRequestCodeExcludes = null;
		protected HashMap<Integer, Handler> mHandlerMap;
		
		/**
		 * A RequestCode manager for dynamic startActivityForResult calls so that static reqeustCodes do
		 * not need to be defined.
		 * @param anAct
		 */
		public Manager(Activity anAct) {
			mAct = new WeakReference<Activity>(anAct);
		}
		
		public Activity getActivity() {
			return mAct.get();
		}
		
		/**
		 * Existing constants being used by the Activity need to be known to avoid conflicts.
		 * Used in cases where this class is being introduced into existing Activities.
		 * @param aExclusionMap - array of integers that are already in use
		 */
		public void setRequestCodeExclusionMap(int[] aExclusionMap) {
			mRequestCodeExcludes = aExclusionMap;
			Arrays.sort(mRequestCodeExcludes);
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
				} while (Arrays.binarySearch(mRequestCodeExcludes,theRequestCode)<0 && 
						!mHandlerMap.containsKey(theRequestCode));
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
				return mHandlerMap.remove(aRequestCode);
			}
		}

		/**
		 * Performs the same function as {@link Activity#startActivityForResult(Intent,int)}.
		 * @param aIntent - intent of Activity to be started
		 * @param aRequestCode - request code to use, returned from {@link #registerResultHandler(Handler)}
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
		public void onActivityResult(int aRequestCode, int aResultCode, Intent aData){
			if (mHandlerMap!=null) {
				Handler theHandler = mHandlerMap.get(aRequestCode);
				if (theHandler!=null)
					theHandler.dispatchMessage(theHandler.obtainMessage(aResultCode,aData));
				unregisterResultHandler(aRequestCode);
			}
		}

	}

}