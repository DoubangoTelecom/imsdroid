package org.doubango.imsdroid.Sip;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Media.MediaType;
import org.doubango.imsdroid.Media.MyProxyAudioConsumer;
import org.doubango.imsdroid.Media.MyProxyAudioProducer;
import org.doubango.imsdroid.Media.MyProxyPlugin;
import org.doubango.imsdroid.Media.MyProxyPluginMgr;
import org.doubango.imsdroid.Media.MyProxyVideoConsumer;
import org.doubango.imsdroid.Media.MyProxyVideoProducer;
import org.doubango.imsdroid.Model.HistoryAVCallEvent;
import org.doubango.imsdroid.Model.HistoryEvent.StatusType;
import org.doubango.imsdroid.Utils.ObservableHashMap;
import org.doubango.imsdroid.Utils.UriUtils;
import org.doubango.tinyWRAP.ActionConfig;
import org.doubango.tinyWRAP.CallSession;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.tinyWRAP.ProxyPlugin;
import org.doubango.tinyWRAP.SipMessage;
import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.twrap_media_type_t;

import android.content.Context;
import android.util.Log;
import android.view.View;

public class MyAVSession extends MyInviteSession{

	private CallSession mSession;
	private boolean mConsumersAndProducersInitialzed;
	private MyProxyVideoConsumer mVideoConsumer;
	private MyProxyAudioConsumer mAudioConsumer;
	private MyProxyVideoProducer mVideoProducer;
	private MyProxyAudioProducer mAudioProducer;
	private Context mContext;
	
	private final HistoryAVCallEvent mHistoryEvent;
	
	private boolean mRemoteHold;
	private boolean mLocalHold;
	private boolean mSendingVideo;
	
    private final static ObservableHashMap<Long, MyAVSession> sessions = new ObservableHashMap<Long, MyAVSession>(true);
    
    public static MyAVSession takeIncomingSession(MySipStack sipStack, CallSession session, twrap_media_type_t mediaType, SipMessage sipMessage){
        MediaType media;

        synchronized (MyAVSession.sessions){
            switch (mediaType){
                case twrap_media_audio:
                    media = MediaType.Audio;
                    break;
                case twrap_media_video:
                    media = MediaType.Video;
                    break;
                case twrap_media_audiovideo:
                    media = MediaType.AudioVideo;
                    break;
                default:
                    return null;
            }
            MyAVSession avSession = new MyAVSession(sipStack, session, media, InviteState.INCOMING);
            if (sipMessage != null){
                avSession.setRemotePartyUri(sipMessage.getSipHeaderValue("f"));
            }
            MyAVSession.sessions.put(avSession.getId(), avSession);
            return avSession;
        }
    }

    public static MyAVSession createOutgoingSession(MySipStack sipStack, MediaType mediaType){
        synchronized (MyAVSession.sessions){
            final MyAVSession avSession = new MyAVSession(sipStack, null, mediaType, InviteState.INPROGRESS);
            MyAVSession.sessions.put(avSession.getId(), avSession);
            return avSession;
        }
    }
	
	public static void releaseSession(MyAVSession session){
		synchronized (MyAVSession.sessions){
            if (session != null && MyAVSession.sessions.containsKey(session.getId())){
                long id = session.getId();
                session.decRef();
                MyAVSession.sessions.remove(id);
            }
        }
    }

	public static MyAVSession getSession(long id) {
		synchronized (MyAVSession.sessions) {
			if (MyAVSession.sessions.containsKey(id))
				return MyAVSession.sessions.get(id);
			else
				return null;
		}
	}

	public static int getSize(){
        synchronized (MyAVSession.sessions){
            return MyAVSession.sessions.size();
        }
    }
	
    public static boolean hasSession(long id){
        synchronized (MyAVSession.sessions){
            return MyAVSession.sessions.containsKey(id);
        }
    }
    
    public static boolean hasActiveSession(){
    	synchronized (MyAVSession.sessions){
    		final Collection<MyAVSession> mysessions = MyAVSession.sessions.values();
	    	for(MyAVSession session : mysessions){
	    		if(session.isActive()){
	    			return true;
	    		}
	    	}
    	}
    	return false;
    }
    
    public static MyAVSession getFirstActiveCallAndNot(long id){
		MyAVSession session;
		for(Map.Entry<Long, MyAVSession> entry : MyAVSession.sessions.entrySet()) {
			session = entry.getValue();
			if(session.getId() != id && session.isConnected() && !session.isLocalHeld() && !session.isRemoteHeld()){
				return session;
			}
		}
		return null;
	}
    
    public static boolean makeAudioCall(String remoteUri, MySipStack sipStack){
    	MyAVSession avSession = MyAVSession.createOutgoingSession(sipStack, MediaType.Audio);
    	return avSession.makeCall(UriUtils.makeValidSipUri(remoteUri));
    }
    
    public static boolean makeAudioVideoCall(String remoteUri, MySipStack sipStack){
    	MyAVSession avSession = MyAVSession.createOutgoingSession(sipStack, MediaType.AudioVideo);
    	return avSession.makeCall(UriUtils.makeValidSipUri(remoteUri));
    }
    
    protected MyAVSession(MySipStack sipStack, CallSession session, MediaType mediaType, InviteState callState){
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
	    
	    mHistoryEvent = new HistoryAVCallEvent((mediaType == MediaType.AudioVideo || mediaType == MediaType.Video), null);
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
			MyProxyPlugin myProxyPlugin;
			// Video
			if(super.mMediaType == MediaType.Video || super.mMediaType == MediaType.AudioVideo){
				if((plugin = mediaMgr.findProxyPluginConsumer(twrap_media_type_t.twrap_media_video)) != null){
					if((myProxyPlugin = MyProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mVideoConsumer = (MyProxyVideoConsumer)myProxyPlugin;
						mVideoConsumer.setContext(mContext);
					}
				}
				if((plugin = mediaMgr.findProxyPluginProducer(twrap_media_type_t.twrap_media_video)) != null){
					if((myProxyPlugin = MyProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mVideoProducer = (MyProxyVideoProducer)myProxyPlugin;
						mVideoProducer.setContext(mContext);
					}
				}
			}
			// Audio
			if(super.mMediaType == MediaType.Audio || super.mMediaType == MediaType.AudioVideo){
				if((plugin = mediaMgr.findProxyPluginConsumer(twrap_media_type_t.twrap_media_audio)) != null){
					if((myProxyPlugin = MyProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mAudioConsumer = (MyProxyAudioConsumer)myProxyPlugin;
					}
				}
				if((plugin = mediaMgr.findProxyPluginProducer(twrap_media_type_t.twrap_media_audio)) != null){
					if((myProxyPlugin = MyProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mAudioProducer = (MyProxyAudioProducer)myProxyPlugin;
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
		if(mAudioConsumer != null){
			mAudioConsumer.setSpeakerphoneOn(speakerOn);
		}
		if(mAudioProducer != null){
			mAudioProducer.setSpeakerphoneOn(speakerOn);
		}
	}
	
	public void toggleSpeakerphone(){
		if(mAudioConsumer != null){
			mAudioConsumer.toggleSpeakerphone();
		}
		if(mAudioProducer != null){
			mAudioProducer.toggleSpeakerphone();
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
				mHistoryEvent.setStartTime(new Date().getTime());
				initializeConsumersAndProducers();
				break;
			
			case TERMINATED:
				if (mHistoryEvent.getStartTime() == mHistoryEvent.getEndTime() 
						&& mHistoryEvent.getStatus() == StatusType.Incoming) {
					mHistoryEvent.setStatus(StatusType.Missed);
				} else {
					mHistoryEvent.setEndTime(new Date().getTime());
				}
				mHistoryEvent.setRemoteParty(getRemotePartyUri());
				ServiceManager.getHistoryService().addEvent(mHistoryEvent);
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
