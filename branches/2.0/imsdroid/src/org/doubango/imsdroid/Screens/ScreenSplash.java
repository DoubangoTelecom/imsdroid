package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;

import android.os.Bundle;
import android.util.Log;

public class ScreenSplash extends BaseScreen {
	private static String TAG = ScreenSplash.class.getCanonicalName();
	
	public ScreenSplash() {
		super(SCREEN_TYPE.SPLASH_T, TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_splash);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				if(!ServiceManager.isStarted()){
					Log.d(TAG, "Try to start service manager");
					ServiceManager.initialize();
					ServiceManager.start();
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {
//						Log.e(TAG, e.toString());
//					}
					ServiceManager.getScreenService().runOnUiThread(new Runnable(){
						@Override
						public void run() {
							//ServiceManager.getScreenService().show(ScreenWelcome.class);
							ServiceManager.getScreenService().show(ScreenHome.class);
							//ServiceManager.getScreenService().show(ScreenAV.class);
						}
					});
					finish();
				}
			}
		}).start();
	}
}
