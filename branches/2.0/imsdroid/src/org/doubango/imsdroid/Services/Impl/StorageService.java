package org.doubango.imsdroid.Services.Impl;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.Services.IStorageService;


public class StorageService  extends BaseService implements IStorageService{

	private final String mCurrentDir;
	private final String mContentShareDir;
	
	public StorageService(){
		this.mCurrentDir = String.format("/data/data/%s", IMSDroid.getContext().getPackageName());
		this.mContentShareDir = "/sdcard/IMSDroid";
	}
	
	@Override
	public boolean start() {
		return true;
	}
	
	@Override
	public boolean stop() {
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
