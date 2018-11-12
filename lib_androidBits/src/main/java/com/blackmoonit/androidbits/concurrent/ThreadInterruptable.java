package com.blackmoonit.androidbits.concurrent;
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

import com.blackmoonit.androidbits.utils.BitsThreadUtils;

import java.util.concurrent.CancellationException;

/**
 * Thread mechanism to overcome the limitations and shortcomings of stopping java threads.
 * @author baracudda
 */
public abstract class ThreadInterruptable extends Thread
{
	protected volatile Thread mBreakman;
	/**
	 * Daemons that want to sleep between task runs would set a positive value in miliseconds.
	 */
	protected Long mDaemonIntermission = null;
	protected Long mDelayTask = null;

	public ThreadInterruptable() {
		mBreakman = this;
		setProcessName(null);
		setProcessPriority(null);
	}

	/**
	 * Handle interruptions and continuous looping if {@link #isDaemon()} is true.
	 */
	@Override
	public void run() {
        Thread thisThread = Thread.currentThread();
		do {
			try {
				if (mDelayTask!=null)
					Thread.sleep(mDelayTask);
				runTask();
				if (!this.isDaemon()) {
					mBreakman = null;
				} else {
					if (!this.isDaemon() || mDaemonIntermission==null)
						yield();
					else
						sleep(mDaemonIntermission);
				}
			} catch (InterruptedException e) {
				mBreakman = null;
			} catch (CancellationException e) {
				mBreakman = null;
			}
		} while ((mBreakman==thisThread) && (!Thread.currentThread().isInterrupted()));
	}

	/**
	 * Task to perform while this thread is active.
	 * @throws InterruptedException a blocking queue might get interrupted.
	 */
	abstract public void runTask() throws InterruptedException;

	/**
	 * Stops this thread if running, interrupting it if necessary.
	 */
	public void halt() {
		if (this.isAlive()) {
			Thread tmpBreakman = mBreakman;
			mBreakman = null;
			if (tmpBreakman != null) {
				tmpBreakman.interrupt();
			}
		}
    }

	/**
	 * Check to see if we've been interrupted, stop if so.
	 * @throws java.util.concurrent.CancellationException if current thread has been interrupted.
	 */
	static protected void ifInterruptedStop() throws CancellationException {
		BitsThreadUtils.ifInterruptedStop();
	}

	/**
	 * Determine if the current thread is the Android activity's UI thread.
	 * @return Returns TRUE if the current thread is the main UI thread.
	 */
	static public boolean isUiThread() {
		return BitsThreadUtils.isUiThread();
	}

	/**
	 * Determine if the current thread is the Android activity's UI thread.
	 * @return Returns TRUE if the current thread is the main UI thread.
	 */
	static public boolean isBackgroundThread() {
		return !BitsThreadUtils.isUiThread();
	}

	/**
	 * Builder-chain friendly helper method.
	 * @param aName - process name, only good for debugging purposes
	 * @return Returns this object so that a chain-call can be continued.
	 */
	public ThreadInterruptable setProcessName(String aName) {
		if (aName!=null) {
			setName(aName);
		}
		return this;
	}

	/**
	 * Builder-chain friendly helper method.
	 * @param aPriority - new priority for the Thread.
	 * @return Returns this object so that a chain-call can be continued.
	 */
	public ThreadInterruptable setProcessPriority(Integer aPriority) {
		if (aPriority==null)
			aPriority = Thread.NORM_PRIORITY-1;
		if (Thread.MIN_PRIORITY<=aPriority && aPriority<=Thread.MAX_PRIORITY)
			setPriority(aPriority);
		return this;
	}

	/**
	 * Checks to see if the thread is not alive before calling start().
	 */
	public ThreadInterruptable execute() {
		if ( !isAlive() ) {
			start();
		}
		return this;
	}

	/**
	 * Checks to see if the thread is not alive before calling start().
	 * @param aDelayInMilliseconds - start task after a delay (in milliseconds).
	 */
	public ThreadInterruptable executeDelayed(long aDelayInMilliseconds) {
		if ( !isAlive() ) {
			mDelayTask = aDelayInMilliseconds;
			start();
		}
		return this;
	}

}
