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

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "events")
public class HistoryList {
	@ElementList(name="event", required = false, inline=true)
    private List<HistoryEvent> events;
	
	public List<HistoryEvent> getList(){
		return this.events;
	}
	
	public void addEvent(HistoryEvent e){
		if(this.events == null){
			this.events = new ArrayList<HistoryEvent>();
		}
		this.events.add(e);
	}
	
	public void removeEvent(HistoryEvent e){
		if(this.events != null){
			this.events.remove(e);
		}
	}
	
	public void removeEvent(int location){
		if(this.events != null){
			this.events.remove(location);
		}
	}
	
	public void clear(){
		if(this.events != null){
			this.events.clear();
		}
	}
}
