package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.Screens.Screen;

import android.app.ActivityGroup;

public interface IScreenService  extends IService{

	void setMainActivity(ActivityGroup main);
	
	boolean show(Screen screen);
	boolean show(String id);
}
