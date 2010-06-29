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

import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.imsdroid.utils.StringUtils;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public abstract class Screen extends Activity implements IScreen {
	public static enum SCREEN_TYPE {
		// Well-Known
		ABOUT_T, AUTHORIZATIONS_T, CHAT_QUEUE_T, CONTACT_VIEW_T, CONTACTS_T, CONTACTS_OPTIONS_T, DIALER_T, FILE_TRANSFER_QUEUE_T, GENERAL_T, HISTORY_T, HOME_T, IDENTITY_T, MESSAGING_T, NATT_T, NETWORK_T, OPTIONS_T, PRESENCE_T, QOS_T,
		REGISTRATIONS_T, SECURITY_T,
		// All others
		AV_T
	}

//	public static final String SCREEN_TITLE_ABOUT = "About";
//	public static final String SCREEN_TITLE_AUTHORIZATIONS = "Authorizations";
//	public static final String SCREEN_TITLE_CALL = "Call";
//	public static final String SCREEN_TITLE_CHAT_QUEUE = "Chat room";
//	public static final String SCREEN_TITLE_CONTACT_VIEW = null;
//	public static final String SCREEN_TITLE_CONTACTS = "Address Book";
//	public static final String SCREEN_TITLE_CONTACTS_OPTIONS = "Options-Contacts";
//	public static final String SCREEN_TITLE_DIALER = "Dialer";
//	public static final String SCREEN_TITLE_FILE_TRANSFER_QUEUE = "File Transfers...";
//	public static final String SCREEN_TITLE_GENERAL = "Options-General";
//	public static final String SCREEN_TITLE_HISTORY = "History";
//	public static final String SCREEN_TITLE_HOME = "Home";
//	public static final String SCREEN_TITLE_IDENTITY = "Options-Identity";
//	public static final String SCREEN_TITLE_OPTIONS = "Options";
//	public static final String SCREEN_TITLE_MESSAGING = "Options-Messaging";
//	public static final String SCREEN_TITLE_NATT = "Options-Nat Traversal";
//	public static final String SCREEN_TITLE_NETWORK = "Options-Network";
//	public static final String SCREEN_TITLE_PRESENCE = "Options-Presence";
//	public static final String SCREEN_TITLE_QOS = "Options-QoS/QoE";
//	public static final String SCREEN_TITLE_REGISTRATIONS = "IMS Registrations";
//	public static final String SCREEN_TITLE_SECURITY = "Options-Security";
	
	protected String id;
	protected final SCREEN_TYPE type;
	protected boolean computeConfiguration;
	
	protected Screen(SCREEN_TYPE type, String id) {
		this.type = type;
		this.id = id;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			ServiceManager.getScreenService().back();
			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0){
			if(!this.haveMenu()){
				ServiceManager.getScreenService().show(ScreenHome.class);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public String getScreenTitle() {
		return this.getId();
	}

	public boolean haveMenu(){
		return false;
	}
	
	public String getId() {
		return this.id;
	}
	
	protected void addConfigurationListener(RadioButton radioButton){
		radioButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Screen.this.computeConfiguration = true;
			}
		});
	}
	
	protected void addConfigurationListener(EditText editText){
		editText.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Screen.this.computeConfiguration = true;
			}
        });
	}
	protected void addConfigurationListener(CheckBox checkBox){
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Screen.this.computeConfiguration = true;
			}
		});
	}
	protected void addConfigurationListener(Spinner spinner){
		// setOnItemClickListener not supported by Spinners
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Screen.this.computeConfiguration = true;
			}
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}
	
	protected int getSpinnerIndex(String value, String[] values){
		int i;
		for(i = 0; i< values.length; i++){
			if(StringUtils.equals(value, values[i], true)){
				return i;
			}
		}
		return 0;
	}
}
