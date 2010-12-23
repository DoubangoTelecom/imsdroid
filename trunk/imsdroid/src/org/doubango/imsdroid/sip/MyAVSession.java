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
package org.doubango.imsdroid.sip;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.HistoryAVCallEvent;
import org.doubango.imsdroid.Model.ObservableHashMap;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Model.HistoryEvent.StatusType;
import org.doubango.imsdroid.Screens.ScreenAV;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.media.MediaType;
import org.doubango.imsdroid.media.MyProxyAudioConsumer;
import org.doubango.imsdroid.media.MyProxyAudioProducer;
import org.doubango.imsdroid.media.MyProxyPlugin;
import org.doubango.imsdroid.media.MyProxyPluginMgr;
import org.doubango.imsdroid.media.MyProxyVideoConsumer;
import org.doubango.imsdroid.media.MyProxyVideoProducer;
import org.doubango.tinyWRAP.ActionConfig;
import org.doubango.tinyWRAP.CallSession;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.tinyWRAP.ProxyPlugin;
import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.tmedia_bandwidth_level_t;
import org.doubango.tinyWRAP.tmedia_qos_strength_t;
import org.doubango.tinyWRAP.tmedia_qos_stype_t;
import org.doubango.tinyWRAP.twrap_media_type_t;

import android.content.Context;
import android.util.Log;
import android.view.View;

public class MyAVSession  extends MyInviteSession{
	private static final String TAG = MyAVSession.class.getCanonicalName();
	private static ObservableHashMap<Long, MyAVSession> sessions;
	
	private boolean isConsumersAndProducersInitialzed;
	private final CallSession session;
	private MediaSessionMgr mediaSessionMgr;
	
	private MyProxyVideoConsumer videoConsumer;
	private MyProxyAudioConsumer audioConsumer;
	private MyProxyVideoProducer videoProducer;
	private MyProxyAudioProducer audioProducer;
	
	private Context context;
	private String remoteParty;
	private final MediaType mediaType;
	private CallState state;
	private boolean sendingVideo;
	private boolean remoteHold;
	private boolean localHold;
	private final HistoryAVCallEvent historyEvent;
	
//	private static AudioConsumer __audioConsumer;
//	private static AudioProducer __audioProducer;
//	private static VideoProducer __videoProducer;
//	private static VideoConsumer __videoConsumer;
	private static ScreenAV.AVInviteEventHandler __callEventHandler;
	
	public enum CallState{
		NONE,
		CALL_INCOMING,
		CALL_INPROGRESS,
		REMOTE_RINGING,
		EARLY_MEDIA,
		INCALL,
		CALL_TERMINATED,
	}
	
	static {
		MyProxyPluginMgr.Initialize();
		MyAVSession.sessions = new ObservableHashMap<Long, MyAVSession>(true);
		__callEventHandler = new ScreenAV.AVInviteEventHandler();
	}
	
	public static ScreenAV.AVInviteEventHandler getCallEventHandler(){
		return MyAVSession.__callEventHandler;
	}
	
	public static MyAVSession takeIncomingSession(MySipStack sipStack, CallSession session, MediaType mediaType){
		synchronized(MyAVSession.sessions){
			MyAVSession avSession = new MyAVSession(sipStack, session, mediaType, CallState.CALL_INCOMING);
			MyAVSession.sessions.put(avSession.getId(), avSession);
			return avSession;
		}
	}
	
	public static MyAVSession createOutgoingSession(MySipStack sipStack, MediaType mediaType){
		synchronized(MyAVSession.sessions){
			MyAVSession avSession = new MyAVSession(sipStack, null, mediaType, CallState.CALL_INPROGRESS);
			MyAVSession.sessions.put(avSession.getId(), avSession);
			return avSession;
		}	
	}
	
	public static MyAVSession getSession(long id){
		synchronized(MyAVSession.sessions){
			return MyAVSession.sessions.get(id);
		}
	}
	
	public static Collection<MyAVSession> getValues(){
		synchronized(MyAVSession.sessions){
			return MyAVSession.sessions.values();
		}
	}
	
	public static ObservableHashMap<Long, MyAVSession> getSessions(){
		synchronized(MyAVSession.sessions){
			return MyAVSession.sessions;
		}
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
	
	public static boolean contains(long id){
		synchronized(MyAVSession.sessions){
			return MyAVSession.sessions.containsKey(id);
		}
	}
	
	public static void releaseSession(MyAVSession session){
		if(session != null){
			synchronized(MyAVSession.sessions){
				long id = session.getId();
				session.delete();
				MyAVSession.sessions.remove(id);
			}
		}
	}
	
	public static void releaseSession(long id){
		synchronized(MyAVSession.sessions){
			MyAVSession.sessions.remove(id);
		}
	}
	
	private MyAVSession(MySipStack sipStack, CallSession session, MediaType mediaType, CallState state) {
		super(sipStack);
		
		this.session = (session == null) ? new CallSession(sipStack) : session;
		this.mediaType = mediaType;
		this.state = state;
		this.historyEvent = new HistoryAVCallEvent((mediaType == MediaType.AudioVideo || mediaType == MediaType.Video), "Unknown");
		
		switch(this.state){
			case CALL_INCOMING:
				this.historyEvent.setStatus(StatusType.Incoming);
				break;
			case CALL_INPROGRESS:
				this.historyEvent.setStatus(StatusType.Outgoing);
				break;
		}
		
		final IConfigurationService configurationService = ServiceManager.getConfigurationService();
		
		// commons
		this.init();
		
		// SigComp
		this.setSigCompId(sipStack.getSigCompId());
		
		// 100rel
		this.session.set100rel(true); // will add "Supported: 100rel"
		
		// Session timers
		if(configurationService.getBoolean(CONFIGURATION_SECTION.QOS, CONFIGURATION_ENTRY.SESSION_TIMERS, Configuration.DEFAULT_QOS_SESSION_TIMERS)){
			this.session.setSessionTimer((long) configurationService.getInt(
					CONFIGURATION_SECTION.QOS,
					CONFIGURATION_ENTRY.SIP_CALLS_TIMEOUT,
					Configuration.DEFAULT_QOS_SIP_CALLS_TIMEOUT),
					configurationService.getString(CONFIGURATION_SECTION.QOS,
							CONFIGURATION_ENTRY.REFRESHER,
							Configuration.DEFAULT_QOS_REFRESHER));
		}
		// Precondition
		this.session.setQoS(
				tmedia_qos_stype_t.valueOf(configurationService.getString(
				CONFIGURATION_SECTION.QOS,
				CONFIGURATION_ENTRY.PRECOND_TYPE,
				Configuration.DEFAULT_QOS_PRECOND_TYPE)), 
				tmedia_qos_strength_t.valueOf(configurationService.getString(
				CONFIGURATION_SECTION.QOS,
				CONFIGURATION_ENTRY.PRECOND_STRENGTH,
				Configuration.DEFAULT_QOS_PRECOND_STRENGTH)));
		// this.session.addHeader("Supported", "precondition"); -> already added by doubango
		
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
		this.session.addCaps("+g.3gpp.icsi-ref", "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"");
		this.session.addHeader("Accept-Contact", "*;+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"");
		this.session.addHeader("P-Preferred-Service", "urn:urn-7:3gpp-service.ims.icsi.mmtel");
	}
	
	private boolean initializeConsumersAndProducers(){
		Log.d(MyAVSession.TAG, "initializeConsumersAndProducers()");
		if(this.isConsumersAndProducersInitialzed){
			return true;
		}
		
		if(this.mediaSessionMgr == null && this.session != null){
			this.mediaSessionMgr = this.session.getMediaMgr();
		}
		if(this.mediaSessionMgr != null){
			ProxyPlugin plugin;
			MyProxyPlugin myProxyPlugin;
			// Video
			if(this.mediaType == MediaType.Video || this.mediaType == MediaType.AudioVideo){
				if((plugin = this.mediaSessionMgr.findProxyPluginConsumer(twrap_media_type_t.twrap_media_video)) != null){
					if((myProxyPlugin = MyProxyPluginMgr.findPlugin(plugin.getId())) != null){
						this.videoConsumer = (MyProxyVideoConsumer)myProxyPlugin;
						this.videoConsumer.setContext(this.context);
					}
				}
				if((plugin = this.mediaSessionMgr.findProxyPluginProducer(twrap_media_type_t.twrap_media_video)) != null){
					if((myProxyPlugin = MyProxyPluginMgr.findPlugin(plugin.getId())) != null){
						this.videoProducer = (MyProxyVideoProducer)myProxyPlugin;
						this.videoProducer.setContext(this.context);
					}
				}
			}
			// Audio
			if(this.mediaType == MediaType.Audio || this.mediaType == MediaType.AudioVideo){
				if((plugin = this.mediaSessionMgr.findProxyPluginConsumer(twrap_media_type_t.twrap_media_audio)) != null){
					if((myProxyPlugin = MyProxyPluginMgr.findPlugin(plugin.getId())) != null){
						this.audioConsumer = (MyProxyAudioConsumer)myProxyPlugin;
					}
				}
				if((plugin = this.mediaSessionMgr.findProxyPluginProducer(twrap_media_type_t.twrap_media_audio)) != null){
					if((myProxyPlugin = MyProxyPluginMgr.findPlugin(plugin.getId())) != null){
						this.audioProducer = (MyProxyAudioProducer)myProxyPlugin;
					}
				}
			}
			
			
			this.isConsumersAndProducersInitialzed = true;
			return true;
		}
		
		return false;	
	}
	
	private void deInitializeMediaSession(){
		if(this.mediaSessionMgr != null){
			this.mediaSessionMgr.delete();
			this.mediaSessionMgr = null;
		}
	}
	
	public Context getContext(){
		return this.context;
	}
	
	public void setContext(Context context){
		this.context = context;
	}
	
	public final View startVideoConsumerPreview(){
		if(this.videoConsumer != null){
			return this.videoConsumer.startPreview();
		}
		return null;
	}
	
	public final View startVideoProducerPreview(){
		if(this.videoProducer != null){
			return this.videoProducer.startPreview();
		}
		return null;
	}
	
	public void pushBlankPacket(){
		if(this.videoProducer != null){
			this.videoProducer.pushBlankPacket();
		}
	}
	
	public void toggleCamera(){
		if(this.videoProducer != null){
			this.videoProducer.toggleCamera();
		}
	}
	
	public void setRotation(int rot){
		if(this.videoProducer != null){
			this.videoProducer.setRotation(rot);
		}
	}
	
	public MediaType getMediaType(){
		return this.mediaType;
	}
	
	public CallState getState(){
		return this.state;
	}
	
	public void setState(CallState state){
		if(this.state == state){
			return;
		}
		
		this.state = state;
		switch(state){
			case CALL_INCOMING:
				this.historyEvent.setStatus(StatusType.Incoming);
				this.initializeConsumersAndProducers();
				break;
			case CALL_INPROGRESS:
				this.historyEvent.setStatus(StatusType.Outgoing);
				this.initializeConsumersAndProducers();
				break;
			case INCALL:
			{
				this.setConnected(true);
				this.historyEvent.setStartTime(new Date().getTime());
				
				this.initializeConsumersAndProducers();
				break;
			}
			case CALL_TERMINATED:
				this.setConnected(false);
				if (this.historyEvent.getStartTime() == this.historyEvent.getEndTime()) {
					if (this.historyEvent.getStatus() == StatusType.Incoming) {
						this.historyEvent.setStatus(StatusType.Missed);
					}
				} else {
					this.historyEvent.setEndTime(new Date().getTime());
				}
				ServiceManager.getHistoryService().addEvent(this.historyEvent);
				
				this.deInitializeMediaSession();
				break;
		}
		
		super.setChangedAndNotifyObservers(this);
	}
	
	public long getStartTime(){
		return this.historyEvent.getStartTime();
	}
	
	public boolean  isSendingVideo(){
		return this.sendingVideo;
	}
	
	public void  setSendingVideo(boolean sendingVideo){
		this.sendingVideo = sendingVideo;
	}
	
	public String getRemoteParty(){
		return this.remoteParty;
	}
	
	public void setRemoteParty(String remoteParty){
		this.remoteParty = remoteParty;
		this.historyEvent.setRemoteParty(remoteParty);
	}
	
	public boolean acceptCall(){		
		return this.session.accept();
	}
	
	public boolean hangUp(){
		if(this.connected){
			return this.session.hangup();
		}
		else{
			return this.session.reject();
		}
	}
	
	public boolean holdCall(){
		return this.session.hold();
	}
	
	public boolean resumeCall(){		
		return this.session.resume();
	}
	
	public boolean isLocalHeld(){
		return this.localHold;
	}
	
	public void setLocalHold(boolean localHold){
		boolean changed = this.localHold != localHold;
		this.localHold = localHold;
		
		if(this.videoProducer != null){
			this.videoProducer.setOnPause(this.localHold || this.remoteHold);
		}
		if(this.audioProducer != null){
			this.audioProducer.setOnPause(this.localHold || this.remoteHold);
		}
		
		if(changed){
			super.setChangedAndNotifyObservers(this);
		}
	}
	
	public boolean isRemoteHeld(){
		return this.remoteHold;
	}
	
	public void setRemoteHold(boolean remoteHold){
		boolean changed = this.remoteHold != remoteHold;
		this.remoteHold = remoteHold;
		
		if(this.videoProducer != null){
			this.videoProducer.setOnPause(this.localHold || this.remoteHold);
		}
		if(this.audioProducer != null){
			this.audioProducer.setOnPause(this.localHold || this.remoteHold);
		}
		
		if(changed){
			super.setChangedAndNotifyObservers(this);
		}
	}
	
	public boolean makeAudioCall(String remoteUri){
		boolean ret;
		String level = ServiceManager.getConfigurationService().getString(
				CONFIGURATION_SECTION.QOS,
				CONFIGURATION_ENTRY.PRECOND_BANDWIDTH,
				Configuration.DEFAULT_QOS_PRECOND_BANDWIDTH);
		tmedia_bandwidth_level_t bl = Configuration.getBandwidthLevel(level);
		
		this.setRemoteParty(remoteUri);
		ActionConfig config = new ActionConfig();
		config.setMediaInt(twrap_media_type_t.twrap_media_audio, "bandwidth-level", bl.swigValue());
		ret = this.session.callAudio(remoteUri, config);
		config.delete();
		
		return ret;
	}
	
	public boolean makeVideoCall(String remoteUri){
		// FIXME: add Video special features
		boolean ret;
		String level = ServiceManager.getConfigurationService().getString(
				CONFIGURATION_SECTION.QOS,
				CONFIGURATION_ENTRY.PRECOND_BANDWIDTH,
				Configuration.DEFAULT_QOS_PRECOND_BANDWIDTH);
		tmedia_bandwidth_level_t bl = Configuration.getBandwidthLevel(level);
		
		this.setRemoteParty(remoteUri);
		ActionConfig config = new ActionConfig();
		config.setMediaInt(twrap_media_type_t.twrap_media_audiovideo, "bandwidth-level", bl.swigValue());
		ret = this.session.callAudioVideo(remoteUri, config);
		config.delete();
		
		return ret;
	}
	
	public boolean sendDTMF(int number){		
		return this.session.sendDTMF(number);
	}

	@Override
	protected SipSession getSession() {
		return this.session;
	}
}
