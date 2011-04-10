package org.doubango.ngn.model;

import java.util.ArrayList;
import java.util.List;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.utils.NgnPredicate;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class NgnHistorySMSEvent extends NgnHistoryEvent{
	
	@Element(data=true, required=false)
	protected String mContent;
	
	NgnHistorySMSEvent(){
		this(null, StatusType.Failed);
	}
	
	public NgnHistorySMSEvent(String remoteParty, StatusType status) {
		super(NgnMediaType.SMS, remoteParty);
		super.setStatus(status);
	}
	
	public void setContent(String content){
		this.mContent = content;
	}
	
	public String getContent(){
		return this.mContent;
	}
	
	public static class HistoryEventSMSIntelligentFilter implements NgnPredicate<NgnHistoryEvent>{
		private final List<String> mRemoteParties = new ArrayList<String>();
		
		protected void reset(){
			mRemoteParties.clear();
		}
		
		@Override
		public boolean apply(NgnHistoryEvent event) {
			if (event != null && (event.getMediaType() == NgnMediaType.SMS || event.getMediaType() == NgnMediaType.Chat)){
				if(!mRemoteParties.contains(event.getRemoteParty())){
					mRemoteParties.add(event.getRemoteParty());
					return true;
				}
			}
			return false;
		}
	}
	
	public static class HistoryEventSMSFilter implements NgnPredicate<NgnHistoryEvent>{
		@Override
		public boolean apply(NgnHistoryEvent event) {
			return (event != null && (event.getMediaType() == NgnMediaType.SMS || event.getMediaType() == NgnMediaType.Chat));
		}
	}
}
