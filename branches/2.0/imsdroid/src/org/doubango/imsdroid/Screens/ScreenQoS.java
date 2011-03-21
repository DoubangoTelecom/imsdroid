package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.tinyWRAP.tmedia_qos_strength_t;
import org.doubango.tinyWRAP.tmedia_qos_stype_t;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

public class ScreenQoS  extends BaseScreen {
	private final static String TAG = ScreenQoS.class.getCanonicalName();
	
	private CheckBox mCbEnableSessionTimers;
	private RelativeLayout mRlSessionTimers;
	private EditText mEtSessionTimeOut;
	private Spinner mSpRefresher;
	private Spinner mSpPrecondStrength;
	private Spinner mSpPrecondType;
	private Spinner mSpPrecondBandwidth;
	
	private final static String[] sSpinnerRefresherItems = new String[] {NgnConfigurationEntry.DEFAULT_QOS_REFRESHER, "uas", "uac"};
	private final static ScreenQoSStrength[] sSpinnerPrecondStrengthItems = new ScreenQoSStrength[] {
			new ScreenQoSStrength(tmedia_qos_strength_t.tmedia_qos_strength_none, "None"),
			new ScreenQoSStrength(tmedia_qos_strength_t.tmedia_qos_strength_optional, "Optional"),
			new ScreenQoSStrength(tmedia_qos_strength_t.tmedia_qos_strength_mandatory, "Mandatory"),
		};
	private final static ScreenQoSType[] sSpinnerPrecondTypeItems = new ScreenQoSType[] {
		   new ScreenQoSType(tmedia_qos_stype_t.tmedia_qos_stype_none, "None"),
		   new ScreenQoSType(tmedia_qos_stype_t.tmedia_qos_stype_segmented, "Segmented"),
		   new ScreenQoSType(tmedia_qos_stype_t.tmedia_qos_stype_e2e, "End2End")
		};
	private final static String[] sSpinnerPrecondBandwidthItems = new String[] {NgnConfigurationEntry.DEFAULT_QOS_PRECOND_BANDWIDTH, "Medium", "High"};
	
	private final INgnConfigurationService mConfigurationService;
	
	public ScreenQoS() {
		super(SCREEN_TYPE.QOS_T, TAG);

		// Services
		mConfigurationService = getEngine().getConfigurationService();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_qos);
		
		// get controls
		mCbEnableSessionTimers = (CheckBox)findViewById(R.id.screen_qos_checkBox_sessiontimers);
		mRlSessionTimers = (RelativeLayout)findViewById(R.id.screen_qos_relativeLayout_sessiontimers);
        mEtSessionTimeOut = (EditText)findViewById(R.id.screen_qos_editText_stimeout);
        mSpRefresher = (Spinner)findViewById(R.id.screen_qos_spinner_refresher);
        mSpPrecondStrength = (Spinner)findViewById(R.id.screen_qos_spinner_precond_strength);
        mSpPrecondType = (Spinner)findViewById(R.id.screen_qos_Spinner_precond_type);
        mSpPrecondBandwidth = (Spinner)findViewById(R.id.screen_qos_spinner_precond_bandwidth);
        
        // spinners
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ScreenQoS.sSpinnerRefresherItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpRefresher.setAdapter(adapter);
        
        ArrayAdapter<ScreenQoSStrength> adapterStrength = new ArrayAdapter<ScreenQoSStrength>(this, android.R.layout.simple_spinner_item, ScreenQoS.sSpinnerPrecondStrengthItems);
        adapterStrength.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpPrecondStrength.setAdapter(adapterStrength);
        
        ArrayAdapter<ScreenQoSType> adapterType = new ArrayAdapter<ScreenQoSType>(this, android.R.layout.simple_spinner_item, ScreenQoS.sSpinnerPrecondTypeItems);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpPrecondType.setAdapter(adapterType);
        
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ScreenQoS.sSpinnerPrecondBandwidthItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpPrecondBandwidth.setAdapter(adapter);
        
        // load values from configuration file (Do it before adding UI listeners)
        mCbEnableSessionTimers.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.QOS_USE_SESSION_TIMERS, NgnConfigurationEntry.DEFAULT_QOS_USE_SESSION_TIMERS));
        mEtSessionTimeOut.setText(mConfigurationService.getString(NgnConfigurationEntry.QOS_SIP_CALLS_TIMEOUT, Integer.toString(NgnConfigurationEntry.DEFAULT_QOS_SIP_CALLS_TIMEOUT)));
		mSpRefresher.setSelection(getSpinnerIndex(
				mConfigurationService.getString(NgnConfigurationEntry.QOS_REFRESHER,
						sSpinnerRefresherItems[0]),
						sSpinnerRefresherItems));
		
		mSpPrecondStrength.setSelection(ScreenQoSStrength.getSpinnerIndex(tmedia_qos_strength_t.valueOf(mConfigurationService.getString(
				NgnConfigurationEntry.QOS_PRECOND_STRENGTH,
				NgnConfigurationEntry.DEFAULT_QOS_PRECOND_STRENGTH))));
		
		
		mSpPrecondType.setSelection(ScreenQoSType.getSpinnerIndex(tmedia_qos_stype_t.valueOf(mConfigurationService.getString(
				NgnConfigurationEntry.QOS_PRECOND_TYPE,
				NgnConfigurationEntry.DEFAULT_QOS_PRECOND_TYPE))));
		
		mSpPrecondBandwidth.setSelection(getSpinnerIndex(
				mConfigurationService.getString(NgnConfigurationEntry.QOS_PRECOND_BANDWIDTH,
						sSpinnerPrecondBandwidthItems[0]),
						sSpinnerPrecondBandwidthItems));
		
		mRlSessionTimers.setVisibility(mCbEnableSessionTimers.isChecked() ? View.VISIBLE : View.INVISIBLE);
		
		// add listeners (for the configuration)
		/* addConfigurationListener(cbEnableSessionTimers); */
		addConfigurationListener(mEtSessionTimeOut);
		addConfigurationListener(mSpRefresher);
		addConfigurationListener(mSpPrecondStrength);
		addConfigurationListener(mSpPrecondType);
		addConfigurationListener(mSpPrecondBandwidth);
		
		// add local listeners
        mCbEnableSessionTimers.setOnCheckedChangeListener(mCbEnableSessionTimers_OnCheckedChangeListener);
	}
	
	protected void onPause() {
		if(super.mComputeConfiguration){
			
			mConfigurationService.putBoolean(NgnConfigurationEntry.QOS_USE_SESSION_TIMERS,
					mCbEnableSessionTimers.isChecked());
			mConfigurationService.putString(NgnConfigurationEntry.QOS_SIP_CALLS_TIMEOUT,
					mEtSessionTimeOut.getText().toString());
			mConfigurationService.putString(NgnConfigurationEntry.QOS_REFRESHER, 
					sSpinnerRefresherItems[mSpRefresher.getSelectedItemPosition()]);
			mConfigurationService.putString(NgnConfigurationEntry.QOS_PRECOND_STRENGTH, 
					sSpinnerPrecondStrengthItems[mSpPrecondStrength.getSelectedItemPosition()].mStrength.toString());
			mConfigurationService.putString(NgnConfigurationEntry.QOS_PRECOND_TYPE, 
					sSpinnerPrecondTypeItems[mSpPrecondType.getSelectedItemPosition()].mType.toString());
			mConfigurationService.putString(NgnConfigurationEntry.QOS_PRECOND_BANDWIDTH, 
					sSpinnerPrecondBandwidthItems[mSpPrecondBandwidth.getSelectedItemPosition()]);
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}	
	
	
	private OnCheckedChangeListener mCbEnableSessionTimers_OnCheckedChangeListener = new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			mRlSessionTimers.setVisibility(isChecked? View.VISIBLE : View.INVISIBLE);
			mComputeConfiguration = true;
		}
	};
	
	private static class ScreenQoSStrength {
		private final String mDescription;
		private final tmedia_qos_strength_t mStrength;

		private ScreenQoSStrength(tmedia_qos_strength_t strength, String description) {
			mStrength = strength;
			mDescription = description;
		}

		@Override
		public String toString() {
			return mDescription;
		}

		@Override
		public boolean equals(Object o) {
			return mStrength.equals(((ScreenQoSStrength)o).mStrength);
		}
		
		static int getSpinnerIndex(tmedia_qos_strength_t strength){
			for(int i = 0; i< sSpinnerPrecondStrengthItems.length; i++){
				if(strength == sSpinnerPrecondStrengthItems[i].mStrength){
					return i;
				}
			}
			return 0;
		}
	}
	
	private static class ScreenQoSType {
		private final String mDescription;
		private final tmedia_qos_stype_t mType;

		private ScreenQoSType(tmedia_qos_stype_t type, String description) {
			mType = type;
			mDescription = description;
		}

		@Override
		public String toString() {
			return mDescription;
		}

		@Override
		public boolean equals(Object o) {
			return mType.equals(((ScreenQoSType)o).mType);
		}
		
		static int getSpinnerIndex(tmedia_qos_stype_t type){
			for(int i = 0; i< sSpinnerPrecondTypeItems.length; i++){
				if(type == sSpinnerPrecondTypeItems[i].mType){
					return i;
				}
			}
			return 0;
		}
	}
}
