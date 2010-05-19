package org.doubango.imsdroid.utils;


public class NetworkUtils {

	public static enum DNS_TYPE {
		DNS_1, DNS_2, DNS_3, DNS_4
	}

	public static String getDNS(NetworkUtils.DNS_TYPE type) {
		@SuppressWarnings("unused")
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
		
		
		
//		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//		String[] dhcpInfos = wifiManager.getDhcpInfo().toString().split(" ");
//		int i = 0;
//		
//		while (i++ < dhcpInfos.length) {
//		  if (dhcpInfos[i-1].equals("dns1")) {
//			  return dhcpInfos[i];
//		  }
//		}
		return null;
		
	}
}
