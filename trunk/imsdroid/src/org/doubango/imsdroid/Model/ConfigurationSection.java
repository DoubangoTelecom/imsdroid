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