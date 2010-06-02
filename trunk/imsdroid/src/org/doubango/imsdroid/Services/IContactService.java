package org.doubango.imsdroid.Services;

import java.util.List;

import org.doubango.imsdroid.Model.Contact;
import org.doubango.imsdroid.events.IContactsEventDispatcher;

public interface IContactService  extends IService, IContactsEventDispatcher
 {
	
	boolean loadContacts();
	boolean isLoadingContacts();
	
	List<Contact> getContacts();
}
