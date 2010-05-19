package org.doubango.imsdroid.events;

public interface INotifPresEventhandler /* extends IEventHandler<NotifPresEventArgs> */{
	boolean onNotifPresEvent(Object sender, NotifPresEventArgs e);
}