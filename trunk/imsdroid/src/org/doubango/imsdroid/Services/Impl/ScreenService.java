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

import org.doubango.imsdroid.Main;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Screens.Screen;
import org.doubango.imsdroid.Screens.ScreenHome;
import org.doubango.imsdroid.Services.IScreenService;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class ScreenService extends Service implements IScreenService {

	private int lastScreensIndex = -1; // ring cursor
	private final String[] lastScreens =  new String[]{ // ring
    		null,
    		null,
    		null,
    		null
	};

	public ScreenService() {
		//this.screens = new HashMap<String, Screen>();
	}

	public boolean start() {
		return true;
	}

	public boolean stop() {
		return true;
	}
	
	public boolean back(){
		String screen;
		
		// no screen in the stack
		if(this.lastScreensIndex < 0){
			return true;
		}
		
		// zero is special case
		if(this.lastScreensIndex == 0){
			if((screen = this.lastScreens[this.lastScreens.length-1]) == null){
				// goto home
				return this.show(ScreenHome.class);
			}
			else{
				return this.show(screen);
			}
		}
		// all other cases
		screen = this.lastScreens[this.lastScreensIndex-1];
		this.lastScreens[this.lastScreensIndex-1] = null;
		this.lastScreensIndex--;
		if(screen == null){
			return this.show(ScreenHome.class);
		}
		else{
			return this.show(screen);
		}
	}
	
	public boolean show(Class<? extends Screen> cls, String id) {
		Main mainActivity = ServiceManager.getMainActivity();
		
		String screen_id = (id == null) ? cls.getCanonicalName() : id;
		Intent intent = new Intent(mainActivity, cls);
		intent.putExtra("id", screen_id);
		View view = mainActivity.getLocalActivityManager().startActivity(screen_id, intent).getDecorView();
		
		LinearLayout layout = (LinearLayout) mainActivity.findViewById(R.id.main_linearLayout_principal);
		layout.removeAllViews();
		layout.addView(view);
		
		// title
		//mainActivity.setScreenTitle(screen.getScreenTitle());
		
		// add to stack
		this.lastScreens[(++this.lastScreensIndex % this.lastScreens.length)] = screen_id;
		this.lastScreensIndex %= this.lastScreens.length;
		
		// update current screen
		//this.currentScreen = screen_id;
		
		return true;
	}
	
	public boolean show(Class<? extends Screen> cls){
		return this.show(cls, null);
	}

	public boolean show(String id) {
		Screen screen = (Screen)ServiceManager.getMainActivity().getLocalActivityManager().getActivity(id);

		if (screen == null) {
			Log.e(this.getClass().getCanonicalName(), String.format(
					"Failed to retrieve the Screen with id=%s", id));
			return false;
		} else {
			return this.show(screen.getClass(), id);
		}
	}

	public void runOnUiThread(Runnable r){
		ServiceManager.getMainActivity().runOnUiThread(r);
	}
	
	public boolean destroy(String id){
		return (ServiceManager.getMainActivity().getLocalActivityManager().destroyActivity(id, true) != null);
	}
	
	public void setProgressInfoText(String text)
	{
		ServiceManager.getMainActivity().setProgressInfo(text);
	}
	
	public Screen getCurrentScreen(){
		return (Screen)ServiceManager.getMainActivity().getLocalActivityManager().getCurrentActivity();
	}
		
	public Screen getScreen(String id){
		return (Screen)ServiceManager.getMainActivity().getLocalActivityManager().getActivity(id);
	}
}
