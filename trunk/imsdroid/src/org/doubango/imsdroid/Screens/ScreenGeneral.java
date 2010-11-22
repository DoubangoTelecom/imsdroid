/*
* Copyright (C) 2010 Mamadou Diop.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango.org>
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
*
*/

package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.Impl.ServiceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class ScreenGeneral  extends Screen {
	
	private Spinner spAudioPlaybackLevel;
	private CheckBox cbFullScreenVideo;
	private CheckBox cbFFC;
	private CheckBox cbVflip;
	private CheckBox cbAutoStart;
	private EditText etEnumDomain;
	
	private final IConfigurationService configurationService;
	private final static String TAG = ScreenGeneral.class.getCanonicalName();
	
	private final static AudioPlayBackLevel[] audioPlaybackLevels =  new AudioPlayBackLevel[]{
					new AudioPlayBackLevel(0.25f, "Low"),
					new AudioPlayBackLevel(0.50f, "Medium"),
					new AudioPlayBackLevel(0.75f, "High"),
					new AudioPlayBackLevel(1.0f, "Maximum"),
			};
	
	public ScreenGeneral() {
		super(SCREEN_TYPE.GENERAL_T, ScreenGeneral.class.getCanonicalName());
		
		this.configurationService = ServiceManager.getConfigurationService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_general);
        
        this.cbFullScreenVideo = (CheckBox)this.findViewById(R.id.screen_general_checkBox_fullscreen);
        this.cbFFC = (CheckBox)this.findViewById(R.id.screen_general_checkBox_ffc);
        this.cbVflip = (CheckBox)this.findViewById(R.id.screen_general_checkBox_videoflip);
        this.cbAutoStart = (CheckBox)this.findViewById(R.id.screen_general_checkBox_autoStart);
        this.spAudioPlaybackLevel = (Spinner)this.findViewById(R.id.screen_general_spinner_playback_level);
        this.etEnumDomain = (EditText)this.findViewById(R.id.screen_general_editText_enum_domain);
        
        // Audio Playback levels
        ArrayAdapter<AudioPlayBackLevel> adapter = new ArrayAdapter<AudioPlayBackLevel>(this, android.R.layout.simple_spinner_item, ScreenGeneral.audioPlaybackLevels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spAudioPlaybackLevel.setAdapter(adapter);
        
        this.cbFullScreenVideo.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.FULL_SCREEN_VIDEO, Configuration.DEFAULT_GENERAL_FULL_SCREEN_VIDEO));
        this.cbFFC.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.FFC, Configuration.DEFAULT_GENERAL_FFC));
        this.cbVflip.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.VIDEO_FLIP, Configuration.DEFAULT_GENERAL_FLIP_VIDEO));
        SharedPreferences settings = getSharedPreferences(IMSDroid.getContext().getPackageName(), 0);
        this.cbAutoStart.setChecked((settings != null && settings.getBoolean("autostarts", Configuration.DEFAULT_GENERAL_AUTOSTART)));
        this.spAudioPlaybackLevel.setSelection(this.getSpinnerIndex(
				this.configurationService.getFloat(
						CONFIGURATION_SECTION.GENERAL,
						CONFIGURATION_ENTRY.AUDIO_PLAY_LEVEL,
						Configuration.DEFAULT_GENERAL_AUDIO_PLAY_LEVEL)));
        this.etEnumDomain.setText(this.configurationService.getString(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.ENUM_DOMAIN, Configuration.DEFAULT_GENERAL_ENUM_DOMAIN));
        
        this.addConfigurationListener(this.cbFullScreenVideo);
        this.addConfigurationListener(this.cbFFC);
        this.addConfigurationListener(this.cbVflip);
        this.addConfigurationListener(this.cbAutoStart);
        this.addConfigurationListener(this.etEnumDomain);
        this.addConfigurationListener(this.spAudioPlaybackLevel);
	}
	
	protected void onPause() {
		if(this.computeConfiguration){
			
			this.configurationService.setBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.FULL_SCREEN_VIDEO, this.cbFullScreenVideo.isChecked());
			this.configurationService.setBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.FFC, this.cbFFC.isChecked());
			this.configurationService.setBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.VIDEO_FLIP, this.cbVflip.isChecked());
			this.configurationService.setFloat(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.AUDIO_PLAY_LEVEL, ((AudioPlayBackLevel)this.spAudioPlaybackLevel.getSelectedItem()).value);
			this.configurationService.setString(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.ENUM_DOMAIN, this.etEnumDomain.getText().toString());
			
			SharedPreferences settings = getSharedPreferences(IMSDroid.getContext().getPackageName(), 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("autostarts", this.cbAutoStart.isChecked());
			editor.commit();
			
			// Compute
			if(!this.configurationService.compute()){
				Log.e(ScreenGeneral.TAG, "Failed to Compute() configuration");
			}
			
			this.computeConfiguration = false;
		}
		super.onPause();
	}
	
	
	private int getSpinnerIndex(float value){
		for(int i = 0; i< ScreenGeneral.audioPlaybackLevels.length; i++){
			if(ScreenGeneral.audioPlaybackLevels[i].value == value){
				return i;
			}
		}
		return 0;
	}
	
	static class AudioPlayBackLevel{
		float value;
		String description;
		
		AudioPlayBackLevel(float value, String description){
			this.value = value;
			this.description = description;
		}

		@Override
		public String toString() {
			return this.description;
		}
	}
}
