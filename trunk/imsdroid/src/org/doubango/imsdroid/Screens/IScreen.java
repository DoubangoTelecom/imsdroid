package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.Screens.Screen.SCREEN_ID;

public interface IScreen {
	Screen.SCREEN_ID getId();
	String getScreenTitle();
	boolean isWellknown();
}
