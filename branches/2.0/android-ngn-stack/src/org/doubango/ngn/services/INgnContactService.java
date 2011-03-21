package org.doubango.ngn.services;

import org.doubango.ngn.model.NgnContact;
import org.doubango.ngn.utils.NgnCallbackFunc;
import org.doubango.ngn.utils.NgnObservableList;

public interface INgnContactService extends INgnBaseService{
	void setOnBeginLoadCallback(NgnCallbackFunc<Object> callback);
	void setOnNewPhoneNumberCallback(NgnCallbackFunc<String> callback);
	void setOnEndLoadCallback(NgnCallbackFunc<Object> callback);
	boolean load();
	boolean isLoading();
	boolean isReady();
	
	NgnContact getContactByUri(String uri);
	NgnContact getContactByPhoneNumber(String anyPhoneNumber);
	
	NgnObservableList<NgnContact> getObservableContacts();
}
