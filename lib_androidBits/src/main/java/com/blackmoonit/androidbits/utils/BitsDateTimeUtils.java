package com.blackmoonit.androidbits.utils;
/*
 * Copyright (C) 2015 Blackmoon Info Tech Services
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

import com.blackmoonit.androidbits.database.DbDateTime;

import java.util.Calendar;
import java.util.TimeZone;

public class BitsDateTimeUtils extends DbDateTime {

	protected BitsDateTimeUtils() {}; //do not instantiate

	/**
	 * Current UTC date and time to the second.
	 * @return Returns the current date and time to the second.
	 */
	static public Calendar getClockNowAsUTC() {
		return getNowAs("UTC", Calendar.SECOND);
	}

	/**
	 * Current UTC date and time to the millisecond.
	 * @return Returns the current date and time to the millisecond.
	 */
	static public Calendar getNowAsUTC() {
		return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Current UTC date without time.
	 * @return Returns the current date, but no time.
	 */
	static public Calendar getTodayAsUTC() {
		return getNowAs("UTC", Calendar.DAY_OF_MONTH);
	}

	/**
	 * Convert calendar object to SQL datetime string format.
	 * @param aCal - the datetime to convert.
	 * @return Returns the string as used by SQL.
	 */
	static public String toISO8601str(Calendar aCal) {
		return toDbStr(aCal);
	}

	/**
	 * Current UTC date and time to the millisecond as ISO8601 formatted string.
	 * @return Returns the string as used by SQL.
	 */
	static public String getNowAsUTCtoISO8601str() {
		return toISO8601str(getNowAsUTC());
	}

	/**
	 * Current UTC date and time to the second as ISO8601 formatted string.
	 * @return Returns the string as used by SQL.
	 */
	static public String getClockNowAsUTCtoISO8601str() {
		return toISO8601str(getClockNowAsUTC());
	}

	/**
	 * Current UTC date to the day as ISO8601 formatted string.
	 * @return Returns the string as used by SQL.
	 */
	static public String getTodaytoISO8601str() {
		return toISO8601str(getToday());
	}
}
