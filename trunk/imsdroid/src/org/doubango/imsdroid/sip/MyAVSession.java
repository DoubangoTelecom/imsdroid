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

import java.util.HashMap;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.media.AudioConsumer;
import org.doubango.imsdroid.media.AudioProducer;
import org.doubango.imsdroid.media.MediaType;
import org.doubango.imsdroid.media.VideoConsumer;
import org.doubango.imsdroid.media.VideoProducer;
import org.doubango.tinyWRAP.CallSession;
import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.tmedia_qos_strength_t;
import org.doubango.tinyWRAP.tmedia_qos_stype_t;

public class MyAVSession  extends MySipSession{

	private static HashMap<Long, MyAVSession> sessions;
	
	private final CallSession session;

	private final MediaType mediaType;
	private boolean connected;
	
	private static AudioConsumer __audioConsumer;
	private static AudioProducer __audioProducer;
	private static VideoProducer __videoProducer;
	private static VideoConsumer __videoConsumer;
	
	static {
		MyAVSession.sessions = new HashMap<Long, MyAVSession>();
		
		__audioConsumer = new AudioConsumer();
		__audioProducer = new AudioProducer();
		__videoProducer = new VideoProducer();
		__videoConsumer = new VideoConsumer();
		
		__audioConsumer.setActive();
		__audioProducer.setActive();
		__videoProducer.setActive();
		__videoConsumer.setActive();
	}
	
	public static VideoProducer getVideoProducer(){
		return MyAVSession.__videoProducer;
	}
	
	public static VideoConsumer getVideoConsumer(){
		return MyAVSession.__videoConsumer;
	}
	
	public static MyAVSession takeIncomingSession(MySipStack sipStack, CallSession session){
		MyAVSession avSession = new MyAVSession(sipStack, session, MediaType.AudioVideo);
		MyAVSession.sessions.put(avSession.getId(), avSession);
		return avSession;
	}
	
	public static MyAVSession createOutgoingSession(MySipStack sipStack, MediaType mediaType){
		MyAVSession avSession = new MyAVSession(sipStack, null, mediaType);
		MyAVSession.sessions.put(avSession.getId(), avSession);
		
		return avSession;
	}
	
	public static MyAVSession getSession(long id){
		return MyAVSession.sessions.get(id);
	}
	
	public static void releaseSession(MyAVSession session){
		if(session != null){
			MyAVSession.sessions.remove(session.getId());
		}
	}
	
	public MyAVSession(MySipStack sipStack, CallSession session, MediaType mediaType) {
		super();
		
		this.session = (session == null) ? new CallSession(sipStack) : session;
		this.mediaType = mediaType;
		
		final IConfigurationService configurationService = ServiceManager.getConfigurationService();
		
		// commons
		this.init();
		
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
		this.session.addHeader("Supported", "precondition");
		
		/* 3GPP TS 24.173
		*
		* 5.1 IMS communication service identifier
		* URN used to define the ICSI for the IMS Multimedia Telephony Communication Service: urn:urn-7:3gpp-service.ims.icsi.mmtel. 
		* The URN is registered at http://www.3gpp.com/Uniform-Resource-Name-URN-list.html.
		* Summary of the URN: This URN indicates that the device supports the IMS Multimedia Telephony Communication Service.
		*
		*/		
		this.session.addHeader("P-Preferred-Service", "urn:urn-7:3gpp-service.ims.icsi.mmtel");
	}
	
	public void setConnected(boolean connected){
		this.connected = connected;
		if(this.connected){
			
		}
	}
	
	public MediaType getMediaType(){
		return this.mediaType;
	}
	
	public boolean acceptCall(){		
		return this.session.accept();
	}
	
	public boolean rejectCall(){
		return this.session.hangup();
	}
	
	public boolean hangUp(){
		return this.session.hangup();
	}
	
	public boolean holdCall(){
		return this.session.hold();
	}
	
	public boolean resumeCall(){		
		return this.session.resume();
	}
	
	public boolean makeAudioCall(String remoteUri){		
		return this.session.callAudio(remoteUri);
	}
	
	public boolean makeVideoCall(String remoteUri){		
		return this.session.callAudioVideo(remoteUri);
	}
	
	public boolean sendDTMF(int number){		
		return this.session.sendDTMF(number);
	}

	@Override
	protected SipSession getSession() {
		return this.session;
	}
}
