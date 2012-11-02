/* Copyright (C) 2012, Doubango Telecom.
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
package com.example.testt140;

import java.io.UnsupportedEncodingException;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnInviteEventTypes;
import org.doubango.ngn.events.NgnMessagingEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnContentType;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.tinyWRAP.tdav_codec_id_t;
import org.doubango.tinyWRAP.tmedia_t140_data_type_t;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getCanonicalName();
	
	private TextView mTextView_Status;
	private TextView mTextView_History;
	private Button mButton_Call;
	private EditText mEditText_Typing;
	
	private BroadcastReceiver mSipBroadCastRecv;
	
	private final NgnEngine mEngine;
	private final INgnConfigurationService mConfigurationService;
	private final INgnSipService mSipService;
	
	private final static String SIP_DOMAIN = "doubango.org";
	private final static String SIP_USERNAME = "005";
	private final static String SIP_PASSWORD = "mysecret";
	private final static String SIP_SERVER_HOST = "192.168.0.9";
	public final static String SIP_REMOTE_PEER = "rtt2";
	private final static int SIP_SERVER_PORT = 5060;
	
	private NgnAVSession mAVSession;
	
	public MainActivity(){
		mEngine = NgnEngine.getInstance();
		mConfigurationService = mEngine.getConfigurationService();
		mSipService = mEngine.getSipService();
		
		// Enable G.711, RED, T.140		
		mConfigurationService.putInt(NgnConfigurationEntry.MEDIA_CODECS, 
				tdav_codec_id_t.tdav_codec_id_pcma.swigValue() | 
				tdav_codec_id_t.tdav_codec_id_pcmu.swigValue() |
				tdav_codec_id_t.tdav_codec_id_red.swigValue() |
				tdav_codec_id_t.tdav_codec_id_t140.swigValue()
				);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mTextView_Status = (TextView)findViewById(R.id.textView_Status);
        mTextView_History = (TextView)findViewById(R.id.textView_History);
        mButton_Call = (Button)findViewById(R.id.button_Call);
        mEditText_Typing = (EditText)findViewById(R.id.editText_Typing);
        
        mButton_Call.setEnabled(false);
        mEditText_Typing.setEnabled(false);

        
        mEditText_Typing.addTextChangedListener(
        	new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if(mAVSession != null && mAVSession.isConnected()){
						if(before < count){
							// added
							int len = (count - before);
							String str = s.subSequence(s.length() - len, s.length()).toString();
							mAVSession.sendT140Data(str);
						}
						else if(before > count){
							// removed
							int len = (before - count);
							for(int i = 0; i < len; ++i){
								mAVSession.sendT140Data(tmedia_t140_data_type_t.tmedia_t140_data_type_backspace);
							}
						}
					}
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
				
				@Override
				public void afterTextChanged(Editable s) {
				}
			}
        );
        
        mButton_Call.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mAVSession != null){
					if(mAVSession.getState() == InviteState.INCOMING){
						mAVSession.acceptCall();
					}
					else{
						mAVSession.hangUpCall();
						mAVSession = null;
						mButton_Call.setText("Call");
					}
				}
				else{
					mAVSession = NgnAVSession.createOutgoingSession(mSipService.getSipStack(), NgnMediaType.AudioT140);
					mAVSession.makeCall(NgnUriUtils.makeValidSipUri(SIP_REMOTE_PEER));
					mButton_Call.setText("End");
				}
			}
		});
        
        // Listen for registration events
 		mSipBroadCastRecv = new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				final String action = intent.getAction();
 				
 				// Registration events
 				if(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
 					NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
 					if(args == null){
 						mTextView_Status.setText("Invalid registration event args");
 						return;
 					}
 					mButton_Call.setEnabled(mSipService.isRegistered());
 					switch(args.getEventType()){
 						case REGISTRATION_NOK:
 							mTextView_Status.setText("Failed to register :(");
 							break;
 						case UNREGISTRATION_OK:
 							mTextView_Status.setText("You are now unregistered :)");
 							break;
 						case REGISTRATION_OK:
 							mTextView_Status.setText("You are now registered :)");
 							break;
 						case REGISTRATION_INPROGRESS:
 							mTextView_Status.setText("Trying to register...");
 							break;
 						case UNREGISTRATION_INPROGRESS:
 							mTextView_Status.setText("Trying to unregister...");
 							break;
 						case UNREGISTRATION_NOK:
 							mTextView_Status.setText("Failed to unregister :(");
 							break;
 					}
 				}
 				
 				// Call session (INVITE) events
 				else if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)){
 					NgnInviteEventArgs args = intent.getParcelableExtra(NgnInviteEventArgs.EXTRA_EMBEDDED);
 					if(args == null){
 						Log.e(TAG, "Invalid event args");
 						return;
 					}
 					
 					// retrieve the session for incoming call
 					if(mAVSession == null && args.getEventType() == NgnInviteEventTypes.INCOMING){
 						mAVSession = NgnAVSession.getSession(args.getSessionId());
 					}
 					
 					
 					if(mAVSession == null || args.getSessionId() != mAVSession.getId()){
 						return;
 					}
 					
 					final InviteState callState = mAVSession.getState();
 					mTextView_Status.setText(getStateDesc(callState));
 					mEditText_Typing.setEnabled(mAVSession.isConnected());
 					
 					switch(callState){
 						case REMOTE_RINGING:
 							mEngine.getSoundService().startRingBackTone();
 							break;
 						case INCOMING:
 							mEngine.getSoundService().startRingTone();
 							mButton_Call.setText("Accept");
 							break;
 						case EARLY_MEDIA:
 						case INCALL:
 							mEngine.getSoundService().stopRingTone();
 							mEngine.getSoundService().stopRingBackTone();
 							mAVSession.setSpeakerphoneOn(false);
 							mButton_Call.setText("End");
 							break;
 						case TERMINATING:
 						case TERMINATED:
 							mEngine.getSoundService().stopRingTone();
 							mEngine.getSoundService().stopRingBackTone();
 							mAVSession = null;
 							final String phrase = args.getPhrase();
 							mTextView_Status.setText(phrase != null ? phrase : "Call terminated");
 							mButton_Call.setText("Call");
 							break;
 						default:
 								break;
 					}
 				}
 				
 				// Messaging events (ShortMessage, T.140, MSRP ...)
 				else if(NgnMessagingEventArgs.ACTION_MESSAGING_EVENT.equals(action)){
 					NgnMessagingEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
 					if(args == null){
 						mTextView_Status.setText("Invalid messaging event args");
 						return;
 					}
 					
 					switch(args.getEventType()){
	 					case INCOMING:
		 					{
		 						if(NgnStringUtils.equals(args.getContentType(), NgnContentType.T140COMMAND, true)){
		 							tmedia_t140_data_type_t dataType = (tmedia_t140_data_type_t)intent.getSerializableExtra(NgnMessagingEventArgs.EXTRA_T140_DATA_TYPE);
		 							switch(dataType){
			 							case tmedia_t140_data_type_backspace:
			 								int length = mTextView_History.getEditableText().length();
			 								if(length > 0){
			 									mTextView_History.getEditableText().delete(length - 1, length);
			 								}
			 								break;
			 							case tmedia_t140_data_type_cr:
			 							case tmedia_t140_data_type_cr_lf:
			 								mTextView_History.getEditableText().append("\n");
			 								break;
			 							default:
			 								break;
		 							}
		 						}
		 						else{ // display any other content type (e.g plain/text, xml, pdf...) "AS IS"
			 						byte[] contentBytes = args.getPayload();
			 						if(contentBytes != null && contentBytes.length > 0){
			 							try {
											String contentStr = new String(contentBytes, "UTF-8");
											mTextView_History.getEditableText().append(contentStr);
										} catch (UnsupportedEncodingException e) {
											Log.e(TAG, e.toString());
										}
			 						}
		 						}
		 						break;
		 					}
	 					default:
		 					{
		 						break;
		 					}
 					}
 				}
 			}
 		};
 		final IntentFilter intentFilter = new IntentFilter();
 		intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
 		intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
 		intentFilter.addAction(NgnMessagingEventArgs.ACTION_MESSAGING_EVENT);
 	    registerReceiver(mSipBroadCastRecv, intentFilter);
    }
    
    @Override
	protected void onDestroy() {
		// Stops the engine
		if(mEngine.isStarted()){
			mEngine.stop();
		}
		
		// hangup the call
		if(mAVSession != null){
			mAVSession.setContext(null);
			if(mAVSession.isConnected()){
				mAVSession.hangUpCall();
			}
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
				mTextView_Status.setText("Engine started :)");
			}
			else{
				mTextView_Status.setText("Failed to start the engine :(");
			}
		}
		// Register
		if(mEngine.isStarted()){
			if(!mSipService.isRegistered()){
				// Set credentials
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, SIP_USERNAME);
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, String.format("sip:%s@%s", SIP_USERNAME, SIP_DOMAIN));
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, SIP_PASSWORD);
				mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST, SIP_SERVER_HOST);
				mConfigurationService.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT, SIP_SERVER_PORT);
				mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, SIP_DOMAIN);
				// VERY IMPORTANT: Commit changes
				mConfigurationService.commit();
				// register (log in)
				mSipService.register(this);
			}
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private String getStateDesc(InviteState state){
		switch(state){
			case NONE:
			default:
				return "Unknown";
			case INCOMING:
				return "Incoming";
			case INPROGRESS:
				return "Inprogress";
			case REMOTE_RINGING:
				return "Ringing";
			case EARLY_MEDIA:
				return "Early media";
			case INCALL:
				return "In Call";
			case TERMINATING:
				return "Terminating";
			case TERMINATED:
				return "termibated";
		}
	}
}
