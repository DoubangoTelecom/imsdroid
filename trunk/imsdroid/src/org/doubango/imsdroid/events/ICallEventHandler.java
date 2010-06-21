package org.doubango.imsdroid.events;

public interface ICallEventHandler /* extends IEventHandler<CallEventArgs> */{
	boolean onCallEvent(Object sender, CallEventArgs e);
}
