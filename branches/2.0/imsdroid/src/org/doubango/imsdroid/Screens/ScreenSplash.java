package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class ScreenSplash extends BaseScreen {
	private static String TAG = ScreenSplash.class.getCanonicalName();
	
	private BroadcastReceiver mBroadCastRecv;
	
	public ScreenSplash() {
		super(SCREEN_TYPE.SPLASH_T, TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_splash);
		
		mBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				Log.d(TAG, "onReceive()");
				
				if(ServiceManager.ACTION_STATE_EVENT.equals(action)){
					if(intent.getBooleanExtra("started", false)){
						ServiceManager.getScreenService().show(ScreenHome.class);
						finish();
					}
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ServiceManager.ACTION_STATE_EVENT);
	    registerReceiver(mBroadCastRecv, intentFilter);
	}
	
	@Override
	protected void onDestroy() {
		if(mBroadCastRecv != null){
			unregisterReceiver(mBroadCastRecv);
		}
		super.onDestroy();
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
				}
			}
		}).start();
	}
}
