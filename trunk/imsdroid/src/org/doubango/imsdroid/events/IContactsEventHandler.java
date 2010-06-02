package org.doubango.imsdroid.events;

public interface IContactsEventHandler /* extends IEventHandler<ContactsEventArgs> */{
	boolean onContactsEvent(Object sender, ContactsEventArgs e);
}