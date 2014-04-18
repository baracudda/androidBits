package com.blackmoonit.concurrent;

import java.util.concurrent.CancellationException;

/**
 * Thread mechanism to overcome the limitations and shortcomings of stopping java threads.
 * @author Ryan Fischbach
 */
public abstract class ThreadInterruptable extends Thread {
	private volatile Thread mBreakman;
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
	 * Handle interruptions and continuous looping if {@link isDeamon} is true.
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
	 * @throws InterruptedException
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
	 * @throws CancellationException
	 */
	protected static void ifInterruptedStop() throws CancellationException {
	    Thread.yield();
	    if (Thread.currentThread().isInterrupted()) {
			throw new CancellationException();
	    }
	}
	
	/**
	 * Determine if the current thread is the Android activity's UI thread.
	 * @return Returns TRUE if the current thread is the main UI thread.
	 */
	public static boolean isUiThread() {
		return Thread.currentThread().getName().equals("main");
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
		if (!isAlive()) {
			start();
		}
		return this;
	}
	
	/**
	 * Checks to see if the thread is not alive before calling start().
	 */
	public ThreadInterruptable executeDelayed(long aDelayInMiliseconds) {
		if (!isAlive()) {
			mDelayTask = aDelayInMiliseconds;
			start();
		}
		return this;
	}
	
}
