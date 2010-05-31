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
	
	@Override
	public int compareTo(Group another) {
		return 0;
	}
}
