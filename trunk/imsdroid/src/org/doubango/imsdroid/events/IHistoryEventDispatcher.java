package org.doubango.imsdroid.events;

public interface IHistoryEventDispatcher /*extends IEventDispatcher<IHistoryEventHandler>*/{
	boolean addHistoryEventHandler(IHistoryEventHandler handler);
	boolean removeHistoryEventHandler(IHistoryEventHandler handler);
}
