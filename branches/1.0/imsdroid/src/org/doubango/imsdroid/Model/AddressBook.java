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

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "address-book", strict = false)
public class AddressBook {
	@ElementList(entry = "group", inline = true, required = false)
	protected List<Group> groups;

	public AddressBook(){
		this.groups = new ArrayList<Group>();
	}
	
	public Group getGroup(String name){
		for(Group group : this.groups){
			if(group.getName().equals(name)){
				return group;
			}
		}
		return null;
	}
	
	public void addGroup(String name, String displayName){
		Group group = new Group(name, displayName);
		this.groups.add(group);
	}
	
	public boolean addContact(Group.Contact contact){
		Group group = this.getGroup(contact.getGroup());
		if(group != null){
			group.addContact(contact);
			return true;
		}
		return false;
	}

	public List<Group> getGroups() {
		return this.groups;
	}
	
	public void set(List<Group> groups){
		if(groups != null){
			this.groups = groups;
		}
	}
	
	public void clear(){
		this.groups.clear();
	}
	
}
