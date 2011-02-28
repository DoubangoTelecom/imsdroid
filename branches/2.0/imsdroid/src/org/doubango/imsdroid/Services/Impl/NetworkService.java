
package org.doubango.imsdroid.Services.Impl;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Utils.ConfigurationUtils;
import org.doubango.imsdroid.Utils.ConfigurationUtils.ConfigurationEntry;

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

public class NetworkService  extends BaseService implements INetworkService {
	private static final String TAG = NetworkService.class.getCanonicalName();
	
	private WifiManager mWifiManager;
	private WifiLock mWifiLock;
	private boolean mAcquired;
	private boolean mStarted;
	
	// Will be added in froyo SDK
	private static int ConnectivityManager_TYPE_WIMAX = 6;
	
	public static enum DNS_TYPE {
		DNS_1, DNS_2, DNS_3, DNS_4
	}
	
	public NetworkService() {
		super();
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "Starting...");
		mWifiManager = (WifiManager) IMSDroid.getContext().getSystemService(Context.WIFI_SERVICE);
		
		if(mWifiManager != null){
			mStarted = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean stop() {
		if(!mStarted){
			Log.w(TAG, "Not started...");
			return false;
		}
		
		Log.d(TAG, "Stopping...");
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
		// From Interfaces
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					Log.d(NetworkService.TAG, inetAddress.getHostAddress()
							.toString());
					if (!inetAddress.isLoopbackAddress()) {
						if (((inetAddress instanceof Inet4Address) && !ipv6)
								|| ((inetAddress instanceof Inet6Address) && ipv6)) {
							return inetAddress.getHostAddress().toString();
						}
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(NetworkService.TAG, ex.toString());
		}

		// Hack
		try {
			java.net.Socket socket = new java.net.Socket(
					ipv6 ? "ipv6.google.com" : "google.com", 80);
			Log.d(NetworkService.TAG, socket.getLocalAddress().getHostAddress());
			return socket.getLocalAddress().getHostAddress();
		} catch (UnknownHostException e) {
			Log.e(NetworkService.TAG, e.toString());
		} catch (IOException e) {
			Log.e(NetworkService.TAG, e.toString());
		}

		return null;
	}

	@Override
	public boolean setNetworkEnabledAndRegister() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setNetworkEnabled(String SSID, boolean enabled) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean forceConnectToNetwork() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean acquire() {
		if (mAcquired) {
			return true;
		}

		boolean connected = false;

		ConnectivityManager connectivityManager = (ConnectivityManager) IMSDroid
				.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		if (networkInfo == null) {
			Toast.makeText(IMSDroid.getContext(),
					"Failed to get Network information", Toast.LENGTH_LONG)
					.show();
			Log.d(NetworkService.TAG, "Failed to get Network information");
			return false;
		}

		int netType = networkInfo.getType();
		int netSubType = networkInfo.getSubtype();

		Log.d(NetworkService.TAG, String.format("netType=%d and netSubType=%d",
				netType, netSubType));

		boolean useWifi = ServiceManager.getConfigurationService().getBoolean(ConfigurationEntry.NETWORK_USE_WIFI, 
				ConfigurationUtils.DEFAULT_NETWORK_USE_WIFI);
		boolean use3G = ServiceManager.getConfigurationService().getBoolean(ConfigurationEntry.NETWORK_USE_3G,
				ConfigurationUtils.DEFAULT_NETWORK_USE_3G);

		if (useWifi && (netType == ConnectivityManager.TYPE_WIFI)) {
			if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
				mWifiLock = mWifiManager.createWifiLock(
						WifiManager.WIFI_MODE_FULL, NetworkService.TAG);
				final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
				if (wifiInfo != null && mWifiLock != null) {
					final DetailedState detailedState = WifiInfo
							.getDetailedStateOf(wifiInfo.getSupplicantState());
					if (detailedState == DetailedState.CONNECTED
							|| detailedState == DetailedState.CONNECTING
							|| detailedState == DetailedState.OBTAINING_IPADDR) {
						mWifiLock.acquire();
						connected = true;
					}
				}
			} else {
				Toast.makeText(IMSDroid.getContext(), "WiFi not enabled",
						Toast.LENGTH_LONG).show();
				Log.d(NetworkService.TAG, "WiFi not enabled");
			}
		} else if (use3G
				&& (netType == ConnectivityManager.TYPE_MOBILE || netType == ConnectivityManager_TYPE_WIMAX)) {
			if ((netSubType >= TelephonyManager.NETWORK_TYPE_UMTS)
					|| // HACK
					(netSubType == TelephonyManager.NETWORK_TYPE_GPRS)
					|| (netSubType == TelephonyManager.NETWORK_TYPE_EDGE)) {
				Toast.makeText(IMSDroid.getContext(),
						"Using 2.5G (or later) network", Toast.LENGTH_SHORT)
						.show();
				connected = true;
			}
		}

		if (!connected) {
			Toast.makeText(IMSDroid.getContext(), "No active network",
					Toast.LENGTH_LONG).show();
			Log.d(NetworkService.TAG, "No active network");
			return false;
		}

		mAcquired = true;
		return true;
	}

	@Override
	public boolean release() {
		if (mWifiLock != null) {
			if(mWifiLock.isHeld()){
				mWifiLock.release();
			}	
			mWifiLock = null;
		}

		mAcquired = false;
		return true;
	}
}
