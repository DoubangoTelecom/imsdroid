package org.doubango.ngn.services;

import java.util.List;

import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.utils.NgnObservableList;
import org.doubango.ngn.utils.NgnPredicate;

public interface INgnHistoryService extends INgnBaseService{
	boolean load();
	boolean isLoading();
	
	void addEvent(NgnHistoryEvent event);
	
    void updateEvent(NgnHistoryEvent event);
    
    void deleteEvent(NgnHistoryEvent event);
    void deleteEvent(final int location);
    void deleteEvents(NgnPredicate<NgnHistoryEvent> predicate);
    
    void clear();
    
    NgnObservableList<NgnHistoryEvent> getObservableEvents();
    List<NgnHistoryEvent> getEvents();
}
