package org.doubango.imsdroid.Sevices.Impl;

import org.doubango.imsdroid.Main;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Services.IStorageService;
import org.doubango.imsdroid.Services.IXcapService;
import org.doubango.tinyWRAP.ProxyAudioConsumer;
import org.doubango.tinyWRAP.ProxyAudioProducer;
import org.doubango.tinyWRAP.ProxyVideoProducer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Screen Manager. Entry point to retrieve all services (Singletons).
 * 
 * @author root
 * 
 */
public class ServiceManager  extends Service {
	/* Singletons */
	private static final ConfigurationService configurationService = new ConfigurationService();
	private static final ContactService contactService = new ContactService();
	private static final NetworkService networkService = new NetworkService();
	private static final ScreenService screenService = new ScreenService();
	private static final SipService sipService = new SipService();
	private static final StorageService storageService = new StorageService();
	private static final XcapService xcapService = new XcapService();
	
	private static final String TAG = ServiceManager.class.getCanonicalName();
	private static final String CONTENT_TITLE = "imsdroid";
	
	private static boolean started;
	private static Main mainActivity;
	private static NotificationManager notifManager;
	private static final int NOTIF_REGISTRATION_ID = 19833891;
	private static final int NOTIF_AVCALL_ID = 19833892;
	
	private static ServiceManager instance;

	// Register Plugins
	static{
		ProxyVideoProducer.registerPlugin();
		ProxyAudioProducer.registerPlugin();
		ProxyAudioConsumer.registerPlugin();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		if(ServiceManager.notifManager == null){
			ServiceManager.notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			// Display a notification about us starting.  We put an icon in the status bar.
			ServiceManager.showRegistartionNotif(R.drawable.bullet_ball_glass_red_16, "You are not connected");
		}
		ServiceManager.instance = this;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Cancel the persistent notifications.
		ServiceManager.notifManager.cancel(ServiceManager.NOTIF_REGISTRATION_ID);
		ServiceManager.notifManager.cancel(ServiceManager.NOTIF_AVCALL_ID);

        // Tell the user we stopped.
        Toast.makeText(this, "imsdroid shutting down...", Toast.LENGTH_SHORT).show();
	}
	
    private static void showNotification(int notifId, int drawableId, String tickerText) {
        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(drawableId, tickerText, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(ServiceManager.getMainActivity(), 0,
                new Intent(ServiceManager.getMainActivity(), Main.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(ServiceManager.getMainActivity(), ServiceManager.CONTENT_TITLE, tickerText, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        ServiceManager.notifManager.notify(notifId, notification);
    }
    
    public static void showRegistartionNotif(int drawableId, String tickerText){
    	ServiceManager.showNotification(NOTIF_REGISTRATION_ID, drawableId, tickerText);
    }
    
    public static void showAVCallNotif(int drawableId, String tickerText){
    	ServiceManager.showNotification(NOTIF_AVCALL_ID, drawableId, tickerText);
    }
    
    public static void cancelAVCallNotif(){
    	ServiceManager.notifManager.cancel(ServiceManager.NOTIF_AVCALL_ID);
    }
    
    public static ServiceManager getInstance(){
    	return ServiceManager.instance;
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
		
		// starts android service
		ServiceManager.getMainActivity().startService(
				new Intent(getMainActivity(), ServiceManager.class));
		
		boolean success = true;

		success &= ServiceManager.configurationService.start();
		success &= ServiceManager.contactService.start();
		success &= ServiceManager.networkService.start();
		success &= ServiceManager.screenService.start();
		success &= ServiceManager.sipService.start();
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
		
		// stops android service
		ServiceManager.getMainActivity().stopService(
				new Intent(ServiceManager.getMainActivity(), ServiceManager.class));
		
		boolean success = true;

		success &= ServiceManager.configurationService.stop();
		success &= ServiceManager.contactService.stop();
		success &= ServiceManager.networkService.stop();
		success &= ServiceManager.screenService.stop();
		success &= ServiceManager.sipService.stop();
		success &= ServiceManager.storageService.stop();
		success &= ServiceManager.xcapService.stop();

		if(!success){
			Log.e(ServiceManager.TAG, "Failed to stop services");
		}
		
		return success;
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
}
