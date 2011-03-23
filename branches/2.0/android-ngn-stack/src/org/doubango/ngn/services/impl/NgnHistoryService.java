package org.doubango.ngn.services.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryList;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.utils.NgnObservableList;
import org.doubango.ngn.utils.NgnPredicate;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;


public class NgnHistoryService extends NgnBaseService implements INgnHistoryService {
	private final static String TAG = NgnHistoryService.class.getCanonicalName();
	private final static String HISTORY_FILE = "history.xml";
	
	private File mHistoryFile;
	private NgnHistoryList mEventsList;
	private final Serializer mSerializer;
	private boolean mLoadingHistory;
	
	public NgnHistoryService(){
		super();
		
		mSerializer = new Persister();
		mEventsList = new NgnHistoryList();
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
		
		mHistoryFile = new File(String.format("%s/%s", NgnEngine.getInstance().getStorageService().getCurrentDir(), NgnHistoryService.HISTORY_FILE));
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
			ex.printStackTrace();
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
	public void addEvent(NgnHistoryEvent event) {
		mEventsList.addEvent(event);
		new Thread(new Runnable(){
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public void updateEvent(NgnHistoryEvent event) {
		Log.e(TAG, "Not impleented");
		//throw new Exception("Not implemented");
	}

	@Override
	public void deleteEvent(NgnHistoryEvent event) {
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
	public void deleteEvents(NgnPredicate<NgnHistoryEvent> predicate){
		mEventsList.removeEvents(predicate);
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
	public List<NgnHistoryEvent> getEvents() {
		return mEventsList.getList().getList();
	}
	
	@Override
	public NgnObservableList<NgnHistoryEvent> getObservableEvents() {
		return mEventsList.getList();
	}
	
	private synchronized boolean compute(){
		synchronized(this){
			if(mHistoryFile == null || mSerializer == null){
				Log.e(TAG, "Invalid arguments");
				return false;
			}
			try{
				mSerializer.write(mEventsList, mHistoryFile);
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}
}
