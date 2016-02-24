package com.blackmoonit.androidbits.concurrent;
/*
 * Copyright (C) 2014 Blackmoon Info Tech Services
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

/**
 * Simple thread class designed to improve on Java's implementation to provide an interruptable
 * background worker thread.<br>
 * Android has something similar, called AsyncTask, but this class is
 * designed to use Runnables instead of requiring subclasses to be defined.
 * @author baracudda
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
	 * @see Thread#MIN_PRIORITY MIN_PRIORITY = 1
	 * @see Thread#NORM_PRIORITY NORM_PRIORITY = 5
	 * @see Thread#MAX_PRIORITY MAX_PRIORITY = 10
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

	static public ThreadTask runThisTask(Runnable aTask, String aName) {
		return (ThreadTask)new ThreadTask(aTask, aName, NORM_PRIORITY).execute();
	}

	static public ThreadTask runThisTask(Runnable aTask, String aName, Long aDelayInMilliseconds) {
		return (ThreadTask)new ThreadTask(aTask, aName, NORM_PRIORITY).executeDelayed(aDelayInMilliseconds);
	}

}
