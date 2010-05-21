package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;

import android.os.Bundle;

public class ScreenAbout extends Screen {

	public ScreenAbout() {
		super(SCREEN_TYPE.ABOUT);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_about);
	}

	public String getId() {
		return Screen.SCREEN_ID_ABOUT;
	}
}
