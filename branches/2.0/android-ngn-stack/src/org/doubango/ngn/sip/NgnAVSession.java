package org.doubango.ngn.sip;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.media.NgnProxyAudioConsumer;
import org.doubango.ngn.media.NgnProxyAudioProducer;
import org.doubango.ngn.media.NgnProxyPlugin;
import org.doubango.ngn.media.NgnProxyPluginMgr;
import org.doubango.ngn.media.NgnProxyVideoConsumer;
import org.doubango.ngn.media.NgnProxyVideoProducer;
import org.doubango.ngn.model.NgnHistoryAVCallEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.tinyWRAP.ActionConfig;
import org.doubango.tinyWRAP.CallSession;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.tinyWRAP.ProxyPlugin;
import org.doubango.tinyWRAP.SipMessage;
import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.twrap_media_type_t;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.View;

public class NgnAVSession extends NgnInviteSession{

	private CallSession mSession;
	private boolean mConsumersAndProducersInitialzed;
	private NgnProxyVideoConsumer mVideoConsumer;
	private NgnProxyAudioConsumer mAudioConsumer;
	private NgnProxyVideoProducer mVideoProducer;
	private NgnProxyAudioProducer mAudioProducer;
	private Context mContext;
	
	private final NgnHistoryAVCallEvent mHistoryEvent;
	
	private boolean mRemoteHold;
	private boolean mLocalHold;
	private boolean mSendingVideo;
	
    private final static NgnObservableHashMap<Long, NgnAVSession> sSessions = new NgnObservableHashMap<Long, NgnAVSession>(true);
    
    public static NgnAVSession takeIncomingSession(NgnSipStack sipStack, CallSession session, twrap_media_type_t mediaType, SipMessage sipMessage){
        NgnMediaType media;

        synchronized (sSessions){
            switch (mediaType){
                case twrap_media_audio:
                    media = NgnMediaType.Audio;
                    break;
                case twrap_media_video:
                    media = NgnMediaType.Video;
                    break;
                case twrap_media_audiovideo:
                    media = NgnMediaType.AudioVideo;
                    break;
                default:
                    return null;
            }
            NgnAVSession avSession = new NgnAVSession(sipStack, session, media, InviteState.INCOMING);
            if (sipMessage != null){
                avSession.setRemotePartyUri(sipMessage.getSipHeaderValue("f"));
            }
            sSessions.put(avSession.getId(), avSession);
            return avSession;
        }
    }

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

	public static NgnAVSession getSession(long id) {
		synchronized (sSessions) {
			if (sSessions.containsKey(id))
				return sSessions.get(id);
			else
				return null;
		}
	}

	public static int getSize(){
        synchronized (sSessions){
            return sSessions.size();
        }
    }
	
    public static boolean hasSession(long id){
        synchronized (sSessions){
            return sSessions.containsKey(id);
        }
    }
    
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
    
    public static NgnAVSession getFirstActiveCallAndNot(long id){
		NgnAVSession session;
		for(Map.Entry<Long, NgnAVSession> entry : sSessions.entrySet()) {
			session = entry.getValue();
			if(session.getId() != id && session.isConnected() && !session.isLocalHeld() && !session.isRemoteHeld()){
				return session;
			}
		}
		return null;
	}
    
    public static boolean makeAudioCall(String remoteUri, NgnSipStack sipStack){
    	NgnAVSession avSession = NgnAVSession.createOutgoingSession(sipStack, NgnMediaType.Audio);
    	return avSession.makeCall(NgnUriUtils.makeValidSipUri(remoteUri));
    }
    
    public static boolean makeAudioVideoCall(String remoteUri, NgnSipStack sipStack){
    	NgnAVSession avSession = NgnAVSession.createOutgoingSession(sipStack, NgnMediaType.AudioVideo);
    	return avSession.makeCall(NgnUriUtils.makeValidSipUri(remoteUri));
    }
    
    protected NgnAVSession(NgnSipStack sipStack, CallSession session, NgnMediaType mediaType, InviteState callState){
		super(sipStack);
		mSession = (session == null) ? new CallSession(sipStack) : session;
	    super.mMediaType = mediaType;
	
	    // commons
	    super.init();
	    // SigComp
	    super.setSigCompId(sipStack.getSigCompId());
	    // 100rel
	    mSession.set100rel(true); // will add "Supported: 100rel"
	
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
	    setState(callState);
	}
    
	@Override
	protected SipSession getSession() {
		return mSession;
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
			if(super.mMediaType == NgnMediaType.Video || super.mMediaType == NgnMediaType.AudioVideo){
				if((plugin = mediaMgr.findProxyPluginConsumer(twrap_media_type_t.twrap_media_video)) != null){
					if((myProxyPlugin = NgnProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mVideoConsumer = (NgnProxyVideoConsumer)myProxyPlugin;
						mVideoConsumer.setContext(mContext);
					}
				}
				if((plugin = mediaMgr.findProxyPluginProducer(twrap_media_type_t.twrap_media_video)) != null){
					if((myProxyPlugin = NgnProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mVideoProducer = (NgnProxyVideoProducer)myProxyPlugin;
						mVideoProducer.setContext(mContext);
					}
				}
			}
			// Audio
			if(super.mMediaType == NgnMediaType.Audio || super.mMediaType == NgnMediaType.AudioVideo){
				if((plugin = mediaMgr.findProxyPluginConsumer(twrap_media_type_t.twrap_media_audio)) != null){
					if((myProxyPlugin = NgnProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mAudioConsumer = (NgnProxyAudioConsumer)myProxyPlugin;
					}
				}
				if((plugin = mediaMgr.findProxyPluginProducer(twrap_media_type_t.twrap_media_audio)) != null){
					if((myProxyPlugin = NgnProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mAudioProducer = (NgnProxyAudioProducer)myProxyPlugin;
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
	
	public Context getContext(){
		return mContext;
	}
	
	public void setContext(Context context){
		mContext = context;
	}
	
	public final View startVideoConsumerPreview(){
		if(mVideoConsumer != null){
			return mVideoConsumer.startPreview();
		}
		return null;
	}
	
	public final View startVideoProducerPreview(){
		if(mVideoProducer != null){
			return mVideoProducer.startPreview();
		}
		return null;
	}
	
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
	
	public void toggleCamera(){
		if(mVideoProducer != null){
			mVideoProducer.toggleCamera();
		}
	}
	
	public void setRotation(int rot){
		if(mVideoProducer != null){
			mVideoProducer.setRotation(rot);
		}
	}
	
	public void setSpeakerphoneOn(boolean speakerOn){
		if(mAudioProducer != null){
			mAudioProducer.setSpeakerphoneOn(speakerOn);
		}
		if(mAudioConsumer != null){
			mAudioConsumer.setSpeakerphoneOn(speakerOn);
		}
	}
	
	public void toggleSpeakerphone(){
		if(mAudioProducer != null){
			mAudioProducer.toggleSpeakerphone();
		}
		if(mAudioConsumer != null){
			mAudioConsumer.toggleSpeakerphone();
		}
	}
	
	public void setModeInCall(boolean bInCall){
		final AudioManager audiomanager = NgnApplication.getAudioManager();
		if(NgnApplication.isSetModeAllowed()){
			audiomanager.setMode(bInCall ? AudioManager.MODE_IN_CALL : AudioManager.MODE_NORMAL);
		}
	}
	
	@Override
	public void setState(InviteState state){
		if(super.mState == state){
			return;
		}
		super.setState(state);
		
		switch(state){
			case INCOMING:
				mHistoryEvent.setStatus(StatusType.Incoming);
				initializeConsumersAndProducers();
				break;
				
			case INPROGRESS:
				mHistoryEvent.setStatus(StatusType.Outgoing);
				initializeConsumersAndProducers();
				break;
				
			case INCALL:
				setModeInCall(true);
				mHistoryEvent.setStartTime(new Date().getTime());
				initializeConsumersAndProducers();
				break;
			
			case TERMINATED:
				setModeInCall(false);
				if (mHistoryEvent.getStartTime() == mHistoryEvent.getEndTime() 
						&& mHistoryEvent.getStatus() == StatusType.Incoming) {
					mHistoryEvent.setStatus(StatusType.Missed);
				} else {
					mHistoryEvent.setEndTime(new Date().getTime());
				}
				mHistoryEvent.setRemoteParty(getRemotePartyUri());
				NgnEngine.getInstance().getHistoryService().addEvent(mHistoryEvent);
				deInitializeMediaSession();
				break;
		}
		
		super.setChangedAndNotifyObservers(this);
    }
	
	public long getStartTime(){
		return mHistoryEvent.getStartTime();
	}
	
	public boolean acceptCall(){
        return mSession.accept();
    }

    public boolean hangUpCall(){
        if (super.isConnected()){
            return mSession.hangup();
        }
        else{
            return mSession.reject();
        }
    }

    public boolean holdCall(){
		return mSession.hold();
	}
	
	public boolean resumeCall(){		
		return mSession.resume();
	}
	
	public boolean isLocalHeld(){
		return mLocalHold;
	}
	
	public void setLocalHold(boolean localHold){
		final boolean changed = mLocalHold!= localHold;
		mLocalHold = localHold;
		
		if(mVideoProducer != null){
			mVideoProducer.setOnPause(mLocalHold || mRemoteHold);
		}
		if(mAudioProducer != null){
			mAudioProducer.setOnPause(mLocalHold || mRemoteHold);
		}
		
		if(changed){
			super.setChangedAndNotifyObservers(this);
		}
	}
	
	public boolean isRemoteHeld(){
		return mRemoteHold;
	}
	
	public void setRemoteHold(boolean remoteHold){
		final boolean changed = mRemoteHold != remoteHold;
		mRemoteHold = remoteHold;
		
		if(mVideoProducer != null){
			mVideoProducer.setOnPause(mLocalHold || mRemoteHold);
		}
		if(mAudioProducer != null){
			mAudioProducer.setOnPause(mLocalHold || mRemoteHold);
		}
		
		if(changed){
			super.setChangedAndNotifyObservers(this);
		}
	}

    public boolean makeCall(String remoteUri){
        boolean ret;

        super.mOutgoing = true;
        super.setToUri(remoteUri);

        ActionConfig config = new ActionConfig();
        switch (super.getMediaType())
        {
            case AudioVideo:
            case Video:
                ret = mSession.callAudioVideo(remoteUri, config);
                break;
            case Audio:
            default:
                ret = mSession.callAudio(remoteUri, config);
                break;
        }
        config.delete();

        return ret;
    }

    public boolean makeVideoSharingCall(String remoteUri){
        boolean ret;

        super.mOutgoing = true;

        ActionConfig config = new ActionConfig();
        ret = mSession.callVideo(remoteUri, config);
        config.delete();

        return ret;
    }

    public boolean sendDTMF(int digit){
        return mSession.sendDTMF(digit);
    }

}