package com.blackmoonit.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.impl.cookie.DateParseException;

import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

public class DbDateTime {

	/**
	 * Current UTC date and time to the second.
	 * @return Returns the current date and time to the second.
	 */
	static public Calendar getNow() {
		Calendar theCurrentDay = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		theCurrentDay.set(Calendar.MILLISECOND,0);
		return theCurrentDay;
	}
	
	/**
	 * Current UTC date without time.
	 * @return Returns the current date, but no time.
	 */
	static public Calendar getToday() {
		Calendar theCurrentDay = getNow();
		theCurrentDay.set(Calendar.HOUR,0);
		theCurrentDay.set(Calendar.MINUTE,0);
		theCurrentDay.set(Calendar.SECOND,0);
		return theCurrentDay;
	}
	
	/**
	 * Convert calendar object to SQL db string format.
	 * @param aCal - the datetime to convert.
	 * @return Returns the string as used by SQL.
	 */
	static public String toDbStr(Calendar aCal) {
		return (String)DateFormat.format("yyyy-MM-dd hh:mm:ss",aCal);
	}
	
	/**
	 * Conver the SQL date time string into a Calendar object.
	 * @param aStr - SQL date time string.
	 * @return Returns the Calendar representation of the db SQL string.
	 * @throws DateParseException
	 */
	static public Calendar fromDbStr(String aStr) {
		Calendar theCurrentDay = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date theDate = null;
		try {
			theDate = iso8601Format.parse(aStr);
			theCurrentDay.setTime(theDate);
		} catch (ParseException e) {
			//means we do not modify the calendar at all, leaves it as "now"
		}
		return theCurrentDay;
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
