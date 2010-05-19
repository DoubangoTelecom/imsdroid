package org.doubango.imsdroid.events;

public interface IEventDispatcher<TEventHandler> {
	boolean add(IEventHandler<TEventHandler> handler);
	boolean remove(IEventHandler<TEventHandler> handler);
}
