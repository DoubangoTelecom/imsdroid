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
package org.doubango.imsdroid.Sevices.Impl;

import java.io.File;
import java.io.IOException;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class ConfigurationService  extends Service implements IConfigurationService{

	private final static String CONFIG_FILE = "configuration.xml";
	private File config_file; 
	private Configuration configuration;
	private final Serializer serializer;
	
	public ConfigurationService(){
		 this.serializer = new Persister();
	}
	
	public boolean start() {
		boolean newfile = false;
		
		// Creates configuration file if does not exist
		this.config_file = new File(String.format("%s/%s", ServiceManager.getStorageService().getCurrentDir(), ConfigurationService.CONFIG_FILE));
		if(!this.config_file.exists()){
			try {
				this.config_file.createNewFile();
				newfile = true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		// create configuration object (deserialization)
		try{
			if(newfile){
				this.configuration = new Configuration();
				return this.compute(); // Empty but valid XML document
			}
			else {
				this.configuration = this.serializer.read(Configuration.class, this.config_file);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			this.configuration = new Configuration();
		}
		
		return true;
	}

	public boolean stop() {
		/* computes */
		return this.compute();
	}
	
	public boolean setString(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, String value){
		return this.configuration.setEntry(section.toString(), entry.toString(), value);
	}
	
	public boolean setInt(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, int value){
		return this.setString(section, entry, new Integer(value).toString());
	}
	
	public boolean setBoolean(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, boolean value){
		return this.setString(section, entry, (value ? "true" : "false"));
	}
	
	public String getString(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, String defaultValue){
		String value = this.configuration.getValue(section.toString(), entry.toString());
		if(value == null){
			return defaultValue;
		}
		else{
			return value;
		}
	}
	
	public int getInt(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, int defaultValue){
		String value = this.configuration.getValue(section.toString(), entry.toString());
		if(value == null){
			return defaultValue;
		}
		else {
			try{
				return Integer.parseInt(value);
			}
			catch(NumberFormatException e){
				Log.e(this.getClass().getCanonicalName(), e.getMessage());
				return defaultValue;
			}
		}
	}
	
	public boolean getBoolean(Configuration.CONFIGURATION_SECTION section, Configuration.CONFIGURATION_ENTRY entry, boolean defaultValue){
		String value = this.configuration.getValue(section.toString(), entry.toString());
		if(value == null){
			return defaultValue;
		}
		else{		
			return Boolean.parseBoolean(value);
		}
	}
	
	
	public boolean compute(){
		try{
			this.serializer.write(this.configuration, this.config_file);
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
