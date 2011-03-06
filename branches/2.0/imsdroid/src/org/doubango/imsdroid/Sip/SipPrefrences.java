package org.doubango.imsdroid.Sip;

public class SipPrefrences {
	private boolean mPresence;
	private boolean mXcapEnabled;
	private boolean mPresenceRLS;
	private boolean mPresencePub;
    private boolean mPresenceSub;
    private boolean mMWI;
    private String mIMPI;
    private String mIMPU;
    private String mRealm;
    private String mPcscfHost;
    private int mPcscfPort;
    private String mTransport;
    private String mIPVersion;
    private boolean mIPsecSecAgree;
    private String mLocalIP;
    private boolean mHackAoR;
    
    public SipPrefrences(){
    	
    }
    
    public void setPresenceEnabled(boolean enabled) {
		this.mPresence = enabled;
	}

	public boolean isPresenceEnabled() {
		return mPresence;
	}
	
	public void setXcapEnabled(boolean xcapEnabled) {
		this.mXcapEnabled = xcapEnabled;
	}
	
	public boolean isXcapEnabled() {
		return mXcapEnabled;
	}
	
	public void setPresenceRLS(boolean presenceRLS) {
		this.mPresenceRLS = presenceRLS;
	}
	
	public boolean isPresenceRLS() {
		return mPresenceRLS;
	}
	
	public void setMWI(boolean MWI) {
		this.mMWI = MWI;
	}
	
	public boolean isMWI() {
		return mMWI;
	}
	
	public void setPresencePub(boolean presencePub) {
		this.mPresencePub = presencePub;
	}
	
	public boolean isPresencePub() {
		return mPresencePub;
	}
	
	public void setIMPI(String IMPI) {
		this.mIMPI = IMPI;
	}
	
	public String getIMPI() {
		return mIMPI;
	}
	
	public void setIMPU(String IMPU) {
		this.mIMPU = IMPU;
	}
	
	public String getIMPU() {
		return mIMPU;
	}
	
	public void setRealm(String realm) {
		if((mRealm = realm) != null){
			if(!mRealm.contains(":")){ // sip:,sips:,...
				mRealm="sip:"+mRealm;
			}
		}
	}
	
	public String getRealm() {
		return mRealm;
	}
	
	public void setPresenceSub(boolean presenceSub) {
		this.mPresenceSub = presenceSub;
	}
	
	public boolean isPresenceSub() {
		return mPresenceSub;
	}
	
	
	public void setPcscfHost(String pcscfHost) {
		this.mPcscfHost = pcscfHost;
	}
	
	public String getPcscfHost() {
		return mPcscfHost;
	}
	
	public void setPcscfPort(int pcscfPort) {
		this.mPcscfPort = pcscfPort;
	}
	
	public int getPcscfPort() {
		return mPcscfPort;
	}
	
	public void setIPVersion(String IPVersion) {
		this.mIPVersion = IPVersion;
	}
	
	public String getIPVersion() {
		return mIPVersion;
	}
	
	public void setTransport(String mTransport) {
		this.mTransport = mTransport;
	}
	public String getTransport() {
		return mTransport;
	}
	
	public void setIPsecSecAgree(boolean IPsecSecAgree) {
		this.mIPsecSecAgree = IPsecSecAgree;
	}
	
	public boolean isIPsecSecAgree() {
		return mIPsecSecAgree;
	}

	public void setLocalIP(String localIP) {
		this.mLocalIP = localIP;
	}

	public String getLocalIP() {
		return mLocalIP;
	}

	public void setHackAoR(boolean mHackAoR) {
		this.mHackAoR = mHackAoR;
	}

	public boolean isHackAoR() {
		return mHackAoR;
	}
	
}
