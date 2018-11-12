package com.blackmoonit.androidbits.utils;
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

import java.util.concurrent.CancellationException;

/**
 * Thread Utilities.
 *
 * @author baracudda
 */
public final class BitsThreadUtils {

	private BitsThreadUtils() {} //do not instantiate this class

	/**
	 * Determine if the current thread is the Android activity's UI thread.
	 * @return Returns TRUE if the current thread is the main UI thread.
	 */
	static public boolean isUiThread() {
		return Thread.currentThread().getName().equals("main");
	}

	/**
	 * Check to see if we've been interrupted, stop if so.
	 * @throws java.util.concurrent.CancellationException if current thread has been interrupted.
	 */
	static public void ifInterruptedStop() throws CancellationException {
	    Thread.yield();
	    if (Thread.currentThread().isInterrupted()) {
			throw new CancellationException();
	    }
	}

}
