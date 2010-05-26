package org.doubango.imsdroid.Screens;


public interface IScreen {
	Screen.SCREEN_ID getId();
	String getScreenTitle();
	boolean isWellknown();
}
