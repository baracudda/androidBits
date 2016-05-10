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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.blackmoonit.androidbits.R;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Completely static provider contracts make it impossible to use OOP in
 * order to handle common definitions and helper methods. These classes and
 * interfaces were designed to work together and mimic static contracts as
 * well as provide access to common definitions and helper methods. The end
 * goal is to make it easier to create ContentProvider contracts, which in
 * turn allow use of a generic ContentProvider such as
 * {@link com.blackmoonit.androidbits.content.SqlContractProvider SqlContractProvider}.
 *
 * @author baracudda
 */
public class ProviderContract {
	static public final String TAG = ProviderContract.class.getSimpleName();

	/**
	 * Our database interface for this contract is stored here. Instantiate it inside a static { }.
	 */
	static public DbProviderInfo mDbInfo;

	/**
	 * Our cache of table contracts.
	 */
	static public HashMap<String, Table> mTableNameContractMap = null;

	/**
	 * Convenience method for obtaining our defined table contracts.
	 * @param aTableName - the table name to look up its contract.
	 * @return Returns the Table contract information.
	 */
	static public ProviderContract.Table getTableContractFromName(String aTableName) {
		if (mTableNameContractMap==null) {
			ArrayList<ProviderContract.Table> theTableList = mDbInfo.getTableList();
			mTableNameContractMap = new HashMap<String, Table>(theTableList.size());
			for (ProviderContract.Table theContractTable : theTableList) {
				String theTableName = theContractTable.getTableName();
				mTableNameContractMap.put(theTableName, theContractTable);
			}
		}
		if (mTableNameContractMap.containsKey(aTableName)) {
			return mTableNameContractMap.get(aTableName);
		} else {
			return null;
		}
	}

	/**
	 * Database meta information definitions required by the ProviderContract.
	 */
	static public interface Database {
		/**
		 * Gets your statically created {@link com.blackmoonit.androidbits.database.ProviderContract.DbProviderInfo} object (simple singleton).
		 * Please create your static object like the sample code provided.<br><code>
		 * static public YourDatabaseContract mDbContract = new YourDatabaseContract();<br>
		 * static public DbProviderInfo mDbInfo = new DbProviderInfo(mDbContract);<br>
		 * private YourDatabaseContract() {} //remember to make your class "final"
		 * </code><br>
		 * @return Rerturn the static variable you created.
		 */
		public DbProviderInfo getDbInfo();

		/**
		 * The name of this Data Dictionary. By default, this name will be used
		 * to augment the {@link com.blackmoonit.androidbits.database.ProviderContract.DbProviderInfo#getAuthority() provider authority} and
		 * {@link com.blackmoonit.androidbits.database.ProviderContract.DbProviderInfo#getBaseMIMEsubtype() MIME subtype} strings to help
		 * ensure their global namespace uniqueness.
		 */
		public String getDbName();

		/**
		 * The version number of this contract for use in Database class to determine onUpgrade()/onDowngrade().
		 * @return Returns the db version as an integer.
		 */
		public int getDbVersion();

		/**
		 * Data actions are specified in the username@authority section of an
		 * ObserverUri. Use DATA_ACTION_NULL for a standard ContentUri.
		 * @see com.blackmoonit.androidbits.database.ProviderContract.TableProviderInfo#getObserverUri(String)
		 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_INSERT
		 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_UPDATE
		 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_DELETE
		 * @see com.blackmoonit.androidbits.database.ProviderContract.DbProviderInfo#ensureContentUri(android.net.Uri)
		 */
		static public final String DATA_ACTION_NULL = "";
		/**
		 * Data actions are specified in the username@authority section of an
		 * ObserverUri. Use DATA_ACTION_NULL for a standard ContentUri.
		 * @see com.blackmoonit.androidbits.database.ProviderContract.TableProviderInfo#getObserverUri(String)
		 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_NULL
		 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_UPDATE
		 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_DELETE
		 * @see com.blackmoonit.androidbits.database.ProviderContract.DbProviderInfo#ensureContentUri(android.net.Uri)
		 */
		static public final String DATA_ACTION_INSERT = "insert@";
		/**
		 * Data actions are specified in the username@authority section of an
		 * ObserverUri. Use DATA_ACTION_NULL for a standard ContentUri.
		 * @see com.blackmoonit.androidbits.database.ProviderContract.TableProviderInfo#getObserverUri(String)
		 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_NULL
		 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_INSERT
		 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_DELETE
		 * @see com.blackmoonit.androidbits.database.ProviderContract.DbProviderInfo#ensureContentUri(android.net.Uri)
		 */
		static public final String DATA_ACTION_UPDATE = "update@";
		/**
		 * Data actions are specified in the username@authority section of an
		 * ObserverUri. Use DATA_ACTION_NULL for a standard ContentUri.
		 * @see com.blackmoonit.androidbits.database.ProviderContract.TableProviderInfo#getObserverUri(String)
		 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_NULL
		 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_INSERT
		 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_UPDATE
		 * @see com.blackmoonit.androidbits.database.ProviderContract.DbProviderInfo#ensureContentUri(android.net.Uri)
		 */
		static public final String DATA_ACTION_DELETE = "delete@";

		/**
		 * MIME category used for returning a set of records.
		 */
		static public final String MIME_CATEGORY_RESULT_SET = "vnd.android.cursor.dir";

		/**
		 * MIME category used for returning a single record.
		 */
		static public final String MIME_CATEGORY_RESULT_ONE = "vnd.android.cursor.item";

		/**
		 * Convenience ascending constant for use in ORDER_BY clause of query strings.
		 */
		static public final String SORT_LoHi = " ASC";

		/**
		 * Convenience descending constant for use in ORDER_BY clause of query strings.
		 */
		static public final String SORT_HiLo = " DESC";

	}

	static public class DbProviderInfo {
		protected final Database mDbContract;
		protected String mDbFilename;
		protected String mAuthorityPrefix = "com.blackmoonit.provider";
		protected String mBaseMIMEsubtypePrefix = "vnd.blackmoonit";

		public DbProviderInfo(Database aDbContract) {
			mDbContract = aDbContract;
			//set the default database filename
			mDbFilename = aDbContract.getDbName()+".db";
		}

		/**
		 * @return Returns the database name.
		 */
		public Database getDbContract() {
			return mDbContract;
		}

		/**
		 * Get the database filename.
		 * @return Returns the filename to use for this database.
		 */
		public String getDbFilename() {
			return mDbFilename;
		}

		/**
		 * Some may wish to override the default db filename.
		 * @param aDbFilename - new db filename to use.
		 * @return Returns this class instance for chaining purposes.
		 */
		public DbProviderInfo setDbFilename(String aDbFilename) {
			mDbFilename = aDbFilename;
			return this;
		}

		/**
		 * Define the prefix part of your provider's authority which will be appended with "."+getDbName()
		 * @param aAuthorityPrefix - string prefix to the dbname.
		 * @return Returns this class instance for chaining purposes.
		 */
		public DbProviderInfo setAuthorityPrefix(String aAuthorityPrefix) {
			mAuthorityPrefix = aAuthorityPrefix;
			return this;
		}

		/**
		 * Defines the authority that will be used to access your provider.
		 * @return Returns the Authority part of the Uri used to access your provider.
		 */
		public String getAuthority() {
			return mAuthorityPrefix+"."+mDbContract.getDbName();
		}

		/**
		 * Define the prefix part of your provider's authority which will be appended with "."+getDbName()
		 * @param aBaseMIMEsubtypePrefix - string prefix to the dbname.
		 * @return Returns this class instance for chaining purposes.
		 */
		public DbProviderInfo setBaseMIMEsubtypePrefix(String aBaseMIMEsubtypePrefix) {
			mBaseMIMEsubtypePrefix = aBaseMIMEsubtypePrefix;
			return this;
		}

		/**
		 * MIME subtype used to differentiate records and record sets from other providers.
		 * @return Returns a string which will be used by the TableDefinitions contained
		 * in this contract to help ensure unique MIME types for our provider results.
		 */
		public String getBaseMIMEsubtype() {
			return mBaseMIMEsubtypePrefix+"."+mDbContract.getDbName();
		}

		/**
		 * MIME type used for the database itself. Specific table rows should use
		 * MyDbContract.MyTableContract.mTableInfo.getMIMEsubtype().
		 * @return MIME type string
		 * @see com.blackmoonit.androidbits.database.ProviderContract.TableProviderInfo#getMIMEsubtype()
		 * @see com.blackmoonit.androidbits.database.ProviderContract.TableProviderInfo#getMIMEtypeForResultSet()
		 * @see com.blackmoonit.androidbits.database.ProviderContract.TableProviderInfo#getMIMEtypeForSingularResult()
		 */
		public String getMIMEtype() {
			return Database.MIME_CATEGORY_RESULT_SET + "/" + getBaseMIMEsubtype();
		}

		/**
		 * The passed in Uri is converted from a potentially DataAction-specific ObserverUri
		 * to a standard ContentUri. Use this function in your ContentObserver's onChange
		 * method to convert the returned Uri back into a ContentUri so you can use it with
		 * the Provider again. It is safe to pass in either an ObserverUri or ContentUri.
		 * @param aUri - either an ObserverUri or ContentUri.
		 * @return Returns a ContentUri.
		 * @see com.blackmoonit.androidbits.database.ProviderContract.TableProviderInfo#ensureContentUri(android.net.Uri)
		 */
		static public Uri ensureContentUri(Uri aUri) {
			if (aUri!=null) {
				String theAuthority = aUri.getAuthority();
				if (theAuthority!=null) {
					int idx = theAuthority.indexOf("@");
					if (idx>=0) {
						aUri = aUri.buildUpon().encodedAuthority(theAuthority.substring(idx+1)).build();
					}
				}
			}
			return aUri;
		}

		private ArrayList<ProviderContract.Table> mTableList = null;
		public ArrayList<ProviderContract.Table> getTableList() {
			if (mTableList==null) {
				mTableList = new ArrayList<ProviderContract.Table>();
				Class[] myClasses = mDbContract.getClass().getClasses();
				for (Class theClass : myClasses) {
					if (ProviderContract.Table.class.isAssignableFrom(theClass)) {
						try {
							TableProviderInfo theTableInfo = (TableProviderInfo) (theClass.getField("mTableInfo").get(null));
							mTableList.add(theTableInfo.getTableContract());
						} catch (NoSuchFieldException nsfe) {
							//don't care, just ignore
						} catch (IllegalAccessException e) {
							//don't care, just ignore
						}
					}
				}
			}
			return mTableList;
		}

	}

	//==================================================================================

	/**
	 * Table meta information definitions required by the ProviderContract.
	 */
	static public interface Table extends BaseColumns {
		/**
		 * Gets your statically created {@link com.blackmoonit.androidbits.database.ProviderContract.TableProviderInfo} object (simple singleton).
		 * Please create your static object like the sample code provided.<br><code>
		 * static public YourTableContract mTableContract = new YourTableContract();<br>
		 * static public TableProviderInfo mTableInfo = <br>
		 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		 * new TableProviderInfo(mDbContract, mTableContract);<br>
		 * private YoutTableContract() {} //remember to make your class "final"
		 * </code><br>
		 * @return Rerturn the static variable you created.
		 */
		public TableProviderInfo getTableInfo();

		/**
		 * Gets the table name for which this dictionary defines.
		 * @return Returns the name of the table this dictionary defines.
		 */
		public String getTableName();

		/**
		 * Return the default sort order as defined by the orderBy parameter of the
		 * {@link android.content.ContentProvider#query(android.net.Uri, String[], String, String[], String) query()}
		 * method.
		 * @return Returns the orderBy string parameter that is default for this table.
		 * Returning NULL will generally be an "unordered" result.
		 */
		public String getDefaultSortOrder();

		/**
		 * Gets the fieldname the provider will use as a record ID field.
		 * _ID is predefined, so it is the recommended default.
		 * @return Returns your ID field name, consider returning _ID as the default.
		 */
		public String getIdFieldName();

		/**
		 * @return Returns either "#" for Longint IDs or "*" for strings.
		 */
		public String getUriMatcherIdWildcardChar();

		/**
		 * The data provider needs to know what columns are required for insert.
		 * Default is none, override and supply column names of required fields.
		 * @return Returns the array of required column names or an empty array if none.
		 */
		public String[] getRequiredColumns();

		/**
		 * The data provider needs to know if any columns have default values.
		 * Act upon the values parameter to add/modify any missing content.
		 * @param aContext - context in case string resources are needed.
		 * @param values - Add or modify existing values to enforce default data.
		 */
		public void populateDefaultValues(Context aContext, ContentValues values);

		/**
		 * May as well define the SQLite table here as well and keep things simple and contained.
		 * @return Returns the SQL string to be executed to create the SQLite table.
		 */
		public String getCreateTableSQL();

		/**
		 * @return Return the RowVar for my table.
		 */
		public RowVar newRowVar();

	}

	static public class TableProviderInfo {
		protected final transient Database mDbContract;
		protected final transient Table mTableContract;

		public TableProviderInfo(Database aDbContract, Table aTableContract) {
			mDbContract = aDbContract;
			mTableContract = aTableContract;
		}

		/**
		 * @return Returns the database contract.
		 */
		public Database getDbContract() {
			return mDbContract;
		}

		/**
		 * @return Returns the table contract.
		 */
		public Table getTableContract() {
			return mTableContract;
		}

		/**
		 * The content Uri for this table. If NULL is passed in as the ID parameter,
		 * the "result set" Uri is returned with no ID path segment appended.<br>
		 * NOTE: a value of "#" for the ID parameter is reserved as the
		 * {@link #getContentUriWithIdPattern() path pattern}.<br>
		 * NOTE on NULL IDs: if NULL is desired as an ID value... unknown how to support it.
		 * Tests will need to be conducted and the results should replace this text.
		 * @param aIDstring - ID parameter already converted to String to place on the Uri.
		 * If NULL is passed in, the standard "result set" Uri is returned.
		 * @return Returns the Uri to be used to interact with this table.
		 * If NULL is passed as the ID value, then the "result set" Uri will
		 * be returned, leaving the ID path segment out.
		 */
		public Uri getContentUri(String aIDstring) {
			Uri theUri = getObserverUri(Database.DATA_ACTION_NULL);
			if (aIDstring!=null) {
				theUri = Uri.withAppendedPath(theUri,aIDstring);
			}
			return theUri;
		}

		/**
		 * ContentObservers that wish to know what kind of action caused the onChange to
		 * fire would need to register their Uri using this function with the appropriate
		 * Database.DATA_ACTION_* constant passed in.
		 * @param aDataAction - one of {@link com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_INSERT DATA_ACTION_INSERT} or
		 * {@link com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_UPDATE DATA_ACTION_UPDATE} or
		 * {@link com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_DELETE DATA_ACTION_DELETE} or
		 * {@link com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_NULL DATA_ACTION_NULL}
		 * @return Returns the Uri to register to observe particular Provider actions.
		 */
		public Uri getObserverUri(String aDataAction) {
			Uri theUri = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + aDataAction +
					mDbContract.getDbInfo().getAuthority() + "/" +
					mTableContract.getTableName());
			return theUri;
		}

		/**
		 * This method is used internally by the SqlContractProvider to convert the
		 * standard Observer notification ContentUri into a specific data action Uri
		 * whose term I have coined as an ObserverUri.
		 * @param aUri - ContentUri to convert to an ObserverUri for a specific data action.
		 * @param aDataAction - encode this data action within the Uri passed in.
		 * @return Returns an ObserverUri encoded for a specific data action.
		 */
		static public Uri cnvContentUriToObserverUri(Uri aUri, String aDataAction) {
			if (aUri!=null) {
				return aUri.buildUpon().encodedAuthority(aDataAction+aUri.getAuthority()).build();
			}
			return null;
		}

		/**
		 * The passed in Uri is converted from a potentially DataAction-specific ObserverUri
		 * to a standard ContentUri. Use this function in your ContentObserver's onChange
		 * method to convert the returned Uri back into a ContentUri so you can use it with
		 * the Provider again. It is safe to pass in either an ObserverUri or ContentUri.
		 * @param aUri - either an ObserverUri or ContentUri.
		 * @return Returns a ContentUri.
		 * @see com.blackmoonit.androidbits.database.ProviderContract.DbProviderInfo#ensureContentUri(android.net.Uri)
		 */
		public Uri ensureContentUri(Uri aUri) {
			return DbProviderInfo.ensureContentUri(aUri);
		}


		/**
		 * Defines which segment of the Uri path is the ID portion.
		 * Base 0 position of an ID path segment means that our basic Uri path of
		 * "content://authority/tablename/ID" will return a result of 1
		 * (0 being that of "tablename").
		 * @return Returns the segment number (base 0) of the ID portion of the Uri
		 * returned in {@link #getContentUri(String)}.
		 */
		public int getUriPathIdPosition() {
			return 1;
		}

		/**
		 * The content Uri match pattern for a single record of this table.
		 * Use this to match incoming Uri's in your provider.
		 * @return Returns {@link #getContentUri(String) getContentUri("#")}.
		 */
		public Uri getContentUriWithIdPattern() {
			return getContentUri(mTableContract.getUriMatcherIdWildcardChar());
		}

		/**
		 * Adds this table's Uri matcher for a single row.
		 * @param aMatcher - the provider matcher being constructed.
		 * @param aMatchCode - the match code to return when an incoming provider Uri matches.
		 */
		public void addTableRowUri(UriMatcher aMatcher, int aMatchCode) {
			aMatcher.addURI(mDbContract.getDbInfo().getAuthority(),
					mTableContract.getTableName()+"/"+
					mTableContract.getUriMatcherIdWildcardChar(),aMatchCode);
		}

		/**
		 * Adds this table's Uri matcher for a set of rows.
		 * @param aMatcher - the provider matcher being constructed.
		 * @param aMatchCode - the match code to return when an incoming provider Uri matches.
		 */
		public void addTableSetUri(UriMatcher aMatcher, int aMatchCode) {
			aMatcher.addURI(mDbContract.getDbInfo().getAuthority(),
					mTableContract.getTableName(),aMatchCode);
		}

		/**
		 * Gets the MIME subtype for this table's data.
		 * @return By default, this returns the data dictionary's
		 * {@link com.blackmoonit.androidbits.database.ProviderContract.DbProviderInfo#getBaseMIMEsubtype() base subtype} with the table's
		 * name appended to it.
		 */
		public String getMIMEsubtype() {
			return mDbContract.getDbInfo().getBaseMIMEsubtype()+"."+
					mTableContract.getTableName();
		}

		/**
		 * The provider will use this method to return the MIME type for result sets.
		 * @return Returns the full MIME type for result sets.
		 */
		public String getMIMEtypeForResultSet() {
			return Database.MIME_CATEGORY_RESULT_SET+"/"+getMIMEsubtype();
		}

		/**
		 * The provider will use this method to return the MIME type for singular results.
		 */
		public String getMIMEtypeForSingularResult() {
			return Database.MIME_CATEGORY_RESULT_ONE+"/"+getMIMEsubtype();
		}

		/**
		 * Cache of the columns found from getColNamesFromMyContract().
		 */
		protected ArrayList<String> myColNamesFromMyContract = null;

		/**
		 * Analyzes the Table Contract and reports back all COL_* fields
		 * as well as the _ID field.
		 * @return Returns the column names as an ArrayList.
		 */
		public ArrayList<String> getColNamesFromMyContract() {
			if (this.myColNamesFromMyContract==null) {
				this.myColNamesFromMyContract = new ArrayList<String>();
				Field[] myFields = getTableContract().getClass().getFields();
				for (Field theField : myFields) {
					if (theField.getName().startsWith("COL_") || theField.getName().equals("_ID")) {
						try {
							this.myColNamesFromMyContract.add((String) theField.get(getTableContract()));
						} catch (IllegalAccessException e) {
							if (!theField.getName().toLowerCase(Locale.US).contains(" as "))
							Log.e(TAG, "Could not access column definition as a field: "+
									theField.getName(), e);
						}
					}
				}
			}
			return this.myColNamesFromMyContract;
		}

		public <T extends RowVar> T newRowVar(Context aContext, String aIDstring) {
			RowVar theResult = getTableContract().newRowVar();
			if (aIDstring!=null) {
				theResult.getSingleRow(aContext, aIDstring);
			}
			return theResult.asMyType();
		}

		public <T extends RowVar> T newRowVar(Context aContext, Long aID) {
			RowVar theResult = getTableContract().newRowVar();
			if (aID!=null) {
				theResult.getSingleRow(aContext, aID);
			}
			return theResult.asMyType();
		}

		public <T extends RowVar> T newRowVar(Cursor aCursor) {
			return getTableContract().newRowVar().setFromCursor(aCursor);
		}

		/**
		 * Called to create the table if it does not exist.
		 * @param db - the database object
		 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(SQLiteDatabase)
		 */
		public void onCreate(SQLiteDatabase db) {
			String theSql = getTableContract().getCreateTableSQL();
			db.execSQL(theSql);
		}

		/**
		 * Called during the database onUpgrade() method.
		 * @param db - the database object
		 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(SQLiteDatabase, int, int)
		 */
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//default behavior is to just drop the table and data, then recreate the new structure
			String theSql = "DROP TABLE IF EXISTS "+getTableContract().getTableName();
			db.execSQL(theSql);
			theSql = getTableContract().getCreateTableSQL();
			db.execSQL(theSql);
		}

		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//just wipe it out for now
			onUpgrade(db, oldVersion, newVersion);
		}

	}

	static public abstract class RowVar {
		protected final transient TableProviderInfo myTableInfo;
		protected final transient boolean bLogInsAndDel = false;
		public Long _id = null; // used for _ID (which is a longint)

		public RowVar(TableProviderInfo aTableInfo) {
			myTableInfo = aTableInfo;
		}

		public <T extends RowVar> T asMyType() {
			return (T)this;
		}

		public TableProviderInfo getMyTableInfo() {
			return myTableInfo;
		}

		public Uri getMyTableUri() {
			return myTableInfo.getContentUri(null);
		}

		public String getMyMimeType() {
			return myTableInfo.getMIMEtypeForSingularResult();
		}

		public Object getMyIdValue() {
			String myIdFieldName = myTableInfo.getTableContract().getIdFieldName();
			try {
				return this.getClass().getField(myIdFieldName).get(this);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				Log.wtf(TAG,String.format(Locale.ENGLISH,
						"The ID field returned by getIdFieldName(), %s, cannot be accessed in RowVar descendant %s.",
						myIdFieldName,this.getClass().getCanonicalName()));
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
				Log.wtf(TAG,String.format(Locale.ENGLISH,
						"The ID field returned by getIdFieldName(), %s, does not exist in RowVar descendant %s.",
						myIdFieldName,this.getClass().getCanonicalName()));
			}
			return null;
		}

		public Uri getMyUri() {
			Object theValue = getMyIdValue();
			//String.valueOf(null) == "null" which isn't NULL.
			String theIdAsStr = (theValue!=null) ? String.valueOf(theValue) : null;
			return myTableInfo.getContentUri(theIdAsStr);
		}

		public ArrayList<String> getColNamesFromMyContract() {
			return myTableInfo.getColNamesFromMyContract();
		}

		public String cnvColNameToRowFieldName(String aColName) {
			//var names cannot have spaces in them like db column names, cnv to "_"
			return (aColName!=null) ? aColName.replace(" ", "_") : null;
		}

		protected Field getRowField(String aRowFieldName) throws NoSuchFieldException {
			return getClass().getField(aRowFieldName);
		}

		protected <T extends RowVar> T setRowField(Field aRowField, Object aRowFieldValue) {
			try {
				aRowField.set(this, aRowFieldValue);
			} catch (IllegalAccessException e) {
				if ( !Modifier.isFinal(aRowField.getModifiers()) )
					e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			return (T)this;
		}

		public Object getColValue(String aColName) throws IllegalArgumentException {
			try {
				getRowField(cnvColNameToRowFieldName(aColName)).get(this);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new IllegalArgumentException(aColName+" does not match any accessible "+
						"fields inside the RowVar class \""+getClass().getSimpleName()+"\"");
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
				throw new IllegalArgumentException(aColName+" does not match any accessible "+
						"fields inside the RowVar class \""+getClass().getSimpleName()+"\"");
			}
			return null;
		}

		public <T extends RowVar> T setColValue(String aColName, Object aColValue) {
			try {
				setRowField(getRowField(cnvColNameToRowFieldName(aColName)), aColValue);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
			return (T)this;
		}

		public <T extends RowVar> T setFromCursor(Cursor aCursor) {
			if (aCursor != null) {
				for (String theColName : getColNamesFromMyContract()) {
					//inner loop vars used so multi-core processors with preditive branch
					//  processing are not constrained by accessing a single var; results
					//  in multiple loops being processed concurrently, thus enhancing speed
					//  at the sacrifice of a bit of memory/garbage collecting
					Field theRowField;
					int theColIdx;
					try {
						theRowField = getRowField(cnvColNameToRowFieldName(theColName));
						theColIdx = aCursor.getColumnIndex(theColName);
						if (theColIdx>=0) {
							Class<?> theRowFieldType = theRowField.getType();
							//Cursor does not have a generic GET method, so need to check types
							if (theRowFieldType.equals(String.class)) {
								setRowField(theRowField, aCursor.getString(theColIdx));
							} else if (theRowFieldType.equals(Integer.TYPE) || theRowFieldType==Integer.class) {
								setRowField(theRowField, aCursor.getInt(theColIdx));
							} else if (theRowFieldType.equals(Long.TYPE) || theRowFieldType==Long.class) {
								setRowField(theRowField, aCursor.getLong(theColIdx));
							} else if (theRowFieldType.equals(Float.TYPE) || theRowFieldType==Float.class) {
								setRowField(theRowField, aCursor.getFloat(theColIdx));
							} else if (theRowFieldType.equals(Double.TYPE) || theRowFieldType==Double.class) {
								setRowField(theRowField, aCursor.getDouble(theColIdx));
							} else if (theRowFieldType.equals(Boolean.TYPE) || theRowFieldType==Boolean.class) {
								setRowField(theRowField, (aCursor.getInt(theColIdx)>0));
							} else if (theRowFieldType.equals(Character.TYPE) || theRowFieldType==Character.class) {
								String s = aCursor.getString(theColIdx);
								if (s!=null && s.length()>0)
									setRowField(theRowField, s.charAt(0));
							} else if (theRowFieldType.equals(Byte.TYPE) || theRowFieldType==Byte.class) {
								setRowField(theRowField,  Byte.parseByte(aCursor.getString(theColIdx)));
							} else if (theRowFieldType.equals(Short.TYPE) || theRowFieldType==Short.class) {
								setRowField(theRowField,  aCursor.getShort(theColIdx));
							}
						}
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					}
				}
			}
			return (T)this;
		}

		/**
		 * Helper method to read a list of RowVars from a Bundle (such as an Intent extra).
		 * @param aBundle - the Bundle containing the list data.
		 * @param aKey - the key used to retrieve the list data.
		 * @return Returns the List of RowVar data.
		 */
		protected <T extends RowVar> void fillRowVarListFromBundle(List<T> aList,
				Bundle aBundle, String aKey, Class<T> aListItemRowVarClass) {
			if (aList!=null && aBundle!=null && aKey!=null &&
					aListItemRowVarClass!=null && aBundle.containsKey(aKey))
			{
				Parcelable[] theParcels = aBundle.getParcelableArray(aKey);
				Bundle[] theBundles = new Bundle[theParcels.length];
				System.arraycopy(theParcels, 0, theBundles, 0, theParcels.length);
				for (Bundle theListItemData : theBundles) {
					//inner loop vars used so multi-core processors with preditive branch
					//  processing are not constrained by accessing a single var; results
					//  in multiple loops being processed concurrently, thus enhancing speed
					//  at the sacrifice of a bit of memory/garbage collecting
					try {
						aList.add((T) aListItemRowVarClass.newInstance().setFromBundle(theListItemData));
					} catch (InstantiationException e) {
						Log.d(TAG, aListItemRowVarClass.getName()+" needs a default no-arg constructor.");
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}

		public <T extends RowVar> T setFromBundle(Bundle aBundle) {
			if (aBundle!=null) {
				for (String theColName : getColNamesFromMyContract()) {
					//inner loop vars used so multi-core processors with preditive branch
					//  processing are not constrained by accessing a single var; results
					//  in multiple loops being processed concurrently, thus enhancing speed
					//  at the sacrifice of a bit of memory/garbage collecting
					Field theRowField;
					try {
						theRowField = getRowField(cnvColNameToRowFieldName(theColName));
						setRowField(theRowField, aBundle.get(theRowField.getName()));
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					}
				}
			}
			return (T)this;
		}

		/**
		 * Helper method that will copy data found in the intent to our RowVar's fields based
		 * on the table contract defining the COL_* names to use.
		 * @param aIntent - the intent whose data we will copy to our fields.
		 */
		public <T extends RowVar> T setFromIntent(Intent aIntent) {
			if (aIntent!=null)
				setFromBundle(aIntent.getExtras());
			return (T)this;
		}

		/**
		 * Helper method that will copy data found in the parameter to our RowVar's fields based
		 * on the public vars defined.
		 * @param aRowVar - the RowVar instance whose data we will copy to our fields.
		 */
		public <T extends RowVar> T setFromRowVar(RowVar aRowVar) {
			if (aRowVar!=null) {
				Field[] theRowVarFields = aRowVar.getClass().getFields();
				for (Field theRowVarField : theRowVarFields) {
					//inner loop vars used so multi-core processors with preditive branch
					//  processing are not constrained by accessing a single var; results
					//  in multiple loops being processed concurrently, thus enhancing speed
					//  at the sacrifice of a bit of memory/garbage collecting
					Field myField;
					try {
						myField = getRowField(theRowVarField.getName());
						setRowField(myField, theRowVarField.get(aRowVar));
					} catch (NoSuchFieldException e) {
						//do not care
					} catch (IllegalAccessException e) {
						//do not care
					}
				}
			}
			return (T)this;
		}

		/**
		 * Helper method to write a list of RowVars to a List of Bundles so they can be stored
		 * as a ParcelableArray in a Bundle such as an Intent extra.
		 * @param aRowVarList - the list to convert.
		 * @return Returns the List of Bundles.
		 */
		protected Parcelable[] cnvRowVarListToParcelableArray(List<? extends RowVar> aRowVarList) {
			Parcelable[] theResult = null;
			if (aRowVarList!=null) {
				theResult = new Parcelable[aRowVarList.size()];
				for (int i = 0; i < aRowVarList.size(); i++) {
					theResult[i] = aRowVarList.get(i).toBundle();
				}
			}
			return theResult;
		}

		public Bundle toBundle(Bundle aBundle) {
			Bundle theResults = (aBundle!=null) ? new Bundle(aBundle) : new Bundle();
			for (String theColName : getColNamesFromMyContract()) {
				//inner loop vars used so multi-core processors with preditive branch
				//  processing are not constrained by accessing a single var; results
				//  in multiple loops being processed concurrently, thus enhancing speed
				//  at the sacrifice of a bit of memory/garbage collecting
				String theRowFieldName = cnvColNameToRowFieldName(theColName);
				Field theRowField;
				try {
					theRowField = getRowField(theRowFieldName);
					Class<?> theRowFieldType = theRowField.getType();
					//Bundle does not have a generic PUT method, so need to check types
					if (theRowFieldType.equals(String.class)) {
						theResults.putString(theRowFieldName, (String) theRowField.get(this));
					} else if (theRowFieldType.equals(Integer.TYPE) || theRowFieldType==Integer.class) {
						theResults.putInt(theRowFieldName, (Integer) theRowField.get(this));
					} else if (theRowFieldType.equals(Long.TYPE) ||
							(theRowFieldType==Long.class && theRowField.get(this)!=null) ) {
						theResults.putLong(theRowFieldName, (Long) theRowField.get(this));
					} else if (theRowFieldType.equals(Float.TYPE) ||
							(theRowFieldType==Float.class && theRowField.get(this)!=null) ) {
						theResults.putFloat(theRowFieldName, (Float) theRowField.get(this));
					} else if (theRowFieldType.equals(Double.TYPE) ||
							(theRowFieldType==Double.class && theRowField.get(this)!=null) ) {
						theResults.putDouble(theRowFieldName, (Double) theRowField.get(this));
					} else if (theRowFieldType.equals(Boolean.TYPE) || theRowFieldType==Boolean.class) {
						theResults.putBoolean(theRowFieldName, (Boolean) theRowField.get(this));
					} else if (theRowFieldType.equals(Character.TYPE) || theRowFieldType==Character.class) {
						theResults.putChar(theRowFieldName, (Character) theRowField.get(this));
					} else if (theRowFieldType.equals(Byte.TYPE) ||
							(theRowFieldType==Byte.class && theRowField.get(this)!=null) ) {
						theResults.putByte(theRowFieldName, (Byte) theRowField.get(this));
					} else if (theRowFieldType.equals(Short.TYPE) ||
							(theRowFieldType==Short.class && theRowField.get(this)!=null) ) {
						theResults.putShort(theRowFieldName, (Short) theRowField.get(this));
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			if (getMyIdValue()!=null)
				theResults.putParcelable("android.intent.extra.ORIGINATING_URI", getMyUri());
			return theResults;
		}

		public Bundle toBundle() {
			return toBundle(null);
		}

		public ContentValues toContentValues(boolean bOmitNulls) {
			ContentValues theResults = new ContentValues();
			for (String theColName : getColNamesFromMyContract()) {
				//inner loop vars used so multi-core processors with preditive branch
				//  processing are not constrained by accessing a single var; results
				//  in multiple loops being processed concurrently, thus enhancing speed
				//  at the sacrifice of a bit of memory/garbage collecting
				String theRowFieldName = cnvColNameToRowFieldName(theColName);
				Field theRowField;
				Object theColValue;
				try {
					theRowField = getRowField(theRowFieldName);
					Class<?> theRowFieldType = theRowField.getType();
					theColValue = theRowField.get(this);
					if (theColValue!=null || !bOmitNulls) {
						if (theColValue==null)
							theResults.putNull(theColName);
						else
						//ContentValues does not have a generic PUT method, so need to check types
						if (theRowFieldType.equals(String.class)) {
							theResults.put(theColName, (String) theColValue);
						} else if (theRowFieldType.equals(Integer.TYPE) || theRowFieldType==Integer.class) {
							theResults.put(theColName, (Integer) theColValue);
						} else if (theRowFieldType.equals(Long.TYPE) || theRowFieldType==Long.class) {
							theResults.put(theColName, (Long) theColValue);
						} else if (theRowFieldType.equals(Float.TYPE) || theRowFieldType==Float.class) {
							theResults.put(theColName, (Float) theColValue);
						} else if (theRowFieldType.equals(Double.TYPE) || theRowFieldType==Double.class) {
							theResults.put(theColName, (Double) theColValue);
						} else if (theRowFieldType.equals(Boolean.TYPE) || theRowFieldType==Boolean.class) {
							theResults.put(theColName, (Boolean) theColValue);
						} else if (theRowFieldType.equals(Character.TYPE) || theRowFieldType==Character.class) {
							theResults.put(theColName, (String) theColValue);
						} else if (theRowFieldType.equals(Byte.TYPE) || theRowFieldType==Byte.class) {
							theResults.put(theColName, (Byte) theColValue);
						} else if (theRowFieldType.equals(Short.TYPE) || theRowFieldType==Short.class) {
							theResults.put(theColName, (Short) theColValue);
						}
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			return theResults;
		}

		public String toString() {
			String theResult = "{" + getClass().getSimpleName();
			for (String theColName : getColNamesFromMyContract()) {
				//inner loop vars used so multi-core processors with preditive branch
				//  processing are not constrained by accessing a single var; results
				//  in multiple loops being processed concurrently, thus enhancing speed
				//  at the sacrifice of a bit of memory/garbage collecting
				Field theRowField;
				try {
					theRowField = getRowField(cnvColNameToRowFieldName(theColName));
					Object theRowValue = theRowField.get(this);
					if (theRowValue!=null)
						theResult += ", "+theRowField.getName()+"="+String.valueOf(theRowValue);
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			theResult += "}";
			return theResult;
		}

		/**
		 * Clear out all data and set to elements to NULL/0 where appropriate.
		 */
		public <T extends RowVar> T clear() {
			for (String theColName : getColNamesFromMyContract()) {
				//inner loop vars used so multi-core processors with preditive branch
				//  processing are not constrained by accessing a single var; results
				//  in multiple loops being processed concurrently, thus enhancing speed
				//  at the sacrifice of a bit of memory/garbage collecting
				Field theRowField;
				try {
					theRowField = getRowField(cnvColNameToRowFieldName(theColName));
					Class<?> theRowFieldType = theRowField.getType();
					if (theRowFieldType.equals(String.class)) {
						setRowField(theRowField, null);
					} else if (theRowFieldType.equals(Integer.TYPE) || theRowFieldType==Integer.class) {
						setRowField(theRowField, 0);
					} else if (theRowFieldType.equals(Long.TYPE) || theRowFieldType==Long.class) {
						setRowField(theRowField, 0L);
					} else if (theRowFieldType.equals(Float.TYPE) || theRowFieldType==Float.class) {
						setRowField(theRowField, 0.0f);
					} else if (theRowFieldType.equals(Double.TYPE) || theRowFieldType==Double.class) {
						setRowField(theRowField, 0.0d);
					} else if (theRowFieldType.equals(Boolean.TYPE) || theRowFieldType==Boolean.class) {
						setRowField(theRowField, false);
					} else if (theRowFieldType.equals(Character.TYPE) || theRowFieldType==Character.class) {
						setRowField(theRowField, '\u0000');
					} else if (theRowFieldType.equals(Byte.TYPE) || theRowFieldType==Byte.class) {
						setRowField(theRowField, 0);
					} else if (theRowFieldType.equals(Short.TYPE) || theRowFieldType==Short.class) {
						setRowField(theRowField, 0);
					} else
						setRowField(theRowField, null);
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			return (T)this;
		}

		/**
		 * Insert this data into the RowVar's table.
		 * @param aContext - context to use.
		 * @param aContentResolver - ContentResolver to use (unit tests use a mock one).
		 * @return Returns the Uri of the inserted data.
		 */
		public Uri insertIntoTable(Context aContext, ContentResolver aContentResolver) {
			ContentValues aValues = toContentValues(true);
			Uri theNewRecUri = aContentResolver.insert(getMyTableUri(), aValues);
			if (bLogInsAndDel && theNewRecUri != null)
				Log.d(TAG, aContext.getString(R.string.sql_contract_provider_msg_insert_success,
						theNewRecUri));
			return theNewRecUri;
		}

		/**
		 * Insert this data into the RowVar's table.
		 * @param aContext - context to use.
		 * @return Returns the Uri of the inserted data.
		 */
		public Uri insertIntoTable(Context aContext) {
			return insertIntoTable(aContext, aContext.getContentResolver());
		}

		/**
		 * Insert this data into the RowVar's table and immediately get it back
		 * with all the other fields that got updated as well.
		 * @param aContext - context to use.
		 * @return Returns the Uri of the inserted data.
		 */
		public Uri insertAndGet(Context aContext) {
			Uri theNewUri = insertIntoTable(aContext, aContext.getContentResolver());
			getSingleRow(aContext, theNewUri);
			return theNewUri;
		}

		/**
		 * Retrieve a specific row in RowVar's table.
		 * @param aContext - context to use.
		 * @param aIDstring - string value of the table's ID field to find and load.
		 * @param aContentResolver - ContentResolver to use (unit tests use a mock one).
		 * @return Returns TRUE on successful query and data load.
		 */
		public boolean getSingleRow(Context aContext, String aIDstring, ContentResolver aContentResolver) {
			Cursor theEntryCursor = null;
			try {
				if (aContentResolver==null)
					aContentResolver = aContext.getContentResolver();
				Uri theUri = myTableInfo.getContentUri(aIDstring);
				theEntryCursor = aContentResolver.query(theUri,null,null,null,null);
				if (theEntryCursor != null && theEntryCursor.moveToFirst()) {
					setFromCursor(theEntryCursor);
					return true;
				}
			}
			finally {
				if (theEntryCursor!=null && !theEntryCursor.isClosed())
					theEntryCursor.close();
			}
			return false;
		}

		/**
		 * Retrieve a specific row in RowVar's table.
		 * @param aContext - context to use.
		 * @param aIDstring - string value of the table's ID field to find and load.
		 * @return Returns TRUE on successful query and data load.
		 */
		public boolean getSingleRow(Context aContext, String aIDstring) {
			return getSingleRow(aContext, aIDstring, null);
		}

		/**
		 * Retrieve a specific row by the _ID value in RowVar's table.
		 * @param aContext - context to use.
		 * @param aID - the _ID of the record to retrieve.
		 * @param aContentResolver - ContentResolver to use (unit tests use a mock one).
		 * @return Returns TRUE on successful query and data load.
		 */
		public boolean getSingleRow(Context aContext, Long aID, ContentResolver aContentResolver) {
			Cursor theEntryCursor = null;
			try {
				if (aContentResolver==null)
					aContentResolver = aContext.getContentResolver();
				theEntryCursor = aContentResolver.query(getMyTableUri(), null,
						Table._ID + "=?",
						new String[] { String.valueOf(aID) }, null);
				if (theEntryCursor != null && theEntryCursor.moveToFirst()) {
					setFromCursor(theEntryCursor);
					return true;
				}
			}
			finally {
				if (theEntryCursor!=null && !theEntryCursor.isClosed())
					theEntryCursor.close();
			}
			return false;
		}

		/**
		 * Retrieve a specific row by the _ID value in RowVar's table.
		 * @param aContext - context to use.
		 * @param aID - the _ID of the record to retrieve.
		 * @return Returns TRUE on successful query and data load.
		 */
		public boolean getSingleRow(Context aContext, Long aID) {
			return getSingleRow(aContext, aID, null);
		}

		/**
		 * Retrieve a specific row by a Uri in RowVar's table.
		 * @param aContext - context to use.
		 * @param aUri - the Uri of the record to retrieve.
		 * @param aContentResolver - ContentResolver to use (unit tests use a mock one).
		 * @return Returns TRUE on successful query and data load.
		 */
		public boolean getSingleRow(Context aContext, Uri aUri, ContentResolver aContentResolver) {
			Cursor theEntryCursor = null;
			try {
				if (aContentResolver==null)
					aContentResolver = aContext.getContentResolver();
				theEntryCursor = aContentResolver.query(aUri, null, null, null, null);
				if (theEntryCursor != null && theEntryCursor.moveToFirst()) {
					setFromCursor(theEntryCursor);
					return true;
				}
			}
			finally {
				if (theEntryCursor!=null && !theEntryCursor.isClosed())
					theEntryCursor.close();
			}
			return false;
		}

		/**
		 * Retrieve a specific row by a Uri in RowVar's table.
		 * @param aContext - context to use.
		 * @param aUri - the Uri of the record to retrieve.
		 * @return Returns TRUE on successful query and data load.
		 */
		public boolean getSingleRow(Context aContext, Uri aUri) {
			return getSingleRow(aContext, aUri, null);
		}

		/**
		 * Delete a specified ID from this RowVar's table.
		 * @param aContext - context to use.
		 * @param aIDstring - string value of the table's ID field to find and delete.
		 * @param aContentResolver - ContentResolver to use (unit tests use a mock one).
		 * @return Returns TRUE on successful delete, FALSE on fail or nothing to delete.
		 */
		public boolean removeSingleRow(Context aContext, String aIDstring, ContentResolver aContentResolver){
			if (aContentResolver==null)
				aContentResolver = aContext.getContentResolver();
			Uri theUri = myTableInfo.getContentUri(aIDstring);
			int theDelResult = aContentResolver.delete(theUri, null, null);
			if (bLogInsAndDel)
				Log.d(TAG, aContext.getString(R.string.sql_contract_provider_msg_delete_result,
						theUri, theDelResult));
			return (theDelResult>0);
		}

		/**
		 * Delete a specified ID from this RowVar's table.
		 * @param aContext - context to use.
		 * @param aIDstring - string value of the table's ID field to find and delete.
		 * @return Returns TRUE on successful delete, FALSE on fail or nothing to delete.
		 */
		public boolean removeSingleRow(Context aContext, String aIDstring){
			return removeSingleRow(aContext, aIDstring, null);
		}

		/**
		 * Delete the row that I represent from this RowVar's table.
		 * @param aContext - context to use.
		 * @return Returns TRUE on successful delete, FALSE on fail or nothing to delete.
		 */
		public boolean removeMyself(Context aContext){
			String theId = String.valueOf(getMyIdValue());
			if (!TextUtils.isEmpty(theId))
				return removeSingleRow(aContext, theId, null);
			else
				return false;
		}

		/**
		 * Update a record in RowVar's table using the RowVar's data.
		 * @param aContext - context to use.
		 * @param aContentResolver - ContentResolver to use (unit tests use a mock one).
		 * @return Returns TRUE on successful update.
		 */
		public boolean updateSingleRow(Context aContext, ContentResolver aContentResolver) {
			if (aContentResolver==null)
				aContentResolver = aContext.getContentResolver();
			ContentValues theValues = toContentValues(false);
			//do not update ID columns
			theValues.remove(BaseColumns._ID);
			//do not update primary IdField either
			theValues.remove(myTableInfo.mTableContract.getIdFieldName());
			if (getMyIdValue()!=null)
				return (aContentResolver.update(getMyUri(), theValues, null, null)>0);
			else
				return false;
		}
		/**
		 * Update a record in RowVar's table using the RowVar's data.
		 * @param aContext - context to use.
		 * @return Returns TRUE on successful update.
		 */
		public boolean updateSingleRow(Context aContext) {
			return updateSingleRow(aContext, null);
		}

	}

}
