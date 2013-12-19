package com.blackmoonit.database;

import com.blackmoonit.database.ProviderContract.DbProviderInfo;
import com.blackmoonit.database.ProviderContract.TableProviderInfo;

/**
 * DO NOT USE!!!
 * Provided as an example for future projects.
 *
 * @author Ryan Fischbach
 */
public final class ProviderContract_Sample implements ProviderContract.Database {
	static public ProviderContract_Sample mDbContract = new ProviderContract_Sample();
	static public DbProviderInfo mDbInfo = new DbProviderInfo(mDbContract);
	private ProviderContract_Sample() {}
	
	@Override
	public ProviderContract.DbProviderInfo getDbInfo() {
		return mDbInfo;
	}

	@Override
	public String getDbName() {
		return "SampleDatabase";
	}

	static public final class SampleTable1 implements ProviderContract.Table {
		static public SampleTable1 mTableContract = new SampleTable1();
		static public TableProviderInfo mTableInfo = 
				new TableProviderInfo(mDbContract, mTableContract);

		private SampleTable1() {}
		
		@Override
		public ProviderContract.TableProviderInfo getTableInfo() {
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

	}


}
