/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*  Copyright (C) 2011, Philippe Verney <verney(dot)philippe(AT)gmail(dot)com>
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
package org.doubango.ngn;

import org.doubango.ngn.media.NgnProxyPluginMgr;
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
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.tinyWRAP.ProxyAudioConsumer;
import org.doubango.tinyWRAP.ProxyAudioProducer;
import org.doubango.tinyWRAP.ProxyVideoConsumer;
import org.doubango.tinyWRAP.ProxyVideoProducer;
import org.doubango.tinyWRAP.tmedia_bandwidth_level_t;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

/**
 * Next Generation Network Engine.
 * This is the main entry point to have access to all services (SIP, XCAP, MSRP, History, ...).
 * Anywhere in the code you can get an instance of the engine by calling @ref getInstance() function.
 */
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
	
	public static void initialize(){
		ProxyVideoProducer.registerPlugin();
		ProxyVideoConsumer.registerPlugin();
		ProxyAudioProducer.registerPlugin();
		ProxyAudioConsumer.registerPlugin();
		
		NgnProxyPluginMgr.Initialize();
	}
	
	/**
	 * Gets an instance of the NGN engine. You can call this function as many as you need and it will always return th
	 * same instance.
	 * @return An instance of the NGN engine.
	 */
	public static NgnEngine getInstance(){
		if(sInstance == null){
			sInstance = new NgnEngine();
		}
		return sInstance;
	}
	
	/**
	 * Default constructor for the NGN engine. You should never call this function from your code. Instead you should
	 * use @ref getInstance().
	 * @sa @ref getInstance()
	 */
	protected NgnEngine(){
		final Context applicationContext = NgnApplication.getContext();
		if(applicationContext != null){
			mNotifManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		else{ 
			mNotifManager = null;
		}
		mVibrator = null;
		
		// Apply defaults using the values stored in the configuration service
		MediaSessionMgr.defaultsSetBandwidthLevel(
							tmedia_bandwidth_level_t.swigToEnum(getConfigurationService().getInt(NgnConfigurationEntry.QOS_PRECOND_BANDWIDTH_LEVEL, NgnConfigurationEntry.DEFAULT_QOS_PRECOND_BANDWIDTH_LEVEL)
				));
		// codecs, AEC, NoiseSuppression, Echo cancellation, ....
		boolean aec         = getConfigurationService().getBoolean(NgnConfigurationEntry.GENERAL_AEC, NgnConfigurationEntry.DEFAULT_GENERAL_AEC) ;
		boolean vad        = getConfigurationService().getBoolean(NgnConfigurationEntry.GENERAL_VAD, NgnConfigurationEntry.DEFAULT_GENERAL_VAD) ;
		boolean nr          = getConfigurationService().getBoolean(NgnConfigurationEntry.GENERAL_NR, NgnConfigurationEntry.DEFAULT_GENERAL_NR) ;
		int         echo_tail = mConfigurationService.getInt(NgnConfigurationEntry.GENERAL_ECHO_TAIL,NgnConfigurationEntry.DEFAULT_GENERAL_ECHO_TAIL);
		
		Log.d(TAG,"Configure AEC["+aec+"/"+echo_tail+"] NoiseSuppression["+nr+"], Voice activity detection["+vad+"]");
		
		if (aec){
			MediaSessionMgr.defaultsSetEchoSuppEnabled(true);
			// Very Important: EchoTail is in milliseconds
			// When using WebRTC AEC, the maximum value is 500ms
			// When using Speex-DSP, any number is valid but you should choose a multiple of 20ms.
			MediaSessionMgr.defaultsSetEchoTail(echo_tail);
			MediaSessionMgr.defaultsSetEchoSkew(0);
		}
		else{
			MediaSessionMgr.defaultsSetEchoSuppEnabled(false);
			MediaSessionMgr.defaultsSetEchoTail(0); 
		}
		MediaSessionMgr.defaultsSetAgcEnabled(false);
		MediaSessionMgr.defaultsSetVadEnabled(vad);
		MediaSessionMgr.defaultsSetNoiseSuppEnabled(nr);
	}
	
	/**
	 * Starts the engine. This function will start all underlying services (SIP, XCAP, MSRP, History, ...).
	 * You must call this function before trying to use any of the underlying services.
	 * @return true if all services have been successfully started and false otherwise
	 */
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
	
	/**
	 * Stops the engine. This function will stop all underlying services (SIP, XCAP, MSRP, History, ...).
	 * @return true if all services have been successfully stopped and false otherwise
	 */
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
	
	/**
	 * Checks whether the engine is started.
	 * @return true is the engine is running and false otherwise.
	 * @sa @ref start() @ref stop()
	 */
	public synchronized boolean isStarted(){
		return mStarted;
	}
	
	/**
	 * Sets the main activity to use as context in order to query some native resources.
	 * It's up to you to call this function in order to retrieve the contacts for the ContactService.
	 * @param mainActivity The activity
	 * @sa @ref getMainActivity()
	 */
	public void setMainActivity(Activity mainActivity){
		mMainActivity = mainActivity;
	}
	
	/**
	 * Gets the main activity.
	 * @return the main activity
	 * @sa @ref setMainActivity()
	 */
	public Activity getMainActivity(){
		return mMainActivity;
	}
	
	/**
	 * Gets the configuration service.
	 * @return the configuration service.
	 */
	public INgnConfigurationService getConfigurationService(){
		if(mConfigurationService == null){
			mConfigurationService = new NgnConfigurationService();
		}
		return mConfigurationService;
	}
	
	/**
	 * Gets the storage service.
	 * @return the storage service.
	 */
	public INgnStorageService getStorageService(){
		if(mStorageService == null){
			mStorageService = new NgnStorageService();
		}
		return mStorageService;
	}
	
	/**
	 * Gets the network service
	 * @return the network service
	 */
	public INgnNetworkService getNetworkService(){
		if(mNetworkService == null){
			mNetworkService = new NgnNetworkService();
		}
		return mNetworkService;
	}
	
	/**
	 * Gets the HTTP service
	 * @return the HTTP service
	 */
	public INgnHttpClientService getHttpClientService(){
		if(mHttpClientService == null){
			mHttpClientService = new NgnHttpClientService();
		}
		return mHttpClientService;
	}
	
	/**
	 * Gets the contact service
	 * @return the contact service
	 */
	public INgnContactService getContactService(){
		if(mContactService == null){
			mContactService = new NgnContactService();
		}
		return mContactService;
	}
	
	/**
	 * Gets the history service
	 * @return the history service
	 */
	public INgnHistoryService getHistoryService(){
		if(mHistoryService == null){
			mHistoryService = new NgnHistoryService();
		}
		return mHistoryService;
	}
	
	/**
	 * Gets the SIP service
	 * @return the sip service
	 */
	public INgnSipService getSipService(){
		if(mSipService == null){
			mSipService = new NgnSipService();
		}
		return mSipService;
	}
	
	/**
	 * Gets the sound service
	 * @return the sound service
	 */
	public INgnSoundService getSoundService(){
		if(mSoundService == null){
			mSoundService = new NgnSoundService();
		}
		return mSoundService;
	}
	
	/**
	 * Gets the native service class
	 * @return the native service class
	 */
	public Class<? extends NgnNativeService> getNativeServiceClass(){
		return NgnNativeService.class;
	}
}
