package com.blackmoonit.androidbits.database;
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

import java.util.ArrayList;

public class UpdatingRecordSemaphore extends ArrayList<String> {
	private static final long serialVersionUID = 694980797717784726L;

	/**
	 * Return TRUE if our thread is first to try to create the map with row X, else FALSE.
	 * @param id - the record _ID
	 */
	public synchronized boolean blockOn(String id) {
		if (indexOf(id) > -1) {
			return false;
		}
		else {
			add(id);
			return true;
		}
	}

	/**
	 * Remove an id so others will not be blocked.
	 * @param id - the record _ID
	 */
	public synchronized void endBlock(String id) {
		remove(id);
	}

	/**
	 * Remove an id so others will not be blocked.
	 * Alias for endBlock().
	 * @param id - the record _ID
	 */
	public synchronized void blockOff(String id) {
		endBlock(id);
	}

}
