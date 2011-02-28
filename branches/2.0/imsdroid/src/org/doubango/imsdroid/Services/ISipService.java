
package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.Sip.MySipStack;
import org.doubango.imsdroid.Sip.PresenceStatus;

public interface ISipService extends IBaseService {
	String getDefaultIdentity();
	void setDefaultIdentity(String identity);
    MySipStack getSipStack();
    boolean isRegistered();
    boolean isXcapEnabled();
    boolean isPublicationEnabled();
    boolean isSubscriptionEnabled();
    boolean isSubscriptionToRLSEnabled();
    int getCodecs();
    void setCodecs(int coddecs);

    byte[] getSubRLSContent();
    byte[] getSubRegContent();
    byte[] getSubMwiContent();
    byte[] getSubWinfoContent();

    boolean stopStack();
    boolean register();
    boolean unRegister();

    boolean PresencePublish();
    boolean PresencePublish(PresenceStatus status);
}
