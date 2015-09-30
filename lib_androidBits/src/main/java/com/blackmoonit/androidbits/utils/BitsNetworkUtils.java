package com.blackmoonit.androidbits.utils;
/*
 * Copyright (C) 2015 Blackmoon Info Tech Services
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

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

/**
 * This class provides utility methods for interacting with various network
 * connectivity modes on an Android device. As a static class,
 * {@code BitsNetworkUtils} cannot be instantiated.
 */
public class BitsNetworkUtils
{
    /**
     * Magic value used by {@link android.net.wifi.WifiInfo} to indicate that no
     * wifi network ID is available. Since that class does not provide a
     * semantic constant for this magic value, we provide one here.
     */
    public static final int NETWORK_ID_UNAVAILABLE = -1 ;

    /** Class should not be instantiated. */
    private BitsNetworkUtils() {}

    /**
     * Indicates whether either wifi or mobile data is enabled.
     * @param ctx a context from which to obtain connectivity information
     * @return true if either wifi or mobile data is enabled
     */
    public static boolean isAnyDataEnabled( Context ctx )
    {
        return( isWifiEnabled(ctx) || isMobileDataEnabled(ctx) ) ;
    }

    /**
     * Indicates whether wifi communication is enabled.
     * Invokes {@link #isWifiEnabled(android.net.wifi.WifiManager)} with the
     * manager instance from the given context.
     * @param ctx a context from which to obtain connectivity information
     * @return true if wifi is enabled
     */
    public static boolean isWifiEnabled( Context ctx )
    {
        if( ctx == null ) return false ;
        WifiManager wifi = (WifiManager)
            (ctx.getSystemService(Context.WIFI_SERVICE)) ;
        return isWifiEnabled(wifi) ;
    }

    /**
     * Indicates whether wifi communication is enabled.
     * The algorithm returns true only if all of the following conditions apply:
     * <ul>
     *     <li>An non-null instance of {@link android.net.wifi.WifiInfo} can be
     *         obtained from the wifi manager.</li>
     *     <li>The network ID reported by this instance is not {@code -1}.</li>
     *     <li>The "supplicant state" is {@code COMPLETED}, indicating that a
     *         connection was successfully established on that network.</li>
     *     <li>A non-null instance of {@link android.net.DhcpInfo} can be
     *         obtained based on the wifi info instance.</li>
     *     <li>The IP address in that DHCP info instance matches what is
     *         recorded in the wifi info instance.</li>
     * </ul>
     * @param wifi Wifi network information for the current context.
     * @return true if wifi is enabled
     */
    public static boolean isWifiEnabled( WifiManager wifi )
    {
        if( wifi == null ) return false ;
        WifiInfo winfo = wifi.getConnectionInfo() ;
        if( winfo == null ) return false ;
        if( winfo.getNetworkId() == NETWORK_ID_UNAVAILABLE ) return false ;
        if( winfo.getSupplicantState() != SupplicantState.COMPLETED )
            return false ;
        DhcpInfo dhcp = wifi.getDhcpInfo() ;
        if( dhcp == null ) return false ;
        if( dhcp.ipAddress != winfo.getIpAddress() ) return false ;
        return true ;
    }

    /**
     * Indicates whether mobile data connection is enabled.
     * Invokes {@link #isMobileDataEnabled(android.telephony.TelephonyManager)}
     * with the manager instance from the given context.
     * @param ctx a context from which to obtain connectivity information
     * @return true if mobile data is enabled
     */
    public static boolean isMobileDataEnabled( Context ctx )
    {
        if( ctx == null ) return false ;
        TelephonyManager tmgr = (TelephonyManager)
            (ctx.getSystemService(Context.TELEPHONY_SERVICE)) ;
        return isMobileDataEnabled(tmgr) ;
    }

    /**
     * Indicates whether mobile data connection is enabled.
     * Examines the value of
     * {@link android.telephony.TelephonyManager#getDataState()} to determine
     * whether the phone is connected to a data network.
     * @param tmgr telephony information for the current context
     * @return true if mobile data is enabled
     */
    public static boolean isMobileDataEnabled( TelephonyManager tmgr )
    {
        if( tmgr == null ) return false ;
        return( tmgr.getDataState() == TelephonyManager.DATA_CONNECTED ) ;
    }
}
