/*
* Copyright (C) 2010 Mamadou Diop.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango.org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*
*/
package org.doubango.imsdroid.Services.Impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.doubango.imsdroid.Model.HistoryEvent;
import org.doubango.imsdroid.Model.HistoryList;
import org.doubango.imsdroid.Services.IHistoryService;
import org.doubango.imsdroid.events.EventHandler;
import org.doubango.imsdroid.events.HistoryEventArgs;
import org.doubango.imsdroid.events.HistoryEventTypes;
import org.doubango.imsdroid.events.IHistoryEventHandler;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.RegistrationEventArgs;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class HistoryService implements IHistoryService ,IRegistrationEventHandler{

	private final static String TAG = HistoryService.class.getCanonicalName();
	
	private final static String HISTORY_FILE = "history.xml";
	
	// Event Handlers
	private final CopyOnWriteArrayList<IHistoryEventHandler> historyEventHandlers;
	
	private File history_file;
	private HistoryList events;
	private final Serializer serializer;
	private boolean loadingHistory;
	
	public HistoryService(){
		super();
		
		this.serializer = new Persister();
		this.events = new HistoryList();
		this.historyEventHandlers = new CopyOnWriteArrayList<IHistoryEventHandler>();
		
		
		//for(int i=0; i<20; i++){
		//	HistoryAVCallEvent e = new HistoryAVCallEvent(i%2==0, "sip:" + i + "@open-ims.test");
		//	this.events.addEvent(e);
		//}
	}
	
	@Override
	public boolean start() {
		// Creates history file if does not exist
		this.history_file = new File(String.format("%s/%s", ServiceManager.getStorageService().getCurrentDir(), HistoryService.HISTORY_FILE));
		if(!this.history_file.exists()){
			try {
				this.history_file.createNewFile();
				this.compute(); /* to create an empty but valid document */
			} catch (IOException e) {
				e.printStackTrace();
				this.history_file = null;
				return false;
			}
		}
		
		// add sip event handlers
		ServiceManager.getSipService().addRegistrationEventHandler(this);
		return true;
	}

	@Override
	public boolean stop() {
		// remove sip event handlers
		ServiceManager.getSipService().removeRegistrationEventHandler(this);
		return true;
	}
	
	@Override
	public boolean isLoadingHistory(){
		return this.loadingHistory;
	}
	
	@Override
	public void addEvent(HistoryEvent event) {
		this.events.addEvent(event);
		new Thread(new Runnable(){
			@Override
			public void run() {
				HistoryService.this.compute();
				HistoryService.this.onHistoryEvent(new HistoryEventArgs(HistoryEventTypes.EVENT_ADDED, -1));
			}
		}).start();
	}

	@Override
	public void deleteEvent(HistoryEvent event) {
		this.events.removeEvent(event);
		new Thread(new Runnable(){
			@Override
			public void run() {
				HistoryService.this.compute();
				HistoryService.this.onHistoryEvent(new HistoryEventArgs(HistoryEventTypes.EVENT_REMOVED, -1));
			}
		}).start();
	}
	
	@Override
	public void deleteEvent(final int location){
		this.events.removeEvent(location);
		new Thread(new Runnable(){
			@Override
			public void run() {
				HistoryService.this.compute();
				HistoryService.this.onHistoryEvent(new HistoryEventArgs(HistoryEventTypes.EVENT_REMOVED, location));
			}
		}).start();
	}

	@Override
	public void clear(){
		this.events.clear();
		new Thread(new Runnable(){
			@Override
			public void run() {
				HistoryService.this.compute();
				HistoryService.this.onHistoryEvent(new HistoryEventArgs(HistoryEventTypes.EVENT_ALL_RESET, -1));
			}
		}).start();
	}
	
	@Override
	public void updateEvent(HistoryEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<HistoryEvent> getEvents() {
		return this.events.getList();
	}
	
	/* ===================== Sip Events ========================*/
	public boolean onRegistrationEvent(Object sender, RegistrationEventArgs e) {
		switch(e.getType()){
			case REGISTRATION_OK:
				// Already in it's own thread
				try {
					this.loadingHistory = true;
					Log.d(HistoryService.TAG, "Loading history");
					this.events = this.serializer.read(this.events.getClass(), this.history_file);
					Log.d(HistoryService.TAG, "History loaded");
				} catch (Exception ex) {
					Log.e(HistoryService.TAG, "Failed to load History");
					ex.printStackTrace();
				}
				this.loadingHistory = false;
				
				this.onHistoryEvent(new HistoryEventArgs(HistoryEventTypes.EVENT_ALL_RESET, -1));
				break;
			case REGISTRATION_NOK:
			case REGISTRATION_INPROGRESS:
			case UNREGISTRATION_OK:
			case UNREGISTRATION_NOK:
			case UNREGISTRATION_INPROGRESS:
				break;
		}
		return true;
	}
	
	/* ===================== Add/Remove handlers ======================== */
	@Override
	public boolean addHistoryEventHandler(IHistoryEventHandler handler) {
		return EventHandler.addEventHandler(this.historyEventHandlers, handler);
	}

	@Override
	public boolean removeHistoryEventHandler(IHistoryEventHandler handler) {
		return EventHandler.removeEventHandler(this.historyEventHandlers, handler);
	}
	
	/* ===================== Dispatch events ======================== */
	private synchronized void onHistoryEvent(final HistoryEventArgs eargs) {
		for(final IHistoryEventHandler handler : this.historyEventHandlers){
			new Thread(new Runnable() {
				public void run() {
					if (!handler.onHistoryEvent(this, eargs)) {
						Log.w(handler.getClass().getName(), "onHistoryEvent failed");
					}
				}
			}).start();
		}
	}
	
	
	private boolean compute(){
		if(this.history_file == null){
			return false;
		}
		try{
			this.serializer.write(this.events, this.history_file);
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
