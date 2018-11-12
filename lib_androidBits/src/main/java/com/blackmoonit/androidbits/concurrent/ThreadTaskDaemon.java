package com.blackmoonit.androidbits.concurrent;
/*
 * Copyright (C) 2018 Blackmoon Info Tech Services
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

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.AbstractQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ThreadTaskDaemon is a Daemon thread that will continue to execute the Runnable tasks in its
 * queue, and keeps doing so as the queue gets filled. If you use a queue that implements
 * BlockingQueue, then an empty queue will block this thread until a task is submitted,
 * waking it up.
 */
@SuppressWarnings("unused, UnusedReturnValue")
public class ThreadTaskDaemon extends ThreadInterruptable
{
	protected AbstractQueue<TaskToRun> mTaskQueue;
	
	/** @return Creates and returns the queue instance to use if none has been set, yet. */
	protected AbstractQueue<TaskToRun> createNewQueue()
	{ return new LinkedBlockingQueue<TaskToRun>(); }
	
	/**
	 * Set our specific queue instance to be used for determining task order.
	 * @param aQueue - the instance of AbstractQueue to be used by this daemon.
	 * @return Returns this object so that a chain-call can be continued.
	 */
	protected ThreadTaskDaemon setQueueToUse( AbstractQueue<TaskToRun> aQueue )
	{
		if ( !isAlive() ) {
			mTaskQueue = aQueue;
			if (aQueue != null && TextUtils.isEmpty(getName())) {
				setProcessName(aQueue.getClass().getSimpleName());
			}
		}
		return this;
	}
	
	public ThreadTaskDaemon()
	{
		super();
		setDaemon(true);
	}
	
	/**
	 * Create the deamon thread queue using this specific queue object.
	 * @param aQueue - the specific queue object to use.
	 */
	public ThreadTaskDaemon( AbstractQueue<TaskToRun> aQueue )
	{
		super();
		setQueueToUse(aQueue);
	}
	
	@Override
	public void run()
	{
		if ( mTaskQueue == null )
		{ setQueueToUse(createNewQueue()); }
		super.run();
	}
	
	/**
	 * Find a specific task in the queue and return it.
	 * @param aID - the ID of the task to search for.
	 * @return Returns the TaskToRun, if found.
	 */
	public TaskToRun findTaskByID( String aID )
	{
		if ( aID != null && mTaskQueue != null ) {
			for (TaskToRun theTask : mTaskQueue) {
				if ( aID.equals(theTask.getTaskID()) ) {
					return theTask;
				}
			}
		}
		return null;
	}

	/**
	 * Find a specific task in the queue and return it.
	 * @param aName - the name of the task to search for.
	 * @return Returns the TaskToRun, if found.
	 */
	public TaskToRun findTaskByName( String aName )
	{
		if ( aName != null && mTaskQueue != null ) {
			for (TaskToRun theTask : mTaskQueue) {
				if ( aName.equals(theTask.getTaskName()) ) {
					return theTask;
				}
			}
		}
		return null;
	}
	
	/**
	 * Submit the task into our queue for processing.
	 * @param aTask - the task to execute.
	 * @return Returns TRUE if successfully added to the queue.
	 * @throws InterruptedException a blocking queue might get interrupted.
	 */
	@SuppressWarnings("unchecked cast,UnusedReturnValue")
	public boolean pushTask( TaskToRun aTask ) throws InterruptedException
	{
		if (aTask==null)
			throw new IllegalArgumentException("Queuing up a NULL task, the shame!");
		if ( mTaskQueue instanceof BlockingQueue<?> ) {
			((BlockingQueue<TaskToRun>)mTaskQueue).put(aTask);
			return true;
		}
		else {
			return mTaskQueue.add(aTask);
		}
	}
	
	/**
	 * Pop off the next task for us to process.
	 * @return Returns the task to execute.
	 * @throws InterruptedException a blocking queue might get interrupted.
	 */
	@SuppressWarnings("unchecked cast,UnusedReturnValue")
	public TaskToRun pullTask() throws InterruptedException
	{
		if ( mTaskQueue == null ) halt();
		if ( mTaskQueue instanceof BlockingQueue<?> ) {
			return ((BlockingQueue<TaskToRun>)mTaskQueue).take();
		}
		else {
			return mTaskQueue.remove();
		}
	}
	
	@Override
	public void runTask()
	{
		if ( !isInterrupted() ) try {
			TaskToRun theTask = pullTask();
			if ( theTask != null ) theTask.execute();
		} catch (InterruptedException ie) {
			Log.i(getName(), "Thread was interrupted.");
		}
	}
	
	/**
	 * Stops this thread after the current task is finished.
	 */
	public void closeTaskQueue() {
		//cannot setDaemon(false) after thread is started
		//do not want to interrupt as that kills current task
		//just set mBreakman to null and loop will end after
		//  current task finishes and before another task is started.
		mBreakman = null;
    }
	
	/**
	 * Add task to the queue. Builder-chain friendly.
	 * @param aTask - the task to add to the queue
	 * @return Returns this object so that a chain-call can be continued.
	 * @throws InterruptedException a blocking queue might get interrupted.
	 */
	public ThreadTaskDaemon queueTask(Runnable aTask, String aName) throws InterruptedException
	{
		pushTask(TaskToRun.prepThisTask(aTask, aName));
		return this;
	}

	/**
	 * Add task that needs to be run on the UI thread to the queue. Builder-chain friendly.
	 * @param aTask - the task to add to the queue
	 * @param aAct - Activity of the UI thread.
	 * @return Returns this object so that a chain-call can be continued.
	 * @throws InterruptedException a blocking queue might get interrupted.
	 */
	public ThreadTaskDaemon queueTaskForUI(Runnable aTask, Activity aAct) throws InterruptedException
	{
		if ( aTask == null ) {
			throw new IllegalArgumentException("Queuing up a NULL task, the shame!");
		}
		pushTask(TaskToRun.prepThisTaskOnUI(aTask, aAct));
		return this;
	}

	/**
	 * Add task that needs to be run on the UI thread to the queue. Builder-chain friendly.
	 * @param aTask - the task to add to the queue
	 * @param aContext - Context needed to determine the UI thread.
	 * @return Returns this object so that a chain-call can be continued.
	 * @throws InterruptedException a blocking queue might get interrupted.
	 */
	public ThreadTaskDaemon queueTaskForUI(Runnable aTask, Context aContext) throws InterruptedException
	{
		if ( aTask == null ) {
			throw new IllegalArgumentException("Queuing up a NULL task, the shame!");
		}
		pushTask(TaskToRun.prepThisTaskOnUI(aTask, aContext));
		return this;
	}
	
	/**
	 * Check if a task is already in the queue by locating its ID.
	 * @param aTaskID - the task ID to check.
	 * @return Returns TRUE if the task ID is found in the queue already.
	 */
	protected boolean isTaskQueued( String aTaskID )
	{ return (findTaskByID(aTaskID)!=null); }

	/**
	 * Queue the task into our task queue by first checking to make sure it isn't already
	 * there.  Ensure other threads aren't adding to our queue by synchronizing the method.
	 * @param aTask - the task to add to the queue
	 * @param aTaskID - the task ID which determined uniqueness.
	 * @param aTaskName - the task name which determines priority sort order, if necessary.
	 * @return Returns this object so that a chain-call can be continued.
	 */
	public synchronized ThreadTaskDaemon queueUniqueTask( Runnable aTask,
			String aTaskID, String aTaskName )
	{
		if ( isTaskQueued(aTaskID) ) {
			Log.w(getName(), "[" + aTaskID + "] already exists in task queue, ignoring.");
			return this;
		}
		try {
			pushTask(TaskToRun.prepThisTask(aTask, aTaskName).setTaskID(aTaskID));
			Log.i(getName(), "[" + aTaskID + "] was added to the task queue.");
		} catch (InterruptedException e) {
			Log.w(getName(), "[" + aTaskID + "] avoided the task queue: interrupted (non-fatal exception).");
		}
		return this;
	}

	/**
	 * Queue the task into our task queue by first checking to make sure it isn't already
	 * there.  Ensure other threads aren't adding to our queue by synchronizing the method.
	 * @param aTask - the task to add to the queue
	 * @param aTaskID - the task ID which determined uniqueness.
	 * @param aTaskName - the task name which determines priority sort order, if necessary.
	 * @param aAct - Activity of the UI thread.
	 * @return Returns this object so that a chain-call can be continued.
	 */
	public synchronized ThreadTaskDaemon queueUniqueTaskOnUI( Runnable aTask,
			String aTaskID, String aTaskName, Activity aAct )
	{
		if ( isTaskQueued(aTaskID) ) {
			Log.w(getName(), "[" + aTaskID + "] already exists in task queue, ignoring.");
			return this;
		}
		try {
			pushTask(TaskToRun.prepThisTaskOnUI(aTask, aAct).setTaskName(aTaskName).setTaskID(aTaskID));
		} catch (InterruptedException e) {
			Log.w(getName(), "[" + aTaskID + "] avoided the task queue: interrupted (non-fatal exception).");
		}
		return this;
	}

	/**
	 * Queue the task into our task queue by first checking to make sure it isn't already
	 * there.  Ensure other threads aren't adding to our queue by synchronizing the method.
	 * @param aTask - the task to add to the queue
	 * @param aTaskID - the task ID which determined uniqueness.
	 * @param aTaskName - the task name which determines priority sort order, if necessary.
	 * @param aContext - Context needed to determine the UI thread.
	 * @return Returns this object so that a chain-call can be continued.
	 */
	public synchronized ThreadTaskDaemon queueUniqueTaskOnUI( Runnable aTask,
			String aTaskID, String aTaskName, Context aContext )
	{
		if ( isTaskQueued(aTaskID) ) {
			Log.w(getName(), "[" + aTaskID + "] already exists in task queue, ignoring.");
			return this;
		}
		try {
			pushTask(TaskToRun.prepThisTaskOnUI(aTask, aContext).setTaskName(aTaskName).setTaskID(aTaskID));
		} catch (InterruptedException e) {
			Log.w(getName(), "[" + aTaskID + "] avoided the task queue: interrupted (non-fatal exception).");
		}
		return this;
	}

}
