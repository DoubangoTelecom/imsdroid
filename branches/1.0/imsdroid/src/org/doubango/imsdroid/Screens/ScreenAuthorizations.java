package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;

import android.os.Bundle;

public class ScreenAuthorizations extends Screen {

	public ScreenAuthorizations() {
		super(SCREEN_TYPE.AUTHORIZATIONS_T, ScreenAuthorizations.class.getCanonicalName());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_authorizations);
	}
}
