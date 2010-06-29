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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ScreenQoS  extends Screen {

	private CheckBox cbEnableSessionTimers;
	private RelativeLayout rlSessionTimers;
	private EditText etSessionTimeOut;
	private Spinner spRefresher;
	private Spinner spPrecondStrength;
	private Spinner spPrecondType;
	private Spinner spPrecondBandwidth;
	
	private final static String[] spinner_refresher_items = new String[] {Configuration.DEFAULT_QOS_REFRESHER, "Client", "Server"};
	private final static String[] spinner_precond_strength_items = new String[] {Configuration.DEFAULT_QOS_PRECOND_STRENGTH, "Optional", "Mandatory"};
	private final static String[] spinner_precond_type_items = new String[] {Configuration.DEFAULT_QOS_PRECOND_TYPE, "End-to-End"};
	private final static String[] spinner_precond_bandwidth_items = new String[] {Configuration.DEFAULT_QOS_PRECOND_BANDWIDTH, "Low", "Medium", "High"};
	
	private final IConfigurationService configurationService;
	private static final String TAG = ScreenQoS.class.getCanonicalName();
	
	public ScreenQoS() {
		super(SCREEN_TYPE.QOS_T, ScreenQoS.class.getCanonicalName());

		// Services
		this.configurationService = ServiceManager.getConfigurationService();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_qos);
		
		// get controls
		this.cbEnableSessionTimers = (CheckBox)this.findViewById(R.id.screen_qos_checkBox_sessiontimers);
		this.rlSessionTimers = (RelativeLayout)this.findViewById(R.id.screen_qos_relativeLayout_sessiontimers);
        this.etSessionTimeOut = (EditText)this.findViewById(R.id.screen_qos_editText_stimeout);
        this.spRefresher = (Spinner)this.findViewById(R.id.screen_qos_spinner_refresher);
        this.spPrecondStrength = (Spinner)this.findViewById(R.id.screen_qos_spinner_precond_strength);
        this.spPrecondType = (Spinner)this.findViewById(R.id.screen_qos_Spinner_precond_type);
        this.spPrecondBandwidth = (Spinner)this.findViewById(R.id.screen_qos_spinner_precond_bandwidth);
        
        // spinners
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ScreenQoS.spinner_refresher_items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spRefresher.setAdapter(adapter);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ScreenQoS.spinner_precond_strength_items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spPrecondStrength.setAdapter(adapter);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ScreenQoS.spinner_precond_type_items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spPrecondType.setAdapter(adapter);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ScreenQoS.spinner_precond_bandwidth_items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spPrecondBandwidth.setAdapter(adapter);
        
        
        // load values from configuration file (Do it before adding UI listeners)
        this.cbEnableSessionTimers.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.QOS, CONFIGURATION_ENTRY.SESSION_TIMERS, Configuration.DEFAULT_QOS_SESSION_TIMERS));
        this.etSessionTimeOut.setText(this.configurationService.getString(CONFIGURATION_SECTION.QOS, CONFIGURATION_ENTRY.SIP_CALLS_TIMEOUT, Integer.toString(Configuration.DEFAULT_QOS_SIP_CALLS_TIMEOUT)));
		this.spRefresher.setSelection(this.getSpinnerIndex(
				this.configurationService.getString(
						CONFIGURATION_SECTION.QOS,
						CONFIGURATION_ENTRY.REFRESHER,
						ScreenQoS.spinner_refresher_items[0]),
						ScreenQoS.spinner_refresher_items));
		this.spPrecondStrength.setSelection(this.getSpinnerIndex(
				this.configurationService.getString(
						CONFIGURATION_SECTION.QOS,
						CONFIGURATION_ENTRY.PRECOND_STRENGTH,
						ScreenQoS.spinner_precond_strength_items[0]),
						ScreenQoS.spinner_precond_strength_items));
		this.spPrecondType.setSelection(this.getSpinnerIndex(
				this.configurationService.getString(
						CONFIGURATION_SECTION.QOS,
						CONFIGURATION_ENTRY.PRECOND_TYPE,
						ScreenQoS.spinner_precond_type_items[0]),
						ScreenQoS.spinner_precond_type_items));
		this.spPrecondBandwidth.setSelection(this.getSpinnerIndex(
				this.configurationService.getString(
						CONFIGURATION_SECTION.QOS,
						CONFIGURATION_ENTRY.PRECOND_BANDWIDTH,
						ScreenQoS.spinner_precond_bandwidth_items[0]),
						ScreenQoS.spinner_precond_bandwidth_items));
		
		this.rlSessionTimers.setVisibility(this.cbEnableSessionTimers.isChecked() ? View.VISIBLE : View.INVISIBLE);
		
		// add listeners (for the configuration)
		/* this.addConfigurationListener(this.cbEnableSessionTimers); */
		this.addConfigurationListener(this.etSessionTimeOut);
		this.addConfigurationListener(this.spRefresher);
		this.addConfigurationListener(this.spPrecondStrength);
		this.addConfigurationListener(this.spPrecondType);
		this.addConfigurationListener(this.spPrecondBandwidth);
		
		// add local listeners
        this.cbEnableSessionTimers.setOnCheckedChangeListener(this.cbEnableSessionTimers_OnCheckedChangeListener);
	}
	
	protected void onPause() {
		if(this.computeConfiguration){
			
			this.configurationService.setBoolean(CONFIGURATION_SECTION.QOS, CONFIGURATION_ENTRY.SESSION_TIMERS,
					this.cbEnableSessionTimers.isChecked());
			this.configurationService.setString(CONFIGURATION_SECTION.QOS, CONFIGURATION_ENTRY.SIP_CALLS_TIMEOUT,
					this.etSessionTimeOut.getText().toString());
			this.configurationService.setString(CONFIGURATION_SECTION.QOS, CONFIGURATION_ENTRY.REFRESHER, 
					ScreenQoS.spinner_refresher_items[this.spRefresher.getSelectedItemPosition()]);
			this.configurationService.setString(CONFIGURATION_SECTION.QOS, CONFIGURATION_ENTRY.PRECOND_STRENGTH, 
					ScreenQoS.spinner_precond_strength_items[this.spPrecondStrength.getSelectedItemPosition()]);
			this.configurationService.setString(CONFIGURATION_SECTION.QOS, CONFIGURATION_ENTRY.PRECOND_TYPE, 
					ScreenQoS.spinner_precond_type_items[this.spPrecondType.getSelectedItemPosition()]);
			this.configurationService.setString(CONFIGURATION_SECTION.QOS, CONFIGURATION_ENTRY.PRECOND_BANDWIDTH, 
					ScreenQoS.spinner_precond_bandwidth_items[this.spPrecondBandwidth.getSelectedItemPosition()]);
			
			// Compute
			if(!this.configurationService.compute()){
				Log.e(ScreenQoS.TAG, "Failed to Compute() configuration");
			}
			
			this.computeConfiguration = false;
		}
		super.onPause();
	}	
	
	
	private OnCheckedChangeListener cbEnableSessionTimers_OnCheckedChangeListener = new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			ScreenQoS.this.rlSessionTimers.setVisibility(isChecked? View.VISIBLE : View.INVISIBLE);
			ScreenQoS.this.computeConfiguration = true;
		}
	};
}
