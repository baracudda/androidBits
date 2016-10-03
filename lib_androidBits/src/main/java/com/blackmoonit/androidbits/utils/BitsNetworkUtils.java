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

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    /**
     * Returns device's true Wi-Fi MAC address, even on those running
     * Android API 23.
     * <p/>
     * This method makes calls that require
     * android.Manifest.permission.ACCESS_WIFI_STATE.
     * @param ctx Context Calling context.
     * @return String Device's Wi-Fi MAC address, or an empty string when
     * Wi-Fi is not supported on this device.
     */
    public static String getWifiMacAddress( Context ctx )
    {
        String detectedWifiAddress = "";
        final WifiManager wifiManager = ( ( WifiManager ) ctx.getSystemService( Context.WIFI_SERVICE ) );
        if( wifiManager != null )
            switch( wifiManager.getWifiState() )
            {
                case WifiManager.WIFI_STATE_ENABLED:
                case WifiManager.WIFI_STATE_DISABLING:
                    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if ( wifiInfo != null )
                    {
                        if( Build.VERSION.SDK_INT < 23 )
                            detectedWifiAddress = wifiInfo.getMacAddress();
                        else
                        {
                            try
                            {
                                List<NetworkInterface> availableNetworkInterfaces =
                                        Collections.list( NetworkInterface.getNetworkInterfaces() );
                                if ( availableNetworkInterfaces != null )
                                {
                                    NetworkInterface detectedWifiNetInterface = null;
                                    for ( NetworkInterface thisNetworkInterface : availableNetworkInterfaces )
                                    {
                                        if ( thisNetworkInterface.getName().equalsIgnoreCase( "wlan0" ) )
                                        {
                                            detectedWifiNetInterface = thisNetworkInterface;
                                            break;
                                        }
                                    }
                                    if ( detectedWifiNetInterface != null )
                                    {
                                        byte[] wifiHardwareAddressByteArray = detectedWifiNetInterface.getHardwareAddress();
                                        if ( wifiHardwareAddressByteArray != null )
                                        {
                                            StringBuilder sb = new StringBuilder();
                                            for ( byte thisByte : wifiHardwareAddressByteArray )
                                            {   // Apply MAC address formatting.
                                                sb.append( String.format( "%02X:", thisByte ) );
                                            }

                                            // Remove extraneous trailing colon.
                                            if ( sb.length() > 0 )
                                                sb.deleteCharAt( sb.length() - 1 );
                                            detectedWifiAddress = sb.toString();
                                        }
                                    }
                                }
                            }
                            catch( SocketException e ) {
                                // leave as default value.
                            }
                        }
                    }
                    break;
                default:
            }
        return detectedWifiAddress != null ? detectedWifiAddress : "";
    }

    /**
     * Returns device's true Bluetooth MAC address, even on those running
     * Android API 23.
     * <p/>
     * This method makes calls that require
     * android.Manifest.permission.BLUETOOTH.
     * @param ctx Context Calling context.
     * @return String Device's Bluetooth MAC address, or an empty string when
     * Bluetooth is not supported on this device.
     */
    public static String getBluetoothAdapterMacAddress( Context ctx )
    {
        String detectedMacAddress = "";
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if( bluetoothAdapter != null )
            if( Build.VERSION.SDK_INT < 23 )
                detectedMacAddress = bluetoothAdapter.getAddress();
            else
                detectedMacAddress = Settings.Secure.getString(
                        ctx.getContentResolver(),
                        "bluetooth_address" );
        return detectedMacAddress != null ? detectedMacAddress : "";
    }

    /**
     * Returns the list of access points found in the most recent scan detected
     * by device, even on those running Android API 23. Location must be enabled
     * on device.
     * <p/>
     * This method makes calls that require
     * android.Manifest.permission.ACCESS_WIFI_STATE.
     * <p/>
     * In addition, calling application must have either
     * android.Manifest.permission.ACCESS_COARSE_LOCATION or
     * android.Manifest.permission.ACCESS_FINE_LOCATION in order to get valid
     * results. Without one of these two permissions, and Location enabled on
     * device, this method will return an empty list.
     * @param ctx Context Calling context.
     * @return List<ScanResult> with found results, or empty list if needed
     * permission(s) and location not enabled.
     */
    public static List<ScanResult> getWifiScanResults( Context ctx )
    {
        List<ScanResult> wifiScanResults = new ArrayList<ScanResult>();
        final WifiManager wifiManager = ( ( WifiManager ) ctx.getSystemService( Context.WIFI_SERVICE ) );
        if( wifiManager != null )
            switch( wifiManager.getWifiState() )
            {
                case WifiManager.WIFI_STATE_ENABLED:
                case WifiManager.WIFI_STATE_DISABLING:
                    boolean canGatherScanResults = false;
                    if ( Build.VERSION.SDK_INT < 23 )
                        canGatherScanResults = true;
                    else {
                        try
                        { // For Android 6+, location service must be enabled to get WiFi scans.
                            int zLocationMode = Settings.Secure.getInt(
                                    ctx.getContentResolver(),
                                    Settings.Secure.LOCATION_MODE );
                            canGatherScanResults = ( zLocationMode != Settings.Secure.LOCATION_MODE_OFF );
                        }
                        catch( Settings.SettingNotFoundException e )
                        {
                            // leave default value.
                        }
                    }
                    if( canGatherScanResults )
                    {
                        final List<ScanResult> aoScannedNetworks = wifiManager.getScanResults();
                        if( aoScannedNetworks != null )
                        {
                            for( ScanResult info : aoScannedNetworks )
                                wifiScanResults.add(info);
                        }
                    } else {
                        // leave default value.
                    }
                    break;
                default:
                    // leave default value.
            }
        return wifiScanResults != null ? wifiScanResults : new ArrayList<ScanResult>();
    }
}
