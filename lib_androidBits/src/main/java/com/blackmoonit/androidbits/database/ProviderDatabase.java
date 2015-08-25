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
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * This class helps open, create, and upgrade the database file based on its ProviderContract.
 */
public class ProviderDatabase extends SQLiteOpenHelper {
	protected final ProviderContract.Database mDbContract;
	protected final WeakReference<Context> mContext;

	public ProviderDatabase(Context aContext, ProviderContract.Database aDbContract) {
		super(aContext, aDbContract.getDbInfo().getDbFilename(), null, aDbContract.getDbVersion());
		mDbContract = aDbContract;
		mContext = new WeakReference<Context>(aContext);
	}

	/**
	 * What contract is this database adhering to?
	 * @return Returns the ProviderContract.Database interface being used.
	 */
	public ProviderContract.Database getDbContract() {
		return mDbContract;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.w(getDbContract().getDbName()+" db", "Creating database");
		for (ProviderContract.Table theContractTable : getDbContract().getDbInfo().getTableList()) {
			theContractTable.getTableInfo().onCreate(db);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(getDbContract().getDbName()+" db", "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which may destroy all old data");
		for (ProviderContract.Table theContractTable : getDbContract().getDbInfo().getTableList()) {
			theContractTable.getTableInfo().onUpgrade(db, oldVersion, newVersion);
		}
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(getDbContract().getDbName() + " db", "Downgrading database from version " + oldVersion + " to "
				+ newVersion + ", which may destroy all old data");
		for (ProviderContract.Table theContractTable : getDbContract().getDbInfo().getTableList()) {
			theContractTable.getTableInfo().onDowngrade(db, oldVersion, newVersion);
		}
	}

	/**
	 * Clears out all data from each table, but does not drop the table.
	 * @param db - the database.
	 */
	public void emptyDb(SQLiteDatabase db) {
		for (ProviderContract.Table theContractTable : getDbContract().getDbInfo().getTableList()) {
			String theTableName = theContractTable.getTableInfo().getTableContract().getTableName();
			db.delete(theTableName, null, null);
		}
	}

	/**
	 * Helper method to safely close cursors.
	 * @param aCursor - a cursor that may be NULL.
	 */
	static public void closeCursor(Cursor aCursor) {
		if (aCursor!=null && !aCursor.isClosed())
			aCursor.close();
	}

	/**
	 * Delete record(s) given a Uri.
	 * @param aContext - the context to use.
	 * @param aUri - Uri of the record(s) to delete; usually a specific record.
	 * @return Returns the number of records removed.
	 */
	static public int removeUri(Context aContext, Uri aUri){
		ContentResolver theCR = aContext.getContentResolver();
		return theCR.delete(aUri, null, null);
	}

}
