package org.doubango.ngn.services;


public interface INgnConfigurationService extends INgnBaseService{
	boolean putString(final String entry, String value, boolean commit);
	boolean putString(final String entry, String value);
	boolean putInt(final String entry, int value, boolean commit);
	boolean putInt(final String entry, int value);
	boolean putFloat(final String entry, float value, boolean commit);
	boolean putFloat(final String entry, float value);
	boolean putBoolean(final String entry, boolean value, boolean commit);
	boolean putBoolean(final String entry, boolean value);
	
	String getString(final String entry, String defaultValue);
	int getInt(final String entry, int defaultValue);
	float getFloat(final String entry, float defaultValue);
	boolean getBoolean(final String entry, boolean defaultValue);
	
	boolean commit();
}
