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

import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.utils.StringUtils;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
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
		ABOUT_T, AUTHORIZATIONS_T, AV_QUEUE_T, CHAT_QUEUE_T, CODECS_T, CONTACT_EDIT_T, CONTACT_VIEW_T, CONTACTS_T, CONTACTS_OPTIONS_T, DIALER_T, FILE_TRANSFER_QUEUE_T, FILE_TRANSFER_VIEW_T, GENERAL_T, HISTORY_T, HOME_T, IDENTITY_T, MESSAGING_T, MSRP_INC_T, NATT_T, NETWORK_T, OPTIONS_T, PRESENCE_T, QOS_T,
		REGISTRATIONS_T, SECURITY_T, SMS_COMPOSE_T, SMS_VIEW_T,
		// All others
		AV_T
	}
	
	protected String id;
	protected final SCREEN_TYPE type;
	protected boolean computeConfiguration;
	
	protected Screen(SCREEN_TYPE type, String id) {
		this.type = type;
		this.id = id;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && this.type != SCREEN_TYPE.HOME_T) {
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

	@Override
	public String getScreenTitle() {
		return this.getId();
	}

	@Override
	public boolean haveMenu(){
		return false;
	}
	
	@Override
	public boolean createOptionsMenu(Menu menu){
		return false;
	}
	
	@Override
	public String getId() {
		return this.id;
	}
	
	public SCREEN_TYPE getType(){
		return this.type;
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
		for(int i = 0; i< values.length; i++){
			if(StringUtils.equals(value, values[i], true)){
				return i;
			}
		}
		return 0;
	}
	
	protected int getSpinnerIndex(Object value, Object[] values){
		for(int i = 0; i< values.length; i++){
			if(value.equals(values[i])){
				return i;
			}
		}
		return 0;
	}
	
	// http://stackoverflow.com/questions/2169649/open-an-image-in-androids-built-in-gallery-app-programmatically
	protected String getPath(Uri uri) {
	    String[] projection = { MediaStore.Images.Media.DATA };
	    Cursor cursor = managedQuery(uri, projection, null, null, null);
	    int column_index = cursor
	            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}

}
