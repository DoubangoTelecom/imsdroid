
package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.Services.Impl.NetworkService.DNS_TYPE;



public interface INetworkService extends IBaseService{
	String getDnsServer(DNS_TYPE type);
	String getLocalIP(boolean ipv6);
	boolean setNetworkEnabledAndRegister();
	boolean setNetworkEnabled(String SSID, boolean enabled);
	boolean forceConnectToNetwork();
	boolean acquire();
	boolean release();
}
