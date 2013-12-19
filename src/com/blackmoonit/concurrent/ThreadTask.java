package com.blackmoonit.concurrent;

/**
 * Simple thread class designed to improve on Java's implementation to provide an interruptable
 * background worker thread.<br>
 * Android has something similar, called AsyncTask, but this class is
 * designed to use Runnables instead of requiring subclasses to be defined.
 * @author Ryan Fischbach
 */
public class ThreadTask extends ThreadInterruptable {
	protected Runnable mTask = null;
	
	/**
	 * Background thread will perform the runnable task once and then exit.
	 * @param aTask - runnable task to perform once
	 */
	public ThreadTask(Runnable aTask) {
		setTask(aTask);
		setProcessName(null);
		setProcessPriority(null);
	}
	
	/**
	 * Builder-chain friendly helper method.
	 * @param aTask - task to perform
	 * @return Returns this object so that a chain-call can be continued.
	 */
	public ThreadTask setTask(Runnable aTask) {
		mTask = aTask;
		if (mTask==null)
			throw new IllegalArgumentException();
		return this;
	}
	
	/**
	 * Execute the task, name the thread, and supply it with a non-normal priority.
	 * Usually only long tasks are given such treatment.
	 * @param aTask - runnable task to perform once
	 * @param aName - used to set the name the thread (debugger friendly!)
	 * @param aPriority - sets the priority of the thread
	 * @see java.lang.Thread.MIN_PRIORITY MIN_PRIORITY = 1
	 * @see java.lang.Thread.NORM_PRIORITY NORM_PRIORITY = 5
	 * @see java.lang.Thread.MAX_PRIORITY MAX_PRIORITY = 10
	 */
	public ThreadTask(Runnable aTask, String aName, Integer aPriority) {
		this(aTask);
		setProcessName(aName);
		setProcessPriority(aPriority);
	}
	
	@Override
	public void runTask() throws InterruptedException {
		if (mTask!=null) {
			mTask.run();
		}
	}
	
	public static ThreadTask runThisTask(Runnable aTask) {
		return (ThreadTask)new ThreadTask(aTask).execute();
	}
	
}
