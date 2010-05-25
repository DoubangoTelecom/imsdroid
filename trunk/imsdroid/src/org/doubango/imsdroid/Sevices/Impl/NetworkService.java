package org.doubango.imsdroid.Sevices.Impl;

import org.doubango.imsdroid.Services.INetworkService;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class NetworkService  extends Service implements INetworkService {

	private WifiManager wifiManager;
	
	public static enum DNS_TYPE {
		DNS_1, DNS_2, DNS_3, DNS_4
	}
	
	public NetworkService(){
		super();
	}
	
	public boolean start() {
		this.wifiManager = (WifiManager) ServiceManager.getMainActivity().getSystemService(Context.WIFI_SERVICE);
		return true;
	}

	public boolean stop() {
		return true;
	}
	
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
		
		String[] dhcpInfos = this.wifiManager.getDhcpInfo().toString().split(" ");
		int i = 0;
		
		while (i++ < dhcpInfos.length) {
		  if (dhcpInfos[i-1].equals(dns)) {
			  return dhcpInfos[i];
		  }
		}
		return null;
	}
	
	public String getLocalIP(boolean ipv6){
		
		if(ipv6){
			return null;
		}
		
		WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		
		if(ipAddress != 0){
			return String.format("%d.%d.%d.%d",
					(ipAddress>>0)&0xFF,
					(ipAddress>>8)&0xFF,
					(ipAddress>>16)&0xFF,
					(ipAddress>>24)&0xFF
					);
		}
		else{
			return null;
		}
	}
}
