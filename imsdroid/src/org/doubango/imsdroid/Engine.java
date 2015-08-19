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
*/
package org.doubango.imsdroid;

import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.Impl.ScreenService;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.NgnNativeService;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.utils.NgnPredicate;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

public class Engine extends NgnEngine{
	private final static String TAG = Engine.class.getCanonicalName();
	
	private static final String CONTENT_TITLE = "IMSDroid";
	
	private static final int NOTIF_AVCALL_ID = 19833892;
	private static final int NOTIF_SMS_ID = 19833893;
	private static final int NOTIF_APP_ID = 19833894;
	private static final int NOTIF_CONTSHARE_ID = 19833895;
	private static final int NOTIF_CHAT_ID = 19833896;
	
	private IScreenService mScreenService;
	
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
        		
        	case NOTIF_CONTSHARE_ID:
                intent.putExtra("action", Main.ACTION_SHOW_CONTSHARE_SCREEN);
                notification.defaults |= Notification.DEFAULT_SOUND;
                break;
                
        	case NOTIF_SMS_ID:
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.defaults |= Notification.DEFAULT_SOUND;
                notification.tickerText = tickerText;
                intent.putExtra("action", Main.ACTION_SHOW_SMS);
                break;
                
        	case NOTIF_AVCALL_ID:
        		tickerText = String.format("%s (%d)", tickerText, NgnAVSession.getSize());
        		intent.putExtra("action", Main.ACTION_SHOW_AVSCREEN);
        		break;
        		
        	case NOTIF_CHAT_ID:
        		notification.defaults |= Notification.DEFAULT_SOUND;
        		tickerText = String.format("%s (%d)", tickerText, NgnMsrpSession.getSize(new NgnPredicate<NgnMsrpSession>() {
					@Override
					public boolean apply(NgnMsrpSession session) {
						return session != null && NgnMediaType.isChat(session.getMediaType());
					}
				}));
        		intent.putExtra("action", Main.ACTION_SHOW_CHAT_SCREEN);
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
	
	public void showContentShareNotif(int drawableId, String tickerText){
    	showNotification(NOTIF_CONTSHARE_ID, drawableId, tickerText);
    }
	
	public void cancelContentShareNotif(){
    	if(!NgnMsrpSession.hasActiveSession(new NgnPredicate<NgnMsrpSession>() {
			@Override
			public boolean apply(NgnMsrpSession session) {
				return session != null && NgnMediaType.isFileTransfer(session.getMediaType());
			}}))
    	{
    		mNotifManager.cancel(NOTIF_CONTSHARE_ID);
    	}
    }
    
	public void refreshContentShareNotif(int drawableId){
		if(!NgnMsrpSession.hasActiveSession(new NgnPredicate<NgnMsrpSession>() {
			@Override
			public boolean apply(NgnMsrpSession session) {
				return session != null && NgnMediaType.isFileTransfer(session.getMediaType());
			}}))
    	{
    		mNotifManager.cancel(NOTIF_CONTSHARE_ID);
    	}
    	else{
    		showNotification(NOTIF_CONTSHARE_ID, drawableId, "Content sharing");
    	}
    }
	
	public void showContentChatNotif(int drawableId, String tickerText){
    	showNotification(NOTIF_CHAT_ID, drawableId, tickerText);
    }
	
	public void cancelChatNotif(){
    	if(!NgnMsrpSession.hasActiveSession(new NgnPredicate<NgnMsrpSession>() {
			@Override
			public boolean apply(NgnMsrpSession session) {
				return session != null && NgnMediaType.isChat(session.getMediaType());
			}}))
    	{
    		mNotifManager.cancel(NOTIF_CHAT_ID);
    	}
    }
    
	public void refreshChatNotif(int drawableId){
		if(!NgnMsrpSession.hasActiveSession(new NgnPredicate<NgnMsrpSession>() {
			@Override
			public boolean apply(NgnMsrpSession session) {
				return session != null && NgnMediaType.isChat(session.getMediaType());
			}}))
    	{
    		mNotifManager.cancel(NOTIF_CHAT_ID);
    	}
    	else{
    		showNotification(NOTIF_CHAT_ID, drawableId, "Chat");
    	}
    }
	
	public void showSMSNotif(int drawableId, String tickerText){
    	showNotification(NOTIF_SMS_ID, drawableId, tickerText);
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
