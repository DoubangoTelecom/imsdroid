/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
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
*/
package org.doubango.ngn.services;

/**@page NgnConfigurationService_page Configuration Service
 * The configuration service is used to store the user preferences. All preferences saved using this service
 * are persistent which means that you can retrieve them when the application/device restarts. <br />
 * You should never create or start this service by yourself. <br />
 * An instance of this service could be retrieved like this:
 * @code
 * final INgnConfigurationService mConfigurationService = NgnEngine.getInstance().getConfigurationService();
 * @endcode
 */
public interface INgnConfigurationService extends INgnBaseService{
	/**
	 * Puts a string value into the user storage space
	 * @param entry the name of the preference to save
	 * @param value the value of the preference
	 * @param commit whether to commit the changes
	 * @return true if succeed and false otherwise
	 * @sa @ref getString()
	 */
	boolean putString(final String entry, String value, boolean commit);
	/**
	 * Puts a string value into the user storage space without committing the change
	 * You must call @ref commit() in order to commit the changes. You should only
	 * use this function if you want to put many values before applying the changes.
	 * @param entry the name of the preference to save
	 * @param value the value of the preference
	 * @return true if succeed and false otherwise
	 * @sa @ref getString() @ref commit()
	 */
	boolean putString(final String entry, String value);
	/**
	 * Puts a integer value into the user storage space
	 * @param entry the name of the preference to save
	 * @param value the value of the preference
	 * @param commit whether to commit the changes
	 * @return true if succeed and false otherwise
	 * @sa @ref getInt()
	 */
	boolean putInt(final String entry, int value, boolean commit);
	/**
	 * Puts an integer value into the user storage space without committing the change
	 * You must call @ref commit() in order to commit the changes. You should only
	 * use this function if you want to put many values before applying the changes.
	 * @param entry the name of the preference to save
	 * @param value the value of the preference
	 * @return true if succeed and false otherwise
	 * @sa @ref getString() @ref commit()
	 */
	boolean putInt(final String entry, int value);
	/**
	 * Puts a float value into the user storage space
	 * @param entry the name of the preference to save
	 * @param value the value of the preference
	 * @param commit whether to commit the changes
	 * @return true if succeed and false otherwise
	 * @sa @ref getFloat()
	 */
	boolean putFloat(final String entry, float value, boolean commit);
	/**
	 * Puts a float value into the user storage space without committing the change
	 * You must call @ref commit() in order to commit the changes. You should only
	 * use this function if you want to put many values before applying the changes.
	 * @param entry the name of the preference to save
	 * @param value the value of the preference
	 * @return true if succeed and false otherwise
	 * @sa @ref getString() @ref commit()
	 */
	boolean putFloat(final String entry, float value);
	/**
	 * Puts a boolean value into the user storage space
	 * @param entry the name of the preference to save
	 * @param value the value of the preference
	 * @param commit whether to commit the changes
	 * @return true if succeed and false otherwise
	 * @sa @ref getBoolean()
	 */
	boolean putBoolean(final String entry, boolean value, boolean commit);
	/**
	 * Puts a boolean value into the user storage space without committing the change
	 * You must call @ref commit() in order to commit the changes. You should only
	 * use this function if you want to put many values before applying the changes.
	 * @param entry the name of the preference to save
	 * @param value the value of the preference
	 * @return true if succeed and false otherwise
	 * @sa @ref getString() @ref commit()
	 */
	boolean putBoolean(final String entry, boolean value);
	
	/**
	 * Gets a string value from the user storage. This value should be previously stored using @ref
	 * putString().
	 * @param entry the name of the preference for which to retrieve the value
	 * @param defaultValue the default value to return if this function fails or the preference has never
	 * been stored using @ref putString()
	 * @return the value of the preference
	 * @sa @ref putString()
	 */
	String getString(final String entry, String defaultValue);
	/**
	 * Gets an integer value from the user storage. This value should be previously stored using @ref
	 * putInt().
	 * @param entry the name of the preference for which to retrieve the value
	 * @param defaultValue the default value to return if this function fails or the preference has never
	 * been stored using @ref putInt()
	 * @return the value of the preference
	 * @sa @ref putInt()
	 */
	int getInt(final String entry, int defaultValue);
	/**
	 * Gets a float value from the user storage. This value should be previously stored using @ref
	 * putFloat().
	 * @param entry the name of the preference for which to retrieve the value
	 * @param defaultValue the default value to return if this function fails or the preference has never
	 * been strored using @ref putFloat()
	 * @return the value of the preference
	 * @sa @ref putFloat()
	 */
	float getFloat(final String entry, float defaultValue);
	/**
	 * Gets a boolean value from the user storage. This value should be previously stored using @ref
	 * putBoolean().
	 * @param entry the name of the preference for which to retrieve the value
	 * @param defaultValue the default value to return if this function fails or the preference has never
	 * been stored using @ref putBoolean()
	 * @return the value of the preference
	 * @sa @ref putBoolean()
	 */
	boolean getBoolean(final String entry, boolean defaultValue);
	
	/**
	 * Commits all pending changes
	 * @return true if succeed and false otherwise
	 */
	boolean commit();
}
