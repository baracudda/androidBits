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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.text.TextUtils;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

/**
 * Static utility methods for database date/time functions.
 */
public class DbDateTime {

	/**
	 * Current UTC date and time to the second.
	 * @return Returns the current date and time to the second.
	 */
	static public Calendar getNowAs(String aTimeZone, int aLeastSignificantTime) {
		Calendar theCurrentDay = Calendar.getInstance(TimeZone.getTimeZone(aTimeZone));
		switch (aLeastSignificantTime) {
			case Calendar.YEAR:
				theCurrentDay.set(Calendar.MONTH,0);
			case Calendar.MONTH:
				theCurrentDay.set(Calendar.DAY_OF_MONTH,0);
			case Calendar.DAY_OF_MONTH:
				theCurrentDay.set(Calendar.HOUR,0);
			case Calendar.HOUR:
				theCurrentDay.set(Calendar.MINUTE,0);
			case Calendar.MINUTE:
				theCurrentDay.set(Calendar.SECOND,0);
			case Calendar.SECOND:
				theCurrentDay.set(Calendar.MILLISECOND,0);
			default:
		}//switch
		return theCurrentDay;
	}

	/**
	 * Current UTC date and time to the second.
	 * @return Returns the current date and time to the second.
	 */
	static public Calendar getNow() {
		return getNowAs("UTC", Calendar.SECOND);
	}

	/**
	 * Current UTC date and time to the millisecond.
	 * @return Returns the current date and time to the millisecond.
	 */
	static public Calendar getNowMs() {
		return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Current UTC date without time.
	 * @return Returns the current date, but no time.
	 */
	static public Calendar getToday() {
		return getNowAs("UTC", Calendar.DAY_OF_MONTH);
	}

	/**
	 * Convert calendar object to SQL db string format.
	 * @param aCal - the datetime to convert.
	 * @return Returns the string as used by SQL.
	 */
	static public String toDbStr(Calendar aCal) {
		/* DateFormat did not support H at all < API18 and additionally k was intentionally broken
		String theResult = (String)DateFormat.format("yyyy-MM-dd'T'HH:mm:ss", aCal);
		int theMs = aCal.get(Calendar.MILLISECOND);
		if (theMs>0) {
			theResult = theResult + "."+String.format(Locale.US,"%03d",theMs)+"000";
		}
		return theResult+"Z";
		*/
		SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.US);
		iso8601Format.setCalendar(aCal); //O.o; needed, idk why, but w/e.
		String theResult = iso8601Format.format(aCal.getTime());
		int theMs = aCal.get(Calendar.MILLISECOND);
		if (theMs>0) {
			theResult = theResult + "."+String.format(Locale.US,"%03d",theMs)+"000";
		}
		return theResult+"Z";
	}

	/**
	 * Convert the SQL date time string into a Calendar object.
	 * @param aStr - SQL date time string.
	 * @return Returns the Calendar representation of the db SQL string.
	 * @throws ParseException
	 */
	static public Calendar fromISO8601str(String aStr) {
		if (!TextUtils.isEmpty(aStr)) {
			SimpleDateFormat iso8601Format;
			int theLeastSignificantTime = Calendar.MILLISECOND;
			if (aStr.contains(".")) {
                iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            } else if (aStr.length() > 10) {
                iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                theLeastSignificantTime = Calendar.SECOND;
			} else {
				iso8601Format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
				theLeastSignificantTime = Calendar.DAY_OF_MONTH;
			}
			try {
				//Java doesn't parse ISO dates correctly. We need to convert "Z" into +0000
				String theStr = aStr.replaceAll("Z$", "+0000");
                //Also, something is wrong with Ts in the dates (if they are there?), so that needs to be turned into a space 
                theStr = theStr.replaceAll("T", " ");
				//additionally, we do not have microsecond resolution, so keep .### and remove .###xxx
				theStr = theStr.replaceAll("(\\.\\d\\d\\d)\\d\\d\\d", "$1");
				Calendar theCal = getNowAs("UTC", theLeastSignificantTime);
				theCal.setTime(iso8601Format.parse(theStr));
				return theCal;
			} catch (ParseException e) {
				Log.e("androidBits.fromDbStr", "Failed to convert " + aStr + " into a UTC datetime.", e);
				return null;
			}
		}
		return null;
	}

	/**
	 * Convert the SQL date time string into a Calendar object.
	 * @param aStr - SQL date time string.
	 * @return Returns the Calendar representation of the db SQL string.
	 * @throws ParseException
	 */
	static public Calendar fromDbStr(String aStr) {
		Calendar theCal = fromISO8601str(aStr);
		if (theCal==null) {
			theCal = getNowMs();
		}
		return theCal;
	}

	/**
	 * Returns the current UTC time in the proper SQL Date format
	 * @return Returns the String representation of the current datetime
	 * @param bNowMs - boolean for getNow with Milliseconds or seconds
	 */
	static public String getNowAsDbStr(boolean bNowMs) {
		if (bNowMs) {
			return toDbStr(getNowMs());
		} else {
			return toDbStr(getNow());
		}
	}

	/**
	 * Adds aNumDays to the timestamp. If timestamp is null, {@link #getToday()} is used.
	 * @param aTimestamp - timestamp, getToday() if null is passed in.
	 * @param aNumDays - number of days to add.
	 * @return Returns calendar object with aNumDays added to it.
	 */
	static public Calendar addDays(Calendar aTimestamp, int aNumDays) {
		if (aTimestamp==null)
			aTimestamp = getToday();
		aTimestamp.add(Calendar.DAY_OF_MONTH,aNumDays);
		return aTimestamp;
	}

	/**
	 * Convenience function to get the date out of a picker as a Calendar.
	 * @param aDatePicker - Widget to read.
	 * @return Returns the Calendar date the picker represents.
	 */
	static public Calendar getDateFromDatePicker(DatePicker aDatePicker) {
		Calendar theDate = getToday();
		if (aDatePicker!=null)
			theDate.set(aDatePicker.getYear(),aDatePicker.getMonth(),aDatePicker.getDayOfMonth());
		return theDate;
	}

	/**
	 * Convenience function to set the picker date.
	 * @param aCal - Date to set, if null, use getNow().
	 * @param aDatePicker - Widget to set.
	 * @param aListener - part of the Widget's params
	 * @return Returns the Calendar object used.
	 */
	static public Calendar setDateToDatePicker(Calendar aCal, DatePicker aDatePicker, OnDateChangedListener aListener) {
		if (aCal==null)
			aCal = getNow();
		if (aDatePicker!=null)
			aDatePicker.init(aCal.get(Calendar.YEAR),aCal.get(Calendar.MONTH),aCal.get(Calendar.DAY_OF_MONTH),aListener);
		return aCal;
	}

}
