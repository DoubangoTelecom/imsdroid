package org.doubango.imsdroid.Sevices.Impl;

import org.doubango.imsdroid.Main;
import org.doubango.imsdroid.Services.IStorageService;

public class StorageService  extends Service implements IStorageService{

	private final String currentDir;
	
	public StorageService(){
		this.currentDir = String.format("/data/data/%s", Main.class.getPackage().getName());
	}
	
	public boolean start() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public boolean stop() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public String getCurrentDir(){
		return this.currentDir;
	}
}
