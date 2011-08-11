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

import org.doubango.imsdroid.utils.StringUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="section")
public class ConfigurationSection{
	
	@Attribute(name="name", required=false)
	private String name;
	
	@ElementList(inline=true, required=false)
	private List<ConfigurationSectionEntry> entries;
	
	public boolean equals(Object o) {
		ConfigurationSection section = (ConfigurationSection)o;
		if(section != null){
			return this.name.equals(section.name);
		}
		return false;
	}

	public ConfigurationSection(){
		this(null);
	}
	
	public ConfigurationSection(String name){
		this.name = name;
		this.entries = new ArrayList<ConfigurationSectionEntry>();
	}
	
	public String getName(){
		return this.name;
	}
	
	public void addEntry(ConfigurationSectionEntry entry){
		int index = this.entries.indexOf(entry);
		if(index == -1){
			this.entries.add(entry);
		}
		else{
			this.entries.set(index, entry);
		}
	}
	
	public ConfigurationSectionEntry getEntry(String key){
		for(ConfigurationSectionEntry entry : this.entries){
			if(StringUtils.equals(entry.getKey(), key, false)){
				return entry;
			}
		}
		return null;
	}
}