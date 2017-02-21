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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

/**
 * Account information used for HTTP Authentication, Broadway scheme.
 * The scheme was developed in order to provide authentication for even rooted devices.
 * Tokens are used by the server and stored in the account to map the account to the
 * device to a particular time-limited authentication token.  No passwords are stored
 * on the device and trust can be revoked by the server at any time.
 */
@SuppressWarnings("unused, MissingPermission")
public class BroadwayAuthAccount extends Account
{
	static private final String TAG = BroadwayAuthAccount.class.getSimpleName() ;
	static public final String USER_DATA_KEY_AUTH_ID = "key_auth_id";
	protected String mAuthId = null;
	protected String mAuthToken = null;

	protected BroadwayAuthAccount(String aAcctName, String aAcctType) {
		super(aAcctName, aAcctType);
	}

	/**
	 * If all the information is available, create the Android account
	 */
	static public BroadwayAuthAccount explicitlyCreateNewAccount( AccountManager aAcctMgr,
			String aAcctName, String aAcctType, String aAuthId, String aUserToken,
			String aAuthToken )
	{
		BroadwayAuthAccount theAccount = new BroadwayAuthAccount(aAcctName, aAcctType);
		if ( aAcctMgr!=null && aAcctMgr.addAccountExplicitly(theAccount, aUserToken, null) )
		{
			//auth_id
			aAcctMgr.setUserData(theAccount, USER_DATA_KEY_AUTH_ID, aAuthId);
			theAccount.setAcctAuthId(aAuthId);
			//auth_token
			aAcctMgr.setAuthToken(theAccount,
					ABroadwayAuthenticator.AUTHTOKEN_KIND_FULL_ACCESS,
					aAuthToken
			);
			theAccount.setAcctAuthToken(aAuthToken);
		}
		return theAccount;
	}

	/**
	 * Find the Android user account based on the username and type of account.
	 * @param aAcctMgr - Android's Account Manager.
	 * @param aAcctName - the account name.
	 * @param aAcctType - the type of the account.
	 * @return Returns the account object found inside Android's Account Manager.
	 */
	static public BroadwayAuthAccount fromAcctMgr(AccountManager aAcctMgr,
			String aAcctName, String aAcctType) {
		BroadwayAuthAccount theResult = new BroadwayAuthAccount(aAcctName, aAcctType);
		//load the other data, if available
		Account[] theAccounts = aAcctMgr.getAccountsByType(aAcctType);
		//getAccountsByType() never returns NULL
		for (Account a : theAccounts) {
			if (a.name.equals(aAcctName)) {
				theResult.obtainAcctAuthId(aAcctMgr);
				break;
			}
		}
		return theResult;
	}

	/**
	 * Create a new account object as a clone of the parameter.
	 * @param aAuthAccount - the object to copy.
	 * @return Returns the newly copied object.
	 */
	static public BroadwayAuthAccount fromAuthAccount(BroadwayAuthAccount aAuthAccount) {
		if (aAuthAccount!=null) {
			BroadwayAuthAccount theResult = new BroadwayAuthAccount(
					aAuthAccount.getAcctName(),
					aAuthAccount.getAcctType());
			theResult.mAuthId = aAuthAccount.getAcctAuthId();
			theResult.mAuthToken = aAuthAccount.getAcctAuthToken();
			return theResult;
		} else
			return null;
	}

	/**
	 * Android's AccountManager passes bundles of account data with specific keys.
	 * @param aAcctMgr - Android's Account Manager.
	 * @param aBundle - the bundle (usually intent extras) to unpack the account from.
	 * @return Returns a newly created account object set with data from the bundle.
	 */
	static public BroadwayAuthAccount fromBundle(AccountManager aAcctMgr, Bundle aBundle) {
		if ( aBundle!=null && !TextUtils.isEmpty(
				aBundle.getString(ABroadwayAuthenticator.EXTRA_ACCOUNT_NAME)) )
		{
			BroadwayAuthAccount theResult = fromAcctMgr(aAcctMgr,
					aBundle.getString(ABroadwayAuthenticator.EXTRA_ACCOUNT_NAME),
					aBundle.getString(ABroadwayAuthenticator.EXTRA_ACCOUNT_TYPE)
			);
			if (!TextUtils.isEmpty(aBundle.getString(AccountManager.KEY_AUTHTOKEN))) {
				theResult.mAuthToken = aBundle.getString(ABroadwayAuthenticator.EXTRA_AUTH_TOKEN);
			}
			return theResult;
		} else
			return null;
	}

	/**
	 * Android's AccountManager expected bundles of account data with specific keys.
	 * @param aBundle - the bundle (usually intent extras) to pack the account into. If NULL, a
	 *     new Bundle will be created and returned.
	 * @return Returns the bundle packed with the account data as the
	 *     Android Account Manager would expect.
	 */
	public Bundle toBundle(Bundle aBundle) {
		Bundle theResult = (aBundle!=null) ? aBundle : new Bundle();
		theResult.putString(ABroadwayAuthenticator.EXTRA_ACCOUNT_NAME, name);
		theResult.putString(ABroadwayAuthenticator.EXTRA_ACCOUNT_TYPE, type);
		if (getAcctAuthToken()!=null) {
			theResult.putString(ABroadwayAuthenticator.EXTRA_AUTH_TOKEN, getAcctAuthToken());
		}
		if (getAcctAuthId()!=null) {
			theResult.putString(ABroadwayAuthenticator.EXTRA_AUTH_ID, getAcctAuthId());
			//also put it into the UserData bundle Android's AccountManager expects.
			Bundle theUserDataBundle = new Bundle();
			theUserDataBundle.putString(ABroadwayAuthenticator.EXTRA_AUTH_ID,
					getAcctAuthId());
			theResult.putBundle(AccountManager.KEY_USERDATA, theUserDataBundle);
		}
		return theResult;
	}

	/**
	 * Android's AccountManager expected bundles of account data with specific keys.
	 * @return Returns a bundle packed with the account data as the
	 *     Android Account Manager would expect.
	 */
	public Bundle toBundle() {
		return toBundle(null);
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += (mAuthId!=null) ? " authID!=null" : " authID==null";
		s += (mAuthToken!=null) ? " authTkn!=null" : " authTkn==null";
		return s;
	}

	/**
	 * @return Return the account name.
	 */
	public String getAcctName() {
		return name;
	}

	/**
	 * @return Return the account type.
	 */
	public String getAcctType() {
		return type;
	}

	/**
	 * The account name cannot be used to auto-auth with the server since it is too easy to
	 * discover and spoof.  We store and use a token for that instead and since we do not
	 * store passwords for this account on the device, we consider this token to be "the password".
	 * @param aAcctMgr - Android's Account Manager.
	 * @return Ask the Android Account Manager for this account's password, which is the token.
	 */
	public String getAcctUserToken(AccountManager aAcctMgr) {
		return aAcctMgr.getPassword(this);
	}

	/**
	 * The account name cannot be used to auto-auth with the server since it is too easy to
	 * discover and spoof.  We store and use a token for that instead and since we do not
	 * store passwords for this account on the device, we consider this token to be "the password".
	 * @param aAcctMgr - Android's Account Manager.
	 * @param aUserToken - the account token.
	 * @return Returns THIS for chaining.
	 */
	public BroadwayAuthAccount setAcctUserToken(AccountManager aAcctMgr, String aUserToken) {
		if (aAcctMgr!=null)
			aAcctMgr.setPassword(this, aUserToken);
		return this;
	}

	/**
	 * @return Returns the AuthID the server has mapped to this account.
	 */
	public String getAcctAuthId() {
		return mAuthId;
	}

	/**
	 * Set the AuthID the server has mapped to this account.
	 * @param aAuthId - the auth ID which is an alias for this account's username.
	 * @return Returns THIS for chaining.
	 */
	public BroadwayAuthAccount setAcctAuthId(String aAuthId) {
		mAuthId = aAuthId;
		return this;
	}

	/**
	 * Retrieve the AuthID for this account from Android's Account Manager and cache it locally.
	 * @param aAcctMgr - Android's Account Manager.
	 * @return Returns THIS for chaining.
	 */
	public BroadwayAuthAccount obtainAcctAuthId(AccountManager aAcctMgr) {
		if (aAcctMgr!=null)
			return setAcctAuthId(aAcctMgr.getUserData(this, USER_DATA_KEY_AUTH_ID));
		else
			return this;
	}

	/**
	 * Clear the AuthID for this account from Android's Account Manager and the local cache.
	 * @param aAcctMgr - Android's Account Manager.
	 * @return Returns THIS for chaining.
	 */
	public BroadwayAuthAccount clearAcctAuthId(AccountManager aAcctMgr) {
		if (aAcctMgr!=null)
			aAcctMgr.setUserData(this, USER_DATA_KEY_AUTH_ID, null);
		return setAcctAuthId(null);
	}

	/**
	 * @return Return the locally cached auth token.
	 */
	public String getAcctAuthToken() {
		return mAuthToken;
	}

	/**
	 * Cache the auth token locally.
	 * @param aAcctAuthToken - the auth token.
	 * @return Returns THIS for chaining.
	 */
	public BroadwayAuthAccount setAcctAuthToken(String aAcctAuthToken) {
		mAuthToken = aAcctAuthToken;
		return this;
	}

	/**
	 * Is our account object considered authorized? We may still be rejected by the server
	 * if the auth token is stale.
	 * @return Returns TRUE if we have an auth token assigned as well as an AuthID.
	 */
	public boolean isAuthorized() {
		return ( !TextUtils.isEmpty(mAuthId) && !TextUtils.isEmpty(mAuthToken) );
	}

	/**
	 * Clear out enough account data to prevent auto-auth since we're manually logging out.
	 * @param aAcctMgr - Android's Account Manager.
	 */
	public BroadwayAuthAccount logout(AccountManager aAcctMgr)
	{
		if( aAcctMgr != null ) {
			aAcctMgr.invalidateAuthToken( getAcctType(), getAcctAuthToken() );
			setAcctAuthToken( null ) ;
		}
		else
			Log.w( TAG, "Skipped invalidating token; no account manager available." ) ;
		return setAcctUserToken( aAcctMgr, null );
	}

	/**
	 * Generates a Base64-encoded authorization header value based on the
	 * current values of the StringBuilder param.
	 * @param aStrBldr - the built-up string to be Base64-encoded.
	 * @return an HTTP Authorization header value
	 */
	static public String composeAuthorizationHeaderValue(StringBuilder aStrBldr)
	{
		if (aStrBldr==null) return "";
		// Note: The .trim() here is a workaround for an acknowledged bug
		// in the Base64.encodeToString() function, which can unpredictably
		// append spurious whitespace characters to the end of the encoded
		// return value.
		return (new StringBuilder()).append( "Broadway " )
			.append( Base64.encodeToString( aStrBldr.toString().getBytes(),
				Base64.NO_WRAP).trim() ).toString() ;
	}

	/**
	 * Do we have enough information in this Account object to attempt an
	 * automatic re-authorization from the server?
	 * @param aAcctMgr - Android's Account Manager.
	 * @return Returns TRUE if we have enough info to attempt a re-auth.
	 */
	public boolean canAutoAuth(AccountManager aAcctMgr)
	{
		return !TextUtils.isEmpty(this.getAcctAuthId()) &&
				aAcctMgr!=null &&
				!TextUtils.isEmpty(this.getAcctUserToken(aAcctMgr));
	}

}
