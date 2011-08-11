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

import org.doubango.imsdroid.utils.ContentType;
import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.SubscriptionSession;

public class MySubscriptionSession extends MySipSession{
	
	private final SubscriptionSession session;
	private final EVENT_PACKAGE_TYPE eventPackage;
	
	public static enum EVENT_PACKAGE_TYPE {
		CONFERENCE, DIALOG, MESSAGE_SUMMARY, PRESENCE, REG, SIP_PROFILE, UA_PROFILE, WINFO, XCAP_DIFF
	}
	
	public MySubscriptionSession(MySipStack sipStack, String toUri, EVENT_PACKAGE_TYPE eventPackage) {
		super(sipStack);
		
		this.session = new SubscriptionSession(sipStack);
		this.eventPackage = eventPackage;
		
		// commons
		this.init();
		
		// SigComp
		this.setSigCompId(sipStack.getSigCompId());
		
		switch(eventPackage){
			case CONFERENCE:
				this.session.addHeader("Event", "conference");
				this.session.addHeader("Accept", ContentType.CONFERENCE_INFO);
				break;
			case DIALOG:
				this.session.addHeader("Event", "dialog");
				this.session.addHeader("Accept", ContentType.DIALOG_INFO);
				break;
			case MESSAGE_SUMMARY:
				this.session.addHeader("Event", "message-summary");
				this.session.addHeader("Accept", ContentType.MESSAGE_SUMMARY);
				break;
			case PRESENCE:
				this.session.addHeader("Event", "presence");
				this.session.addHeader("Accept", 
						String.format("%s", 
								ContentType.PIDF));
//				this.session.addHeader("Accept", 
//						String.format("%s, %s, %s, %s, %s, %s", 
//								ContentType.MULTIPART_RELATED, 
//								ContentType.RLMI, 
//								ContentType.PIDF, 
//								ContentType.RPID, 
//								ContentType.XCAP_DIFF, 
//								ContentType.EXTERNAL_BODY));
				break;
			case REG:
				this.session.addHeader("Event", "reg");
				this.session.addHeader("Accept", ContentType.REG_INFO);
				// 3GPP TS 24.229 5.1.1.6 User-initiated deregistration
				this.session.setSilentHangup(true);
				break;
			case SIP_PROFILE:
				this.session.addHeader("Event", "sip-profile");
				this.session.addHeader("Accept", ContentType.OMA_DEFERRED_LIST);
				break;
			case UA_PROFILE:
				this.session.addHeader("Event", "ua-profile");
				this.session.addHeader("Accept", ContentType.XCAP_DIFF);
				break;
			case WINFO:
				this.session.addHeader("Event", "presence.winfo");
				this.session.addHeader("Accept", ContentType.WATCHER_INFO);
				break;
			case XCAP_DIFF:
				this.session.addHeader("Event", "xcap-diff");
				this.session.addHeader("Accept", ContentType.XCAP_DIFF);
				break;
		}
		
		this.session.setToUri(toUri);
		// common to all subscription sessions
		this.session.addHeader("Allow-Events", "refer, presence, presence.winfo, xcap-diff, conference");
	}
	
	public boolean subscribe(){
		return this.session.subscribe();
	}
	
	public boolean unsubscribe(){
		return this.session.unSubscribe();
	}
	
	public EVENT_PACKAGE_TYPE getEventPackage(){
		return this.eventPackage;
		
	}
	
	protected SipSession getSession() {
		return this.session;
	}
}
