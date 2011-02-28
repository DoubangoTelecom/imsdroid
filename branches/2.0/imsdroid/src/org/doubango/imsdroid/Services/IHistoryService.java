
package org.doubango.imsdroid.Services;

import java.util.List;

import org.doubango.imsdroid.Model.HistoryEvent;
import org.doubango.imsdroid.Utils.ObservableList;


public interface IHistoryService extends IBaseService{
	boolean load();
	boolean isLoading();
	
	void addEvent(HistoryEvent event);
	
    void updateEvent(HistoryEvent event);
    
    void deleteEvent(HistoryEvent event);
    void deleteEvent(final int location);
    
    void clear();
    
    ObservableList<HistoryEvent> getObservableEvents();
    List<HistoryEvent> getEvents();
}
