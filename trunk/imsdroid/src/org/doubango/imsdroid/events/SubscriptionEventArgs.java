package org.doubango.imsdroid.events;

public class SubscriptionEventArgs  extends EventArgs{

	private final SubscriptionEventTypes type;
	private final short sipCode;
	private final String phrase;
	private final byte[] content;
	private final String contentType;
	
	public SubscriptionEventArgs(SubscriptionEventTypes type, short sipCode, String phrase, byte[] content, String contentType){
		super();
		
		this.type = type;
		this.sipCode = sipCode;
		this.phrase = phrase;
		this.content = content;
		this.contentType = contentType;
	}
	
	public SubscriptionEventTypes getType(){
		return this.type;
	}
	
	public short getSipCode(){
		return this.sipCode;
	}
	
	public String getPhrase(){
		return this.phrase;
	}
	
	public byte[] getContent(){
		return this.content;
	}
	
	public String getContentType(){
		return this.contentType;
	}
}
