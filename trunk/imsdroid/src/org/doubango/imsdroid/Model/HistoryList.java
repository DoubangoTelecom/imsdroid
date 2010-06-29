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
