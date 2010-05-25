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