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

import com.blackmoonit.androidbits.app.UITaskRunner;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread worker thread class that might run in the background or in the main thread.<br>
 * This class is designed to use Runnables instead of requiring subclasses to be defined.
 * @author baracudda
 */
@SuppressWarnings("unused, UnusedReturnValue")
public class TaskToRun extends ThreadTask implements Comparable<TaskToRun>
{
	static protected final AtomicLong autoIDgen = new AtomicLong(1);
	protected boolean bRunOnUi = false;
	protected WeakReference<Activity> wrAct = null;
	protected WeakReference<Context> wrContext = null;
	/** Task ID defaults to an auto-generated INT, but can be overridden as an UUID. */
	protected String mTaskID = String.valueOf(autoIDgen.getAndAdd(1));
	protected String mTaskName = null;
	
	public TaskToRun(Runnable aTask)
	{ super(aTask); }
	
	@Override
	public TaskToRun setProcessName(String aName)
	{
		this.mTaskName = aName;
		return (TaskToRun)super.setProcessName(aName);
	}
	
	/**
	 * Execute this task on the UI thread using the parameter as context.
	 * @param aAct - the Activity to use as a context for executing the task on the UI thread.
	 * @return Returns this object so that a chain-call can be continued.
	 * @throws IllegalArgumentException if the param is null.
	 */
	public TaskToRun setTaskToRunOnUI( Activity aAct )
	{
		if ( aAct == null ) {
			throw new IllegalArgumentException("Activity is required to queue UI tasks.");
		}
		wrAct = new WeakReference<Activity>(aAct);
		bRunOnUi = true;
		return this;
	}
	
	/**
	 * Execute this task on the UI thread using the parameter as context.
	 * @param aContext - the context to use for executing the task on the UI thread.
	 * @return Returns this object so that a chain-call can be continued.
	 * @throws IllegalArgumentException if the param is null.
	 */
	public TaskToRun setTaskToRunOnUI( Context aContext )
	{
		if ( aContext == null ) {
			throw new IllegalArgumentException("Context is required to queue UI tasks.");
		}
		wrContext = new WeakReference<Context>(aContext);
		bRunOnUi = true;
		return this;
	}
	
	/**
	 * Get he task ID used to determine uniqueness in the queue.
	 * @return Returns the string used to determine uniqueness.
	 */
	public String getTaskID()
	{ return mTaskID; }
	
	/**
	 * Set the task ID used to determine uniqueness in the queue.
	 * @return Returns this object so that a chain-call can be continued.
	 */
	public TaskToRun setTaskID( String aID )
	{ mTaskID = aID; return this; }
	
	/**
	 * Get the task name used to determine queue sort order, if necessary.
	 * @return Returns the string used to determine possible queue sort order.
	 */
	public String getTaskName()
	{ return mTaskName; }
	
	/**
	 * Set the task name used to determine queue sort order, if necessary.
	 * @param aName - the name to use for sort ordering inside the queue, if needed.
	 * @return Returns this object so that a chain-call can be continued.
	 */
	public TaskToRun setTaskName( String aName )
	{ return setProcessName(aName); }
	
	@Override
	public void runTask()
	{
		if ( mTask == null ) return;
		//we have a task to run, see if it needs to run on UI thread
		if ( bRunOnUi ) {
			Activity theAct = (wrAct!=null) ? wrAct.get() : null;
			if ( theAct!=null ) {
				theAct.runOnUiThread(mTask);
			}
			else {
				Context theContext = (wrContext!=null) ? wrContext.get() : null;
				if (theContext!=null) {
					UITaskRunner.runOnUiThread(theContext, mTask);
				}
			}
		}
	}
	
	@Override
	public int compareTo(TaskToRun o) {
		if ( mTaskName != null )
		{ return mTaskName.compareTo(o.getTaskName()); }
		else if ( mTaskID != null )
		{ return mTaskID.compareTo(o.getTaskID()); }
		else
		{ return 0; }
	}
	
	static public TaskToRun prepThisTask(Runnable aTask, String aName)
	{ return (new TaskToRun(aTask)).setProcessName(aName); }
	
	static public TaskToRun prepThisTaskOnUI(Runnable aTask, Activity aAct)
	{ return (new TaskToRun(aTask)).setTaskToRunOnUI(aAct); }
	
	static public TaskToRun prepThisTaskOnUI(Runnable aTask, Context aContext)
	{ return (new TaskToRun(aTask)).setTaskToRunOnUI(aContext); }
	
	/**
	 * Immediately start executing this task.
	 * @param aTask - the task to run.
	 * @return Returns the task wrapper created.
	 */
	static public TaskToRun runThisTask(Runnable aTask)
	{ return (TaskToRun) (new TaskToRun(aTask)).execute(); }
	
	/**
	 * Immediately start executing this task.
	 * @param aTask - the task to run.
	 * @param aName - the name of the task thread to use.
	 * @return Returns the task wrapper created.
	 */
	static public TaskToRun runThisTask(Runnable aTask, String aName)
	{ return (TaskToRun)prepThisTask(aTask, aName).execute(); }
	
	/**
	 * Immediately start executing this task after a set delay.
	 * @param aTask - the task to run.
	 * @param aDelayInMilliseconds - the ms delay before execution starts.
	 * @return Returns the task wrapper created.
	 */
	static public TaskToRun runThisTask(Runnable aTask, Long aDelayInMilliseconds)
	{ return (TaskToRun) (new TaskToRun(aTask)).executeDelayed(aDelayInMilliseconds); }

	/**
	 * Immediately start executing this task after a set delay.
	 * @param aTask - the task to run.
	 * @param aName - the name of the task thread to use.
	 * @param aDelayInMilliseconds - the ms delay before execution starts.
	 * @return Returns the task wrapper created.
	 */
	static public TaskToRun runThisTask(Runnable aTask, String aName, Long aDelayInMilliseconds)
	{ return (TaskToRun)prepThisTask(aTask, aName).executeDelayed(aDelayInMilliseconds); }
	
}
