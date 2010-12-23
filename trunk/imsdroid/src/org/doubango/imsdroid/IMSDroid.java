/*
* Copyright (C) 2010 Mamadou Diop.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango.org>
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
*
*/

package org.doubango.imsdroid;

import org.doubango.imsdroid.utils.StringUtils;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

public class IMSDroid extends Application {

    private static IMSDroid instance;
    private static PackageManager packageManager;
    private static String packageName;
    private static String deviceURN;
    private static int sdkVersion;

    public IMSDroid() {
    	IMSDroid.instance = this;
    }

    public static Context getContext() {
        return IMSDroid.instance;
    }
    
    @Override
	public void onCreate() {
		super.onCreate();
		
		IMSDroid.packageManager = IMSDroid.instance.getPackageManager();    		
		IMSDroid.packageName = IMSDroid.instance.getPackageName();
	}
    
    public static int getSDKVersion(){
    	if(IMSDroid.sdkVersion == 0){
    		IMSDroid.sdkVersion = Integer.parseInt(Build.VERSION.SDK);
    	}
    	return IMSDroid.sdkVersion;
    }
    
	public static int getVersionCode(){
    	if(IMSDroid.packageManager != null){
    		try {
				return IMSDroid.packageManager.getPackageInfo(IMSDroid.packageName, 0).versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
    	}
    	return 0;
    }
    
    public static String getVersionName(){
    	if(IMSDroid.packageManager != null){
    		try {
				return IMSDroid.packageManager.getPackageInfo(IMSDroid.packageName, 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
    	}
    	return "0.0";
    }
    
    public static String getDeviceURN(){
    	if(StringUtils.isNullOrEmpty(IMSDroid.deviceURN)){
	    	try{
		    	TelephonyManager telephonyMgr = (TelephonyManager) IMSDroid.getContext().getSystemService(Context.TELEPHONY_SERVICE);
		        String msisdn = telephonyMgr.getLine1Number();
		        if(msisdn == null){
		        	IMSDroid.deviceURN = String.format("urn:imei:%s", telephonyMgr.getDeviceId());
		        }
		        else{
		        	IMSDroid.deviceURN = String.format("urn:tel:%s", msisdn);
		        }
	    	}
	    	catch(Exception e){
	    		Log.d("org.doubango.imsdroid", e.toString());
	    		IMSDroid.deviceURN = "urn:uuid:3ca50bcb-7a67-44f1-afd0-994a55f930f4";
	    	}
    	}
    	return IMSDroid.deviceURN;
    }
}
