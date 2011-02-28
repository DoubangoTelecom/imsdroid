
package org.doubango.imsdroid.Services.Impl;

import java.io.File;
import java.io.IOException;
import java.util.List;


import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Model.HistoryEvent;
import org.doubango.imsdroid.Model.HistoryList;
import org.doubango.imsdroid.Services.IHistoryService;
import org.doubango.imsdroid.Utils.ObservableList;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class HistoryService extends BaseService implements IHistoryService {
	private final static String TAG = HistoryService.class.getCanonicalName();
	private final static String HISTORY_FILE = "history.xml";
	
	private File mHistoryFile;
	private HistoryList mEventsList;
	private final Serializer mSerializer;
	private boolean mLoadingHistory;
	
	public HistoryService(){
		super();
		
		mSerializer = new Persister();
		mEventsList = new HistoryList();
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "Starting...");
		boolean result = true;
		
		/*	http://code.google.com/p/dalvik/wiki/JavaxPackages
	     * Ensure the factory implementation is loaded from the application
	     * classpath (which contains the implementation classes), rather than the
	     * system classpath (which doesn't).
	     */
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		
		mHistoryFile = new File(String.format("%s/%s", ServiceManager.getStorageService().getCurrentDir(), HistoryService.HISTORY_FILE));
		if(!mHistoryFile.exists()){
			try {
				mHistoryFile.createNewFile();
				result = compute(); /* to create an empty but valid document */
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				mHistoryFile = null;
				result =  false;
			}
		}
		
//		for(int i=0; i<20; i++){
//			HistoryAVCallEvent e = new HistoryAVCallEvent(i%2==0, "sip:" + i + "@open-ims.test");
//			mEventsList.addEvent(e);
//		}
//		compute();
		
		return result;
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "Stopping");
		return true;
	}

	@Override
	public boolean load(){
		boolean result = true;
		
		try {
			mLoadingHistory = true;
			Log.d(TAG, "Loading history");
			mEventsList = mSerializer.read(mEventsList.getClass(), mHistoryFile);
			Log.d(TAG, "History loaded");
		} catch (Exception ex) {
			Log.e(TAG, ex.toString());
			result = false;
		}
		mLoadingHistory = false;
		return result;
	}
	
	@Override
	public boolean isLoading() {
		return mLoadingHistory;
	}

	@Override
	public void addEvent(HistoryEvent event) {
		mEventsList.addEvent(event);
		new Thread(new Runnable(){
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public void updateEvent(HistoryEvent event) {
		Log.e(TAG, "Not impleented");
		//throw new Exception("Not implemented");
	}

	@Override
	public void deleteEvent(HistoryEvent event) {
		mEventsList.removeEvent(event);
		new Thread(new Runnable(){
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public void deleteEvent(int location) {
		mEventsList.removeEvent(location);
		new Thread(new Runnable(){
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public void clear() {
		mEventsList.clear();
		new Thread(new Runnable(){
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public List<HistoryEvent> getEvents() {
		return mEventsList.getList().getList();
	}
	
	@Override
	public ObservableList<HistoryEvent> getObservableEvents() {
		return mEventsList.getList();
	}
	
	private boolean compute(){
		if(mHistoryFile == null || mSerializer == null){
			Log.e(TAG, "Invalid arguments");
			return false;
		}
		try{
			mSerializer.write(mEventsList, mHistoryFile);
		}
		catch (Exception e) {
			Log.e(TAG, e.toString());
			return false;
		}
		return true;
	}
}
