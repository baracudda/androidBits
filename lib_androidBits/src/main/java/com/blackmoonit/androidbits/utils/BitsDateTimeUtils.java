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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Static utility methods for date/time functions.
 */
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

	/**
	 * Format an ISO8601 datetime string into a local time with another format.
	 * @param aISO8601str - the datetime string.
	 * @param aFormat - the format to convert the datetime string into showing.
	 * @return Returns the datetime as the new aFormatted string.
	 */
	static public CharSequence fromUTCtoLocalTimeStr(String aISO8601str, String aFormat) {
		Calendar theTs = fromISO8601str(aISO8601str);
		if (theTs!=null) {
			theTs.setTimeZone(TimeZone.getDefault());
			SimpleDateFormat iso8601Format = new SimpleDateFormat(aFormat, Locale.US);
			iso8601Format.setCalendar(theTs); //O.o; needed, idk why, but w/e.
			String theResult = iso8601Format.format(theTs.getTime());
			/* milliseconds may have to use special handling if we want to show it
			int theMs = theTs.get(Calendar.MILLISECOND);
			if (theMs>0) {
				theResult = theResult + "."+String.format(Locale.US,"%03d",theMs)+"000";
			}
			*/
			return theResult;
		}
		return "";
	}


}
