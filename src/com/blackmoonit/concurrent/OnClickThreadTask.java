package com.blackmoonit.concurrent;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * OnClick drop-in replacement handler that utilizes a background task for its heavy work.
 * Non-trivial tasks should use this class instead of a typical inline OnClickListener so that
 * UI response time is not disrupted on potentially slow jobs - which would cause multiple click
 * events while the user keeps pressing the button/widget trying to make the UI respond.
 * Useful if you want interruptable slow tasks performed in the background.
 * @see RunClickTask
 * @author Ryan Fischbach
 */
public class OnClickThreadTask extends ThreadInterruptable implements OnClickListener {
	
	public interface TaskDef {
		/**
		 * Portion of onClick that needs to be done on UI thread prior to doTask().
		 * @param v - the View that is firing the onClick event.
		 * @return The return value is passed into doTask() as a param.
		 */
		public Object beforeTask(View v);
		
		/**
		 * Actual work to be done is performed off the UI thread.
		 * @param v - the View that is firing the onClick event.
		 * @param aBeforeTaskResult - result of beforeTask().
		 * @return Returns an Object which will be passed as a param into afterTask().
		 */
	    public Object doTask(View v, Object aBeforeTaskResult);
	    
	    /**
	     * After work is done, this part is run on the UI thread.
		 * @param v - the View that is firing the onClick event.
	     * @param aTaskResult - result of doTask().
	     */
	    public void afterTask(View v, Object aTaskResult);
		
	}
	
	protected OnClickThreadTask.TaskDef mTask = null;
	protected View mClickedView = null;
	protected Object mBeforeTaskResult = null;
	
	static protected class InternalHandler extends Handler {
		WeakReference<OnClickThreadTask> mClickTask;
		
		InternalHandler(OnClickThreadTask aClickTask) {
			mClickTask = new WeakReference<OnClickThreadTask>(aClickTask);
		}
		
		@Override
		public void handleMessage(Message msg) {
			OnClickThreadTask theClickTask = mClickTask.get();
			if (msg.what>0 && theClickTask!=null && theClickTask.mTask!=null)
				theClickTask.mTask.afterTask(theClickTask.mClickedView,msg.obj);
		}
	}
	protected final InternalHandler mHandler = new InternalHandler(this);

	/**
	 * Background thread will perform the runnable task once and then exit.
	 * 
	 * @param aTask - runnable task to perform once
	 */
	public OnClickThreadTask(OnClickThreadTask.TaskDef aTask) {
		setTask(aTask);
		setProcessName(null);
		setProcessPriority(null);
	}
	
	/**
	 * Execute the task, name the thread, and supply it with a non-normal priority.
	 * Usually only long tasks are given such treatment.
	 * 
	 * @param aTask - the before/do/after task to perform
	 * @param aName - used to the set name the thread (debugger friendly!)
	 * @param aPriority - sets the priority of the thread
	 * @see java.lang.Thread.MIN_PRIORITY MIN_PRIORITY = 1
	 * @see java.lang.Thread.NORM_PRIORITY NORM_PRIORITY = 5
	 * @see java.lang.Thread.MAX_PRIORITY MAX_PRIORITY = 10
	 */
	public OnClickThreadTask(OnClickThreadTask.TaskDef aTask, String aName, Integer aPriority) {
		setTask(aTask);
		setProcessName(aName);
		setProcessPriority(aPriority);
	}
	
	/**
	 * Builder-chain friendly helper method.
	 * @param aTask - task to perform
	 * @return Returns this object so that a chain-call can be continued.
	 */
	public OnClickThreadTask setTask(OnClickThreadTask.TaskDef aTask) {
		mTask = aTask;
		if (mTask==null)
			throw new IllegalArgumentException();
		return this;
	}
	
	@Override
	public void runTask() throws InterruptedException {
		final Object taskResult = mTask.doTask(mClickedView,mBeforeTaskResult);
		mHandler.obtainMessage(1,taskResult).sendToTarget();
	}

	@Override
	public void onClick(View v) {
		if (!isAlive()) { //also helps prevent multi-clicks from running event more than once
			mClickedView = v;
			execute();
		}
	}

	/**
	 * Starts the task like onClick, but there is no View param.
	 */
	@Override
	public ThreadInterruptable execute() {
		if (!isAlive()) {
			mBeforeTaskResult = mTask.beforeTask(mClickedView);
		}
		return super.execute();
	}
	
	static public OnClickThreadTask runThisTask(TaskDef aTask, String aName) {
		return (OnClickThreadTask)new OnClickThreadTask(aTask, aName, Thread.NORM_PRIORITY).execute();
	}
	
}
