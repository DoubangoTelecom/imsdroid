package org.doubango.ngn.services;

import java.util.List;

import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.utils.NgnObservableList;
import org.doubango.ngn.utils.NgnPredicate;


/**@page NgnHistoryService_page History Service
 * This service is used to store/retrieve history event (audio/video, messaging, ...). You should never create or start this service by yourself. <br />
 * An instance of this service could be retrieved like this:
 * @code
 * final INgnHistoryService mHistoryService = NgnEngine.getInstance().getHistoryService();
 * @endcode
 * 
 */
public interface INgnHistoryService extends INgnBaseService{
	boolean load();
	/**
	 * Checks whether the service is loading the entries
	 * @return true if the entries are being loaded and false otherwise
	 */
	boolean isLoading();
	/**
	 * Adds new event into the history. The event will be put in front of the list.
	 * @param event the event to put into the list of events
	 */
	void addEvent(NgnHistoryEvent event);
	/**
	 * Updates and event and commit the changes.
	 * @param event the event to update
	 */
    void updateEvent(NgnHistoryEvent event);
    /**
     * Deletes an event from the history list
     * @param event the event to delete
     */
    void deleteEvent(NgnHistoryEvent event);
    /**
     * Deletes an event from the history list
     * @param location the location (zero-based index) of the event to remove from the history list
     */
    void deleteEvent(final int location);
    /**
     * Deletes events matching the given criteria from the history list
     * @param predicate the predicate function used to check if an event should be deleted or not
     * @code
     * // Delete all "File Transfer" events stored in the history list
     * final INgnHistoryService historyService = NgnEngine.getInstance().getHistoryService();
     * historyService.deleteEvents(new NgnPredicate<NgnHistoryEvent>() {
			@Override
			public boolean apply(NgnHistoryEvent event) {
				// TODO Auto-generated method stub
				return event.getMediaType() == NgnMediaType.FileTransfer;
			}
		});
     * @endcode
     */
    void deleteEvents(NgnPredicate<NgnHistoryEvent> predicate);
    /**
     * Removes all events from the history list
     */
    void clear();
    /**
     * Gets the list of all stored events
     * @return an observable collection containing all the events
     * @sa @ref getEvents()
     */
    NgnObservableList<NgnHistoryEvent> getObservableEvents();
    /**
     * Gets the list of all stored events
     * @return a collection containing all the events
     * @sa @ref getObservableEvents()
     */
    List<NgnHistoryEvent> getEvents();
}
