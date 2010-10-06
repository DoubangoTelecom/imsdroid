/*
* Copyright (C) 2010 Mamadou Diop.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango.org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*
*/
package org.doubango.imsdroid.Services.Impl;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.INetworkService;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class NetworkService  extends Service implements INetworkService {

	private static final String TAG = NetworkService.class.getCanonicalName();
	
	private WifiManager wifiManager;
	private WifiLock wifiLock;
	private boolean acquired;
	
	// Will be added in froyo SDK
	private static int ConnectivityManager_TYPE_WIMAX = 6;
	
	public static enum DNS_TYPE {
		DNS_1, DNS_2, DNS_3, DNS_4
	}
	
	public NetworkService(){
		super();
	}
	
	@Override
	public boolean start() {
		this.wifiManager = (WifiManager) IMSDroid.getContext().getSystemService(Context.WIFI_SERVICE);
		return true;
	}

	@Override
	public boolean stop() {
		this.release();
		return true;
	}
	
	@Override
	public String getDnsServer(DNS_TYPE type) {
		String dns = null;
		switch (type) {
		case DNS_1:
		default:
			dns = "dns1";
			break;
		case DNS_2:
			dns = "dns2";
			break;
		case DNS_3:
			dns = "dns3";
			break;
		case DNS_4:
			dns = "dns4";
			break;
		}
		
		if(this.wifiManager != null){
			String[] dhcpInfos = this.wifiManager.getDhcpInfo().toString().split(" ");
			int i = 0;
			
			while (i++ < dhcpInfos.length) {
			  if (dhcpInfos[i-1].equals(dns)) {
				  return dhcpInfos[i];
			  }
			}
		}
		return null;
	}
	@Override
	public String getLocalIP(boolean ipv6) {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                   return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        Log.e(NetworkService.TAG, ex.toString());
	    }
	    
	    return null;
	}
	
	@Override
	public boolean acquire(){
		if(this.acquired){
			return true;
		}
		
		boolean connected = false;
		
		 ConnectivityManager connectivityManager = (ConnectivityManager) IMSDroid.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		 NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		 
		 if(networkInfo == null){
			 Toast.makeText(IMSDroid.getContext(), "Failed to get Network information", Toast.LENGTH_LONG).show();
			 Log.d(NetworkService.TAG, "Failed to get Network information");
			 return false;
		 }
		 
		 int netType = networkInfo.getType();
		 int netSubType = networkInfo.getSubtype();
		 
		 Log.d(NetworkService.TAG, String.format("netType=%d and netSubType=%d", netType, netSubType));
		 
		 boolean useWifi = ServiceManager.getConfigurationService().getBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.WIFI, Configuration.DEFAULT_WIFI);
		 boolean use3G = ServiceManager.getConfigurationService().getBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.THREE_3G, Configuration.DEFAULT_3G);
		
		if(useWifi && (netType == ConnectivityManager.TYPE_WIFI)){
			if(this.wifiManager.isWifiEnabled()){
				this.wifiLock = this.wifiManager.createWifiLock(NetworkService.TAG);
				final WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
				if(wifiInfo != null && this.wifiLock != null){
					final DetailedState detailedState = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
					if(detailedState == DetailedState.CONNECTED 
							|| detailedState == DetailedState.CONNECTING || detailedState == DetailedState.OBTAINING_IPADDR){
						this.wifiLock.acquire();
						connected = true;
					}
				}
			}
			else{
				Toast.makeText(IMSDroid.getContext(), "WiFi not enabled", Toast.LENGTH_LONG).show();
				Log.d(NetworkService.TAG, "WiFi not enabled");
			}
		}
		else if(use3G && (netType == ConnectivityManager.TYPE_MOBILE || netSubType == ConnectivityManager_TYPE_WIMAX)){
			if(		(netSubType >= TelephonyManager.NETWORK_TYPE_UMTS) || // HACK
				    (netSubType == TelephonyManager.NETWORK_TYPE_GPRS) ||
				    (netSubType == TelephonyManager.NETWORK_TYPE_EDGE)
				    ){
				Toast.makeText(IMSDroid.getContext(), "Using 3G/4G/2.5G network", Toast.LENGTH_SHORT).show();
				connected = true;
			}
		}

		if(!connected){
			Toast.makeText(IMSDroid.getContext(), "No active network", Toast.LENGTH_LONG).show();
			Log.d(NetworkService.TAG, "No active network");
			return false;
		}
		
		this.acquired = true;
		return true;
	}
	
	@Override
	public boolean release(){
		/*if(!this.acquired){
			return true;
		}*/
		
		/* wifi */
		if(this.wifiLock != null && this.wifiLock.isHeld()){
			this.wifiLock.release();
		}
		
		this.acquired = false;
		return true;
	}
}
