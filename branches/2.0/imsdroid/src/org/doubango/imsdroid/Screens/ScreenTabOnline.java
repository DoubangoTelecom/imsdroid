package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;

import android.os.Bundle;

public class ScreenTabOnline extends BaseScreen{
	private static String TAG = ScreenTabOnline.class.getCanonicalName();
	
	public ScreenTabOnline() {
		super(SCREEN_TYPE.TAB_ONLINE, TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_online);
	}
}
