package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.events.IRegistrationEventDispatcher;
import org.doubango.imsdroid.events.ISubscriptionEventDispatcher;

public interface ISipService  extends IService, 
IRegistrationEventDispatcher, 
ISubscriptionEventDispatcher
{

	boolean register();
	boolean unregister();
	
	boolean isRegistered();
}
