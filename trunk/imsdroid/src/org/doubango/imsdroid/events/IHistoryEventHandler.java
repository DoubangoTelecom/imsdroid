package org.doubango.imsdroid.events;

public interface IHistoryEventHandler /* extends IEventHandler<HistoryEventArgs> */{
	boolean onHistoryEvent(Object sender, HistoryEventArgs e);
}