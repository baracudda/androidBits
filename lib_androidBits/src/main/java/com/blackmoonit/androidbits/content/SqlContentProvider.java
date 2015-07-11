package com.blackmoonit.androidbits.content;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Ancestor content provider that handles many of the mundane operations in a uniform way so that
 * any provider based on it will function similarly and with minimal hassle.
 * Query Uris can use the LIMIT and OFFSET keywords in their query as long as they make them part
 * of the Uri query parameters {"content://blah/blah?limit=1&offset=5"} as lowercase keys.
 *
 * @author Ryan Fischbach
 */
public abstract class SqlContentProvider extends ContentProvider {
	static public final String QUERY_LIMIT = "limit";
	static public final String QUERY_OFFSET = "offset";

	protected SQLiteOpenHelper mDb;
	protected UriMatcher mUriMatcher;
	private int R_string_sql_content_provider_msg_insert_failed = 0;
	private int R_string_sql_content_provider_msg_column_missing = 0;

	static protected int getResId(Context aContext, String aResType, String aResName) {
        try {
            return aContext.getApplicationContext().getResources().getIdentifier(aResName, aResType,
                    aContext.getPackageName());
        } catch (Exception e) {
            return 0;
        }
	}

	protected int getResId(String aResType, String aResName) {
		return getResId(getContext(),aResType,aResName);
	}

	/**
	 * Builds up a UriMatcher.
	 */
	protected abstract UriMatcher buildUriMatcher();

	/**
	 * Construct a new database instance to use in this provider.
	 * Called by onCreate().
	 * @param aContext - the context of the provider.
	 * @return Returns the instantiated database object.
	 */
	protected abstract SQLiteOpenHelper newDbInstance(Context aContext);

	@Override
	public boolean onCreate() {
		R_string_sql_content_provider_msg_insert_failed = getResId("string","sql_content_provider_msg_column_missing");
		R_string_sql_content_provider_msg_column_missing = getResId("string","R_string_sql_content_provider_msg_column_missing");
		mUriMatcher = buildUriMatcher();
		mDb = newDbInstance(getContext());
		return (mDb!=null);
	}

	/**
	 * Common cleanup when finished method called by shutdown() or finalize().
	 */
	protected void cleanup() {
		if (mDb!=null) {
			mDb.close();
			mDb = null;
		}
	}

	@Override
    @TargetApi(11)
	public void shutdown() {
		cleanup();
		super.shutdown();
	}

	@Override
	protected void finalize() throws Throwable {
		cleanup();
		super.finalize();
	}

	protected abstract String getTableName(int aMatchId);

	protected abstract String getDefaultSortOrder(int aMatchId);

	protected abstract String[] getDefaultColumns(int aMatchId);

	/**
	 * Helper function for {@link #appendSelection(android.net.Uri,int,String)}.
	 * @param aSelection - the selection string
	 * @param aTermToAppend - the term to append to the selection string.
	 * @return Returns the appended string.
	 */
	static public String appendToSelection(String aSelection, String aTermToAppend) {
		return (TextUtils.isEmpty(aSelection)) ? aTermToAppend : "("+aSelection+") AND "+aTermToAppend;
	}

	/**
	 * Helper function for {@link #appendSelArgs(android.net.Uri,int,String[])}.
	 * @param aSelectionArgs - the selection args.
	 * @param aArgToAppend - the arg to append to the selection arg array.
	 * @return Returns the appended array.
	 */
	static public String[] appendToSelArgs(String[] aSelectionArgs, String aArgToAppend) {
		if (aSelectionArgs==null || aSelectionArgs.length<1) {
			return new String[] { aArgToAppend };
		} else {
		    String[] theResult = new String[aSelectionArgs.length+1];
		    System.arraycopy(aSelectionArgs, 0, theResult, 0, aSelectionArgs.length);
		    theResult[aSelectionArgs.length] = aArgToAppend;
		    return theResult;
		}
	}

	/**
	 * Some queries may need to append items to the where clause based on the Uri.
	 * @param aUri - Uri of the statement.
	 * @param aMatchId - match int defined in {@link #buildUriMatcher()}.
	 * @param aSelection - the selection arg passed in.
	 * @return Returns the updated Selection string. May be NULL.
	 */
	protected abstract String appendSelection(Uri aUri, int aMatchId, String aSelection);

	/**
	 * Appends more arguments into the SelectionArgs passed in.
	 * @param aUri - Uri of the statement.
	 * @param aMatchId - match int defined in {@link #buildUriMatcher()}.
	 * @param aSelectionArgs - the selection args passed in.
	 */
	protected abstract String[] appendSelArgs(Uri aUri, int aMatchId, String[] aSelectionArgs);

	protected String getQueryLimitClause(Uri aUri) {
		String theResult = aUri.getQueryParameter(QUERY_LIMIT);
		if (theResult!=null) {
			String theOffset = aUri.getQueryParameter(QUERY_OFFSET);
			if (theOffset!=null) {
				theResult += ", "+theOffset;
			}
		}
		return theResult;
	}

	@Override
	public Cursor query(Uri aUri, String[] aProjection, String aSelection,
			String[] aSelectionArgs, String aSortOrder) {
		int theMatchId = mUriMatcher.match(aUri);
		if (TextUtils.isEmpty(aSortOrder)) {
			aSortOrder = getDefaultSortOrder(theMatchId);
		}
		String[] theCols = ((aProjection==null) ? getDefaultColumns(theMatchId) : aProjection);
		String theSelection = appendSelection(aUri,theMatchId,aSelection);
		String[] theSelectionArgs = appendSelArgs(aUri,theMatchId,aSelectionArgs);
		String theQueryLimit = getQueryLimitClause(aUri);
		Cursor theResult;
		theResult = mDb.getReadableDatabase().query(getTableName(theMatchId),
				theCols,theSelection,theSelectionArgs,null,null,aSortOrder,theQueryLimit);
		theResult.setNotificationUri(getContext().getContentResolver(),aUri);
		return theResult;
	}

	/**
	 * Insert may require certain fields be present in order to succeed.
	 * @param aMatchId - which table match ID being used.
	 * @return Returns an array of column names (may be empty).
	 */
	protected abstract String[] getRequiredColumns(int aMatchId);

	protected abstract void populateDefaultValues(int aMatchId, ContentValues aValues);

	protected abstract Uri getContentIdBaseUri(int aMatchId);

    protected Uri craftInsertResult(int aMatchId, ContentValues aValues, long aInserted_ID) {
        Uri theResultUriBase = getContentIdBaseUri(aMatchId);
        return ContentUris.withAppendedId(theResultUriBase,aInserted_ID);
    }

	protected void notifyInsert(int aMatchId, Uri aInsertResult) {
		getContext().getContentResolver().notifyChange(aInsertResult,null);
	}

	@Override
	public Uri insert(Uri aUri, ContentValues aValues) {
		int theMatchId = mUriMatcher.match(aUri);
		long theRowIdAdded = -1L;
		ContentValues theValues = (aValues!=null) ? aValues : new ContentValues();
		for (String theColumnName : getRequiredColumns(theMatchId)) {
			if (!theValues.containsKey(theColumnName)) {
				String s = (R_string_sql_content_provider_msg_column_missing!=0)
						? getContext().getString(R_string_sql_content_provider_msg_column_missing,theColumnName)
						: "Missing column: "+theColumnName;
				throw new IllegalArgumentException(s);
			}
		}
		populateDefaultValues(theMatchId,theValues);
		String theTableName = getTableName(theMatchId);
		theRowIdAdded = mDb.getWritableDatabase().insert(theTableName,null,theValues);
		if (theRowIdAdded > -1L) {
            Uri theResult = craftInsertResult(theMatchId,theValues,theRowIdAdded);
			notifyInsert(theMatchId,theResult);
			return theResult;
		} else {
			String s = (R_string_sql_content_provider_msg_insert_failed!=0)
					? getContext().getString(R_string_sql_content_provider_msg_insert_failed,aUri.toString())
					: "Failed to insert row into "+aUri.toString();
			throw new SQLException(s);
		}
	}

	protected void notifyDelete(int aMatchId, Uri aUri, int aNumDeleted) {
		getContext().getContentResolver().notifyChange(aUri,null);
	}

	@Override
	public int delete(Uri aUri, String aSelection, String[] aSelectionArgs) {
		int theMatchId = mUriMatcher.match(aUri);
		int theNumDel = 0;
		aSelection = appendSelection(aUri, theMatchId, aSelection);
		aSelectionArgs = appendSelArgs(aUri, theMatchId, aSelectionArgs);
		theNumDel = mDb.getWritableDatabase().delete(getTableName(theMatchId),aSelection,aSelectionArgs);
		if (theNumDel>0) {
			notifyDelete(theMatchId, aUri, theNumDel);
		}
		return theNumDel;
	}

	protected void notifyUpdate(int aMatchId, Uri aUri, int aNumUpdated) {
		getContext().getContentResolver().notifyChange(aUri,null);
	}

	@Override
	public int update(Uri aUri, ContentValues aValues, String aSelection, String[] aSelectionArgs) {
		int theMatchId = mUriMatcher.match(aUri);
		int theNumUpdated = 0;
		aSelection = appendSelection(aUri, theMatchId, aSelection);
		aSelectionArgs = appendSelArgs(aUri, theMatchId, aSelectionArgs);
		String theTableName = getTableName(theMatchId);
		theNumUpdated = mDb.getWritableDatabase().update(theTableName,aValues,aSelection,aSelectionArgs);
		if (theNumUpdated>0) {
			notifyUpdate(theMatchId,aUri,theNumUpdated);
		}
		return theNumUpdated;
	}


}
