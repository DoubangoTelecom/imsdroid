package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.Screens.Screen;

import android.app.ActivityGroup;

public interface IScreenService  extends IService{
	
	boolean back();
	boolean show(Screen screen);
	boolean show(String id);
	boolean show(Screen.SCREEN_ID id);
	void setProgressInfoText(String text);
	Screen getCurrentScreen();
}
