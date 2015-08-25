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

import com.blackmoonit.androidbits.content.SqlContractProvider;

abstract public class ProviderService extends SqlContractProvider {

	/* mDb may not be defined yet, so force descendant to provide the static result.
	@Override
	protected ProviderContract.Database getDbContract() {
		return ((ProviderDatabase)mDb).getDbContract();
	}
	*/

}
