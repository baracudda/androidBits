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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * Service class compatible with pre-Android 2.0 devices and 3.0+ devices (.setForeground() removed)
 *
 * @author Ryan Fischbach
 */
public abstract class AppService extends Service {
	private static final String TAG = "androidBits.AppService";
	protected NotificationManager mNotificationMgr = null;
	private static final Class<?>[] mSetForegroundSignature = new Class[] {boolean.class};
	private static final Class<?>[] mStartForegroundSignature = new Class[] {int.class, Notification.class};
	private static final Class<?>[] mStopForegroundSignature = new Class[] {boolean.class};
	private Method mSetForeground = null;
	private Method mStartForeground = null;
	private Method mStopForeground = null;
	private Object[] mSetForegroundArgs = new Object[1];
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];

	void invokeMethod(Method method, Object[] args) {
		try {
			method.invoke(this,args);
		} catch (InvocationTargetException e) {
			// Should not happen.
			Log.w(TAG,"Unable to invoke method "+method.getName(),e);
		} catch (IllegalAccessException e) {
			// Should not happen.
			Log.w(TAG,"Unable to invoke method "+method.getName(),e);
		}
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	protected void startForegroundCompat(int id, Notification aNotification) {
		// If we have the new startForeground API, then use it. 2.0+
		if (mStartForeground != null) {
			mStartForegroundArgs[0] = Integer.valueOf(id);
			mStartForegroundArgs[1] = aNotification;
			invokeMethod(mStartForeground, mStartForegroundArgs);
		} else if (mSetForeground!=null) {
			// Fall back on the old API (deprecated in 2.0, gone in 3.0)
			mSetForegroundArgs[0] = Boolean.TRUE;
			invokeMethod(mSetForeground, mSetForegroundArgs);
			mNotificationMgr.notify(id, aNotification);
		}
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	protected void stopForegroundCompat(int id) {
		// If we have the new stopForeground API, then use it. 2.0+
		if (mStopForeground != null) {
			mStopForegroundArgs[0] = Boolean.TRUE;
			invokeMethod(mStopForeground,mStopForegroundArgs);
		} else if (mSetForeground!=null) {
			// Fall back on the old API (deprecated in 2.0, gone in 3.0)
			// Note to cancel BEFORE changing the foreground state,
			//   since we could be killed at that point.
			mNotificationMgr.cancel(id);
			mSetForegroundArgs[0] = Boolean.FALSE;
			invokeMethod(mSetForeground, mSetForegroundArgs);
		}
	}

	@Override
	public void onCreate() {
		mNotificationMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		try {
			//2.0+
			mStartForeground = getClass().getMethod("startForeground",mStartForegroundSignature);
			mStopForeground = getClass().getMethod("stopForeground",mStopForegroundSignature);
		} catch (NoSuchMethodException e2) {
			// Fall back on the old API (deprecated in 2.0, gone in 3.0)
	        try {
	            mSetForeground = getClass().getMethod("setForeground",mSetForegroundSignature);
	        } catch (NoSuchMethodException e1) {
	            throw new IllegalStateException(
	                    "OS doesn't have Service.startForeground OR Service.setForeground!");
	        }
		}
	}

	/**
	 * This is the old onStart method that will be called on the pre-2.0
	 * platform.  On 2.0 or later we override onStartCommand() so this
	 * method will not be called.
	 */
	@Override
	public void onStart(Intent aIntent, int startId) {
		handleCommand(aIntent);
	}

	/**
	 * Android 2.0+ calls this method instead of the old onStart(Intent, in) method.
	 */
	//@Override - we need to work with Android 1.5 which doesn't have this method
	public int onStartCommand(Intent aIntent, int aFlags, int startId) {
		handleCommand(aIntent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return 1;//START_STICKY;
	}

	/**
	 * Equivalent to {@link #onStartCommand(android.content.Intent, int, int)}.
	 *
	 * @param aIntent - the Intent used to startService()
	 */
	public abstract void handleCommand(Intent aIntent);

	/**
	 * Run the action on the UI thread found out via the context used.
	 * @param aAction - the runnable to execute on the UI thread.
	 * @return Returns "this" for chaining purposes.
	 */
	public <T extends AppService> T runOnUiThread( Runnable aAction ) {
		new android.os.Handler(getMainLooper()).post(aAction);
		return (T)this;
	}

	/**
	 * Services cannot easily show UI widgets like toast, this helper
	 * method will show a constructed Toast object on the UI thread.
	 * @param aToastToShow - constructed Toast: e.g. pass in Toast.makeToast().
	 * @return Returns "this" for chaining purposes.
	 */
	public <T extends AppService> T showToast( final Toast aToastToShow ) {
		if (aToastToShow!=null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					aToastToShow.show();
				}
			});
		}
		return (T)this;
	}

}
