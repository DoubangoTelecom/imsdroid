package org.doubango.imsdroid.events;

public interface IContactsEventDispatcher /*extends IEventDispatcher<IContactsEventHandler>*/{
	boolean addContactsEventHandler(IContactsEventHandler handler);
	boolean removeContactsEventHandler(IContactsEventHandler handler);
}
