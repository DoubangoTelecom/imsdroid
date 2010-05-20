package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;

import android.os.Bundle;

public class HomeScreen extends Screen{

/* ===================== Activity ========================*/
    
    public HomeScreen() {
		super(SCREEN_TYPE.HOME);
	}

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_home);
    }
}
