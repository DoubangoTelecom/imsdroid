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
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.HistoryAVCallEvent;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Model.HistoryEvent.StatusType;
import org.doubango.imsdroid.Services.IHistoryService;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.Services.Impl.SipService;
import org.doubango.imsdroid.events.CallEventArgs;
import org.doubango.imsdroid.events.ICallEventHandler;
import org.doubango.imsdroid.media.MediaType;
import org.doubango.imsdroid.sip.MyAVSession;
import org.doubango.imsdroid.sip.MySipStack;
import org.doubango.imsdroid.utils.UriUtils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;


public class ScreenAV extends Screen 
implements ICallEventHandler {
	
	private static HashMap<String, ScreenAV> screens;
	private static SimpleDateFormat timerFormat;
	
	
	private boolean remoteHold;
	private boolean localHold;
	private boolean sendingVideo;
	
	private String remoteUri;
	
	private MyAVSession avSession;
	
	private HistoryAVCallEvent historyEvent;
	private int timerCount;
	private final Timer timerInCall;
	private final Timer timerSuicide;
	private final Handler handler;
	private final AudioManager audioManager;
	
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
		
	private final ISipService sipService;
	private final IScreenService screenService;
	private final IHistoryService historyService;
	
	private final static int MENU_PICKUP = 0;
	private final static int MENU_HANGUP= 1;
	private final static int MENU_HOLD_RESUME = 2;
	private final static int MENU_SEND_STOP_VIDEO = 3;
	private final static int MENU_SPEAKER = 4;
	
	static {
		ScreenAV.screens = new HashMap<String, ScreenAV>();
		ScreenAV.timerFormat = new SimpleDateFormat("HH:mm:ss");
	}
	
	public ScreenAV() {
		super(SCREEN_TYPE.AV_T, null);
		
		this.timerInCall = new Timer();
		this.timerSuicide = new Timer();
		
		this.sipService = ServiceManager.getSipService();
		this.screenService = ServiceManager.getScreenService();
		this.historyService = ServiceManager.getHistoryService();
		
		this.audioManager = (AudioManager)ServiceManager.getMainActivity().getSystemService(Context.AUDIO_SERVICE);
		this.handler = new Handler();
	}	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_av);       
        
        // retrieve id
        this.id = getIntent().getStringExtra("id");
        
        ScreenAV.put(this);
        
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
        
        //this.timer.schedule(this.timerTask, 0, 1000);
        
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
        
        // add event handlers
        this.sipService.addCallEventHandler(this);
        
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
	}	
	
	@Override
	protected void onDestroy() {
		ScreenAV.remove(this);
		
		this.timerInCall.cancel();
		this.timerSuicide.cancel();
		
		// remove event handlers
        this.sipService.removeCallEventHandler(this);
        
        if(avSession != null){
        	// FIXME: cleanup
        	MyAVSession.getVideoProducer().setContext(null);
        }
        
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ScreenAV.MENU_PICKUP, 0, "Answer").setIcon(R.drawable.phone_pick_up_48);
		menu.add(0, ScreenAV.MENU_HANGUP, 0, "Hang-up").setIcon(R.drawable.phone_hang_up_48);
		menu.add(0, ScreenAV.MENU_HOLD_RESUME, 0, "Hold").setIcon(R.drawable.phone_hold_48);
		menu.add(1, ScreenAV.MENU_SEND_STOP_VIDEO, 0, "Send Video").setIcon(R.drawable.video_start_48);
		menu.add(1, ScreenAV.MENU_SPEAKER, 0, "Speaker").setIcon(R.drawable.phone_speaker_48);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		MenuItem item;
		if((item = menu.findItem(ScreenAV.MENU_HOLD_RESUME)) != null){
			item.setTitle(this.localHold? "Resume" : "Hold").setIcon(this.localHold? R.drawable.phone_resume_48 : R.drawable.phone_hold_48);
		}
		
		if((item = menu.findItem(ScreenAV.MENU_SEND_STOP_VIDEO)) != null){
			if(this.avSession != null && this.avSession.getMediaType() == MediaType.AudioVideo){
				item.setTitle(this.sendingVideo? "Stop Video" : "Send Video").setIcon(this.sendingVideo? R.drawable.video_stop_48 : R.drawable.video_start_48);
				item.setEnabled(true);
			}
			else{
				item.setEnabled(false);
			}
		}
		
		if((item = menu.findItem(ScreenAV.MENU_SPEAKER)) != null){
			item.setTitle(this.audioManager.isSpeakerphoneOn() ? "Speaker OFF" : "Speaker ON");
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
			case ScreenAV.MENU_PICKUP:
				if(ScreenAV.this.avSession != null){
					MyAVSession.getVideoProducer().setContext(this);
					MyAVSession.getVideoConsumer().setContext(this);
					
					ScreenAV.this.avSession.acceptCall();
				}
				break;
				
			case ScreenAV.MENU_HANGUP:
				if(ScreenAV.this.avSession != null){
					ScreenAV.this.avSession.hangUp();
				}
				break;
				
			case ScreenAV.MENU_HOLD_RESUME:
				if(ScreenAV.this.avSession != null){
					if(ScreenAV.this.localHold){
						ScreenAV.this.avSession.resumeCall();
					}
					else{
						ScreenAV.this.avSession.holdCall();
					}
				}
				break;
				
			case ScreenAV.MENU_SEND_STOP_VIDEO:
				this.llVideoLocal.removeAllViews();
				if(!this.sendingVideo){
					if(this.avSession != null && this.avSession.getMediaType() == MediaType.AudioVideo){
						View local_preview = MyAVSession.getVideoProducer().startPreview();
						if(local_preview != null){
							ScreenAV.this.llVideoLocal.removeAllViews();
							ScreenAV.this.llVideoLocal.addView(local_preview);
						}
					}
					this.sendingVideo = true;
				}
				else{
					this.sendingVideo = false;
				}
				break;
				
			case ScreenAV.MENU_SPEAKER:
				this.audioManager.setSpeakerphoneOn(!this.audioManager.isSpeakerphoneOn());
				break;
		}
		return true;
	}
	
	
	/* ===================== IScreen (Screen) ======================== */
	@Override
	public boolean haveMenu(){
		return true;
	}
	
	private static void put(ScreenAV screen){
		synchronized(ScreenAV.screens){
			ScreenAV.screens.put(screen.getId(), screen);
		}
	}
	
	private static void remove(ScreenAV screen){
		synchronized(ScreenAV.screens){
			ScreenAV.screens.remove(screen.getId());
		}
	}
	
	public static String getCurrent(){
		synchronized(ScreenAV.screens){
			if(!ScreenAV.screens.isEmpty()){
				return ScreenAV.screens.entrySet().iterator().next().getKey();
			}
		}
		return null;
	}
	
	public static boolean makeCall(String remoteUri, MediaType mediaType){
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
		
		String id = UUID.randomUUID().toString();
		ServiceManager.getScreenService().show(ScreenAV.class, id);
		
		ScreenAV av = (ScreenAV)ServiceManager.getScreenService().getScreen(id);
		if(av != null){
			switch(mediaType){
				case AudioVideo:
					return av.makeVideoCall(remoteUri);
				default:
					return av.makeAudioCall(remoteUri);
			}
		}
		return false;
	}
	
	public static boolean receiveCall(MyAVSession avSession, String remoteUri, MediaType mediaType){
		String id = UUID.randomUUID().toString();
		ServiceManager.getScreenService().show(ScreenAV.class, id);
		
		ScreenAV av = (ScreenAV)ServiceManager.getScreenService().getScreen(id);
		if(av != null){
			switch(mediaType){
				case Audio:
				case AudioVideo:
					av.avSession = avSession;
					av.remoteUri = remoteUri;
					
					/***** HACK: OnIncoming() event could be raised before Activity.onCreated() ******/
					av.historyEvent = new HistoryAVCallEvent(mediaType == MediaType.AudioVideo, remoteUri);
					av.historyEvent.setStatus(StatusType.Incoming);
					
					ServiceManager.vibrate(500);
					ServiceManager.getSoundService().playRingTone();
					
					av.tvInfo.setText(String.format("Incoming Call from %s", UriUtils.getDisplayName(remoteUri)));
					av.ivState.setImageResource(R.drawable.bullet_ball_glass_grey_16);
					ServiceManager.showAVCallNotif(R.drawable.phone_call_16, "Incoming call");
					return true;
				default:
					return false;
			}
		}
		return false;
	}
	
	public boolean makeAudioCall(String remoteUri){
		if(this.avSession == null){
			this.remoteUri = remoteUri;
			this.avSession = MyAVSession.createOutgoingSession(this.sipService.getStack(), MediaType.Audio);
			return this.avSession.makeAudioCall(this.remoteUri);
		}
		return false;
	}
	
	public boolean makeVideoCall(String remoteUri){
		if(this.avSession == null){
			this.remoteUri = remoteUri;
			this.avSession = MyAVSession.createOutgoingSession(this.sipService.getStack(), MediaType.AudioVideo);
			
			MyAVSession.getVideoProducer().setContext(this);
			MyAVSession.getVideoConsumer().setContext(this);
			
			return this.avSession.makeVideoCall(this.remoteUri);
		}
		return false;
	}
	
	private TimerTask timerTaskInCall = new TimerTask(){
		@Override
		public void run() {
			ScreenAV.this.timerCount++;
			final Date date = new Date(2000, 1, 1, ScreenAV.this.timerCount/3600, ScreenAV.this.timerCount/60, ScreenAV.this.timerCount%60);
			ScreenAV.this.handler.post(new Runnable() {
				public void run() {
					ScreenAV.this.tvTime.setText(ScreenAV.timerFormat.format(date));
				}});
		}
	};
	
	private TimerTask timerTaskSuicide = new TimerTask(){
		@Override
		public void run() {
			ScreenAV.this.handler.post(new Runnable() {
				public void run() {
					ServiceManager.cancelAVCallNotif();
					ScreenAV.this.screenService.show(ScreenHome.class);
					ScreenAV.this.screenService.destroy(ScreenAV.this.getId());
				}});
		}
	};
	

	@Override
	public boolean onCallEvent(Object sender, CallEventArgs e) {
		if(this.avSession == null || this.avSession.getId() != e.getSessionId()){
			return true;
		}
		
		final String phrase = e.getPhrase();
		
		switch(e.getType()){
			case INCOMING:
				ScreenAV.this.remoteUri = (String) e.getExtra("from");
				this.handler.post(new Runnable() {
					public void run() {
						setRequestedOrientation(getResources().getConfiguration().orientation);
						
						ScreenAV.this.historyEvent = new HistoryAVCallEvent(ScreenAV.this.avSession.getMediaType() == MediaType.AudioVideo, ScreenAV.this.remoteUri);
						ScreenAV.this.historyEvent.setStatus(StatusType.Incoming);
						
						ServiceManager.vibrate(500);
						ServiceManager.getSoundService().playRingTone();
						
						ScreenAV.this.tvInfo.setText(String.format("Incoming Call from \"%s\"", UriUtils.getDisplayName(ScreenAV.this.remoteUri)));
						ScreenAV.this.ivState.setImageResource(R.drawable.bullet_ball_glass_grey_16);
						ServiceManager.showAVCallNotif(R.drawable.phone_call_16, "Incoming call");
						ScreenAV.this.screenService.show(ScreenAV.this.getId());
					}});
				break;
			case INPROGRESS:
				this.handler.post(new Runnable() {
					public void run() {		
						setRequestedOrientation(getResources().getConfiguration().orientation);
						
						ScreenAV.this.historyEvent = new HistoryAVCallEvent(ScreenAV.this.avSession.getMediaType() == MediaType.AudioVideo, ScreenAV.this.remoteUri);
						ScreenAV.this.historyEvent.setStatus(StatusType.Outgoing);
						
						ServiceManager.getSoundService().playRingBackTone();
						
						ScreenAV.this.tvInfo.setText("In progress ...");
						ScreenAV.this.ivState.setImageResource(R.drawable.bullet_ball_glass_grey_16);
						ServiceManager.showAVCallNotif(R.drawable.phone_call_16, "Outgoing Call");
					}});
				break;
			case RINGING:
				this.handler.post(new Runnable() {
					public void run() {
						// FIXME: Not raised by the SIP stack
						//ServiceManager.getSoundService().playRingBackTone();
						
						ScreenAV.this.tvInfo.setText("Ringing");
						ScreenAV.this.ivState.setImageResource(R.drawable.bullet_ball_glass_grey_16);
					}});
				break;
			case EARLY_MEDIA:
				this.handler.post(new Runnable() {
					public void run() {
						ServiceManager.getSoundService().stopRingBackTone();
						ServiceManager.getSoundService().stopRingTone();
						
						ScreenAV.this.audioManager.setMode(AudioManager.MODE_IN_CALL);
						ScreenAV.this.audioManager.setSpeakerphoneOn(false);
						
						// Notification
						ScreenAV.this.tvInfo.setText(phrase);
					}});
				break;
			case CONNECTED:
				this.handler.post(new Runnable() {
					public void run() {
						ServiceManager.getSoundService().stopRingBackTone();
						ServiceManager.getSoundService().stopRingTone();
						
						ScreenAV.this.audioManager.setMode(AudioManager.MODE_IN_CALL);
						ScreenAV.this.audioManager.setSpeakerphoneOn(false);
						
						// Notification
						ScreenAV.this.tvInfo.setText("In call");
						ScreenAV.this.ivState.setImageResource(R.drawable.bullet_ball_glass_green_16);
						ServiceManager.showAVCallNotif(R.drawable.phone_call_25, "In Call");
						
						// History event
						if(ScreenAV.this.historyEvent != null){
							ScreenAV.this.historyEvent.setStartTime(new Date().getTime());
						}
						
						// Views
						//ScreenAV.this.llVideoLocal.removeAllViews();
						ScreenAV.this.llVideoRemote.removeAllViews();
						if(ScreenAV.this.avSession != null && ScreenAV.this.avSession.getMediaType() == MediaType.AudioVideo){
							
							//final View local_preview = MyAVSession.getVideoProducer().startPreview();
							final View remote_preview = MyAVSession.getVideoConsumer().startPreview();
							//remote_preview.setLayoutParams(new LinearLayout.LayoutParams(
							//          LinearLayout.LayoutParams.FILL_PARENT,
							//          LinearLayout.LayoutParams.FILL_PARENT
							//      ));

							//if(local_preview != null){
							//	ScreenAV.this.llVideoLocal.addView(local_preview);
							//}
							if(remote_preview != null){
								ScreenAV.this.llVideoRemote.addView(remote_preview);
							}
						}
						
						ScreenAV.this.tvRemoteUri.setText(String.format("In call with %s since:", UriUtils.getDisplayName(ScreenAV.this.remoteUri)));
					}});
				this.timerInCall.schedule(ScreenAV.this.timerTaskInCall, 0, 1000);
				break;
			case DISCONNECTED:
				this.handler.post(new Runnable() {
					public void run() {
						
						ScreenAV.this.tvInfo.setText(phrase);
						ScreenAV.this.ivState.setImageResource(R.drawable.bullet_ball_glass_red_16);
						ServiceManager.showAVCallNotif(R.drawable.phone_call_16, "Call Terminated");
						
						ServiceManager.getSoundService().stopRingBackTone();
						ServiceManager.getSoundService().stopRingTone();
						
						if (ScreenAV.this.historyEvent != null) {
							// StartTime should be updated by onConnected() event,
							// otherwise both times are equal
							if (ScreenAV.this.historyEvent.getStartTime() == ScreenAV.this.historyEvent
									.getEndTime()) {
								if (ScreenAV.this.historyEvent.getStatus() == StatusType.Incoming) {
									ScreenAV.this.historyEvent
											.setStatus(StatusType.Missed);
								}
							} else {
								ScreenAV.this.historyEvent.setEndTime(new Date()
										.getTime());
							}
							ScreenAV.this.historyService.addEvent(ScreenAV.this.historyEvent);
						}
						
						ScreenAV.this.audioManager.setMode(AudioManager.MODE_NORMAL);
						
						ServiceManager.vibrate(100);
					}});
				/* release session */
				MyAVSession.releaseSession(this.avSession);
				/* schedule suicide */
				this.timerSuicide.schedule(this.timerTaskSuicide, new Date(new Date().getTime() + 1500));
				this.timerTaskInCall.cancel();
				break;
			case LOCAL_HOLD_OK:
				this.handler.post(new Runnable() {
					public void run() {
						ScreenAV.this.tvInfo.setText("Call placed on hold");
						ScreenAV.this.localHold = true;
					}});
				this.localHold = true;
				break;
			case LOCAL_HOLD_NOK:
				this.handler.post(new Runnable() {
					public void run() {
						ScreenAV.this.tvInfo.setText("Local hold NOK");
					}});
				break;
			case LOCAL_RESUME_OK:
				this.handler.post(new Runnable() {
					public void run() {
						ScreenAV.this.tvInfo.setText("Call taken off hold");
						ScreenAV.this.localHold = false;
					}});
				this.localHold = false;
				break;
			case LOCAL_RESUME_NOK:
				this.handler.post(new Runnable() {
					public void run() {
						ScreenAV.this.tvInfo.setText("Local Resume NOK");
					}});
				break;
			case REMOTE_HOLD:
				this.handler.post(new Runnable() {
					public void run() {
						ScreenAV.this.tvInfo.setText("Placed on hold by remote party");
						ScreenAV.this.remoteHold = true;
					}});
				this.remoteHold = true;
				break;
			case REMOTE_RESUME:
				this.handler.post(new Runnable() {
					public void run() {
						ScreenAV.this.tvInfo.setText("Taken off hold by remote party");
						ScreenAV.this.remoteHold = false;
					}});
				this.remoteHold = false;
				break;
		}
		
		return true;
	}
	
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
}
