package com.blackmoonit.androidbits.database;

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
