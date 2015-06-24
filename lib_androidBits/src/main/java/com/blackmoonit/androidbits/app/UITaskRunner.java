package com.blackmoonit.androidbits.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * Allows a non-Activity context to execute a runnable on the UI thread.
 * Created by ryanf on 6/24/15.
 */
public class UITaskRunner extends Handler {

	public UITaskRunner(Looper aLooper) {
		super(aLooper);
	}

	/**
	 * Run the action on the UI thread found out via the context used.
	 * @param aContext - the context to use (need not be an Activity).
	 * @param aAction - the runnable to execute on the UI thread.
	 */
	static public void runOnUiThread( Context aContext, Runnable aAction ) {
		new UITaskRunner(aContext.getMainLooper()).post(aAction);
	}
}
