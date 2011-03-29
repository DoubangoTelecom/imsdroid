package org.doubango.ngn.sip;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.tinyWRAP.RegistrationSession;
import org.doubango.tinyWRAP.SipSession;

/**
 * Registration state
 */
public class NgnRegistrationSession extends NgnSipSession {

	private final RegistrationSession mSession;

	/**
	 * Creates new registration session
	 * @param sipStack the stack to use to create the session
	 */
    public NgnRegistrationSession(NgnSipStack sipStack){
    	super(sipStack);
        mSession = new RegistrationSession(sipStack);

        super.init();
        super.setSigCompId(sipStack.getSigCompId());
        
        mSession.setExpires(NgnEngine.getInstance().getConfigurationService().getInt(NgnConfigurationEntry.NETWORK_REGISTRATION_TIMEOUT, 
        		NgnConfigurationEntry.DEFAULT_NETWORK_REGISTRATION_TIMEOUT));
        
        /* support for 3GPP SMS over IP */
        super.addCaps("+g.3gpp.smsip");
        /* support for OMA Large message (as per OMA SIMPLE IM v1) */
        super.addCaps("+g.oma.sip-im.large-message");

        /* 3GPP TS 24.173
        *
        * 5.1 IMS communication service identifier
        * URN used to define the ICSI for the IMS Multimedia Telephony Communication Service: urn:urn-7:3gpp-service.ims.icsi.mmtel. 
        * The URN is registered at http://www.3gpp.com/Uniform-Resource-Name-URN-list.html.
        * Summary of the URN: This URN indicates that the device supports the IMS Multimedia Telephony Communication Service.
        *
        * 5.2 Session control procedures
        * The multimedia telephony participant shall include the g.3gpp. icsi-ref feature tag equal to the ICSI value defined 
        * in subclause 5.1 in the Contact header field in initial requests and responses as described in 3GPP TS 24.229 [13].
        */
        /* GSMA RCS phase 3 - 3.2 Registration */
        super.addCaps("audio");
        super.addCaps("+g.3gpp.icsi-ref", "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"");
        super.addCaps("+g.3gpp.icsi-ref", "\"urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-vs\"");
        // In addition, in RCS Release 3 the BA Client when used as a primary device will indicate the capability to receive SMS 
        // messages over IMS by registering the SMS over IP feature tag in accordance with [24.341]:
        super.addCaps("+g.3gpp.cs-voice");
    }

    /**
     * Sends SIP REGISTER request
     * @return true if succeed and false otherwise
     */
    public boolean register(){
        return mSession.register_();
    }

    /**
     * Unregisters (SIP REGISTER with expires=0)
     * @return true if succeed and false otherwise
     */
    public boolean unregister(){
        return mSession.unRegister();
    }

	@Override
	protected SipSession getSession() {
		return mSession;
	}
}
