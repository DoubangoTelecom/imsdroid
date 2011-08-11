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
package org.doubango.imsdroid.events;

import java.util.concurrent.CopyOnWriteArrayList;


public abstract class EventHandler/*<TEventArgs> implements IEventHandler<TEventArgs>*/
{

//	private final ArrayList<IEventHandler<TEventArgs>> handlers;
//	
//	public EventHandler()
//	{
//		this.handlers = new ArrayList<IEventHandler<TEventArgs>>();
//	}
//	
//	public void add(IEventHandler<TEventArgs> handler)
//	{
//		this.handlers.add(handler);
//	}
//	
//	public void remove(IEventHandler<TEventArgs> handler)
//	{
//		this.handlers.remove(handler);
//	}
	
	public static <TEventHandler> boolean addEventHandler(
			CopyOnWriteArrayList<TEventHandler> eventHandlers, TEventHandler eventHandler) {
		if (eventHandler != null && eventHandlers != null && !eventHandlers.contains(eventHandler)) {
			return eventHandlers.add(eventHandler);
		}
		return false;
	}
	
	public static <TEventHandler> boolean removeEventHandler(
			CopyOnWriteArrayList<TEventHandler> eventHandlers, TEventHandler eventHandler) {
		if (eventHandler != null && eventHandlers != null) {
			return eventHandlers.remove(eventHandler);
		}
		return false;
	}
}
