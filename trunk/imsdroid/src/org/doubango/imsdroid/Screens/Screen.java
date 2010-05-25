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
		ABOUT_T, CONTACTS_T, CONTACTS_OPTIONS_T, GENERAL_T, HISTORY_T, HOME_T, IDENTITY_T, NETWORK_T, OPTIONS_T, PRESENCE_T
	}
	
	public static enum SCREEN_ID { 
		ABOUT_I, CONTACTS_OPTIONS_I, GENERAL_I, HISTORY_I, HOME_I, IDENTITY_I, NETWORK_I, OPTIONS_I, PRESENCE_I
	}

	public static final String SCREEN_TITLE_ABOUT = "About";
	public static final String SCREEN_TITLE_CONTACTS_OPTIONS = "Options-Contacts";
	public static final String SCREEN_TITLE_GENERAL = "Options-General";
	public static final String SCREEN_TITLE_HOME = "Home";
	public static final String SCREEN_TITLE_IDENTITY = "Options-Identity";
	public static final String SCREEN_TITLE_OPTIONS = "Options";
	public static final String SCREEN_TITLE_NETWORK = "Options-Network";
	public static final String SCREEN_TITLE_PRESENCE = "Options-Presence";
	
	protected final SCREEN_TYPE type;
	protected boolean computeConfiguration;
	
	protected Screen(SCREEN_TYPE type) {
		this.type = type;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			ServiceManager.getScreenService().back();
			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0){
			ServiceManager.getScreenService().show(SCREEN_ID.HOME_I);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public static final boolean isWellknown(Screen.SCREEN_TYPE type) {
		switch (type) {
		// Well-know screens
		case ABOUT_T:
		case CONTACTS_T:
		case CONTACTS_OPTIONS_T:
		case GENERAL_T:
		case HISTORY_T:
		case HOME_T:
		case IDENTITY_T:
		case NETWORK_T:
		case OPTIONS_T:
		case PRESENCE_T:
			return true;
		}
		return false;
	}
	
	public boolean isWellknown() {
		return Screen.isWellknown(this.type);
	}

	public String getScreenTitle() {
		switch (this.type) {
		// Well-know screens
		case ABOUT_T:
			return Screen.SCREEN_TITLE_ABOUT;
		case CONTACTS_T:
			break;
		case CONTACTS_OPTIONS_T:
			return Screen.SCREEN_TITLE_CONTACTS_OPTIONS;
		case GENERAL_T:
			return Screen.SCREEN_TITLE_GENERAL;
		case HISTORY_T:
			break;
		case HOME_T:
			return Screen.SCREEN_TITLE_HOME;
		case IDENTITY_T:
			return Screen.SCREEN_TITLE_IDENTITY;
		case NETWORK_T:
			return Screen.SCREEN_TITLE_NETWORK;
		case OPTIONS_T:
			return Screen.SCREEN_TITLE_OPTIONS;
		case PRESENCE_T:
			return Screen.SCREEN_TITLE_PRESENCE;

			// all others
		default:
			return null;
		}
		return null;
	}

	public Screen.SCREEN_ID getId() {
		switch (this.type) {
		// Well-know screens
		case ABOUT_T:
			return Screen.SCREEN_ID.ABOUT_I;
		case CONTACTS_T:
			break;
		case CONTACTS_OPTIONS_T:
			return Screen.SCREEN_ID.CONTACTS_OPTIONS_I;
		case GENERAL_T:
			return Screen.SCREEN_ID.GENERAL_I;
		case HISTORY_T:
			break;
		case HOME_T:
			return Screen.SCREEN_ID.HOME_I;
		case IDENTITY_T:
			return Screen.SCREEN_ID.IDENTITY_I;
		case NETWORK_T:
			return Screen.SCREEN_ID.NETWORK_I;
		case OPTIONS_T:
			return Screen.SCREEN_ID.OPTIONS_I;
		case PRESENCE_T:
			return Screen.SCREEN_ID.PRESENCE_I;

			// all others
		default:
			return null;
		}
		return null;
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
