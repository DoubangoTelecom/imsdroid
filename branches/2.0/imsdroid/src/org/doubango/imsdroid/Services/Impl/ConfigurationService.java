
package org.doubango.imsdroid.Services.Impl;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Utils.ConfigurationUtils.ConfigurationEntry;

import android.content.SharedPreferences;
import android.util.Log;

public class ConfigurationService extends BaseService implements IConfigurationService {
	private final static String TAG = ConfigurationService.class.getCanonicalName();
	
	private final SharedPreferences mSettings;
	private final SharedPreferences.Editor mSettingsEditor;
	
	public ConfigurationService(){
		mSettings = IMSDroid.getContext().getSharedPreferences(IMSDroid.getContext().getPackageName(), 0);
		mSettingsEditor = mSettings.edit();
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "starting...");
		return true;
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "stopping...");
		return true;
	}

	@Override
	public boolean putString(ConfigurationEntry entry, String value, boolean commit) {
		mSettingsEditor.putString(entry.toString(), value);
		if(commit){
			return mSettingsEditor.commit();
		}
		return true;
	}

	@Override
	public boolean putString(ConfigurationEntry entry, String value) {
		return putString(entry, value, false);
	}

	@Override
	public boolean putInt(ConfigurationEntry entry, int value, boolean commit) {
		mSettingsEditor.putInt(entry.toString(), value);
		if(commit){
			return mSettingsEditor.commit();
		}
		return true;
	}

	@Override
	public boolean putInt(ConfigurationEntry entry, int value) {
		return putInt(entry, value, false);
	}

	@Override
	public boolean putFloat(ConfigurationEntry entry, float value, boolean commit) {
		mSettingsEditor.putFloat(entry.toString(), value);
		if(commit){
			return mSettingsEditor.commit();
		}
		return true;
	}

	@Override
	public boolean putFloat(ConfigurationEntry entry, float value) {
		return putFloat(entry, value, false);
	}

	@Override
	public boolean putBoolean(ConfigurationEntry entry, boolean value, boolean commit) {
		mSettingsEditor.putBoolean(entry.toString(), value);
		if(commit){
			return mSettingsEditor.commit();
		}
		return true;
	}

	@Override
	public boolean putBoolean(ConfigurationEntry entry, boolean value) {
		return putBoolean(entry, value, false);
	}

	@Override
	public String getString(ConfigurationEntry entry, String defaultValue) {
		return mSettings.getString(entry.toString(), defaultValue);
	}

	@Override
	public int getInt(ConfigurationEntry entry, int defaultValue) {
		return mSettings.getInt(entry.toString(), defaultValue);
	}

	@Override
	public float getFloat(ConfigurationEntry entry, float defaultValue) {
		return mSettings.getFloat(entry.toString(), defaultValue);
	}

	@Override
	public boolean getBoolean(ConfigurationEntry entry, boolean defaultValue) {
		return mSettings.getBoolean(entry.toString(), defaultValue);
	}

	@Override
	public boolean commit() {
		return mSettingsEditor.commit();
	}
}
