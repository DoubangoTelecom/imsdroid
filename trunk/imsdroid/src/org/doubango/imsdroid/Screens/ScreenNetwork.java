package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class ScreenNetwork extends Screen {

	private final IConfigurationService ConfigurationService;
	
	private Spinner spIPversion;
	private EditText etProxyHost;
	private EditText etProxyPort;
	private Spinner spTransport;
	private Spinner spProxyDiscovery;
	private CheckBox cbSigComp;
	
	private static String[] spinner_ipversion_items = new String[] {Configuration.DEFAULT_IP_VERSION, "IPv6"};
	private static String[] spinner_transport_items = new String[] {Configuration.DEFAULT_TRANSPORT.toUpperCase(), "TCP", "TLS", "SCTP"};
	private static String[] spinner_proxydiscovery_items = new String[] {Configuration.DEFAULT_PCSCF_DISCOVERY, "DNS NAPTR+SRV", "DHCPv4/v6", "Both"};
	
	public ScreenNetwork() {
		super(SCREEN_TYPE.NETWORK_T);
		
		this.ConfigurationService = ServiceManager.getConfigurationService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_network);
        
        // get controls
        this.spIPversion = (Spinner)this.findViewById(R.id.screen_network_spinner_ipversion);
        this.etProxyHost = (EditText)this.findViewById(R.id.screen_network_editText_pcscf_host);
        this.etProxyPort = (EditText)this.findViewById(R.id.screen_network_editText_pcscf_port);
        this.spTransport = (Spinner)this.findViewById(R.id.screen_network_spinner_transport);
        this.spProxyDiscovery = (Spinner)this.findViewById(R.id.screen_network_spinner_pcscf_discovery);
        this.cbSigComp = (CheckBox)this.findViewById(R.id.screen_network_checkBox_sigcomp);
        
        // spinners
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinner_ipversion_items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spIPversion.setAdapter(adapter);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinner_transport_items);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spTransport.setAdapter(adapter2);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinner_proxydiscovery_items);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spProxyDiscovery.setAdapter(adapter3);

        
        // load values from configuration file (Do it before adding UI listeners)
		this.spIPversion.setSelection(this.getSpinnerIndex(
				this.ConfigurationService.getString(
						CONFIGURATION_SECTION.NETWORK,
						CONFIGURATION_ENTRY.IP_VERSION,
						ScreenNetwork.spinner_ipversion_items[0]),
				ScreenNetwork.spinner_ipversion_items));
        this.etProxyHost.setText(this.ConfigurationService.getString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_HOST, Configuration.DEFAULT_PCSCF_HOST));
        this.etProxyPort.setText(this.ConfigurationService.getString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_PORT, Integer.toString(Configuration.DEFAULT_PCSCF_PORT)));
        this.spTransport.setSelection(this.getSpinnerIndex(
				this.ConfigurationService.getString(
						CONFIGURATION_SECTION.NETWORK,
						CONFIGURATION_ENTRY.TRANSPORT,
						ScreenNetwork.spinner_transport_items[0]),
				ScreenNetwork.spinner_transport_items));
        this.spProxyDiscovery.setSelection(this.getSpinnerIndex(
				this.ConfigurationService.getString(
						CONFIGURATION_SECTION.NETWORK,
						CONFIGURATION_ENTRY.PCSCF_DISCOVERY,
						ScreenNetwork.spinner_proxydiscovery_items[0]),
				ScreenNetwork.spinner_proxydiscovery_items));
        this.cbSigComp.setChecked(this.ConfigurationService.getBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.SIGCOMP, false));
        
        // add listeners (for the configuration)
        this.addConfigurationListener(this.spIPversion);
        this.addConfigurationListener(this.etProxyHost);
        this.addConfigurationListener(this.etProxyPort);
        this.addConfigurationListener(this.spTransport);
        this.addConfigurationListener(this.spProxyDiscovery);
        this.addConfigurationListener(this.cbSigComp);
	}
	
	protected void onPause() {
		if(this.computeConfiguration){
			
			this.ConfigurationService.setString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.IP_VERSION,
					ScreenNetwork.spinner_ipversion_items[this.spIPversion.getSelectedItemPosition()]);
			this.ConfigurationService.setString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_HOST, 
					this.etProxyHost.getText().toString());
			this.ConfigurationService.setString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_PORT, 
					this.etProxyPort.getText().toString());
			this.ConfigurationService.setString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.TRANSPORT, 
					ScreenNetwork.spinner_transport_items[this.spTransport.getSelectedItemPosition()]);
			this.ConfigurationService.setString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_DISCOVERY, 
					ScreenNetwork.spinner_proxydiscovery_items[this.spProxyDiscovery.getSelectedItemPosition()]);
			this.ConfigurationService.setBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.SIGCOMP, 
					this.cbSigComp.isChecked());
			
			// Compute
			if(!this.ConfigurationService.compute()){
				Log.e(this.getClass().getCanonicalName(), "Failed to Compute() configuration");
			}
			
			this.computeConfiguration = false;
		}
		super.onPause();
	}	
}
