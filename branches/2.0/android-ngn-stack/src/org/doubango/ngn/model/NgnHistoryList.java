/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
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
*/
package org.doubango.ngn.model;

import java.util.Collection;
import java.util.List;

import org.doubango.ngn.utils.NgnObservableList;
import org.doubango.ngn.utils.NgnPredicate;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "events")
public class NgnHistoryList {
    private final NgnObservableList<NgnHistoryEvent> mEvents;
    
    @SuppressWarnings("unused")
	@ElementList(name="event", required=false, inline=true)
	private List<NgnHistoryEvent> mSerializableEvents;
	
    public NgnHistoryList(){
    	mEvents = new NgnObservableList<NgnHistoryEvent>(true);
    	mSerializableEvents = mEvents.getList();
    }
    
	public NgnObservableList<NgnHistoryEvent> getList(){
		return mEvents;
	}
	
	public void addEvent(NgnHistoryEvent e){
		mEvents.add(0, e);
	}
	
	public void removeEvent(NgnHistoryEvent e){
		if(mEvents != null){
			mEvents.remove(e);
		}
	}
	
	public void removeEvents(Collection<NgnHistoryEvent> events){
		if(mEvents != null){
			mEvents.removeAll(events);
		}
	}
	
	public void removeEvents(NgnPredicate<NgnHistoryEvent> predicate){
		if(mEvents != null){
			final List<NgnHistoryEvent> eventsToRemove = mEvents.filter(predicate);
			mEvents.removeAll(eventsToRemove);
		}
	}
	
	public void removeEvent(int location){
		if(mEvents != null){
			mEvents.remove(location);
		}
	}
	
	public void clear(){
		if(mEvents != null){
			mEvents.clear();
		}
	}
	
	
}
