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
package org.doubango.imsdroid;

import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class GlobalBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			SharedPreferences settings = context.getSharedPreferences(NgnConfigurationEntry.SHARED_PREF_NAME, 0);
			if (settings != null && settings.getBoolean(NgnConfigurationEntry.GENERAL_AUTOSTART.toString(), NgnConfigurationEntry.DEFAULT_GENERAL_AUTOSTART)) {
				Intent i = new Intent(context, NativeService.class);
				i.putExtra("autostarted", true);
				context.startService(i);
			}
		} else if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action) && Engine.getInstance().getSipService().isRegistered()) {
			final String number = getResultData();
			if (NgnStringUtils.isNullOrEmpty(number)) {
				return;
			}
			final boolean intercept = Engine.getInstance().getConfigurationService().getBoolean(NgnConfigurationEntry.GENERAL_INTERCEPT_OUTGOING_CALLS, NgnConfigurationEntry.DEFAULT_GENERAL_INTERCEPT_OUTGOING_CALLS);
			if (intercept) {
				//ServiceManager.getScreenService().bringToFront(Main.ACTION_INTERCEPT_OUTGOING_CALL, new String[] { "number", number });
				//setResultData(null);
				//return;
			}

			setResultData(number);
		}

	}
}