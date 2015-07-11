package com.blackmoonit.androidbits.database;

import com.blackmoonit.androidbits.content.SqlContractProvider;

abstract public class ProviderService extends SqlContractProvider {

	@Override
	protected ProviderContract.Database getDbContract() {
		return ((ProviderDatabase)mDb).getDbContract();
	}

}
