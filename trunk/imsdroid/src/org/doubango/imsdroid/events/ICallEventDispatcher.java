package org.doubango.imsdroid.events;

public interface ICallEventDispatcher /*extends IEventDispatcher<ICallEventHandler>*/{
	boolean addCallEventHandler(ICallEventHandler handler);
	boolean removeCallEventHandler(ICallEventHandler handler);
}