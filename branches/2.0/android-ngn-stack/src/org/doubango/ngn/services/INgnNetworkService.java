package org.doubango.ngn.services;

import org.doubango.ngn.model.NgnAccessPoint;
import org.doubango.ngn.services.impl.NgnNetworkService.DNS_TYPE;
import org.doubango.ngn.utils.NgnObservableList;

public interface INgnNetworkService extends INgnBaseService{
	String getDnsServer(DNS_TYPE type);
	String getLocalIP(boolean ipv6);
	boolean isScanning();
	boolean setNetworkEnabledAndRegister();
	boolean setNetworkEnabled(String SSID, boolean enabled, boolean force);
	boolean setNetworkEnabled(int networkId, boolean enabled, boolean force);
	boolean forceConnectToNetwork();
	NgnObservableList<NgnAccessPoint> getAccessPoints();
	int configure(NgnAccessPoint ap, String password, boolean bHex);
	boolean scan();
	boolean acquire();
	boolean release();
}
