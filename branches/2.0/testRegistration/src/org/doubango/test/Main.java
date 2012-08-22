package org.doubango.test;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.utils.AndroidUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Main extends Activity {
	private static String TAG = Main.class.getCanonicalName();
	
	private TextView mTvInfo;
	private EditText mEtPublicIdentity;
	private EditText mEtPrivateIdentity;
	private EditText mEtPassword;
	private EditText mEtRealm;
	private EditText mEtProxyHost;
	private EditText mEtProxyPort;
	private Button mBtSignInOut;
	
	private BroadcastReceiver mSipBroadCastRecv;
	
	private final NgnEngine mEngine;
	private final INgnConfigurationService mConfigurationService;
	private final INgnSipService mSipService;
	private static final String DATA_FOLDER = String.format("/data/data/%s", Main.class.getPackage().getName());
	private static final String LIBS_FOLDER = String.format("%s/lib", Main.DATA_FOLDER);
	
	// Load native libraries (the shared libraries are from 'android-ngn-stack' project)
	static {
		// See 'http://code.google.com/p/imsdroid/issues/detail?id=197' for more information
		// Load Android utils library (required to detect CPU features)
		System.load(String.format("%s/%s", Main.LIBS_FOLDER, "libutils_armv5te.so"));
		Log.d(TAG,"CPU_Feature=" + AndroidUtils.getCpuFeatures());
		if(NgnApplication.isCpuNeon()){
			Log.d(TAG,"isCpuNeon()=YES");
			System.load(String.format("%s/%s", Main.LIBS_FOLDER, "libtinyWRAP_armv7-a.so"));
		}
		else{
			Log.d(TAG,"isCpuNeon()=NO");
			System.load(String.format("%s/%s", Main.LIBS_FOLDER, "libtinyWRAP_armv5te.so"));
		}
		// Initialize the engine
		NgnEngine.initialize();
	}
	
	public Main(){
		mEngine = NgnEngine.getInstance();
		mConfigurationService = mEngine.getConfigurationService();
		mSipService = mEngine.getSipService();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mTvInfo = (TextView)findViewById(R.id.textViewInfo);
        mEtPublicIdentity = (EditText)findViewById(R.id.editTextPublicIdentity);
        mEtPrivateIdentity = (EditText)findViewById(R.id.editTextPrivateIdentity);
        mEtPassword = (EditText)findViewById(R.id.editTextPassword);
        mEtRealm = (EditText)findViewById(R.id.editTextRealm);
        mEtProxyHost = (EditText)findViewById(R.id.editTextProxyHost);
        mEtProxyPort = (EditText)findViewById(R.id.editTextProxyPort);
        mBtSignInOut = (Button)findViewById(R.id.buttonSignInOut);
        
        // Subscribe for registration state changes
        mSipBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				
				// Registration Event
				if(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
					NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					switch(args.getEventType()){
						case REGISTRATION_NOK:
							mTvInfo.setText("Failed to register :(");
							break;
						case UNREGISTRATION_OK:
							mTvInfo.setText("You are now unregistered :)");
							break;
						case REGISTRATION_OK:
							mTvInfo.setText("You are now registered :)");
							break;
						case REGISTRATION_INPROGRESS:
							mTvInfo.setText("Trying to register...");
							break;
						case UNREGISTRATION_INPROGRESS:
							mTvInfo.setText("Trying to unregister...");
							break;
						case UNREGISTRATION_NOK:
							mTvInfo.setText("Failed to unregister :(");
							break;
					}
					mBtSignInOut.setText(mSipService.isRegistered() ? "Sign Out" : "Sign In");
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
	    registerReceiver(mSipBroadCastRecv, intentFilter);
        
        mBtSignInOut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mEngine.isStarted()){
					if(!mSipService.isRegistered()){
						// Set credentials
						mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, 
								mEtPrivateIdentity.getText().toString());
						mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, 
								mEtPublicIdentity.getText().toString());
						mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD,
								mEtPassword.getText().toString());
						mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST,
								mEtProxyHost.getText().toString());
						mConfigurationService.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT,
								NgnStringUtils.parseInt(mEtProxyPort.getText().toString(), 5060));
						mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM,
								mEtRealm.getText().toString());
						// VERY IMPORTANT: Commit changes
						mConfigurationService.commit();
						// register (log in)
						mSipService.register(Main.this);
					}
					else{
						// unregister (log out)
						mSipService.unRegister();
					}
				}
				else{
					mTvInfo.setText("Engine not started yet");
				}
			}
		});
    }

	@Override
	protected void onDestroy() {
		// Stops the engine
		if(mEngine.isStarted()){
			mEngine.stop();
		}
		// release the listener
		if (mSipBroadCastRecv != null) {
			unregisterReceiver(mSipBroadCastRecv);
			mSipBroadCastRecv = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Starts the engine
		if(!mEngine.isStarted()){
			if(mEngine.start()){
				mTvInfo.setText("Engine started :)");
			}
			else{
				mTvInfo.setText("Failed to start the engine :(");
			}
		}
	}    
}