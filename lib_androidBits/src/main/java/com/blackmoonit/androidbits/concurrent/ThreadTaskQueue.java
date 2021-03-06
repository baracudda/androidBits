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

import java.util.concurrent.PriorityBlockingQueue;

/**
 * ThreadTaskQueue is a Daemon thread that will continue to execute the Runnable tasks in its
 * queue, in FIFO order, and keep doing so as they come in.  An empty queue blocks this thread
 * until a task is submitted, waking it up.
 * @deprecated use ThreadTaskDaemon instead.
 * @author baracudda
 */
public class ThreadTaskQueue extends ThreadTaskDaemon
{

	public ThreadTaskQueue() {
		super();
	}
	
	//left for backward compatibility

}
