package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;

import android.os.Bundle;

public class ScreenTabMessages extends BaseScreen {
	private static String TAG = ScreenTabMessages.class.getCanonicalName();
	
	public ScreenTabMessages() {
		super(SCREEN_TYPE.TAB_MESSAGES_T, TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_messages);
	}
}
