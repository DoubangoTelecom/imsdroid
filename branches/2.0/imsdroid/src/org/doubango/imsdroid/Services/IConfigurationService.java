
package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.Utils.ConfigurationUtils;

public interface IConfigurationService extends IBaseService{
	boolean putString(ConfigurationUtils.ConfigurationEntry entry, String value, boolean commit);
	boolean putString(ConfigurationUtils.ConfigurationEntry entry, String value);
	boolean putInt(ConfigurationUtils.ConfigurationEntry entry, int value, boolean commit);
	boolean putInt(ConfigurationUtils.ConfigurationEntry entry, int value);
	boolean putFloat(ConfigurationUtils.ConfigurationEntry entry, float value, boolean commit);
	boolean putFloat(ConfigurationUtils.ConfigurationEntry entry, float value);
	boolean putBoolean(ConfigurationUtils.ConfigurationEntry entry, boolean value, boolean commit);
	boolean putBoolean(ConfigurationUtils.ConfigurationEntry entry, boolean value);
	
	String getString(ConfigurationUtils.ConfigurationEntry entry, String defaultValue);
	int getInt(ConfigurationUtils.ConfigurationEntry entry, int defaultValue);
	float getFloat(ConfigurationUtils.ConfigurationEntry entry, float defaultValue);
	boolean getBoolean(ConfigurationUtils.ConfigurationEntry entry, boolean defaultValue);
	
	boolean commit();
}
