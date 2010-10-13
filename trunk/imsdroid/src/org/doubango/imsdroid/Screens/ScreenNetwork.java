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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

public class ScreenNetwork extends Screen {

	private final IConfigurationService configurationService;
	
	private EditText etProxyHost;
	private EditText etProxyPort;
	private Spinner spTransport;
	private Spinner spProxyDiscovery;
	private CheckBox cbSigComp;
	private CheckBox cbWiFi;
	private CheckBox cb3G;
	private RadioButton rbIPv4;
	private RadioButton rbIPv6;
	
	private final static String[] spinner_transport_items = new String[] {Configuration.DEFAULT_TRANSPORT.toUpperCase(), "TCP", /*"TLS", "SCTP"*/};
	private final static String[] spinner_proxydiscovery_items = new String[] {Configuration.DEFAULT_PCSCF_DISCOVERY, Configuration.PCSCF_DISCOVERY_DNS/*, "DHCPv4/v6", "Both"*/};
	
	public ScreenNetwork() {
		super(SCREEN_TYPE.NETWORK_T, ScreenNetwork.class.getCanonicalName());
		
		this.configurationService = ServiceManager.getConfigurationService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_network);
        
        // get controls
        this.etProxyHost = (EditText)this.findViewById(R.id.screen_network_editText_pcscf_host);
        this.etProxyPort = (EditText)this.findViewById(R.id.screen_network_editText_pcscf_port);
        this.spTransport = (Spinner)this.findViewById(R.id.screen_network_spinner_transport);
        this.spProxyDiscovery = (Spinner)this.findViewById(R.id.screen_network_spinner_pcscf_discovery);
        this.cbSigComp = (CheckBox)this.findViewById(R.id.screen_network_checkBox_sigcomp);
        this.cbWiFi = (CheckBox)this.findViewById(R.id.screen_network_checkBox_wifi);
        this.cb3G = (CheckBox)this.findViewById(R.id.screen_network_checkBox_3g);
        this.rbIPv4 = (RadioButton)this.findViewById(R.id.screen_network_radioButton_ipv4);
        this.rbIPv6 = (RadioButton)this.findViewById(R.id.screen_network_radioButton_ipv6);
        
        // spinners
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinner_transport_items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spTransport.setAdapter(adapter);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinner_proxydiscovery_items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spProxyDiscovery.setAdapter(adapter);

        
        // load values from configuration file (Do it before adding UI listeners)
        this.etProxyHost.setText(this.configurationService.getString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_HOST, Configuration.DEFAULT_PCSCF_HOST));
        this.etProxyPort.setText(this.configurationService.getString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_PORT, Integer.toString(Configuration.DEFAULT_PCSCF_PORT)));
        this.spTransport.setSelection(this.getSpinnerIndex(
				this.configurationService.getString(
						CONFIGURATION_SECTION.NETWORK,
						CONFIGURATION_ENTRY.TRANSPORT,
						ScreenNetwork.spinner_transport_items[0]),
				ScreenNetwork.spinner_transport_items));
        this.spProxyDiscovery.setSelection(this.getSpinnerIndex(
				this.configurationService.getString(
						CONFIGURATION_SECTION.NETWORK,
						CONFIGURATION_ENTRY.PCSCF_DISCOVERY,
						ScreenNetwork.spinner_proxydiscovery_items[0]),
				ScreenNetwork.spinner_proxydiscovery_items));
        this.cbSigComp.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.SIGCOMP, Configuration.DEFAULT_SIGCOMP));
        
        this.cbWiFi.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.WIFI, Configuration.DEFAULT_WIFI));
        this.cb3G.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.THREE_3G, Configuration.DEFAULT_3G));
        this.rbIPv4.setChecked(this.configurationService.getString(
				CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.IP_VERSION,
				Configuration.DEFAULT_IP_VERSION).equalsIgnoreCase("ipv4"));
        this.rbIPv6.setChecked(!this.rbIPv4.isChecked());
        
        // add listeners (for the configuration)
        this.addConfigurationListener(this.etProxyHost);
        this.addConfigurationListener(this.etProxyPort);
        this.addConfigurationListener(this.spTransport);
        this.addConfigurationListener(this.spProxyDiscovery);
        this.addConfigurationListener(this.cbSigComp);
        this.addConfigurationListener(this.cbWiFi);
        this.addConfigurationListener(this.cb3G);
        this.addConfigurationListener(this.rbIPv4);
        this.addConfigurationListener(this.rbIPv6);
	}
	
	protected void onPause() {
		if(this.computeConfiguration){
			
			this.configurationService.setString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_HOST, 
					this.etProxyHost.getText().toString());
			this.configurationService.setString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_PORT, 
					this.etProxyPort.getText().toString());
			this.configurationService.setString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.TRANSPORT, 
					ScreenNetwork.spinner_transport_items[this.spTransport.getSelectedItemPosition()]);
			this.configurationService.setString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_DISCOVERY, 
					ScreenNetwork.spinner_proxydiscovery_items[this.spProxyDiscovery.getSelectedItemPosition()]);
			this.configurationService.setBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.SIGCOMP, 
					this.cbSigComp.isChecked());
			this.configurationService.setBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.WIFI, 
					this.cbWiFi.isChecked());
			this.configurationService.setBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.THREE_3G, 
					this.cb3G.isChecked());
			this.configurationService.setString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.IP_VERSION, 
					this.rbIPv4.isChecked()?"ipv4":"ipv6");
			
			// Compute
			if(!this.configurationService.compute()){
				Log.e(this.getClass().getCanonicalName(), "Failed to Compute() configuration");
			}
			
			this.computeConfiguration = false;
		}
		super.onPause();
	}	
}
