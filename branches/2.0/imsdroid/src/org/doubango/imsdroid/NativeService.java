package org.doubango.imsdroid;

import org.doubango.imsdroid.Screens.ScreenAV;
import org.doubango.ngn.NgnNativeService;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnMessagingEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventTypes;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.sip.NgnAVSession;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

public class NativeService extends NgnNativeService {
	private final static String TAG = NativeService.class.getCanonicalName();
	public static final String ACTION_STATE_EVENT = TAG + ".ACTION_STATE_EVENT";
	
	private PowerManager.WakeLock mWakeLock;
	private BroadcastReceiver mBroadcastReceiver;
	private Engine mEngine;
	
	public NativeService(){
		super();
		mEngine = (Engine)Engine.getInstance();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
		
		final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if(powerManager != null && mWakeLock == null){
			mWakeLock = powerManager.newWakeLock(PowerManager.ON_AFTER_RELEASE 
					| PowerManager.SCREEN_BRIGHT_WAKE_LOCK 
					| PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
		}
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart()");
		
		// register()
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				
				// Registration Event
				if(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
					NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
					final NgnRegistrationEventTypes type;
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					switch((type = args.getEventType())){
						case REGISTRATION_OK:
						case REGISTRATION_NOK:
						case REGISTRATION_INPROGRESS:
						case UNREGISTRATION_INPROGRESS:
						case UNREGISTRATION_OK:
						case UNREGISTRATION_NOK:
						default:
							final boolean bTrying = (type == NgnRegistrationEventTypes.REGISTRATION_INPROGRESS || type == NgnRegistrationEventTypes.UNREGISTRATION_INPROGRESS);
							if(mEngine.getSipService().isRegistered()){
								mEngine.showAppNotif(bTrying ?R.drawable.bullet_ball_glass_grey_16 : R.drawable.bullet_ball_glass_green_16, null);
								IMSDroid.acquirePowerLock();
							}
							else{
								mEngine.showAppNotif(bTrying ?R.drawable.bullet_ball_glass_grey_16 : R.drawable.bullet_ball_glass_red_16, null);
								IMSDroid.releasePowerLock();
							}
							break;
					}
				}
				
				// Messaging Event
				if(NgnMessagingEventArgs.ACTION_MESSAGING_EVENT.equals(action)){
					NgnMessagingEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					switch(args.getEventType()){
						case INCOMING:
//							String dateString = intent.getStringExtra(NgnMessagingEventArgs.EXTRA_DATE);
//							String remoteParty = intent.getStringExtra(NgnMessagingEventArgs.EXTRA_REMOTE_PARTY);
//							if(NgnStringUtils.isNullOrEmpty(remoteParty)){
//								remoteParty = NgnStringUtils.nullValue();
//							}
//							NgnHistorySMSEvent event = new NgnHistorySMSEvent(remoteParty, StatusType.Incoming);
//							event.setContent(new String(args.getPayload()));
//							event.setStartTime(DateTimeUtils.parseKASDate(dateString).getTime());
//							mEngine.getHistoryService().addEvent(event);
							break;
					}
				}
				
				// Invite Event
				else if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)){
					NgnInviteEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					
					final NgnMediaType mediaType = args.getMediaType();
					
					switch(args.getEventType()){							
						case TERMWAIT:
						case TERMINATED:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								mEngine.refreshAVCallNotif(R.drawable.phone_call_25);
								mEngine.getSoundService().stopRingBackTone();
								mEngine.getSoundService().stopRingTone();
							}
							else if(NgnMediaType.isFileTransfer(mediaType)){
								mEngine.refreshContentShareNotif(R.drawable.image_gallery_25);
							}
							break;
							
						case INCOMING:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								final NgnAVSession avSession = NgnAVSession.getSession(args.getSessionId());
								if(avSession != null){
									mEngine.showAVCallNotif(R.drawable.phone_call_25, getString(R.string.string_call_incoming));
									ScreenAV.receiveCall(avSession);
									if(mWakeLock != null && !mWakeLock.isHeld()){
										mWakeLock.acquire(1);
									}
									mEngine.getSoundService().startRingTone();
								}
								else{
									Log.e(TAG, String.format("Failed to find session with id=%ld", args.getSessionId()));
								}
							}
							else if(NgnMediaType.isFileTransfer(mediaType)){
								mEngine.refreshContentShareNotif(R.drawable.image_gallery_25);
							}
							break;
							
						case INPROGRESS:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								mEngine.showAVCallNotif(R.drawable.phone_call_25, getString(R.string.string_call_outgoing));
							}
							else if(NgnMediaType.isFileTransfer(mediaType)){
								mEngine.refreshContentShareNotif(R.drawable.image_gallery_25);
							}
							break;
							
						case RINGING:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								mEngine.getSoundService().startRingBackTone();
							}
							break;
						
						case CONNECTED:
						case EARLY_MEDIA:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								mEngine.showAVCallNotif(R.drawable.phone_call_25, getString(R.string.string_incall));
								mEngine.getSoundService().stopRingBackTone();
								mEngine.getSoundService().stopRingTone();
							}
							break;
						default: break;
					}
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
		intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
		intentFilter.addAction(NgnMessagingEventArgs.ACTION_MESSAGING_EVENT);
		registerReceiver(mBroadcastReceiver, intentFilter);
		
		if(intent != null){
			Bundle bundle = intent.getExtras();
			if (bundle != null && bundle.getBoolean("autostarted")) {
				if (mEngine.start()) {
					mEngine.getSipService().register(null);
				}
			}
		}
		
		// alert()
		final Intent i = new Intent(ACTION_STATE_EVENT);
		i.putExtra("started", true);
		sendBroadcast(i);
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		if(mBroadcastReceiver != null){
			unregisterReceiver(mBroadcastReceiver);
			mBroadcastReceiver = null;
		}
		if(mWakeLock != null){
			if(mWakeLock.isHeld()){
				mWakeLock.release();
				mWakeLock = null;
			}
		}
		super.onDestroy();
	}
}
