package org.doubango.imsdroid.Model;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "contacts")
public class ContactList {
	@ElementList(name="contact", required = false, inline=true)
    private List<Contact> contacts;
	
	public List<Contact> getList(){
		return this.contacts;
	}
	
	public void addContact(Contact c){
		if(this.contacts == null){
			this.contacts = new ArrayList<Contact>();
		}
		this.contacts.add(c);
	}
}
