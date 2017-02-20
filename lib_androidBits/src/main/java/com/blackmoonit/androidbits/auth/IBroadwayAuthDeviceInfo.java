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

public interface IBroadwayAuthDeviceInfo {
	/**
	 * Standard device information sent to the server to determine auth status.
	 * Non-volatile information that should not change between API calls.
	 * @return Returns the various data collected to present to the server.
	 */
	String[] getDeviceFingerprints();

	/**
	 * Volatile device information sent to the server to determine auth status.
	 * This covers information such as GPS location and timestamp.
	 * @return Returns the various data collected to present to the server.
	 */
	String[] getDeviceCircumstances();

	/**
	 * Composes the data to be used in an Authorization header string.
	 * @param aStrBldr - the StringBuilder to use, if NULL, a new one is created.
	 * @return The passed in StringBuilder with the data fields of an auth header.
     */
	StringBuilder composeBroadwayAuthData(StringBuilder aStrBldr);

}
