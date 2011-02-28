package org.doubango.imsdroid.Sip;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Services.Impl.NetworkService.DNS_TYPE;
import org.doubango.tinyWRAP.SipCallback;
import org.doubango.tinyWRAP.SipStack;

public class MySipStack extends SipStack{

	public enum STACK_STATE {
	     NONE, STARTING, STARTED, STOPPING, STOPPED
	}
	
	private STACK_STATE mState = STACK_STATE.NONE;
	private String mCompId;
	private final INetworkService mNetworkService;
	
	public MySipStack(SipCallback callback, String realmUri, String impiUri, String impuUri){
		super(callback, realmUri, impiUri, impuUri);
		
		// Services
		mNetworkService = ServiceManager.getNetworkService();
		
		// Set first and second DNS servers (used for DNS NAPTR+SRV discovery and ENUM)
		String dnsServer;
		if((dnsServer = this.mNetworkService.getDnsServer(DNS_TYPE.DNS_1)) != null && !dnsServer.equals("0.0.0.0")){
			this.addDnsServer(dnsServer);
			if((dnsServer = this.mNetworkService.getDnsServer(DNS_TYPE.DNS_2)) != null && !dnsServer.equals("0.0.0.0")){
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
        super.addHeader("User-Agent", String.format("IM-client/OMA1.0 IMSDroid/v%s (doubango r%s)", 
				IMSDroid.getVersionName(), 
				IMSDroid.getContext().getString(R.string.doubango_revision)));
	
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
