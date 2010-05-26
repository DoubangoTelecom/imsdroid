package org.doubango.imsdroid.Screens;

import java.io.File;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ScreenSecurity extends Screen {

	private final IConfigurationService configurationService;
	private final static String TAG = ScreenSecurity.class.getCanonicalName();
	
	private final static int REQUEST_CODE_PRIV_KEY = 1234;
	private final static int REQUEST_CODE_PUB_KEY = 12345;
	private final static int REQUEST_CODE_CA = 123456;
	
	private ImageButton ibPrivKey;
	private ImageButton ibPubKey;
	private ImageButton ibCA;
	private EditText etPrivKey;
	private EditText etPubKey;
	private EditText etCA;
	private CheckBox cbTlsSecAgree;
	
	public  ScreenSecurity() {
		super(SCREEN_TYPE.SECURITY_T);
		
		this.configurationService = ServiceManager.getConfigurationService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_security);
        
      // get controls
        this.ibPrivKey = (ImageButton)this.findViewById(R.id.screen_security_imageButton_private_key);
        this.ibPubKey = (ImageButton)this.findViewById(R.id.screen_security_imageButton_public_key);
        this.ibCA = (ImageButton)this.findViewById(R.id.screen_security_imageButton_ca);
        this.etPrivKey = (EditText)this.findViewById(R.id.screen_security_editText_private_key);
        this.etPubKey = (EditText)this.findViewById(R.id.screen_security_editText_public_key);
        this.etCA = (EditText)this.findViewById(R.id.screen_security_editText_ca);
        this.cbTlsSecAgree = (CheckBox)this.findViewById(R.id.screen_security_checkBox_tls_secagree);
        
        // load values from configuration file (do it before adding UI listeners)
        this.etPrivKey.setText(this.configurationService.getString(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_PRIV_KEY_FILE, Configuration.DEFAULT_TLS_PRIV_KEY_FILE));
        this.etPubKey.setText(this.configurationService.getString(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_PUB_KEY_FILE, Configuration.DEFAULT_TLS_PUB_KEY_FILE));
        this.etCA.setText(this.configurationService.getString(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_CA_FILE, Configuration.DEFAULT_TLS_CA_FILE));
        this.cbTlsSecAgree.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_SEC_AGREE, Configuration.DEFAULT_TLS_SEC_AGREE));
        
        // local listeners
        this.ibPrivKey.setOnClickListener(this.ibPrivKey_OnClickListener);
        this.ibPubKey.setOnClickListener(this.ibPubKey_OnClickListener);
        this.ibCA.setOnClickListener(this.ibCA_OnClickListener);
        this.cbTlsSecAgree.setOnCheckedChangeListener(this.cbTlsSecAgree_OnCheckedChangeListener);
	}
	
	protected void onPause() {
		if(this.computeConfiguration){
			
			//this.configurationService.setString(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_PRIV_KEY_FILE, this.etPrivKey.getText().toString());
			//this.configurationService.setString(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_PUB_KEY_FILE, this.etPubKey.getText().toString());
			//this.configurationService.setString(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_CA_FILE, this.etCA.getText().toString());
			
	        
			// Compute
			if(!this.configurationService.compute()){
				Log.e(ScreenSecurity.TAG, "Failed to Compute() configuration");
			}
			
			this.computeConfiguration = false;
		}
		super.onPause();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		if (requestCode == ScreenSecurity.REQUEST_CODE_PRIV_KEY) {
			Uri uri = data.getData();
			Log.d(ScreenSecurity.TAG, uri.toString());
		}
		else if (requestCode == ScreenSecurity.REQUEST_CODE_PUB_KEY) {
			
		}
		else if (requestCode == ScreenSecurity.REQUEST_CODE_CA) {

		}
		else{
			Log.e(ScreenSecurity.TAG, String.format("%d is an unknown request code", requestCode));
		}
	}
	
	private OnClickListener ibPrivKey_OnClickListener = new OnClickListener(){
		public void onClick(View v) {
			// Files and directories !
			// Files and directories !
			

			Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
			//intent.setType("file:///sdcard/image/*");  
			//startActivityForResult(intent, 1);
			intent.setType("*/*");                    
			intent.addCategory(Intent.CATEGORY_OPENABLE);          
			ServiceManager.getMainActivity().startActivityForResult(intent, 1);
			
			
			
			/*Intent intent = new Intent();
			Uri startDir = Uri.fromFile(new File("/sdcard"));
			intent.setAction(Intent.ACTION_PICK);
			intent.setDataAndType(startDir, "vnd.android.cursor.dir/*");
			//intent.setDataAndType(startDir, "file://");
			// Title
			intent.putExtra("explorer_title", "Select a file");
			// Optional colors
			intent.putExtra("browser_title_background_color", "440000AA");
			intent.putExtra("browser_title_foreground_color", "FFFFFFFF");
			intent.putExtra("browser_list_background_color", "00000066");
			// Optional font scale
			intent.putExtra("browser_list_fontscale", "120%");
			// Optional 0=simple list, 1 = list with filename and size, 2 = list with filename, size and date.
			intent.putExtra("browser_list_layout", "2");
			startActivityForResult(intent, 999);*/
			
			/*Intent intent = new Intent();
			Uri directory = Uri.fromFile(new File("/sdcard"));
			intent.setAction(Intent.ACTION_PICK);
			intent.setDataAndType(directory, "vnd.android.cursor.dir/*");
			//intent.setDataAndType(directory, "file://");
			intent.putExtra("browser_list_layout", "2");
			intent.putExtra("explorer_title", "alors christophe");
			startActivityForResult(intent, 666);*/
			
			//intent.setDataAndType(startDir, "vnd.android.cursor.dir/*");
			//intent.setDataAndType(startDir, "file://");
			//Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
			
			//ScreenSecurity.this.startActivityForResult(i, 999);
			//ScreenSecurity.this.startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT), ScreenSecurity.REQUEST_CODE_PRIV_KEY);
		}
	};
	
	private OnClickListener ibPubKey_OnClickListener = new OnClickListener(){
		public void onClick(View v) {
		}
	};
	
	private OnClickListener ibCA_OnClickListener = new OnClickListener(){
		public void onClick(View v) {
		}
	};
	
	private OnCheckedChangeListener cbTlsSecAgree_OnCheckedChangeListener = new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			ScreenSecurity.this.configurationService.setBoolean(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_SEC_AGREE, isChecked);
		}
	};
}
