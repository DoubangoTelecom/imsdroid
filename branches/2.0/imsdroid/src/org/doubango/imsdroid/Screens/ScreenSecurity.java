package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ScreenSecurity extends BaseScreen {
	private final static String TAG = ScreenSecurity.class.getCanonicalName();
	
	private final INgnConfigurationService mConfigurationService;
	
	private final static int REQUEST_CODE_PRIV_KEY = 1234;
	private final static int REQUEST_CODE_PUB_KEY = 12345;
	private final static int REQUEST_CODE_CA = 123456;
	
	private LinearLayout mLlTlsFiles;
	private ImageButton mIbPrivKey;
	private ImageButton mIbPubKey;
	private ImageButton mIbCA;
	private EditText mEtAMF;
	private EditText mEtOpId;
	@SuppressWarnings("unused")
	private EditText mEtPrivKey;
	@SuppressWarnings("unused")
	private EditText mEtPubKey;
	@SuppressWarnings("unused")
	private EditText mEtCA;
	private CheckBox mCbTlsSecAgree;
	private CheckBox mCbTlsFiles;
	
	public  ScreenSecurity() {
		super(SCREEN_TYPE.SECURITY_T, TAG);
		
		mConfigurationService = getEngine().getConfigurationService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_security);
        
      // get controls
        mLlTlsFiles = (LinearLayout)findViewById(R.id.screen_security_linearLayout_tlsfiles);
        mCbTlsFiles = (CheckBox)findViewById(R.id.screen_security_checkBox_tlsfiles);
        mIbPrivKey = (ImageButton)findViewById(R.id.screen_security_imageButton_private_key);
        mIbPubKey = (ImageButton)findViewById(R.id.screen_security_imageButton_public_key);
        mIbCA = (ImageButton)findViewById(R.id.screen_security_imageButton_ca);
        mEtAMF = (EditText)findViewById(R.id.screen_security_editText_amf);
        mEtOpId = (EditText)findViewById(R.id.screen_security_editText_opid);
        mEtPrivKey = (EditText)findViewById(R.id.screen_security_editText_private_key);
        mEtPubKey = (EditText)findViewById(R.id.screen_security_editText_public_key);
        mEtCA = (EditText)findViewById(R.id.screen_security_editText_ca);
        mCbTlsSecAgree = (CheckBox)findViewById(R.id.screen_security_checkBox_tls_secagree);
        
        // load values from configuration file (do it before adding UI listeners)
        mEtAMF.setText(mConfigurationService.getString(NgnConfigurationEntry.SECURITY_IMSAKA_AMF, NgnConfigurationEntry.DEFAULT_SECURITY_IMSAKA_AMF));
        mEtOpId.setText(mConfigurationService.getString(NgnConfigurationEntry.SECURITY_IMSAKA_OPID, NgnConfigurationEntry.DEFAULT_SECURITY_IMSAKA_OPID));
        //mEtPrivKey.setText(mConfigurationService.getString(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_PRIV_KEY_FILE, Configuration.DEFAULT_TLS_PRIV_KEY_FILE));
        //mEtPubKey.setText(mConfigurationService.getString(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_PUB_KEY_FILE, Configuration.DEFAULT_TLS_PUB_KEY_FILE));
        //mEtCA.setText(mConfigurationService.getString(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_CA_FILE, Configuration.DEFAULT_TLS_CA_FILE));
        //mCbTlsSecAgree.setChecked(mConfigurationService.getBoolean(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_SEC_AGREE, Configuration.DEFAULT_TLS_SEC_AGREE));
        
        
        addConfigurationListener(mEtAMF);
        addConfigurationListener(mEtOpId);
        
        // local listeners
        mIbPrivKey.setOnClickListener(ibPrivKey_OnClickListener);
        mIbPubKey.setOnClickListener(ibPubKey_OnClickListener);
        mIbCA.setOnClickListener(ibCA_OnClickListener);
        mCbTlsSecAgree.setOnCheckedChangeListener(cbTlsSecAgree_OnCheckedChangeListener);
        mCbTlsFiles.setOnCheckedChangeListener(cbTlsFiles_OnCheckedChangeListener);
	}
	
	protected void onPause() {
		if(super.mComputeConfiguration){
			
			mConfigurationService.putString(NgnConfigurationEntry.SECURITY_IMSAKA_AMF, mEtAMF.getText().toString());
			mConfigurationService.putString(NgnConfigurationEntry.SECURITY_IMSAKA_OPID, mEtOpId.getText().toString());
			
			//configurationService.setString(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_PRIV_KEY_FILE, etPrivKey.getText().toString());
			//configurationService.setString(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_PUB_KEY_FILE, etPubKey.getText().toString());
			//configurationService.setString(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_CA_FILE, etCA.getText().toString());
			
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to Compute() configuration");
			}
			
			super.mComputeConfiguration = false;
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
			Log.d(TAG, uri.toString());
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
			

//			Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
//			//intent.setType("file:///sdcard/image/*");  
//			//startActivityForResult(intent, 1);
//			intent.setType("*/*");                    
//			intent.addCategory(Intent.CATEGORY_OPENABLE);          
//			ServiceManager.getMainActivity().startActivityForResult(intent, 1);
			
			
			
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
			intent.putExtra("explorer_title", "ca ne marche pas");
			startActivityForResult(intent, 666);*/
			
			//intent.setDataAndType(startDir, "vnd.android.cursor.dir/*");
			//intent.setDataAndType(startDir, "file://");
			//Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
			
			//ScreenSecurity.startActivityForResult(i, 999);
			//ScreenSecurity.startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT), ScreenSecurity.REQUEST_CODE_PRIV_KEY);
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
	
	private OnCheckedChangeListener cbTlsFiles_OnCheckedChangeListener = new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			mLlTlsFiles.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
			Toast.makeText(ScreenSecurity.this, "Not implemented", Toast.LENGTH_SHORT).show();
		}
	};
	
	private OnCheckedChangeListener cbTlsSecAgree_OnCheckedChangeListener = new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			// mConfigurationService.setBoolean(CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.TLS_SEC_AGREE, isChecked);
		}
	};
}
