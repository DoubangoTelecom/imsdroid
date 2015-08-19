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
	
	private final INgnConfigurationService mConfigurationService;
	
	public ScreenContacts() {
		super(SCREEN_TYPE.CONTACTS_T, TAG);
		
		mConfigurationService = getEngine().getConfigurationService();
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
        mRbRemote.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.XCAP_ENABLED, NgnConfigurationEntry.DEFAULT_XCAP_ENABLED));
        //rbRemote.setChecked(!rbLocal.isChecked());
        mEtXcapRoot.setText(mConfigurationService.getString(NgnConfigurationEntry.XCAP_XCAP_ROOT, NgnConfigurationEntry.DEFAULT_XCAP_ROOT));
        mEtXUI.setText(mConfigurationService.getString(NgnConfigurationEntry.XCAP_USERNAME, NgnConfigurationEntry.DEFAULT_XCAP_USERNAME));
        mEtPassword.setText(mConfigurationService.getString(NgnConfigurationEntry.XCAP_PASSWORD, NgnConfigurationEntry.DEFAULT_XCAP_PASSWORD));
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
			mConfigurationService.putBoolean(NgnConfigurationEntry.XCAP_ENABLED, mRbRemote.isChecked());
			mConfigurationService.putString(NgnConfigurationEntry.XCAP_XCAP_ROOT, 
				mEtXcapRoot.getText().toString());
			mConfigurationService.putString(NgnConfigurationEntry.XCAP_USERNAME, 
				mEtXUI.getText().toString());
			mConfigurationService.putString(NgnConfigurationEntry.XCAP_PASSWORD, 
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
