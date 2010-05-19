package org.doubango.imsdroid.events;

import java.util.concurrent.CopyOnWriteArrayList;


public abstract class EventHandler/*<TEventArgs> implements IEventHandler<TEventArgs>*/
{

//	private final ArrayList<IEventHandler<TEventArgs>> handlers;
//	
//	public EventHandler()
//	{
//		this.handlers = new ArrayList<IEventHandler<TEventArgs>>();
//	}
//	
//	public void add(IEventHandler<TEventArgs> handler)
//	{
//		this.handlers.add(handler);
//	}
//	
//	public void remove(IEventHandler<TEventArgs> handler)
//	{
//		this.handlers.remove(handler);
//	}
	
	public static <TEventHandler> boolean addEventHandler(
			CopyOnWriteArrayList<TEventHandler> eventHandlers, TEventHandler eventHandler) {
		if (eventHandler != null && eventHandlers != null && !eventHandlers.contains(eventHandler)) {
			return eventHandlers.add(eventHandler);
		}
		return false;
	}
	
	public static <TEventHandler> boolean removeEventHandler(
			CopyOnWriteArrayList<TEventHandler> eventHandlers, TEventHandler eventHandler) {
		if (eventHandler != null && eventHandlers != null) {
			return eventHandlers.remove(eventHandler);
		}
		return false;
	}
}
