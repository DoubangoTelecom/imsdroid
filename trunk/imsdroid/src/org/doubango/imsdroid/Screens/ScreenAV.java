package org.doubango.imsdroid.Screens;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.imsdroid.events.CallEventArgs;
import org.doubango.imsdroid.events.ICallEventHandler;
import org.doubango.imsdroid.media.MediaType;
import org.doubango.imsdroid.sip.MyAVSession;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ScreenAV extends Screen 
implements ICallEventHandler {
	
	private static HashMap<String, ScreenAV> screens;
	private static SimpleDateFormat timerFormat;

	
	private boolean remoteHold;
	private boolean localHold;
	
	private String remoteUri;
	
	private MyAVSession avSession;
	
	private int timerCount;
	private final Timer timer;
	private final Timer timerSuicide;
	private final Handler handler;
	
	private LinearLayout llVideoLocal;
	private LinearLayout llVideoRemote;
	private ImageView ivState;
	private TextView tvInfo;
	private TextView tvTime;
	private ImageButton ibPickup;
	private ImageButton ibHangup;
	private ImageButton ibHoldResume;
		
	private final ISipService sipService;
	private final IScreenService screenService;
	
	static {
		ScreenAV.screens = new HashMap<String, ScreenAV>();
		ScreenAV.timerFormat = new SimpleDateFormat("HH:mm:ss");
	}
	
	public ScreenAV() {
		super(SCREEN_TYPE.AV_T, null);
		
		this.timer = new Timer();
		this.timerSuicide = new Timer();
		
		this.sipService = ServiceManager.getSipService();
		this.screenService = ServiceManager.getScreenService();
		
		this.handler = new Handler();
	}	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_av);
        
        // retrieve id
        this.id = getIntent().getStringExtra("id");
        
        ScreenAV.put(this);
        
        // get controls
        this.llVideoLocal = (LinearLayout)this.findViewById(R.id.screen_av_linearLayout_video_local);
        this.llVideoRemote = (LinearLayout)this.findViewById(R.id.screen_av_linearLayout_video_remote);
        this.ivState = (ImageView)this.findViewById(R.id.screen_av_imageView_state);
        this.tvInfo = (TextView)this.findViewById(R.id.screen_av_textView_info);
        this.tvTime = (TextView)this.findViewById(R.id.screen_av_textView_time);
        this.ibPickup = (ImageButton)this.findViewById(R.id.screen_av_imageButton_Pick);
        this.ibHangup = (ImageButton)this.findViewById(R.id.screen_av_imageButton_hangup);
        this.ibHoldResume = (ImageButton)this.findViewById(R.id.screen_av_imageButton_holdresume);
        
        this.ibPickup.setOnClickListener(this.ibPickup_OnClickListener);
        this.ibHangup.setOnClickListener(this.ibHangup_OnClickListener);
        this.ibHoldResume.setOnClickListener(this.ibHoldResume_OnClickListener);
        
        //this.timer.schedule(this.timerTask, 0, 1000);
                
        // add event handlers
        this.sipService.addCallEventHandler(this);
	}	
	
	@Override
	protected void onDestroy() {
		ScreenAV.remove(this);
		
		this.timer.cancel();
		this.timerSuicide.cancel();
		
		// remove event handlers
        this.sipService.removeCallEventHandler(this);
        
        if(avSession != null){
        	// FIXME: cleanup
        	MyAVSession.getVideoProducer().setContext(null);
        }
        
		super.onDestroy();
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
					
					// HACK: OnIncoming() event could be raised before Activity.onCreated()
					av.tvInfo.setText(String.format("Incoming Call from %s", remoteUri));
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
			return this.avSession.makeVideoCall(this.remoteUri);
		}
		return false;
	}
	
	@Override
	public boolean haveMenu(){
		return false;
	}
	
	private OnClickListener ibPickup_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(ScreenAV.this.avSession != null){
				ScreenAV.this.avSession.acceptCall();
			}
		}
	};
	
	private OnClickListener ibHangup_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(ScreenAV.this.avSession != null){
				ScreenAV.this.avSession.hangUp();
			}
		}
	};
	
	private OnClickListener ibHoldResume_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(ScreenAV.this.avSession != null){
				if(ScreenAV.this.localHold){
					ScreenAV.this.avSession.resumeCall();
				}
				else{
					ScreenAV.this.avSession.holdCall();
				}
			}
		}
	};
	
	private TimerTask timerTaskChrono = new TimerTask(){
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
		
		switch(e.getType()){
			case INCOMING:
				final String from = (String) e.getExtra("from");
				this.handler.post(new Runnable() {
					public void run() {
						setRequestedOrientation(getResources().getConfiguration().orientation);
						
						ScreenAV.this.tvInfo.setText(String.format("Incoming Call from \"%s\"", from));
						ScreenAV.this.ivState.setImageResource(R.drawable.bullet_ball_glass_grey_16);
						ServiceManager.showAVCallNotif(R.drawable.phone_call_16, "Incoming call");
						ScreenAV.this.screenService.show(ScreenAV.this.getId());
					}});
				break;
			case INPROGRESS:
				this.handler.post(new Runnable() {
					public void run() {		
						setRequestedOrientation(getResources().getConfiguration().orientation);
						
						ScreenAV.this.tvInfo.setText("In progress ...");
						ScreenAV.this.ivState.setImageResource(R.drawable.bullet_ball_glass_grey_16);
						ServiceManager.showAVCallNotif(R.drawable.phone_call_16, "Outgoing Call");
					}});
				break;
			case RINGING:
				this.handler.post(new Runnable() {
					public void run() {
						ScreenAV.this.tvInfo.setText("Ringing");
						ScreenAV.this.ivState.setImageResource(R.drawable.bullet_ball_glass_grey_16);
					}});
				break;
			case CONNECTED:
				this.handler.post(new Runnable() {
					public void run() {

						ScreenAV.this.tvInfo.setText("In call");
						ScreenAV.this.ivState.setImageResource(R.drawable.bullet_ball_glass_green_16);
						ServiceManager.showAVCallNotif(R.drawable.phone_call_16, "In Call");
						
						ScreenAV.this.llVideoLocal.removeAllViews();
						if(ScreenAV.this.avSession != null && ScreenAV.this.avSession.getMediaType() == MediaType.AudioVideo){
							final View preview = MyAVSession.getVideoProducer().startPreview();
							if(preview != null){
								ScreenAV.this.llVideoLocal.addView(preview);
							}
						}
					}});
				this.timer.schedule(ScreenAV.this.timerTaskChrono, 0, 1000);
				break;
			case DISCONNECTED:
				final String phrase = e.getPhrase();
				this.handler.post(new Runnable() {
					public void run() {
						ScreenAV.this.tvInfo.setText(phrase);
						ScreenAV.this.ivState.setImageResource(R.drawable.bullet_ball_glass_red_16);
						ServiceManager.showAVCallNotif(R.drawable.phone_call_16, "Call Terminated");
						
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
						
					}});
				/* release session */
				MyAVSession.releaseSession(this.avSession);
				/* schedule suicide */
				this.timerSuicide.schedule(this.timerTaskSuicide, new Date(new Date().getTime() + 1500));
				this.timerTaskChrono.cancel();
				break;
			case LOCAL_HOLD_OK:
				this.handler.post(new Runnable() {
					public void run() {
						ScreenAV.this.tvInfo.setText("Call placed on hold");
						ScreenAV.this.ibHoldResume.setImageResource(R.drawable.phone_resume_48);
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
						ScreenAV.this.ibHoldResume.setImageResource(R.drawable.phone_hold_48);
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
					}});
				this.remoteHold = true;
				break;
			case REMOTE_RESUME:
				this.handler.post(new Runnable() {
					public void run() {
						ScreenAV.this.tvInfo.setText("Taken off hold by remote party");
					}});
				this.remoteHold = false;
				break;
		}
		
		return true;
	}
}
