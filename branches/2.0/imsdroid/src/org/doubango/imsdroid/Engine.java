package org.doubango.imsdroid;

import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.Impl.ScreenService;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.NgnNativeService;
import org.doubango.ngn.sip.NgnAVSession;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

public class Engine extends NgnEngine{
	private final static String TAG = Engine.class.getCanonicalName();
	
	private static final String CONTENT_TITLE = "IMSDroid";
	
	private static final int NOTIF_AVCALL_ID = 19833892;
	@SuppressWarnings("unused")
	private static final int NOTIF_SMS_ID = 19833893;
	private static final int NOTIF_APP_ID = 19833894;
	
	private IScreenService mScreenService;
	
	static {
		System.load(String.format("/data/data/%s/lib/libtinyWRAP.so", Main.class.getPackage().getName()));
		NgnEngine.initialize();
	}
	
	public static NgnEngine getInstance(){
		if(sInstance == null){
			sInstance = new Engine();
		}
		return sInstance;
	}
	
	public Engine(){
		super();
	}
	
	@Override
	public boolean start() {
		return super.start();
	}
	
	@Override
	public boolean stop() {
		return super.stop();
	}
	
	private void showNotification(int notifId, int drawableId, String tickerText) {
		if(!mStarted){
			return;
		}
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
        		tickerText = String.format("%s (%d)", tickerText, NgnAVSession.getSize());
        		intent.putExtra("action", Main.ACTION_SHOW_AVSCREEN);
        		break;
        		
       		default:
       			
       			break;
        }
        
        PendingIntent contentIntent = PendingIntent.getActivity(IMSDroid.getContext(), notifId/*requestCode*/, intent, PendingIntent.FLAG_UPDATE_CURRENT);     

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(IMSDroid.getContext(), CONTENT_TITLE, tickerText, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNotifManager.notify(notifId, notification);
    }
	
	public void showAppNotif(int drawableId, String tickerText){
    	Log.d(TAG, "showAppNotif");
    	showNotification(NOTIF_APP_ID, drawableId, tickerText);
    }
	
	public void showAVCallNotif(int drawableId, String tickerText){
    	showNotification(NOTIF_AVCALL_ID, drawableId, tickerText);
    }
	
	public void cancelAVCallNotif(){
    	if(!NgnAVSession.hasActiveSession()){
    		mNotifManager.cancel(NOTIF_AVCALL_ID);
    	}
    }
    
	public void refreshAVCallNotif(int drawableId){
    	if(!NgnAVSession.hasActiveSession()){
    		mNotifManager.cancel(NOTIF_AVCALL_ID);
    	}
    	else{
    		showNotification(NOTIF_AVCALL_ID, drawableId, "In Call");
    	}
    }
	
	public IScreenService getScreenService(){
		if(mScreenService == null){
			mScreenService = new ScreenService();
		}
		return mScreenService;
	}
	
	@Override
	public Class<? extends NgnNativeService> getNativeServiceClass(){
		return NativeService.class;
	}
}
