/*
* Copyright (C) 2010 Mamadou Diop.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango.org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*
*/

package org.doubango.imsdroid.Model;

import java.util.ArrayList;
import java.util.List;

import org.doubango.imsdroid.sip.PresenceStatus;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import android.graphics.Bitmap;

@Root(name = "group")
public class Group implements Comparable<Group> {
	
	@ElementList(entry = "contact", inline=true, required=false)
    protected List<Group.Contact> contacts;
	
	@Attribute(name = "name", required = true)
	private String name;
	@Attribute(name = "displayName", required = true)
	private String displayName;
	
	public Group(){
		this(null, null);
	}
	
	public Group(String name, String displayName){
		this.contacts = new ArrayList<Contact>();
		
		this.name = name;
		this.displayName = displayName;
	}
	
	public void addContact(Group.Contact contact){
		this.contacts.add(contact);
	}
    
    public List<Group.Contact> getContacts() {
        if (contacts == null) {
        	contacts = new ArrayList<Group.Contact>();
        }
        return this.contacts;
    }
    
    public Group.Contact getContact(String uri) {
        if (this.contacts != null) {
        	for(Group.Contact contact : this.contacts){
        		if(contact.getUri().equalsIgnoreCase(uri)){
        			return contact;
        		}
        	}
        }
        return null;
    }

	public String getName(){
		return this.name;
	}
	
	public String getDisplayName(){
		return this.displayName;
	}
	
	@Override
	public String toString() {
		return this.displayName;
	}

	@Override
	public int compareTo(Group another) {
		if(this.name != null && another != null){
			return this.name.compareTo(another.getName());
		}
		return -1;
	}
	
	@Root(name = "contact", strict = false)
    public static class Contact implements Comparable<Contact>{
		@Attribute(name = "uri", required = true)
		private String uri;
		@Element(name = "group", required = false)
		private String group;
		@Element(name = "displayName", required = false)
		private String displayName;
		@Element(name = "firstName", required = false)
		private String firstName;
		@Element(name = "lastName", required = false)
		private String lastName;
		@Element(name = "phoneNumber", required = false)
		private String phoneNumber;
		@Element(name = "freeText", required = false)
		private String freeText;
		
		private PresenceStatus status;
		private Bitmap avatarImage;
		
		public Contact(String uri, String displayName, String group){
			this.uri = uri;
			this.displayName = displayName;
			this.status = PresenceStatus.Offline;
			this.group = group;
		}
		
		public Contact(String uri, String displayName){
			this(uri, displayName, null);
		}

		public Contact(){
			this(null, null, null);
		}
		
		public String getUri(){
			return this.uri;
		}
		
		public void setUri(String  uri){
			this.uri = uri;
		}
		
		public String getDisplayName(){
			return this.displayName;
		}
		
		public void setDisplayName(String displayName){
			this.displayName = displayName;
		}
		
		public String getGroup(){
			return this.group;
		}
		
		public void setGroup(String group){
			this.group = group;
		}
		
		public String getFirstName(){
			return this.firstName;
		}
		
		public void setFirstName(String firstName){
			this.firstName = firstName;
		}
		
		public void setLastName(String lastName){
			this.lastName = lastName;
		}
		
		public String getLastName(){
			return this.lastName;
		}
		
		public void setPhoneNumber(String phoneNumber){
			this.phoneNumber = phoneNumber;
		}
		
		public String getPhoneNumber(){
			return this.phoneNumber;
		}
		
		public void setFreeText(String freeText){
			this.freeText = freeText;
		}
		
		public String getFreeText(){
			return this.freeText;
		}
		
		public void setStatus(PresenceStatus status){
			this.status = status;
		}
		
		public PresenceStatus getStatus(){
			return this.status;
		}
		
		public Bitmap getAvatar(){
			return this.avatarImage;
		}
		
		public void setAvatar(String base64String){
			this.avatarImage = null;
		}
		
		@Override
		public int compareTo(Contact another) {
			return this.uri.compareTo(another.uri);
		}
	}
}
