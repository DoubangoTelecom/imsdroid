package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Utils.ConfigurationUtils;
import org.doubango.imsdroid.Utils.ConfigurationUtils.ConfigurationEntry;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

public class ScreenNetwork extends BaseScreen {
	private final static String TAG = ScreenNetwork.class.getCanonicalName();
	
	private final IConfigurationService mConfigurationService;
	
	private EditText mEtProxyHost;
	private EditText mEtProxyPort;
	private Spinner mSpTransport;
	private Spinner mSpProxyDiscovery;
	private CheckBox mCbSigComp;
	private CheckBox mCbWiFi;
	private CheckBox mCb3G;
	private RadioButton mRbIPv4;
	private RadioButton mRbIPv6;
	
	private final static String[] sSpinnerTransportItems = new String[] {ConfigurationUtils.DEFAULT_NETWORK_TRANSPORT.toUpperCase(), "TCP", /*"TLS", "SCTP"*/};
	private final static String[] sSpinnerProxydiscoveryItems = new String[] {ConfigurationUtils.DEFAULT_NETWORK_PCSCF_DISCOVERY, ConfigurationUtils.PCSCF_DISCOVERY_DNS_SRV/*, "DHCPv4/v6", "Both"*/};
	
	public ScreenNetwork() {
		super(SCREEN_TYPE.NETWORK_T, TAG);
		
		this.mConfigurationService = ServiceManager.getConfigurationService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_network);
        
        mEtProxyHost = (EditText)findViewById(R.id.screen_network_editText_pcscf_host);
        mEtProxyPort = (EditText)findViewById(R.id.screen_network_editText_pcscf_port);
        mSpTransport = (Spinner)findViewById(R.id.screen_network_spinner_transport);
        mSpProxyDiscovery = (Spinner)findViewById(R.id.screen_network_spinner_pcscf_discovery);
        mCbSigComp = (CheckBox)findViewById(R.id.screen_network_checkBox_sigcomp);
        mCbWiFi = (CheckBox)findViewById(R.id.screen_network_checkBox_wifi);
        mCb3G = (CheckBox)findViewById(R.id.screen_network_checkBox_3g);
        mRbIPv4 = (RadioButton)findViewById(R.id.screen_network_radioButton_ipv4);
        mRbIPv6 = (RadioButton)findViewById(R.id.screen_network_radioButton_ipv6);
        
        // spinners
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sSpinnerTransportItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpTransport.setAdapter(adapter);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sSpinnerProxydiscoveryItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpProxyDiscovery.setAdapter(adapter);
        
        mEtProxyHost.setText(mConfigurationService.getString(ConfigurationEntry.NETWORK_PCSCF_HOST, ConfigurationUtils.DEFAULT_NETWORK_PCSCF_HOST));
        mEtProxyPort.setText(mConfigurationService.getString(ConfigurationEntry.NETWORK_PCSCF_PORT, Integer.toString(ConfigurationUtils.DEFAULT_NETWORK_PCSCF_PORT)));
        mSpTransport.setSelection(super.getSpinnerIndex(
				mConfigurationService.getString(ConfigurationEntry.NETWORK_TRANSPORT, sSpinnerTransportItems[0]),
				sSpinnerTransportItems));
        mSpProxyDiscovery.setSelection(super.getSpinnerIndex(
				mConfigurationService.getString(ConfigurationEntry.NETWORK_PCSCF_DISCOVERY, sSpinnerProxydiscoveryItems[0]),
				sSpinnerProxydiscoveryItems));
        mCbSigComp.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.NETWORK_USE_SIGCOMP, ConfigurationUtils.DEFAULT_NETWORK_USE_SIGCOMP));
        
        mCbWiFi.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.NETWORK_USE_WIFI, ConfigurationUtils.DEFAULT_NETWORK_USE_WIFI));
        mCb3G.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.NETWORK_USE_3G, ConfigurationUtils.DEFAULT_NETWORK_USE_3G));
        mRbIPv4.setChecked(mConfigurationService.getString(ConfigurationEntry.NETWORK_IP_VERSION,
				ConfigurationUtils.DEFAULT_NETWORK_IP_VERSION).equalsIgnoreCase("ipv4"));
        mRbIPv6.setChecked(!mRbIPv4.isChecked());
        
        // add listeners (for the configuration)
        super.addConfigurationListener(mEtProxyHost);
        super.addConfigurationListener(mEtProxyPort);
        super.addConfigurationListener(mSpTransport);
        super.addConfigurationListener(mSpProxyDiscovery);
        super.addConfigurationListener(mCbSigComp);
        super.addConfigurationListener(mCbWiFi);
        super.addConfigurationListener(mCb3G);
        super.addConfigurationListener(mRbIPv4);
        super.addConfigurationListener(mRbIPv6);
	}
	
	protected void onPause() {
		if(super.mComputeConfiguration){
			
			mConfigurationService.putString(ConfigurationEntry.NETWORK_PCSCF_HOST, 
					mEtProxyHost.getText().toString().trim());
			mConfigurationService.putString(ConfigurationEntry.NETWORK_PCSCF_PORT, 
					mEtProxyPort.getText().toString().trim());
			mConfigurationService.putString(ConfigurationEntry.NETWORK_TRANSPORT, 
					ScreenNetwork.sSpinnerTransportItems[mSpTransport.getSelectedItemPosition()]);
			mConfigurationService.putString(ConfigurationEntry.NETWORK_PCSCF_DISCOVERY, 
					ScreenNetwork.sSpinnerProxydiscoveryItems[mSpProxyDiscovery.getSelectedItemPosition()]);
			mConfigurationService.putBoolean(ConfigurationEntry.NETWORK_USE_SIGCOMP,  mCbSigComp.isChecked());
			mConfigurationService.putBoolean(ConfigurationEntry.NETWORK_USE_WIFI, 
					mCbWiFi.isChecked());
			mConfigurationService.putBoolean(ConfigurationEntry.NETWORK_USE_3G, 
					mCb3G.isChecked());
			mConfigurationService.putString(ConfigurationEntry.NETWORK_IP_VERSION, 
					mRbIPv4.isChecked() ? "ipv4" : "ipv6");
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}	
}
