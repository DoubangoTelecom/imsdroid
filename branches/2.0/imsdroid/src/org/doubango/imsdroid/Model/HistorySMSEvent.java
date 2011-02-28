package org.doubango.imsdroid.Model;

import org.doubango.imsdroid.Media.MediaType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class HistorySMSEvent extends HistoryEvent{
	
	@Element(data=true, required=false)
	protected String mContent;
	
	HistorySMSEvent(){
		this(null, StatusType.Failed);
	}
	
	public HistorySMSEvent(String remoteParty, StatusType status) {
		super(MediaType.SMS, remoteParty);
		super.setStatus(status);
	}
	
	public void setContent(String content){
		this.mContent = content;
	}
	
	public String getContent(){
		return this.mContent;
	}
}
