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
package org.doubango.imsdroid.Services.Impl;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.Main;
import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.utils.StringUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ServiceManagerReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		String action = intent.getAction();
		
		if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
			SharedPreferences settings = context.getSharedPreferences(IMSDroid.getContext().getPackageName(), 0);
			 if(settings != null && settings.getBoolean("autostarts", Configuration.DEFAULT_GENERAL_AUTOSTART)){
					Intent i = new Intent(context, ServiceManager.class);
					i.putExtra("autostarted", true);
					context.startService(i);
			 }
		}
		else if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action) && ServiceManager.getSipService().isRegistered()) {
			String number = getResultData();
			if(StringUtils.isNullOrEmpty(number)){
				return;
			}
			
			boolean intercept = ServiceManager.getConfigurationService().getBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.INTERCEPT_OUTGOING_CALLS, Configuration.DEFAULT_GENERAL_INTERCEPT_OUTGOING_CALLS);
			if(intercept){
				ServiceManager.getScreenService().bringToFront(Main.ACTION_INTERCEPT_OUTGOING_CALL,
						new String[] {"number", number}
				);
				
				setResultData(null);
				return;
			}
			
			setResultData(number);
		}
		
		 
	}
}