package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.events.ICallEventDispatcher;
import org.doubango.imsdroid.events.IRegistrationEventDispatcher;
import org.doubango.imsdroid.events.ISubscriptionEventDispatcher;
import org.doubango.imsdroid.sip.MySipStack;

public interface ISipService  extends IService, 
IRegistrationEventDispatcher, 
ISubscriptionEventDispatcher,
ICallEventDispatcher
{
	MySipStack getStack();
	
	byte[] getReginfo();
	byte[] getWinfo();
	
	boolean register();
	boolean unregister();
	
	boolean publish();
	
	boolean isRegistered();
}
