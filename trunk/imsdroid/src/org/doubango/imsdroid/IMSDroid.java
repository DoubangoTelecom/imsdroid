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

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class IMSDroid extends Application {

    private static IMSDroid instance;
    private static PackageManager packageManager;
    private static String packageName;

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
}
