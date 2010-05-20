package org.doubango.imsdroid.Screens;

import android.app.Activity;

public abstract class Screen extends Activity implements IScreen{
	public static enum SCREEN_TYPE {
		ABOUT, EAB, HISTORY, HOME
	}
	
	protected final SCREEN_TYPE type;
	
	protected Screen(SCREEN_TYPE type)
	{
		this.type = type;
	}
}
