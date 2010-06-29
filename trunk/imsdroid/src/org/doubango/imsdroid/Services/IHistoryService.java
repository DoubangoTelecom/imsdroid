package org.doubango.imsdroid.Services;

import java.util.List;

import org.doubango.imsdroid.Model.HistoryEvent;
import org.doubango.imsdroid.events.IHistoryEventDispatcher;

public interface IHistoryService  extends IService, IHistoryEventDispatcher{
	
	boolean isLoadingHistory();
	
	void addEvent(HistoryEvent event);
	
    void updateEvent(HistoryEvent event);
    
    void deleteEvent(HistoryEvent event);
    void deleteEvent(final int location);
    
    void clear();
    
    List<HistoryEvent> getEvents();
}
