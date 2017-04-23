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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

public class ScreenNatt extends BaseScreen {
	private final static String TAG = ScreenNatt.class.getCanonicalName();
	
	private CheckBox mCbHackAoR;
	private CheckBox mCbEnableStunForSIP;
	private CheckBox mCbEnableStunForICE;
	private CheckBox mCbEnableTurnForICE;
	private CheckBox mCbEnableIce;
	private RelativeLayout mRlStunServer;
	private RadioButton mRbDiscoStun;
	private RadioButton mRbSetStun;
	private EditText mEtStunStunHostname;
	private EditText mEtStunPort;
	private EditText mEtStunUsername;
	private EditText mEtStunPassword;
	
	private final INgnConfigurationService mConfigurationService;
	
	public  ScreenNatt() {
		super(SCREEN_TYPE.NATT_T, TAG);
		
		mConfigurationService = getEngine().getConfigurationService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_natt);
        
        // get controls
        mCbHackAoR = (CheckBox)findViewById(R.id.screen_natt_checkBox_hack_aor);
        mCbEnableStunForSIP = (CheckBox)findViewById(R.id.screen_natt_checkBox_stun_for_sip);
        mCbEnableStunForICE = (CheckBox)findViewById(R.id.screen_natt_checkBox_stun_for_ice);
        mCbEnableTurnForICE = (CheckBox)findViewById(R.id.screen_natt_checkBox_turn_for_ice);
        mRlStunServer = (RelativeLayout)findViewById(R.id.screen_natt_relativeLayout_stun_server);
        mCbEnableIce = (CheckBox)findViewById(R.id.screen_natt_checkBox_ice);
        mRbDiscoStun = (RadioButton)findViewById(R.id.screen_natt_radioButton_stun_disco);
        mRbSetStun = (RadioButton)findViewById(R.id.screen_natt_radioButton_stun_set);
        mEtStunStunHostname = (EditText)findViewById(R.id.screen_natt_editText_stun_server);
        mEtStunPort = (EditText)findViewById(R.id.screen_natt_editText_stun_port);
        mEtStunUsername = (EditText)findViewById(R.id.screen_natt_editText_stun_username);
        mEtStunPassword = (EditText)findViewById(R.id.screen_natt_editText_stun_password);
        
        // load values from configuration file (do it before adding UI listeners)
        mCbHackAoR.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.NATT_HACK_AOR, NgnConfigurationEntry.DEFAULT_NATT_HACK_AOR));
        mCbEnableStunForSIP.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.NATT_USE_STUN_FOR_SIP, NgnConfigurationEntry.DEFAULT_NATT_USE_STUN_FOR_SIP));
        mCbEnableStunForICE.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.NATT_USE_STUN_FOR_ICE, NgnConfigurationEntry.DEFAULT_NATT_USE_STUN_FOR_ICE));
        mCbEnableTurnForICE.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.NATT_USE_TURN_FOR_ICE, NgnConfigurationEntry.DEFAULT_NATT_USE_TURN_FOR_ICE));
        mCbEnableIce.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.NATT_USE_ICE, NgnConfigurationEntry.DEFAULT_NATT_USE_ICE));
        mRbDiscoStun.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.NATT_STUN_DISCO, NgnConfigurationEntry.DEFAULT_NATT_STUN_DISCO));
        mEtStunStunHostname.setText(mConfigurationService.getString(NgnConfigurationEntry.NATT_STUN_SERVER, NgnConfigurationEntry.DEFAULT_NATT_STUN_SERVER));
        mEtStunPort.setText(mConfigurationService.getString(NgnConfigurationEntry.NATT_STUN_PORT, Integer.toString(NgnConfigurationEntry.DEFAULT_NATT_STUN_PORT)));
        mEtStunUsername.setText(mConfigurationService.getString(NgnConfigurationEntry.NATT_STUN_USERNAME, NgnConfigurationEntry.DEFAULT_NATT_STUN_USERNAME));
        mEtStunPassword.setText(mConfigurationService.getString(NgnConfigurationEntry.NATT_STUN_PASSWORD, NgnConfigurationEntry.DEFAULT_NATT_STUN_PASSWORD));
        
        // mRlEnableStun.setVisibility(mCbEnableStun.isChecked() ? View.VISIBLE : View.INVISIBLE);
        mRlStunServer.setVisibility(mRbSetStun.isChecked() ? View.VISIBLE : View.INVISIBLE);
        
        
        addConfigurationListener(mCbHackAoR);
        /* addConfigurationListener(cbEnableStun); */
        addConfigurationListener(mCbEnableIce);
        addConfigurationListener(mCbEnableStunForSIP);
        addConfigurationListener(mCbEnableStunForICE);
        addConfigurationListener(mCbEnableTurnForICE);
        /* addConfigurationListener(rbDiscoStun);
        addConfigurationListener(rbSetStun); */
        addConfigurationListener(mEtStunStunHostname);
        addConfigurationListener(mEtStunPort);
        addConfigurationListener(mEtStunUsername);
        addConfigurationListener(mEtStunPassword);
        
        
        mCbEnableStunForSIP.setOnCheckedChangeListener(mCbEnableStun_OnCheckedChangeListener);
        mRbSetStun.setOnCheckedChangeListener(mRbSetStun_OnCheckedChangeListener);
	}

	protected void onPause() {
		if(super.mComputeConfiguration){
			
			mConfigurationService.putBoolean(NgnConfigurationEntry.NATT_HACK_AOR, mCbHackAoR.isChecked());
			mConfigurationService.putBoolean(NgnConfigurationEntry.NATT_USE_STUN_FOR_SIP, mCbEnableStunForSIP.isChecked());
			mConfigurationService.putBoolean(NgnConfigurationEntry.NATT_USE_STUN_FOR_ICE, mCbEnableStunForICE.isChecked());
			mConfigurationService.putBoolean(NgnConfigurationEntry.NATT_USE_TURN_FOR_ICE, mCbEnableTurnForICE.isChecked());
			mConfigurationService.putBoolean(NgnConfigurationEntry.NATT_USE_ICE, mCbEnableIce.isChecked());
			mConfigurationService.putBoolean(NgnConfigurationEntry.NATT_STUN_DISCO, mRbDiscoStun.isChecked());
			mConfigurationService.putString(NgnConfigurationEntry.NATT_STUN_SERVER, mEtStunStunHostname.getText().toString());
			try {
				mConfigurationService.putInt(NgnConfigurationEntry.NATT_STUN_PORT, Integer.parseInt(mEtStunPort.getText().toString()));
			} catch(NumberFormatException e) {
				Log.e(TAG, "stun port must be an integer");
			}
			mConfigurationService.putString(NgnConfigurationEntry.NATT_STUN_USERNAME, mEtStunUsername.getText().toString());
			mConfigurationService.putString(NgnConfigurationEntry.NATT_STUN_PASSWORD, mEtStunPassword.getText().toString());
	        
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to commit() configuration");
			}
			else{
				MediaSessionMgr.defaultsSetIceEnabled(mCbEnableIce.isChecked());
				MediaSessionMgr.defaultsSetIceStunEnabled(mCbEnableStunForICE.isChecked());
				MediaSessionMgr.defaultsSetIceTurnEnabled(mCbEnableTurnForICE.isChecked());
				MediaSessionMgr.defaultsSetStunEnabled(mCbEnableStunForSIP.isChecked());
				MediaSessionMgr.defaultsSetStunServer(mEtStunStunHostname.getText().toString(), Integer.parseInt(mEtStunPort.getText().toString()));
				MediaSessionMgr.defaultsSetStunCred(mEtStunUsername.getText().toString(), mEtStunPassword.getText().toString());
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
	
	private OnCheckedChangeListener mCbEnableStun_OnCheckedChangeListener = new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// mRlEnableStun.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
			mComputeConfiguration = true;
		}
	};
	
	private OnCheckedChangeListener mRbSetStun_OnCheckedChangeListener = new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			mRlStunServer.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
			mComputeConfiguration = true;
		}
	};
}
