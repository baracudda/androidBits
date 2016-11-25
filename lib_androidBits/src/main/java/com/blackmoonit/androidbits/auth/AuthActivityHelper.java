package com.blackmoonit.androidbits.auth;
/*
 * Copyright (C) 2016 Blackmoon Info Tech Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Helper class for implementing an Activity that is used to help implement an
 * AbstractAccountAuthenticator. If the AbstractAccountAuthenticator needs to use an activity
 * to handle the request then it can have the activity create this helper and use its events.
 * The AbstractAccountAuthenticator passes in the response to the intent using the following:
 * <pre>
 * intent.putExtra({@link AccountManager#KEY_ACCOUNT_AUTHENTICATOR_RESPONSE}, response);
 * </pre>
 * The activity then sets the result that is to be handed to the response via
 * {@link #setAccountAuthenticatorResult(android.os.Bundle)} which will call this helper's
 * method of the same name.
 * This result will be sent as the result of the request when the activity finishes. If this
 * is never set or if it is set to null then error {@link AccountManager#ERROR_CODE_CANCELED}
 * will be set as the response.
 */
public class AuthActivityHelper
{
	private final Context mContext;
	protected AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
	protected Bundle mResultBundle = null;

	public AuthActivityHelper(Context aContext) {
		mContext = aContext;
	}

	public Context getContext()
	{ return mContext; }

	/**
	 * Set the result that is to be sent as the result of the request that caused this
	 * Activity to be launched. If result is null or this method is never called then
	 * the request will be canceled.
	 * @param result this is returned as the result of the AbstractAccountAuthenticator request
	 */
	public void setAccountAuthenticatorResult(Bundle result) {
		mResultBundle = result;
	}

	public void onNewIntent(Intent aIntent) {
		if (mAccountAuthenticatorResponse==null) {
			mAccountAuthenticatorResponse =
					aIntent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
			if (mAccountAuthenticatorResponse != null) {
				mAccountAuthenticatorResponse.onRequestContinued();
			}
		}
	}

	/**
	 * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
	 */
	public void onFinish() {
		if (mAccountAuthenticatorResponse != null) {
			// send the result bundle back if set, otherwise send an error.
			if (mResultBundle != null) {
				mAccountAuthenticatorResponse.onResult(mResultBundle);
			} else {
				mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
			}
			mAccountAuthenticatorResponse = null;
		}
	}
}
