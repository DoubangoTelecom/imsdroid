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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "entry")
public class ConfigurationSectionEntry {

	@Attribute(name = "key")
	private String key;

	@Attribute(name = "value")
	private String value;

	public ConfigurationSectionEntry() {
		this(null, null);
	}

	public ConfigurationSectionEntry(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public boolean equals(Object o) {
		ConfigurationSectionEntry entry = (ConfigurationSectionEntry) o;
		if (entry != null) {
			return this.key.equals(entry.key);
		}
		return false;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
}