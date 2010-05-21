// Application Fundamentals: http://developer.android.com/guide/topics/fundamentals.html#acttask
// Android Activity Launch Modes: http://www.justinlee.sg/2010/03/13/android-activity-launch-modes/
// Hellow Views: http://developer.android.com/guide/tutorials/views/index.html

package org.doubango.imsdroid.Sevices.Impl;

import java.util.HashMap;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Screens.Screen;
import org.doubango.imsdroid.Screens.ScreenAbout;
import org.doubango.imsdroid.Screens.ScreenHome;
import org.doubango.imsdroid.Screens.Screen.SCREEN_TYPE;
import org.doubango.imsdroid.Services.IScreenService;

import android.app.ActivityGroup;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class ScreenService extends Service implements IScreenService {

	private ActivityGroup mainActivity;
	private final HashMap<String, Screen> screens;
	private static final HashMap<String, Screen.SCREEN_TYPE> WELL_KNOWN_SCREENS;

	static {
		WELL_KNOWN_SCREENS = new HashMap<String, Screen.SCREEN_TYPE>();
		WELL_KNOWN_SCREENS.put(Screen.SCREEN_ID_ABOUT, SCREEN_TYPE.ABOUT);
		WELL_KNOWN_SCREENS.put(Screen.SCREEN_ID_HOME, SCREEN_TYPE.HOME);
	}

	public ScreenService() {
		this.screens = new HashMap<String, Screen>();
	}

	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setMainActivity(ActivityGroup main) {
		this.mainActivity = main;
	}

	public boolean show(Screen screen) {
		if (screen == null) {
			Log.e(this.getClass().getCanonicalName(), "Null Screen");
			return false;
		}
		
		Intent intent = new Intent(this.mainActivity, screen.getClass());
		View view = this.mainActivity.getLocalActivityManager().startActivity(screen.getId(), intent).getDecorView();
		
		LinearLayout layout = (LinearLayout) this.mainActivity.findViewById(R.id.main_linearLayout_principal);
		layout.removeAllViews();
		layout.addView(view);

		return true;
	}

	public boolean show(String id) {
		Screen screen;

		/* already exist? */
		if ((screen = this.screens.get(id)) == null) {
			/* does not exist: is it a well-known screen? */
			if (ScreenService.WELL_KNOWN_SCREENS.containsKey(id)) {
				Screen.SCREEN_TYPE type = ScreenService.WELL_KNOWN_SCREENS
						.get(id);
				switch (type) {
				case ABOUT:
					screen = new ScreenAbout();
					break;

				case EAB:
					break;

				case HISTORY:
					break;

				case HOME:
					screen = new ScreenHome();
					break;
				}
				/* adds the newly created well-know screen */
				if (screen != null) {
					this.screens.put(screen.getId(), screen);
				}
			}
		}

		if (screen == null) {
			Log.e(this.getClass().getCanonicalName(), String.format(
					"Failed to retrieve the Screen with id=%s", id));
			return false;
		} else {
			return this.show(screen);
		}
	}

	// public boolean show(Screen screen) {
	// if (screen instanceof Activity && this.mainActivity != null) {
	// Intent intent = new Intent(this.mainActivity, screen.getClass());
	// intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	// screen.startActivity(intent);
	// return true;
	// }
	// return false;
	// }
	//
	// public boolean show(SCREEN_TYPE type) {
	// Screen screen = this.getScreen(type);
	// if (screen != null) {
	// return this.show(screen);
	// } else {
	// return false;
	// }
	// }
	//	
	// public Screen getScreen(SCREEN_TYPE type) {
	// switch (type) {
	// case ABOUT:
	// break;
	// case EAB:
	// break;
	// case HISTORY:
	// break;
	// case HOME:
	// if (this.homeScreen == null) {
	// this.homeScreen = new HomeScreen();
	// }
	// return this.homeScreen;
	// }
	// return null;
	// }
}
