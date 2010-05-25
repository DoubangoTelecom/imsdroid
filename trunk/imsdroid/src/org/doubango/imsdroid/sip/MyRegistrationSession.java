package org.doubango.imsdroid.sip;

import org.doubango.tinyWRAP.RegistrationSession;
import org.doubango.tinyWRAP.SipSession;

public class MyRegistrationSession extends MySipSession{

	private final RegistrationSession session;
	public MyRegistrationSession(MySipStack sipStack) {
		super();
		
		this.session = new RegistrationSession(sipStack);
		
		// commons
		this.init();
		
		// support for 3GPP SMS over IP
		this.session.addCaps("+g.3gpp.smsip");
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
