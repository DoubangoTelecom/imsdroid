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
package org.doubango.imsdroid.Services.Impl;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.Services.IStorageService;

public class StorageService  extends Service implements IStorageService{

	private final String currentDir;
	
	public StorageService(){
		this.currentDir = String.format("/data/data/%s", IMSDroid.getContext().getPackageName());
	}
	
	public boolean start() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public boolean stop() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public String getCurrentDir(){
		return this.currentDir;
	}
}
