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

import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnNetworkEventArgs;
import org.doubango.ngn.events.NgnNetworkEventTypes;
import org.doubango.ngn.services.INgnNetworkService;
import org.doubango.ngn.utils.NgnNetworkConnection;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnObservableList;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.tinyWRAP.SipStack;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**@page NgnNetworkService_page Network Service
 * The network service is used to manage both WiFi and 3g/4g network connections.
 */

/**
 * Network service.
 */
public class NgnNetworkService  extends NgnBaseService implements INgnNetworkService {
	private static final String TAG = NgnNetworkService.class.getCanonicalName();
	private WifiManager mWifiManager;
	private WifiLock mWifiLock;
	private PowerManager.WakeLock mCellularLock;
	private String mConnetedSSID;
	private boolean mStarted;
	private String mProxyCSCFHost;
	private String mProxyCSCFIPversion;
	private int mProxyCSCFPort;
	private String mProxyCSCFTransport;
	private final NgnObservableList<NgnNetworkConnection> mConnections;
	private BroadcastReceiver mNetworkWatcher;

	private static int WifiManager_WIFI_MODE = WifiManager.WIFI_MODE_FULL;
	private static int ConnectivityManager_TYPE_ETHERNET = 0x00000009;
	private static int ConnectivityManager_TYPE_WIMAX = 0x00000006;
	private static Method NetworkInterface_isUp;

	static{
		final int sdkVersion = NgnApplication.getSDKVersion();
		if(sdkVersion >= 9){
			try {
				WifiManager_WIFI_MODE = WifiManager.class.getDeclaredField("WIFI_MODE_FULL_HIGH_PERF").getInt(null);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, e.toString());
			}
		}
		if(sdkVersion >= 13){
			try {
				ConnectivityManager_TYPE_ETHERNET = ConnectivityManager.class.getDeclaredField("TYPE_ETHERNET").getInt(null);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, e.toString());
			}
		}

		if(sdkVersion >= 8){
			try {
				ConnectivityManager_TYPE_WIMAX = ConnectivityManager.class.getDeclaredField("TYPE_WIMAX").getInt(null);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, e.toString());
			}
		}

		// according to the documentation, NetworkInterface::isUp() is only defined starting API Level 9 but it's available on my GS1 (API Level 8)
		// this is why we don't test sdk version
		try{
			NetworkInterface_isUp = NetworkInterface.class.getDeclaredMethod("isUp");
		}
		catch (Exception e) { }
	}

	public static final int[] sWifiSignalValues = new int[] {
			0,
			1,
			2,
			3,
			4
	};

	public static enum DNS_TYPE {
		DNS_1, DNS_2, DNS_3, DNS_4
	}

	public NgnNetworkService() {
		super();

		mConnections = new NgnObservableList<NgnNetworkConnection>(true);
	}

	/**
	 * Starts the network service.
	 * @return true if it succeed, false otherwise.
	 */
	@Override
	public boolean start() {
		Log.d(TAG, "Starting...");
		mWifiManager = (WifiManager) NgnApplication.getContext().getSystemService(Context.WIFI_SERVICE);

		if (mWifiManager == null){
			Log.e(TAG, "WiFi manager is Null");
			return false;
		}

		mWifiManager.setWifiEnabled(true);

		if (mNetworkWatcher == null){
			IntentFilter intentNetWatcher = new IntentFilter();
			intentNetWatcher.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

			mNetworkWatcher = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (!isInitialStickyBroadcast()) {
						handleNetworkEvent(context, intent);
					}
				}
			};
			NgnApplication.getContext().registerReceiver(mNetworkWatcher, intentNetWatcher);
		}

		if (!loadInterfaces()) {
			return false;
		}

		acquire();

		mStarted = true;
		return true;
	}

	/**
	 * Stops the network service.
	 * @return true if it succeed, false otherwise.
	 */
	@Override
	public boolean stop() {
		Log.d(TAG, "Stopping...");
		if (!mStarted){
			Log.w(TAG, "Not started...");
			return false;
		}

		if (mNetworkWatcher != null){
			NgnApplication.getContext().unregisterReceiver(mNetworkWatcher);
			mNetworkWatcher = null;
		}
		mConnections.getList().clear();
		release();
		mStarted = false;
		return true;
	}

	/**
	 * Gets the list of active connections.
	 * @return
	 */
	@Override
	public NgnObservableList<NgnNetworkConnection> getConnections()
	{
		return mConnections;
	}

	/**
	 * Sets the Proxy CSCF
	 * @param transport
	 * @param IPversion
	 * @param host
	 * @param port
	 * @return
	 */
	@Override
	public boolean setProxyCSCF(final String transport, final String IPversion, final String host, final int port)
	{
		final boolean use3G = NgnEngine.getInstance().getConfigurationService().getBoolean(NgnConfigurationEntry.NETWORK_USE_3G,
				NgnConfigurationEntry.DEFAULT_NETWORK_USE_3G);
		if (use3G) {
			final boolean ipv6 = NgnStringUtils.equals(IPversion, "ipv6", true); // "ipv46" and "ipv64" use "IPv4" as default transport
			// Enable cellular data for the route to P-CSCF
			requestCellularRouteToHost(host, ipv6);
		}
		else {
			Log.d(TAG, "3G not enabled, not requesting cellular route to " + host);
		}

		// Update values
		mProxyCSCFTransport = transport;
		mProxyCSCFIPversion = IPversion;
		mProxyCSCFHost = host;
		mProxyCSCFPort = port;

		// Update old values with new parameters
		synchronized (mConnections) {
			final List<NgnNetworkConnection> connections = mConnections.getList();
			NgnNetworkConnection connection;
			Iterator<NgnNetworkConnection> it = connections.iterator();
			while (it.hasNext()) {
				connection = it.next();
				connection.setProxyCSCF(host, port);
				connection.setTransport(transport, IPversion);
			}
		}
		return true;
	}

	/**
	 * Gets the address of the default DNS server.
	 * @param type The type of DNS server.
	 * @return The address of the DNS server if it succeed, null otherwise.
	 */
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

	/**
	 * Gets the default connection.
	 * @param ipv6 Whether to loop for IPv6 addresses only. Set this value to false to look for IPv4 addresses.
	 * @return
	 */
	@Override
	public NgnNetworkConnection getBestConnection(boolean ipv6) {
		synchronized (mConnections) {
			// Tunnel
			final NgnNetworkConnection vpnConnection = mConnections.first(new NgnNetworkConnection.NgnNetworkConnectionFilterByUpAndIPv6AndNameStartsWith(true, ipv6, "tun"));
			if (vpnConnection != null) {
				return vpnConnection;
			}
			final Context context = NgnApplication.getContext();
			final ConnectivityManager connectivityManager = NgnApplication.getConnectivityManager();
			NetworkInfo networkInfo = null;
			if (connectivityManager != null) {
				networkInfo = connectivityManager.getActiveNetworkInfo();
			}

			// Wifi
			if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				final boolean useWifi = NgnEngine.getInstance().getConfigurationService().getBoolean(NgnConfigurationEntry.NETWORK_USE_WIFI,
						NgnConfigurationEntry.DEFAULT_NETWORK_USE_WIFI);
				if (useWifi) {
					final NgnNetworkConnection wifiConnection = mConnections.first(new NgnNetworkConnection.NgnNetworkConnectionFilterByUpAndIPv6AndNameStartsWith(true, ipv6, "wlan"));
					if (wifiConnection != null) {
						return wifiConnection;
					}
				}
			}

			// Cellular
			if (networkInfo != null && (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE || networkInfo.getType() == ConnectivityManager_TYPE_WIMAX)) {
				final boolean use3G = NgnEngine.getInstance().getConfigurationService().getBoolean(NgnConfigurationEntry.NETWORK_USE_3G,
						NgnConfigurationEntry.DEFAULT_NETWORK_USE_3G);
				if (use3G) {
					final NgnNetworkConnection cellularConnection = mConnections.first(new NgnNetworkConnection.NgnNetworkConnectionFilterByUpAndIPv6AndNameStartsWith(true, ipv6, "rmnet"));
					if (cellularConnection != null) {
						return cellularConnection;
					}
				}
				else {
					Log.d(TAG, "Cellular network up but not enabled in user interface");
				}
			}

			// Any UP connection
			final NgnNetworkConnection upConnection = mConnections.first(new NgnNetworkConnection.NgnNetworkConnectionFilterByUpAndIPv6(true, ipv6));
			if (upConnection != null) {
				return upConnection;
			}

			// Any UP or DOWN connection
			if (!mConnections.getList().isEmpty()) {
				return mConnections.getList().get(0);
			}
			return null;
		}
	}

	/**
	 * Ensure that a network route exists to deliver traffic to the specified host via Cellular data.
	 * @param host
	 * @param IPv6
	 * @return
	 */
	public boolean requestCellularRouteToHost(final String host, final boolean IPv6)
	{
		Log.d(TAG, "requestCellularRouteToHost("+host+")");
		int sdkVersion = NgnApplication.getSDKVersion();
		if (sdkVersion < 8) {
			Log.e(TAG, "requestCellularRouteToHost not implemented for API level " + sdkVersion);
			return false;
		}
		if (sdkVersion < 21) {
			return requestCellularRouteToHostApi8(host, IPv6);
		}
		return requestCellularRouteToHostApi21(host, IPv6);
	}

	/**
	 * Make sure all connections created by the current process will use the specified SIP network connection.
	 * @param connection The SIP network connection to use. Null to unbind.
	 * @return true if succeed, false otherwise.
	 */
	public boolean bindProcessToConnection(final NgnNetworkConnection connection)
	{
		if (connection != null && !connection.isUp()) {
			Log.w(TAG, "Binding process to down connection ->" + connection);
		}
		Log.d(TAG, "bindProcessToConnection(" + connection + ")");
		final int sdkVersion = NgnApplication.getSDKVersion();
		if (sdkVersion < 8) {
			Log.e(TAG, "bindProcessToConnection not implemented for API level " + sdkVersion);
			return false;
		}
		if (sdkVersion < 21) {
			return bindProcessToConnectionApi8(connection);
		}
		if (sdkVersion < 23) {
			return bindProcessToConnectionApi21(connection);
		}
		return bindProcessToConnectionApi23(connection);
	}

	/**
	 * Locks the network and start using it. Later, the network must be unlocked using @release.
	 * @return true if succeed, false otherwise.
	 * @sa release
	 */
	@Override
	public boolean acquire() {
		Log.d(TAG, "acquireNetworkLock()");

		final NetworkInfo networkInfo = NgnApplication.getConnectivityManager().getActiveNetworkInfo();
		if (networkInfo == null) {
			Log.e(TAG, "Failed to get Network information");
			return false;
		}

		final int netType = networkInfo.getType();
		final int netSubType = networkInfo.getSubtype();
		Log.d(NgnNetworkService.TAG, String.format("netType=%d and netSubType=%d", netType, netSubType));

		if (mWifiLock == null && isWifiNetwork(networkInfo)) {
			if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
				mWifiLock = mWifiManager.createWifiLock(NgnNetworkService.WifiManager_WIFI_MODE, NgnNetworkService.TAG + "Lock_Wifi");
				final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
				if (wifiInfo != null && mWifiLock != null) {
					final DetailedState detailedState = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
					if (detailedState == DetailedState.CONNECTED
							|| detailedState == DetailedState.CONNECTING
							|| detailedState == DetailedState.OBTAINING_IPADDR) {
						mWifiLock.acquire();
						mConnetedSSID = wifiInfo.getSSID();
					}
				}
			} else {
				Log.d(NgnNetworkService.TAG, "WiFi not enabled");
			}
		} else if (mCellularLock == null && isMobileNetwork(networkInfo)) {
			PowerManager pm = (PowerManager) NgnApplication.getContext().getSystemService(Context.POWER_SERVICE);
			mCellularLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, NgnNetworkService.TAG + "Lock_Cellular");
			if (mCellularLock != null) {
				mCellularLock.acquire();
			}
		}
		return true;
	}

	/**
	 * Unlocks the network and stop using it. The network must be locked first using @ref acquire.
	 * @return true is succeed, false otherwise.
	 * @sa acquire
	 */
	@Override
	public boolean release() {
		Log.d(TAG, "releaseNetworkLock()");
		if (mWifiLock != null) {
			if (mWifiLock.isHeld()){
				mWifiLock.release();
			}
			mWifiLock = null;
		}
		if (mCellularLock != null) {
			if (mCellularLock.isHeld()){
				mCellularLock.release();
			}
			mCellularLock = null;
		}
		return true;
	}

	/**
	 * Handles the network events.
	 * @param context the context associated event.
	 * @param intent The intent associated to the event.
	 */
	private void handleNetworkEvent(Context context, Intent intent){
		final String action = intent.getAction();
		Log.d(TAG, "NetworkService::BroadcastReceiver(" + action + ")");

		if (mWifiManager == null){
			Log.e(TAG, "Invalid state");
			return;
		}

		// NETWORK_STATE_CHANGED_ACTION: Broadcast intent action indicating that the state of Wi-Fi connectivity has changed.
		if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)){
			// TODO(dmi): do not reload all, just get what changed
			final NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (networkInfo != null){
				Log.d(TAG, "NETWORK_STATE_CHANGED_ACTION.State=" + networkInfo.getState());
				final boolean connected = networkInfo.isConnected();
				if(connected && mStarted) {
					acquire();
				}
				loadInterfaces();
				broadcastNetworkEvent(new NgnNetworkEventArgs(connected ? NgnNetworkEventTypes.CONNECTED : NgnNetworkEventTypes.DISCONNECTED));
			}
		}
	}

	private boolean loadInterfaces()
	{
		synchronized (mConnections) {
			NgnNetworkConnection connection;
			List<NgnNetworkConnection> upList = null;
			boolean triedActivatingNetwork = false;

			while (upList == null || upList.isEmpty()) {
				try {
					for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
						final NetworkInterface intf = en.nextElement();
						final String name = intf.getName();

						if (name.startsWith("usb")) {
							Log.i(TAG, "interface=" + intf.getName() + " is USB and ignored");
							continue;
						}

						connection = mConnections.first(new NgnNetworkConnection.NgnNetworkConnectionFilterByName(name));
						if (connection != null) {
							connection.setUp(false);
						}

						// http://code.google.com/p/imsdroid/issues/detail?id=398#c3
						try {
							if (NetworkInterface_isUp != null && !(Boolean) NetworkInterface_isUp.invoke(intf)) {
								Log.i(TAG, "interface=" + name + " is not up");
								continue;
							}
							Log.d(TAG, "interface=" + name + " is up");
						} catch (Exception e) {
						}

						for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
							final InetAddress inetAddress = enumIpAddr.nextElement();
							Log.d(NgnNetworkService.TAG, inetAddress.getHostAddress().toString());
							if (inetAddress.isLoopbackAddress() || ((inetAddress instanceof Inet6Address) && ((Inet6Address) inetAddress).isLinkLocalAddress())) {
								continue;
							}
							final String localIP = inetAddress.getHostAddress().toString();
							if (NgnStringUtils.isNullOrEmpty(localIP)) {
								continue;
							}
							connection = mConnections.first(new NgnNetworkConnection.NgnNetworkConnectionFilterByName(name));
							if (connection == null) {
								connection = new NgnNetworkConnection(name, mProxyCSCFTransport, mProxyCSCFIPversion);
								mConnections.add(connection);
								Log.d(TAG, "Add connection with name = " + name + " and address = " + localIP);
							}
							connection.setProxyCSCF(mProxyCSCFHost, mProxyCSCFPort);
							connection.setIPv6(inetAddress instanceof Inet6Address);
							connection.setUp(true);
							connection.setLocalIP(localIP);
						}
					}
				} catch (SocketException e) {
					e.printStackTrace();
					Log.e(TAG, e.toString());
				}

				upList = mConnections.filter(new NgnNetworkConnection.NgnNetworkConnectionFilterByUp(true));
				if (upList.isEmpty() && !triedActivatingNetwork) {
					for (int a = 0; a < 2; ++a) {
						try {
							java.net.Socket socket = new java.net.Socket((a == 0) ? "ipv6.google.com" : "google.com", 80);
							Log.d(TAG, socket.getLocalAddress().getHostAddress());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					triedActivatingNetwork = true;
				} else {
					return true;
				}
			}

			return true;
		}
	}

	@TargetApi(8)
	private boolean requestCellularRouteToHostApi8(String host, boolean IPv6)
	{
		if (IPv6) {
			Log.e(TAG, "requestCellularRouteToHost cannot be used with IPv6 address when API level is < 21");
			return false;
		}
		final ConnectivityManager connectivityManager = NgnApplication.getConnectivityManager();
		if (connectivityManager == null) {
			Log.e(TAG, "Failed to retrieve a connection manager");
			return false;
		}

		// Check mobile connection status
		NetworkInfo.State state = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).getState();
		Log.d(TAG, "TYPE_MOBILE_HIPRI network state: " + state);
		if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
			Log.d(TAG, "TYPE_MOBILE_HIPRI network connected or connecting");
		}

		// Activate the mobile connection if not already and do nothing if already done
		int resultInt = connectivityManager.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
		Log.d(TAG, "startUsingNetworkFeature(TYPE_MOBILE, enableHIPRI) returned " + resultInt);
		if ( resultInt == -1) {
			Log.e(TAG, "startUsingNetworkFeature(TYPE_MOBILE, enableHIPRI) failed");
			return false;
		}
		if (resultInt == 0) {
			Log.d(TAG, "Mobile data already activated for all hosts");
			return true;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try  {
					for (int counter=0; counter<10; counter++) {
						NetworkInfo.State checkState = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).getState();
						if (checkState == NetworkInfo.State.CONNECTED) {
							Log.d(TAG, "requestCellularRouteToHostApi8: Cellular network is available");
							broadcastNetworkEvent(new NgnNetworkEventArgs(NgnNetworkEventTypes.CELLULAR_AVAILABLE));
							return;
						}
						Thread.sleep(500);
					}
					Log.w(TAG, "TYPE_MOBILE_HIPRI: State never goes to CONNECTED");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		return true;
	}

	@TargetApi(21)
	private boolean requestCellularRouteToHostApi21(String host, boolean IPv6)
	{
		final ConnectivityManager connectivityManager = NgnApplication.getConnectivityManager();
		if (connectivityManager == null) {
			Log.e(TAG, "Failed to retrieve a connection manager");
			return false;
		}
		NetworkRequest.Builder req = new NetworkRequest.Builder();
		req.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
		req.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
		connectivityManager.requestNetwork(req.build(), new ConnectivityManager.NetworkCallback() {
			@Override
			public void onAvailable(Network network) {
				// FIXME(dmi): do not reload all
				loadInterfaces();
				broadcastNetworkEvent(new NgnNetworkEventArgs(NgnNetworkEventTypes.CELLULAR_AVAILABLE));
				Log.d(TAG, "requestCellularRouteToHostApi21: Cellular network is available");
			}
		});

		return true;
	}

	@TargetApi(21)
	private Network findNetworkByConnectionApi21(final NgnNetworkConnection connection)
	{
		final ConnectivityManager connectivityManager = NgnApplication.getConnectivityManager();
		if (connectivityManager == null) {
			Log.e(TAG, "Failed to retrieve a connection manager");
			return null;
		}
		final Network[] network = connectivityManager.getAllNetworks();
		if (network != null && network.length > 0){
			final String name = connection.getName();
			for (int i = 0 ; i < network.length ; i++){
				LinkProperties prop = connectivityManager.getLinkProperties(network[i]);
				try {
					final NetworkInterface iface = NetworkInterface.getByName(prop.getInterfaceName());
					if (name.equals(iface.getName())) {
						return network[i];
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, e.toString());
				}
			}
		}
		return null;
	}

	@TargetApi(8)
	private boolean bindProcessToConnectionApi8(final NgnNetworkConnection connection)
	{
		if (connection == null) {
			return true;
		}
		final String proxyHost = connection.getProxyHost();
		final int hostAddressInt = hostnameToInt(proxyHost);
		if (hostAddressInt == 0) {
			Log.e(TAG, "hostnameIPv4ToInt("+proxyHost+") failed");
			return false;
		}
		final ConnectivityManager connectivityManager = NgnApplication.getConnectivityManager();
		if (connectivityManager == null) {
			Log.e(TAG, "Failed to retrieve a connection manager");
			return false;
		}

		return connectivityManager.requestRouteToHost(ConnectivityManager.TYPE_MOBILE_HIPRI, hostAddressInt);
	}

	@TargetApi(21)
	private boolean bindProcessToConnectionApi21(final NgnNetworkConnection connection)
	{
		final Network network = connection == null ? null : findNetworkByConnectionApi21(connection);
		if (network == null && connection != null) {
			Log.e(TAG, "Failed to find network with connection ->" + connection);
			return false;
		}
		final ConnectivityManager connectivityManager = NgnApplication.getConnectivityManager();
		if (connectivityManager == null) {
			Log.e(TAG, "Failed to retrieve a connection manager");
			return false;
		}
		return connectivityManager.setProcessDefaultNetwork(network);
	}

	@TargetApi(23)
	private boolean bindProcessToConnectionApi23(final NgnNetworkConnection connection)
	{
		final Network network = connection == null ? null : findNetworkByConnectionApi21(connection);
		if (network == null && connection != null) {
			Log.e(TAG, "Failed to find network with connection ->" + connection);
			return false;
		}
		final ConnectivityManager connectivityManager = NgnApplication.getConnectivityManager();
		if (connectivityManager == null) {
			Log.e(TAG, "Failed to retrieve a connection manager");
			return false;
		}
		return connectivityManager.bindProcessToNetwork(network);
	}

	private void broadcastNetworkEvent(NgnNetworkEventArgs args) {
		final Intent intent = new Intent(
				NgnNetworkEventArgs.ACTION_NETWORK_EVENT);
		intent.putExtra(NgnNetworkEventArgs.EXTRA_EMBEDDED, args);
		NgnApplication.getContext().sendBroadcast(intent);
	}

	private static int hostnameToInt(String hostname) {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			return -1;
		}
		byte[] addrBytes;
		int addr;
		addrBytes = inetAddress.getAddress();
		addr = ((addrBytes[3] & 0xff) << 24)
				| ((addrBytes[2] & 0xff) << 16)
				| ((addrBytes[1] & 0xff) << 8 )
				|  (addrBytes[0] & 0xff);
		return addr;
	}

	private static boolean isWifiNetwork(final NetworkInfo networkInfo) {
		if (networkInfo != null) {
			return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
		}
		return false;
	}

	private static boolean isMobileNetwork(final NetworkInfo networkInfo) {
		if (networkInfo != null) {
			final int netType = networkInfo.getType();
			if ((netType == ConnectivityManager.TYPE_MOBILE || netType == ConnectivityManager_TYPE_WIMAX)) {
				final int netSubType = networkInfo.getSubtype();
				return ((netSubType >= TelephonyManager.NETWORK_TYPE_UMTS)
						|| // HACK
						(netSubType == TelephonyManager.NETWORK_TYPE_GPRS)
						|| (netSubType == TelephonyManager.NETWORK_TYPE_EDGE));
			}
		}
		return false;
	}
}
