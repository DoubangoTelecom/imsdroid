package org.doubango.imsdroid.Sevices.Impl;

import java.util.concurrent.CopyOnWriteArrayList;

import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.events.EventHandler;
import org.doubango.imsdroid.events.INotifPresEventhandler;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.NotifPresEventArgs;
import org.doubango.imsdroid.events.RegistrationEventArgs;
import org.doubango.imsdroid.events.RegistrationEventTypes;

import android.util.Log;

public class SipService extends Service implements ISipService {

	// Event Handlers
	private final CopyOnWriteArrayList<IRegistrationEventHandler> registrationEventHandlers;
	private final CopyOnWriteArrayList<INotifPresEventhandler> notifPresEventhandler;

	public SipService() {
		super();

		this.registrationEventHandlers = new CopyOnWriteArrayList<IRegistrationEventHandler>();
		this.notifPresEventhandler = new CopyOnWriteArrayList<INotifPresEventhandler>();
	}

	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addRegistrationEventHandler(IRegistrationEventHandler handler) 
	{
		return EventHandler.addEventHandler(this.registrationEventHandlers,handler);
	}

	public boolean removeRegistrationEventHandler(IRegistrationEventHandler handler) 
	{
		return EventHandler.removeEventHandler(this.registrationEventHandlers, handler);
	}

	public boolean addNotifPresEventhandler(INotifPresEventhandler handler) 
	{
		return EventHandler.addEventHandler(this.notifPresEventhandler, handler);
	}

	public boolean removeNotifPresEventhandler(INotifPresEventhandler handler) 
	{
		return EventHandler.removeEventHandler(this.notifPresEventhandler, handler);
	}
	
	// should be private
	public void onTestRegistrationChanged()
	{
		RegistrationEventArgs e = new RegistrationEventArgs(RegistrationEventTypes.REGISTRATION_FAILED, (short)403, "Not Authorized");
		for(IRegistrationEventHandler handler : this.registrationEventHandlers)
		{
			if(!handler.onRegistrationEvent(this, e))
			{
				Log.w(handler.getClass().getName(), "onRegistrationEvent failed");
			}
		}
	}
	
	// should be private
	public void onTestNotifPresChanged()
	{
		NotifPresEventArgs e = new NotifPresEventArgs();
		for(INotifPresEventhandler handler : this.notifPresEventhandler)
		{
			if(!handler.onNotifPresEvent(this, e))
			{
				Log.w(handler.getClass().getName(), "onNotifPresEvent failed");
			}
		}
	}
}
