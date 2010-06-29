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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "group")
public class Group implements Comparable<Group> {
	
	@ElementList(required = false, inline=true)
    private List<Contact> contact;
	
	@Attribute(name = "name", required = true)
	private String name;
	@Attribute(name = "displayName", required = true)
	private String displayName;
	
	public Group(){
		this(null, null);
	}
	
	public Group(String name, String displayName){
		this.contact = new ArrayList<Contact>();
		
		this.name = name;
		this.displayName = displayName;
	}
	
	public List<Contact> getContacts(){
		return this.contact;
	}

	public String getName(){
		return this.name;
	}
	
	public String getDisplayName(){
		return this.displayName;
	}
	
	public int compareTo(Group another) {
		return 0;
	}
}
