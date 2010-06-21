package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.Screens.Screen;

public interface IScreenService  extends IService{
	
	boolean back();
	
	boolean show(Class<? extends Screen> cls, String id);
	boolean show(Class<? extends Screen> cls);
	boolean show(String id);
	void runOnUiThread(Runnable r);
	boolean destroy(String id);
	
	//boolean show(Screen screen);
	//boolean show(String id);
	//boolean show(Screen.SCREEN_ID id);
	//Screen get(String id);
	//Screen get(Screen.SCREEN_ID id);
	void setProgressInfoText(String text);
	Screen getCurrentScreen();
	Screen getScreen(String id);
}
