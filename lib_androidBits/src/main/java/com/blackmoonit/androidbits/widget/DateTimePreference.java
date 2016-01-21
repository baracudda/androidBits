package com.blackmoonit.androidbits.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateTimePreference extends DialogPreference implements DatePicker.OnDateChangedListener, TimePicker.OnTimeChangedListener
{
	private final String LOG_TAG = this.getClass().getSimpleName();
	private final SimpleDateFormat m_oSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private final SimpleDateFormat m_oVerySimpleDateFormat = new SimpleDateFormat("LLL d, yyyy h:mm:ss a");

	protected String mDefaultValue; //set in onGetDefaultValue
	protected List<String> mPrefEntries = null;
	protected List<String> mPrefValues = null;
	protected int R_array_pref_entries = 0;
	protected int R_array_pref_entryvalues = 0;

	private int m_nSelectedMinute;
	private int m_nSelectedHour;
	private int m_nSelectedDay;
	private int m_nSelectedMonth;
	private int m_nSelectedYear;

	private long persistedDateTime;
	private boolean noDateTimeSet = true;

	protected int m_nDatePickerResource;
	protected int m_nTimePickerResource;

	protected DatePicker m_oDatePicker;
	protected TimePicker m_oTimePicker;

	public DateTimePreference(Context aContext, AttributeSet attrs) {
		super(aContext, attrs);
		setup(aContext, attrs);
	}

	public DateTimePreference(Context aContext, AttributeSet attrs, int defStyle) {
		super(aContext, attrs, defStyle);
		setup(aContext, attrs);
	}

	protected void setup(Context aContext, AttributeSet attrs) {
		int[] attrsWanted = getAttrsWanted();
		final TypedArray theAttrs = aContext.obtainStyledAttributes(attrs, attrsWanted);
		processWantedAttributes(theAttrs);
		theAttrs.recycle();
		getResIds(aContext.getResources(), aContext.getPackageName());

	}

	private void savePreferences(final boolean shouldSave) {
		if (shouldSave) {
			persistString(String.valueOf(getSelectedTimeAsLong()));
		} else {
			persistString("0");
			noDateTimeSet = true;
		}
	}

	protected int[] getAttrsWanted() {
		return new int[] {
				android.R.attr.entries,
				android.R.attr.entryValues,
				android.R.attr.defaultValue,
				android.R.attr.positiveButtonText,
				android.R.attr.negativeButtonText,
				android.R.attr.max,
				android.R.attr.dialogLayout
			};
	}

	protected int processWantedAttributes(final TypedArray aAttrs)
    {
        int i = 0;
		R_array_pref_entries = aAttrs.getResourceId(i++, R_array_pref_entries);
		R_array_pref_entryvalues = aAttrs.getResourceId(i++, R_array_pref_entryvalues);
		if (mDefaultValue==null && aAttrs.peekValue(i)!=null)
			mDefaultValue = aAttrs.getString(i++);
		else
			i += 1;

		setPositiveButtonText(aAttrs.getResourceId(i++, android.R.string.ok));
		setNegativeButtonText("Unset");

		setDialogLayoutResource( aAttrs.getResourceId( i++,
            getResId( "layout", "dialog_pref_datetime" ) ) );
        /*
         * This method formerly tried to get things like the "dialogMessage" and
         * "summary" but apparently those are intercepted and unavailable. Even
         * the other attributes above, such as default value, are suspicious;
         * "defaultValue" was also found to be unobtained.
         */
		return i;
	}

	protected int getResId(String aResType, String aResName) {
		return getContext().getResources().getIdentifier(aResName,aResType,getContext().getPackageName());
	}

	protected void getResIds(Resources aResources, String aPackageName) {
		m_nDatePickerResource = aResources.getIdentifier("dialog_pref_datetime_datepicker","id",aPackageName);
		m_nTimePickerResource = aResources.getIdentifier("dialog_pref_datetime_timepicker","id",aPackageName);

		if (R_array_pref_entries!=0 && R_array_pref_entryvalues!=0) {
			mPrefEntries = Arrays.asList(aResources.getStringArray(R_array_pref_entries));
			mPrefValues = Arrays.asList(aResources.getStringArray(R_array_pref_entryvalues));
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray attrs, int aIndex) {
		TypedValue tv = attrs.peekValue(aIndex);
		if (tv!=null)
			mDefaultValue = tv.coerceToString().toString();
		else
			mDefaultValue = null;
		return mDefaultValue;
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		persistedDateTime = -1;
		if (restorePersistedValue) {
			String persistedDateTimeString = getPersistedString("");
			try {
				persistedDateTime = Long.parseLong(persistedDateTimeString);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		if (!restorePersistedValue || persistedDateTime <= 0) {
			noDateTimeSet = true;
			persistedDateTime = getCurrentDateTimeAsLong();
		} else {
			noDateTimeSet = false;
		}

		setSelectedDateTime(persistedDateTime, true);
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		m_oDatePicker = (DatePicker) v.findViewById(m_nDatePickerResource);
		m_oTimePicker = (TimePicker) v.findViewById(m_nTimePickerResource);
		m_oDatePicker.init(m_nSelectedYear, m_nSelectedMonth, m_nSelectedDay, this);
		m_oTimePicker.setOnTimeChangedListener(this);
		m_oTimePicker.setCurrentHour(m_nSelectedHour);
		m_oTimePicker.setCurrentMinute(m_nSelectedMinute);
		if (Build.VERSION.SDK_INT >= 11)
			m_oDatePicker.setMinDate(getCurrentDateTime().getTimeInMillis());
	}

	protected String getEntry() {
		if (noDateTimeSet) {
			return "Not Set";
		} else {
			long theValue = getSavedValue(persistedDateTime);
			Calendar selectedCalendar = Calendar.getInstance();
			selectedCalendar.setTimeInMillis(theValue);
			return "Set to disable on " + m_oVerySimpleDateFormat.format(selectedCalendar.getTime());
		}
	}

	private Calendar getCurrentDateTime() {
		return Calendar.getInstance();
	}

	private long getCurrentDateTimeAsLong() {
		return getCurrentDateTime().getTimeInMillis();
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		setSummary(getEntry());
		return super.onCreateView(parent);
	}

	@Override
	protected boolean persistString(String aNewValue) {
		if (super.persistString(aNewValue)) {
			setSummary(getEntry());
			notifyChanged();
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		savePreferences(positiveResult);
		super.onDialogClosed(positiveResult);
	}

	/**
	 * Called upon a date change.
	 *
	 * @param view        The view associated with this listener.
	 * @param year        The year that was set.
	 * @param monthOfYear The month that was set (0-11) for compatibility
	 *                    with {@link Calendar}.
	 * @param dayOfMonth  The day of the month that was set.
	 */
	@Override
	public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		m_nSelectedDay 		= dayOfMonth;
		m_nSelectedMonth 	= monthOfYear;
		m_nSelectedYear 	= year;
	}

	/**
	 * @param view      The view associated with this listener.
	 * @param hourOfDay The current hour.
	 * @param minute    The current minute.
	 */
	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

		final int currentHour = getCurrentDateTime().get(Calendar.HOUR_OF_DAY);
		final int currentMinute = getCurrentDateTime().get(Calendar.MINUTE);

		// Prevent user from setting a time before current time.
		if (hourOfDay < currentHour) {
			hourOfDay = currentHour;
			view.setCurrentHour(currentHour);

			if (minute < currentMinute) {
				minute = currentMinute;
				view.setCurrentMinute(currentMinute);
			}
		}

		m_nSelectedHour = hourOfDay;
		m_nSelectedMinute = minute;
	}

	/**
	 * Given a slider value, return the value that will be persisted.
	 *
	 * @param aSliderValue - the slider value
	 * @return Returns the slider value or the entryValues[slidervalue] if defined.
	 */
	public Object getValueToSave(int aSliderValue) {
		if (mPrefValues!=null) {
			if (aSliderValue>=0 && aSliderValue<mPrefValues.size()) {
				return mPrefValues.get(aSliderValue);
			} else {
				throw new IndexOutOfBoundsException("Slider Value outside entryValues index range.");
			}
		} else {
			return (Integer)aSliderValue;
		}
	}

	/**
	 * Retrieve the saved value and convert it to a raw slider value if entryValues are defined.
	 *
	 * @param aDefaultValue - default slider value
	 * @return Returns saved slider value. If entryValues are defined, the index of the
	 * entryValue is returned. If there is no value or any kind of exception, aDefaultValue is returned.
	 */
	public long getSavedValue(long aDefaultValue) {
		if (shouldPersist()) {
			if (mPrefValues!=null) {
				try {
					return mPrefValues.indexOf(getPersistedString(Long.toString(aDefaultValue)));
				} catch (Exception e) {
					return aDefaultValue;
				}
			} else {
				return Long.valueOf(getPersistedString(Long.toString(aDefaultValue)));
			}
		} else {
			return aDefaultValue;
		}
	}

	public void setUIWithDate(final int aDay, final int aMonth, final int aYear) {
		if (m_oDatePicker != null) {
			m_oDatePicker.updateDate(aYear, aMonth, aDay);
		}
	}

	public void setUIWithTime(final int aHour, final int aMinute) {
		if (m_oTimePicker != null) {
			m_oTimePicker.setCurrentMinute(aMinute);
			m_oTimePicker.setCurrentHour(aHour);
		}
	}

	public int getSelectedMinute() { return m_nSelectedMinute; }

	public int getSelectedHour() { return m_nSelectedHour; }

	public int getSelectedDay() { return m_nSelectedDay; }

	public int getSelectedMonth() { return m_nSelectedMonth; }

	public int getSelectedYear() { return m_nSelectedYear; }

	public Calendar getSelectedCalendar() {
		Calendar selectedCalendar = Calendar.getInstance();
		selectedCalendar.set(m_nSelectedYear, m_nSelectedMonth, m_nSelectedDay, m_nSelectedHour, m_nSelectedMinute);

		return selectedCalendar;
	}

	public Date getSelectedDateTime() {
		return getSelectedCalendar().getTime();
	}

	public String getSelectedDateTimeAsString() {
		return m_oSimpleDateFormat.format(getSelectedDateTime());
	}

	public long getSelectedTimeAsLong() {
		return getSelectedCalendar().getTimeInMillis();
	}

	public void setSelectedDateTime(final long selectedDateTime, final boolean updateUI) {
		Calendar selectedCalendar = Calendar.getInstance();
		selectedCalendar.setTimeInMillis(selectedDateTime);
		setSelectedDateTime(selectedCalendar, false);

		if (updateUI) {
			setUIWithDate(m_nSelectedDay, m_nSelectedMonth, m_nSelectedYear);
			setUIWithTime(m_nSelectedHour, m_nSelectedMinute);
		}
	}

	public void setSelectedDateTime(final String selectedString, final boolean updateUI) {
		try {
			Date dateTime = m_oSimpleDateFormat.parse(selectedString);
			setSelectedDateTime(dateTime, false);
		} catch (ParseException e) {
			Log.i(LOG_TAG, e.getMessage());
			e.printStackTrace();
		}

		if (updateUI) {
			setUIWithDate(m_nSelectedDay, m_nSelectedMonth, m_nSelectedYear);
			setUIWithTime(m_nSelectedHour, m_nSelectedMinute);
		}
	}

	public void setSelectedDateTime(final Date selectedDateTime, final boolean updateUI) {
		Calendar selectedCalendar = Calendar.getInstance();
		selectedCalendar.setTime(selectedDateTime);
		setSelectedDateTime(selectedCalendar, false);

		if (updateUI) {
			setUIWithDate(m_nSelectedDay, m_nSelectedMonth, m_nSelectedYear);
			setUIWithTime(m_nSelectedHour, m_nSelectedMinute);
		}
	}

	public void setSelectedDateTime(final Calendar selectedDateTime, final boolean updateUI) {
		m_nSelectedMinute = selectedDateTime.get(Calendar.MINUTE);
		m_nSelectedHour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
		m_nSelectedDay = selectedDateTime.get(Calendar.DAY_OF_MONTH);
		m_nSelectedMonth = selectedDateTime.get(Calendar.MONTH);
		m_nSelectedYear = selectedDateTime.get(Calendar.YEAR);

		if (updateUI) {
			setUIWithDate(m_nSelectedDay, m_nSelectedMonth, m_nSelectedYear);
			setUIWithTime(m_nSelectedHour, m_nSelectedMinute);
		}
	}
}
