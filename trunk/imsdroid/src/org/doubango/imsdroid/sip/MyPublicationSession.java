package org.doubango.imsdroid.sip;

import org.doubango.imsdroid.utils.ContentType;
import org.doubango.tinyWRAP.PublicationSession;
import org.doubango.tinyWRAP.SipSession;

public class MyPublicationSession  extends MySipSession{
	
	private final PublicationSession session;
	
	// impu, basic, activity, note, basic, 
	private final static String PUBLISH_PAYLOAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
			"<presence xmlns:caps=\"urn:ietf:params:xml:ns:pidf:caps\" xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\" xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\" xmlns:op=\"urn:oma:xml:prs:pidf:oma-pres\" entity=\"%s\" xmlns=\"urn:ietf:params:xml:ns:pidf\">" +
			  "<pdm:person id=\"FPNZFGON\">" +
			    "<op:overriding-willingness>" +
			      "<op:basic>%s</op:basic>" +
			    "</op:overriding-willingness>" +
			    "<rpid:activities>" +
			      "<rpid:%s />" +
			    "</rpid:activities>" +
			    "<pdm:note>%s</pdm:note>" +
			  "</pdm:person>" +
			  "<pdm:device id=\"d1983\">" +
			    "<status>" +
			      "<basic>%s</basic>" +
			    "</status>" +
			    "<caps:devcaps>" +
			      "<caps:mobility>" +
			        "<caps:supported>" +
			         "<caps:mobile />" +
			        "</caps:supported>" +
			      "</caps:mobility>" +
			    "</caps:devcaps>" +
			    "<op:network-availability>" +
			      "<op:network id=\"IMS\">" +
			        "<op:active />" +
			      "</op:network>" +
			    "</op:network-availability>" +
			  "</pdm:device>" +
			"</presence>";
	
	public MyPublicationSession(MySipStack sipStack, String toUri) {
		super();
		
		this.session = new PublicationSession(sipStack);
		
		// commons
		this.init();
		
		this.session.addHeader("Event", "presence");
		this.session.addHeader("Content-Type", ContentType.PIDF);
		
		this.session.setToUri(toUri);
	}
	
	protected SipSession getSession() {
		return this.session;
	}
	
	public boolean publish(PresenceStatus status, String note){
		String basic = "open";
		String activity = "unknown";
		switch(status)
		{
			case Online:
				break;
			case Busy:
				activity = "busy";
				break;
			case Away:
				activity = "away";
				break;
			case BeRightBack:
				activity = "vacation";
				break;
			case OnThePhone:
				activity = "onthephone";
				break;
			case Offline:
				basic = "close";
				break;
			case HyperAvail:
				break;
		}
		
		String payload = String.format(MyPublicationSession.PUBLISH_PAYLOAD, 
				this.getFromUri(), basic, activity, note, basic);
		return this.session.Publish(payload.getBytes());
	}
	
	public boolean unpublish(){
		return this.session.UnPublish();
	}
}
