// Application Fundamentals: http://developer.android.com/guide/topics/fundamentals.html#acttask
// Android Activity Launch Modes: http://www.justinlee.sg/2010/03/13/android-activity-launch-modes/

package org.doubango.imsdroid.Sevices.Impl;

import org.doubango.imsdroid.Screens.HomeScreen;
import org.doubango.imsdroid.Screens.Screen;
import org.doubango.imsdroid.Screens.Screen.SCREEN_TYPE;
import org.doubango.imsdroid.Services.IScreenService;

import android.app.Activity;
import android.content.Intent;

public class ScreenService extends Service implements IScreenService {

	private Activity mainActivity;
	private HomeScreen homeScreen;

	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setMainActivity(Activity main) {
		this.mainActivity = main;
	}
	
	public boolean show(Screen screen) {
		if (screen instanceof Activity && this.mainActivity != null) {	
			Intent intent = new Intent(this.mainActivity, screen.getClass());
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			screen.startActivity(intent);
			return true;
		}
		return false;
	}

	public Screen getScreen(SCREEN_TYPE type) {
		switch (type) {
		case ABOUT:
			break;
		case EAB:
			break;
		case HISTORY:
			break;
		case HOME:
			if (this.homeScreen == null) {
				this.homeScreen = new HomeScreen();
			}
			return this.homeScreen;
		}
		return null;
	}

	public boolean show(SCREEN_TYPE type) {
		Screen screen = this.getScreen(type);
		if (screen != null) {
			return this.show(screen);
		} else {
			return false;
		}
	}
}
