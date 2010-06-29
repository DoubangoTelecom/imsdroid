package org.doubango.imsdroid.sip;

import java.util.HashMap;

import org.doubango.imsdroid.media.AudioConsumer;
import org.doubango.imsdroid.media.AudioProducer;
import org.doubango.imsdroid.media.MediaType;
import org.doubango.imsdroid.media.VideoConsumer;
import org.doubango.imsdroid.media.VideoProducer;
import org.doubango.tinyWRAP.CallSession;
import org.doubango.tinyWRAP.SipSession;

public class MyAVSession  extends MySipSession{

	private static HashMap<Long, MyAVSession> sessions;
	
	private final CallSession session;
	//private final AudioConsumer audioConsumer;
	//private final AudioProducer audioProducer;
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
		
		//this.audioConsumer = new AudioConsumer();
		//this.audioProducer = new AudioProducer();
		this.mediaType = mediaType;
		
		//this.audioConsumer.setActive();
		//this.audioProducer.setActive();
		
		// commons
		this.init();
		
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
		//this.audioConsumer.setActive();
		//this.audioProducer.setActive();
		
		return this.session.Accept();
	}
	
	public boolean rejectCall(){
		// Disable
		return this.session.Hangup();
	}
	
	public boolean hangUp(){
		// Disable
		return this.session.Hangup();
	}
	
	public boolean holdCall(){
		// Disable
		return this.session.Hold();
	}
	
	public boolean resumeCall(){
		//this.audioConsumer.setActive();
		//this.audioProducer.setActive();
		
		return this.session.Resume();
	}
	
	public boolean makeAudioCall(String remoteUri){
		//this.audioConsumer.setActive();
		//this.audioProducer.setActive();
		
		return this.session.CallAudio(remoteUri);
	}
	
	public boolean makeVideoCall(String remoteUri){
		//this.audioConsumer.setActive();
		//this.audioProducer.setActive();
		
		return this.session.CallAudioVideo(remoteUri);
	}

	@Override
	protected SipSession getSession() {
		return this.session;
	}
}
