package org.doubango.imsdroid.sip;

import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.imsdroid.Sevices.Impl.NetworkService.DNS_TYPE;
import org.doubango.tinyWRAP.SipCallback;
import org.doubango.tinyWRAP.SipStack;

public class MySipStack extends SipStack {
	
	public static enum STACK_STATE {
		NONE, STARTING, STARTED, STOPPING, STOPPED
	}
	private final INetworkService networkService;
	private STACK_STATE state = STACK_STATE.NONE;
		
	public MySipStack(SipCallback callback, String realmUri, String impiUri, String impuUri) {
		super(callback, realmUri, impiUri, impuUri);
		
		// Services
		this.networkService = ServiceManager.getNetworkService();
		
		// Set first and second DNS servers (used for DNS NAPTR+SRV discovery and ENUM)
		String dnsServer;
		if((dnsServer = this.networkService.getDnsServer(DNS_TYPE.DNS_1)) != null){
			this.addDnsServer(dnsServer);
		}
		if((dnsServer = this.networkService.getDnsServer(DNS_TYPE.DNS_2)) != null){
			this.addDnsServer(dnsServer);
		}
		
		// Sip headers
		this.addHeader("Allow", "INVITE, ACK, CANCEL, BYE, MESSAGE, OPTIONS, NOTIFY, PRACK, UPDATE, REFER");
		this.addHeader("Privacy", "header;id");
		this.addHeader("P-Access-Network-Info", "ADSL;utran-cell-id-3gpp=00000000");
		this.addHeader("User-Agent", "IM-client/OMA1.0 imsdroid/v1.0.0"); // Get version from ConfigurationService or strings res
	}

	@Override
	public boolean start() {
		this.state = STACK_STATE.STARTING;
		return super.start();
	}

	@Override
	public boolean stop() {
		this.state = STACK_STATE.STOPPING;
		return super.stop();
	}
	
	public void setState(STACK_STATE state){
		this.state = state;
	}
	
	public STACK_STATE getState(){
		return this.state;
	}
}
