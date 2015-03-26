package com.blackmoonit.androidbits.utils;

import java.util.concurrent.CancellationException;

/**
 * Thread Utilities.
 *
 * @author Ryan Fischbach
 */
public final class BitsThreadUtils {

	private BitsThreadUtils() {} //do not instanciate this class

	/**
	 * Determine if the current thread is the Android activity's UI thread.
	 * @return Returns TRUE if the current thread is the main UI thread.
	 */
	static public boolean isUiThread() {
		return Thread.currentThread().getName().equals("main");
	}

	/**
	 * Check to see if we've been interrupted, stop if so.
	 * @throws java.util.concurrent.CancellationException
	 */
	static public void ifInterruptedStop() throws CancellationException {
	    Thread.yield();
	    if (Thread.currentThread().isInterrupted()) {
			throw new CancellationException();
	    }
	}

}
