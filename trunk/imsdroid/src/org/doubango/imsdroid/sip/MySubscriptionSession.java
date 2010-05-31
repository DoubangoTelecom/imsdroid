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
		super();
		
		this.session = new SubscriptionSession(sipStack);
		this.eventPackage = eventPackage;
		
		// commons
		this.init();
		
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
						String.format("%s, %s, %s, %s, %s, %s", 
								ContentType.MULTIPART_RELATED, 
								ContentType.RLMI, 
								ContentType.PIDF, 
								ContentType.RPID, 
								ContentType.XCAP_DIFF, 
								ContentType.EXTERNAL_BODY));
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
