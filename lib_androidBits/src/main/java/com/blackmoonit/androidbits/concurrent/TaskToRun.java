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

/**
 * A thread worker thread class that might run in the background or in the main thread.<br>
 * This class is designed to use Runnables instead of requiring subclasses to be defined.
 * @author baracudda
 */
public class TaskToRun extends ThreadTask
{
	protected boolean bRunOnUi = false;
	protected WeakReference<Activity> wrAct = null;
	protected WeakReference<Context> wrContext = null;
	protected Long mTaskID = null;
	protected String mTaskName = null;
	
	public TaskToRun(Runnable aTask)
	{ super(aTask); }
	
	@Override
	public TaskToRun setProcessName(String aName)
	{
		this.mTaskName = aName;
		return (TaskToRun)super.setProcessName(aName);
	}
	
	public Long getTaskID()
	{ return mTaskID; }
	
	public TaskToRun setTaskID( Long aID )
	{ mTaskID = aID; return this; }
	
	public String getTaskName()
	{ return mTaskName; }
	
	public TaskToRun setTaskName( String aName )
	{ return setProcessName(aName); }
	
	@Override
	public void runTask() throws InterruptedException
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
	
	static public TaskToRun prepThisTask(Runnable aTask, String aName)
	{
		TaskToRun theResult = new TaskToRun(aTask);
		theResult.setProcessName(aName).setProcessPriority(NORM_PRIORITY);
		return theResult;
	}
	
	static public TaskToRun prepThisTaskOnUI(Runnable aTask, Activity aAct)
	{
		TaskToRun theResult = new TaskToRun(aTask);
		theResult.bRunOnUi = true;
		theResult.wrAct = new WeakReference<Activity>(aAct);
		return theResult;
	}
	
	static public TaskToRun prepThisTaskOnUI(Runnable aTask, Context aContext)
	{
		TaskToRun theResult = new TaskToRun(aTask);
		theResult.bRunOnUi = true;
		theResult.wrContext = new WeakReference<Context>(aContext);
		return theResult;
	}
	
	static public TaskToRun runThisTask(Runnable aTask, String aName)
	{ return (TaskToRun)prepThisTask(aTask, aName).execute(); }

	static public TaskToRun runThisTask(Runnable aTask, String aName, Long aDelayInMilliseconds)
	{ return (TaskToRun)prepThisTask(aTask, aName).executeDelayed(aDelayInMilliseconds); }

}
