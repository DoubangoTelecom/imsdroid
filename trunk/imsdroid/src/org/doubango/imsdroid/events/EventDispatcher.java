package org.doubango.imsdroid.events;

public abstract class EventDispatcher /* implements
		IEventDispatcher<EventHandler<EventArgs>> */ {

	public static <TEventArgs> boolean RaiseEvent(
			IEventHandler<TEventArgs> handler, Object sender, TEventArgs e) {
		if (handler != null) {
			return handler.onEvent(sender, e);
		}
		return false;
	}
}
