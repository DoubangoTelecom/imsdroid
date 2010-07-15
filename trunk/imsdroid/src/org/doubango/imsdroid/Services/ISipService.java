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
package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.events.ICallEventDispatcher;
import org.doubango.imsdroid.events.IRegistrationEventDispatcher;
import org.doubango.imsdroid.events.ISubscriptionEventDispatcher;
import org.doubango.imsdroid.sip.MySipStack;
import org.doubango.imsdroid.sip.MySubscriptionSession;
import org.doubango.imsdroid.sip.MySubscriptionSession.EVENT_PACKAGE_TYPE;

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
	
	MySubscriptionSession createPresenceSession(String toUri, EVENT_PACKAGE_TYPE eventPackage);
	void clearPresenceSessions();
	void removePresenceSession(MySubscriptionSession session);
}
