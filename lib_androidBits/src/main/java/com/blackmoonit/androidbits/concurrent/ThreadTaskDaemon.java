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
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractQueue;
import java.util.concurrent.BlockingQueue;

/**
 * ThreadTaskDaemon is a Daemon thread that will continue to execute the Runnable tasks in its
 * queue, and keeps doing so as the queue gets filled. If you use a queue that implements
 * BlockingQueue, then an empty queue will block this thread until a task is submitted,
 * waking it up.
 * @param <Q> the type of AbstractQueue used by this daemon
 */
@SuppressWarnings("unused")
public class ThreadTaskDaemon<Q extends AbstractQueue<TaskToRun>> extends ThreadInterruptable
{
	protected String mQueueName;
	protected final Constructor<? extends Q> ctor;
	protected Q mTaskQueue;
	
	/**
	 * Prepare the queue by instantiating it and setting our thread to its name.
	 * @return Returns TRUE if all is well.
	 */
	protected boolean prepNewQueue()
	{
		try {
			mTaskQueue = ctor.newInstance();
			mQueueName = mTaskQueue.getClass().getSimpleName();
			setProcessName(mQueueName);
			return true;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

	public ThreadTaskDaemon(Class<? extends Q> impl) throws NoSuchMethodException
	{
		super();
		ctor = impl.getConstructor();
		if ( prepNewQueue() ) {
			setProcessPriority(null);
			setDaemon(true);
		}
	}

	public TaskToRun findTaskByID(Long aID)
	{
		if ( aID!=null ) {
			for (TaskToRun theTask : mTaskQueue) {
				if ( aID.equals(theTask.getTaskID()) ) {
					return theTask;
				}
			}
		}
		return null;
	}

	public TaskToRun findTaskByName(String aName)
	{
		if ( aName!=null ) {
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
			Log.i(getName(), mQueueName+" was interrupted.");
		}
	}

	/**
	 * Add task to the queue. Builder-chain friendly.
	 * @param aTask - runnable to loop endlessly
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
	 * @param aTask - runnable to loop endlessly
	 * @param aAct - Activity of the UI thread.
	 * @return Returns this object so that a chain-call can be continued.
	 * @throws InterruptedException a blocking queue might get interrupted.
	 */
	public ThreadTaskDaemon queueTaskForUI(Runnable aTask, Activity aAct) throws InterruptedException
	{
		if ( aTask == null )
			throw new IllegalArgumentException("Queuing up a NULL task, the shame!");
		if ( aAct == null )
			throw new IllegalArgumentException("Activity cannot be null");
		pushTask(TaskToRun.prepThisTaskOnUI(aTask, aAct));
		return this;
	}

	/**
	 * Add task that needs to be run on the UI thread to the queue. Builder-chain friendly.
	 * @param aTask - runnable to loop endlessly
	 * @param aContext - Context needed to determine the UI thread.
	 * @return Returns this object so that a chain-call can be continued.
	 * @throws InterruptedException a blocking queue might get interrupted.
	 */
	public ThreadTaskDaemon queueTaskForUI(Runnable aTask, Context aContext) throws InterruptedException
	{
		if ( aTask == null )
			throw new IllegalArgumentException("Queuing up a NULL task, the shame!");
		if ( aContext == null )
			throw new IllegalArgumentException("Context cannot be null");
		pushTask(TaskToRun.prepThisTaskOnUI(aTask, aContext));
		return this;
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

}
