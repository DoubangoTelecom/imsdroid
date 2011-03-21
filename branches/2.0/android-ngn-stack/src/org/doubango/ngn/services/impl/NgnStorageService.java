
package org.doubango.ngn.services.impl;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.services.INgnStorageService;

import android.util.Log;

public class NgnStorageService  extends NgnBaseService implements INgnStorageService{
	private final static String TAG = NgnStorageService.class.getCanonicalName();
	
	private final String mCurrentDir;
	private final String mContentShareDir;
	
	public NgnStorageService(){
		mCurrentDir = String.format("/data/data/%s", NgnApplication.getContext().getPackageName());
		mContentShareDir = "/sdcard/wiPhone";
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "starting...");
		return true;
	}
	
	@Override
	public boolean stop() {
		Log.d(TAG, "stopping...");
		return true;
	}
	
	@Override
	public String getCurrentDir(){
		return this.mCurrentDir;
	}
	
	@Override
	public String getContentShareDir(){
		return this.mContentShareDir;
	}
}
