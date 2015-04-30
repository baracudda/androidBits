package com.blackmoonit.androidbits.content;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.net.Uri;

import com.blackmoonit.androidbits.database.ProviderContract;
import com.blackmoonit.androidbits.database.ProviderContract.Database;
import com.blackmoonit.androidbits.database.ProviderContract.DbProviderInfo;
import com.blackmoonit.androidbits.database.ProviderContract.TableProviderInfo;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link com.blackmoonit.androidbits.database.ProviderContract ProviderContract}
 * class provides a mechanism for the provider
 * to query the contract itself for various simple tasks, especially Uri
 * and MIME types required by the provider and can be automated (less
 * developer attention required). Another compelling reason to use a
 * SqlContractProvider is that ContentObservers have to option to listen
 * for specific data actions (insert/update/delete) rather than just
 * any kind of change.
 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_INSERT
 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_UPDATE
 * @see com.blackmoonit.androidbits.database.ProviderContract.Database#DATA_ACTION_DELETE
 * @see com.blackmoonit.androidbits.database.ProviderContract.TableProviderInfo#getObserverUri(String)
 * @see com.blackmoonit.androidbits.database.ProviderContract.TableProviderInfo#ensureContentUri(android.net.Uri)
 * @see com.blackmoonit.androidbits.database.ProviderContract.DbProviderInfo#ensureContentUri(android.net.Uri)
 *
 * @author Ryan Fischbach
 */
@SuppressLint("UseSparseArrays")
abstract public class SqlContractProvider extends SqlContentProvider {
	/**
	 * What contract is this provider adhering to?
	 * @return Returns the ProviderContract.Database interface being used.
	 */
	abstract protected ProviderContract.Database getDbContract();

	/**
	 * Contains a map of integer Match Codes to TableProviderInfo for all
	 * tables the provider will respond to with set results.
	 */
	protected final ConcurrentHashMap<Integer, TableProviderInfo> mTableInfoMapSets =
			new ConcurrentHashMap<Integer, TableProviderInfo>();
	/**
	 * Contains a map of integer Match Codes to TableProviderInfo for all
	 * tables the provider will respond to with single row results.
	 */
	protected final ConcurrentHashMap<Integer, TableProviderInfo> mTableInfoMapRows =
			new ConcurrentHashMap<Integer, TableProviderInfo>();

	/**
	 * Fill the {@link #mTableInfoMapSets} variable.
	 */
	protected void fillTableInfoMapSets() {
		ArrayList<ProviderContract.Table> theTables = getDbContract().getDbInfo().getTableList();
		for (int i = 0; i < theTables.size(); i++) {
			ProviderContract.Table theContractTable = theTables.get(i);
			mTableInfoMapSets.put(i+1, theContractTable.getTableInfo());
		}
	}

	/**
	 * Fill the {@link #mTableInfoMapRows} variable.
	 */
	protected void fillTableInfoMapRows() {
		ArrayList<ProviderContract.Table> theTables = getDbContract().getDbInfo().getTableList();
		for (int i = 0; i < theTables.size(); i++) {
			ProviderContract.Table theContractTable = theTables.get(i);
			mTableInfoMapRows.put(i+1+theTables.size()+1, theContractTable.getTableInfo());
		}
	}

	@Override
	public boolean onCreate() {
		fillTableInfoMapSets();
		fillTableInfoMapRows();
		return super.onCreate();
	}

	/**
	 * Gets the TableProviderInfo from either mTableInfoMapSets or mTableInfoMapRows,
	 * whichever one holds the Match ID parameter. Use this function when you do not
	 * care which variable the info came from.
	 * @param aMatchId - the match code integer returned by the UriMatcher.
	 * @return Returns the TableProviderInfo if found, else returns NULL.
	 */
	protected TableProviderInfo getTableInfo(Integer aMatchId) {
		// Note, written with two different local vars so that multi-core processors
		// do not have to execute the code in sequence, parallel would be nice.
		TableProviderInfo theTableInfoSet = mTableInfoMapSets.get(aMatchId);
		if (theTableInfoSet!=null) {
			return theTableInfoSet;
		}
		TableProviderInfo theTableInfoRow = mTableInfoMapRows.get(aMatchId);
		if (theTableInfoRow!=null) {
			return theTableInfoRow;
		}
		return null;
	}

	@Override
	protected UriMatcher buildUriMatcher() {
		// Note, written with different local vars so that multi-core processors
		// do not have to execute the code in sequence, parallel would be nice.
		UriMatcher theMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        for (Map.Entry<Integer, TableProviderInfo> theEntry : mTableInfoMapSets.entrySet()) {
            theEntry.getValue().addTableSetUri(theMatcher, theEntry.getKey());
        }
        for (Map.Entry<Integer, TableProviderInfo> theEntry : mTableInfoMapRows.entrySet()) {
            theEntry.getValue().addTableRowUri(theMatcher, theEntry.getKey());
        }
		return theMatcher;
	}

	@Override
	public String getType(Uri aUri) {
		// Note, written with different local vars so that multi-core processors
		// do not have to execute the code in sequence, parallel would be nice.
		Integer theMatchId = mUriMatcher.match(aUri);
		TableProviderInfo theTableInfoSet = mTableInfoMapSets.get(theMatchId);
		if (theTableInfoSet!=null) {
			return theTableInfoSet.getMIMEtypeForResultSet();
		}
		TableProviderInfo theTableInfoRow = mTableInfoMapRows.get(theMatchId);
		if (theTableInfoRow!=null) {
			return theTableInfoRow.getMIMEtypeForSingularResult();
		}
		return "";
	}

	@Override
	protected String getTableName(int aMatchId) {
		TableProviderInfo theTableInfo = getTableInfo(aMatchId);
		if (theTableInfo!=null) {
			return theTableInfo.getTableContract().getTableName();
		} else {
			throw new UnsupportedOperationException();
		}
	}

    @Override
    protected String[] getDefaultColumns(int aMatchId) {
        TableProviderInfo theTableInfo = getTableInfo(aMatchId);
        if (theTableInfo!=null) {
            ArrayList<String> theColNames = theTableInfo.getColNamesFromMyContract();
            return theColNames.toArray(new String[theColNames.size()]);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
	protected String getDefaultSortOrder(int aMatchId) {
		TableProviderInfo theTableInfo = getTableInfo(aMatchId);
		if (theTableInfo!=null) {
			return theTableInfo.getTableContract().getDefaultSortOrder();
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	protected String[] getRequiredColumns(int aMatchId) {
		TableProviderInfo theTableInfo = getTableInfo(aMatchId);
		if (theTableInfo!=null) {
			return theTableInfo.getTableContract().getRequiredColumns();
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	protected void populateDefaultValues(int aMatchId, ContentValues aValues) {
		TableProviderInfo theTableInfo = getTableInfo(aMatchId);
		if (theTableInfo!=null) {
			theTableInfo.getTableContract().populateDefaultValues(aValues);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	protected String appendSelection(Uri aUri, int aMatchId, String aSelection) {
		TableProviderInfo theTableInfoRow = mTableInfoMapRows.get(aMatchId);
		if (theTableInfoRow!=null) {
			return appendToSelection(aSelection,
					theTableInfoRow.getTableContract().getIdFieldName()+"=?");
		} else {
			return aSelection;
		}
	}

	@Override
	protected String[] appendSelArgs(Uri aUri, int aMatchId, String[] aSelectionArgs) {
		TableProviderInfo theTableInfoRow = mTableInfoMapRows.get(aMatchId);
		if (theTableInfoRow!=null) {
			return appendToSelArgs(aSelectionArgs,aUri.getPathSegments().get(
					theTableInfoRow.getUriPathIdPosition()));
		} else {
			return aSelectionArgs;
		}
	}

	@Override
	protected Uri getContentIdBaseUri(int aMatchId) {
		TableProviderInfo theTableInfoSet = mTableInfoMapSets.get(aMatchId);
		if (theTableInfoSet!=null) {
			return theTableInfoSet.getContentUri(null);
		} else {
			throw new UnsupportedOperationException();
		}
	}

    @Override
    protected Uri craftInsertResult(int aMatchId, ContentValues aValues, long aInserted_ID) {
        TableProviderInfo theTableInfoSet = mTableInfoMapSets.get(aMatchId);
        if (theTableInfoSet.getTableContract().getIdFieldName().equals(theTableInfoSet.getTableContract()._ID)) {
            return super.craftInsertResult(aMatchId,aValues,aInserted_ID);
        } else {
            return theTableInfoSet.getContentUri(
                    aValues.getAsString(theTableInfoSet.getTableContract().getIdFieldName())
            );
        }
    }

    @Override
	protected void notifyInsert(int aMatchId, Uri aInsertResult) {
		// Note, written with different local vars so that multi-core processors
		// do not have to execute the code in sequence, parallel would be nice.
		super.notifyInsert(aMatchId, aInsertResult);
		Uri theObserverUri = TableProviderInfo.cnvContentUriToObserverUri(aInsertResult,Database.DATA_ACTION_INSERT);
		getContext().getContentResolver().notifyChange(theObserverUri,null);
	}

	@Override
	protected void notifyDelete(int aMatchId, Uri aUri, int aNumDeleted) {
		// Note, written with different local vars so that multi-core processors
		// do not have to execute the code in sequence, parallel would be nice.
		super.notifyDelete(aMatchId, aUri, aNumDeleted);
		Uri theObserverUri = TableProviderInfo.cnvContentUriToObserverUri(aUri,Database.DATA_ACTION_DELETE);
		getContext().getContentResolver().notifyChange(theObserverUri,null);
	}

	@Override
	protected void notifyUpdate(int aMatchId, Uri aUri, int aNumUpdated) {
		// Note, written with different local vars so that multi-core processors
		// do not have to execute the code in sequence, parallel would be nice.
		super.notifyUpdate(aMatchId, aUri, aNumUpdated);
		Uri theObserverUri = TableProviderInfo.cnvContentUriToObserverUri(aUri,Database.DATA_ACTION_UPDATE);
		getContext().getContentResolver().notifyChange(theObserverUri,null);
	}

}
