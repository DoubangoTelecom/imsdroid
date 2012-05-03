/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
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
* @contributors: See $(DOUBANGO_HOME)\contributors.txt
*/
package org.doubango.imsdroid.Screens;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import org.doubango.imsdroid.Engine;
import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.Main;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Utils.DialerUtils;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnMediaPluginEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnContact;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnSipStack;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnContentType;
import org.doubango.ngn.utils.NgnGraphicsUtils;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ScreenAV extends BaseScreen{
	private static final String TAG = ScreenAV.class.getCanonicalName();
	private static final SimpleDateFormat sDurationTimerFormat = new SimpleDateFormat("mm:ss");
	
	private static int mCountBlankPacket;
	private static int mLastRotation; // values: degrees
	private boolean mSendDeviceInfo;
	private int mLastOrientation; // values: portrait, landscape...
	
	private String mRemotePartyDisplayName;
	private Bitmap mRemotePartyPhoto;
	
	private ViewType mCurrentView;
	private LayoutInflater mInflater;
	private RelativeLayout mMainLayout;
	private BroadcastReceiver mBroadCastRecv;
	
	private View mViewTrying;
	private View mViewInAudioCall;
	private View mViewInCallVideo;
	private FrameLayout mViewLocalVideoPreview;
	private FrameLayout mViewRemoteVideoPreview;
	private View mViewTermwait;
	private View mViewProxSensor;
	
	private final NgnTimer mTimerInCall;
	private final NgnTimer mTimerSuicide;
	private final NgnTimer mTimerBlankPacket;
	private NgnAVSession mAVSession;
	private boolean mIsVideoCall;
	
	private TextView mTvInfo;
	private TextView mTvDuration;
	
	private MyProxSensor mProxSensor;
	private KeyguardLock mKeyguardLock;
	private OrientationEventListener mListener;
	
	private PowerManager.WakeLock mWakeLock;
	
	private static final int SELECT_CONTENT = 1;
	
	private final static int MENU_PICKUP = 0;
	private final static int MENU_HANGUP= 1;
	private final static int MENU_HOLD_RESUME = 2;
	private final static int MENU_SEND_STOP_VIDEO = 3;
	private final static int MENU_SHARE_CONTENT = 4;
	private final static int MENU_SPEAKER = 5;
	
	private static boolean SHOW_SIP_PHRASE = true;
	
	private static enum ViewType{
		ViewNone,
		ViewTrying,
		ViewInCall,
		ViewProxSensor,
		ViewTermwait
	}
	
	public ScreenAV() {
		super(SCREEN_TYPE.AV_T, TAG);
		
		mCurrentView = ViewType.ViewNone;
		
		mTimerInCall = new NgnTimer();
		mTimerSuicide = new NgnTimer();
		mTimerBlankPacket = new NgnTimer();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_av);
		
		super.mId = getIntent().getStringExtra("id");
		if(NgnStringUtils.isNullOrEmpty(super.mId)){
			Log.e(TAG, "Invalid audio/video session");
			finish(); 
			mScreenService.show(ScreenHome.class);
			return;
		}
		mAVSession = NgnAVSession.getSession(NgnStringUtils.parseLong(super.mId, -1));
		if(mAVSession == null){
			Log.e(TAG, String.format("Cannot find audio/video session with id=%s", super.mId));
			finish(); 
			mScreenService.show(ScreenHome.class);
			return;
		}
		mAVSession.incRef();
		mAVSession.setContext(this);
		
		final NgnContact remoteParty = getEngine().getContactService().getContactByUri(mAVSession.getRemotePartyUri());
		if(remoteParty != null){
			mRemotePartyDisplayName = remoteParty.getDisplayName();
			if((mRemotePartyPhoto = remoteParty.getPhoto()) != null){
				mRemotePartyPhoto = NgnGraphicsUtils.getResizedBitmap(mRemotePartyPhoto, 
						NgnGraphicsUtils.getSizeInPixel(128), NgnGraphicsUtils.getSizeInPixel(128));
			}
		}
		else{
			mRemotePartyDisplayName = NgnUriUtils.getDisplayName(mAVSession.getRemotePartyUri());
		}
		if(NgnStringUtils.isNullOrEmpty(mRemotePartyDisplayName)){
			mRemotePartyDisplayName = "Unknown";
		}
		
		mIsVideoCall = mAVSession.getMediaType() == NgnMediaType.AudioVideo || mAVSession.getMediaType() == NgnMediaType.Video;
		
		mSendDeviceInfo = getEngine().getConfigurationService().getBoolean(NgnConfigurationEntry.GENERAL_SEND_DEVICE_INFO, NgnConfigurationEntry.DEFAULT_GENERAL_SEND_DEVICE_INFO);
		mCountBlankPacket = 0;
		mLastRotation = -1;
		mLastOrientation = -1;
		
		mInflater = LayoutInflater.from(this);
		
		mBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(intent.getAction())){
					handleSipEvent(intent);
				}
				else if(NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT.equals(intent.getAction())){
					handleMediaEvent(intent);
				}
			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
		intentFilter.addAction(NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT);
	    registerReceiver(mBroadCastRecv, intentFilter);
	    
	    if(mIsVideoCall){
		    mListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
				@Override
				public void onOrientationChanged(int orient) {
					try {				
						if ((orient > 345 || orient <15)  ||
								(orient > 75 && orient <105)   ||
								(orient > 165 && orient < 195) ||
								(orient > 255 && orient < 285)){
							int rotation = mAVSession.compensCamRotation(true);
							if (rotation != mLastRotation) {
								Log.d(ScreenAV.TAG,"Received Screen Orientation Change setRotation["+ String.valueOf(rotation)+ "]");						
								applyCamRotation(rotation);
								if(mSendDeviceInfo && mAVSession != null){
									final android.content.res.Configuration conf = getResources().getConfiguration();
									if(conf.orientation != mLastOrientation){
										mLastOrientation = conf.orientation;
										switch(mLastOrientation){
											case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
												mAVSession.sendInfo("orientation:landscape\r\nlang:fr-FR\r\n", NgnContentType.DOUBANGO_DEVICE_INFO);
												break;
											case android.content.res.Configuration.ORIENTATION_PORTRAIT:
												mAVSession.sendInfo("orientation:portrait\r\nlang:fr-FR\r\n", NgnContentType.DOUBANGO_DEVICE_INFO);
												break;
										}
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			if(!mListener.canDetectOrientation()){
				Log.w(TAG, "canDetectOrientation() is equal to false");
			}
	    }
			
		mMainLayout = (RelativeLayout)findViewById(R.id.screen_av_relativeLayout);
        loadView();
        
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG,"onStart()");
		
		final KeyguardManager keyguardManager = IMSDroid.getKeyguardManager();
		if(keyguardManager != null){
			if(mKeyguardLock == null){
				mKeyguardLock = keyguardManager.newKeyguardLock(ScreenAV.TAG);
			}
			if(keyguardManager.inKeyguardRestrictedInputMode()){
				mKeyguardLock.disableKeyguard();
			}
		}
		
		final PowerManager powerManager = IMSDroid.getPowerManager();
		if(powerManager != null && mWakeLock == null){
			mWakeLock = powerManager.newWakeLock(PowerManager.ON_AFTER_RELEASE | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
			if(mWakeLock != null){
				mWakeLock.acquire();
			}
		}
		
		if(mProxSensor == null && !IMSDroid.isBuggyProximitySensor()){
			mProxSensor = new MyProxSensor(this);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG,"onPause()");
		
		if(mProxSensor != null){
			mProxSensor.stop();
		}
		
		if(mWakeLock != null && mWakeLock.isHeld()){
			mWakeLock.release();
		}
		
		if (mListener != null && mListener.canDetectOrientation()) {
			mListener.disable();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG,"onResume()");
		
		if(mProxSensor != null){
			mProxSensor.start();
		}
		
		if (mAVSession.getState() == InviteState.INCALL) {
			mTimerInCall.schedule(mTimerTaskInCall, 0, 1000);
		}

		if (mListener != null && mListener.canDetectOrientation()) {
			mListener.enable();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG,"onStop()");
		
		if(mKeyguardLock != null){
			mKeyguardLock.reenableKeyguard();
		}
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG,"onDestroy()");
       if(mBroadCastRecv != null){
    	   unregisterReceiver(mBroadCastRecv);
    	   mBroadCastRecv = null;
       }
       
       mTimerInCall.cancel();
       mTimerSuicide.cancel();
       cancelBlankPacket();
       
       if(mWakeLock != null && mWakeLock.isHeld()){
			mWakeLock.release();
       }
       mWakeLock = null;
       
       if(mAVSession != null){
    	   mAVSession.setContext(null);
    	   mAVSession.decRef();
       }
       super.onDestroy();
	}
	
	@Override
	public boolean hasMenu(){
		return true;
	}
	
	@Override
	public boolean createOptionsMenu(Menu menu){
		if(mAVSession == null){
			return true;
		}
		
		MenuItem itemSendStopVideo = null;
		
		MenuItem itemPickUp = menu.add(0, ScreenAV.MENU_PICKUP, 0, getString(R.string.string_answer)).setIcon(R.drawable.phone_pick_up_48);
		MenuItem itemHangUp = menu.add(0, ScreenAV.MENU_HANGUP, 0, getString(R.string.string_endcall)).setIcon(R.drawable.phone_hang_up_48);
		MenuItem itemHoldResume = menu.add(0, ScreenAV.MENU_HOLD_RESUME, 0, mAVSession.isLocalHeld() ? getString(R.string.string_resume) : getString(R.string.string_hold))
			.setIcon(mAVSession.isLocalHeld() ? R.drawable.phone_resume_48 : R.drawable.phone_hold_48);
		if(mIsVideoCall){
			itemSendStopVideo = menu.add(1, ScreenAV.MENU_SEND_STOP_VIDEO, 0, getString(R.string.string_send_video));
		}
		MenuItem itemShareContent = menu.add(1, ScreenAV.MENU_SHARE_CONTENT, 0, "Share Content").setIcon(R.drawable.image_gallery_48);
		MenuItem itemSpeaker = menu.add(1, ScreenAV.MENU_SPEAKER, 0, IMSDroid.getAudioManager().isSpeakerphoneOn() ? getString(R.string.string_speaker_off) : getString(R.string.string_speaker_on))
			.setIcon(R.drawable.phone_speaker_48);
		
		switch(mAVSession.getState()){
			case INCOMING:
			{
				itemPickUp.setEnabled(true);
				itemHangUp.setEnabled(true);
				itemHoldResume.setEnabled(false);
				itemSpeaker.setEnabled(false);
				if(itemSendStopVideo != null){
					itemSendStopVideo.setEnabled(false);
				}
				itemShareContent.setEnabled(false);
				break;
			}
			
			case INPROGRESS:
			{
				itemPickUp.setEnabled(false);
				itemHangUp.setEnabled(true);
				itemHoldResume.setEnabled(false);
				itemSpeaker.setEnabled(false);
				if(itemSendStopVideo != null){
					itemSendStopVideo.setEnabled(false);
				}
				itemShareContent.setEnabled(false);
				break;
			}
			
			case INCALL:
			{
				itemHangUp.setEnabled(true);
				itemHoldResume.setEnabled(true);
				itemSpeaker.setEnabled(true);
				
				if(itemSendStopVideo != null){
					itemSendStopVideo.setTitle(mAVSession.isSendingVideo() ? "Stop Video" : getString(R.string.string_send_video))
					.setIcon(mAVSession.isSendingVideo() ? R.drawable.video_stop_48 : R.drawable.video_start_48);
					itemSendStopVideo.setEnabled(true);
					//Replace Answer by camera switcher
					itemPickUp.setEnabled(true);
					itemPickUp.setTitle(getString(R.string.string_switch_camera)).setIcon(R.drawable.refresh_48);
				}
				else{
					itemPickUp.setEnabled(false);
				}
				itemShareContent.setEnabled(true);
				break;
			}
				
			case TERMINATED:
			case TERMINATING:
			{
				itemPickUp.setEnabled(false);
				itemHangUp.setEnabled(false);
				itemHoldResume.setEnabled(false);
				itemSpeaker.setEnabled(false);
				if(itemSendStopVideo != null){
					itemSendStopVideo.setEnabled(false);
				}
				itemShareContent.setEnabled(false);
				break;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(mAVSession == null){
			return true;
		}
		switch(item.getItemId()){
			case ScreenAV.MENU_PICKUP:
			{
				if (mAVSession.getState() == InviteState.INCALL) {
					Log.d(TAG, "Toggle Camera");
					mAVSession.toggleCamera();
				} else {
					acceptCall();
				}
				break;
			}
				
			case ScreenAV.MENU_HANGUP:
			{
				if(mTvInfo != null){
					mTvInfo.setText("Ending the call...");
				}
				hangUpCall();
				break;
			}
				
			case ScreenAV.MENU_HOLD_RESUME:
			{
				if(mAVSession.isLocalHeld()){
					mAVSession.resumeCall();
				}
				else{
					mAVSession.holdCall();
				}
				break;
			}
				
			case ScreenAV.MENU_SEND_STOP_VIDEO:
			{
				 startStopVideo(!mAVSession.isSendingVideo());
				 break;
			}
				
			case ScreenAV.MENU_SHARE_CONTENT:
			{
				 Intent intent = new Intent();
				 intent.setType("*/*").addCategory(Intent.CATEGORY_OPENABLE).setAction(Intent.ACTION_GET_CONTENT);
				 startActivityForResult(Intent.createChooser(intent, "Select content"), SELECT_CONTENT);
				break;
			}
			
			case ScreenAV.MENU_SPEAKER:
			{
				mAVSession.toggleSpeakerphone();
				break;
			}
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case SELECT_CONTENT:
					if (mAVSession != null) {
						Uri selectedContentUri = data.getData();
						String selectedContentPath = super.getPath(selectedContentUri);
						ScreenFileTransferView.sendFile(mAVSession.getRemotePartyUri(), selectedContentPath);
					}
					break;
			}
		}
	}
	
	@Override
	public boolean hasBack(){
		return true;
	}
	
	@Override
	public boolean back(){
		boolean ret =  mScreenService.show(ScreenHome.class);
		if(ret){
			mScreenService.destroy(getId());
		}
		return ret;
	}
	
	public boolean onVolumeChanged(boolean bDown){
		if(mAVSession != null){
			return mAVSession.onVolumeChanged(bDown);
		}
		return false;
	}
	
	public static boolean receiveCall(NgnAVSession avSession){
		((Engine)Engine.getInstance()).getScreenService().bringToFront(Main.ACTION_SHOW_AVSCREEN,
				new String[] {"session-id", Long.toString(avSession.getId())}
		);
		return true;
	}
	
	public static boolean makeCall(String remoteUri, NgnMediaType mediaType){
		final Engine engine = (Engine)Engine.getInstance();
		final INgnSipService sipService = engine.getSipService();
		final INgnConfigurationService configurationService = engine.getConfigurationService();
		final IScreenService screenService = engine.getScreenService();
		final String validUri = NgnUriUtils.makeValidSipUri(remoteUri);
		if(validUri == null){
			Log.e(TAG, "failed to normalize sip uri '" + remoteUri + "'");
			return false;
		}
		else{
			remoteUri = validUri;
			if(remoteUri.startsWith("tel:")){
				// E.164 number => use ENUM protocol
				final NgnSipStack sipStack = sipService.getSipStack();
				if(sipStack != null){
					String phoneNumber = NgnUriUtils.getValidPhoneNumber(remoteUri);
					if(phoneNumber != null){
						String enumDomain = configurationService.getString(
								NgnConfigurationEntry.GENERAL_ENUM_DOMAIN, NgnConfigurationEntry.DEFAULT_GENERAL_ENUM_DOMAIN);
						String sipUri = sipStack.dnsENUM("E2U+SIP", phoneNumber, enumDomain);
						if(sipUri != null){
							remoteUri = sipUri;
						}
					}
				}
			}
		}
		
		final NgnAVSession avSession = NgnAVSession.createOutgoingSession(sipService.getSipStack(), mediaType);
		avSession.setRemotePartyUri(remoteUri); // HACK
		screenService.show(ScreenAV.class, Long.toString(avSession.getId()));	
		
		// Hold the active call
		final NgnAVSession activeCall = NgnAVSession.getFirstActiveCallAndNot(avSession.getId());
		if(activeCall != null){
			activeCall.holdCall();
		}
		
		return avSession.makeCall(remoteUri);
	}
	
	private void applyCamRotation(int rotation){
		if(mAVSession != null){
			mLastRotation = rotation;
			switch (rotation) {
				case 0:
				case 90:
					mAVSession.setRotation(rotation);
					mAVSession.setProducerFlipped(false);
					break;
				case 180:
					mAVSession.setRotation(0);
					mAVSession.setProducerFlipped(true);
					break;
				case 270:								
					mAVSession.setRotation(90);
					mAVSession.setProducerFlipped(true);
					break;
				}
		}
	}
	
	private boolean hangUpCall(){
		if(mAVSession != null){
			return mAVSession.hangUpCall();
		}
		return false;
	}
	
	private boolean acceptCall(){
		if(mAVSession != null){
			return mAVSession.acceptCall();
		}
		return false;
	}
	
	private void handleMediaEvent(Intent intent){
		final String action = intent.getAction();
	
		if(NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT.equals(action)){
			NgnMediaPluginEventArgs args = intent.getParcelableExtra(NgnMediaPluginEventArgs.EXTRA_EMBEDDED);
			if(args == null){
				Log.e(TAG, "Invalid event args");
				return;
			}
			
			switch(args.getEventType()){
				case STARTED_OK: //started or restarted (e.g. reINVITE)
				{
					mIsVideoCall = (mAVSession.getMediaType() == NgnMediaType.AudioVideo || mAVSession.getMediaType() == NgnMediaType.Video);
					loadView();
					
					break;
				}
				case PREPARED_OK:
				case PREPARED_NOK:
				case STARTED_NOK:
				case STOPPED_OK:
				case STOPPED_NOK:
				case PAUSED_OK:
				case PAUSED_NOK:
				{
					break;
				}
			}
		}
	}
	
	private void handleSipEvent(Intent intent){
		InviteState state;
		if(mAVSession == null){
			Log.e(TAG, "Invalid session object");
			return;
		}
		final String action = intent.getAction();
		if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)){
			NgnInviteEventArgs args = intent.getParcelableExtra(NgnInviteEventArgs.EXTRA_EMBEDDED);
			if(args == null){
				Log.e(TAG, "Invalid event args");
				return;
			}
			if(args.getSessionId() != mAVSession.getId()){
				return;
			}
			
			switch((state = mAVSession.getState())){
				case NONE:
				default:
					break;
					
				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
					loadTryingView();
					break;
					
				case EARLY_MEDIA:
				case INCALL:
					//if(state == InviteState.INCALL){
						// stop using the speaker (also done in ServiceManager())
						getEngine().getSoundService().stopRingTone();
						mAVSession.setSpeakerphoneOn(false);
					//}
					if(state == InviteState.INCALL){
						loadInCallView();
					}
					// Send blank packets to open NAT pinhole
					if(mAVSession != null){
						applyCamRotation(mAVSession.compensCamRotation(true));
						mTimerBlankPacket.schedule(mTimerTaskBlankPacket, 0, 250);
						if(!mIsVideoCall){
							mTimerInCall.schedule(mTimerTaskInCall, 0, 1000);
						}
					}
					
					// release power lock if not video call
					if(!mIsVideoCall && mWakeLock != null && mWakeLock.isHeld()){
						mWakeLock.release();
			        }
					
					switch(args.getEventType()){
						case REMOTE_DEVICE_INFO_CHANGED:
							{
								Log.d(TAG, String.format("Remote device info changed: orientation: %s", mAVSession.getRemoteDeviceInfo().getOrientation()));
								break;
							}
						case MEDIA_UPDATED:
							{
								//if((mIsVideoCall = (mAVSession.getMediaType() == NgnMediaType.AudioVideo || mAVSession.getMediaType() == NgnMediaType.Video))){
								//	loadInCallVideoView();
								//}
								//else{
								//	loadInCallAudioView();
								//}
								break;
							}
					}					
					break;
					
				case TERMINATING:
				case TERMINATED:
					mTimerSuicide.schedule(mTimerTaskSuicide, new Date(new Date().getTime() + 1500));
					mTimerTaskInCall.cancel();
					mTimerBlankPacket.cancel();
					loadTermView(SHOW_SIP_PHRASE ? args.getPhrase() : null);
					
					// release power lock
					if(mWakeLock != null && mWakeLock.isHeld()){
						mWakeLock.release();
			        }
					break;
			}
		}
	}
	
	private void loadView(){
		switch(mAVSession.getState()){
	        case INCOMING:
	        case INPROGRESS:
	        case REMOTE_RINGING:
	        case EARLY_MEDIA:
	        	loadTryingView();
	        	break;
	        	
	        case INCALL:
	        	loadInCallView();
	        	break;
	        	
	        case NONE:
	        case TERMINATING:
	        case TERMINATED:
	        default:
	        	loadTermView();
	        	break;
	    }
	}
	
	private void loadTryingView(){
		if(mCurrentView == ViewType.ViewTrying){
			return;
		}
		Log.d(TAG, "loadTryingView()");	
		
		if(mViewTrying == null){
			mViewTrying = mInflater.inflate(R.layout.view_call_trying, null);
			loadKeyboard(mViewTrying);
		}
		mTvInfo = (TextView)mViewTrying.findViewById(R.id.view_call_trying_textView_info);
		
		final TextView tvRemote = (TextView)mViewTrying.findViewById(R.id.view_call_trying_textView_remote);
		final ImageButton btPick = (ImageButton)mViewTrying.findViewById(R.id.view_call_trying_imageButton_pick);
		final ImageButton btHang = (ImageButton)mViewTrying.findViewById(R.id.view_call_trying_imageButton_hang);
		final ImageView ivAvatar = (ImageView)mViewTrying.findViewById(R.id.view_call_trying_imageView_avatar);
		btPick.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				acceptCall();
			}
		});
		btHang.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hangUpCall();
			}
		});
		
		switch(mAVSession.getState()){
	        case INCOMING:
	        	mTvInfo.setText(getString(R.string.string_call_incoming));
	        	break;
	        case INPROGRESS:
	        case REMOTE_RINGING:
	        case EARLY_MEDIA:
	        default:
	        	mTvInfo.setText(getString(R.string.string_call_outgoing));
	        	btPick.setVisibility(View.GONE);
	        	break;
	    }
		
		tvRemote.setText(mRemotePartyDisplayName);
		if(mRemotePartyPhoto != null){
			ivAvatar.setImageBitmap(mRemotePartyPhoto);
		}
		
		mMainLayout.removeAllViews();
		mMainLayout.addView(mViewTrying);
		mCurrentView = ViewType.ViewTrying;
	}
	
	private void loadInCallAudioView(){
		Log.d(TAG, "loadInCallAudioView()");
		if(mViewInAudioCall == null){
			mViewInAudioCall = mInflater.inflate(R.layout.view_call_incall_audio, null);
			loadKeyboard(mViewInAudioCall);
		}
		mTvInfo = (TextView)mViewInAudioCall.findViewById(R.id.view_call_incall_audio_textView_info);
		
		final TextView tvRemote = (TextView)mViewInAudioCall.findViewById(R.id.view_call_incall_audio_textView_remote);
		final ImageButton btHang = (ImageButton)mViewInAudioCall.findViewById(R.id.view_call_incall_audio_imageButton_hang);
		final ImageView ivAvatar = (ImageView)mViewInAudioCall.findViewById(R.id.view_call_incall_audio_imageView_avatar);
		mTvDuration = (TextView)mViewInAudioCall.findViewById(R.id.view_call_incall_audio_textView_duration);
		
		btHang.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hangUpCall();
			}
		});
		
		tvRemote.setText(mRemotePartyDisplayName);
		if(mRemotePartyPhoto != null){
			ivAvatar.setImageBitmap(mRemotePartyPhoto);
		}
		mTvInfo.setText(getString(R.string.string_incall));
		
		mViewInAudioCall.findViewById(R.id.view_call_incall_audio_imageView_secure)
			.setVisibility(mAVSession.isSecure() ? View.VISIBLE : View.INVISIBLE);
		
		mMainLayout.removeAllViews();
		mMainLayout.addView(mViewInAudioCall);
		mCurrentView = ViewType.ViewInCall;
	}
	
	private void loadInCallVideoView(){
		Log.d(TAG, "loadInCallVideoView()");
		if(mViewInCallVideo == null){
			mViewInCallVideo = mInflater.inflate(R.layout.view_call_incall_video, null);
			mViewLocalVideoPreview = (FrameLayout)mViewInCallVideo.findViewById(R.id.view_call_incall_video_FrameLayout_local_video);
			mViewRemoteVideoPreview = (FrameLayout)mViewInCallVideo.findViewById(R.id.view_call_incall_video_FrameLayout_remote_video);
		}
		if(mTvDuration != null){
			synchronized(mTvDuration){
		        mTvDuration = null;
			}
		}
		mTvInfo = null;
		mMainLayout.removeAllViews();
		mMainLayout.addView(mViewInCallVideo);
		
		mViewInCallVideo.findViewById(R.id.view_call_incall_video_imageView_secure)
				.setVisibility(mAVSession.isSecure() ? View.VISIBLE : View.INVISIBLE);
		
		// Video Consumer
		loadVideoPreview();
		
		// Video Producer
		startStopVideo(mAVSession.isSendingVideo());
		
		mCurrentView = ViewType.ViewInCall;
	}
	
	private void loadInCallView(){
		if(mCurrentView == ViewType.ViewInCall){
			return;
		}
		Log.d(TAG, "loadInCallView()");
		
		if(mIsVideoCall){
			loadInCallVideoView();
		}
		else{
			loadInCallAudioView();
		}
	}
	
	private void loadProxSensorView(){
		if(mCurrentView == ViewType.ViewProxSensor){
			return;
		}
		Log.d(TAG, "loadProxSensorView()");
		if(mViewProxSensor == null){
			mViewProxSensor = mInflater.inflate(R.layout.view_call_proxsensor, null);
		}
		mMainLayout.removeAllViews();
		mMainLayout.addView(mViewProxSensor);
		mCurrentView = ViewType.ViewProxSensor;
	}
	
	private void loadTermView(String phrase){
		Log.d(TAG, "loadTermView()");
		
		if(mViewTermwait == null){
			mViewTermwait = mInflater.inflate(R.layout.view_call_trying, null);
			loadKeyboard(mViewTermwait);
		}
		mTvInfo = (TextView)mViewTermwait.findViewById(R.id.view_call_trying_textView_info);
		mTvInfo.setText(NgnStringUtils.isNullOrEmpty(phrase) ? getString(R.string.string_call_terminated) : phrase);
		
		// loadTermView() could be called twice (onTermwait() and OnTerminated) and this is why we need to
		// update the info text for both
		if(mCurrentView == ViewType.ViewTermwait){
			return;
		}
		
		final TextView tvRemote = (TextView)mViewTermwait.findViewById(R.id.view_call_trying_textView_remote);
		final ImageView ivAvatar = (ImageView)mViewTermwait.findViewById(R.id.view_call_trying_imageView_avatar);
		mViewTermwait.findViewById(R.id.view_call_trying_imageButton_pick).setVisibility(View.GONE);
		mViewTermwait.findViewById(R.id.view_call_trying_imageButton_hang).setVisibility(View.GONE);
		mViewTermwait.setBackgroundResource(R.drawable.grad_bkg_termwait);
		
		tvRemote.setText(mRemotePartyDisplayName);
		if(mRemotePartyPhoto != null){
			ivAvatar.setImageBitmap(mRemotePartyPhoto);
		}
		
		mMainLayout.removeAllViews();
		mMainLayout.addView(mViewTermwait);
		mCurrentView = ViewType.ViewTermwait;
	}
	
	private void loadTermView(){
		loadTermView(null);
	}
	
	private void loadVideoPreview(){
		mViewRemoteVideoPreview.removeAllViews();
        final View remotePreview = mAVSession.startVideoConsumerPreview();
		if(remotePreview != null){
            final ViewParent viewParent = remotePreview.getParent();
            if(viewParent != null && viewParent instanceof ViewGroup){
                  ((ViewGroup)(viewParent)).removeView(remotePreview);
            }
            mViewRemoteVideoPreview.addView(remotePreview);
        }
	}
	
	private final TimerTask mTimerTaskInCall = new TimerTask(){
		@Override
		public void run() {
			if(mAVSession != null && mTvDuration != null){
				synchronized(mTvDuration){
					final Date date = new Date(new Date().getTime() - mAVSession.getStartTime());
					ScreenAV.this.runOnUiThread(new Runnable() {
						public void run() {
							mTvDuration.setText(sDurationTimerFormat.format(date));
						}});
				}
			}
		}
	};
	
	private final TimerTask mTimerTaskBlankPacket = new TimerTask(){
		@Override
		public void run() {	
			Log.d(TAG,"Resending Blank Packet " +String.valueOf(mCountBlankPacket));
			if (mCountBlankPacket < 3) {
				if (mAVSession != null) {
					mAVSession.pushBlankPacket();
				}
				mCountBlankPacket++;
			}
			else {
				cancel();
				mCountBlankPacket=0;
			}
		}
	};
	
	private void cancelBlankPacket(){
		if(mTimerBlankPacket != null){
			mTimerBlankPacket.cancel();
			mCountBlankPacket=0;
		}
	}
	
	private final TimerTask mTimerTaskSuicide = new TimerTask(){
		@Override
		public void run() {
			ScreenAV.this.runOnUiThread(new Runnable() {
				public void run() {
					IBaseScreen currentScreen = mScreenService.getCurrentScreen();
					boolean gotoHome = (currentScreen != null && currentScreen.getId() == getId());
					if(gotoHome){
						mScreenService.show(ScreenHome.class);
					}
					mScreenService.destroy(getId());
				}});
		}
	};
	
	
	
	private void startStopVideo(boolean bStart){
		Log.d(TAG, "startStopVideo("+bStart+")");
		if(!mIsVideoCall){
			return;
		}
		
		mAVSession.setSendingVideo(bStart);
		
		if(mViewLocalVideoPreview != null){
			mViewLocalVideoPreview.removeAllViews();
			if(bStart){
				cancelBlankPacket();
				final View localPreview = mAVSession.startVideoProducerPreview();
				if(localPreview != null){
					final ViewParent viewParent = localPreview.getParent();
					if(viewParent != null && viewParent instanceof ViewGroup){
						((ViewGroup)(viewParent)).removeView(localPreview);
					}
					if(localPreview instanceof SurfaceView){
						((SurfaceView)localPreview).setZOrderOnTop(true);
					}
					mViewLocalVideoPreview.addView(localPreview);
					mViewLocalVideoPreview.bringChildToFront(localPreview);
				}
			}
			mViewLocalVideoPreview.setVisibility(bStart ? View.VISIBLE : View.GONE);
			mViewLocalVideoPreview.bringToFront();
		}
	}
	
	private View.OnClickListener mOnKeyboardClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mAVSession != null){
				mAVSession.sendDTMF(NgnStringUtils.parseInt(v.getTag().toString(), -1));
			}
		}
	};
	
	private void loadKeyboard(View view){
		DialerUtils.setDialerTextButton(view.findViewById(R.id.view_dialer_buttons_0), "0", "+", DialerUtils.TAG_0, mOnKeyboardClickListener);
		DialerUtils.setDialerTextButton(view.findViewById(R.id.view_dialer_buttons_1), "1", "", DialerUtils.TAG_1, mOnKeyboardClickListener);
		DialerUtils.setDialerTextButton(view.findViewById(R.id.view_dialer_buttons_2), "2", "ABC", DialerUtils.TAG_2, mOnKeyboardClickListener);
		DialerUtils.setDialerTextButton(view.findViewById(R.id.view_dialer_buttons_3), "3", "DEF", DialerUtils.TAG_3, mOnKeyboardClickListener);
		DialerUtils.setDialerTextButton(view.findViewById(R.id.view_dialer_buttons_4), "4", "GHI", DialerUtils.TAG_4, mOnKeyboardClickListener);
		DialerUtils.setDialerTextButton(view.findViewById(R.id.view_dialer_buttons_5), "5", "JKL", DialerUtils.TAG_5, mOnKeyboardClickListener);
		DialerUtils.setDialerTextButton(view.findViewById(R.id.view_dialer_buttons_6), "6", "MNO", DialerUtils.TAG_6, mOnKeyboardClickListener);
		DialerUtils.setDialerTextButton(view.findViewById(R.id.view_dialer_buttons_7), "7", "PQRS", DialerUtils.TAG_7, mOnKeyboardClickListener);
		DialerUtils.setDialerTextButton(view.findViewById(R.id.view_dialer_buttons_8), "8", "TUV", DialerUtils.TAG_8, mOnKeyboardClickListener);
		DialerUtils.setDialerTextButton(view.findViewById(R.id.view_dialer_buttons_9), "9", "WXYZ", DialerUtils.TAG_9, mOnKeyboardClickListener);
		DialerUtils.setDialerTextButton(view.findViewById(R.id.view_dialer_buttons_star), "*", "", DialerUtils.TAG_STAR, mOnKeyboardClickListener);
		DialerUtils.setDialerTextButton(view.findViewById(R.id.view_dialer_buttons_sharp), "#", "", DialerUtils.TAG_SHARP, mOnKeyboardClickListener);
	}
	
	/**
	 * MyProxSensor
	 */
	static class MyProxSensor implements SensorEventListener
	{
		private final SensorManager mSensorManager;
		private Sensor mSensor;
		private final ScreenAV mAVScreen;
		private float mMaxRange;
		
		MyProxSensor(ScreenAV avScreen){
			mAVScreen = avScreen;
			mSensorManager = IMSDroid.getSensorManager();
		}
		
		void start(){
			if(mSensorManager != null && mSensor == null){
				if((mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)) != null){
					mMaxRange = mSensor.getMaximumRange();
					mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
				}
			}
		}
		
		void stop(){
			if(mSensorManager != null && mSensor != null){
				mSensorManager.unregisterListener(this);
				mSensor = null;
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			try{ // Keep it until we get a phone supporting this feature
				if(mAVScreen == null){
					Log.e(ScreenAV.TAG, "invalid state");
					return;
				}
				if(event.values != null && event.values.length >0){
					if(event.values[0] < this.mMaxRange){
						Log.d(TAG, "reenableKeyguard()");
						mAVScreen.loadProxSensorView();
					}
					else{
						Log.d(TAG, "disableKeyguard()");
						mAVScreen.loadView();
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
