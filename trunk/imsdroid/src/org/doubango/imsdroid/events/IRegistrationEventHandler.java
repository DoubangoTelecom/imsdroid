package org.doubango.imsdroid.events;

public interface IRegistrationEventHandler /* extends IEventHandler<RegistrationEventArgs> */{
	boolean onRegistrationEvent(Object sender, RegistrationEventArgs e);
}
