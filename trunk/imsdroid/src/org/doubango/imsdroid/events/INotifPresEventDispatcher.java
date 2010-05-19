package org.doubango.imsdroid.events;

public interface INotifPresEventDispatcher  /* extends IEventDispatcher<INotifPresEventhandler> */{
	boolean addNotifPresEventhandler(INotifPresEventhandler handler);
	boolean removeNotifPresEventhandler(INotifPresEventhandler handler);
}
