package org.doubango.imsdroid.events;

/**
 * Generic Event Handler to implements something likes Micro$oft delegates.
 *
 * @param <TEventArgs>
 */
public interface IEventHandler<TEventArgs> {
	boolean onEvent(Object sender, TEventArgs e);
}
