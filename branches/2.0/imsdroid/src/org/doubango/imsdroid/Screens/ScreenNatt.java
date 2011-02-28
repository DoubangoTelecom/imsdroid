package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Utils.ConfigurationUtils;
import org.doubango.imsdroid.Utils.ConfigurationUtils.ConfigurationEntry;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

public class ScreenNatt extends BaseScreen {
	private final static String TAG = ScreenNatt.class.getCanonicalName();
	
	private CheckBox mCbHackAoR;
	private CheckBox mCbEnableStun;
	private RelativeLayout mRlEnableStun;
	private CheckBox mCbEnableIce;
	private RelativeLayout mRlStunServer;
	private RadioButton mRbDiscoStun;
	private RadioButton mRbSetStun;
	private EditText mEtStunServer;
	private EditText mEtStunPort;
	
	private final IConfigurationService mConfigurationService;
	
	public  ScreenNatt() {
		super(SCREEN_TYPE.NATT_T, TAG);
		
		mConfigurationService = ServiceManager.getConfigurationService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_natt);
        
        // get controls
        mCbHackAoR = (CheckBox)findViewById(R.id.screen_natt_checkBox_hack_aor);
        mCbEnableStun = (CheckBox)findViewById(R.id.screen_natt_checkBox_stun);
        mRlEnableStun = (RelativeLayout)findViewById(R.id.screen_natt_relativeLayout_stun);
        mCbEnableIce = (CheckBox)findViewById(R.id.screen_natt_checkBox_ice);
        mRlStunServer = (RelativeLayout)findViewById(R.id.screen_natt_relativeLayout_stun_server);
        mRbDiscoStun = (RadioButton)findViewById(R.id.screen_natt_radioButton_stun_disco);
        mRbSetStun = (RadioButton)findViewById(R.id.screen_natt_radioButton_stun_set);
        mEtStunServer = (EditText)findViewById(R.id.screen_natt_editText_stun_server);
        mEtStunPort = (EditText)findViewById(R.id.screen_natt_editText_stun_port);
        
        // load values from configuration file (do it before adding UI listeners)
        mCbHackAoR.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.NATT_HACK_AOR, ConfigurationUtils.DEFAULT_NATT_HACK_AOR));
        mCbEnableStun.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.NATT_USE_STUN, ConfigurationUtils.DEFAULT_NATT_USE_STUN));
        mCbEnableIce.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.NATT_USE_ICE, ConfigurationUtils.DEFAULT_NATT_USE_ICE));
        mRbDiscoStun.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.NATT_STUN_DISCO, ConfigurationUtils.DEFAULT_NATT_STUN_DISCO));
        mEtStunServer.setText(mConfigurationService.getString(ConfigurationEntry.NATT_STUN_SERVER, ConfigurationUtils.DEFAULT_NATT_STUN_SERVER));
        mEtStunPort.setText(mConfigurationService.getString(ConfigurationEntry.NATT_STUN_PORT, Integer.toString(ConfigurationUtils.DEFAULT_NATT_STUN_PORT)));
        
        mRlEnableStun.setVisibility(mCbEnableStun.isChecked() ? View.VISIBLE : View.INVISIBLE);
        mRlStunServer.setVisibility(mRbSetStun.isChecked() ? View.VISIBLE : View.INVISIBLE);
        
        
        addConfigurationListener(mCbHackAoR);
        /* addConfigurationListener(cbEnableStun); */
        addConfigurationListener(mCbEnableIce);
        /* addConfigurationListener(rbDiscoStun);
        addConfigurationListener(rbSetStun); */
        addConfigurationListener(mEtStunServer);
        addConfigurationListener(mEtStunPort);
        
        
        mCbEnableStun.setOnCheckedChangeListener(mCbEnableStun_OnCheckedChangeListener);
        mRbSetStun.setOnCheckedChangeListener(mRbSetStun_OnCheckedChangeListener);
	}

	protected void onPause() {
		if(super.mComputeConfiguration){
			
			mConfigurationService.putBoolean(ConfigurationEntry.NATT_HACK_AOR, mCbHackAoR.isChecked());
			mConfigurationService.putBoolean(ConfigurationEntry.NATT_USE_STUN, mCbEnableStun.isChecked());
			mConfigurationService.putBoolean(ConfigurationEntry.NATT_USE_ICE, mCbEnableIce.isChecked());
			mConfigurationService.putBoolean(ConfigurationEntry.NATT_STUN_DISCO, mRbDiscoStun.isChecked());
			mConfigurationService.putString(ConfigurationEntry.NATT_STUN_SERVER, mEtStunServer.getText().toString());
			mConfigurationService.putString(ConfigurationEntry.NATT_STUN_PORT, mEtStunPort.getText().toString());
	        
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
	
	private OnCheckedChangeListener mCbEnableStun_OnCheckedChangeListener = new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			mRlEnableStun.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
			mComputeConfiguration = true;
		}
	};
	
	private OnCheckedChangeListener mRbSetStun_OnCheckedChangeListener = new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			mRlStunServer.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
			mComputeConfiguration = true;
		}
	};
}
