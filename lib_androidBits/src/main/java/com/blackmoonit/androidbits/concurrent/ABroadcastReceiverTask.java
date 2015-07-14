package com.blackmoonit.androidbits.concurrent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver that does its work in a background thread.
 */
abstract public class ABroadcastReceiverTask extends BroadcastReceiver {
	protected String mTaskName = "background onReceive task";

	public ABroadcastReceiverTask() {
		super();
	}

	public ABroadcastReceiverTask(String aTaskName) {
		this();
		mTaskName = aTaskName;
	}

	abstract public void doWhenReceived(Context context, Intent intent);

	@Override
	public void onReceive(Context context, Intent intent) {
		final Context theContext = context;
		final Intent theIntent = intent;
		ThreadTask.runThisTask(new Runnable() {
			@Override
			public void run() {
				doWhenReceived(theContext, theIntent);
			}
		}, mTaskName);
	}
}
