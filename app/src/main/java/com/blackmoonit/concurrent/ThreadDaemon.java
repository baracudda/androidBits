package com.blackmoonit.concurrent;

import java.lang.ref.WeakReference;

import android.app.Activity;

/**
 * Simple thread class designed to improve on Java's implementation to provide an interruptable
 * background worker thread.<br>
 * The Daemon thread will continue to execute the Runnable task in an infinite loop.
 * 
 * @author Ryan Fischbach
 */
public class ThreadDaemon extends ThreadInterruptable {
	protected Runnable mTask = null;
	protected boolean bRunOnUiThread = false;
	protected WeakReference<Activity> wAct = null;

	public ThreadDaemon(Long aIntermission) {
		setIntermission(aIntermission);
		setProcessName(null);
		setProcessPriority(null);
		setDaemon(true);
	}
	
	/**
	 * Execute the task repeatedly and forever until interrupted.
	 * @param aTask - runnable to loop endlessly
	 */
	public ThreadDaemon(Runnable aTask) {
		this(0L);
		setTask(aTask);
	}
	
	/**
	 * Execute the task repeatedly and forever until interrupted. Sleep for given set of milliseconds between
	 * task executions.
	 * @param aTask - runnable to loop endlessly
	 * @param aIntermission - sleep this number of milliseconds between task executions
	 */
	public ThreadDaemon(Long aIntermission, Runnable aTask) {
		this(aIntermission);
		setTask(aTask);
	}
	
	@Override
	public void runTask() throws InterruptedException {
		if (mTask!=null) {
			if (!bRunOnUiThread)
				mTask.run();
			else {
				if (wAct!=null && wAct.get()!=null)
					wAct.get().runOnUiThread(mTask);
				else
					mTask.run();
			}
				
		}
	}
	
	/**
	 * Self halting Daemon Tasks require the task to be defined after the constructor is run if using
	 * inline classes. Builder-chain friendly.
	 * @param aTask - runnable to loop endlessly
	 * @return Returns this object so that a chain-call can be continued.
	 */
	public ThreadDaemon setTask(Runnable aTask) {
		setTask(aTask,false,null);
		return this;
	}
	
	/**
	 * Self halting Daemon Tasks require the task to be defined after the constructor is run if using
	 * inline classes. Builder-chain friendly.
	 * @param aTask - runnable to loop endlessly
	 * @param isRunOnUiThread - if TRUE, will run the task on the UI thread instead of normal background
	 * @param aAct - Activity required if isRunOnUiThread is TRUE, otherwise it is ignored.
	 */
	public ThreadDaemon setTask(Runnable aTask, boolean isRunOnUiThread, Activity aAct) {
		mTask = aTask;
		if (mTask==null)
			throw new IllegalArgumentException();
		bRunOnUiThread = isRunOnUiThread;
		if (aAct!=null)
			wAct = new WeakReference<Activity>(aAct);
		return this;
	}
	
	/**
	 * Sleep for a given set of milliseconds between task executions.
	 * @param aIntermission - sleep this number of milliseconds between task executions
	 * @throws IllegalArgumentException if parameter is < 0.
	 */
	public ThreadDaemon setIntermission(Long aIntermission) {
		if (aIntermission>=0L)
			mDaemonIntermission = (aIntermission!=0L)?aIntermission:null;
		else
			throw new IllegalArgumentException("Intermission must be >= 0.");
		return this;
	}

}
