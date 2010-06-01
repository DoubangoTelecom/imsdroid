package org.doubango.imsdroid.Services;

import java.util.List;

import org.doubango.imsdroid.Model.Contact;

public interface IContactService  extends IService{
	
	List<Contact> getContacts();
}
