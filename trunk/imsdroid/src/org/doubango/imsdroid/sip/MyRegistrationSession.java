package org.doubango.imsdroid.sip;

import org.doubango.tinyWRAP.RegistrationSession;
import org.doubango.tinyWRAP.SipSession;

public class MyRegistrationSession extends MySipSession {

	private final RegistrationSession session;
	
	public MyRegistrationSession(MySipStack sipStack) {
		super();
		
		this.session = new RegistrationSession(sipStack);
		
		// commons
		this.init();
		
		/* support for 3GPP SMS over IP */
		this.session.addCaps("+g.3gpp.smsip");
		
		/* support for OMA Large message (as per OMA SIMPLE IM v1) */
		this.session.addCaps("+g.oma.sip-im.large-message");
		
		/* GSMA RCS phase 2 - 3.2 Registration */
		this.session.addCaps("audio");
		this.session.addCaps("+g.3gpp.icsi-ref", "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"");
		//Note: RCS Release 2 Broadband Access clients shall register the +g.3gpp.cs-voice feature 
		// tag in order to provide good content sharing interoperability to mobile clients.
		this.session.addCaps("+g.3gpp.cs-voice");
	}
	
	protected SipSession getSession() {
		return this.session;
	}
	
	public boolean register(){
		return this.session.Register();
	}
	
	public boolean unregister(){
		return this.session.UnRegister();
	}
}
