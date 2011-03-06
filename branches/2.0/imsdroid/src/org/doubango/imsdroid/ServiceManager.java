package org.doubango.imsdroid;


import org.doubango.imsdroid.Events.EventArgs;
import org.doubango.imsdroid.Events.InviteEventArgs;
import org.doubango.imsdroid.Events.RegistrationEventArgs;
import org.doubango.imsdroid.Events.RegistrationEventTypes;
import org.doubango.imsdroid.Media.MyProxyPluginMgr;
import org.doubango.imsdroid.Screens.ScreenAV;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.Services.IHistoryService;
import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Services.ISoundService;
import org.doubango.imsdroid.Services.IStorageService;
import org.doubango.imsdroid.Services.Impl.ConfigurationService;
import org.doubango.imsdroid.Services.Impl.ContactService;
import org.doubango.imsdroid.Services.Impl.HistoryService;
import org.doubango.imsdroid.Services.Impl.NetworkService;
import org.doubango.imsdroid.Services.Impl.ScreenService;
import org.doubango.imsdroid.Services.Impl.SipService;
import org.doubango.imsdroid.Services.Impl.SoundService;
import org.doubango.imsdroid.Services.Impl.StorageService;
import org.doubango.imsdroid.Sip.MyAVSession;
import org.doubango.tinyWRAP.ProxyAudioConsumer;
import org.doubango.tinyWRAP.ProxyAudioProducer;
import org.doubango.tinyWRAP.ProxyVideoConsumer;
import org.doubango.tinyWRAP.ProxyVideoProducer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

public class ServiceManager   extends Service {
	private final static String TAG = ServiceManager.class.getCanonicalName();
	private static final String CONTENT_TITLE = "IMSDroid";
	public static final String ACTION_STATE_EVENT = TAG + ".ACTION_STATE_EVENT";
	
	private static boolean sStarted;
	private static boolean sInitialized;
	private static Main sMainActivity;
	private static NotificationManager sNotifManager;
	@SuppressWarnings("unused")
	private static Vibrator sVibrator;
	
	private static IConfigurationService sConfigurationService;
	private static IStorageService sStorageService;
	private static INetworkService sNetworkService;
	private static IContactService sContactService;
	private static IHistoryService sHistoryService;
	private static IScreenService sScreenService;
	private static ISipService sSipService;
	private static ISoundService sSoundService;
	
	private static BroadcastReceiver sBroadcastReceiver;
	
	private static final int NOTIF_AVCALL_ID = 19833892;
	@SuppressWarnings("unused")
	private static final int NOTIF_SMS_ID = 19833893;
	private static final int NOTIF_APP_ID = 19833894;
	
	static {
		try {
			System.load(String.format("/data/data/%s/lib/libtinyWRAP.so", Main.class
					.getPackage().getName()));
			
			ProxyVideoProducer.registerPlugin();
			ProxyVideoConsumer.registerPlugin();
			ProxyAudioProducer.registerPlugin();
			ProxyAudioConsumer.registerPlugin();
			
			MyProxyPluginMgr.Initialize();
		} catch (UnsatisfiedLinkError e) {
			Log.e(TAG,
					"Native code library failed to load.\n" + e.getMessage());
		} catch (Exception e) {
			Log.e(TAG,
					"Native code library failed to load.\n" + e.getMessage());
		}
	}
	
	public ServiceManager(){
		super();
		if(!sInitialized){
			sInitialized = ServiceManager.initialize();
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "onBind()");
		return null;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind()");
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
		
		if(sNotifManager == null){
			sNotifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart()");
		
		// register()
		sBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				
				// Registration Event
				if(SipService.ACTION_REGISTRATION_EVENT.equals(action)){
					RegistrationEventArgs args = intent.getParcelableExtra(EventArgs.EXTRA_NAME);
					@SuppressWarnings("unused")
					final RegistrationEventTypes type;
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
							showAppNotif(sSipService.isRegistered() ? R.drawable.bullet_ball_glass_green_16 : R.drawable.bullet_ball_glass_red_16, null);
							break;
					}
				}
				
				// Invite Event
				else if(SipService.ACTION_INVITE_EVENT.equals(action)){
					InviteEventArgs args = intent.getParcelableExtra(EventArgs.EXTRA_NAME);
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					
					final MyAVSession avSession = MyAVSession.getSession(args.getSessionId());
					
					switch(args.getEventType()){							
						case TERMWAIT:
						case TERMINATED:
							//if(avSession != null){ //FIXME: could be removed by SipService.OnDialogEvent()
								refreshAVCallNotif(R.drawable.phone_call_25);
							//}
							sSoundService.stopRingBackTone();
							sSoundService.stopRingTone();
							break;
							
						case INCOMING:
							if(avSession != null){
								showAVCallNotif(R.drawable.phone_call_25, "Incoming call");
								ScreenAV.receiveCall(avSession);
							}
							sSoundService.startRingTone();
							break;
							
						case INPROGRESS:
							if(avSession != null){
								showAVCallNotif(R.drawable.phone_call_25, "Outgoing call");
							}
							sSoundService.startRingBackTone();
							break;
						
						case CONNECTED:
						case EARLY_MEDIA:
							if(avSession != null){
								showAVCallNotif(R.drawable.phone_call_25, "In Call");
							}
							sSoundService.stopRingBackTone();
							sSoundService.stopRingTone();
							break;
						default: break;
					}
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SipService.ACTION_REGISTRATION_EVENT);
		intentFilter.addAction(SipService.ACTION_INVITE_EVENT);
		registerReceiver(sBroadcastReceiver, intentFilter);
		
		// alert()
		final Intent i = new Intent(ACTION_STATE_EVENT);
		i.putExtra("started", true);
		sendBroadcast(i);
	}
	
	
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		if(sBroadcastReceiver != null){
			unregisterReceiver(sBroadcastReceiver);
			sBroadcastReceiver = null;
		}
		// Cancel the persistent notifications.
		if(sNotifManager != null){
			sNotifManager.cancelAll();
		}
		super.onDestroy();
	}
	
	public static boolean initialize(){
		if(sInitialized){
			Log.w(TAG, "Already initialized");
			return true;
		}
		
		if(sConfigurationService == null){
			sConfigurationService = new ConfigurationService();
		}
		if(sStorageService == null){
			sStorageService = new StorageService();
		}
		if(sNetworkService == null){
			sNetworkService = new NetworkService();
		}
		if(sHistoryService == null){
			sHistoryService = new HistoryService();
		}
		if(sScreenService == null){
			sScreenService = new ScreenService();
		}
		if(sSoundService == null){
			sSoundService = new SoundService();
		}
		if(sContactService == null){
			sContactService = new ContactService();
		}
		if(sSipService == null){
			sSipService = new SipService();
		}
		
		
		return true;
	}
	
	public static boolean start() {
		if(sStarted){
			return true;
		}
		
		// Start Android service
		IMSDroid.getContext().startService(
				new Intent(IMSDroid.getContext(), ServiceManager.class));
		
		boolean success = true;
		
		success &= sConfigurationService.start();
		success &= sStorageService.start();
		success &= sNetworkService.start();
		success &= sHistoryService.start();
		success &= sScreenService.start();
		success &= sContactService.start();
		success &= sSipService.start();
		success &= sSoundService.start();
		
		if(success){
			success &= sHistoryService.load();
			/* success &=*/ sContactService.load();
		}
		else{
			Log.e(TAG, "Failed to start services");
		}
		
		sStarted = true;
		return success;
	}
	
	public static boolean stop() {
		if(!sStarted){
			return true;
		}
		
		boolean success = true;
		
		success &= sConfigurationService.stop();
		success &= sScreenService.stop();
		success &= sHistoryService.stop();
		success &= sStorageService.stop();
		success &= sContactService.stop();
		success &= sSipService.stop();
		success &= sSoundService.stop();
		success &= sNetworkService.stop();
		
		// stops Android service
		IMSDroid.getContext().stopService(
				new Intent(IMSDroid.getContext(), ServiceManager.class));
		if(!success){
			Log.e(TAG, "Failed to stop services");
		}
		
		sStarted = false;
		return success;
	}
	
	public static boolean isStarted(){
		return sStarted;
	}
	
	public static void setMainActivity(Main mainActivity){
		sMainActivity = mainActivity;
	}
	
	public static Main getMainActivity(){
		return sMainActivity;
	}
	
	public static IConfigurationService getConfigurationService(){
		return sConfigurationService;
	}
	
	public static IStorageService getStorageService(){
		return sStorageService;
	}
	
	public static INetworkService getNetworkService(){
		return sNetworkService;
	}
	
	public static IContactService getContactService(){
		return sContactService;
	}
	
	public static IHistoryService getHistoryService(){
		return sHistoryService;
	}
	
	public static IScreenService getScreenService(){
		return sScreenService;
	}
	
	public static ISipService getSipService(){
		return sSipService;
	}
	
	public static ISoundService getSoundService(){
		return sSoundService;
	}
	
	private static void showNotification(int notifId, int drawableId, String tickerText) {
        // Set the icon, scrolling text and timestamp
        final Notification notification = new Notification(drawableId, "", System.currentTimeMillis());
        
        Intent intent = new Intent(IMSDroid.getContext(), Main.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP  | Intent.FLAG_ACTIVITY_NEW_TASK);
        
        switch(notifId){
        	case NOTIF_APP_ID:
        		notification.flags |= Notification.FLAG_ONGOING_EVENT;
        		intent.putExtra("notif-type", "reg");
        		break;
        		
        	case NOTIF_AVCALL_ID:
        		tickerText = String.format("%s (%d)", tickerText, MyAVSession.getSize());
        		intent.putExtra("action", Main.ACTION_SHOW_AVSCREEN);
        		break;
        		
       		default:
       			
       			break;
        }
        
        PendingIntent contentIntent = PendingIntent.getActivity(IMSDroid.getContext(), notifId/*requestCode*/, intent, PendingIntent.FLAG_UPDATE_CURRENT);     

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(IMSDroid.getContext(), ServiceManager.CONTENT_TITLE, tickerText, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        ServiceManager.sNotifManager.notify(notifId, notification);
    }
	
	private static void showAppNotif(int drawableId, String tickerText){
    	Log.d(TAG, "showAppNotif");
    	ServiceManager.showNotification(NOTIF_APP_ID, drawableId, tickerText);
    }
	
	private static void showAVCallNotif(int drawableId, String tickerText){
    	ServiceManager.showNotification(NOTIF_AVCALL_ID, drawableId, tickerText);
    }
	
	@SuppressWarnings("unused")
	private static void cancelAVCallNotif(){
    	if(!MyAVSession.hasActiveSession()){
    		sNotifManager.cancel(ServiceManager.NOTIF_AVCALL_ID);
    	}
    }
    
    private static void refreshAVCallNotif(int drawableId){
    	if(!MyAVSession.hasActiveSession()){
    		sNotifManager.cancel(ServiceManager.NOTIF_AVCALL_ID);
    	}
    	else{
    		showNotification(NOTIF_AVCALL_ID, drawableId, "In Call");
    	}
    }
}