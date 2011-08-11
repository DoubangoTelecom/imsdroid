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
package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.Model.Configuration;

public interface IConfigurationService extends IService{
	boolean setString(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, String value);
	boolean setInt(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, int value);
	boolean setFloat(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, float value);
	boolean setBoolean(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, boolean value);
	
	String getString(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, String defaultValue);
	int getInt(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, int defaultValue);
	float getFloat(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, float defaultValue);
	boolean getBoolean(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, boolean defaultValue);
	
	boolean compute();
}
