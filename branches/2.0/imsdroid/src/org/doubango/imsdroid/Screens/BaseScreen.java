package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Utils.StringUtils;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

public abstract class BaseScreen extends Activity implements IBaseScreen {

	public static enum SCREEN_TYPE {
		// Well-Known
		ABOUT_T,
		CODECS_T,
		CONTACTS_T,
		DIALER_T,
		HOME_T,
		IDENTITY_T,
		GENERAL_T,
		MESSAGING_T,
		NATT_T,
		NETWORK_T,
		PRESENCE_T,
		QOS_T,
		SETTINGS_T,
		SECURITY_T,
		SPLASH_T,
		
		TAB_CONTACTS, 
		TAB_HISTORY_T, 
		TAB_INFO_T, 
		TAB_ONLINE,
		TAB_MESSAGES_T,
		
		
		// All others
		AV_T
	}
	
	protected String mId;
	protected final SCREEN_TYPE mType;
	protected boolean mComputeConfiguration;
	
	protected BaseScreen(SCREEN_TYPE type, String id) {
		super();
		mType = type;
		mId = id;
	}
	
	@Override
	public String getId() {
		return mId;
	}
	
	@Override
	public SCREEN_TYPE getType(){
		return mType;
	}

	@Override
	public boolean hasMenu() {
		return false;
	}

	@Override
	public boolean hasBack(){
		return false;
	}
	
	@Override
	public boolean back(){
		return ServiceManager.getScreenService().back();
	}
	
	@Override
	public boolean createOptionsMenu(Menu menu) {
		return false;
	}
	
	protected void addConfigurationListener(RadioButton radioButton){
		radioButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mComputeConfiguration = true;
			}
		});
	}
	protected void addConfigurationListener(EditText editText){
		editText.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mComputeConfiguration = true;
			}
        });
	}
	protected void addConfigurationListener(CheckBox checkBox){
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mComputeConfiguration = true;
			}
		});
	}
	protected void addConfigurationListener(Spinner spinner){
		// setOnItemClickListener not supported by Spinners
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				mComputeConfiguration = true;
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
}
