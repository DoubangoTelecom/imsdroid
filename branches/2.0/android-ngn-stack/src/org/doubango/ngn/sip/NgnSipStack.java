package org.doubango.ngn.sip;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.R;
import org.doubango.ngn.services.INgnNetworkService;
import org.doubango.ngn.services.impl.NgnNetworkService.DNS_TYPE;
import org.doubango.tinyWRAP.SipCallback;
import org.doubango.tinyWRAP.SipStack;

public class NgnSipStack extends SipStack{

	public enum STACK_STATE {
	     NONE, STARTING, STARTED, STOPPING, STOPPED
	}
	
	private STACK_STATE mState = STACK_STATE.NONE;
	private String mCompId;
	private final INgnNetworkService mNetworkService;
	
	public NgnSipStack(SipCallback callback, String realmUri, String impiUri, String impuUri){
		super(callback, realmUri, impiUri, impuUri);
		
		// Services
		mNetworkService = NgnEngine.getInstance().getNetworkService();
		
		// Set first and second DNS servers (used for DNS NAPTR+SRV discovery and ENUM)
		String dnsServer;
		if((dnsServer = mNetworkService.getDnsServer(DNS_TYPE.DNS_1)) != null && !dnsServer.equals("0.0.0.0")){
			this.addDnsServer(dnsServer);
			if((dnsServer = mNetworkService.getDnsServer(DNS_TYPE.DNS_2)) != null && !dnsServer.equals("0.0.0.0")){
				this.addDnsServer(dnsServer);
			}
		}
		else{
			// On the emulator FIXME
			this.addDnsServer("212.27.40.241");
		}
		
	     // Sip headers
        super.addHeader("Allow", "INVITE, ACK, CANCEL, BYE, MESSAGE, OPTIONS, NOTIFY, PRACK, UPDATE, REFER");
        super.addHeader("Privacy", "none");
        super.addHeader("P-Access-Network-Info", "ADSL;utran-cell-id-3gpp=00000000");
        super.addHeader("User-Agent", String.format("IM-client/OMA1.0 android-ngn-stack/v%s (doubango r%s)", 
				NgnApplication.getVersionName(), 
				NgnApplication.getContext().getString(R.string.doubango_revision)));
	}

	@Override
	public boolean start() {
		if(mNetworkService.acquire()){
			mState = STACK_STATE.STARTING;
			return super.start();
		}
		else{
			return false;
		}
	}

	@Override
	public boolean stop() {
		mNetworkService.release();
		mState = STACK_STATE.STOPPING;
		return super.stop();
	}
	
	public void setState(STACK_STATE state){
		mState = state;
	}
	
	public STACK_STATE getState(){
		return mState;
	}
	
	public String getSigCompId(){
		return mCompId;
	}
	
	public void setSigCompId(String compId){
		if(mCompId != null && mCompId != compId){
			super.removeSigCompCompartment(mCompId);
		}
		if((mCompId = compId) != null){
			super.addSigCompCompartment(mCompId);
		}
	}
}
