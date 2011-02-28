package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;

import android.os.Bundle;

public class ScreenTabInfo extends BaseScreen {
	private static String TAG = ScreenTabInfo.class.getCanonicalName();
	
	public ScreenTabInfo() {
		super(SCREEN_TYPE.TAB_INFO_T, TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_info);
	}
}
