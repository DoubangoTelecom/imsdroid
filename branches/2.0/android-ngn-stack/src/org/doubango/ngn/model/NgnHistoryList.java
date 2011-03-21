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
