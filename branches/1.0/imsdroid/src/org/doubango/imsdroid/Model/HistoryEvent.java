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

package org.doubango.imsdroid.Model;

import java.util.Date;

import org.doubango.imsdroid.media.MediaType;
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
	protected MediaType mediaType;
	@Element(name = "start", required = true)
	protected long startTime;
	@Element(name = "end", required = true)
	protected long endTime;
	@Element(name = "remote", required = true)
	protected String remoteParty;
	@Element(name = "seen", required = true)
	protected boolean seen;
	@Element(name = "status", required = true)
	protected StatusType status;
	
	
	protected HistoryEvent(MediaType mediaType, String remoteParty){
		this.mediaType = mediaType;
		this.startTime = new Date().getTime();
		this.endTime = this.startTime;
		this.remoteParty = remoteParty;
		this.status = StatusType.Missed;
	}
	
	public void setStartTime(long time){
		this.startTime = time;
	}
	
	public long getStartTime(){
		return this.startTime;
	}
	
	public long getEndTime(){
		return this.endTime;
	}
	
	public void setEndTime(long time){
		this.endTime = time;
	}
	
	public MediaType getMediaType(){
		return this.mediaType;
	}
	
	public String getRemoteParty(){
		return this.remoteParty;
	}
	
	public void setRemoteParty(String remoteParty){
		this.remoteParty = remoteParty;
	}
	
	public boolean isSeen(){
		return this.seen;
	}
	
	public void setSeen(boolean seen){
		this.seen = seen;
	}
	
	public StatusType getStatus(){
		return this.status;
	}
	
	public void setStatus(StatusType status){
		this.status = status;
	}
	
	@Override
	public int compareTo(HistoryEvent another) {
		return (int)(startTime - another.startTime);
	}
}
