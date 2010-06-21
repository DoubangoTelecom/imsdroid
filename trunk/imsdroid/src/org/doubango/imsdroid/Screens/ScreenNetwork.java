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

	private final IConfigurationService configurationService;
	
	private Spinner spIPversion;
	private EditText etProxyHost;
	private EditText etProxyPort;
	private Spinner spTransport;
	private Spinner spProxyDiscovery;
	private CheckBox cbSigComp;
	
	private final static String[] spinner_ipversion_items = new String[] {Configuration.DEFAULT_IP_VERSION, "IPv6"};
	private final static String[] spinner_transport_items = new String[] {Configuration.DEFAULT_TRANSPORT.toUpperCase(), "TCP", "TLS", "SCTP"};
	private final static String[] spinner_proxydiscovery_items = new String[] {Configuration.DEFAULT_PCSCF_DISCOVERY, "DNS NAPTR+SRV", "DHCPv4/v6", "Both"};
	
	public ScreenNetwork() {
		super(SCREEN_TYPE.NETWORK_T, ScreenNetwork.class.getCanonicalName());
		
		this.configurationService = ServiceManager.getConfigurationService();
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
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinner_transport_items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spTransport.setAdapter(adapter);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinner_proxydiscovery_items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spProxyDiscovery.setAdapter(adapter);

        
        // load values from configuration file (Do it before adding UI listeners)
		this.spIPversion.setSelection(this.getSpinnerIndex(
				this.configurationService.getString(
						CONFIGURATION_SECTION.NETWORK,
						CONFIGURATION_ENTRY.IP_VERSION,
						ScreenNetwork.spinner_ipversion_items[0]),
				ScreenNetwork.spinner_ipversion_items));
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
        this.cbSigComp.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.SIGCOMP, false));
        
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
			
			this.configurationService.setString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.IP_VERSION,
					ScreenNetwork.spinner_ipversion_items[this.spIPversion.getSelectedItemPosition()]);
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
			
			// Compute
			if(!this.configurationService.compute()){
				Log.e(this.getClass().getCanonicalName(), "Failed to Compute() configuration");
			}
			
			this.computeConfiguration = false;
		}
		super.onPause();
	}	
}
