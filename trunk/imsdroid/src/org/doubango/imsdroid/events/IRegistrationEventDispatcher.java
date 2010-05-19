package org.doubango.imsdroid.events;

public interface IRegistrationEventDispatcher /*extends IEventDispatcher<IRegistrationEventHandler>*/{
	boolean addRegistrationEventHandler(IRegistrationEventHandler handler);
	boolean removeRegistrationEventHandler(IRegistrationEventHandler handler);
}
