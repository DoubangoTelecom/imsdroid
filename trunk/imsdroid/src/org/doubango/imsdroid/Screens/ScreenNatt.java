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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ScreenNatt extends Screen {

	private CheckBox cbHackAoR;
	private CheckBox cbEnableStun;
	private RelativeLayout rlEnableStun;
	private CheckBox cbEnableIce;
	private RelativeLayout rlStunServer;
	private RadioButton rbDiscoStun;
	private RadioButton rbSetStun;
	private EditText etStunServer;
	private EditText etStunPort;
	
	private final IConfigurationService configurationService;
	private final static String TAG = ScreenNatt.class.getCanonicalName();
	
	public  ScreenNatt() {
		super(SCREEN_TYPE.NATT_T);
		
		this.configurationService = ServiceManager.getConfigurationService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_natt);
        
        // get controls
        this.cbHackAoR = (CheckBox)this.findViewById(R.id.screen_natt_checkBox_hack_aor);
        this.cbEnableStun = (CheckBox)this.findViewById(R.id.screen_natt_checkBox_stun);
        this.rlEnableStun = (RelativeLayout)this.findViewById(R.id.screen_natt_relativeLayout_stun);
        this.cbEnableIce = (CheckBox)this.findViewById(R.id.screen_natt_checkBox_ice);
        this.rlStunServer = (RelativeLayout)this.findViewById(R.id.screen_natt_relativeLayout_stun_server);
        this.rbDiscoStun = (RadioButton)this.findViewById(R.id.screen_natt_radioButton_stun_disco);
        this.rbSetStun = (RadioButton)this.findViewById(R.id.screen_natt_radioButton_stun_set);
        this.etStunServer = (EditText)this.findViewById(R.id.screen_natt_editText_stun_server);
        this.etStunPort = (EditText)this.findViewById(R.id.screen_natt_editText_stun_port);
        
        // load values from configuration file (do it before adding UI listeners)
        this.cbHackAoR.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.HACK_AOR, Configuration.DEFAULT_NATT_HACK_AOR));
        this.cbEnableStun.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.USE_STUN, Configuration.DEFAULT_NATT_USE_STUN));
        this.cbEnableIce.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.USE_ICE, Configuration.DEFAULT_NATT_USE_ICE));
        this.rbDiscoStun.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.STUN_DISCO, Configuration.DEFAULT_NATT_STUN_DISCO));
        this.etStunServer.setText(this.configurationService.getString(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.STUN_SERVER, Configuration.DEFAULT_NATT_STUN_SERVER));
        this.etStunPort.setText(this.configurationService.getString(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.STUN_PORT, Integer.toString(Configuration.DEFAULT_NATT_STUN_PORT)));
        
        this.rlEnableStun.setVisibility(this.cbEnableStun.isChecked() ? View.VISIBLE : View.INVISIBLE);
        this.rlStunServer.setVisibility(this.rbSetStun.isChecked() ? View.VISIBLE : View.INVISIBLE);
        
        
        this.addConfigurationListener(this.cbHackAoR);
        /* this.addConfigurationListener(this.cbEnableStun); */
        this.addConfigurationListener(this.cbEnableIce);
        /* this.addConfigurationListener(this.rbDiscoStun);
        this.addConfigurationListener(this.rbSetStun); */
        this.addConfigurationListener(this.etStunServer);
        this.addConfigurationListener(this.etStunPort);
        
        
        this.cbEnableStun.setOnCheckedChangeListener(this.cbEnableStun_OnCheckedChangeListener);
        this.rbSetStun.setOnCheckedChangeListener(this.rbSetStun_OnCheckedChangeListener);
	}

	protected void onPause() {
		if(this.computeConfiguration){
			
			this.configurationService.setBoolean(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.HACK_AOR, this.cbHackAoR.isChecked());
			this.configurationService.setBoolean(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.USE_STUN, this.cbEnableStun.isChecked());
			this.configurationService.setBoolean(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.USE_ICE, this.cbEnableIce.isChecked());
			this.configurationService.setBoolean(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.STUN_DISCO, this.rbDiscoStun.isChecked());
			this.configurationService.setString(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.STUN_SERVER, this.etStunServer.getText().toString());
			this.configurationService.setString(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.STUN_PORT, this.etStunPort.getText().toString());
	        
			// Compute
			if(!this.configurationService.compute()){
				Log.e(ScreenNatt.TAG, "Failed to Compute() configuration");
			}
			
			this.computeConfiguration = false;
		}
		super.onPause();
	}
	
	private OnCheckedChangeListener cbEnableStun_OnCheckedChangeListener = new OnCheckedChangeListener(){

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			ScreenNatt.this.rlEnableStun.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
			ScreenNatt.this.computeConfiguration = true;
		}
	};
	
	private OnCheckedChangeListener rbSetStun_OnCheckedChangeListener = new OnCheckedChangeListener(){

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			ScreenNatt.this.rlStunServer.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
			ScreenNatt.this.computeConfiguration = true;
		}
	};
}
