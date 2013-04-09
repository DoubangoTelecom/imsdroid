/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.tinyWRAP.tmedia_pref_video_size_t;
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
	
	private CheckBox mCbEnableZeroArtifacts;
	private CheckBox mCbEnableSessionTimers;
	private RelativeLayout mRlSessionTimers;
	private EditText mEtSessionTimeOut;
	private Spinner mSpRefresher;
	private Spinner mSpPrecondStrength;
	private Spinner mSpPrecondType;
	private Spinner mSpVsize;
	
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
	private final static ScreenQoSVsize[] sSpinnerVsizeItems = new ScreenQoSVsize[] {
			new ScreenQoSVsize("SQCIF (128 x 98)", tmedia_pref_video_size_t.tmedia_pref_video_size_sqcif),
	        new ScreenQoSVsize("QCIF (176 x 144)", tmedia_pref_video_size_t.tmedia_pref_video_size_qcif),
	        new ScreenQoSVsize("QVGA (320 x 240)", tmedia_pref_video_size_t.tmedia_pref_video_size_qvga),
	        new ScreenQoSVsize("CIF (352 x 288)", tmedia_pref_video_size_t.tmedia_pref_video_size_cif),
	        new ScreenQoSVsize("HVGA (480 x 320)", tmedia_pref_video_size_t.tmedia_pref_video_size_hvga),
	        new ScreenQoSVsize("VGA (640 x 480)", tmedia_pref_video_size_t.tmedia_pref_video_size_vga),
	        new ScreenQoSVsize("4CIF (704 x 576)", tmedia_pref_video_size_t.tmedia_pref_video_size_4cif),
	        new ScreenQoSVsize("SVGA (800 x 600)", tmedia_pref_video_size_t.tmedia_pref_video_size_svga),
	        // FIXME: OpenGL rendering issue for 480P
	        // new ScreenQoSVsize("480P (852 x 480)", tmedia_pref_video_size_t.tmedia_pref_video_size_480p),
	        new ScreenQoSVsize("720P (1280 x 720)", tmedia_pref_video_size_t.tmedia_pref_video_size_720p),
	        new ScreenQoSVsize("16CIF (1408 x 1152)", tmedia_pref_video_size_t.tmedia_pref_video_size_16cif),
	        new ScreenQoSVsize("1080P (1920 x 1080)", tmedia_pref_video_size_t.tmedia_pref_video_size_1080p)
		};
	
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
		mCbEnableZeroArtifacts = (CheckBox)findViewById(R.id.screen_qos_checkBox_zeroArtifacts);
		mCbEnableSessionTimers = (CheckBox)findViewById(R.id.screen_qos_checkBox_sessiontimers);
		mRlSessionTimers = (RelativeLayout)findViewById(R.id.screen_qos_relativeLayout_sessiontimers);
        mEtSessionTimeOut = (EditText)findViewById(R.id.screen_qos_editText_stimeout);
        mSpRefresher = (Spinner)findViewById(R.id.screen_qos_spinner_refresher);
        mSpPrecondStrength = (Spinner)findViewById(R.id.screen_qos_spinner_precond_strength);
        mSpPrecondType = (Spinner)findViewById(R.id.screen_qos_Spinner_precond_type);
        mSpVsize = (Spinner)findViewById(R.id.screen_qos_Spinner_vsize);
        
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
        
        ArrayAdapter<ScreenQoSVsize> adapterVsize = new ArrayAdapter<ScreenQoSVsize>(this, android.R.layout.simple_spinner_item, ScreenQoS.sSpinnerVsizeItems);
        adapterVsize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpVsize.setAdapter(adapterVsize);
        
        // load values from configuration file (Do it before adding UI listeners)
        mCbEnableSessionTimers.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.QOS_USE_SESSION_TIMERS, NgnConfigurationEntry.DEFAULT_QOS_USE_SESSION_TIMERS));
        mCbEnableZeroArtifacts.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.QOS_USE_ZERO_VIDEO_ARTIFACTS, NgnConfigurationEntry.DEFAULT_QOS_USE_ZERO_VIDEO_ARTIFACTS));
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
		
		mSpVsize.setSelection(ScreenQoSVsize.getSpinnerIndex(tmedia_pref_video_size_t.valueOf(mConfigurationService.getString(
				NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE,
				NgnConfigurationEntry.DEFAULT_QOS_PREF_VIDEO_SIZE))));
		
		
		mRlSessionTimers.setVisibility(mCbEnableSessionTimers.isChecked() ? View.VISIBLE : View.INVISIBLE);
		
		// add listeners (for the configuration)
		/* addConfigurationListener(cbEnableSessionTimers); */
		addConfigurationListener(mEtSessionTimeOut);
		addConfigurationListener(mSpRefresher);
		addConfigurationListener(mSpPrecondStrength);
		addConfigurationListener(mSpPrecondType);
		addConfigurationListener(mSpVsize);
		
		// add local listeners
        mCbEnableSessionTimers.setOnCheckedChangeListener(mCbEnableSessionTimers_OnCheckedChangeListener);
	}
	
	protected void onPause() {
		if(super.mComputeConfiguration){
			
			mConfigurationService.putBoolean(NgnConfigurationEntry.QOS_USE_SESSION_TIMERS,
					mCbEnableSessionTimers.isChecked());
			mConfigurationService.putBoolean(NgnConfigurationEntry.QOS_USE_ZERO_VIDEO_ARTIFACTS,
					mCbEnableZeroArtifacts.isChecked());						
			mConfigurationService.putString(NgnConfigurationEntry.QOS_SIP_CALLS_TIMEOUT,
					mEtSessionTimeOut.getText().toString());
			mConfigurationService.putString(NgnConfigurationEntry.QOS_REFRESHER, 
					sSpinnerRefresherItems[mSpRefresher.getSelectedItemPosition()]);
			mConfigurationService.putString(NgnConfigurationEntry.QOS_PRECOND_STRENGTH, 
					sSpinnerPrecondStrengthItems[mSpPrecondStrength.getSelectedItemPosition()].mStrength.toString());
			mConfigurationService.putString(NgnConfigurationEntry.QOS_PRECOND_TYPE, 
					sSpinnerPrecondTypeItems[mSpPrecondType.getSelectedItemPosition()].mType.toString());
			mConfigurationService.putString(NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE, 
					sSpinnerVsizeItems[mSpVsize.getSelectedItemPosition()].mValue.toString());
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to commit() configuration");
			}
			else{
				MediaSessionMgr.defaultsSetPrefVideoSize(sSpinnerVsizeItems[mSpVsize.getSelectedItemPosition()].mValue);
				MediaSessionMgr.defaultsSetVideoZeroArtifactsEnabled(mCbEnableZeroArtifacts.isChecked());
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
	
	private static class ScreenQoSVsize{
		private final String mDescription;
		private final tmedia_pref_video_size_t mValue;
		
		private ScreenQoSVsize(String description, tmedia_pref_video_size_t value) {
			mValue = value;
			mDescription = description;
		}
		
		@Override
		public String toString() {
			return mDescription;
		}
		
		@Override
		public boolean equals(Object o) {
			return mValue.equals(((ScreenQoSVsize)o).mValue);
		}
		
		static int getSpinnerIndex(tmedia_pref_video_size_t value){
			for(int i = 0; i< sSpinnerVsizeItems.length; i++){
				if(value == sSpinnerVsizeItems[i].mValue){
					return i;
				}
			}
			return 0;
		}
	}
}
