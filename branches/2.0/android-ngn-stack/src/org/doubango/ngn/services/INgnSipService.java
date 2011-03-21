package org.doubango.ngn.services;

import org.doubango.ngn.sip.NgnPresenceStatus;
import org.doubango.ngn.sip.NgnSipSession.ConnectionState;
import org.doubango.ngn.sip.NgnSipStack;

import android.content.Context;

public interface INgnSipService extends INgnBaseService {
	String getDefaultIdentity();
	void setDefaultIdentity(String identity);
    NgnSipStack getSipStack();
    boolean isRegistered();
    ConnectionState getRegistrationState();
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
    boolean register(Context context);
    boolean unRegister();

    boolean PresencePublish();
    boolean PresencePublish(NgnPresenceStatus status);
}
