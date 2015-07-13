package com.blackmoonit.androidbits.database;

import com.blackmoonit.androidbits.content.SqlContractProvider;

abstract public class ProviderService extends SqlContractProvider {

	/* mDb may not be defined yet, so force descendant to provide the static result.
	@Override
	protected ProviderContract.Database getDbContract() {
		return ((ProviderDatabase)mDb).getDbContract();
	}
	*/

}
