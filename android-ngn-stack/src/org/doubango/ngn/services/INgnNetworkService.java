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
