package com.blackmoonit.androidbits.app;
/*
 * Copyright (C) 2015 Blackmoon Info Tech Services
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
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Allows a non-Activity context to execute a runnable on the UI thread.
 */
public class UITaskRunner extends Handler {

	public UITaskRunner(Looper aLooper) {
		super(aLooper);
	}

	public UITaskRunner(Context aContext) {
		this(aContext.getMainLooper());
	}

	/**
	 * Run the action on the UI thread found out via the context used.
	 * @param aContext - the context to use (need not be an Activity).
	 * @param aAction - the runnable to execute on the UI thread.
	 */
	static public void runOnUiThread( Context aContext, Runnable aAction ) {
		new UITaskRunner(aContext).post(aAction);
	}

	/**
	 * Non-activities cannot easily show UI widgets like toast, this helper
	 * method will show a constructed Toast object on the UI thread.
	 * @param aToastToShow - constructed Toast: e.g. pass in Toast.makeToast().
	 */
	static public void showToast( final Toast aToastToShow ) {
		if (aToastToShow!=null) {
			new UITaskRunner(aToastToShow.getView().getContext()).post(new Runnable() {
				@Override
				public void run() {
					aToastToShow.show();
				}
			});
		}
	}

	/**
	 * Some non-activities cannot even create the Toast to show as they instead get
	 * <pre>RuntimeException: Can't create handler inside thread that has not called Looper.prepare()</pre>;
	 * this helper method will construct the Toast object and then show it on the UI thread.
	 * @param aContext - the context to use.
	 * @param aMsg - the message to show as a Toast.
	 * @param aToastLength - the length to show the toast.
	 */
	static public void showMsgAsToast(final Context aContext,
			final CharSequence aMsg, final int aToastLength )
	{
		if (aContext!=null && aMsg!=null) {
			new UITaskRunner(aContext).post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(aContext, aMsg, aToastLength).show();
				}
			});
		}
	}

}
