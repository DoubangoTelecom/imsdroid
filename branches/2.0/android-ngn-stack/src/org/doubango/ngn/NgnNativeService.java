package org.doubango.ngn;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NgnNativeService extends Service {
	private final static String TAG = NgnNativeService.class.getCanonicalName();
	
	public NgnNativeService(){
		super();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "onBind()");
		return null;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind()");
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart()");
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
	}
}
