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

package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;

import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;

public class ScreenGeneral  extends Screen {
	
	private CheckBox cbFullScreenVideo;
	private CheckBox cbAutoStart;
	
	private final IConfigurationService configurationService;
	private final static String TAG = ScreenGeneral.class.getCanonicalName();
	
	public ScreenGeneral() {
		super(SCREEN_TYPE.GENERAL_T, ScreenGeneral.class.getCanonicalName());
		
		this.configurationService = ServiceManager.getConfigurationService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_general);
        
        this.cbFullScreenVideo = (CheckBox)this.findViewById(R.id.screen_general_checkBox_fullscreen);
        this.cbAutoStart = (CheckBox)this.findViewById(R.id.screen_general_checkBox_autoStart);
        
        this.cbFullScreenVideo.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.FULL_SCREEN_VIDEO, Configuration.DEFAULT_GENERAL_FULL_SCREEN_VIDEO));
        this.cbAutoStart.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.AUTOSTART, Configuration.DEFAULT_GENERAL_AUTOSTART));
        
        this.addConfigurationListener(this.cbFullScreenVideo);
        this.addConfigurationListener(this.cbFullScreenVideo);
	}
	
	protected void onPause() {
		if(this.computeConfiguration){
			
			this.configurationService.setBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.FULL_SCREEN_VIDEO, this.cbFullScreenVideo.isChecked());
			this.configurationService.setBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.AUTOSTART, this.cbAutoStart.isChecked());
	        
			// Compute
			if(!this.configurationService.compute()){
				Log.e(ScreenGeneral.TAG, "Failed to Compute() configuration");
			}
			
			this.computeConfiguration = false;
		}
		super.onPause();
	}
}
