package com.blackmoonit.database;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Completely static provider contracts make it impossible to use OOP in
 * order to handle common definitions and helper methods. These classes and
 * interfaces were designed to work together and mimic static contracts as
 * well as provide access to common definitions and helper methods. The end
 * goal is to make it easier to create ContentProvider contracts, which in 
 * turn allow use of a generic ContentProvider such as {@link ContractedProvider}.
 *
 * @author Ryan Fischbach
 */
public class ProviderContract {
	
	/**
	 * Database meta information definitions required by the ProviderContract.
	 */
	static public interface Database {		
		/**
		 * Gets your statically created {@link #DbProviderInfo} object (simple singleton). 
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
		 * to augment the {@link DbProviderInfo#getAuthority() provider authority} and 
		 * {@link DbProviderInfo#getBaseMIMEsubtype() MIME subtype} strings to help 
		 * ensure their global namespace uniqueness.
		 */
		public String getDbName();

	    /**
	     * Data provider scheme is traditionally "content".
	     */
		static public final String SCHEME = "content";
		
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
		protected final ProviderContract.Database mDbContract;
		
		public DbProviderInfo(Database aDbContract) {
			mDbContract = aDbContract;
		}

		/**
		 * @return Returns the database name.
		 */
		public ProviderContract.Database getDbContract() {
			return mDbContract;
		}

		/**
		 * Defines the authority that will be used to access your provider.
		 * @return Returns the Authority part of the Uri used to access your provider.
		 */
		public String getAuthority() {
			return "com.istresearch.provider."+mDbContract.getDbName();
		}

		/**
		 * MIME subtype used to differentiate records and record sets from other providers.
		 * @return Returns a string which will be used by the TableDefinitions contained
		 * in this contract to help ensure unique MIME types for our provider results.
		 */
		public String getBaseMIMEsubtype() {
			return "vnd.istresearch."+mDbContract.getDbName();
		}
		
	}
	
	//==================================================================================
	
	/**
	 * Table meta information definitions required by the ProviderContract.
	 */
	static public interface Table extends BaseColumns {
		/**
		 * Gets your statically created {@link #TableProviderInfo} object (simple singleton). 
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
		 * {@link android.content.ContentProvider#query(Uri, String[], String, String[], String) query()} 
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

	}
	
	static public class TableProviderInfo {
		protected final Database mDbContract;
		protected final Table mTableContract;
		
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
			Uri theBaseUri = Uri.parse(Database.SCHEME+"://"+
					mDbContract.getDbInfo().getAuthority()+"/"+
					mTableContract.getTableName());
			if (aIDstring!=null) {
				return Uri.withAppendedPath(theBaseUri,aIDstring);
			} else {
				return theBaseUri;
			}
		}
		
		/**
		 * Defines which segment of the Uri path is the ID portion.
		 * Base 0 position of an ID path segment means that our basic Uri path of
		 * "content://authority/tablename/ID" will return a result of 1 (0 being 
		 * that of "tablename").
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
			return getContentUri("#");
		}

		/**
		 * Adds this table's Uri matcher for a single row.
		 * @param aMatcher - the provider matcher being constructed.
		 * @param aMatchCode - the match code to return when an incoming provider Uri matches.
		 */
		public void addTableRowUri(UriMatcher aMatcher, int aMatchCode) {
			aMatcher.addURI(mDbContract.getDbInfo().getAuthority(),
					mTableContract.getTableName()+"/#",aMatchCode);
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
		 * {@link #getBaseSubType() base subtype} with the table's 
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
			return Database.MIME_CATEGORY_RESULT_SET+getMIMEsubtype();
		}
		
		/**
		 * The provider will use this method to return the MIME type for singular results.
		 */
		public String getMIMEtypeForSingularResult() {
			return Database.MIME_CATEGORY_RESULT_ONE+getMIMEsubtype();
		}
	
	}

}
