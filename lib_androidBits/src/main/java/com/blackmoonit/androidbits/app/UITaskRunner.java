package com.blackmoonit.androidbits.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Allows a non-Activity context to execute a runnable on the UI thread.
 * Created by ryanf on 6/24/15.
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
	 * Non-activies cannot easily show UI widgets like toast, this helper
	 * method will show a constructed Toast object on the UI thread.
	 * @param aToastToShow - constructed Toast: e.g. pass in Toast.makeToast().
	 * @return Returns "this" for chaining purposes.
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

}
