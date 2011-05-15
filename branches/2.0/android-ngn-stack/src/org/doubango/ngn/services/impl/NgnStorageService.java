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
package org.doubango.ngn.services.impl;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.services.INgnStorageService;

import android.util.Log;

/**@page NgnStorageService_page Storage Service
 * This service is used to manage storage functions.
 */

/**
 * Storage service.
 */
public class NgnStorageService  extends NgnBaseService implements INgnStorageService{
	private final static String TAG = NgnStorageService.class.getCanonicalName();
	
	private final String mCurrentDir;
	private final String mContentShareDir;
	
	public NgnStorageService(){
		mCurrentDir = String.format("/data/data/%s", NgnApplication.getContext().getPackageName());
		mContentShareDir = "/sdcard/wiPhone";
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "starting...");
		return true;
	}
	
	@Override
	public boolean stop() {
		Log.d(TAG, "stopping...");
		return true;
	}
	
	@Override
	public String getCurrentDir(){
		return this.mCurrentDir;
	}
	
	@Override
	public String getContentShareDir(){
		return this.mContentShareDir;
	}
}
