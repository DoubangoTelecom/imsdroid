package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;

import android.os.Bundle;

public class ScreenGeneral  extends Screen {
	
	public ScreenGeneral() {
		super(SCREEN_TYPE.GENERAL_T);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_general);
	}
}
