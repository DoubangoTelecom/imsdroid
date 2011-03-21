package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;

import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;

public class ScreenMessaging  extends BaseScreen {
	private final static String TAG = ScreenMessaging.class.getCanonicalName();
	
	private EditText mEtConferenceFactory;
	private EditText mEtSMSC;
	private CheckBox mCbBinarySMS;
	private CheckBox mCbMsrpSuccessReports;
	private CheckBox mCbMsrpFailureReports;
	private CheckBox mCbMsrpOMFDR;
	private CheckBox mCbMWI;
	
	private final INgnConfigurationService mConfigurationService;
	
	
	public ScreenMessaging() {
		super(SCREEN_TYPE.MESSAGING_T, TAG);
		
		mConfigurationService = getEngine().getConfigurationService();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_messaging);
		
		// get controls
        mEtConferenceFactory = (EditText)findViewById(R.id.screen_messaging_editText_conf_fact);
        mEtSMSC = (EditText)findViewById(R.id.screen_messaging_editText_psi);
        mCbBinarySMS = (CheckBox)findViewById(R.id.screen_messaging_checkBox_binary_sms);
        mCbMsrpSuccessReports = (CheckBox)findViewById(R.id.screen_messaging_checkBox_msrp_success);
        mCbMsrpFailureReports = (CheckBox)findViewById(R.id.screen_messaging_checkBox_msrp_failure);
        mCbMsrpOMFDR = (CheckBox)findViewById(R.id.screen_messaging_checkBox_ofdr);
        mCbMWI = (CheckBox)findViewById(R.id.screen_messaging_checkBox_mwi);
        
        // load values from configuration file (do it before adding UI listeners)
        mEtConferenceFactory.setText(mConfigurationService.getString(NgnConfigurationEntry.RCS_CONF_FACT, NgnConfigurationEntry.DEFAULT_RCS_CONF_FACT));
        mEtSMSC.setText(mConfigurationService.getString(NgnConfigurationEntry.RCS_SMSC, NgnConfigurationEntry.DEFAULT_RCS_SMSC));
        mCbBinarySMS.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.RCS_USE_BINARY_SMS, NgnConfigurationEntry.DEFAULT_RCS_USE_BINARY_SMS));
        mCbMsrpSuccessReports.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.RCS_USE_MSRP_SUCCESS, NgnConfigurationEntry.DEFAULT_RCS_USE_MSRP_SUCCESS));
        mCbMsrpFailureReports.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.RCS_USE_MSRP_FAILURE, NgnConfigurationEntry.DEFAULT_RCS_USE_MSRP_FAILURE));
        mCbMsrpOMFDR.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.RCS_USE_OMAFDR, NgnConfigurationEntry.DEFAULT_RCS_USE_OMAFDR));
        mCbMWI.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.RCS_USE_MWI, NgnConfigurationEntry.DEFAULT_RCS_USE_MWI));
        
        addConfigurationListener(mEtConferenceFactory);
        addConfigurationListener(mEtSMSC);
        addConfigurationListener(mCbBinarySMS);
        addConfigurationListener(mCbMsrpSuccessReports);
        addConfigurationListener(mCbMsrpFailureReports);
        addConfigurationListener(mCbMsrpOMFDR);
        addConfigurationListener(mCbMWI);
	}
	
	@Override
	protected void onPause() {
		if(super.mComputeConfiguration){
			
			mConfigurationService.putString(NgnConfigurationEntry.RCS_CONF_FACT, mEtConferenceFactory.getText().toString());
	        mConfigurationService.putString(NgnConfigurationEntry.RCS_SMSC, mEtSMSC.getText().toString());
	        mConfigurationService.putBoolean(NgnConfigurationEntry.RCS_USE_BINARY_SMS, mCbBinarySMS.isChecked());
	        mConfigurationService.putBoolean(NgnConfigurationEntry.RCS_USE_MSRP_SUCCESS, mCbMsrpSuccessReports.isChecked());
	        mConfigurationService.putBoolean(NgnConfigurationEntry.RCS_USE_MSRP_FAILURE, mCbMsrpFailureReports.isChecked());
	        mConfigurationService.putBoolean(NgnConfigurationEntry.RCS_USE_OMAFDR, mCbMsrpOMFDR.isChecked());
	        mConfigurationService.putBoolean(NgnConfigurationEntry.RCS_USE_MWI, mCbMWI.isChecked());
	        
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
}
