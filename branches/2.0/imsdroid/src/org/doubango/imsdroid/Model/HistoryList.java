package org.doubango.imsdroid.Model;

import java.util.List;

import org.doubango.imsdroid.Utils.ObservableList;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "events")
public class HistoryList {
    private final ObservableList<HistoryEvent> mEvents;
    
    @SuppressWarnings("unused")
	@ElementList(name="event", required=false, inline=true)
	private List<HistoryEvent> mSerializableEvents;
	
    public HistoryList(){
    	this.mEvents = new ObservableList<HistoryEvent>(true);
    	mSerializableEvents = mEvents.getList();
    }
    
	public ObservableList<HistoryEvent> getList(){
		return this.mEvents;
	}
	
	public void addEvent(HistoryEvent e){
		this.mEvents.add(0, e);
	}
	
	public void removeEvent(HistoryEvent e){
		if(this.mEvents != null){
			this.mEvents.remove(e);
		}
	}
	
	public void removeEvent(int location){
		if(this.mEvents != null){
			this.mEvents.remove(location);
		}
	}
	
	public void clear(){
		if(this.mEvents != null){
			this.mEvents.clear();
		}
	}
}
