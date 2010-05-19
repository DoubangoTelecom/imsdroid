package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.events.INotifPresEventDispatcher;
import org.doubango.imsdroid.events.IRegistrationEventDispatcher;

public interface ISipService  extends IService, 
IRegistrationEventDispatcher, INotifPresEventDispatcher{

	// For tests only
	void onTestRegistrationChanged();
	void onTestNotifPresChanged();
}
