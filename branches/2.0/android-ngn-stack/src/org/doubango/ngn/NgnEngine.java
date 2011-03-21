package org.doubango.ngn;

import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnContactService;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.services.INgnHttpClientService;
import org.doubango.ngn.services.INgnNetworkService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.services.INgnSoundService;
import org.doubango.ngn.services.INgnStorageService;
import org.doubango.ngn.services.impl.NgnConfigurationService;
import org.doubango.ngn.services.impl.NgnContactService;
import org.doubango.ngn.services.impl.NgnHistoryService;
import org.doubango.ngn.services.impl.NgnHttpClientService;
import org.doubango.ngn.services.impl.NgnNetworkService;
import org.doubango.ngn.services.impl.NgnSipService;
import org.doubango.ngn.services.impl.NgnSoundService;
import org.doubango.ngn.services.impl.NgnStorageService;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

public class NgnEngine {
	private final static String TAG = NgnEngine.class.getCanonicalName();
	
	protected static NgnEngine sInstance;
	
	protected boolean mStarted;
	protected Activity mMainActivity;
	
	protected final NotificationManager mNotifManager;
	protected final Vibrator mVibrator;
	
	protected INgnConfigurationService mConfigurationService;
	protected INgnStorageService mStorageService;
	protected INgnNetworkService mNetworkService;
	protected INgnHttpClientService mHttpClientService;
	protected INgnContactService mContactService;
	protected INgnHistoryService mHistoryService;
	protected INgnSipService mSipService;
	protected INgnSoundService mSoundService;
	
	public static NgnEngine getInstance(){
		if(sInstance == null){
			sInstance = new NgnEngine();
		}
		return sInstance;
	}
	
	public NgnEngine(){
		mNotifManager = (NotificationManager) NgnApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		mVibrator = null;
	}
	
	public synchronized boolean start() {
		if(mStarted){
			return true;
		}
		
		boolean success = true;
		
		success &= getConfigurationService().start();
		success &= getStorageService().start();
		success &= getNetworkService().start();
		success &= getHttpClientService().start();
		success &= getHistoryService().start();
		success &= getContactService().start();
		success &= getSipService().start();
		success &= getSoundService().start();
		
		if(success){
			success &= getHistoryService().load();
			/* success &=*/ getContactService().load();
			
			NgnApplication.getContext().startService(
					new Intent(NgnApplication.getContext(), getNativeServiceClass()));
		}
		else{
			Log.e(TAG, "Failed to start services");
		}
		
		mStarted = true;
		return success;
	}
	
	public synchronized boolean stop() {
		if(!mStarted){
			return true;
		}
		
		boolean success = true;
		
		success &= getConfigurationService().stop();
		success &= getHttpClientService().stop();
		success &= getHistoryService().stop();
		success &= getStorageService().stop();
		success &= getContactService().stop();
		success &= getSipService().stop();
		success &= getSoundService().stop();
		success &= getNetworkService().stop();
		
		if(!success){
			Log.e(TAG, "Failed to stop services");
		}
		
		NgnApplication.getContext().stopService(
				new Intent(NgnApplication.getContext(), getNativeServiceClass()));
		
		// Cancel the persistent notifications.
		if(mNotifManager != null){
			mNotifManager.cancelAll();
		}
		
		mStarted = false;
		return success;
	}
	
	public synchronized boolean isStarted(){
		return mStarted;
	}
	
	public void setMainActivity(Activity mainActivity){
		mMainActivity = mainActivity;
	}
	
	public Activity getMainActivity(){
		return mMainActivity;
	}
	
	public INgnConfigurationService getConfigurationService(){
		if(mConfigurationService == null){
			mConfigurationService = new NgnConfigurationService();
		}
		return mConfigurationService;
	}
	
	public INgnStorageService getStorageService(){
		if(mStorageService == null){
			mStorageService = new NgnStorageService();
		}
		return mStorageService;
	}
	
	public INgnNetworkService getNetworkService(){
		if(mNetworkService == null){
			mNetworkService = new NgnNetworkService();
		}
		return mNetworkService;
	}
	
	public INgnHttpClientService getHttpClientService(){
		if(mHttpClientService == null){
			mHttpClientService = new NgnHttpClientService();
		}
		return mHttpClientService;
	}
	
	public INgnContactService getContactService(){
		if(mContactService == null){
			mContactService = new NgnContactService();
		}
		return mContactService;
	}
	
	public INgnHistoryService getHistoryService(){
		if(mHistoryService == null){
			mHistoryService = new NgnHistoryService();
		}
		return mHistoryService;
	}
	
	public INgnSipService getSipService(){
		if(mSipService == null){
			mSipService = new NgnSipService();
		}
		return mSipService;
	}
	
	public INgnSoundService getSoundService(){
		if(mSoundService == null){
			mSoundService = new NgnSoundService();
		}
		return mSoundService;
	}
	
	public Class<? extends NgnNativeService> getNativeServiceClass(){
		return NgnNativeService.class;
	}
}
