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
package org.doubango.imsdroid.Services.Impl;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.Main;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.ObservableHashMap;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.Services.IHistoryService;
import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Services.ISoundService;
import org.doubango.imsdroid.Services.IStorageService;
import org.doubango.imsdroid.Services.IXcapService;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.RegistrationEventArgs;
import org.doubango.imsdroid.events.RegistrationEventTypes;
import org.doubango.imsdroid.sip.MyAVSession;
import org.doubango.tinyWRAP.ProxyAudioConsumer;
import org.doubango.tinyWRAP.ProxyAudioProducer;
import org.doubango.tinyWRAP.ProxyVideoConsumer;
import org.doubango.tinyWRAP.ProxyVideoProducer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

/**
 * Screen Manager. Entry point to retrieve all services (Singletons).
 * 
 * @author root
 * 
 */
public class ServiceManager  extends Service 
implements IRegistrationEventHandler
{
	
	static {
		try {
			//System.loadLibrary("tinyWRAP");
			System.load(String.format("/data/data/%s/lib/libtinyWRAP.so", Main.class
					.getPackage().getName()));
			
			ProxyVideoProducer.registerPlugin();
			ProxyVideoConsumer.registerPlugin();
			ProxyAudioProducer.registerPlugin();
			ProxyAudioConsumer.registerPlugin();
			// See MyProxyPluginMgr for Default chromas

		} catch (UnsatisfiedLinkError e) {
			Log.e(Main.class.getCanonicalName(),
					"Native code library failed to load.\n" + e.getMessage());
		} catch (Exception e) {
			Log.e(Main.class.getCanonicalName(),
					"Native code library failed to load.\n" + e.getMessage());
		}
	}
	
	/* Singletons */
	private static final ConfigurationService configurationService = new ConfigurationService();
	private static final NetworkService networkService = new NetworkService();
	private static final XcapService xcapService = new XcapService();
	private static final ContactService contactService = new ContactService();
	private static final HistoryService historyService = new HistoryService();
	private static final ScreenService screenService = new ScreenService();
	private static final SipService sipService = new SipService();
	private static final SoundService soundService = new SoundService();
	private static final StorageService storageService = new StorageService();
	
	private static final String TAG = ServiceManager.class.getCanonicalName();
	private static final String CONTENT_TITLE = "IMSDroid";
	
	private static Vibrator vibrator;
	
	private static boolean started;
	private static Main mainActivity;
	private static NotificationManager notifManager;
	private static final int NOTIF_REGISTRATION_ID = 19833891;
	private static final int NOTIF_AVCALL_ID = 19833892;
	private static final int NOTIF_SMS_ID = 19833893;
	private static final int NOTIF_CONTSHARE_ID = 19833894;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(ServiceManager.notifManager == null){
			ServiceManager.notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}
		
		// Display a notification about us starting.  We put an icon in the status bar.
		ServiceManager.showRegistartionNotif(R.drawable.bullet_ball_glass_red_16, "You are not connected");
		
		ServiceManager.sipService.addRegistrationEventHandler(this);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		Bundle bundle = intent.getExtras();
		if(bundle != null && bundle.getBoolean("autostarted")){
			ServiceManager.start();
			getSipService().register();
		}
		
		/* autostarts next time, unless user explicitly exited */
		SharedPreferences settings = getSharedPreferences(Main.class.getCanonicalName(), 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("autostarts", true);
		editor.commit();
	}	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// Cancel the persistent notifications.
		ServiceManager.notifManager.cancel(ServiceManager.NOTIF_REGISTRATION_ID);
		ServiceManager.notifManager.cancel(ServiceManager.NOTIF_AVCALL_ID);

		ServiceManager.sipService.removeRegistrationEventHandler(this);
		
        // Tell the user we stopped.
        //Toast.makeText(this, "imsdroid shutting down...", Toast.LENGTH_SHORT).show();
	}
	
    private static void showNotification(int notifId, int drawableId, String tickerText) {
        // Set the icon, scrolling text and timestamp
        final Notification notification = new Notification(drawableId, "", System.currentTimeMillis());
        
        Intent intent = new Intent(IMSDroid.getContext(), Main.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP  | Intent.FLAG_ACTIVITY_NEW_TASK);
        
        switch(notifId){
        	case NOTIF_REGISTRATION_ID:
        		//notification.ledARGB = 0xff00ff00;
        		//notification.ledOnMS = 300;
        		//notification.ledOffMS = 1000;
        		notification.flags |= Notification.FLAG_ONGOING_EVENT /*| Notification.FLAG_SHOW_LIGHTS*/;
        		//notification.defaults |= Notification.DEFAULT_SOUND;
        		intent.putExtra("notif-type", "reg");
        		/* Main activity already onTop -> do not pass the screen Id */
        		break;
        	case NOTIF_SMS_ID:
        		notification.flags |= Notification.FLAG_AUTO_CANCEL;
        		intent.putExtra("action", Main.ACTION_SHOW_HISTORY);
        		break;
        	case NOTIF_CONTSHARE_ID:
        		//notification.flags |= Notification.FLAG_AUTO_CANCEL;
        		intent.putExtra("action", Main.ACTION_SHOW_CONTSHARE_SCREEN);
        		break;
        	case NOTIF_AVCALL_ID:
        		final ObservableHashMap<Long, MyAVSession> sessions  = MyAVSession.getSessions();
        		tickerText = String.format("%s (%d)", tickerText, sessions.size());
        		if(sessions.size()>0){
        			intent.putExtra("action", Main.ACTION_SHOW_AVCALLS_SCREEN);
        			//--intent.putExtra("action", Main.ACTION_SHOW_AVSCREEN);
        			//--intent.putExtra("session-id", MyAVSession.getFirstId().toString());
        		}
        		break;
       		default:
       			
       			break;
        }
        
        PendingIntent contentIntent = PendingIntent.getActivity(IMSDroid.getContext(), notifId/*requestCode*/, intent, PendingIntent.FLAG_UPDATE_CURRENT);     

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(IMSDroid.getContext(), ServiceManager.CONTENT_TITLE, tickerText, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        ServiceManager.notifManager.notify(notifId, notification);
    }
    
    public static void showRegistartionNotif(int drawableId, String tickerText){
    	Log.d(ServiceManager.TAG, "showRegistartionNotif");
    	ServiceManager.showNotification(NOTIF_REGISTRATION_ID, drawableId, tickerText);
    }
    
    public static void showAVCallNotif(int drawableId, String tickerText){
    	ServiceManager.showNotification(NOTIF_AVCALL_ID, drawableId, tickerText);
    }
    
    public static void showSMSNotif(int drawableId, String tickerText){
    	ServiceManager.showNotification(NOTIF_SMS_ID, drawableId, tickerText);
    }
    
    public static void showContShareNotif(int drawableId, String tickerText){
    	ServiceManager.showNotification(NOTIF_CONTSHARE_ID, drawableId, tickerText);
    }
    
    public static void cancelAVCallNotif(boolean force){
    	if(force || MyAVSession.getSessions().size()==0){
    		ServiceManager.notifManager.cancel(ServiceManager.NOTIF_AVCALL_ID);
    	}
    }
    
    public static void refreshAVCallNotif(int drawableId){
    	if(MyAVSession.getSessions().size()==0){
    		ServiceManager.notifManager.cancel(ServiceManager.NOTIF_AVCALL_ID);
    	}
    	else{
    		ServiceManager.showNotification(NOTIF_AVCALL_ID, drawableId, "In Call");
    	}
    }
    
    public static void cancelContShareNotif(){
    	ServiceManager.notifManager.cancel(ServiceManager.NOTIF_CONTSHARE_ID);
    }
    
	public static void setMainActivity(Main mainActivity){
		ServiceManager.mainActivity = mainActivity;
	}
	
	public static Main getMainActivity(){
		return ServiceManager.mainActivity;
	}
	
	/**
	 * Starts all services
	 * 
	 * @return true if succeed and false otherwise
	 */
	public static boolean start() {
		if(ServiceManager.started){
			return true;
		}
		
		// Start Android service
		IMSDroid.getContext().startService(
				new Intent(IMSDroid.getContext(), ServiceManager.class));
		
		boolean success = true;

		success &= ServiceManager.configurationService.start();
		success &= ServiceManager.contactService.start();
		success &= ServiceManager.historyService.start();
		success &= ServiceManager.networkService.start();
		success &= ServiceManager.screenService.start();
		success &= ServiceManager.sipService.start();
		success &= ServiceManager.soundService.start();
		success &= ServiceManager.storageService.start();
		success &= ServiceManager.xcapService.start();

		if(!success){
			Log.e(ServiceManager.TAG, "Failed to start services");
		}
		
		ServiceManager.started = true;
		return true;
	}

	/**
	 * Stops all services
	 * 
	 * @return true if succeed and false otherwise
	 */
	public static boolean stop() {
		if(!ServiceManager.started){
			return true;
		}
		
		// stops Android service
		IMSDroid.getContext().stopService(
				new Intent(IMSDroid.getContext(), ServiceManager.class));
		
		boolean success = true;

		success &= ServiceManager.configurationService.stop();
		success &= ServiceManager.contactService.stop();
		success &= ServiceManager.historyService.stop();
		success &= ServiceManager.networkService.stop();
		success &= ServiceManager.screenService.stop();
		success &= ServiceManager.sipService.stop();
		success &= ServiceManager.soundService.stop();
		success &= ServiceManager.storageService.stop();
		success &= ServiceManager.xcapService.stop();

		ServiceManager.notifManager.cancel(ServiceManager.NOTIF_REGISTRATION_ID);
		ServiceManager.notifManager.cancel(ServiceManager.NOTIF_AVCALL_ID);
		
		if(!success){
			Log.e(ServiceManager.TAG, "Failed to stop services");
		}
		
		ServiceManager.started = false;
		return success;
	}
	
	public static void vibrate(long milliseconds){
		if(ServiceManager.vibrator == null){
			ServiceManager.vibrator = (Vibrator)IMSDroid.getContext().getSystemService(Context.VIBRATOR_SERVICE);
		}
		ServiceManager.vibrator.vibrate(milliseconds);
	}
	
	/**
	 * Gets the Configuration Service.
	 * 
	 * @return
	 */
	public static IConfigurationService getConfigurationService() {
		return ServiceManager.configurationService;
	}

	/**
	 * Gets the Configuration Service.
	 * 
	 * @return
	 */
	public static IContactService getContactService() {
		return (IContactService) ServiceManager.contactService;
	}
	
	/**
	 * Gets the History Service.
	 * 
	 * @return
	 */
	public static IHistoryService getHistoryService() {
		return ServiceManager.historyService;
	}
	
	/**
	 * Gets the Network Service.
	 * 
	 * @return
	 */
	public static INetworkService getNetworkService() {
		return (INetworkService) ServiceManager.networkService;
	}

	/**
	 * Gets the Screen Service.
	 * 
	 * @return
	 */
	public static IScreenService getScreenService() {
		return (IScreenService) ServiceManager.screenService;
	}

	/**
	 * Gets the Sip Service.
	 * 
	 * @return
	 */
	public static ISipService getSipService() {
		return (ISipService) ServiceManager.sipService;
	}

	/**
	 * Gets the Sound Service.
	 * 
	 * @return
	 */
	public static ISoundService getSoundService() {
		return (ISoundService) ServiceManager.soundService;
	}
	
	/**
	 * Gets the Storage Service.
	 * 
	 * @return
	 */
	public static IStorageService getStorageService() {
		return (IStorageService) ServiceManager.storageService;
	}

	/**
	 * Gets the XCAP Service.
	 * 
	 * @return
	 */
	public static IXcapService getXcapService() {
		return ServiceManager.xcapService;
	}
	
	
	
	/* ===================== Sip Events ========================*/
	public boolean onRegistrationEvent(Object sender, RegistrationEventArgs e) {
		Log.i(this.getClass().getName(), "onRegistrationEvent");
		
		final RegistrationEventTypes type = e.getType();
		final short code = e.getSipCode();
		final String phrase = e.getPhrase();
		
		switch(type){
			case REGISTRATION_OK:
				ServiceManager.showRegistartionNotif(R.drawable.bullet_ball_glass_green_16, "You are connected");
				break;
			
			case UNREGISTRATION_OK:		
				ServiceManager.showRegistartionNotif(R.drawable.bullet_ball_glass_red_16, "You are disconnected");
				break;
				
			case REGISTRATION_INPROGRESS:
			case UNREGISTRATION_INPROGRESS:
					ServiceManager.showRegistartionNotif(R.drawable.bullet_ball_glass_grey_16, String.format("Trying to %s...", (type == RegistrationEventTypes.REGISTRATION_INPROGRESS) ? "connect" : "disconnect"));
				break;
				
			case REGISTRATION_NOK:
			case UNREGISTRATION_NOK:
			default:
			{
				Log.d(ServiceManager.TAG, String.format("Registration/unregistration failed. code=%d and phrase=%s", code, phrase));
				break;
			}
		}
		
		return true;
	}
}
