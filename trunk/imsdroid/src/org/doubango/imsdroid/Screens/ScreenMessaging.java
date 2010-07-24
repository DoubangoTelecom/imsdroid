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
import org.doubango.imsdroid.Services.Impl.ServiceManager;

import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;

public class ScreenMessaging  extends Screen {

	private EditText etConferenceFactory;
	private EditText etSMSC;
	private CheckBox cbBinarySMS;
	private CheckBox cbMsrpSuccessReports;
	private CheckBox cbMsrpFailureReports;
	private CheckBox cbMsrpOMFDR;
	private CheckBox cbMWI;
	
	private final IConfigurationService configurationService;
	private final static String TAG = ScreenMessaging.class.getCanonicalName();
	
	public ScreenMessaging() {
		super(SCREEN_TYPE.MESSAGING_T, ScreenMessaging.class.getCanonicalName());

		// Services
		this.configurationService = ServiceManager.getConfigurationService();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_messaging);
		
		// get controls
        this.etConferenceFactory = (EditText)this.findViewById(R.id.screen_messaging_editText_conf_fact);
        this.etSMSC = (EditText)this.findViewById(R.id.screen_messaging_editText_psi);
        this.cbBinarySMS = (CheckBox)this.findViewById(R.id.screen_messaging_checkBox_binary_sms);
        this.cbMsrpSuccessReports = (CheckBox)this.findViewById(R.id.screen_messaging_checkBox_msrp_success);
        this.cbMsrpFailureReports = (CheckBox)this.findViewById(R.id.screen_messaging_checkBox_msrp_failure);
        this.cbMsrpOMFDR = (CheckBox)this.findViewById(R.id.screen_messaging_checkBox_ofdr);
        this.cbMWI = (CheckBox)this.findViewById(R.id.screen_messaging_checkBox_mwi);
        
        // load values from configuration file (do it before adding UI listeners)
        this.etConferenceFactory.setText(this.configurationService.getString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.CONF_FACT, Configuration.DEFAULT_RCS_CONF_FACT));
        this.etSMSC.setText(this.configurationService.getString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.SMSC, Configuration.DEFAULT_RCS_SMSC));
        this.cbBinarySMS.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.BINARY_SMS, Configuration.DEFAULT_RCS_BINARY_SMS));
        this.cbMsrpSuccessReports.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.MSRP_SUCCESS, Configuration.DEFAULT_RCS_MSRP_SUCCESS));
        this.cbMsrpFailureReports.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.MSRP_FAILURE, Configuration.DEFAULT_RCS_MSRP_FAILURE));
        this.cbMsrpOMFDR.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.OMAFDR, Configuration.DEFAULT_RCS_OMAFDR));
        this.cbMWI.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.MWI, Configuration.DEFAULT_RCS_MWI));
        
        this.addConfigurationListener(this.etConferenceFactory);
        this.addConfigurationListener(this.etSMSC);
        this.addConfigurationListener(this.cbBinarySMS);
        this.addConfigurationListener(this.cbMsrpSuccessReports);
        this.addConfigurationListener(this.cbMsrpFailureReports);
        this.addConfigurationListener(this.cbMsrpOMFDR);
        this.addConfigurationListener(this.cbMWI);
	}
	
	protected void onPause() {
		if(this.computeConfiguration){
			
			this.configurationService.setString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.CONF_FACT, this.etConferenceFactory.getText().toString());
	        this.configurationService.setString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.SMSC, this.etSMSC.getText().toString());
	        this.configurationService.setBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.BINARY_SMS, this.cbBinarySMS.isChecked());
	        this.configurationService.setBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.MSRP_SUCCESS, this.cbMsrpSuccessReports.isChecked());
	        this.configurationService.setBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.MSRP_FAILURE, this.cbMsrpFailureReports.isChecked());
	        this.configurationService.setBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.OMAFDR, this.cbMsrpOMFDR.isChecked());
	        this.configurationService.setBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.MWI, this.cbMWI.isChecked());
	        
			// Compute
			if(!this.configurationService.compute()){
				Log.e(ScreenMessaging.TAG, "Failed to Compute() configuration");
			}
			
			this.computeConfiguration = false;
		}
		super.onPause();
	}
}
