package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.Screens.BaseScreen.SCREEN_TYPE;

import android.view.Menu;

public interface IBaseScreen {
	String getId();
	SCREEN_TYPE getType();
	boolean hasMenu();
	boolean hasBack();
	boolean back();
	boolean createOptionsMenu(Menu menu);
}
