package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Utils.ConfigurationUtils;
import org.doubango.imsdroid.Utils.ConfigurationUtils.ConfigurationEntry;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

public class ScreenContacts  extends BaseScreen {
	private static String TAG = ScreenContacts.class.getCanonicalName();
	
	private RadioButton mRbLocal;
	private RadioButton mRbRemote;
	private EditText mEtXcapRoot;
	private EditText mEtXUI;
	private EditText mEtPassword;
	private RelativeLayout mRlRemote;
	
	private final IConfigurationService mConfigurationService;
	
	public ScreenContacts() {
		super(SCREEN_TYPE.CONTACTS_T, TAG);
		
		mConfigurationService = ServiceManager.getConfigurationService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_contacts);
        
        // get controls
        mRbLocal = (RadioButton)findViewById(R.id.screen_contacts_radioButton_local);
        mRbRemote = (RadioButton)findViewById(R.id.screen_contacts_radioButton_remote);
        mEtXcapRoot = (EditText)findViewById(R.id.screen_contacts_editText_xcaproot);
        mEtXUI = (EditText)findViewById(R.id.screen_contacts_editText_xui);
        mEtPassword = (EditText)findViewById(R.id.screen_contacts_editText_password);
        mRlRemote = (RelativeLayout)findViewById(R.id.screen_contacts_relativeLayout_remote);
        
        // load values from configuration file (Do it before adding UI listeners)
        mRbRemote.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.XCAP_ENABLED, ConfigurationUtils.DEFAULT_XCAP_ENABLED));
        //rbRemote.setChecked(!rbLocal.isChecked());
        mEtXcapRoot.setText(mConfigurationService.getString(ConfigurationEntry.XCAP_ROOT, ConfigurationUtils.DEFAULT_XCAP_ROOT));
        mEtXUI.setText(mConfigurationService.getString(ConfigurationEntry.XCAP_USERNAME, ConfigurationUtils.DEFAULT_XCAP_USERNAME));
        mEtPassword.setText(mConfigurationService.getString(ConfigurationEntry.XCAP_PASSWORD, ConfigurationUtils.DEFAULT_XCAP_PASSWORD));
        mRlRemote.setVisibility(mRbLocal.isChecked() ? View.INVISIBLE : View.VISIBLE);
        
        // add listeners (for the configuration)
        addConfigurationListener(mRbLocal);
        addConfigurationListener(mRbRemote);
        addConfigurationListener(mEtXcapRoot);
        addConfigurationListener(mEtXUI);
        addConfigurationListener(mEtPassword);
        
        mRbLocal.setOnCheckedChangeListener(rbLocal_OnCheckedChangeListener);
	}
	
	private OnCheckedChangeListener rbLocal_OnCheckedChangeListener = new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			mRlRemote.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);
		}
	};
	
	protected void onPause() {
		if(super.mComputeConfiguration){
			mConfigurationService.putBoolean(ConfigurationEntry.XCAP_ENABLED, mRbRemote.isChecked());
			mConfigurationService.putString(ConfigurationEntry.XCAP_ROOT, 
				mEtXcapRoot.getText().toString());
			mConfigurationService.putString(ConfigurationEntry.XCAP_USERNAME, 
				mEtXUI.getText().toString());
			mConfigurationService.putString(ConfigurationEntry.XCAP_PASSWORD, 
				mEtPassword.getText().toString());
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to Commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
}
