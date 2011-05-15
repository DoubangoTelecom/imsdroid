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
package org.doubango.ngn.model;

import org.doubango.ngn.services.impl.NgnNetworkService;
import org.doubango.ngn.utils.NgnObservableObject;
import org.doubango.ngn.utils.NgnStringUtils;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

public class NgnAccessPoint extends NgnObservableObject {
	// Constants used for different security types
	public static final String AP_WPA2 = "WPA2";
	public static final String AP_WPA = "WPA";
	public static final String AP_WEP = "WEP";
	public static final String AP_OPEN = "Open";
	// For EAP Enterprise fields
	public static final String AP_WPA_EAP = "WPA-EAP";
	public static final String AP_IEEE8021X = "IEEE8021X";
	public static final String[] AP_SECURITY_MODES = { AP_WEP, AP_WPA, AP_WPA2, AP_WPA_EAP, AP_IEEE8021X };

	public static final int AP_WEP_PASSWORD_AUTO = 0;
	public static final int AP_WEP_PASSWORD_ASCII = 1;
	public static final int AP_WEP_PASSWORD_HEX = 2;
	
	private int mNetworkId;
	private String mSSID;
	private String mDescription;
	private boolean mConnected;
	private boolean mHasPassword;
	private boolean mOpen;
	private int mLevel;
	private WifiConfiguration mConf;
	private ScanResult mSR;
	
	public NgnAccessPoint(){
		mNetworkId = -1;
		mSSID = NgnStringUtils.emptyValue();
		mLevel = 0;
		mDescription = NgnStringUtils.emptyValue();
	}
	
	public NgnAccessPoint(int networkId, String SSID){
		this();
		mNetworkId = networkId;
		mSSID = SSID;
	}
	
	public NgnAccessPoint(ScanResult sr){
		this();
		if((mSR = sr) != null){
			mSSID = sr.SSID;
			mLevel = WifiManager.calculateSignalLevel(mSR.level,
					NgnNetworkService.sWifiSignalValues.length);
			final String rs = getScanResultSecurity(mSR);
			mOpen = NgnStringUtils.equals(rs, AP_OPEN, false);
			mDescription = String.format("%s;%s", 
					rs, 
					mSR.capabilities);
		}
	}
	
	public NgnAccessPoint(WifiConfiguration conf){
		this();
		if((mConf = conf) != null){
			mSSID = NgnStringUtils.unquote(mConf.SSID, "\"") ;
			mNetworkId = conf.networkId;
			mHasPassword = !NgnStringUtils.isNullOrEmpty(conf.preSharedKey);
		}
	}
	
	public WifiConfiguration getConf(){
		return mConf;
	}
	
	public ScanResult getSR(){
		return mSR;
	}
	
	public void setNetworkId(int networkId){
		mNetworkId = networkId;
	}
	
	public int getNetworkId(){
		return mNetworkId;
	}
	
	public String getSSID(){
		return mSSID;
	}
	
	public String getDescription(){
		return mDescription;
	}
	
	public int getLevel(){
		return mLevel;
	}
	
	public void setLevel(int level){
		if(mLevel != level){
			mLevel = level;
			super.setChangedAndNotifyObservers(this);
		}
	}
	
	public boolean isConfigured(){
		return (mNetworkId >= 0);
	}
	
	public void setConnected(boolean connected){
		if(mConnected != connected){
			mConnected = connected;
			super.setChangedAndNotifyObservers(this);
		}
	}
	
	public boolean isConnected(){
		return mConnected;
	}
	
	public boolean hasPassword(){
		return mHasPassword;
	}
	
	public boolean isOpen(){
		return mOpen;
	}
	
	public static String getScanResultSecurity(ScanResult scanResult) {
		final String cap = scanResult.capabilities;
		for (int i = NgnAccessPoint.AP_SECURITY_MODES.length - 1; i >= 0; i--) {
			if (cap.contains(NgnAccessPoint.AP_SECURITY_MODES[i])) {
				return NgnAccessPoint.AP_SECURITY_MODES[i];
			}
		}

		return NgnAccessPoint.AP_OPEN;
	}
}
