package org.doubango.ngn.services.impl;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


public class NgnConfigurationService extends NgnBaseService implements INgnConfigurationService {
	private final static String TAG = NgnConfigurationService.class.getCanonicalName();
	
	private SharedPreferences mSettings;
	private SharedPreferences.Editor mSettingsEditor;
	
	public NgnConfigurationService(){
		final Context applicationContext = NgnApplication.getContext();
		if(applicationContext != null){
			mSettings = NgnApplication.getContext().getSharedPreferences(NgnConfigurationEntry.SHARED_PREF_NAME, 0);
			mSettingsEditor = mSettings.edit();
		}
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
	public boolean putString(final String entry, String value, boolean commit) {
		if(mSettingsEditor == null){
			Log.e(TAG,"Settings are null");
			return false;
		}
		mSettingsEditor.putString(entry.toString(), value);
		if(commit){
			return mSettingsEditor.commit();
		}
		return true;
	}

	@Override
	public boolean putString(final String entry, String value) {
		return putString(entry, value, false);
	}

	@Override
	public boolean putInt(final String entry, int value, boolean commit) {
		if(mSettingsEditor == null){
			Log.e(TAG,"Settings are null");
			return false;
		}
		mSettingsEditor.putInt(entry.toString(), value);
		if(commit){
			return mSettingsEditor.commit();
		}
		return true;
	}

	@Override
	public boolean putInt(final String entry, int value) {
		return putInt(entry, value, false);
	}

	@Override
	public boolean putFloat(final String entry, float value, boolean commit) {
		if(mSettingsEditor == null){
			Log.e(TAG,"Settings are null");
			return false;
		}
		mSettingsEditor.putFloat(entry.toString(), value);
		if(commit){
			return mSettingsEditor.commit();
		}
		return true;
	}

	@Override
	public boolean putFloat(final String entry, float value) {
		return putFloat(entry, value, false);
	}

	@Override
	public boolean putBoolean(final String entry, boolean value, boolean commit) {
		if(mSettingsEditor == null){
			Log.e(TAG,"Settings are null");
			return false;
		}
		mSettingsEditor.putBoolean(entry.toString(), value);
		if(commit){
			return mSettingsEditor.commit();
		}
		return true;
	}

	@Override
	public boolean putBoolean(final String entry, boolean value) {
		return putBoolean(entry, value, false);
	}

	@Override
	public String getString(final String entry, String defaultValue) {
		if(mSettingsEditor == null){
			Log.e(TAG,"Settings are null");
			return defaultValue;
		}
		try{
			return mSettings.getString(entry.toString(), defaultValue);
		}
		catch(Exception e){
			e.printStackTrace();
			return defaultValue;
		}
	}

	@Override
	public int getInt(final String entry, int defaultValue) {
		if(mSettingsEditor == null){
			Log.e(TAG,"Settings are null");
			return defaultValue;
		}
		try{
			return mSettings.getInt(entry.toString(), defaultValue);
		}
		catch(Exception e){
			e.printStackTrace();
			return defaultValue;
		}
	}

	@Override
	public float getFloat(final String entry, float defaultValue) {
		if(mSettingsEditor == null){
			Log.e(TAG,"Settings are null");
			return defaultValue;
		}
		try{
			return mSettings.getFloat(entry.toString(), defaultValue);
		}
		catch(Exception e){
			e.printStackTrace();
			return defaultValue;
		}
	}

	@Override
	public boolean getBoolean(final String entry, boolean defaultValue) {
		if(mSettingsEditor == null){
			Log.e(TAG,"Settings are null");
			return defaultValue;
		}
		try{
			return mSettings.getBoolean(entry.toString(), defaultValue);
		}
		catch(Exception e){
			e.printStackTrace();
			return defaultValue;
		}
	}
	
	@Override
	public boolean commit() {
		if(mSettingsEditor == null){
			Log.e(TAG,"Settings are null");
			return false;
		}
		return mSettingsEditor.commit();
	}
}
