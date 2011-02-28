package org.doubango.imsdroid.Model;

import java.util.Date;

import org.doubango.imsdroid.Media.MediaType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public abstract class HistoryEvent  implements Comparable<HistoryEvent> {
	
	public enum StatusType{
		Outgoing,
		Incoming,
		Missed,
		Failed
	}
	
	// For performance reasons, do not use Date() class
	
	@Element(name = "type", required = true)
	protected MediaType mMediaType;
	@Element(name = "start", required = true)
	protected long mStartTime;
	@Element(name = "end", required = true)
	protected long mEndTime;
	@Element(name = "remote", required = true)
	protected String mRemoteParty;
	@Element(name = "seen", required = true)
	protected boolean mSeen;
	@Element(name = "status", required = true)
	protected StatusType mStatus;
	
	
	protected HistoryEvent(MediaType mediaType, String remoteParty){
		mMediaType = mediaType;
		mStartTime = new Date().getTime();
		mEndTime = mStartTime;
		mRemoteParty = remoteParty;
		mStatus = StatusType.Missed;
	}
	
	public void setStartTime(long time){
		mStartTime = time;
	}
	
	public long getStartTime(){
		return mStartTime;
	}
	
	public long getEndTime(){
		return mEndTime;
	}
	
	public void setEndTime(long time){
		mEndTime = time;
	}
	
	public MediaType getMediaType(){
		return mMediaType;
	}
	
	public String getRemoteParty(){
		return mRemoteParty;
	}
	
	public void setRemoteParty(String remoteParty){
		mRemoteParty = remoteParty;
	}
	
	public boolean isSeen(){
		return mSeen;
	}
	
	public void setSeen(boolean seen){
		mSeen = seen;
	}
	
	public StatusType getStatus(){
		return mStatus;
	}
	
	public void setStatus(StatusType status){
		mStatus = status;
	}
	
	@Override
	public int compareTo(HistoryEvent another) {
		return (int)(mStartTime - another.mStartTime);
	}
}
