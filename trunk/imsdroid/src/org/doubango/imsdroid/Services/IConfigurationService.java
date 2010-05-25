package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.Model.Configuration;

public interface IConfigurationService extends IService{
	boolean setString(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, String value);
	boolean setInt(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, int value);
	boolean setBoolean(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, boolean value);
	
	String getString(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, String defaultValue);
	int getInt(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, int defaultValue);
	boolean getBoolean(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, boolean defaultValue);
	
	boolean compute();
}
