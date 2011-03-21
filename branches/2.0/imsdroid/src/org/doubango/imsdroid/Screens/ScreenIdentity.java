package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;

import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;

public class ScreenIdentity  extends BaseScreen {
	private final static String TAG = ScreenIdentity.class.getCanonicalName();
	private final INgnConfigurationService mConfigurationService;
	
	private EditText mEtDisplayName;
	private EditText mEtIMPU;
	private EditText mEtIMPI;
	private EditText mEtPassword;
	private EditText mEtRealm;
	private CheckBox mCbEarlyIMS;
	
	public ScreenIdentity() {
		super(SCREEN_TYPE.IDENTITY_T, TAG);
		
		mConfigurationService = getEngine().getConfigurationService();
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
        
        mEtDisplayName.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME));
        mEtIMPU.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPU, NgnConfigurationEntry.DEFAULT_IDENTITY_IMPU));
        mEtIMPI.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI));
        mEtPassword.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_PASSWORD, NgnStringUtils.emptyValue()));
        mEtRealm.setText(mConfigurationService.getString(NgnConfigurationEntry.NETWORK_REALM, NgnConfigurationEntry.DEFAULT_NETWORK_REALM));
        mCbEarlyIMS.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS, NgnConfigurationEntry.DEFAULT_NETWORK_USE_EARLY_IMS));
        
        super.addConfigurationListener(mEtDisplayName);
        super.addConfigurationListener(mEtIMPU);
        super.addConfigurationListener(mEtIMPI);
        super.addConfigurationListener(mEtPassword);
        super.addConfigurationListener(mEtRealm);
        super.addConfigurationListener(mCbEarlyIMS);
	}	

	protected void onPause() {
		if(super.mComputeConfiguration){
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, 
					mEtDisplayName.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, 
					mEtIMPU.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, 
					mEtIMPI.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, 
					mEtPassword.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, 
					mEtRealm.getText().toString().trim());
			mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS, 
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
