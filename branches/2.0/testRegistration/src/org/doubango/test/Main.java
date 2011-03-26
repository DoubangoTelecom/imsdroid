package org.doubango.test;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;

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
	
	// Load native libraries (the shared libraries are from 'android-ngn-stack' project)
	static {
		System.load(String.format("/data/data/%s/lib/libtinyWRAP.so", Main.class.getPackage().getName()));
		NgnEngine.initialize();
	}
	
	public Main(){
		mEngine = NgnEngine.getInstance();
		mConfigurationService = mEngine.getConfigurationService();
		mSipService = mEngine.getSipService();
		
		
//		final String realm = "sip:doubango.org";
//		final String privateIdentity = "001";
//		final String publicIdentity = "sip:001@doubango.org";
//		final String password = "my secret";
//		final String proxyHost = "192.168.0.1";
//		RegistrationSession registrationSession;
//		// Sip Callback
//		final SipCallback callback = new SipCallback(){
//			@Override
//			public int OnDialogEvent(DialogEvent e) {
//				final SipSession sipSession = e.getBaseSession();
//				final long sipSessionId = sipSession.getId();
//				final short code = e.getCode();
//				switch (code){
//					case tinyWRAPConstants.tsip_event_code_dialog_connecting:
//						if(registrationSession != null && registrationSession.getId() == sipSessionId){
//							// Registration in progress
//						}
//						break;
//					case tinyWRAPConstants.tsip_event_code_dialog_connected:
//						if(registrationSession != null && registrationSession.getId() == sipSessionId){
//							// You are registered
//						}
//						break;
//					case tinyWRAPConstants.tsip_event_code_dialog_terminating:
//						if(registrationSession != null && registrationSession.getId() == sipSessionId){
//							// You are unregistering
//						}
//						break;
//					case tinyWRAPConstants.tsip_event_code_dialog_terminated:
//						if(registrationSession != null && registrationSession.getId() == sipSessionId){
//							// You are unregistered
//						}
//						break;
//				}
//					
//				return 0;
//			}
//
//			@Override
//			public int OnRegistrationEvent(RegistrationEvent e) {
//				// low level events
//				return 0;
//			}
//		};
//		// Create the SipStack
//		SipStack sipStack = new SipStack(callback, realm, privateIdentity, publicIdentity);
//		// Set Proxy Host and port
//		sipStack.setProxyCSCF(proxyHost, 5060, "UDP", "IPv4");
//		// Set password
//		sipStack.setPassword(password);
//		if(sipStack.isValid()){
//			if(sipStack.start()){
//				registrationSession = new RegistrationSession(sipStack);
//				registrationSession.setFromUri(publicIdentity);
//				// Send SIP register request
//				registrationSession.register_();
//			}
//		}
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