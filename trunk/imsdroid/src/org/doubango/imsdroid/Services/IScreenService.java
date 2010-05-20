package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.Screens.Screen;

import android.app.Activity;

public interface IScreenService  extends IService{

	void setMainActivity(Activity main);
	
	boolean show(Screen screen);
	boolean show(Screen.SCREEN_TYPE type);
	Screen getScreen(Screen.SCREEN_TYPE type);
}
