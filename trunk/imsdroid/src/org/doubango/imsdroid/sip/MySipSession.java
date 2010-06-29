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

package org.doubango.imsdroid.sip;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.tinyWRAP.SipSession;

public abstract class MySipSession {
	
	// Services
	protected final IConfigurationService configurationService;
	
	//
	protected boolean connected;
	protected String fromUri;
	protected String toUri;
	
	public MySipSession() {
				
		this.configurationService = ServiceManager.getConfigurationService();
		/* init must be called by the child class after session_create() */
		/* this.init(); */
	}
	
	public long getId(){
		return this.getSession().getId();
	}
	
	public boolean isConnected(){
		return this.connected;
	}
	
	public void setConnected(boolean connected){
		this.connected = connected;
	}
	
	public String getFromUri(){
		return this.fromUri;
	}
	
	public boolean setFromUri(String fromUri){
		boolean ret = this.getSession().setFromUri(fromUri);
		this.fromUri = fromUri;
		return ret;
	}
	
	public String getToUri(){
		return this.toUri;
	}
	
	public boolean setToUri(String toUri){
		boolean ret = this.getSession().setToUri(fromUri);
		this.toUri = toUri;
		return ret;
	}
	
	protected void init(){
		// Sip Expires
		this.getSession().setExpires(this.configurationService.getInt(CONFIGURATION_SECTION.QOS, CONFIGURATION_ENTRY.SIP_SESSIONS_TIMEOUT, Configuration.DEFAULT_QOS_SIP_SESSIONS_TIMEOUT));
		
		// Sip Headers (common to all sessions)
		this.getSession().addCaps("+g.oma.sip-im");
		this.getSession().addCaps("language", "\"en,fr\"");
	}
	
	protected abstract SipSession getSession();
}
