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

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.blackmoonit.androidbits.R;
import com.blackmoonit.androidbits.utils.BitsAppUtils;

/**
 * Android Account Manager descendant for working with Broadway Auth REST server.
 * Defined as a pseudo-abstract class due to ServiceAuthenticator::IBinder complaining if it
 * was truly abstract, override requestServerForAuthToken() method to use one of your own.
 */
@SuppressWarnings("unused")
public class ABroadwayAuthenticator extends AbstractAccountAuthenticator
{
	static private final String TAG = ABroadwayAuthenticator.class.getSimpleName();

	static public final String EXTRA_ACCOUNT_NAME = AccountManager.KEY_ACCOUNT_NAME;
	static public final String EXTRA_ACCOUNT_TYPE = AccountManager.KEY_ACCOUNT_TYPE;
	static public final String EXTRA_USER_TOKEN = AccountManager.KEY_PASSWORD;
	static public final String EXTRA_AUTH_TOKEN = AccountManager.KEY_AUTHTOKEN;
	static public final String EXTRA_AUTH_ID = BroadwayAuthAccount.USER_DATA_KEY_AUTH_ID;

	static public final String EXTRA_AUTHTOKEN_KIND = "authtoken_kind";
	static public final String EXTRA_USER_INPUT = "ticketholder";
	static public final String EXTRA_PW_INPUT = "crowd_cheer";
	static public final String EXTRA_EMAIL = "account_email";

	/**
	 * THIS MUST MATCH THE android:accountType ATTRIBUTE IN OUR authenticator_meta.xml!!!
	 * Trying to use the AccountManager will result in Bind Errors otherwise.
	 */
	static public final int RES_ACCOUNT_TYPE = R.string.account_auth_type;
	/**
	 * What kind of access will our auth token grant us? Entirely up to us.
	 */
	static public final String AUTHTOKEN_KIND_FULL_ACCESS = "FULL_ACCESS";

	/**
	 * Android Authenticator requires the Authenticator class to be a Locally Bound service.
	 */
	static public class ServiceAuthenticator extends Service {
		@Override
		public IBinder onBind(Intent intent) {
			return (new ABroadwayAuthenticator( getApplicationContext() )).getIBinder();
		}
	}

	protected final Context mContext;

	public ABroadwayAuthenticator(Context aContext) {
		super(aContext);
		mContext = aContext;
	}

	public Context getContext() {
		return mContext;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse aResponse, String aAccountType,
			String aAuthTokenType, String[] aRequiredFeatures, Bundle options)
		throws NetworkErrorException
	{
		Context theContext = getContext();
		final Intent theIntent = new Intent( theContext,
				BitsAppUtils.obtainClassForName( theContext,
						theContext.getString(R.string.class_for_activity_login),
						TAG
				)
		);
		theIntent.putExtra(EXTRA_ACCOUNT_TYPE, aAccountType);
		theIntent.putExtra(EXTRA_AUTHTOKEN_KIND, aAuthTokenType);
		theIntent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, aResponse);
		final Bundle theResult = new Bundle();
		theResult.putParcelable(AccountManager.KEY_INTENT, theIntent);
		return theResult;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType)
	{
		return null;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse aResponse,
			Account aAccount, Bundle options)
		throws NetworkErrorException
	{
		return null;
	}

	/**
	 * OVERRIDE ME IN YOUR DESCENDANT!
	 * Use this method to actually talk to the server to obtain an auth token.
	 * @param aAcctMgr - the Android Account Manager.
	 * @param aAccount - the account information.
	 * @return Returns the auth token on success, else NULL.
	 */
	protected String requestServerForAuthToken(AccountManager aAcctMgr,
			BroadwayAuthAccount aAccount)
	{
		/* EXAMPLE CODE
		String theUserToken = aAccount.getAcctUserToken(aAcctMgr);
		if (!TextUtils.isEmpty(theUserToken)) {
			Context theContext = getContext();
			AuthRestClient mAuthRestClient = new AuthRestClient(theContext, obtainAuthDeviceInfo());
			AuthRestAPI.MobileAuthTokenRequestByAndroid theRequest =
					new AuthRestAPI.MobileAuthTokenRequestByAndroid();
			theRequest.auth_id = aAccount.getAcctAuthId();
			theRequest.user_token = theUserToken;
			AuthRestAPI.MobileAuthTokenResponse theResponse =
					mAuthRestClient.requestMobileAuthByAndroid(theRequest);
			if (theResponse!=null) {
				return theResponse.auth_token;
			}
		}
		END EXAMPLE CODE */
		return null;
	}

	@SuppressWarnings("MissingPermission")
	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse aResponse, Account aAccount,
			String aAuthTokenKind, Bundle options) throws NetworkErrorException
	{
		Context theContext = getContext();
		//Extract the username and pw (user_token) from the Account Manager, and ask
		//  the server for an appropriate AuthToken.
		final AccountManager theAcctMgr = AccountManager.get(theContext);
		BroadwayAuthAccount theAcct = BroadwayAuthAccount.fromAcctMgr(theAcctMgr, aAccount.name,
				aAccount.type);
		String theAuthToken = theAcctMgr.peekAuthToken(aAccount, aAuthTokenKind);
		if (TextUtils.isEmpty(theAuthToken)) {
			theAuthToken = requestServerForAuthToken(theAcctMgr, theAcct);
		}
		//return the auth token!
		if (!TextUtils.isEmpty(theAuthToken)) {
			return theAcct.setAcctAuthToken(theAuthToken).toBundle();
		}

		//If we get here, then we need to re-prompt them for their credentials.
		//  We do that by creating an intent to display our authenticator login activity.
		final Intent theResultIntent = new Intent( theContext,
				BitsAppUtils.obtainClassForName( theContext,
						theContext.getString(R.string.class_for_activity_login),
						TAG
				)
		);
		theResultIntent.putExtra(EXTRA_ACCOUNT_NAME, theAcct.name);
		theResultIntent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, aResponse);
		theResultIntent.putExtra(EXTRA_ACCOUNT_TYPE, theAcct.type);
		theResultIntent.putExtra(EXTRA_AUTHTOKEN_KIND, AUTHTOKEN_KIND_FULL_ACCESS);
		final Bundle theResult = new Bundle();
		theResult.putParcelable(AccountManager.KEY_INTENT, theResultIntent);
		return theResult;
	}

	@Override
	public String getAuthTokenLabel(String aAuthTokenType)
	{
		return getContext().getString(R.string.account_auth_label_auth_token);
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse aResponse,
			Account aAccount, String aAuthTokenType, Bundle options)
		throws NetworkErrorException
	{
		/*
		Context theContext = getContext();
		final Intent theResultIntent = new Intent(theContext, ActivityAccountAuthLogin.class);
		theResultIntent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, aResponse);
		theResultIntent.putExtra(AccountAuthenticatorForFresnel.EXTRA_ACCOUNT_TYPE, aAccount.type);
		theResultIntent.putExtra(AccountAuthenticatorForFresnel.EXTRA_AUTHTOKEN_KIND,
				AccountAuthenticatorForFresnel.AUTHTOKEN_KIND_FULL_ACCESS);
		final Bundle theResult = new Bundle();
		theResult.putParcelable(AccountManager.KEY_INTENT, theResultIntent);
		return theResult;
		*/
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse aResponse,
			Account aAccount, String[] aFeatures)
		throws NetworkErrorException
	{
		return null;
	}

}
