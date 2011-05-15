/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
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
*/
package org.doubango.ngn.services.impl;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.model.NgnAccessPoint;
import org.doubango.ngn.services.INgnNetworkService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnObservableList;
import org.doubango.ngn.utils.NgnStringUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager.WifiLock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**@page NgnNetworkService_page Network Service
 * The network service is used to manage both WiFi and 3g/4g network connections.
 */

/**
 * Network service.
 */
public class NgnNetworkService  extends NgnBaseService implements INgnNetworkService {
	private static final String TAG = NgnNetworkService.class.getCanonicalName();
	private static final String OPENVPN_INTERFACE_NAME = "tun0";
	
	private WifiManager mWifiManager;
	private WifiLock mWifiLock;
	private String mConnetedSSID;
	private boolean mAcquired;
	private boolean mStarted;
	private boolean mScanning;
	private final NgnObservableList<NgnAccessPoint> mAccessPoints;
	private BroadcastReceiver mNetworkWatcher;
	
	public static final int[] sWifiSignalValues = new int[] {
        0,
        1,
        2,
        3,
        4
    };
	
	// Will be added in froyo SDK
	private static int ConnectivityManager_TYPE_WIMAX = 6;
	
	public static enum DNS_TYPE {
		DNS_1, DNS_2, DNS_3, DNS_4
	}
	
	public NgnNetworkService() {
		super();
		
		mAccessPoints = new NgnObservableList<NgnAccessPoint>(true);
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "Starting...");
		mWifiManager = (WifiManager) NgnApplication.getContext().getSystemService(Context.WIFI_SERVICE);
		
		if(mWifiManager == null){
			Log.e(TAG, "WiFi manager is Null");
			return false;
		}
		
		mStarted = true;
		return true;
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "Stopping...");
		if(!mStarted){
			Log.w(TAG, "Not started...");
			return false;
		}
		
		if(mNetworkWatcher != null){
			NgnApplication.getContext().unregisterReceiver(mNetworkWatcher);
			mNetworkWatcher = null;
		}
		
		release();
		mStarted = false;
		return true;
	}

	@Override
	public String getDnsServer(DNS_TYPE type) {
		String dns = null;
		switch (type) {
			case DNS_1: default: dns = "dns1"; break;
			case DNS_2: dns = "dns2"; break;
			case DNS_3: dns = "dns3"; break;
			case DNS_4: dns = "dns4"; break;
		}

		if (mWifiManager != null) {
			String[] dhcpInfos = mWifiManager.getDhcpInfo().toString().split(" ");
			int i = 0;

			while (i++ < dhcpInfos.length) {
				if (dhcpInfos[i - 1].equals(dns)) {
					return dhcpInfos[i];
				}
			}
		}
		return null;
	}

	@Override
	public String getLocalIP(boolean ipv6) {
		final HashMap<String, String> addressMap = new HashMap<String, String>();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					Log.d(NgnNetworkService.TAG, inetAddress.getHostAddress().toString());
					if (!inetAddress.isLoopbackAddress()) {
						if (((inetAddress instanceof Inet4Address) && !ipv6) || ((inetAddress instanceof Inet6Address) && ipv6)) {
							addressMap.put(intf.getName(), inetAddress.getHostAddress().toString());
						}
					}
				}
			}
			if(addressMap.size() > 0){
				final String openvpn = addressMap.get(OPENVPN_INTERFACE_NAME);
				if(!NgnStringUtils.isNullOrEmpty(openvpn)){
					return openvpn;
				}
				return addressMap.values().iterator().next();
			}
		} catch (SocketException ex) {
			Log.e(NgnNetworkService.TAG, ex.toString());
		}

		// Hack
		try {
			java.net.Socket socket = new java.net.Socket(ipv6 ? "ipv6.google.com" : "google.com", 80);
			Log.d(NgnNetworkService.TAG, socket.getLocalAddress().getHostAddress());
			return socket.getLocalAddress().getHostAddress();
		} catch (UnknownHostException e) {
			Log.e(NgnNetworkService.TAG, e.toString());
		} catch (IOException e) {
			Log.e(NgnNetworkService.TAG, e.toString());
		}

		return null;
	}

	@Override
	public boolean isScanning(){
		return mScanning;
	}
	
	@Override
	public boolean setNetworkEnabledAndRegister() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setNetworkEnabled(String SSID, boolean enabled, boolean force) {
		return setNetworkEnabled(getNetworkIdBySSID(SSID), enabled, force);
	}
	
	@Override
	public boolean setNetworkEnabled(int networkId, boolean enabled, boolean force){
		Log.d(TAG, "setNetworkEnabled(" + enabled + ")");
		
		if(mWifiManager == null){
			Log.e(TAG, "WiFi manager is Null");
			return false;
		}
		
		final boolean useWifi = NgnEngine.getInstance().getConfigurationService().getBoolean(
				NgnConfigurationEntry.NETWORK_USE_WIFI, NgnConfigurationEntry.DEFAULT_NETWORK_USE_WIFI);

		if (useWifi) {
			boolean ret = false;
			if ((force || !mWifiManager.isWifiEnabled()) && enabled) {
				Toast.makeText(NgnApplication.getContext(), "Trying to start WiFi",
						Toast.LENGTH_SHORT).show();
				ret = mWifiManager.setWifiEnabled(true);
				if (ret && networkId>=0) {
					ret = mWifiManager.enableNetwork(networkId, true);
				}
			} else if ((force || mWifiManager.isWifiEnabled()) && !enabled) {
				Toast.makeText(NgnApplication.getContext(), "Trying to stop WiFi",
						Toast.LENGTH_SHORT).show();
				ret = mWifiManager.setWifiEnabled(false);
				if (ret && networkId>=0) {
					ret = mWifiManager.disableNetwork(networkId);
				}
			}
			return ret;
		}
		else{
			Log.w(TAG, "setNetworkEnabled() is called but WiFi not enabled");
		}
		return false;
	}

	@Override
	public boolean forceConnectToNetwork() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public NgnObservableList<NgnAccessPoint> getAccessPoints(){
		return mAccessPoints;
	}

	@Override
	public int configure(NgnAccessPoint ap, String password, boolean bHex){
		if(ap == null){
			Log.e(TAG, "Null AccessPoint");
			return -1;
		}
		else if(ap.isConfigured()){
			Log.w(TAG, "AccessPoint already configured");
			return -1;
		}
		else if(ap.getSR() == null){
			Log.e(TAG, "Null SR");
		}
		else if(mWifiManager == null){
			Log.e(TAG, "Null WifiManager");
			return -1;
		}
		
		final ScanResult sr = ap.getSR();
		WifiConfiguration wConf = new WifiConfiguration();
		//http://developer.android.com/reference/android/net/wifi/WifiConfiguration.html#SSID
		wConf.SSID = "\"" + sr.SSID + "\"";
		wConf.BSSID = sr.BSSID;
		wConf.priority = 40;
		String security = NgnAccessPoint.getScanResultSecurity(sr);
		if(security == NgnAccessPoint.AP_WEP){
			wConf.wepKeys[0] = bHex ? password : NgnStringUtils.quote(password, "\"");//hex not quoted
            
			wConf.wepTxKeyIndex = 0;
            
            wConf.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            wConf.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);

            wConf.allowedKeyManagement.set(KeyMgmt.NONE);
            
            wConf.allowedGroupCiphers.set(GroupCipher.WEP40);
            wConf.allowedGroupCiphers.set(GroupCipher.WEP104);
		}
		else if(security == NgnAccessPoint.AP_WPA || security == NgnAccessPoint.AP_WPA2){
			wConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);  
			wConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);  
			wConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);  
			wConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);  
			wConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);  
			wConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);  
			wConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);  
			wConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);  
			wConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);  
			wConf.preSharedKey = "\"".concat("mamadoudiop").concat("\""); 
		}
		else if(security == NgnAccessPoint.AP_OPEN){
			wConf.allowedKeyManagement.set(KeyMgmt.NONE);
		}
		return mWifiManager.addNetwork(wConf);
	}
	
	@Override
	public boolean scan(){
		if(mWifiManager == null){
			Log.e(TAG,"WiFi manager is Null");
			return false;
		}
		
		Toast.makeText(NgnApplication.getContext(), "Network Scanning...", Toast.LENGTH_SHORT).show();
		
		if(mNetworkWatcher == null){
			IntentFilter intentNetWatcher = new IntentFilter();
			intentNetWatcher.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			intentNetWatcher.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
			intentNetWatcher.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
			intentNetWatcher.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			intentNetWatcher.addAction(WifiManager.RSSI_CHANGED_ACTION);
			mNetworkWatcher = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					handleNetworkEvent(context, intent);
				}
			};
			NgnApplication.getContext().registerReceiver(mNetworkWatcher, intentNetWatcher);
		}
		
		mScanning = true;
		if(mWifiManager.setWifiEnabled(true)){
			return mWifiManager.reassociate();
		}
		return false;
	}
	
	@Override
	public boolean acquire() {
		if (mAcquired) {
			return true;
		}
		
		Log.d(TAG, "acquireNetworkLock()");

		boolean connected = false;
		NetworkInfo networkInfo = NgnApplication.getConnectivityManager().getActiveNetworkInfo();
		if (networkInfo == null) {
			Log.e(NgnNetworkService.TAG, "Failed to get Network information");
			return false;
		}

		int netType = networkInfo.getType();
		int netSubType = networkInfo.getSubtype();

		Log.d(NgnNetworkService.TAG, String.format("netType=%d and netSubType=%d",
				netType, netSubType));

		boolean useWifi = NgnEngine.getInstance().getConfigurationService().getBoolean(NgnConfigurationEntry.NETWORK_USE_WIFI, 
				NgnConfigurationEntry.DEFAULT_NETWORK_USE_WIFI);
		boolean use3G = NgnEngine.getInstance().getConfigurationService().getBoolean(NgnConfigurationEntry.NETWORK_USE_3G,
				NgnConfigurationEntry.DEFAULT_NETWORK_USE_3G);

		if (useWifi && (netType == ConnectivityManager.TYPE_WIFI)) {
			if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
				mWifiLock = mWifiManager.createWifiLock(
						WifiManager.WIFI_MODE_FULL, NgnNetworkService.TAG);
				final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
				if (wifiInfo != null && mWifiLock != null) {
					final DetailedState detailedState = WifiInfo
							.getDetailedStateOf(wifiInfo.getSupplicantState());
					if (detailedState == DetailedState.CONNECTED
							|| detailedState == DetailedState.CONNECTING
							|| detailedState == DetailedState.OBTAINING_IPADDR) {
						mWifiLock.acquire();
						mConnetedSSID = wifiInfo.getSSID();
						connected = true;
					}
				}
			} else {
				Log.d(NgnNetworkService.TAG, "WiFi not enabled");
			}
		} else if (use3G
				&& (netType == ConnectivityManager.TYPE_MOBILE || netType == ConnectivityManager_TYPE_WIMAX)) {
			if ((netSubType >= TelephonyManager.NETWORK_TYPE_UMTS)
					|| // HACK
					(netSubType == TelephonyManager.NETWORK_TYPE_GPRS)
					|| (netSubType == TelephonyManager.NETWORK_TYPE_EDGE)) {
				//Toast.makeText(WiPhone.getContext(),
				//		"Using 2.5G (or later) network", Toast.LENGTH_SHORT)
				//		.show();
				connected = true;
			}
		}

		if (!connected) {
			Log.d(NgnNetworkService.TAG, "No active network");
			return false;
		}

		mAcquired = true;
		return true;
	}

	@Override
	public boolean release() {
		if (mWifiLock != null) {
			if(mWifiLock.isHeld()){
				Log.d(TAG, "releaseNetworkLock()");
				mWifiLock.release();
			}	
			mWifiLock = null;
		}

		mAcquired = false;
		return true;
	}
	
	private int getNetworkIdBySSID(String SSID) {
		synchronized(mAccessPoints){
			final NgnAccessPoint ap = getAccessPointBySSID(SSID);
			if(ap != null){
				return ap.getNetworkId();
			}
			return -1;
		}
	}

	@SuppressWarnings("unused")
	private WifiConfiguration getWifiConfBySSID(String SSID) {
		synchronized(mAccessPoints){
			final NgnAccessPoint ap = getAccessPointBySSID(SSID);
			if(ap != null){
				return ap.getConf();
			}
			return null;
		}
	}
	
	private NgnAccessPoint getAccessPointBySSID(String SSID) {
		final List<NgnAccessPoint> accessPoints = mAccessPoints.getList();
		for (NgnAccessPoint ap : accessPoints) {
			String SSID1 = NgnStringUtils.unquote(ap.getSSID(), "\"");
			String SSID2 = NgnStringUtils.unquote(SSID, "\"");
			if (SSID1.equalsIgnoreCase(SSID2)) {
				return ap;
			}
		}
		return null;
	}
	
	private void loadConfiguredNetworks(){
		synchronized(mAccessPoints){
			mAccessPoints.clear();
			final List<WifiConfiguration> confNetworks = mWifiManager.getConfiguredNetworks();
			for (WifiConfiguration wifiConf : confNetworks) {
				NgnAccessPoint ap = new NgnAccessPoint(wifiConf);
				ap.setConnected(NgnStringUtils.equals(mConnetedSSID, ap.getSSID(), false));
				mAccessPoints.add(ap);
			}
		}
	}
	
	private void handleNetworkEvent(Context context, Intent intent){
		final String action = intent.getAction();
		Log.d(TAG, "NetworkService::BroadcastReceiver(" + action + ")");
		
		if(mWifiManager == null){
			Log.e(TAG, "Invalid state");
			return;
		}
		
		if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
			mScanning = true;
			// load() configured networks
			loadConfiguredNetworks();
			// load() network results
			synchronized(mAccessPoints){
				List<ScanResult> scanResults = mWifiManager.getScanResults();
				for(ScanResult sr : scanResults){
					NgnAccessPoint ap = getAccessPointBySSID(sr.SSID);
					if(ap == null){
						ap = new NgnAccessPoint(sr);
						mAccessPoints.add(ap);
					}
				}
			}
			
			updateConnectionState();
			mScanning = false;
		}
		else if(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)){
			final boolean connected = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
			Log.d(TAG, "SUPPLICANT_CONNECTION_CHANGE_ACTION.CONNECTED="+connected);
			if(connected){
				final WifiInfo wInfo = mWifiManager.getConnectionInfo();
				if(wInfo != null){
					if(!NgnStringUtils.equals(mConnetedSSID, wInfo.getSSID(), false)){
						triggerSipRegistration();
					}
					mConnetedSSID = wInfo.getSSID();
				}
			}
			updateConnectionState();
//			synchronized(mAccessPoints){
//				final List<AccessPoint> aps = mAccessPoints.getList();
//				for(AccessPoint ap : aps){
//					ap.setConnected(connected && StringUtils.equals(mConnetedSSID, ap.getSSID(), false));
//				}
//			}
		}
		else if(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)){
			updateConnectionState();
//			final SupplicantState newState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
//			if(newState != null){
//				synchronized(mAccessPoints){
//					final List<AccessPoint> aps = mAccessPoints.getList();
//					final WifiInfo wInfo = mWifiManager.getConnectionInfo();
//					if(wInfo != null){
//						for(AccessPoint ap : aps){
//							ap.setConnected((newState == SupplicantState.ASSOCIATED) && StringUtils.equals(wInfo.getSSID(), ap.getSSID(), false));
//						}
//					}
//				}
//			}
		}
		else if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)){
			updateConnectionState();
//			final boolean connected = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
//				== WifiManager.WIFI_STATE_ENABLED;
//			synchronized(mAccessPoints){
//				final List<AccessPoint> aps = mAccessPoints.getList();
//				final WifiInfo wInfo = mWifiManager.getConnectionInfo();
//				if(wInfo != null){
//					for(AccessPoint ap : aps){
//						ap.setConnected(connected && StringUtils.equals(wInfo.getSSID(), ap.getSSID(), false));
//					}
//				}
//			}
		}
		else if(WifiManager.RSSI_CHANGED_ACTION.equals(action)){
			final WifiInfo wInfo = mWifiManager.getConnectionInfo();
			if(wInfo != null){
				final NgnAccessPoint ap = getAccessPointBySSID(wInfo.getSSID());
				if(ap != null){
					final int newRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200);
					ap.setLevel(WifiManager.calculateSignalLevel(newRssi,
							sWifiSignalValues.length));
				}
			}
		}
	}
	
	private void updateConnectionState(){
		final WifiInfo wInfo = mWifiManager.getConnectionInfo();
		boolean bAtLeastOneConnected = false;
		if(wInfo != null){
			final DetailedState detailedState = WifiInfo
				.getDetailedStateOf(wInfo.getSupplicantState());
			boolean isConnecting = detailedState == DetailedState.CONNECTED
			|| detailedState == DetailedState.CONNECTING
			|| detailedState == DetailedState.OBTAINING_IPADDR;
			synchronized(mAccessPoints){
				final List<NgnAccessPoint> aps = mAccessPoints.getList();
				if(wInfo != null){
					for(NgnAccessPoint ap : aps){
						final boolean connected = isConnecting && NgnStringUtils.equals(wInfo.getSSID(), ap.getSSID(), false);
						ap.setConnected(connected);
						bAtLeastOneConnected |= connected;
					}
				}
			}
		}
		
		if(bAtLeastOneConnected || !NgnEngine.getInstance().getSipService().isRegistered()){
			triggerSipRegistration();
		}
		
	}
	
	private void triggerSipRegistration(){
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				Log.d(TAG, "Network connection chaged: restart the stack");
//				final ISipService sipService = ServiceManager.getSipService();
//				final ConnectionState registrationState = sipService.getRegistrationState();
//				switch(registrationState){
//					case NONE:
//					case TERMINATED:
//						sipService.register(null);
//						break;
//					case CONNECTING:
//					case TERMINATING:
//					case CONNECTED:
//						sipService.unRegister();
//						sipService.register(null);
//						break;
//				}
//			}
//		}).start();
	}
}
