package com.blackmoonit.androidbits.concurrent;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.blackmoonit.androidbits.app.UITaskRunner;

import java.lang.ref.WeakReference;
import java.util.concurrent.LinkedBlockingQueue;

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
		protected boolean bRunOnUi = false;
		protected WeakReference<Activity> wrAct = null;
		protected WeakReference<Context> wrContext = null;

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
			theResult.bRunOnUi = true;
			theResult.wrAct = new WeakReference<Activity>(aAct);
			return theResult;
		}

		static public TaskDef runThisTaskOnUI(Runnable aTask, Context aContext) {
			TaskDef theResult = new TaskDef();
			theResult.mTask = aTask;
			theResult.bRunOnUi = true;
			theResult.wrContext = new WeakReference<Context>(aContext);
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
			if (theTaskDef.mTask==null)
				return;
			//we have a task to run, see if it needs to run on UI thread
			if (theTaskDef.bRunOnUi) {
				Activity theAct = (theTaskDef.wrAct!=null) ? theTaskDef.wrAct.get() : null;
				Context theContext = (theTaskDef.wrContext!=null) ? theTaskDef.wrContext.get() : null;
				if (theAct!=null) {
					theAct.runOnUiThread(theTaskDef.mTask);
				}
				else if (theContext!=null) {
					UITaskRunner.runOnUiThread(theContext, theTaskDef.mTask);
				}
			}
			else if (theTaskDef.mTask!=null) {
				if (theTaskDef.mName!=null)
					setProcessName(mQueueName+": "+theTaskDef.mName);
				theTaskDef.mTask.run();
				setProcessName(mQueueName);
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
	 * @param aAct - Activity of the UI thread.
	 * @return Returns this object so that a chain-call can be continued.
	 * @throws InterruptedException
	 */
	public ThreadTaskQueue queueTaskForUI(Runnable aTask, Activity aAct) throws InterruptedException {
		if (aTask==null)
			throw new IllegalArgumentException("Queuing up a NULL task, the shame!");
		if (aAct!=null)
			mTaskDefQueue.put(TaskDef.runThisTaskOnUI(aTask, aAct));
		else
			throw new IllegalArgumentException("Activity cannot be null");
		return this;
	}

	/**
	 * Add task that needs to be run on the UI thread to the queue. Builder-chain friendly.
	 * @param aTask - runnable to loop endlessly
	 * @param aContext - Context needed to determine the UI thread.
	 * @return Returns this object so that a chain-call can be continued.
	 * @throws InterruptedException
	 */
	public ThreadTaskQueue queueTaskForUI(Runnable aTask, Context aContext) throws InterruptedException {
		if (aTask==null)
			throw new IllegalArgumentException("Queuing up a NULL task, the shame!");
		if (aContext!=null)
			mTaskDefQueue.put(TaskDef.runThisTaskOnUI(aTask, aContext));
		else
			throw new IllegalArgumentException("Context cannot be null");
		return this;
	}

}
