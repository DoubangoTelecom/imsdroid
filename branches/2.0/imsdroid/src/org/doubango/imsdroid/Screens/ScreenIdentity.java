package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Utils.ConfigurationUtils;
import org.doubango.imsdroid.Utils.ConfigurationUtils.ConfigurationEntry;
import org.doubango.imsdroid.Utils.StringUtils;

import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;

public class ScreenIdentity  extends BaseScreen {
	private final static String TAG = ScreenIdentity.class.getCanonicalName();
	private final IConfigurationService mConfigurationService;
	
	private EditText mEtDisplayName;
	private EditText mEtIMPU;
	private EditText mEtIMPI;
	private EditText mEtPassword;
	private EditText mEtRealm;
	private CheckBox mCbEarlyIMS;
	
	public ScreenIdentity() {
		super(SCREEN_TYPE.IDENTITY_T, TAG);
		
		mConfigurationService = ServiceManager.getConfigurationService();
	}

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_identity);
        
        mEtDisplayName = (EditText)findViewById(R.id.screen_identity_editText_displayname);
        mEtIMPU = (EditText)findViewById(R.id.screen_identity_editText_impu);
        mEtIMPI = (EditText)findViewById(R.id.screen_identity_editText_impi);
        mEtPassword = (EditText)findViewById(R.id.screen_identity_editText_password);
        mEtRealm = (EditText)findViewById(R.id.screen_identity_editText_realm);
        mCbEarlyIMS = (CheckBox)findViewById(R.id.screen_identity_checkBox_earlyIMS);
        
        mEtDisplayName.setText(mConfigurationService.getString(ConfigurationEntry.IDENTITY_DISPLAY_NAME, ConfigurationUtils.DEFAULT_IDENTITY_DISPLAY_NAME));
        mEtIMPU.setText(mConfigurationService.getString(ConfigurationEntry.IDENTITY_IMPU, ConfigurationUtils.DEFAULT_IDENTITY_IMPU));
        mEtIMPI.setText(mConfigurationService.getString(ConfigurationEntry.IDENTITY_IMPI, ConfigurationUtils.DEFAULT_IDENTITY_IMPI));
        mEtPassword.setText(mConfigurationService.getString(ConfigurationEntry.IDENTITY_PASSWORD, StringUtils.emptyValue()));
        mEtRealm.setText(mConfigurationService.getString(ConfigurationEntry.NETWORK_REALM, ConfigurationUtils.DEFAULT_NETWORK_REALM));
        mCbEarlyIMS.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.NETWORK_USE_EARLY_IMS, ConfigurationUtils.DEFAULT_NETWORK_USE_EARLY_IMS));
        
        super.addConfigurationListener(mEtDisplayName);
        super.addConfigurationListener(mEtIMPU);
        super.addConfigurationListener(mEtIMPI);
        super.addConfigurationListener(mEtPassword);
        super.addConfigurationListener(mEtRealm);
        super.addConfigurationListener(mCbEarlyIMS);
	}	

	protected void onPause() {
		if(super.mComputeConfiguration){
			mConfigurationService.putString(ConfigurationEntry.IDENTITY_DISPLAY_NAME, 
					mEtDisplayName.getText().toString().trim());
			mConfigurationService.putString(ConfigurationEntry.IDENTITY_IMPU, 
					mEtIMPU.getText().toString().trim());
			mConfigurationService.putString(ConfigurationEntry.IDENTITY_IMPI, 
					mEtIMPI.getText().toString().trim());
			mConfigurationService.putString(ConfigurationEntry.IDENTITY_PASSWORD, 
					mEtPassword.getText().toString().trim());
			mConfigurationService.putString(ConfigurationEntry.NETWORK_REALM, 
					mEtRealm.getText().toString().trim());
			mConfigurationService.putBoolean(ConfigurationEntry.NETWORK_USE_EARLY_IMS, 
					mCbEarlyIMS.isChecked());
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to Commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
}
