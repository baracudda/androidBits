package com.blackmoonit.androidbits.concurrent;

import java.lang.ref.WeakReference;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.util.Log;

/**
 * ThreadTaskQueue is a Daemon thread that will continue to execute the Runnable tasks in its
 * queue, in FIFO order, and keep doing so as they come in.  An empty queue blocks this thread
 * until a task is submitted, waking it up.
 *
 * @author Ryan Fischbach
 */
public class ThreadTaskQueue extends ThreadInterruptable {

	/**
	 * Tasks may need to be executed in the background or on the
	 * UI thread. Use of this class allows the queue to execute
	 * the task in the appropriate thread.
	 */
	static public class TaskDef {
		protected Runnable mTask = null;
		protected String mName = null;
		protected WeakReference<Activity> wAct = null;

		public TaskDef() {
			super();
		}

		static public TaskDef runThisTask(Runnable aTask, String aName) {
			TaskDef theResult = new TaskDef();
			theResult.mTask = aTask;
			theResult.mName = aName;
			return theResult;
		}

		static public TaskDef runThisTaskOnUI(Runnable aTask, Activity aAct) {
			TaskDef theResult = new TaskDef();
			theResult.mTask = aTask;
			theResult.wAct = new WeakReference<Activity>(aAct);
			return theResult;
		}
	}

	protected class TaskDefQueue extends LinkedBlockingQueue<TaskDef> {
		private static final long serialVersionUID = 1997118084770061386L;

		public TaskDefQueue() {
			super();
		}
	}

	protected String mQueueName = TaskDefQueue.class.getSimpleName();
	protected TaskDefQueue mTaskDefQueue = new TaskDefQueue();

	public ThreadTaskQueue() {
		setProcessName(mQueueName);
		setProcessPriority(null);
		setDaemon(true);
	}

	@Override
	public void runTask() throws InterruptedException {
		if (!isInterrupted()) try {
			//blocking operation, thread halts here until queue is not empty
			TaskDef theTaskDef = mTaskDefQueue.take();
			//theTaskDef is guaranteed to be non-null as the queue
			//  itself will not accept NULL values.
			Activity theAct = (theTaskDef.wAct!=null) ? theTaskDef.wAct.get() : null;
			if (theTaskDef.mTask!=null && theAct==null) {
				if (theTaskDef.mName!=null)
					setProcessName(mQueueName+": "+theTaskDef.mName);
				theTaskDef.mTask.run();
				setProcessName(mQueueName);
			} else {
				theAct.runOnUiThread(theTaskDef.mTask);
			}
		} catch (InterruptedException ie) {
			Log.i(getName(), mQueueName+" was interrupted.");
		}
	}

	/**
	 * Add task to the queue. Builder-chain friendly.
	 * @param aTask - runnable to loop endlessly
	 * @return Returns this object so that a chain-call can be continued.
	 * @throws InterruptedException
	 */
	public ThreadTaskQueue queueTask(Runnable aTask, String aName) throws InterruptedException {
		if (aTask==null)
			throw new IllegalArgumentException("Queuing up a NULL task, the shame!");
		mTaskDefQueue.put(TaskDef.runThisTask(aTask,aName));
		return this;
	}

	/**
	 * Add task that needs to be run on the UI thread to the queue. Builder-chain friendly.
	 * @param aTask - runnable to loop endlessly
	 * @param aAct - Activity of the UI thread (optional, but if NULL, {@link #queueTask(Runnable)} is used).
	 * @return Returns this object so that a chain-call can be continued.
	 * @throws InterruptedException
	 */
	public ThreadTaskQueue queueTask(Runnable aTask, Activity aAct) throws InterruptedException {
		if (aTask==null)
			throw new IllegalArgumentException("Queuing up a NULL task, the shame!");
		if (aAct!=null)
			mTaskDefQueue.put(TaskDef.runThisTaskOnUI(aTask, aAct));
		else
			queueTask(aTask,(String)null);
		return this;
	}

}
