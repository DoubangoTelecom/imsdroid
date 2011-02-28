package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Utils.ConfigurationUtils;
import org.doubango.imsdroid.Utils.ConfigurationUtils.ConfigurationEntry;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class ScreenGeneral  extends BaseScreen {
	private final static String TAG = ScreenGeneral.class.getCanonicalName();
	
	private Spinner mSpAudioPlaybackLevel;
	private CheckBox mCbFullScreenVideo;
	private CheckBox mCbFFC;
	private CheckBox mCbVflip;
	private CheckBox mCbAutoStart;
	private CheckBox mCbInterceptOutgoingCalls;
	private EditText mEtEnumDomain;
	
	private final IConfigurationService mConfigurationService;
	
	private final static AudioPlayBackLevel[] sAudioPlaybackLevels =  new AudioPlayBackLevel[]{
					new AudioPlayBackLevel(0.25f, "Low"),
					new AudioPlayBackLevel(0.50f, "Medium"),
					new AudioPlayBackLevel(0.75f, "High"),
					new AudioPlayBackLevel(1.0f, "Maximum"),
			};
	
	public ScreenGeneral() {
		super(SCREEN_TYPE.GENERAL_T, TAG);
		
		mConfigurationService = ServiceManager.getConfigurationService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_general);
        
        mCbFullScreenVideo = (CheckBox)findViewById(R.id.screen_general_checkBox_fullscreen);
        mCbInterceptOutgoingCalls = (CheckBox)findViewById(R.id.screen_general_checkBox_interceptCall);
        mCbFFC = (CheckBox)findViewById(R.id.screen_general_checkBox_ffc);
        mCbVflip = (CheckBox)findViewById(R.id.screen_general_checkBox_videoflip);
        mCbAutoStart = (CheckBox)findViewById(R.id.screen_general_checkBox_autoStart);
        mSpAudioPlaybackLevel = (Spinner)findViewById(R.id.screen_general_spinner_playback_level);
        mEtEnumDomain = (EditText)findViewById(R.id.screen_general_editText_enum_domain);
        
        // Audio Playback levels
        ArrayAdapter<AudioPlayBackLevel> adapter = new ArrayAdapter<AudioPlayBackLevel>(this, android.R.layout.simple_spinner_item, ScreenGeneral.sAudioPlaybackLevels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpAudioPlaybackLevel.setAdapter(adapter);
        
        mCbFullScreenVideo.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.GENERAL_FULL_SCREEN_VIDEO, ConfigurationUtils.DEFAULT_GENERAL_FULL_SCREEN_VIDEO));
        mCbInterceptOutgoingCalls.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.GENERAL_INTERCEPT_OUTGOING_CALLS, ConfigurationUtils.DEFAULT_GENERAL_INTERCEPT_OUTGOING_CALLS));
        mCbFFC.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.GENERAL_USE_FFC, ConfigurationUtils.DEFAULT_GENERAL_USE_FFC));
        mCbVflip.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.GENERAL_VIDEO_FLIP, ConfigurationUtils.DEFAULT_GENERAL_FLIP_VIDEO));
        mCbAutoStart.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.GENERAL_AUTOSTART, ConfigurationUtils.DEFAULT_GENERAL_AUTOSTART));
        
        mSpAudioPlaybackLevel.setSelection(getSpinnerIndex(
				mConfigurationService.getFloat(
						ConfigurationEntry.GENERAL_AUDIO_PLAY_LEVEL,
						ConfigurationUtils.DEFAULT_GENERAL_AUDIO_PLAY_LEVEL)));
        mEtEnumDomain.setText(mConfigurationService.getString(ConfigurationEntry.GENERAL_ENUM_DOMAIN, ConfigurationUtils.DEFAULT_GENERAL_ENUM_DOMAIN));
        
        super.addConfigurationListener(mCbFullScreenVideo);
        super.addConfigurationListener(mCbInterceptOutgoingCalls);
        super.addConfigurationListener(mCbFFC);
        super.addConfigurationListener(mCbVflip);
        super.addConfigurationListener(mCbAutoStart);
        super.addConfigurationListener(mEtEnumDomain);
        super.addConfigurationListener(mSpAudioPlaybackLevel);
	}
	
	protected void onPause() {
		if(super.mComputeConfiguration){
			
			mConfigurationService.putBoolean(ConfigurationEntry.GENERAL_AUTOSTART, mCbAutoStart.isChecked());
			mConfigurationService.putBoolean(ConfigurationEntry.GENERAL_FULL_SCREEN_VIDEO, mCbFullScreenVideo.isChecked());
			mConfigurationService.putBoolean(ConfigurationEntry.GENERAL_INTERCEPT_OUTGOING_CALLS, mCbInterceptOutgoingCalls.isChecked());
			mConfigurationService.putBoolean(ConfigurationEntry.GENERAL_USE_FFC, mCbFFC.isChecked());
			mConfigurationService.putBoolean(ConfigurationEntry.GENERAL_VIDEO_FLIP, mCbVflip.isChecked());
			mConfigurationService.putFloat(ConfigurationEntry.GENERAL_AUDIO_PLAY_LEVEL, ((AudioPlayBackLevel)mSpAudioPlaybackLevel.getSelectedItem()).mValue);
			mConfigurationService.putString(ConfigurationEntry.GENERAL_ENUM_DOMAIN, mEtEnumDomain.getText().toString());
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
	
	
	private int getSpinnerIndex(float value){
		for(int i = 0; i< sAudioPlaybackLevels.length; i++){
			if(sAudioPlaybackLevels[i].mValue == value){
				return i;
			}
		}
		return 0;
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
	}
}
