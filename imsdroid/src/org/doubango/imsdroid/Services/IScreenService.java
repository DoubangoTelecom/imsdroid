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
package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.Screens.IBaseScreen;
import org.doubango.ngn.services.INgnBaseService;

import android.app.Activity;

public interface IScreenService extends INgnBaseService{
	boolean back();
	boolean bringToFront(int action, String[]... args);
	boolean bringToFront(String[]... args);
	boolean show(Class<? extends Activity> cls, String id);
	boolean show(Class<? extends Activity> cls);
	boolean show(String id);
	void runOnUiThread(Runnable r);
	boolean destroy(String id);
	void setProgressInfoText(String text);
	IBaseScreen getCurrentScreen();
	IBaseScreen getScreen(String id);
}
