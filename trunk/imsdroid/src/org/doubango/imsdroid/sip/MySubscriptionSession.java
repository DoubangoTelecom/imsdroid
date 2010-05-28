package org.doubango.imsdroid.sip;

import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.SubscriptionSession;

public class MySubscriptionSession extends MySipSession{
	
	private final SubscriptionSession session;
	private final EVENT_PACKAGE_TYPE eventPackage;
	
	public static enum EVENT_PACKAGE_TYPE {
		CONFERENCE, DIALOG, MESSAGE_SUMMARY, PRESENCE, REG, SIP_PROFILE, UA_PROFILE, WINFO, XCAP_DIFF
	}
	
	public MySubscriptionSession(MySipStack sipStack, String toUri, EVENT_PACKAGE_TYPE eventPackage) {
		super();
		
		this.session = new SubscriptionSession(sipStack);
		this.eventPackage = eventPackage;
		
		// commons
		this.init();
		
		switch(eventPackage){
			case CONFERENCE:
				this.session.addHeader("Event", "conference");
				this.session.addHeader("Accept", "application/conference-info+xml");
				break;
			case DIALOG:
				this.session.addHeader("Event", "dialog");
				this.session.addHeader("Accept", "application/dialog-info+xml");
				break;
			case MESSAGE_SUMMARY:
				this.session.addHeader("Event", "message-summary");
				this.session.addHeader("Accept", "application/simple-message-summary");
				break;
			case PRESENCE:
				this.session.addHeader("Event", "presence");
				this.session.addHeader("Accept", "multipart/related, application/rlmi+xml, application/pidf+xml, application/rpid+xml, application/xcap-diff+xml, message/external-body");
				break;
			case REG:
				this.session.addHeader("Event", "reg");
				this.session.addHeader("Accept", "application/reginfo+xml");
				break;
			case SIP_PROFILE:
				this.session.addHeader("Event", "sip-profile");
				this.session.addHeader("Accept", "application/vnd.oma.im.deferred-list+xml");
				break;
			case UA_PROFILE:
				this.session.addHeader("Event", "ua-profile");
				this.session.addHeader("Accept", "application/xcap-diff+xml");
				break;
			case WINFO:
				this.session.addHeader("Event", "presence.winfo");
				this.session.addHeader("Accept", "application/watcherinfo+xml");
				break;
			case XCAP_DIFF:
				this.session.addHeader("Event", "xcap-diff");
				this.session.addHeader("Accept", "application/xcap-diff+xml");
				break;
		}
		
		this.session.setToUri(toUri);
		this.session.addHeader("Allow-Events", "refer, presence, presence.winfo, xcap-diff, conference");
	}
	
	public boolean subscribe(){
		return this.session.Subscribe();
	}
	
	public boolean unsubscribe(){
		return this.session.UnSubscribe();
	}
	
	public EVENT_PACKAGE_TYPE getEventPackage(){
		return this.eventPackage;
		
	}
	
	protected SipSession getSession() {
		return this.session;
	}
}
