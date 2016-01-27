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
package org.doubango.ngn.sip;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnMessagingEventArgs;
import org.doubango.ngn.events.NgnMessagingEventTypes;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.media.NgnProxyAudioConsumer;
import org.doubango.ngn.media.NgnProxyAudioProducer;
import org.doubango.ngn.media.NgnProxyPlugin;
import org.doubango.ngn.media.NgnProxyPluginMgr;
import org.doubango.ngn.media.NgnProxyVideoConsumer;
import org.doubango.ngn.media.NgnProxyVideoProducer;
import org.doubango.ngn.model.NgnHistoryAVCallEvent;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnContentType;
import org.doubango.ngn.utils.NgnDateTimeUtils;
import org.doubango.ngn.utils.NgnListUtils;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.tinyWRAP.ActionConfig;
import org.doubango.tinyWRAP.CallSession;
import org.doubango.tinyWRAP.Codec;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.tinyWRAP.ProxyPlugin;
import org.doubango.tinyWRAP.SipMessage;
import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.T140Callback;
import org.doubango.tinyWRAP.T140CallbackData;
import org.doubango.tinyWRAP.tmedia_bandwidth_level_t;
import org.doubango.tinyWRAP.tmedia_qos_strength_t;
import org.doubango.tinyWRAP.tmedia_qos_stype_t;
import org.doubango.tinyWRAP.tmedia_t140_data_type_t;
import org.doubango.tinyWRAP.twrap_media_type_t;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.util.Log;
import android.view.View;

/**
 * Audio/Video call session
 */
public class NgnAVSession extends NgnInviteSession{
	private static final String TAG = NgnAVSession.class.getCanonicalName();
	
	private NgnT140Callback mT140Callback;
	private CallSession mSession;
	private boolean mConsumersAndProducersInitialzed;
	private NgnProxyVideoConsumer mVideoConsumer;
	private NgnProxyAudioConsumer mAudioConsumer;
	private NgnProxyVideoProducer mVideoProducer;
	private NgnProxyAudioProducer mAudioProducer;
	private Context mContext;
	
	private final NgnHistoryAVCallEvent mHistoryEvent;
	private final INgnConfigurationService mConfigurationService;
	
	private boolean mSendingVideo;
	private boolean mMuteOn;
	private boolean mSpeakerOn;
	
    private final static NgnObservableHashMap<Long, NgnAVSession> sSessions = new NgnObservableHashMap<Long, NgnAVSession>(true);
	private static int AudioManager_MODE_IN_COMMUNICATION = AudioManager.MODE_IN_CALL;

	static {
		if (NgnApplication.getSDKVersion() >= 11) {
			try {
				final Field f = AudioManager.class.getDeclaredField("MODE_IN_COMMUNICATION");
				AudioManager_MODE_IN_COMMUNICATION = f.getInt(null);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
    
    public static NgnAVSession takeIncomingSession(NgnSipStack sipStack, CallSession session, twrap_media_type_t mediaType, SipMessage sipMessage){
        NgnMediaType media = NgnMediaType.ConvertFromNative(mediaType);
        if(media == NgnMediaType.None){
        	Log.e(TAG, "Invalid media type");
        	return null;
        }
        synchronized (sSessions){
            NgnAVSession avSession = new NgnAVSession(sipStack, session, media, InviteState.INCOMING);
            if (sipMessage != null){
                avSession.setRemotePartyUri(sipMessage.getSipHeaderValue("f"));
            }
            sSessions.put(avSession.getId(), avSession);
            return avSession;
        }
    }
    
    public static boolean handleMediaUpdate(long id, twrap_media_type_t newMediaType){
    	NgnAVSession avSession = NgnAVSession.getSession(id);
        if (avSession != null){
        	NgnMediaType _newMediaType = NgnMediaType.ConvertFromNative(newMediaType);
        	if(_newMediaType != NgnMediaType.None){
        		avSession.mMediaType = _newMediaType; // mediaType must be updated here because it's used by initializeConsumersAndProducers();
        	}
        	avSession.mConsumersAndProducersInitialzed = false;
        	return avSession.initializeConsumersAndProducers();
        }
        
        return false;
    }

    /**
     * Creates an outgoing audio/video call session.
     * @param sipStack the IMS/SIP stack to use to make the call
     * @param mediaType the media type. 
     * @return an audio/video session
     * @sa @ref makeAudioCall() @ref makeAudioVideoCall()
     */
    public static NgnAVSession createOutgoingSession(NgnSipStack sipStack, NgnMediaType mediaType){
        synchronized (sSessions){
            final NgnAVSession avSession = new NgnAVSession(sipStack, null, mediaType, InviteState.INPROGRESS);
            sSessions.put(avSession.getId(), avSession);
            return avSession;
        }
    }
	
	public static void releaseSession(NgnAVSession session){
		synchronized (sSessions){
            if (session != null && sSessions.containsKey(session.getId())){
                long id = session.getId();
                session.decRef();
                sSessions.remove(id);
            }
        }
    }

	public static NgnObservableHashMap<Long, NgnAVSession> getSessions(){
		return sSessions;
	}

	/**
	 * Retrieves an audio/video session by id.
	 * @param id the id of the audio/video session to retrieve
	 * @return an audio/video session with the specified id if exist and null otherwise
	 */
	public static NgnAVSession getSession(long id) {
		synchronized (sSessions) {
			if (sSessions.containsKey(id))
				return sSessions.get(id);
			else
				return null;
		}
	}
	
	public static NgnAVSession getSession(NgnPredicate<NgnAVSession> predicate) {
		synchronized (sSessions) {
			return NgnListUtils.getFirstOrDefault(sSessions.values(), predicate);
		}
	}

	/**
	 * Gets the number of pending audio/video sessions. These sessions could be active or not.
	 * @return the number of pending audio/video sessions.
	 * @sa @ref hasActiveSession()
	 */
	public static int getSize(){
        synchronized (sSessions){
            return sSessions.size();
        }
    }
	
	public static int getSize(NgnPredicate<NgnAVSession> predicate) {
		synchronized (sSessions) {
			return NgnListUtils.filter(sSessions.values(), predicate).size();
		}
	}
	
	/**
	 * Checks whether we already have an audio/video session with the specified id.
	 * @param id the id of the session to look for
	 * @return true if exist and false otherwise
	 */
    public static boolean hasSession(long id){
        synchronized (sSessions){
            return sSessions.containsKey(id);
        }
    }
    
    /**
     * Check whether we have at least one active audio/video session.
     * @return true if exist and false otherwise
     */
    public static boolean hasActiveSession(){
    	synchronized (sSessions){
    		final Collection<NgnAVSession> mysessions = sSessions.values();
	    	for(NgnAVSession session : mysessions){
	    		if(session.isActive()){
	    			return true;
	    		}
	    	}
    	}
    	return false;
    }
    
    /**
     * Gets the first active audio/video session with an id different than the one specified
     * as parameter
     * @param id the id of the session to exclude from the search
     * @return an audio/video session matching the criteria or null if no one exist
     */
    public static NgnAVSession getFirstActiveCallAndNot(long id){
		NgnAVSession session;
		for(Map.Entry<Long, NgnAVSession> entry : sSessions.entrySet()) {
			session = entry.getValue();
			if(session.getId() != id && session.isActive() && !session.isLocalHeld() && !session.isRemoteHeld()){
				return session;
			}
		}
		return null;
	}
    
    /**
     * Places an audio call. Event if the NGN engine supports multi-line calls it's recommended
     * to check that there is no active call before trying to make new one. You can use @ref hasActiveSession()
     * function to check there is already an active audio/video session. Putting the current active active call
     * in hold before placing the new one could also be a recommended solution.
     * @param remoteUri the remote party uri. Could be a SIP/TEL uri, nomadic number, MSISDN number, ...
     * example: sip:test@doubango.org, tel:+33600000000, 78888667, ...
     * @param sipStack the SIP/IMS stack to use
     * @return true if the call has been successfully placed and false otherwise
     * @sa @ref createOutgoingSession() @ref makeAudioVideoCall()
     */
    public static boolean makeAudioCall(String remoteUri, NgnSipStack sipStack){
    	NgnAVSession avSession = NgnAVSession.createOutgoingSession(sipStack, NgnMediaType.Audio);
    	return avSession.makeCall(NgnUriUtils.makeValidSipUri(remoteUri));
    }
    
    /**
     * Places an audio/video call. Event if the NGN engine supports multi-line calls it's recommended
     * to check that there is no active call before trying to make new one. You can use @ref hasActiveSession()
     * function to check there is already an active audio/video session. Putting the current active active call
     * in hold before placing the new one could also be a recommended solution.
     * @param remoteUri the remote party uri. Could be a SIP/TEL uri, nomadic number, MSISDN number, ...
     * example: sip:test@doubango.org, tel:+33600000000, 78888667, ...
     * @param sipStack the SIP/IMS stack to use
     * @return true if the call has been successfully placed and false otherwise
     * @sa @ref createOutgoingSession() @ref makeAudioCall()
     */
    public static boolean makeAudioVideoCall(String remoteUri, NgnSipStack sipStack){
    	NgnAVSession avSession = NgnAVSession.createOutgoingSession(sipStack, NgnMediaType.AudioVideo);
    	return avSession.makeCall(NgnUriUtils.makeValidSipUri(remoteUri));
    }
    
    /**
	 * Makes an audio/video call. The call type depends on the mediaType define in the session object.
	 * @param remoteUri the remote party uri. Could be a SIP/TEL uri, nomadic number, MSISDN number, ...
     * example: sip:test@doubango.org, tel:+33600000000, 78888667, ...
	 * @return true if the call succeed and false otherwise
	 * @sa @ref createOutgoingSession() @ref makeAudioCall() @ref makeAudioVideoCall()
	 */
    public boolean makeCall(String remoteUri){
        boolean ret;

        super.mOutgoing = true;
        super.setToUri(remoteUri);

        ActionConfig config = new ActionConfig();
        // this just to show how to use parameters which only apply to the current
        // action (call)
        int level = mConfigurationService.getInt(NgnConfigurationEntry.QOS_PRECOND_BANDWIDTH_LEVEL,
        		NgnConfigurationEntry.DEFAULT_QOS_PRECOND_BANDWIDTH_LEVEL);
        tmedia_bandwidth_level_t bl = tmedia_bandwidth_level_t.swigToEnum(level);
        config.setMediaInt(twrap_media_type_t.twrap_media_audiovideo, "bandwidth-level", bl.swigValue());
        ret = mSession.call(remoteUri, NgnMediaType.ConvertToNative(super.getMediaType()), config);
        config.delete();

        return ret;
    }
    
    /**
     * Starts video sharing session
     * @param remoteUri  the remote party uri. Could be a SIP/TEL uri, nomadic number, MSISDN number, ...
     * example: sip:test@doubango.org, tel:+33600000000, 78888667, ...
	 * @return true if the call succeed and false otherwise
     */
    public boolean makeVideoSharingCall(String remoteUri){
        boolean ret;

        super.mOutgoing = true;

        ActionConfig config = new ActionConfig();
        ret = mSession.callVideo(remoteUri, config);
        config.delete();

        return ret;
    }
    
    protected NgnAVSession(NgnSipStack sipStack, CallSession session, NgnMediaType mediaType, InviteState callState){
		super(sipStack);
		mSession = (session == null) ? new CallSession(sipStack) : session;
	    super.mMediaType = mediaType;
	    
	    mConfigurationService = NgnEngine.getInstance().getConfigurationService();
	    
	    mSendingVideo = mConfigurationService.getBoolean(
	    		NgnConfigurationEntry.GENERAL_AUTOSTART_VIDEO,
				NgnConfigurationEntry.DEFAULT_GENERAL_AUTOSTART_VIDEO);
	    
	    // commons
	    super.init();
	    // SigComp
	    super.setSigCompId(sipStack.getSigCompId());
	    // 100rel
	    // mSession.set100rel(true); // will add "Supported: 100rel"   => Use defaults     
        // Session timers
        if(mConfigurationService.getBoolean(NgnConfigurationEntry.QOS_USE_SESSION_TIMERS, NgnConfigurationEntry.DEFAULT_QOS_USE_SESSION_TIMERS)){
			mSession.setSessionTimer((long) mConfigurationService.getInt(
					NgnConfigurationEntry.QOS_SIP_CALLS_TIMEOUT,
					NgnConfigurationEntry.DEFAULT_QOS_SIP_CALLS_TIMEOUT),
					mConfigurationService.getString(NgnConfigurationEntry.QOS_REFRESHER,
							NgnConfigurationEntry.DEFAULT_QOS_REFRESHER));
        }
        // Precondition
		mSession.setQoS(tmedia_qos_stype_t.valueOf(mConfigurationService
				.getString(NgnConfigurationEntry.QOS_PRECOND_TYPE,
						NgnConfigurationEntry.DEFAULT_QOS_PRECOND_TYPE)),
				tmedia_qos_strength_t.valueOf(mConfigurationService.getString(NgnConfigurationEntry.QOS_PRECOND_STRENGTH,
						NgnConfigurationEntry.DEFAULT_QOS_PRECOND_STRENGTH)));

		// T.140 callback
		if(NgnMediaType.isT140Type(getMediaType())){
			mT140Callback = new NgnT140Callback(this);
		}
		
	    /* 3GPP TS 24.173
	        *
	        * 5.1 IMS communication service identifier
	        * URN used to define the ICSI for the IMS Multimedia Telephony Communication Service: urn:urn-7:3gpp-service.ims.icsi.mmtel. 
	        * The URN is registered at http://www.3gpp.com/Uniform-Resource-Name-URN-list.html.
	        * Summary of the URN: This URN indicates that the device supports the IMS Multimedia Telephony Communication Service.
	        *
	        * Contact: <sip:impu@doubango.org;gr=urn:uuid:xxx;comp=sigcomp>;+g.3gpp.icsi-ref="urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel"
	        * Accept-Contact: *;+g.3gpp.icsi-ref="urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel"
	        * P-Preferred-Service: urn:urn-7:3gpp-service.ims.icsi.mmtel
	        */
	    super.addCaps("+g.3gpp.icsi-ref", "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"");
	    super.addHeader("Accept-Contact", "*;+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"");
	    super.addHeader("P-Preferred-Service", "urn:urn-7:3gpp-service.ims.icsi.mmtel");
	    
	    mHistoryEvent = new NgnHistoryAVCallEvent((mediaType == NgnMediaType.AudioVideo || mediaType == NgnMediaType.Video), null);
	    this.setState(callState);
	}
    
	@Override
	protected SipSession getSession() {
		return mSession;
	}
	
	@Override
	protected  NgnHistoryEvent getHistoryEvent(){
		 return mHistoryEvent;
	}
	
	private boolean initializeConsumersAndProducers(){
		Log.d(TAG, "initializeConsumersAndProducers()");
		if(mConsumersAndProducersInitialzed){
			return true;
		}
		
		final MediaSessionMgr mediaMgr;
		if((mediaMgr = super.getMediaSessionMgr()) != null){
			ProxyPlugin plugin;
			NgnProxyPlugin myProxyPlugin;
			// Video
			if(NgnMediaType.isVideoType(super.mMediaType)){
				if((plugin = mediaMgr.findProxyPluginConsumer(twrap_media_type_t.twrap_media_video)) != null){
					if((myProxyPlugin = NgnProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mVideoConsumer = (NgnProxyVideoConsumer)myProxyPlugin;
						mVideoConsumer.setContext(mContext);
						mVideoConsumer.setSipSessionId(super.getId());
					}
				}
				if((plugin = mediaMgr.findProxyPluginProducer(twrap_media_type_t.twrap_media_video)) != null){
					if((myProxyPlugin = NgnProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mVideoProducer = (NgnProxyVideoProducer)myProxyPlugin;
						mVideoProducer.setContext(mContext);
						mVideoProducer.setSipSessionId(super.getId());
					}
				}
			}
			// Audio
			if(NgnMediaType.isAudioType(super.mMediaType)){
				if((plugin = mediaMgr.findProxyPluginConsumer(twrap_media_type_t.twrap_media_audio)) != null){
					if((myProxyPlugin = NgnProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mAudioConsumer = (NgnProxyAudioConsumer)myProxyPlugin;
						mAudioConsumer.setSipSessionId(super.getId());
					}
				}
				if((plugin = mediaMgr.findProxyPluginProducer(twrap_media_type_t.twrap_media_audio)) != null){
					if((myProxyPlugin = NgnProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mAudioProducer = (NgnProxyAudioProducer)myProxyPlugin;
						mAudioProducer.setSipSessionId(super.getId());
					}
				}
			}
			
			mConsumersAndProducersInitialzed = true;
			return true;
		}
		
		return false;	
	}
	
	private void deInitializeMediaSession(){
		if(super.mMediaSessionMgr != null){
			super.mMediaSessionMgr.delete();
			super.mMediaSessionMgr = null;
		}
	}
	
	private void updateEchoTail(){
		if(mConfigurationService.getBoolean(NgnConfigurationEntry.GENERAL_USE_ECHO_TAIL_ADAPTIVE,
				NgnConfigurationEntry.DEFAULT_GENERAL_USE_ECHO_TAIL_ADAPTIVE)){
			Log.d(TAG, "Setting new echo tail");
			final MediaSessionMgr mediaMgr;
			if((mediaMgr = super.getMediaSessionMgr()) != null){
				final Codec codec = mediaMgr.producerGetCodec(twrap_media_type_t.twrap_media_audio);
				if(codec == null){
					Log.e(TAG, "Failed to get producer codec");
					return;
				}
				final int samplingRate = codec.getAudioSamplingRate();
				final int channels = codec.getAudioChannels();
				if(samplingRate <= 0){
					Log.e(TAG, samplingRate + " not valid as audio sampling rate");
					return;
				}
				if(channels != 1 && channels != 2){
					Log.e(TAG, channels + " not valid as audio channels value");
					return;
				}
				final int minBufferSize = AudioRecord.getMinBufferSize(samplingRate, channels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
				Log.d(TAG, "getMinBufferSize("+samplingRate+ ","+channels+")="+minBufferSize);
				final int echoTail1 = (((1000 * minBufferSize) / samplingRate) << 1);
				final int echoTail2 = echoTail1 < 200 ? 200 : echoTail1; // make it more reasonable
				Log.d(TAG, "Echo tail ("+echoTail1+"->"+echoTail2+")");
				mediaMgr.sessionSetInt32(twrap_media_type_t.twrap_media_audio, "echo-tail", echoTail2);
			}
		}
	}
	
	/**
	 * Gets the context associated to this session. Only used for video session to track the SurfaceView
	 * lifecycle
	 * @return the context
	 */
	public Context getContext(){
		return mContext;
	}
	
	/**
	 * Sets a context to associated to this session
	 * @param context the context
	 */
	public void setContext(Context context){
		mContext = context;
	}
	
	/**
	 * Starts the video consumer. A video consumer view used to display the video stream
	 * sent from the remote party. It's up to you to embed this view into a layout (LinearLayout, RelativeLayou,
	 * FrameLayout, ...) in order to display it.
	 * @return the view where the remote video stream will be displayed
	 */
	public final View startVideoConsumerPreview(){
		if(mVideoConsumer != null){
			return mVideoConsumer.startPreview(mContext);
		}
		return null;
	}
	
	/**
	 * Starts the video producer. A video producer is any device capable to generate video frames.
	 * It's likely a video camera (front facing or rear). The view associated to the producer is used as a feedback to
	 * show the local video stream sent to the remote party.
	 * It's up to you to embed this view into a layout (LinearLayout, RelativeLayou,
	 * FrameLayout, ...) in order to display it.
	 * @return the view where the local video stream will be displayed
	 */
	public final View startVideoProducerPreview(){
		if(mVideoProducer != null){
			return mVideoProducer.startPreview(mContext);
		}
		return null;
	}
	
	/**
	 * Checks whether we are sending video or not
	 * @return true if we are already sending video and false otherwise
	 */
	public boolean isSendingVideo(){
		return mSendingVideo;
	}
	
	public void setSendingVideo(boolean sendingVideo){
		mSendingVideo = sendingVideo;
	}
	
	public void pushBlankPacket(){
		if(mVideoProducer != null){
			mVideoProducer.pushBlankPacket();
		}
	}
	
	/**
	 * Switch from rear to font-facing camera or vice-versa
	 */
	public void toggleCamera(){
		if(mVideoProducer != null){
			mVideoProducer.toggleCamera();
		}
	}
	
	public boolean isFrontFacingCameraEnabled() {
		if(mVideoProducer != null){
			return mVideoProducer.isFrontFacingCameraEnabled();
		}
		return false ;
	}
	
	public int compensCamRotation(boolean preview){
		if(mVideoProducer != null){
			return mVideoProducer.compensCamRotation(preview);
		}
		return 0;
	}
	
	public int camRotation(boolean preview){
		if(mVideoProducer != null){
			return mVideoProducer.getNativeCameraHardRotation(preview);
		}
		return 0;
	}
	
	/**
	 * Sets the local video rotation angle
	 * @param rot rotation angle in degree
	 */
	public void setRotation(int rot){
		if (mVideoProducer != null) {
			mVideoProducer.setRotation(rot);
		}
	}
	
	/**
	 * Sets whether to mirror the outgoing video
	 * @param mirror
	 */
	public void setMirror(boolean mirror){
		if(mVideoProducer != null){
			mVideoProducer.setMirror(mirror);
		}
	}
	
	public boolean setProducerFlipped(boolean flipped){
		final MediaSessionMgr mediaMgr;
		if((mediaMgr = super.getMediaSessionMgr()) != null){
			return mediaMgr.producerSetInt32(twrap_media_type_t.twrap_media_video, "flip", flipped ? 1 : 0);
		}
		return false;
	}
	
	public boolean setConsumerFlipped(boolean flipped){
		final MediaSessionMgr mediaMgr;
		if((mediaMgr = super.getMediaSessionMgr()) != null){
			return mediaMgr.consumerSetInt32(twrap_media_type_t.twrap_media_video, "flip", flipped ? 1 : 0);
		}
		return false;
	}
	
	public boolean isSecure(){
		final MediaSessionMgr mediaMgr;
		if((mediaMgr = super.getMediaSessionMgr()) != null){
			return (mediaMgr.sessionGetInt32(twrap_media_type_t.twrap_media_audiovideo, "srtp-enabled") != 0);
		}
		return false;
	}
	
	// Doubango AEC for THIS session. Default value (for all sessions) is the one in "NgnConfigurationEntry.GENERAL_AEC" configuration entry.
	public boolean setAECEnabled(boolean enabled){
		final MediaSessionMgr mediaMgr;
		if((mediaMgr = super.getMediaSessionMgr()) != null){
			return mediaMgr.sessionSetInt32(twrap_media_type_t.twrap_media_audio, "echo-supp", enabled ? 1 : 0);
		}
		return false;
	}
	
	public boolean setVideoFps(int fps) {
		if (mSession != null) {
			return mSession.setVideoFps(fps);
		}
		return false;
	}
	
	public boolean setVideoBandwidthUploadMax(int bw_max_kbps) {
		if (mSession != null) {
			return mSession.setVideoBandwidthUploadMax(bw_max_kbps);
		}
		return false;
	}
	
	public boolean setVideoBandwidthDownloadMax(int bw_max_kbps) {
		if (mSession != null) {
			return mSession.setVideoBandwidthDownloadMax(bw_max_kbps);
		}
		return false;
	}

	/**
	 * Enables or disables the speakerphone
	 * @param speakerOn true to enable the speakerphone and false to disable it
	 */
	public void setSpeakerphoneOn(boolean speakerOn){
		if(NgnApplication.isSLEs2KnownToWork()){
			final MediaSessionMgr mediaMgr;
			if((mediaMgr = super.getMediaSessionMgr()) != null){
				if(mediaMgr.consumerSetInt32(twrap_media_type_t.twrap_media_audio, "speaker-on", speakerOn ? 1 : 0)){
					mSpeakerOn = speakerOn;
				}
			}
		}
		else{
			if(mAudioProducer != null){
				mAudioProducer.setSpeakerphoneOn(speakerOn);
			}
			if(mAudioConsumer != null){
				mAudioConsumer.setSpeakerphoneOn(speakerOn);
			}
			mSpeakerOn = speakerOn;
		}
	}
	
	/**
	 * Toggles the speakerphone. Enable it if disabled and vice-versa
	 */
	public void toggleSpeakerphone(){
		setSpeakerphoneOn(!mSpeakerOn);
	}
	
	public boolean isSpeakerOn(){
		if(!NgnApplication.isSLEs2KnownToWork()){
			if(mAudioProducer != null){
				return mAudioProducer.isSpeakerOn();
			}
		}
		return mSpeakerOn;
	}
	
	public boolean isMicrophoneMute() {
		if(NgnApplication.isSLEs2KnownToWork()){
			return mMuteOn;
		}
		else{
			if(mAudioProducer != null){
				return mAudioProducer.isOnMute();
			}
			return mMuteOn;
		}
	}
	
	public void setMicrophoneMute(boolean mute) {
		if(NgnApplication.isSLEs2KnownToWork()){
			final MediaSessionMgr mediaMgr;
			if((mediaMgr = super.getMediaSessionMgr()) != null){
				if(mediaMgr.producerSetInt32(twrap_media_type_t.twrap_media_audio, "mute", mute ? 1 : 0)){
					mMuteOn =  mute;
				}
			}
		}
		else{
		    if(mAudioProducer != null){
		        mAudioProducer.setOnMute(mute);
		        mMuteOn = mAudioProducer.isOnMute();
		    } 
		}
	}
	
	public boolean onVolumeChanged(boolean bDown){
		if(!NgnApplication.isSLEs2KnownToWork()){
			if(mAudioProducer == null || !mAudioProducer.onVolumeChanged(bDown)){
				return false;
			}
			if(mAudioConsumer == null || !mAudioConsumer.onVolumeChanged(bDown)){
				return false;
			}
		}
		return false;
	}
	
	public void setMode(InviteState state){
		if(NgnApplication.isSetModeAllowed()){
			final AudioManager audiomanager = NgnApplication.getAudioManager();
			if(audiomanager != null){
				Log.d(TAG, "setMode("+state+")");
				switch(state){
					case INCOMING:
					case INPROGRESS:
					case REMOTE_RINGING:
						audiomanager.setMode(AudioManager.MODE_RINGTONE);
						break;
					case INCALL:
					case EARLY_MEDIA:
						audiomanager.setMode(NgnApplication.getSDKVersion() >= 11 ? AudioManager_MODE_IN_COMMUNICATION : AudioManager.MODE_IN_CALL);
						break;
					case TERMINATED:
					case TERMINATING:
						audiomanager.setMode(AudioManager.MODE_NORMAL);
						break;
					default: break;
				}
			}
		}
	}
	
	@Override
	public void setState(InviteState state){
		if(super.mState == state){
			return;
		}
		Log.d(TAG, "setState("+state+")");
		super.setState(state);
		setMode(state);
		
		switch(state){
			case INCOMING:
				initializeConsumersAndProducers();
				break;
				
			case INPROGRESS:
				initializeConsumersAndProducers();
				break;
				
			case INCALL:
			case EARLY_MEDIA:
				initializeConsumersAndProducers();
				updateEchoTail();
				mSession.setT140Callback(mT140Callback);
				break;
			
			case TERMINATED:
			case TERMINATING:
				deInitializeMediaSession();
				mSession.setT140Callback(null);
				break;
			default:
				break;
		}
		
		super.setChangedAndNotifyObservers(this);
    }
	
	public long getStartTime(){
		return mHistoryEvent.getStartTime();
	}
	
	public int getVideoWidthNegotiated() {
		return (mVideoConsumer != null) ? mVideoConsumer.getVideoWidthNegotiated() : 0;
	}
	
	public int getVideoHeightNegotiated() {
		return (mVideoConsumer != null) ? mVideoConsumer.getVideoHeightNegotiated() : 0;
	}
	
	public int getVideoWidthReceived() {
		return (mVideoConsumer != null) ? mVideoConsumer.getVideoWidthReceived() : 0;
	}
	
	public int getVideoHeightReceived() {
		return (mVideoConsumer != null) ? mVideoConsumer.getVideoHeightReceived() : 0;
	}
	
	/**
	 * Accepts an incoming audio/video call
	 * @return true is succeed and false otherwise
	 * @sa @ref hangUpCall()
	 */
	public boolean acceptCall(){
        return super.isActive() ? mSession.accept() : false;
    }

	/**
	 * Ends an audio/video call. The call could be in any state: incoming, outgoing, incall, ...
	 * @return true if succeed and false otherwise
	 */
    public boolean hangUpCall(){
    	if (super.isActive()) {
    		return super.isConnected() ? mSession.hangup() : mSession.reject();
    	}
    	return false;
    }

    /**
     * Puts the call on hold. At any time you can check if the call is held or not by using @ref isLocalHeld()
     * @return true if succeed and false otherwise
     * @sa @ref resumeCall() @ref isLocalHeld() @ref isRemoteHeld() @ref resumeCall()
     */
    public boolean holdCall(){
		return super.isActive() ? mSession.hold() : false;
	}
    
    /**
     * Resumes a call. The call should be previously held using @ref holdCall()
     * @return true is succeed and false otherwise
     * @sa @ref holdCall() @ref isLocalHeld() @ref isRemoteHeld()
     */
	public boolean resumeCall(){		
		return super.isActive() ? mSession.resume() : false;
	}
	
	/**
	 * Transfers the current call.
	 * @param transferUri The destination Uri.
	 * @return true if succeed and false otherwise.
	 */
	public boolean transferCall(String transferUri){
        if (NgnStringUtils.isNullOrEmpty(transferUri) || !NgnUriUtils.isValidSipUri(transferUri)){
            return false;
        }
        return super.isActive() ? mSession.transfer(transferUri) : false;
    }

	/**
	 * Accepts the incoming call transfer request.
	 * @return true if succeed and false otherwise.
	 */
    public boolean acceptCallTransfer(){
        return super.isActive() ? mSession.acceptTransfer() : false;
    }

    /**
     * Rejects the incoming call transfer request.
     * @return true if succeed and false otherwise.
     */
    public boolean rejectCallTransfer(){
        return super.isActive() ? mSession.rejectTransfer() : false;
    }

	/**
	 * Update the call session
	 * @param newMediaType new media type
	 * @return true if succeed and false otherwise.
	 */
	public boolean updateCall(NgnMediaType newMediaType) {
		if (super.mMediaType == newMediaType) {
			return true;
		}
		boolean ret;
		super.mMediaType = newMediaType;
		return makeCall(super.getRemotePartyUri());
	}

	/**
	 * Checks whether the call is locally held held or not. You should use @ref resumeCall() to resume
	 * the call.
	 * @return true if locally held and false otherwise
	 * @sa @ref isRemoteHeld()
	 */
	@Override
	public boolean isLocalHeld(){
		return super.isLocalHeld();
	}
	
	@Override
	public void setLocalHold(boolean localHold){
		final boolean changed = mLocalHold!= localHold;
		super.setLocalHold(localHold);
		
		if(mVideoProducer != null){
			mVideoProducer.setOnPause(mLocalHold);
		}
		if(mAudioProducer != null){
			mAudioProducer.setOnPause(mLocalHold);
		}
		
		if(changed){
			super.setChangedAndNotifyObservers(this);
		}
	}
	
	/**
	 * Checks whether the call is remotely held or not
	 * @return true if the call is remotely held and false otherwise
	 * @sa @ref isLocalHeld()
	 */
	@Override
	public boolean isRemoteHeld(){
		return super.isRemoteHeld();
	}
	
	@Override
	public void setRemoteHold(boolean remoteHold){
		final boolean changed = mRemoteHold != remoteHold;
		super.setRemoteHold(remoteHold);
		
		if(changed){
			super.setChangedAndNotifyObservers(this);
		}
	}

	public boolean isOnMute(){
		if(mAudioProducer != null){
			return mAudioProducer.isOnMute();
		}
		return false;
	}
	
	public void setOnMute(boolean bOnMute){
		if(mAudioProducer != null){
			mAudioProducer.setOnMute(bOnMute);
		}
	}
	
    /**
     * Sends DTMF digit. The session must be active (incoming, outgoing, incall, ...) in order to try
     * to send DTMF digits.
     * @param digit the digit to send
     * @return true if succeed and false otherwise
     */
    public boolean sendDTMF(int digit){
        return mSession.sendDTMF(digit);
    }
    
    private boolean sendT140Data(tmedia_t140_data_type_t dataType, String dataStr) {
    	if(!isConnected()){
    		Log.e(TAG, "Cannot send T.140 data. The session must be connected first.");
    		return false;
    	}
    	if(!NgnMediaType.isT140Type(getMediaType())){
    		Log.e(TAG, "Cannot send T.140 data. Not supported by this session.");
    		return false;
    	}
        if (!NgnStringUtils.isNullOrEmpty(dataStr)){
			try {
				byte[] bytes = dataStr.getBytes("UTF-8");
				ByteBuffer dataPtr = ByteBuffer.allocateDirect(bytes.length);
	            dataPtr.put(bytes);      
	            return mSession.sendT140Data(dataType, dataPtr, dataPtr.capacity());
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, e.toString());
				return false;
			}
        }
        else{
            return mSession.sendT140Data(dataType);
        }
    }

    public boolean sendT140Data(String dataStr){
        return sendT140Data(tmedia_t140_data_type_t.tmedia_t140_data_type_utf8, dataStr);
    }

    public boolean sendT140Data(tmedia_t140_data_type_t dataType){
        return sendT140Data(dataType, null);
    }
    
    
    /**
     * NgnT140Callback
     */
    static class NgnT140Callback extends T140Callback {
    	final NgnAVSession mAVSession;
    	
    	NgnT140Callback(NgnAVSession avSession){
    		mAVSession = avSession;
    	}

		@Override
		public int ondata(T140CallbackData pData) {
			tmedia_t140_data_type_t dataType = pData.getType();
			final Intent intent = new Intent(NgnMessagingEventArgs.ACTION_MESSAGING_EVENT);
			final byte[] bytes;
			final String contentType;
			switch(dataType){
				case tmedia_t140_data_type_utf8:
					{
						bytes = pData.getData();
						contentType = NgnContentType.TEXT_PLAIN;
						break;
					}
				default:
					{
						bytes = null;
						contentType = NgnContentType.T140COMMAND;
						break;
					}
			}
			
			final NgnMessagingEventArgs args = new NgnMessagingEventArgs(mAVSession.getId(), NgnMessagingEventTypes.INCOMING, 
					"T.140", bytes, contentType);
			intent.putExtra(NgnMessagingEventArgs.EXTRA_REMOTE_PARTY, mAVSession.getRemotePartyUri());
			intent.putExtra(NgnMessagingEventArgs.EXTRA_DATE, NgnDateTimeUtils.now());
			intent.putExtra(NgnMessagingEventArgs.EXTRA_EMBEDDED, args);
			intent.putExtra(NgnMessagingEventArgs.EXTRA_T140_DATA_TYPE, dataType);
			NgnApplication.getContext().sendBroadcast(intent);
			
			return 0;
		}
    }
}