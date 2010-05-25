package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.Sevices.Impl.NetworkService.DNS_TYPE;

public interface INetworkService  extends IService{
	String getDnsServer(DNS_TYPE type);
	String getLocalIP(boolean ipv6);
}
