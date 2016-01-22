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

import com.blackmoonit.androidbits.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Allows a user to set a preference where the stored value is a string
 * representation of a long integer, which is a <b>UTC timestamp in
 * milliseconds</b>, but displayed as a string. When not specified, the default
 * value of the preference is the constant value {@link #TIMESTAMP_NOT_SET}.
 *
 * The dialog supports the custom attribute
 * {@code dateTimePreferenceDisplayFormat}, which must reference a string
 * resource containing a {@link SimpleDateFormat} specification. This allows the
 * consuming app to render custom and/or localized text on the preference
 * activity when a value is set. If no value is specified for this attribute,
 * then the format specified by the constant {@link #READABLE_DATE_FORMAT} will
 * be used.
 */
public class DateTimePreference
    extends DialogPreference
    implements DatePicker.OnDateChangedListener, TimePicker.OnTimeChangedListener
{
	protected static final String LOG_TAG =
        DateTimePreference.class.getSimpleName() ;

    /**
     * Canonical date format. Mirrors the format used by SQLite.
     */
	public static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") ;

    /**
     * A more readable date format suitable for UI.
     */
	public static final SimpleDateFormat READABLE_DATE_FORMAT =
        new SimpleDateFormat("LLL d, yyyy 'at' h:mm a");

    /**
     * The default value to be stored in the preference, in absence of an input
     * from the user. This is initialized in {@link #onGetDefaultValue}.
     */
	protected String mDefaultValue = null ; //set in onGetDefaultValue

	private int mSelectedMinute ;
	private int mSelectedHour ;
	private int mSelectedDay ;
	private int mSelectedMonth ;
	private int mSelectedYear ;

    /** A default value indicating that the preference has not been set. */
    public static final long TIMESTAMP_NOT_SET = -1L ;
    /** The current value of the date/time preference as a UTC timestamp. */
	private long mCurrentTimestamp = TIMESTAMP_NOT_SET ;

    /** The resource ID of the date picker. */
	protected int mDatePickerResID ;
    /** The resource ID of the time picker. */
	protected int mTimePickerResID ;
    /** A persistent reference to the date picker. */
	protected DatePicker mDatePicker ;
    /** A persistent reference to the time picker. */
	protected TimePicker mTimePicker ;

    /** The date/time format to use when displaying values onscreen. */
    protected SimpleDateFormat mDateTimeDisplayFormat = READABLE_DATE_FORMAT ;

	public DateTimePreference(Context aContext, AttributeSet aAttrs) {
		super(aContext, aAttrs);
		setup(aContext, aAttrs);
	}

	public DateTimePreference(Context aContext, AttributeSet aAttrs, int defStyle) {
		super(aContext, aAttrs, defStyle);
		setup(aContext, aAttrs);
	}

    /**
     * Initializes the dialog based on the specified attributes from the XML.
     * @param aContext the dialog's context
     * @param aAttrs a set of attributes defined in the XML
     * @return the dialog preference, for chained invocations
     */
	protected DateTimePreference setup( Context aContext, AttributeSet aAttrs )
    {
		final TypedArray theAttrs =
            aContext.obtainStyledAttributes( aAttrs, CUSTOM_ATTRS ) ;
		this.processAttributes( theAttrs ) ;
		theAttrs.recycle() ;
		this.getResIds( aContext.getResources(), aContext.getPackageName() ) ;
        return this ;
	}

    /**
     * An array of attributes to be requested by {@link #setup} and processed by
     * {@link #processAttributes}.
     */
    protected static final int[] CUSTOM_ATTRS =
        {
            android.R.attr.dialogLayout,
            android.R.attr.defaultValue,
            android.R.attr.positiveButtonText,
            android.R.attr.negativeButtonText,
            R.attr.dateTimePreferenceDisplayFormat,
        };

    /**
     * Processes each of the attributes defined in {@link #CUSTOM_ATTRS}.
     * @param aAttrs a set of attributes to process
     * @return the dialog preference, for chained invocations
     */
	protected DateTimePreference processAttributes( final TypedArray aAttrs )
    {
        // These indices MUST correspond tightly to the indices of items in the
        // static CUSTOM_ATTRS array.
        final int DIALOG_LAYOUT_IDX = 0 ;
        final int DEFAULT_VALUE_IDX = 1 ;
        final int POSITIVE_TEXT_IDX = 2 ;
        final int NEGATIVE_TEXT_IDX = 3 ;
        final int CUSTOM_DATETIME_FORMAT_IDX = 4 ;

        this.setDialogLayoutResource( aAttrs.getResourceId( DIALOG_LAYOUT_IDX,
            R.layout.dialog_pref_datetime ) ) ;

        if( aAttrs.peekValue(DEFAULT_VALUE_IDX) != null )
        {
            final String theDefault = aAttrs.getString( DEFAULT_VALUE_IDX ) ;
            final long theTimestamp = this.parseTimestamp( theDefault ) ;
            mDefaultValue = aAttrs.getString( DEFAULT_VALUE_IDX ) ;
            mCurrentTimestamp = this.parseTimestamp( mDefaultValue ) ;
        }
        else
        {
            mDefaultValue = this.getContext().getString(
                R.string.datetime_pref_value_not_set ) ;
            mCurrentTimestamp = TIMESTAMP_NOT_SET ;
        }

        this.setPositiveButtonText( aAttrs.getResourceId( POSITIVE_TEXT_IDX,
            android.R.string.ok ) ) ;

        this.setNegativeButtonText( aAttrs.getResourceId( NEGATIVE_TEXT_IDX,
            R.string.datetime_pref_dialog_negative_button) ) ;

        if( aAttrs.peekValue( CUSTOM_DATETIME_FORMAT_IDX ) != null )
        {
            final String theDateFormat =
                aAttrs.getString( CUSTOM_DATETIME_FORMAT_IDX ) ;
            try
            { mDateTimeDisplayFormat = new SimpleDateFormat(theDateFormat) ; }
            catch( NullPointerException npx )
            {
                Log.w( LOG_TAG, "Date format null detection failed.", npx ) ;
                mDateTimeDisplayFormat = READABLE_DATE_FORMAT ;
            }
            catch( IllegalArgumentException iax )
            {
                Log.w( LOG_TAG, (new StringBuffer())
                        .append( "Invalid date format specification [" )
                        .append( theDateFormat )
                        .append( "]; using default instead." )
                        .toString()
                    , iax
                    ) ;
            }
        }
        else
            mDateTimeDisplayFormat = READABLE_DATE_FORMAT ;

		return this ;
	}

    /**
     * Discovers the resource ID of a given resource name.
     * @param aResType the type of the resource (string, layout, etc)
     * @param aResName the name of the resource
     * @return the resource ID
     */
	protected int getResId(String aResType, String aResName)
    {
		return this.getContext().getResources().getIdentifier(
            aResName, aResType, this.getContext().getPackageName() ) ;
	}

    /**
     * Discovers the action resource ID of various entities within the dialog.
     * @param aResources a set of resources for the appropriate context
     * @param aPackageName the package name to use as a context
     * @return the dialog preference, for chained invocations
     */
	protected DateTimePreference getResIds( Resources aResources,
                                            String aPackageName )
    {
		mDatePickerResID = aResources.getIdentifier(
            "dialog_pref_datetime_datepicker", "id", aPackageName ) ;
		mTimePickerResID = aResources.getIdentifier(
            "dialog_pref_datetime_timepicker", "id", aPackageName ) ;
        return this ;
	}

    /**
     * Since the preference is actually a long integer (timestamp) but must be
     * stored as a string, this method will "coerce" the persistent value into a
     * string. This override also narrows the scope of the return value from
     * {@code Object} to {@code String} for easier processing.
     * @param aAttrs a set of attributes which will contain
     *  {@link android.R.attr#defaultValue}
     * @param aIndex the index of the default value item inside the array
     * @return the default value for this preference
     */
	@Override
	protected String onGetDefaultValue( TypedArray aAttrs, int aIndex )
    {
		TypedValue tv = aAttrs.peekValue(aIndex) ;
		if (tv!=null)
			mDefaultValue = tv.coerceToString().toString() ;
		else
			mDefaultValue = null;
		return mDefaultValue ;
	}

    /**
     * Loads the initial timestamp value from the preference, and initializes
     * the date and time pickers to indicate the same value. If no value is set
     * in the preference, then the current time will be displayed.
     * @param bRestoreValue specifies whether to load a value from the
     *  preference ({@code true}), or force the default value ({@code false}).
     * @param aDefault the default value to be used if {@code bRestoreValue} is
     *  {@code false}
     */
	@Override
	protected void onSetInitialValue( boolean bRestoreValue, Object aDefault )
    {
		mCurrentTimestamp = TIMESTAMP_NOT_SET ;
		if( bRestoreValue )
        {
            mCurrentTimestamp =
                this.parseTimestamp( this.getPersistedString(mDefaultValue) ) ;
		}

		if( !bRestoreValue || mCurrentTimestamp == TIMESTAMP_NOT_SET )
            this.setSelectedDateTime( this.getCurrentDateTimeAsLong(), true ) ;
        else
		    this.setSelectedDateTime( mCurrentTimestamp, true ) ;
	}

    /**
     * Tries to parse a long timestamp value from the given string.
     * @param aString a string that (we hope) contains a long integer
     * @return the long integer, or the value of {@link #TIMESTAMP_NOT_SET} if
     *  the method encounters a parsing error
     */
    protected long parseTimestamp( final String aString )
    {
        long theTimestamp = TIMESTAMP_NOT_SET ;
        try
        { theTimestamp = Long.parseLong( aString ) ; }
        catch( NumberFormatException nfx )
        {
            Log.e( LOG_TAG, (new StringBuffer())
                    .append( "Could not parse value [" )
                    .append( aString )
                    .append( "] as long integer; using default value [" )
                    .append( TIMESTAMP_NOT_SET )
                    .append( "] instead." )
                    .toString()
                , nfx );
        }
        return theTimestamp ;
    }

	@Override
	protected void onBindDialogView(View v)
    {
		super.onBindDialogView( v );
		mDatePicker = (DatePicker) v.findViewById( mDatePickerResID );
		mTimePicker = (TimePicker) v.findViewById( mTimePickerResID );
		mDatePicker.init( mSelectedYear, mSelectedMonth, mSelectedDay, this );
		mTimePicker.setOnTimeChangedListener( this );
		mTimePicker.setCurrentHour( mSelectedHour );
		mTimePicker.setCurrentMinute( mSelectedMinute );
		if (Build.VERSION.SDK_INT >= 11)
			mDatePicker.setMinDate(getCurrentDateTime().getTimeInMillis());
	}

    /**
     * Gets a string to be displayed in the dialog to represent the current
     * value.
     * @return a formatted string representing the current setting
     */
	protected String setSummaryText()
    {
        String theText ;
		if( mCurrentTimestamp == TIMESTAMP_NOT_SET )
			theText = mDefaultValue ;
        else
        {
            Calendar theCalendar = Calendar.getInstance() ;
            theCalendar.setTimeInMillis( mCurrentTimestamp ) ;
            theText = mDateTimeDisplayFormat.format( theCalendar.getTime() ) ;
		}
        this.setSummary( theText ) ;
        return theText ;
	}

	private Calendar getCurrentDateTime()
    { return Calendar.getInstance() ; }

    /** Gets the current date/time in milliseconds. */
	private long getCurrentDateTimeAsLong()
    { return getCurrentDateTime().getTimeInMillis() ; }

	@Override
	protected View onCreateView(ViewGroup parent)
    {
		this.setSummaryText() ;
		return super.onCreateView(parent);
	}

	@Override
	protected boolean persistString(String aNewValue)
    {
		if( super.persistString(aNewValue) )
        {
			this.setSummaryText() ;
			this.notifyChanged() ;
			return true ;
		}
        else
			return false ;
	}

	@Override
	protected void onDialogClosed( boolean positiveResult )
    {
		this.savePreference( positiveResult ) ;
		super.onDialogClosed( positiveResult ) ;
	}

    /**
     * Converts the dialog's value to a string representation of the long
     * timestamp, only if the user actually committed the value.
     * @param bSave whether the user clicked the dialog's "positive" button
     */
    private void savePreference( final boolean bSave )
    {
        if( bSave )
            this.persistString( String.valueOf( this.getSelectedTimeAsLong() ) ) ;
        else
            this.persistString( String.valueOf( TIMESTAMP_NOT_SET ) ) ;
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
	public void onDateChanged( DatePicker view, int year, int monthOfYear, int dayOfMonth)
    {
        final Calendar now = this.getCurrentDateTime() ;
        now.add( Calendar.SECOND, -1 ) ; // Prevent a race condition.
        final Calendar selected = this.getSelectedCalendar() ;

        if( selected.compareTo( now ) < 0 )
            this.setSelectedDateTime( now, true ) ;
        else
        {
            mSelectedDay = dayOfMonth;
            mSelectedMonth = monthOfYear;
            mSelectedYear = year;
        }
	}

	/**
	 * @param view      The view associated with this listener.
	 * @param hourOfDay The current hour.
	 * @param minute    The current minute.
	 */
	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
    {
        final Calendar now = this.getCurrentDateTime() ;
        now.add( Calendar.SECOND, -1 ) ; // Prevent a race condition.
        final Calendar selected = this.getSelectedCalendar() ;

        if( selected.compareTo( now ) < 0 )
            this.setSelectedDateTime( now, true );
        else
        {
            mSelectedHour = hourOfDay ;
            mSelectedMinute = minute ;
        }
	}

	/**
	 * Given a slider value, return the value that will be persisted.
	 *
	 * @param aSliderValue - the slider value
	 * @return Returns the slider value or the entryValues[slidervalue] if defined.
	 */
/*	public Object getValueToSave(int aSliderValue) {
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
*/
	/**
	 * Retrieves a timestamp parsed from the currently-saved string value in the
     * preferences.
	 * @param aDefaultValue a default value if the setting cannot be retrieved
	 * @return the timestamp parsed from the preference, or the specified
     *  default
	 */
	public long getSavedValue( long aDefaultValue )
    {
		if( this.shouldPersist() )
        {
			return this.parseTimestamp( this.getPersistedString(
                Long.toString( aDefaultValue ) ) ) ;
		}
        else
			return aDefaultValue ;
	}

    /**
     * Updates the UI's date picker with the specified values.
     * @param aDay the day of the month
     * @param aMonth the month
     * @param aYear the year
     */
	public void setUIWithDate(final int aDay, final int aMonth, final int aYear) {
		if ( mDatePicker != null) {
			mDatePicker.updateDate(aYear, aMonth, aDay);
		}
	}

    /**
     * Updates the UI's time picker with the specified values.
     * @param aHour the hour
     * @param aMinute the minute
     */
	public void setUIWithTime(final int aHour, final int aMinute) {
		if ( mTimePicker != null) {
			mTimePicker.setCurrentMinute(aMinute);
			mTimePicker.setCurrentHour(aHour);
		}
	}

	public int getSelectedMinute() { return mSelectedMinute; }

	public int getSelectedHour() { return mSelectedHour; }

	public int getSelectedDay() { return mSelectedDay; }

	public int getSelectedMonth() { return mSelectedMonth; }

	public int getSelectedYear() { return mSelectedYear; }

	public Calendar getSelectedCalendar() {
		Calendar selectedCalendar = Calendar.getInstance();
		selectedCalendar.set( mSelectedYear, mSelectedMonth, mSelectedDay,
            mSelectedHour,
            mSelectedMinute,
			0 ) ;

		return selectedCalendar;
	}

	public Date getSelectedDateTime() {
		return getSelectedCalendar().getTime();
	}

	public String getSelectedDateTimeAsString() {
		return DATE_FORMAT.format(getSelectedDateTime());
	}

	public static String getDateTimeAsString(final long dateTime) {
		Calendar selectedCalendar = Calendar.getInstance();
		selectedCalendar.setTimeInMillis(dateTime);
		Date thisDateTime = selectedCalendar.getTime();
		return READABLE_DATE_FORMAT.format(thisDateTime);
	}

	public static long getMillisecondsFromCurrentTime(final long dateTime) {
		long now = System.currentTimeMillis();
		long difference = dateTime - now;

		Log.i(LOG_TAG, "---------------------------------------------");
		Log.i(LOG_TAG, "time chosen: " + String.valueOf(dateTime));
		Log.i(LOG_TAG, "time now   : " + String.valueOf(now));
		Log.i(LOG_TAG, "difference : " + String.valueOf(difference));
		Log.i(LOG_TAG, "---------------------------------------------");

		return difference;
	}

    /**
     * Returns a UTC timestamp representing the date and time shown on the date
     * and time pickers.
     * @return the UTC timestamp, <i>in milliseconds</i>, shown on the pickers
     */
	public long getSelectedTimeAsLong() {
		return getSelectedCalendar().getTimeInMillis();
	}

	public void setSelectedDateTime(final long selectedDateTime, final boolean updateUI) {
		Calendar selectedCalendar = Calendar.getInstance();
		selectedCalendar.setTimeInMillis(selectedDateTime);
		setSelectedDateTime(selectedCalendar, false);

		if (updateUI) {
			setUIWithDate( mSelectedDay, mSelectedMonth, mSelectedYear );
			setUIWithTime( mSelectedHour, mSelectedMinute );
		}
	}

	public void setSelectedDateTime(final String selectedString, final boolean updateUI) {
		try {
			Date dateTime = DATE_FORMAT.parse(selectedString);
			setSelectedDateTime(dateTime, false);
		} catch (ParseException e) {
			Log.i(LOG_TAG, e.getMessage());
			e.printStackTrace();
		}

		if (updateUI) {
			setUIWithDate( mSelectedDay, mSelectedMonth, mSelectedYear );
			setUIWithTime( mSelectedHour, mSelectedMinute );
		}
	}

	public void setSelectedDateTime(final Date selectedDateTime, final boolean updateUI) {
		Calendar selectedCalendar = Calendar.getInstance();
		selectedCalendar.setTime(selectedDateTime);
		setSelectedDateTime(selectedCalendar, false);

		if (updateUI) {
			setUIWithDate( mSelectedDay, mSelectedMonth, mSelectedYear );
			setUIWithTime( mSelectedHour, mSelectedMinute );
		}
	}

	public void setSelectedDateTime(final Calendar selectedDateTime, final boolean updateUI) {
		mSelectedMinute = selectedDateTime.get(Calendar.MINUTE);
		mSelectedHour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
		mSelectedDay = selectedDateTime.get(Calendar.DAY_OF_MONTH);
		mSelectedMonth = selectedDateTime.get(Calendar.MONTH);
		mSelectedYear = selectedDateTime.get(Calendar.YEAR);

		if (updateUI) {
			setUIWithDate( mSelectedDay, mSelectedMonth, mSelectedYear );
			setUIWithTime( mSelectedHour, mSelectedMinute );
		}
	}
}
