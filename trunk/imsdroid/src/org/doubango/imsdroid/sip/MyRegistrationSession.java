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
		
		/* 3GPP TS 24.173
		*
		* 5.1 IMS communication service identifier
		* URN used to define the ICSI for the IMS Multimedia Telephony Communication Service: urn:urn-7:3gpp-service.ims.icsi.mmtel. 
		* The URN is registered at http://www.3gpp.com/Uniform-Resource-Name-URN-list.html.
		* Summary of the URN: This URN indicates that the device supports the IMS Multimedia Telephony Communication Service.
		*
		* 5.2 Session control procedures
		* The multimedia telephony participant shall include the g.3gpp. icsi-ref feature tag equal to the ICSI value defined 
		* in subclause 5.1 in the Contact header field in initial requests and responses as described in 3GPP TS 24.229 [13].
		*/
		/* GSMA RCS phase 3 - 3.2 Registration */
		this.session.addCaps("audio");
		this.session.addCaps("+g.3gpp.icsi-ref", "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"");
		this.session.addCaps("+g.3gpp.icsi-ref", "\"urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-vs\"");
		// In addition, in RCS Release 3 the BA Client when used as a primary device will indicate the capability to receive SMS 
		// messages over IMS by registering the SMS over IP feature tag in accordance with [24.341]:
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
