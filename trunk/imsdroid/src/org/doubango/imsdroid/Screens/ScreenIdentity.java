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

public class ScreenIdentity  extends Screen {
	
	private final IConfigurationService ConfigurationService;
	
	private EditText etDisplayName;
	private EditText etIMPU;
	private EditText etIMPI;
	private EditText etPassword;
	private EditText etRealm;
	private CheckBox cbEarlyIMS;
	
	public ScreenIdentity() {
		super(SCREEN_TYPE.IDENTITY_T, ScreenIdentity.class.getCanonicalName());
		
		this.ConfigurationService = ServiceManager.getConfigurationService();
	}

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_identity);
        
        // get controls
        this.etDisplayName = (EditText)this.findViewById(R.id.screen_identity_editText_displayname);
        this.etIMPU = (EditText)this.findViewById(R.id.screen_identity_editText_impu);
        this.etIMPI = (EditText)this.findViewById(R.id.screen_identity_editText_impi);
        this.etPassword = (EditText)this.findViewById(R.id.screen_identity_editText_password);
        this.etRealm = (EditText)this.findViewById(R.id.screen_identity_editText_realm);
        this.cbEarlyIMS = (CheckBox)this.findViewById(R.id.screen_identity_checkBox_earlyIMS);
        
        // load values from configuration file (Do it before adding UI listeners)
        this.etDisplayName.setText(this.ConfigurationService.getString(CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.DISPLAY_NAME, Configuration.DEFAULT_DISPLAY_NAME));
        this.etIMPU.setText(this.ConfigurationService.getString(CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.IMPU, Configuration.DEFAULT_IMPU));
        this.etIMPI.setText(this.ConfigurationService.getString(CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.IMPI, Configuration.DEFAULT_IMPI));
        this.etPassword.setText(this.ConfigurationService.getString(CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.PASSWORD, ""));
        this.etRealm.setText(this.ConfigurationService.getString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.REALM, Configuration.DEFAULT_REALM));
        this.cbEarlyIMS.setChecked(this.ConfigurationService.getBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.EARLY_IMS, Configuration.DEFAULT_EARLY_IMS));
        
        // add listeners (for the configuration)
        this.addConfigurationListener(this.etDisplayName);
        this.addConfigurationListener(this.etIMPU);
        this.addConfigurationListener(this.etIMPI);
        this.addConfigurationListener(this.etPassword);
        this.addConfigurationListener(this.etRealm);
        this.addConfigurationListener(this.cbEarlyIMS);
	}	

	protected void onPause() {
		if(this.computeConfiguration){
			this.ConfigurationService.setString(CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.DISPLAY_NAME, 
				this.etDisplayName.getText().toString());
			this.ConfigurationService.setString(CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.IMPU, 
				this.etIMPU.getText().toString());
			this.ConfigurationService.setString(CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.IMPI, 
				this.etIMPI.getText().toString());
			this.ConfigurationService.setString(CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.PASSWORD, 
				this.etPassword.getText().toString());
			this.ConfigurationService.setString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.REALM, 
				this.etRealm.getText().toString());
			this.ConfigurationService.setBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.EARLY_IMS, 
					this.cbEarlyIMS.isChecked());
			
			// update main activity info
			ServiceManager.getMainActivity().setDisplayName(this.etDisplayName.getText().toString());
			
			// Compute
			if(!this.ConfigurationService.compute()){
				Log.e(this.getClass().getCanonicalName(), "Failed to Compute() configuration");
			}
			
			this.computeConfiguration = false;
		}
		super.onPause();
	}
}
