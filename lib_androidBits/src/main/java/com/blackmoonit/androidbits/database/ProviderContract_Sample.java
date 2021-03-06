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

import android.content.ContentValues;
import android.content.Context;

import com.blackmoonit.androidbits.database.ProviderContract.DbProviderInfo;
import com.blackmoonit.androidbits.database.ProviderContract.TableProviderInfo;

import java.util.UUID;

/**
 * DO NOT USE!!!
 * Provided as an example for future projects.
 *
 * @author baracudda
 */
public final class ProviderContract_Sample extends ProviderContract implements ProviderContract.Database {
	static public ProviderContract_Sample mDbContract = new ProviderContract_Sample();
	static {
		mDbInfo = new DbProviderInfo(mDbContract);
	}
	private ProviderContract_Sample() {}

	@Override
	public DbProviderInfo getDbInfo() {
		return mDbInfo;
	}

	@Override
	public String getDbName() {
		return "SampleDatabase";
	}

	@Override
	public int getDbVersion() {
		return 1;
	}

	static public final class SampleTable1 implements ProviderContract.Table {
		static public SampleTable1 mTableContract = new SampleTable1();
		//use "mTableInfo" so that the DbProviderInfo.getTableList() will function correctly
		static public TableProviderInfo mTableInfo = new TableProviderInfo(mDbContract, mTableContract);

		private SampleTable1() {}

		@Override
		public TableProviderInfo getTableInfo() {
			return mTableInfo;
		}

		@Override
		public String getTableName() {
			return "MyTable1";
		}

		@Override
		public String getIdFieldName() {
			return _ID;
		}

        @Override
        public String getUriMatcherIdWildcardChar() { return "#"; }

		/*
		 * ===== Column definitions =====
		 */

		/**
		 * Desc: UUID for the record.
		 * Type: TEXT
		 */
		static public final String COL_ITEM_ID = "item_id";

		/**
		 * Desc: Rank value.
		 * Type: INTEGER
		 */
		static public final String COL_ITEM_RANK = "rank_num";

		@Override
		public String getDefaultSortOrder() {
			return COL_ITEM_RANK+SORT_HiLo;
		}

		/**
		 * The data provider needs to know what columns are required for insert.
		 * Default is none, override and supply column names of required fields.
		 * @return Returns the array of required column names or an empty array if none.
		 */
		@Override
		public String[] getRequiredColumns() {
			return new String[] {
					COL_ITEM_RANK,
			};
		}

		/**
		 * The data provider needs to know if any columns have default values.
		 * Act upon the values parameter to add/modify any missing content.
		 * @param aContext - context in case string resources are needed.
		 * @param values - Add or modify existing values to enforce default data.
		 */
		@Override
		public void populateDefaultValues(Context aContext, ContentValues values) {
			if (!values.containsKey(COL_ITEM_ID))
				values.put(COL_ITEM_ID, UUID.randomUUID().toString());
		}

		/**
		 * May as well define the SQLite table here as well and keep things simple and contained.
		 * @return Returns the SQL string to be executed to create the SQLite table.
		 */
		@Override
		public String getCreateTableSQL() {
			return "CREATE TABLE IF NOT EXISTS " + getTableName()
				+" ("+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" //Android only (for ListAdapters)
				+", "+ COL_ITEM_ID + " TEXT"
				+", "+ COL_ITEM_RANK + " INTEGER"
				+ ");";
		}

		@Override
		public ProviderContract.RowVar newRowVar() {
			return null;
		}

	}


}
