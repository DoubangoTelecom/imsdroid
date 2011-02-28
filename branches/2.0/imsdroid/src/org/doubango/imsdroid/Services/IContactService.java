
package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.Model.Contact;
import org.doubango.imsdroid.Utils.ObservableList;


public interface IContactService extends IBaseService{
	boolean load();
	boolean isLoading();
	
	Contact getContactByUri(String uri);
	Contact getContactByPhoneNumber(String primaryPhoneNumber);
	
	ObservableList<Contact> getObservableContacts();
}
