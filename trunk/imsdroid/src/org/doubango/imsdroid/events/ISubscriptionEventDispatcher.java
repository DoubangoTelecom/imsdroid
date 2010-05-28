package org.doubango.imsdroid.events;

public interface ISubscriptionEventDispatcher /*extends IEventDispatcher<ISubscriptionEventHandler>*/{
	boolean addSubscriptionEventHandler(ISubscriptionEventHandler handler);
	boolean removeSubscriptionEventHandler(ISubscriptionEventHandler handler);
}