package org.doubango.imsdroid.events;

public interface ISubscriptionEventHandler /* extends IEventHandler<SubscriptionEventArgs> */{
	boolean onSubscriptionEvent(Object sender, SubscriptionEventArgs e);
}
