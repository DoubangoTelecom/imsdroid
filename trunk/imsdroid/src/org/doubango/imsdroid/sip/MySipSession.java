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
		this.getSession().setExpires(this.configurationService.getInt(CONFIGURATION_SECTION.QOS, CONFIGURATION_ENTRY.SIP_SESSIONS_TIMEOUT, Configuration.DEFAULT_SIP_SESSIONS_TIMEOUT));
		
		// Sip Headers (common to all sessions)
		this.getSession().addCaps("+g.oma.sip-im");
		this.getSession().addCaps("language", "\"en,fr\"");
	}
	
	protected abstract SipSession getSession();
}
