/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*  Copyright (C) 2011, Philippe Verney <verney(dot)philippe(AT)gmail(dot)com>
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
import org.doubango.tinyWRAP.tmedia_profile_t;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class ScreenGeneral  extends BaseScreen {
	private final static String TAG = ScreenGeneral.class.getCanonicalName();
	
	private Spinner mSpProfile;
	private Spinner mSpAudioPlaybackLevel;
	private CheckBox mCbFullScreenVideo;
	private CheckBox mCbFFC;
	private CheckBox mCbAutoStart;
	private CheckBox mCbInterceptOutgoingCalls;
	private EditText mEtEnumDomain;
	private CheckBox mCbAEC;
	private CheckBox mCbVAD;
	private CheckBox mCbNR;
	
	private final INgnConfigurationService mConfigurationService;
	
	private final static AudioPlayBackLevel[] sAudioPlaybackLevels =  new AudioPlayBackLevel[]{
					new AudioPlayBackLevel(0.25f, "Low"),
					new AudioPlayBackLevel(0.50f, "Medium"),
					new AudioPlayBackLevel(0.75f, "High"),
					new AudioPlayBackLevel(1.0f, "Maximum"),
			};
	private final static Profile[] sProfiles =  new Profile[]{
		new Profile(tmedia_profile_t.tmedia_profile_default, "Default (User Defined)"),
        new Profile(tmedia_profile_t.tmedia_profile_rtcweb, "RTCWeb (Override)")
	};
	
	public ScreenGeneral() {
		super(SCREEN_TYPE.GENERAL_T, TAG);
		
		mConfigurationService = getEngine().getConfigurationService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_general);
        
        mCbFullScreenVideo = (CheckBox)findViewById(R.id.screen_general_checkBox_fullscreen);
        mCbInterceptOutgoingCalls = (CheckBox)findViewById(R.id.screen_general_checkBox_interceptCall);
        mCbFFC = (CheckBox)findViewById(R.id.screen_general_checkBox_ffc);
        mCbAutoStart = (CheckBox)findViewById(R.id.screen_general_checkBox_autoStart);
        mSpAudioPlaybackLevel = (Spinner)findViewById(R.id.screen_general_spinner_playback_level);
        mSpProfile = (Spinner)findViewById(R.id.screen_general_spinner_media_profile);
        mEtEnumDomain = (EditText)findViewById(R.id.screen_general_editText_enum_domain);
        mCbAEC = (CheckBox)this.findViewById(R.id.screen_general_checkBox_AEC);
        mCbVAD = (CheckBox)this.findViewById(R.id.screen_general_checkBox_VAD);
        mCbNR = (CheckBox)this.findViewById(R.id.screen_general_checkBox_NR);    
        
        // Audio Playback levels
        ArrayAdapter<AudioPlayBackLevel> adapter = new ArrayAdapter<AudioPlayBackLevel>(this, android.R.layout.simple_spinner_item, ScreenGeneral.sAudioPlaybackLevels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpAudioPlaybackLevel.setAdapter(adapter);
        // Media Profile
        ArrayAdapter<Profile> adapterProfile = new ArrayAdapter<Profile>(this, android.R.layout.simple_spinner_item, ScreenGeneral.sProfiles);
        adapterProfile.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpProfile.setAdapter(adapterProfile);
        
        mCbFullScreenVideo.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_FULL_SCREEN_VIDEO, NgnConfigurationEntry.DEFAULT_GENERAL_FULL_SCREEN_VIDEO));
        mCbInterceptOutgoingCalls.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_INTERCEPT_OUTGOING_CALLS, NgnConfigurationEntry.DEFAULT_GENERAL_INTERCEPT_OUTGOING_CALLS));
        mCbFFC.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_USE_FFC, NgnConfigurationEntry.DEFAULT_GENERAL_USE_FFC));
        mCbAutoStart.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_AUTOSTART, NgnConfigurationEntry.DEFAULT_GENERAL_AUTOSTART));
        mCbAEC.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_AEC,NgnConfigurationEntry.DEFAULT_GENERAL_AEC));
        mCbVAD.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_VAD,NgnConfigurationEntry.DEFAULT_GENERAL_VAD));
        mCbNR.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_NR,NgnConfigurationEntry.DEFAULT_GENERAL_NR));
        
        mSpProfile.setSelection(Profile.getSpinnerIndex(tmedia_profile_t.valueOf(mConfigurationService.getString(
				NgnConfigurationEntry.MEDIA_PROFILE,
				NgnConfigurationEntry.DEFAULT_MEDIA_PROFILE))));
        mSpAudioPlaybackLevel.setSelection(AudioPlayBackLevel.getSpinnerIndex(
				mConfigurationService.getFloat(
						NgnConfigurationEntry.GENERAL_AUDIO_PLAY_LEVEL,
						NgnConfigurationEntry.DEFAULT_GENERAL_AUDIO_PLAY_LEVEL)));
        mEtEnumDomain.setText(mConfigurationService.getString(NgnConfigurationEntry.GENERAL_ENUM_DOMAIN, NgnConfigurationEntry.DEFAULT_GENERAL_ENUM_DOMAIN));
        
        super.addConfigurationListener(mCbFullScreenVideo);
        super.addConfigurationListener(mCbInterceptOutgoingCalls);
        super.addConfigurationListener(mCbFFC);
        super.addConfigurationListener(mCbAutoStart);
        super.addConfigurationListener(mEtEnumDomain);
        super.addConfigurationListener(mSpAudioPlaybackLevel);
        super.addConfigurationListener(mSpProfile);
        super.addConfigurationListener(mCbAEC);
        super.addConfigurationListener(mCbVAD);
        super.addConfigurationListener(mCbNR);
	}
	
	protected void onPause() {
		if(super.mComputeConfiguration){
			
			mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_AUTOSTART, mCbAutoStart.isChecked());
			mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_FULL_SCREEN_VIDEO, mCbFullScreenVideo.isChecked());
			mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_INTERCEPT_OUTGOING_CALLS, mCbInterceptOutgoingCalls.isChecked());
			mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_USE_FFC, mCbFFC.isChecked());
			mConfigurationService.putFloat(NgnConfigurationEntry.GENERAL_AUDIO_PLAY_LEVEL, ((AudioPlayBackLevel)mSpAudioPlaybackLevel.getSelectedItem()).mValue);
			mConfigurationService.putString(NgnConfigurationEntry.GENERAL_ENUM_DOMAIN, mEtEnumDomain.getText().toString());
			mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_AEC, mCbAEC.isChecked());
			mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_VAD, mCbVAD.isChecked());
			mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_NR, mCbNR.isChecked());
			// profile should be moved to another screen (e.g. Media)
			mConfigurationService.putString(NgnConfigurationEntry.MEDIA_PROFILE, sProfiles[mSpProfile.getSelectedItemPosition()].mValue.toString());
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to commit() configuration");
			}
			else
			{
				// codecs, AEC, NoiseSuppression, Echo cancellation, ....
				boolean aec        = mCbAEC.isChecked() ;
				boolean vad        = mCbVAD.isChecked();
				boolean nr          = mCbNR.isChecked() ;
				int         echo_tail = mConfigurationService.getInt(NgnConfigurationEntry.GENERAL_ECHO_TAIL,NgnConfigurationEntry.DEFAULT_GENERAL_ECHO_TAIL);
				Log.d(TAG,"Configure AEC["+aec+"/"+echo_tail+"] NoiseSuppression["+nr+"], Voice activity detection["+vad+"]");
				if ( aec)
				{
					MediaSessionMgr.defaultsSetEchoSuppEnabled(true);
					MediaSessionMgr.defaultsSetEchoTail(echo_tail); // 2s  == 100 packets of 20 ms 
				}
				else
				{
					MediaSessionMgr.defaultsSetEchoSuppEnabled(false);
					MediaSessionMgr.defaultsSetEchoTail(0); 
				}
				MediaSessionMgr.defaultsSetVadEnabled(vad);
				MediaSessionMgr.defaultsSetNoiseSuppEnabled(nr);
				MediaSessionMgr.defaultsSetProfile(sProfiles[mSpProfile.getSelectedItemPosition()].mValue);
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
	
	static class Profile{
		tmedia_profile_t mValue;
		String mDescription;
		
		Profile(tmedia_profile_t value, String description){
			mValue = value;
			mDescription = description;
		}

		@Override
		public String toString() {
			return mDescription;
		}
		
		static int getSpinnerIndex(tmedia_profile_t value){
			for(int i = 0; i< sProfiles.length; i++){
				if(sProfiles[i].mValue == value){
					return i;
				}
			}
			return 0;
		}
	}
	
	static class AudioPlayBackLevel{
		float mValue;
		String mDescription;
		
		AudioPlayBackLevel(float value, String description){
			mValue = value;
			mDescription = description;
		}

		@Override
		public String toString() {
			return mDescription;
		}
		
		static int getSpinnerIndex(float value){
			for(int i = 0; i< sAudioPlaybackLevels.length; i++){
				if(sAudioPlaybackLevels[i].mValue == value){
					return i;
				}
			}
			return 0;
		}
	}
}
