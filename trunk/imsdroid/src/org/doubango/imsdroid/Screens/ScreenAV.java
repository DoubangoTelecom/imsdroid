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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.doubango.imsdroid.CallDialog;
import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.Main;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.events.IInviteEventHandler;
import org.doubango.imsdroid.events.InviteEventArgs;
import org.doubango.imsdroid.media.MediaType;
import org.doubango.imsdroid.sip.MyAVSession;
import org.doubango.imsdroid.sip.MySipStack;
import org.doubango.imsdroid.sip.MyAVSession.CallState;
import org.doubango.imsdroid.utils.UriUtils;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;


public class ScreenAV extends Screen {
	
	
	private static SimpleDateFormat __timerFormat;
	
	private final static String TAG = ScreenAV.class.getCanonicalName();
	
	private String remoteUri;
	
	private MyAVSession avSession;
	
	private long startTime;
	private final Timer timerInCall;
	private final Timer timerSuicide;
	
	
	private ViewFlipper fvFlipper;
	private ImageView ivDialer;
	private LinearLayout llVideoLocal;
	private LinearLayout llVideoRemote;
	private ImageView ivState;
	private TextView tvInfo;
	private TextView tvTime;
	private TextView tvRemoteUri;
	private Button btBack2Call;
	
	private ImageButton btDtmf_0;
	private ImageButton btDtmf_1;
	private ImageButton btDtmf_2;
	private ImageButton btDtmf_3;
	private ImageButton btDtmf_4;
	private ImageButton btDtmf_5;
	private ImageButton btDtmf_6;
	private ImageButton btDtmf_7;
	private ImageButton btDtmf_8;
	private ImageButton btDtmf_9;
	private ImageButton btDtmf_Sharp;
	private ImageButton btDtmf_Star;
	
	private ProxSensor proxSensor;
	private KeyguardLock keyguardLock;
	private final IScreenService screenService;
	
	private static final int SELECT_CONTENT = 1;
	
	private final static int MENU_PICKUP = 0;
	private final static int MENU_HANGUP= 1;
	private final static int MENU_HOLD_RESUME = 2;
	private final static int MENU_SEND_STOP_VIDEO = 3;
	private final static int MENU_SHARE_CONTENT = 4;
	private final static int MENU_SPEAKER = 5;
	
	static {
		//ScreenAV.__timerFormat = new SimpleDateFormat("HH:mm:ss");
		ScreenAV.__timerFormat = new SimpleDateFormat("mm:ss");
	}
	
	public ScreenAV() {
		super(SCREEN_TYPE.AV_T, null);
		
		this.timerInCall = new Timer();
		this.timerSuicide = new Timer();
		
		this.screenService = ServiceManager.getScreenService();
	}	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_av);       
        
        // retrieve id
        this.id = getIntent().getStringExtra("id");
        this.avSession = MyAVSession.getSession(Long.parseLong(this.id));
        MyAVSession.getCallEventHandler().setAvScreen(this);
        
        // get controls
        this.fvFlipper = (ViewFlipper) this.findViewById(R.id.screen_av_flipperView);
        this.ivDialer = (ImageView) this.findViewById(R.id.screen_av_imageView_dialer);
        this.llVideoLocal = (LinearLayout)this.findViewById(R.id.screen_av_linearLayout_video_local);
        this.llVideoRemote = (LinearLayout)this.findViewById(R.id.screen_av_linearLayout_video_remote);
        this.ivState = (ImageView)this.findViewById(R.id.screen_av_imageView_state);
        this.tvInfo = (TextView)this.findViewById(R.id.screen_av_textView_info);
        this.tvTime = (TextView)this.findViewById(R.id.screen_av_textView_time);
        this.tvRemoteUri = (TextView)this.findViewById(R.id.screen_av_textView_remoteUri);
        
        this.btBack2Call = (Button)this.findViewById(R.id.screen_av_button_back2call);
        
        this.btDtmf_0 = (ImageButton)this.findViewById(R.id.screen_av_imageButton_0);
        this.btDtmf_1 = (ImageButton)this.findViewById(R.id.screen_av_imageButton_1);
        this.btDtmf_2 = (ImageButton)this.findViewById(R.id.screen_av_imageButton_2);
        this.btDtmf_3 = (ImageButton)this.findViewById(R.id.screen_av_imageButton_3);
        this.btDtmf_4 = (ImageButton)this.findViewById(R.id.screen_av_imageButton_4);
        this.btDtmf_5 = (ImageButton)this.findViewById(R.id.screen_av_imageButton_5);
        this.btDtmf_6 = (ImageButton)this.findViewById(R.id.screen_av_imageButton_6);
        this.btDtmf_7 = (ImageButton)this.findViewById(R.id.screen_av_imageButton_7);
        this.btDtmf_8 = (ImageButton)this.findViewById(R.id.screen_av_imageButton_8);
        this.btDtmf_9 = (ImageButton)this.findViewById(R.id.screen_av_imageButton_9);
        this.btDtmf_Sharp = (ImageButton)this.findViewById(R.id.screen_av_imageButton_sharp);
        this.btDtmf_Star = (ImageButton)this.findViewById(R.id.screen_av_imageButton_star);
        
        if(this.avSession != null){
        	this.remoteUri = this.avSession.getRemoteParty();
        	this.tvRemoteUri.setText(String.format("In call with %s since:", UriUtils.getDisplayName(this.remoteUri)));
        	
        	MyAVSession.getVideoProducer().setContext(this);
			MyAVSession.getVideoConsumer().setContext(this);
			
			this.updateState(this.avSession.getState());
        }
        
        this.fvFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slidein));
        this.fvFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slideout));
        
        this.ivDialer.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ScreenAV.this.fvFlipper.showNext();
			}
        });
        this.btBack2Call.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ScreenAV.this.fvFlipper.showPrevious();
			}
        });
        
        this.btDtmf_0.setOnClickListener(this.dtmf_OnClickListener);
        this.btDtmf_1.setOnClickListener(this.dtmf_OnClickListener);
        this.btDtmf_2.setOnClickListener(this.dtmf_OnClickListener);
        this.btDtmf_3.setOnClickListener(this.dtmf_OnClickListener);
        this.btDtmf_4.setOnClickListener(this.dtmf_OnClickListener);
        this.btDtmf_5.setOnClickListener(this.dtmf_OnClickListener);
        this.btDtmf_6.setOnClickListener(this.dtmf_OnClickListener);
        this.btDtmf_7.setOnClickListener(this.dtmf_OnClickListener);
        this.btDtmf_8.setOnClickListener(this.dtmf_OnClickListener);
        this.btDtmf_9.setOnClickListener(this.dtmf_OnClickListener);
        this.btDtmf_Sharp.setOnClickListener(this.dtmf_OnClickListener);
        this.btDtmf_Star.setOnClickListener(this.dtmf_OnClickListener);
		
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		if(keyguardManager != null && keyguardManager.inKeyguardRestrictedInputMode()){
			if(this.keyguardLock == null){
				this.keyguardLock = keyguardManager.newKeyguardLock(ScreenAV.TAG);
			}
			this.keyguardLock.disableKeyguard();
		}
		
		if(this.proxSensor == null){
			this.proxSensor = new ProxSensor(this);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if(this.proxSensor != null){
			this.proxSensor.stop();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(this.proxSensor != null){
			this.proxSensor.start();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		if(this.keyguardLock != null){
			this.keyguardLock.reenableKeyguard();
		}
	}
	
	@Override
	protected void onDestroy() {
		MyAVSession.getCallEventHandler().setAvScreen(null);
		
		this.timerInCall.cancel();
		this.timerSuicide.cancel();
        
        if(avSession != null){
        	// FIXME: cleanup
        	MyAVSession.getVideoProducer().setContext(null);
        } 
        
		super.onDestroy();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
			case ScreenAV.MENU_PICKUP:
				if(this.avSession != null){					
					this.avSession.acceptCall();
				}
				break;
				
			case ScreenAV.MENU_HANGUP:
				if(this.avSession != null){
					this.tvInfo.setText("Ending the call...");
					this.avSession.hangUp();
				}
				break;
				
			case ScreenAV.MENU_HOLD_RESUME:
				if(this.avSession != null){
					if(this.avSession.isLocalHeld()){
						this.avSession.resumeCall();
					}
					else{
						this.avSession.holdCall();
					}
				}
				break;
				
			case ScreenAV.MENU_SEND_STOP_VIDEO:
				if(this.avSession != null){
					this.startStopVideo(!this.avSession.isSendingVideo());
				}
				break;
				
			case ScreenAV.MENU_SHARE_CONTENT:
				 Intent intent = new Intent();
				 intent.setType("*/*").addCategory(Intent.CATEGORY_OPENABLE).setAction(Intent.ACTION_GET_CONTENT);
				 startActivityForResult(Intent.createChooser(intent, "Select content"), ScreenAV.SELECT_CONTENT);
				break;
				
			case ScreenAV.MENU_SPEAKER:
				AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
				am.setSpeakerphoneOn(!am.isSpeakerphoneOn());
				break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
	    if (resultCode == RESULT_OK) {
	        if (requestCode == ScreenAV.SELECT_CONTENT && this.remoteUri != null) {
	            Uri selectedContentUri = data.getData();
	            String selectedContentPath = this.getPath(selectedContentUri);
	            if(ScreenFileTransferView.ShareContent(this.remoteUri, selectedContentPath, false)){
	            	ServiceManager.showContShareNotif(R.drawable.image_gallery_25, "Content Sharing");
	        		ServiceManager.getSoundService().playNewEvent();
	            }
	        }
	    }
	}
	
	/* ===================== IScreen (Screen) ======================== */
	@Override
	public boolean haveMenu(){
		return true;
	}
	
	@Override
	public boolean createOptionsMenu(Menu menu){
		if(this.avSession == null){
			return false;
		}
		
		MenuItem itemPickUp = menu.add(0, ScreenAV.MENU_PICKUP, 0, "Answer").setIcon(R.drawable.phone_pick_up_48);
		MenuItem itemHangUp = menu.add(0, ScreenAV.MENU_HANGUP, 0, "Hang-up").setIcon(R.drawable.phone_hang_up_48);
		MenuItem itemHoldResume = menu.add(0, ScreenAV.MENU_HOLD_RESUME, 0, "Hold").setIcon(R.drawable.phone_hold_48);
		MenuItem itemSendStopVideo = menu.add(1, ScreenAV.MENU_SEND_STOP_VIDEO, 0, "Send Video").setIcon(R.drawable.video_start_48);
		MenuItem itemShareContent = menu.add(1, ScreenAV.MENU_SHARE_CONTENT, 0, "Share Content").setIcon(R.drawable.image_gallery_48);
		MenuItem itemSpeaker = menu.add(1, ScreenAV.MENU_SPEAKER, 0, "Speaker ON").setIcon(R.drawable.phone_speaker_48);
		
		switch(this.avSession.getState()){
			case CALL_INCOMING:
				itemPickUp.setEnabled(true);
				itemHangUp.setEnabled(true);
				itemHoldResume.setEnabled(false);
				itemSpeaker.setEnabled(false);
				itemSendStopVideo.setEnabled(false);
				itemShareContent.setEnabled(false);
				break;
			case CALL_INPROGRESS:
				itemPickUp.setEnabled(false);
				itemHangUp.setEnabled(true);
				itemHoldResume.setEnabled(false);
				itemSpeaker.setEnabled(false);
				itemSendStopVideo.setEnabled(false);
				itemShareContent.setEnabled(false);
				break;
			case INCALL:
				itemPickUp.setEnabled(false);
				itemHangUp.setEnabled(true);
				itemHoldResume.setEnabled(true);
				itemSpeaker.setEnabled(true);
				itemSpeaker.setTitle(((AudioManager)getSystemService(Context.AUDIO_SERVICE)).isSpeakerphoneOn() ? "Speaker OFF" : "Speaker ON");
				
				if((this.avSession.getMediaType() == MediaType.AudioVideo || this.avSession.getMediaType() == MediaType.Video)){
					itemSendStopVideo.setTitle(this.avSession.isSendingVideo()? "Stop Video" : "Send Video").setIcon(this.avSession.isSendingVideo()? R.drawable.video_stop_48 : R.drawable.video_start_48);
					itemSendStopVideo.setEnabled(true);
				}
				else{
					itemSendStopVideo.setEnabled(false);
				}
				itemShareContent.setEnabled(true);
				itemHoldResume.setTitle(this.avSession.isLocalHeld()? "Resume" : "Hold").setIcon(this.avSession.isLocalHeld()? R.drawable.phone_resume_48 : R.drawable.phone_hold_48);
				break;
			case CALL_TERMINATED:
				itemPickUp.setEnabled(false);
				itemHangUp.setEnabled(false);
				itemHoldResume.setEnabled(false);
				itemSpeaker.setEnabled(false);
				itemSendStopVideo.setEnabled(false);
				itemShareContent.setEnabled(false);
				break;
		}
		
		return true;
	}
	
	public static boolean makeCall(String remoteUri, MediaType mediaType){
		if(MyAVSession.getFirstId() != null){
			Log.e(ScreenAV.TAG, "There is already an outgoing audio/video session");
			return false;
		}
		
		String validUri = UriUtils.makeValidSipUri(remoteUri);
		if(validUri == null){
			// Show DialogError
			return false;
		}
		else{
			remoteUri = validUri;
			if(remoteUri.startsWith("tel:")){
				// E.164 number => use ENUM protocol
				final MySipStack sipStack = ServiceManager.getSipService().getStack();
				if(sipStack != null){
					String phoneNumber = UriUtils.getValidPhoneNumber(remoteUri);
					if(phoneNumber != null){
						String enumDomain = ServiceManager.getConfigurationService().getString(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.ENUM_DOMAIN, Configuration.DEFAULT_GENERAL_ENUM_DOMAIN);
						String sipUri = sipStack.dnsENUM("E2U+SIP", phoneNumber, enumDomain);
						if(sipUri != null){
							remoteUri = sipUri;
						}
					}
				}
			}
		}
		
		MyAVSession avSession = MyAVSession.createOutgoingSession(ServiceManager.getSipService().getStack(), mediaType);
		avSession.setRemoteParty(remoteUri); // HACK
		ServiceManager.getScreenService().show(ScreenAV.class, new Long(avSession.getId()).toString());	
		
		switch(mediaType){
			case AudioVideo:
			case Video:
				return avSession.makeVideoCall(remoteUri);
			default:
				return avSession.makeAudioCall(remoteUri);
		}
	}
	
	public static boolean receiveCall(MyAVSession avSession){
		
		ServiceManager.getScreenService().bringToFront(Main.ACTION_SHOW_AVSCREEN,
				new String[] {"session-id", new Long(avSession.getId()).toString()}
		);
		return true;
	}
	
	private TimerTask timerTaskInCall = new TimerTask(){
		@Override
		public void run() {			
			final Date date = new Date(new Date().getTime() - ScreenAV.this.startTime);
			ScreenAV.this.runOnUiThread(new Runnable() {
				public void run() {
					ScreenAV.this.tvTime.setText(ScreenAV.__timerFormat.format(date));
				}});
		}
	};
	
	private TimerTask timerTaskSuicide = new TimerTask(){
		@Override
		public void run() {
			ScreenAV.this.runOnUiThread(new Runnable() {
				public void run() {
					ScreenAV.this.screenService.show(ScreenHome.class);
					ScreenAV.this.screenService.destroy(ScreenAV.this.getId());
				}});
		}
	};
	
	
	private OnClickListener dtmf_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(ScreenAV.this.avSession == null){
				return;
			}
			
			if(v == ScreenAV.this.btDtmf_0){
				if(ScreenAV.this.avSession.sendDTMF(0)){
					ServiceManager.getSoundService().playDTMF(0);
				}
			}
			else if(v == ScreenAV.this.btDtmf_1){
				if(ScreenAV.this.avSession.sendDTMF(1)){
					ServiceManager.getSoundService().playDTMF(1);
				}
			}
			else if(v == ScreenAV.this.btDtmf_2){
				if(ScreenAV.this.avSession.sendDTMF(2)){
					ServiceManager.getSoundService().playDTMF(2);
				}
			}
			else if(v == ScreenAV.this.btDtmf_3){
				if(ScreenAV.this.avSession.sendDTMF(3)){
					ServiceManager.getSoundService().playDTMF(3);
				}
			}
			else if(v == ScreenAV.this.btDtmf_4){
				if(ScreenAV.this.avSession.sendDTMF(4)){
					ServiceManager.getSoundService().playDTMF(4);
				}
			}
			else if(v == ScreenAV.this.btDtmf_5){
				if(ScreenAV.this.avSession.sendDTMF(5)){
					ServiceManager.getSoundService().playDTMF(5);
				}
			}
			else if(v == ScreenAV.this.btDtmf_6){
				if(ScreenAV.this.avSession.sendDTMF(6)){
					ServiceManager.getSoundService().playDTMF(6);
				}
			}
			else if(v == ScreenAV.this.btDtmf_7){
				if(ScreenAV.this.avSession.sendDTMF(7)){
					ServiceManager.getSoundService().playDTMF(7);
				}
			}
			else if(v == ScreenAV.this.btDtmf_8){
				if(ScreenAV.this.avSession.sendDTMF(8)){
					ServiceManager.getSoundService().playDTMF(8);
				}
			}
			else if(v == ScreenAV.this.btDtmf_9){
				if(ScreenAV.this.avSession.sendDTMF(9)){
					ServiceManager.getSoundService().playDTMF(9);
				}
			}
			else if(v == ScreenAV.this.btDtmf_Star){
				if(ScreenAV.this.avSession.sendDTMF(10)){
					ServiceManager.getSoundService().playDTMF(10);
				}
			}
			else if(v == ScreenAV.this.btDtmf_Sharp){
				if(ScreenAV.this.avSession.sendDTMF(11)){
					ServiceManager.getSoundService().playDTMF(11);
				}
			}
		}
	};

	private void startStopVideo(boolean start){
		if(this.avSession== null || (this.avSession.getMediaType() != MediaType.AudioVideo && this.avSession.getMediaType() != MediaType.Video)){
			return;
		}
		
		this.avSession.setSendingVideo(start);
		
		this.llVideoLocal.removeAllViews();
		if(start){
			final View local_preview = MyAVSession.getVideoProducer().startPreview();
			if(local_preview != null){
				final ViewParent viewParent = local_preview.getParent();
				if(viewParent != null && viewParent instanceof ViewGroup){
					((ViewGroup)(viewParent)).removeView(local_preview);
				}
				this.llVideoLocal.addView(local_preview);
			}
		}
	}
	
	private void updateState(CallState state){
		if(this.avSession== null){
			return;
		}
		
		switch(state){
			case CALL_INPROGRESS:						
				this.tvInfo.setText("In progress ...");
				this.ivState.setImageResource(R.drawable.bullet_ball_glass_grey_16);
				this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
							View layout = CallDialog.getView(ScreenAV.this,
									UriUtils.getDisplayName(ScreenAV.this.avSession.getRemoteParty()),
									null
									, new OnClickListener() {
										@Override
										public void onClick(View v) {
											ScreenAV.this.tvInfo
													.setText("Ending the call...");
											ScreenAV.this.avSession.hangUp();
										}
									});
							layout.setLayoutParams(new LinearLayout.LayoutParams(
									          LinearLayout.LayoutParams.FILL_PARENT,
									          LinearLayout.LayoutParams.FILL_PARENT
									      ));
							ScreenAV.this.llVideoRemote.removeAllViews();
							ScreenAV.this.llVideoRemote.addView(layout);
						}
					});
			break;
			
			case CALL_INCOMING:							
					this.tvInfo.setText(String.format("Incoming Call from %s", UriUtils.getDisplayName(remoteUri)));
					this.ivState.setImageResource(R.drawable.bullet_ball_glass_grey_16);
					this.runOnUiThread(new Runnable(){
						@Override
						public void run() {
								View layout = CallDialog.getView(ScreenAV.this,
										UriUtils.getDisplayName(ScreenAV.this.avSession.getRemoteParty()),
										new OnClickListener() {
											@Override
											public void onClick(View v) {
												ScreenAV.this.avSession.acceptCall();
											}
										}, new OnClickListener() {
											@Override
											public void onClick(View v) {
												ScreenAV.this.tvInfo
														.setText("Ending the call...");
												ScreenAV.this.avSession.hangUp();
											}
										});
								layout.setLayoutParams(new LinearLayout.LayoutParams(
										          LinearLayout.LayoutParams.FILL_PARENT,
										          LinearLayout.LayoutParams.FILL_PARENT
										      ));
								ScreenAV.this.llVideoRemote.removeAllViews();
								ScreenAV.this.llVideoRemote.addView(layout);
							}
						});
				break;
				
			case INCALL:				
					this.tvInfo.setText("In call");
					this.ivState.setImageResource(R.drawable.bullet_ball_glass_green_16);
					
					this.startTime = this.avSession.getStartTime();
					this.timerInCall.schedule(this.timerTaskInCall, 0, 1000);
					
					// Video consumer
					this.llVideoRemote.removeAllViews();
					if(this.avSession.getMediaType() == MediaType.AudioVideo || this.avSession.getMediaType() == MediaType.Video){
						final View remote_preview = MyAVSession.getVideoConsumer().startPreview();
						if(remote_preview != null){
							final ViewParent viewParent = remote_preview.getParent();
							if(viewParent != null && viewParent instanceof ViewGroup){
								((ViewGroup)(viewParent)).removeView(remote_preview);
							}
							this.llVideoRemote.addView(remote_preview);
						}
					}
					
					// Video producer
					this.startStopVideo(this.avSession.isSendingVideo());
				break;
				
			case CALL_TERMINATED:
					ScreenAV.this.tvInfo.setText("Call Terminated");
					ScreenAV.this.ivState.setImageResource(R.drawable.bullet_ball_glass_red_16);
					
					/* schedule suicide */
					this.timerSuicide.schedule(this.timerTaskSuicide, new Date(new Date().getTime() + 1500));
					this.timerTaskInCall.cancel();
				break;
		}
		
		if(this.avSession.isLocalHeld()){
			this.tvInfo.setText("Call placed on hold");
		}
		else if(this.avSession.isRemoteHeld()){
			this.tvInfo.setText("Placed on hold by remote party");
		}
	}
	
	
	
	
	/* ============================ Static Call Event Handler =========================*/
	public static class AVInviteEventHandler implements IInviteEventHandler
	{
		final static String TAG = AVInviteEventHandler.class.getCanonicalName();
		ScreenAV avScreen;
		final AudioManager audioManager;
		final PowerManager.WakeLock wakeLock;
		
		public AVInviteEventHandler(){
			ServiceManager.getSipService().addInviteEventHandler(this);
			
			this.audioManager = (AudioManager)IMSDroid.getContext().getSystemService(Context.AUDIO_SERVICE);
			PowerManager pm = (PowerManager) IMSDroid.getContext().getSystemService(Context.POWER_SERVICE);
			this.wakeLock = pm == null ? null : pm.newWakeLock(PowerManager.ON_AFTER_RELEASE | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, AVInviteEventHandler.TAG);
		}
		
		@Override
		protected void finalize() throws Throwable {
			ServiceManager.getSipService().removeInviteEventHandler(this);
			super.finalize();
		}
		
		void setAvScreen(ScreenAV avScreen){
			this.avScreen = avScreen;
		}
		
		@Override
		public long getId(){
			if(this.avScreen != null && this.avScreen.avSession != null){
				return this.avScreen.avSession.getId();
			}
			return -1;
		}
		
		@Override
		public boolean canHandle(long id){
			return (MyAVSession.getSession(id) != null);
		}
		
		@Override
		public boolean onInviteEvent(Object sender, InviteEventArgs e) {
			final String phrase = e.getPhrase();
			final MyAVSession avSession;
			
			if((avSession = MyAVSession.getSession(e.getSessionId())) == null){
				return false;
			}
			
			switch(e.getType()){
				case INCOMING:
					ServiceManager.showAVCallNotif(R.drawable.phone_call_25, "Incoming call");
					avSession.setState(CallState.CALL_INCOMING);
					
					if(this.avScreen != null){
						this.avScreen.runOnUiThread(new Runnable() {
							public void run() {
								AVInviteEventHandler.this.avScreen.updateState(CallState.CALL_INCOMING);
							}});
					}
					if(this.wakeLock != null && !this.wakeLock.isHeld()){
						this.wakeLock.acquire();
					}
					ServiceManager.vibrate(500);
					ServiceManager.getSoundService().playRingTone();
					break;
				case INPROGRESS:
					ServiceManager.showAVCallNotif(R.drawable.phone_call_25, "Outgoing Call");
					avSession.setState(CallState.CALL_INPROGRESS);
					
					if(this.avScreen != null){
						this.avScreen.runOnUiThread(new Runnable() {
							public void run() {		
								AVInviteEventHandler.this.avScreen.updateState(CallState.CALL_INPROGRESS);
							}});
					}
					break;
				case RINGING:
					ServiceManager.getSoundService().playRingBackTone();
					if(this.avScreen != null){
						this.avScreen.runOnUiThread(new Runnable() {
							public void run() {
								AVInviteEventHandler.this.avScreen.tvInfo.setText(phrase);
								AVInviteEventHandler.this.avScreen.ivState.setImageResource(R.drawable.bullet_ball_glass_grey_16);
							}});
					}
					break;
				case EARLY_MEDIA:
					this.audioManager.setMode(AudioManager.MODE_IN_CALL);
					this.audioManager.setSpeakerphoneOn(false);
					if(this.avScreen != null){
						this.avScreen.runOnUiThread(new Runnable() {
							public void run() {
								ServiceManager.getSoundService().stopRingBackTone();
								ServiceManager.getSoundService().stopRingTone();
								
								// Notification
								AVInviteEventHandler.this.avScreen.tvInfo.setText(phrase);
							}});
					}
					break;
				case CONNECTED:
					ServiceManager.getSoundService().stopRingBackTone();
					ServiceManager.getSoundService().stopRingTone();
					ServiceManager.showAVCallNotif(R.drawable.phone_call_25, "In Call");
					
					this.audioManager.setMode(AudioManager.MODE_IN_CALL);
					this.audioManager.setSpeakerphoneOn(false);
					
					avSession.setState(CallState.INCALL);
					
					if(this.wakeLock != null && this.wakeLock.isHeld()){
						this.wakeLock.release();
					}
					if(this.avScreen != null){
						this.avScreen.runOnUiThread(new Runnable() {
							public void run() {
								AVInviteEventHandler.this.avScreen.updateState(CallState.INCALL);					
							}});
					}
					
					break;
				case DISCONNECTED:
				case TERMWAIT:
					ServiceManager.getSoundService().stopRingBackTone();
					ServiceManager.getSoundService().stopRingTone();
					ServiceManager.cancelAVCallNotif();
					
					if(avSession.getState() == CallState.CALL_TERMINATED){
						// already terminated by termwait
						break;
					}
					avSession.setState(CallState.CALL_TERMINATED);
					
					
					if(this.avScreen != null){
						this.avScreen.runOnUiThread(new Runnable() {
							public void run() {
								AVInviteEventHandler.this.avScreen.updateState(CallState.CALL_TERMINATED);
							}});
					}
					if(this.wakeLock != null && this.wakeLock.isHeld()){
						this.wakeLock.release();
					} 
					ServiceManager.vibrate(100);
					MyAVSession.releaseSession(e.getSessionId());
					this.audioManager.setMode(AudioManager.MODE_NORMAL);
					break;
				case LOCAL_HOLD_OK:
					avSession.setLocalHold(true);
					
					if(this.avScreen != null){
						this.avScreen.runOnUiThread(new Runnable() {
							public void run() {
								AVInviteEventHandler.this.avScreen.tvInfo.setText("Call placed on hold");
							}});
					}
					break;
				case LOCAL_HOLD_NOK:
					if(this.avScreen != null){
						this.avScreen.runOnUiThread(new Runnable() {
							public void run() {
								AVInviteEventHandler.this.avScreen.tvInfo.setText("Failed to place remote party on hold");
							}});
					}
					break;
				case LOCAL_RESUME_OK:
					avSession.setLocalHold(false);
					
					if(this.avScreen != null){
						this.avScreen.runOnUiThread(new Runnable() {
							public void run() {
								AVInviteEventHandler.this.avScreen.tvInfo.setText("Call taken off hold");
							}});
					}
					break;
				case LOCAL_RESUME_NOK:
					if(this.avScreen != null){
						this.avScreen.runOnUiThread(new Runnable() {
							public void run() {
								AVInviteEventHandler.this.avScreen.tvInfo.setText("Failed to unhold call");
							}});
					}
					break;
				case REMOTE_HOLD:
					avSession.setRemoteHold(true);
					
					if(this.avScreen != null){
						this.avScreen.runOnUiThread(new Runnable() {
							public void run() {
								AVInviteEventHandler.this.avScreen.tvInfo.setText("Placed on hold by remote party");
							}});
					}
					break;
				case REMOTE_RESUME:
					avSession.setRemoteHold(false);
					
					if(this.avScreen != null){
						this.avScreen.runOnUiThread(new Runnable() {
							public void run() {
								AVInviteEventHandler.this.avScreen.tvInfo.setText("Taken off hold by remote party");
							}});
					}
					break;
			}
			
			return true;
		}
	}
	
	/* ============================ Proximity sensor =========================*/
	static class ProxSensor implements SensorEventListener
	{
		private SensorManager sensorManager;
		private Sensor proxSensor;
		private ScreenAV avScreen;
		private float maxRange;
		
		ProxSensor(ScreenAV avScreen){
			this.avScreen = avScreen;
			this.sensorManager = (SensorManager)avScreen.getSystemService(Context.SENSOR_SERVICE);
		}
		
		void start(){
			if(this.sensorManager != null && this.proxSensor == null){
				if((this.proxSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)) != null){
					this.maxRange = this.proxSensor.getMaximumRange();
					this.sensorManager.registerListener(this, this.proxSensor, SensorManager.SENSOR_DELAY_UI);
				}
			}
		}
		
		void stop(){
			if(this.sensorManager != null && this.proxSensor != null){
				this.sensorManager.unregisterListener(this);
				this.proxSensor = null;
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if(event.values != null && event.values.length >0){
				if(event.values[0] < this.maxRange){
					
				}
				else{
					
				}
			}
		}
	}
}
