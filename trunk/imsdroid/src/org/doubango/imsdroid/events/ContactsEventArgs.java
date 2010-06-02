package org.doubango.imsdroid.events;

public class ContactsEventArgs {
	private final ContactsEventTypes type;
	
	public ContactsEventArgs(ContactsEventTypes type){
		this.type = type;
	}
	
	public ContactsEventTypes getType(){
		return this.type;
	}
}
