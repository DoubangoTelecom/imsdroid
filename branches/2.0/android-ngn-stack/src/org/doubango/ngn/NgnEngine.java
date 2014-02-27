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

import java.io.File;

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
import org.doubango.tinyWRAP.SipStack;
import org.doubango.tinyWRAP.tdav_codec_id_t;
import org.doubango.tinyWRAP.tmedia_pref_video_size_t;
import org.doubango.tinyWRAP.tmedia_profile_t;
import org.doubango.tinyWRAP.tmedia_srtp_mode_t;
import org.doubango.tinyWRAP.tmedia_srtp_type_t;
import org.doubango.tinyWRAP.twrap_media_type_t;
import org.doubango.utils.AndroidUtils;

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
	private static boolean sInitialized;
	private static final String DATA_FOLDER = String.format("/data/data/%s", NgnApplication.getContext().getPackageName());
	private static final String LIBS_FOLDER = String.format("%s/lib", NgnEngine.DATA_FOLDER);
	
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
	
	static{
		NgnEngine.initialize2();
	}
	
	// This function will be renamed as "initialize()" when "initialize()" get removed
	private static void initialize2(){
		// do not add try/catch to let the app die if libraries are missing or incompatible
		if(!sInitialized){
			// See 'http://code.google.com/p/imsdroid/issues/detail?id=197' for more information
			// Load Android utils library (required to detect CPU features)
			boolean haveLibUtils = new File(String.format("%s/%s", NgnEngine.LIBS_FOLDER, "libutils_armv5te.so")).exists();
			if (haveLibUtils) { // only "armeabi-v7a" comes with "libutils.so"
				System.load(String.format("%s/%s", NgnEngine.LIBS_FOLDER, "libutils_armv5te.so"));
				Log.d(TAG,"CPU_Feature="+AndroidUtils.getCpuFeatures());
				if(NgnApplication.isCpuNeon()){
					Log.d(TAG,"isCpuNeon()=YES");
					System.load(String.format("%s/%s", NgnEngine.LIBS_FOLDER, "libtinyWRAP_neon.so"));
				}
				else{
					Log.d(TAG,"isCpuNeon()=NO");
					System.load(String.format("%s/%s", NgnEngine.LIBS_FOLDER, "libtinyWRAP.so"));
				}
			}
			else {
				// "armeabi", "mips", "x86"...
				System.load(String.format("%s/%s", NgnEngine.LIBS_FOLDER, "libtinyWRAP.so"));
			}
				
			// If OpenSL ES is supported and know to work on current device then used it
			if(NgnApplication.isSLEs2KnownToWork()){
				final String pluginPath = String.format("%s/%s", NgnEngine.LIBS_FOLDER, "libplugin_audio_opensles.so");
				
				// returned value is the number of registered add-ons (2 = 1 consumer + 1 producer)
				if(MediaSessionMgr.registerAudioPluginFromFile(pluginPath) < 2){
					// die if cannot load add-ons
					throw new RuntimeException("Failed to register audio plugin with path=" + pluginPath);
				}
				
				Log.d(TAG, "Using OpenSL ES audio driver");
			}
			// otherwise, use AudioTrack/Record
			else{
				ProxyAudioProducer.registerPlugin();
				ProxyAudioConsumer.registerPlugin();
			}
			
			ProxyVideoProducer.registerPlugin();
			ProxyVideoConsumer.registerPlugin();
			
			SipStack.initialize();
			
			NgnProxyPluginMgr.Initialize();
			
			sInitialized = true;
		}
	}
	
	// This function is deprecated and you no longer need to call it. Also, do not load the native libs in your app
	// Will be removed in the next releases
	@Deprecated
	public static void initialize(){
		initialize2();
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
		final INgnConfigurationService configurationService = getConfigurationService();
		if(applicationContext != null){
			mNotifManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		else{ 
			mNotifManager = null;
		}
		mVibrator = null;
		
		// Initialize SIP stack
		SipStack.initialize();
		// Set codec priorities
		int prio = 0;
		SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_g722, prio++);
		SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_speex_wb, prio++);
		SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_speex_uwb, prio++);
		SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_speex_nb, prio++);
		SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_pcma, prio++);
		SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_pcmu, prio++);
		SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_ilbc, prio++);
        SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_gsm, prio++);
        SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_g729ab, prio++);
        SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_amr_nb_oa, prio++);
        SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_amr_nb_be, prio++);
        
        SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_h264_bp, prio++);
        SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_h264_mp, prio++);
        SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_vp8, prio++);
        SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_mp4ves_es, prio++);
        SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_theora, prio++);
        SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_h263, prio++);
        SipStack.setCodecPriority(tdav_codec_id_t.tdav_codec_id_h261, prio++);
        
        // Profile
        MediaSessionMgr.defaultsSetProfile(tmedia_profile_t.valueOf(configurationService.getString(
				NgnConfigurationEntry.MEDIA_PROFILE,
				NgnConfigurationEntry.DEFAULT_MEDIA_PROFILE)));
        // Set default mediaType to use when receiving bodiless INVITE
        MediaSessionMgr.defaultsSetMediaType(twrap_media_type_t.twrap_media_audiovideo);
		// Preferred video size
		MediaSessionMgr.defaultsSetPrefVideoSize(tmedia_pref_video_size_t.valueOf(configurationService.getString(
				NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE,
				NgnConfigurationEntry.DEFAULT_QOS_PREF_VIDEO_SIZE)));
		// Zero Video Artifacts
		MediaSessionMgr.defaultsSetVideoZeroArtifactsEnabled(configurationService.getBoolean(
				NgnConfigurationEntry.QOS_USE_ZERO_VIDEO_ARTIFACTS,
				NgnConfigurationEntry.DEFAULT_QOS_USE_ZERO_VIDEO_ARTIFACTS));
		// SRTP mode
		MediaSessionMgr.defaultsSetSRtpMode(tmedia_srtp_mode_t.valueOf(configurationService.getString(
				NgnConfigurationEntry.SECURITY_SRTP_MODE,
				NgnConfigurationEntry.DEFAULT_SECURITY_SRTP_MODE)));
		// SRTP type
		MediaSessionMgr.defaultsSetSRtpType(tmedia_srtp_type_t.valueOf(configurationService.getString(
				NgnConfigurationEntry.SECURITY_SRTP_TYPE,
				NgnConfigurationEntry.DEFAULT_SECURITY_SRTP_TYPE)));
		// NAT Traversal (ICE, STUN and TURN)
		MediaSessionMgr.defaultsSetIceEnabled(configurationService.getBoolean(NgnConfigurationEntry.NATT_USE_ICE, NgnConfigurationEntry.DEFAULT_NATT_USE_ICE));
		MediaSessionMgr.defaultsSetIceStunEnabled(true); // we want ICE reflexive candidates
		MediaSessionMgr.defaultsSetStunEnabled(configurationService.getBoolean(NgnConfigurationEntry.NATT_USE_STUN, NgnConfigurationEntry.DEFAULT_NATT_USE_STUN));
		
		// codecs, AEC, NoiseSuppression, Echo cancellation, ....
		final boolean aec = configurationService.getBoolean(NgnConfigurationEntry.GENERAL_AEC, NgnConfigurationEntry.DEFAULT_GENERAL_AEC) ;
		final boolean echo_tail_adaptive = configurationService.getBoolean(NgnConfigurationEntry.GENERAL_USE_ECHO_TAIL_ADAPTIVE, NgnConfigurationEntry.DEFAULT_GENERAL_USE_ECHO_TAIL_ADAPTIVE);
		final boolean vad = configurationService.getBoolean(NgnConfigurationEntry.GENERAL_VAD, NgnConfigurationEntry.DEFAULT_GENERAL_VAD) ;
		final boolean nr = configurationService.getBoolean(NgnConfigurationEntry.GENERAL_NR, NgnConfigurationEntry.DEFAULT_GENERAL_NR) ;
		final int echo_tail = configurationService.getInt(NgnConfigurationEntry.GENERAL_ECHO_TAIL, NgnConfigurationEntry.DEFAULT_GENERAL_ECHO_TAIL);
		
		Log.d(TAG, "Configure AEC["+aec+"/"+echo_tail+"] AEC_TAIL_ADAPT["+echo_tail_adaptive+"] NoiseSuppression["+nr+"], Voice activity detection["+vad+"]");
		
		if (aec){
			MediaSessionMgr.defaultsSetEchoSuppEnabled(true);
			// Very Important: EchoTail is in milliseconds
			// When using WebRTC AEC, the maximum value is 500ms
			// When using Speex-DSP, any number is valid but you should choose a multiple of 20ms
			// In all cases this value will be updated per session if adaptive echo tail option is enabled
			MediaSessionMgr.defaultsSetEchoTail(echo_tail);
			MediaSessionMgr.defaultsSetEchoSkew(0);
		}
		else{
			MediaSessionMgr.defaultsSetEchoSuppEnabled(false);
			MediaSessionMgr.defaultsSetEchoTail(0); 
		}
		MediaSessionMgr.defaultsSetAgcEnabled(true);
		MediaSessionMgr.defaultsSetVadEnabled(vad);
		MediaSessionMgr.defaultsSetNoiseSuppEnabled(nr);
		MediaSessionMgr.defaultsSetJbMargin(100);
		// /!\IMPORTANT: setting the Jitter buffer max late to (0) cause "SIGFPE" error in SpeexDSP function "jitter_buffer_ctl(JITTER_BUFFER_SET_MAX_LATE_RATE)"
		// This only happen when the audio engine is dynamically loaded from shared library (at least on Galaxy Nexus)
		MediaSessionMgr.defaultsSetJbMaxLateRate(1);
		MediaSessionMgr.defaultsSetRtcpEnabled(true);
		MediaSessionMgr.defaultsSetRtcpMuxEnabled(true);
		// supported opus mw_rates: 8000,12000,16000,24000,48000
		// opensl-es playback_rates: 8000, 11025, 16000, 22050, 24000, 32000, 44100, 64000, 88200, 96000, 192000
		// webrtc aec record_rates: 8000, 16000, 32000
		MediaSessionMgr.defaultsSetOpusMaxCaptureRate(16000);// /!\IMPORTANT: only 8k and 16k will work with WebRTC AEC
		MediaSessionMgr.defaultsSetOpusMaxPlaybackRate(16000);
		
		MediaSessionMgr.defaultsSetCongestionCtrlEnabled(false);
		MediaSessionMgr.defaultsSetBandwidthVideoDownloadMax(-1);
		MediaSessionMgr.defaultsSetBandwidthVideoUploadMax(-1);
		
		MediaSessionMgr.defaultsSetAudioChannels(1, 1); // (mono, mono)
		MediaSessionMgr.defaultsSetAudioPtime(20);
		
		MediaSessionMgr.defaultsSetAvpfTail(30, 160);
		MediaSessionMgr.defaultsSetVideoFps(15);
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
