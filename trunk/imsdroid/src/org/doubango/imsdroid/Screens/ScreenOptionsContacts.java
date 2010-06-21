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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ScreenOptionsContacts  extends Screen {

	private RadioButton rbLocal;
	private RadioButton rbRemote;
	private EditText etXcapRoot;
	private EditText etXUI;
	private EditText etPassword;
	private RelativeLayout rlRemote;
	
	private final IConfigurationService configurationService;
	
	public ScreenOptionsContacts() {
		super(SCREEN_TYPE.CONTACTS_OPTIONS_T, ScreenOptionsContacts.class.getCanonicalName());
		
		this.configurationService = ServiceManager.getConfigurationService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_options_contacts);
        
        // get controls
        this.rbLocal = (RadioButton)this.findViewById(R.id.screen_options_contacts_radioButton_local);
        this.rbRemote = (RadioButton)this.findViewById(R.id.screen_options_contacts_radioButton_remote);
        this.etXcapRoot = (EditText)this.findViewById(R.id.screen_options_contacts_editText_xcaproot);
        this.etXUI = (EditText)this.findViewById(R.id.screen_options_contacts_editText_xui);
        this.etPassword = (EditText)this.findViewById(R.id.screen_options_contacts_editText_password);
        this.rlRemote = (RelativeLayout)this.findViewById(R.id.screen_options_contacts_relativeLayout_remote);
        
        // load values from configuration file (Do it before adding UI listeners)
        this.rbRemote.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.ENABLED, Configuration.DEFAULT_XCAP_ENABLED));
        //this.rbRemote.setChecked(!this.rbLocal.isChecked());
        this.etXcapRoot.setText(this.configurationService.getString(CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.XCAP_ROOT, Configuration.DEFAULT_XCAP_ROOT));
        this.etXUI.setText(this.configurationService.getString(CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.USERNAME, Configuration.DEFAULT_XUI));
        this.etPassword.setText(this.configurationService.getString(CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.PASSWORD, ""));
        this.rlRemote.setVisibility(this.rbLocal.isChecked() ? View.INVISIBLE : View.VISIBLE);
        
        // add listeners (for the configuration)
        this.addConfigurationListener(this.rbLocal);
        this.addConfigurationListener(this.rbRemote);
        this.addConfigurationListener(this.etXcapRoot);
        this.addConfigurationListener(this.etXUI);
        this.addConfigurationListener(this.etPassword);
        
        this.rbLocal.setOnCheckedChangeListener(this.rbLocal_OnCheckedChangeListener);
	}
	
	private OnCheckedChangeListener rbLocal_OnCheckedChangeListener = new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			ScreenOptionsContacts.this.rlRemote.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);
		}
	};
	
	protected void onPause() {
		if(this.computeConfiguration){
			this.configurationService.setBoolean(CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.ENABLED, this.rbRemote.isChecked());
			this.configurationService.setString(CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.XCAP_ROOT, 
				this.etXcapRoot.getText().toString());
			this.configurationService.setString(CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.USERNAME, 
				this.etXUI.getText().toString());
			this.configurationService.setString(CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.PASSWORD, 
				this.etPassword.getText().toString());
			
			// Compute
			if(!this.configurationService.compute()){
				Log.e(this.getClass().getCanonicalName(), "Failed to Compute() configuration");
			}
			
			this.computeConfiguration = false;
		}
		super.onPause();
	}
}
