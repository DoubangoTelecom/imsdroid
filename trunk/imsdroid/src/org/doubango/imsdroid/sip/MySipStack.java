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

import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.Services.Impl.NetworkService.DNS_TYPE;
import org.doubango.tinyWRAP.SipCallback;
import org.doubango.tinyWRAP.SipStack;

public class MySipStack extends SipStack {
	
	public static enum STACK_STATE {
		NONE, STARTING, STARTED, STOPPING, STOPPED
	}
	private final INetworkService networkService;
	private STACK_STATE state = STACK_STATE.NONE;
	private String compId;
		
	public MySipStack(SipCallback callback, String realmUri, String impiUri, String impuUri) {
		super(callback, realmUri, impiUri, impuUri);
		
		// Services
		this.networkService = ServiceManager.getNetworkService();
		
		// Set first and second DNS servers (used for DNS NAPTR+SRV discovery and ENUM)
		String dnsServer;
		if((dnsServer = this.networkService.getDnsServer(DNS_TYPE.DNS_1)) != null && !dnsServer.equals("0.0.0.0")){
			this.addDnsServer(dnsServer);
			if((dnsServer = this.networkService.getDnsServer(DNS_TYPE.DNS_2)) != null && !dnsServer.equals("0.0.0.0")){
				this.addDnsServer(dnsServer);
			}
		}
		else{
			// On the emulator FIXME
			this.addDnsServer("212.27.40.241");
		}
		
		// Sip headers
		this.addHeader("Allow", "INVITE, ACK, CANCEL, BYE, MESSAGE, OPTIONS, NOTIFY, PRACK, UPDATE, REFER");
		this.addHeader("Privacy", "none");
		this.addHeader("P-Access-Network-Info", "ADSL;utran-cell-id-3gpp=00000000");
		//--this.addHeader("User-Agent", String.format("IM-client/OMA1.0 IMSDroid/v%s (doubango r%s)", IMSDroid.getContext().getString(R.string.Version), IMSDroid.getContext().getString(R.string.doubango_revision)));
	}

	@Override
	public boolean start() {
		if(this.networkService.acquire()){
			this.state = STACK_STATE.STARTING;
			
			return super.start();
		}
		else{
			return false;
		}
	}

	@Override
	public boolean stop() {
		this.networkService.release();
		this.state = STACK_STATE.STOPPING;
		return super.stop();
	}
	
	public void setState(STACK_STATE state){
		this.state = state;
	}
	
	public STACK_STATE getState(){
		return this.state;
	}
	
	public String getSigCompId(){
		return this.compId;
	}
	
	public void setSigCompId(String compId){
		if(this.compId != null && this.compId != compId){
			this.removeSigCompCompartment(this.compId);
		}
		if((this.compId = compId) != null){
			this.addSigCompCompartment(this.compId);
		}
	}
}
