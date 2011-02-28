
package org.doubango.imsdroid.Services.Impl;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Events.EventArgs;
import org.doubango.imsdroid.Events.InviteEventArgs;
import org.doubango.imsdroid.Events.InviteEventTypes;
import org.doubango.imsdroid.Events.RegistrationEventArgs;
import org.doubango.imsdroid.Events.RegistrationEventTypes;
import org.doubango.imsdroid.Model.HistoryEvent.StatusType;
import org.doubango.imsdroid.Model.HistorySMSEvent;
import org.doubango.imsdroid.Screens.ScreenAV;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sip.MyAVSession;
import org.doubango.imsdroid.Sip.MyInviteSession;
import org.doubango.imsdroid.Sip.MyInviteSession.InviteState;
import org.doubango.imsdroid.Sip.MyMessagingSession;
import org.doubango.imsdroid.Sip.MyRegistrationSession;
import org.doubango.imsdroid.Sip.MySipSession;
import org.doubango.imsdroid.Sip.MySipSession.ConnectionState;
import org.doubango.imsdroid.Sip.MySipStack;
import org.doubango.imsdroid.Sip.MySipStack.STACK_STATE;
import org.doubango.imsdroid.Sip.PresenceStatus;
import org.doubango.imsdroid.Sip.SipPrefrences;
import org.doubango.imsdroid.Utils.ConfigurationUtils;
import org.doubango.imsdroid.Utils.ConfigurationUtils.ConfigurationEntry;
import org.doubango.imsdroid.Utils.ContentType;
import org.doubango.imsdroid.Utils.StringUtils;
import org.doubango.imsdroid.Utils.UriUtils;
import org.doubango.tinyWRAP.CallSession;
import org.doubango.tinyWRAP.DDebugCallback;
import org.doubango.tinyWRAP.DialogEvent;
import org.doubango.tinyWRAP.InviteEvent;
import org.doubango.tinyWRAP.InviteSession;
import org.doubango.tinyWRAP.MessagingEvent;
import org.doubango.tinyWRAP.MessagingSession;
import org.doubango.tinyWRAP.OptionsSession;
import org.doubango.tinyWRAP.RPMessage;
import org.doubango.tinyWRAP.SMSData;
import org.doubango.tinyWRAP.SMSEncoder;
import org.doubango.tinyWRAP.SipCallback;
import org.doubango.tinyWRAP.SipMessage;
import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.SipStack;
import org.doubango.tinyWRAP.StackEvent;
import org.doubango.tinyWRAP.tinyWRAPConstants;
import org.doubango.tinyWRAP.tsip_invite_event_type_t;
import org.doubango.tinyWRAP.tsip_message_event_type_t;
import org.doubango.tinyWRAP.twrap_media_type_t;
import org.doubango.tinyWRAP.twrap_sms_type_t;

import android.content.Intent;
import android.os.ConditionVariable;
import android.util.Log;
import android.widget.Toast;

public class SipService extends BaseService 
implements ISipService, tinyWRAPConstants {
	private final static String TAG = SipService.class.getCanonicalName();

	public static final String ACTION_REGISTRATION_EVENT = TAG + ".ACTION_REGISTRATION_CHANGED";
	public static final String ACTION_MESSAGING_EVENT = TAG + ".ACTION_MESSAGING_EVENT";
	public static final String ACTION_STACK_EVENT = TAG + ".ACTION_STACK_EVENT";
	public static final String ACTION_SUBSCRIBTION_EVENT = TAG + ".ACTION_SUBSCRIBTION_EVENT";
	public static final String ACTION_INVITE_EVENT = TAG + ".ACTION_INVITE_EVENT";
	
	private MyRegistrationSession mRegSession;
	private MySipStack mSipStack;
	private final MySipCallback mSipCallback;
	private final SipPrefrences mPreferences;
	
	private final IConfigurationService mConfigurationService;
	private final INetworkService mNetworkService;
	
	private ConditionVariable mCondHackAoR;
	
	public SipService() {
		super();
		
		mSipCallback = new MySipCallback(this);
		mPreferences = new SipPrefrences();
		
		mConfigurationService = ServiceManager.getConfigurationService();
		mNetworkService = ServiceManager.getNetworkService();
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "starting...");
		return true;
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "stopping...");
		if(mSipStack != null && mSipStack.getState() == STACK_STATE.STARTED){
			return mSipStack.stop();
		}
		return true;
	}

	@Override
	public String getDefaultIdentity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultIdentity(String identity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MySipStack getSipStack() {
		return mSipStack;
	}

	@Override
	public boolean isRegistered() {
		if (mRegSession != null) {
			return mRegSession.isConnected();
		}
		return false;
	}

	@Override
	public boolean isXcapEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPublicationEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSubscriptionEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSubscriptionToRLSEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getCodecs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setCodecs(int coddecs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getSubRLSContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getSubRegContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getSubMwiContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getSubWinfoContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean stopStack() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean register() {
		mPreferences.setRealm(mConfigurationService.getString(ConfigurationEntry.NETWORK_REALM, 
				ConfigurationUtils.DEFAULT_NETWORK_REALM));
		mPreferences.setIMPI(mConfigurationService.getString(ConfigurationEntry.IDENTITY_IMPI, 
				ConfigurationUtils.DEFAULT_IDENTITY_IMPI));
		mPreferences.setIMPU(mConfigurationService.getString(ConfigurationEntry.IDENTITY_IMPU, 
				ConfigurationUtils.DEFAULT_IDENTITY_IMPU));
		
		Log.d(TAG, String.format(
				"realm='%s', impu='%s', impi='%s'", mPreferences.getRealm(), mPreferences.getIMPU(), mPreferences.getIMPI()));
		
		if (mSipStack == null) {
			mSipStack = new MySipStack(mSipCallback, mPreferences.getRealm(), mPreferences.getIMPI(), mPreferences.getIMPU());	
			mSipStack.setDebugCallback(new DDebugCallback());
			SipStack.setCodecs_2(mConfigurationService.getInt(ConfigurationEntry.MEDIA_CODECS, 
					ConfigurationUtils.DEFAULT_MEDIA_CODECS));
		} else {
			if (!mSipStack.setRealm(mPreferences.getRealm())) {
				Log.e(TAG, "Failed to set realm");
				return false;
			}
			if (!mSipStack.setIMPI(mPreferences.getIMPI())) {
				Log.e(TAG, "Failed to set IMPI");
				return false;
			}
			if (!mSipStack.setIMPU(mPreferences.getIMPU())) {
				Log.e(TAG, "Failed to set IMPU");
				return false;
			}
		}
		
		// set the Password
		mSipStack.setPassword(mConfigurationService.getString(
				ConfigurationEntry.IDENTITY_PASSWORD, ConfigurationUtils.DEFAULT_IDENTITY_PASSWORD));
		// Set AMF
		mSipStack.setAMF(mConfigurationService.getString(
				ConfigurationEntry.SECURITY_IMSAKA_AMF, ConfigurationUtils.DEFAULT_SECURITY_IMSAKA_AMF));
		// Set Operator Id
		mSipStack.setOperatorId(mConfigurationService.getString(
				ConfigurationEntry.SECURITY_IMSAKA_OPID, ConfigurationUtils.DEFAULT_SECURITY_IMSAKA_OPID));
		
		// Check stack validity
		if (!mSipStack.isValid()) {
			Log.e(TAG, "Trying to use invalid stack");
			return false;
		}
		
		// Set STUN information
		if(mConfigurationService.getBoolean(ConfigurationEntry.NATT_USE_STUN, ConfigurationUtils.DEFAULT_NATT_USE_STUN)){			
			Log.d(TAG, "STUN=yes");
			if(mConfigurationService.getBoolean(ConfigurationEntry.NATT_STUN_DISCO, ConfigurationUtils.DEFAULT_NATT_STUN_DISCO)){
				final String realm = mPreferences.getRealm();
				String domain = realm.substring(realm.indexOf(':')+1);
				int []port = new int[1];
				String server = mSipStack.dnsSrv(String.format("_stun._udp.%s", domain), port);
				if(server == null){
					ServiceManager.getScreenService().setProgressInfoText("STUN discovery has failed");
				}
				Log.d(TAG, String.format("STUN1 - server=%s and port=%d", server, port[0]));
				mSipStack.setSTUNServer(server, port[0]);// Needed event if null
			}
			else{
				String server = mConfigurationService.getString(ConfigurationEntry.NATT_STUN_SERVER, 
						ConfigurationUtils.DEFAULT_NATT_STUN_SERVER);
				int port = mConfigurationService.getInt(ConfigurationEntry.NATT_STUN_PORT, 
						ConfigurationUtils.DEFAULT_NATT_STUN_PORT);
				Log.d(SipService.TAG, String.format("STUN2 - server=%s and port=%d", server, port));
				mSipStack.setSTUNServer(server, port);
			}
		}
		else{
			Log.d(TAG, "STUN=no");
			mSipStack.setSTUNServer(null, 0);
		}
		
		// Set Proxy-CSCF
		mPreferences.setPcscfHost(mConfigurationService.getString(ConfigurationEntry.NETWORK_PCSCF_HOST,
				null)); // null will trigger DNS NAPTR+SRV
		mPreferences.setPcscfPort(mConfigurationService.getInt(ConfigurationEntry.NETWORK_PCSCF_PORT,
				ConfigurationUtils.DEFAULT_NETWORK_PCSCF_PORT));
		mPreferences.setTransport(mConfigurationService.getString(ConfigurationEntry.NETWORK_TRANSPORT,
				ConfigurationUtils.DEFAULT_NETWORK_TRANSPORT));
		mPreferences.setIPVersion(mConfigurationService.getString(ConfigurationEntry.NETWORK_IP_VERSION,
				ConfigurationUtils.DEFAULT_NETWORK_IP_VERSION));
		
		Log.d(TAG, String.format(
				"pcscf-host='%s', pcscf-port='%d', transport='%s', ipversion='%s'",
				mPreferences.getPcscfHost(), 
				mPreferences.getPcscfPort(),
				mPreferences.getTransport(),
				mPreferences.getIPVersion()));

		if (!mSipStack.setProxyCSCF(mPreferences.getPcscfHost(), mPreferences.getPcscfPort(), mPreferences.getTransport(),
				mPreferences.getIPVersion())) {
			Log.e(SipService.TAG, "Failed to set Proxy-CSCF parameters");
			return false;
		}
		
		// Set local IP (If your reusing this code on non-Android platforms (iOS, Symbian, WinPhone, ...),
		// let Doubango retrieve the best IP address)
		boolean ipv6 = StringUtils.equals(mPreferences.getIPVersion(), "ipv6", true);
		mPreferences.setLocalIP(mNetworkService.getLocalIP(ipv6));
		if(mPreferences.getLocalIP() == null){
//			if(fromNetworkService){
//				this.preferences.localIP = ipv6 ? "::" : "10.0.2.15"; /* Probably on the emulator */
//			}
//			else{
//				Log.e(TAG, "IP address is Null. Trying to start network");
//				this.networkService.setNetworkEnabledAndRegister();
//				return false;
//			}
		}
		if (!mSipStack.setLocalIP(mPreferences.getLocalIP())) {
			Log.e(TAG, "Failed to set the local IP");
			return false;
		}
		Log.d(TAG, String.format("Local IP='%s'", mPreferences.getLocalIP()));
		
		// Whether to use DNS NAPTR+SRV for the Proxy-CSCF discovery (even if the DNS requests are sent only when the stack starts,
		// should be done after setProxyCSCF())
		String discoverType = mConfigurationService.getString(ConfigurationEntry.NETWORK_PCSCF_DISCOVERY, ConfigurationUtils.DEFAULT_NETWORK_PCSCF_DISCOVERY);
		mSipStack.setDnsDiscovery(StringUtils.equals(discoverType, ConfigurationUtils.PCSCF_DISCOVERY_DNS_SRV, true));		
		
		// enable/disable 3GPP early IMS
		mSipStack.setEarlyIMS(mConfigurationService.getBoolean(ConfigurationEntry.NETWORK_USE_EARLY_IMS,
				ConfigurationUtils.DEFAULT_NETWORK_USE_EARLY_IMS));
		
		// SigComp (only update compartment Id if changed)
		if(mConfigurationService.getBoolean(ConfigurationEntry.NETWORK_USE_SIGCOMP, ConfigurationUtils.DEFAULT_NETWORK_USE_SIGCOMP)){
			String compId = String.format("urn:uuid:%s", UUID.randomUUID().toString());
			mSipStack.setSigCompId(compId);
		}
		else{
			mSipStack.setSigCompId(null);
		}
		
		// Start the Stack
		if (!mSipStack.start()) {
			Toast.makeText(IMSDroid.getContext(), "Failed to start the SIP stack", Toast.LENGTH_LONG).show();
			Log.e(TAG, "Failed to start the SIP stack");
			return false;
		}
		
		// Preference values
		mPreferences.setXcapEnabled(mConfigurationService.getBoolean(ConfigurationEntry.XCAP_ENABLED,
				ConfigurationUtils.DEFAULT_XCAP_ENABLED));
		mPreferences.setPresenceEnabled(mConfigurationService.getBoolean(ConfigurationEntry.RCS_USE_PRESENCE,
				ConfigurationUtils.DEFAULT_RCS_USE_PRESENCE));
		mPreferences.setMWI(mConfigurationService.getBoolean(ConfigurationEntry.RCS_USE_MWI,
				ConfigurationUtils.DEFAULT_RCS_USE_MWI));
		
		// Create registration session
		if (mRegSession == null) {
			mRegSession = new MyRegistrationSession(mSipStack);
		}
		else{
			mRegSession.setSigCompId(mSipStack.getSigCompId());
		}
		
		// Set/update From URI. For Registration ToUri should be equals to realm
		// (done by the stack)
		mRegSession.setFromUri(mPreferences.getIMPU());
		
		/* Before registering, check if AoR hacking id enabled */
		mPreferences.setHackAoR(mConfigurationService.getBoolean(ConfigurationEntry.NATT_HACK_AOR, 
				ConfigurationUtils.DEFAULT_NATT_HACK_AOR));
		if (mPreferences.isHackAoR()) {
			if (mCondHackAoR == null) {
				mCondHackAoR = new ConditionVariable();
			}
			final OptionsSession optSession = new OptionsSession(mSipStack);
			// optSession.setToUri(String.format("sip:%s@%s", "hacking_the_aor", this.preferences.realm));
			optSession.send();
			try {
				synchronized (mCondHackAoR) {
					mCondHackAoR.wait(mConfigurationService.getInt(ConfigurationEntry.NATT_HACK_AOR_TIMEOUT,
							ConfigurationUtils.DEFAULT_NATT_HACK_AOR_TIMEOUT));
				}
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage());
			}
			mCondHackAoR = null;
			optSession.delete();
		}

		if (!mRegSession.register()) {
			Log.e(TAG, "Failed to send REGISTER request");
			return false;
		}
		
		return true;
	}
	@Override
	public boolean unRegister() {
		if (isRegistered()) {
			new Thread(new Runnable(){
				@Override
				public void run() {
					mSipStack.stop();
				}
			}).start();
		}
		return true;
	}

	@Override
	public boolean PresencePublish() {
		return false;
	}

	@Override
	public boolean PresencePublish(PresenceStatus status) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void broadcastRegistrationEvent(RegistrationEventArgs args){
		final Intent intent = new Intent(ACTION_REGISTRATION_EVENT);
		intent.putExtra(EventArgs.EXTRA_NAME, args);
		IMSDroid.getContext().sendBroadcast(intent);
	}
	
	private void broadcastInviteEvent(InviteEventArgs args){
		final Intent intent = new Intent(ACTION_INVITE_EVENT);
		intent.putExtra(EventArgs.EXTRA_NAME, args);
		IMSDroid.getContext().sendBroadcast(intent);
	}
	
	private void broadcastMessagingEvent(){
		
	}
	
	/**
	 * MySipCallback
	 */
	static class MySipCallback extends SipCallback{
		private final SipService mSipService;

		private MySipCallback(SipService sipService) {
			super();

			mSipService = sipService;
		}
		
		@Override
		public int OnDialogEvent(DialogEvent e){
			final String phrase = e.getPhrase();
			final short code = e.getCode();
			final SipSession session = e.getBaseSession();
			if(session == null){
				return 0;
			}
			
			final long sessionId = session.getId();
			MySipSession mySession = null;
			
			Log.d(TAG, String.format("OnDialogEvent (%s,%d)", phrase,sessionId));
			
			
			switch (code){
				//== Connecting ==
				case tinyWRAPConstants.tsip_event_code_dialog_connecting:
				{
					// Connecting //
                    if (mSipService.mRegSession != null && mSipService.mRegSession.getId() == sessionId){
                    	mSipService.mRegSession.setConnectionState(ConnectionState.CONNECTING);
                    	mSipService.broadcastRegistrationEvent(new RegistrationEventArgs(RegistrationEventTypes.REGISTRATION_INPROGRESS, 
                    			code, phrase));
                    }
                    // Audio/Video/MSRP
                    else if (((mySession = MyAVSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTING);
                        ((MyInviteSession)mySession).setState(InviteState.INPROGRESS);
                        mSipService.broadcastInviteEvent(new InviteEventArgs(sessionId, InviteEventTypes.INPROGRESS, phrase));
                    } 

					break;
				}
				
				//== Connected == //
				case tinyWRAPConstants.tsip_event_code_dialog_connected:
				{
					// Registration
                    if (mSipService.mRegSession != null && mSipService.mRegSession.getId() == sessionId){
                    	mSipService.mRegSession.setConnectionState(ConnectionState.CONNECTED);
                        // Update default identity (vs barred)
                        String _defaultIdentity = mSipService.mSipStack.getPreferredIdentity();
                        if (!StringUtils.isNullOrEmpty(_defaultIdentity)){
                        	mSipService.setDefaultIdentity(_defaultIdentity);
                        }
                        mSipService.broadcastRegistrationEvent(new RegistrationEventArgs(RegistrationEventTypes.REGISTRATION_OK, 
                        		code, phrase));
                    }
                    // Audio/Video/MSRP
                    else if (((mySession = MyAVSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTED);
                    	((MyInviteSession)mySession).setState(InviteState.INCALL);
                        mSipService.broadcastInviteEvent(new InviteEventArgs(sessionId, InviteEventTypes.CONNECTED, phrase));
                    }

					break;
				}
				
				//== Terminating == //
				case tinyWRAPConstants.tsip_event_code_dialog_terminating:
				{
					// Registration
					if (mSipService.mRegSession != null && mSipService.mRegSession.getId() == sessionId){
						mSipService.mRegSession.setConnectionState(ConnectionState.TERMINATING);
						mSipService.broadcastRegistrationEvent(new RegistrationEventArgs(RegistrationEventTypes.UNREGISTRATION_INPROGRESS, 
								code, phrase));
					}
					// Audio/Video/MSRP
                    else if (((mySession = MyAVSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.TERMINATING);
                    	((MyInviteSession)mySession).setState(InviteState.TERMINATING);
                    	mSipService.broadcastInviteEvent(new InviteEventArgs(sessionId, InviteEventTypes.TERMWAIT, phrase));
                    }

					break;
				}
				
				//== Terminated == //
				case tinyWRAPConstants.tsip_event_code_dialog_terminated:
				{
					// Registration
					if (mSipService.mRegSession != null && mSipService.mRegSession.getId() == sessionId){
						mSipService.mRegSession.setConnectionState(ConnectionState.TERMINATED);
						mSipService.broadcastRegistrationEvent(new RegistrationEventArgs(RegistrationEventTypes.UNREGISTRATION_OK, code, phrase));
						/* Stop the stack (as we are already in the stack-thread, then do it in a new thread) */
						new Thread(new Runnable(){
							public void run() {	
								if(mSipService.mSipStack.getState() == STACK_STATE.STARTING || mSipService.mSipStack.getState() == STACK_STATE.STARTED){
									mSipService.mSipStack.stop();
								}
							}
						}).start();
					}
					// PagerMode IM
					else if(MyMessagingSession.hasSession(sessionId)){
						MyMessagingSession.releaseSession(sessionId);
					}
					// Audio/Video/MSRP
                    else if (((mySession = MyAVSession.getSession(sessionId)) != null)){
                        mySession.setConnectionState(ConnectionState.TERMINATED);
                        ((MyInviteSession)mySession).setState(InviteState.TERMINATED);
                        mSipService.broadcastInviteEvent(new InviteEventArgs(sessionId, InviteEventTypes.DISCONNECTED, phrase));
                        if(mySession instanceof MyAVSession){
                        	// FIXME: ERROR/dalvikvm(1098): ERROR: detaching thread with interp frames (count=4)
                        	MyAVSession.releaseSession((MyAVSession)mySession);
                        }
                    }
					break;
				}
			}
			
			return 0;
		}
		
		@SuppressWarnings("null")
		@Override
		public int OnInviteEvent(InviteEvent e) {
			 final tsip_invite_event_type_t type = e.getType();
			 final short code = e.getCode();
			 final String phrase = e.getPhrase();
			 InviteSession session = e.getSession();
			
			switch (type){
                case tsip_i_newcall:
                    if (session != null) /* As we are not the owner, then the session MUST be null */{
                        Log.e(TAG, "Invalid incoming session");
                        session.hangup(); // To avoid another callback event
                        return -1;
                    }

                    SipMessage message = e.getSipMessage();
                    if (message == null){
                        Log.e(TAG,"Invalid message");
                        return -1;
                    }
                    twrap_media_type_t sessionType = e.getMediaType();

                    switch (sessionType){
                        case twrap_media_msrp:
                            {
                            	session.hangup();
                            	return -1;
//                                if ((session = e.takeMsrpSessionOwnership()) == null){
//                                    Log.e(TAG,"Failed to take MSRP session ownership");
//                                    return -1;
//                                }
//
//                                MyMsrpSession msrpSession = MyMsrpSession.TakeIncomingSession(this.sipService.SipStack, session as MsrpSession, message);
//                                if (msrpSession == null)
//                                {
//                                    LOG.Error("Failed to create new session");
//                                    session.hangup();
//                                    session.Dispose();
//                                    return 0;
//                                }
//                                msrpSession.State = MyInviteSession.InviteState.INCOMING;
//
//                                InviteEventArgs eargs = new InviteEventArgs(msrpSession.Id, InviteEventTypes.INCOMING, phrase);
//                                eargs.AddExtra(InviteEventArgs.EXTRA_SESSION, msrpSession);
//                                EventHandlerTrigger.TriggerEvent<InviteEventArgs>(this.sipService.onInviteEvent, this.sipService, eargs);
//                                break;
                            }

                        case twrap_media_audio:
                        case twrap_media_audiovideo:
                        case twrap_media_video:
                            {
                                if ((session = e.takeCallSessionOwnership()) == null){
                                    Log.e(TAG,"Failed to take audio/video session ownership");
                                    return -1;
                                }
                                final MyAVSession avSession = MyAVSession.takeIncomingSession(mSipService.getSipStack(), (CallSession)session, sessionType, message);
                                ScreenAV.receiveCall(avSession);
                                mSipService.broadcastInviteEvent(new InviteEventArgs(avSession.getId(), InviteEventTypes.INCOMING, phrase));
                                break;
                            }

                        default:
                            Log.e(TAG,"Invalid media type");
                            return 0;
                        
                    }
                    break;

                case tsip_ao_request:
                    if (code == 180 && session != null){
                    	mSipService.broadcastInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.RINGING, phrase));
                    }
                    break;

                case tsip_i_request:
                case tsip_o_ect_ok:
                case tsip_o_ect_nok:
                case tsip_i_ect:
                    {
                        break;
                    }
                case tsip_m_early_media:
                    {
                    	mSipService.broadcastInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.EARLY_MEDIA, phrase));
                        break;
                    }
                case tsip_m_local_hold_ok:
                    {
                    	mSipService.broadcastInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.LOCAL_HOLD_OK, phrase));
                        break;
                    }
                case tsip_m_local_hold_nok:
                    {
                    	mSipService.broadcastInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.LOCAL_HOLD_NOK, phrase));
                        break;
                    }
                case tsip_m_local_resume_ok:
                    {
                    	mSipService.broadcastInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.LOCAL_RESUME_OK, phrase));
                        break;
                    }
                case tsip_m_local_resume_nok:
                    {
                    	mSipService.broadcastInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.LOCAL_RESUME_NOK, phrase));
                        break;
                    }
                case tsip_m_remote_hold:
                    {
                    	mSipService.broadcastInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.REMOTE_HOLD, phrase));
                        break;
                    }
                case tsip_m_remote_resume:
                    {
                    	mSipService.broadcastInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.REMOTE_RESUME, phrase));
                        break;
                    }
            }
			
			return 0;
		}
		
		@Override
		public int OnMessagingEvent(MessagingEvent e) {
			final tsip_message_event_type_t type = e.getType();
			
			switch(type){
				case tsip_ao_message:
					/* String phrase = e.getPhrase(); */
					/* short code = e.getCode(); */
					mSipService.broadcastMessagingEvent();
					break;
				case tsip_i_message:
					final SipMessage message = e.getSipMessage();
					MessagingSession _session = e.getSession();
					MyMessagingSession imSession;
					if (_session == null){
		             /* "Server-side-session" e.g. Initial MESSAGE sent by the remote party */
						_session = e.takeSessionOwnership();
					}
					
					if(_session == null){
						Log.e(SipService.TAG, "Failed to take session ownership");
						return -1;
					}
					imSession = MyMessagingSession.takeIncomingSession(mSipService.mSipStack, _session, message);
					if(message == null){
						imSession.reject();
						imSession.decRef();
						return 0;
					}
					
					
					String from = message.getSipHeaderValue("f");
					final String contentType = message.getSipHeaderValue("c");
					final byte[] bytes = message.getSipContent();
					byte[] content = null;
					
					if(bytes == null || bytes.length ==0){
						Log.e(SipService.TAG, "Invalid MESSAGE");
						imSession.reject();
						imSession.decRef();
						return 0;
					}
					
					imSession.accept();
					
					if(StringUtils.equals(contentType, ContentType.SMS_3GPP, true)){
						/* ==== 3GPP SMSIP  === */
						ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
						buffer.put(bytes);
						SMSData smsData = SMSEncoder.decode(buffer, buffer.capacity(), false);
                        if (smsData != null){
                            twrap_sms_type_t smsType = smsData.getType();
                            if (smsType == twrap_sms_type_t.twrap_sms_type_rpdata){
                            	/* === We have received a RP-DATA message === */
                                long payLength = smsData.getPayloadLength();
                                String SMSC = message.getSipHeaderValue("P-Asserted-Identity");
                                String SMSCPhoneNumber;
                                String origPhoneNumber = smsData.getOA();
                                
                                /* Destination address */
                                if(origPhoneNumber != null){
                                	from = UriUtils.makeValidSipUri(origPhoneNumber);
                                }
                                else if((origPhoneNumber = UriUtils.getValidPhoneNumber(from)) == null){
                                	Log.e(SipService.TAG, "Invalid destination address");
                                	return 0;
                                }
                                
                                /* SMS Center 
                                 * 3GPP TS 24.341 - 5.3.2.4	Sending a delivery report
                                 * The address of the IP-SM-GW is received in the P-Asserted-Identity header in the SIP MESSAGE 
                                 * request including the delivered short message.
                                 * */
                                if((SMSCPhoneNumber = UriUtils.getValidPhoneNumber(SMSC)) == null){
                                	SMSC = ServiceManager.getConfigurationService().getString(ConfigurationEntry.RCS_SMSC, ConfigurationUtils.DEFAULT_RCS_SMSC);
                                	if((SMSCPhoneNumber = UriUtils.getValidPhoneNumber(SMSC)) == null){
                                		Log.e(SipService.TAG, "Invalid IP-SM-GW address");
                                		return 0;
                                	}
                                }
                                
                                if (payLength > 0) {
                                    /* Send RP-ACK */
                                    RPMessage rpACK = SMSEncoder.encodeACK(smsData.getMR(), SMSCPhoneNumber, origPhoneNumber, false);
                                    if (rpACK != null){
                                        long ack_len = rpACK.getPayloadLength();
                                        if (ack_len > 0){
                                        	buffer = ByteBuffer.allocateDirect((int)ack_len);
                                            long len = rpACK.getPayload(buffer, buffer.capacity());
                                            MessagingSession m = new MessagingSession(mSipService.getSipStack());
                                            m.setToUri(SMSC);
                                            m.addHeader("Content-Type", ContentType.SMS_3GPP);
                                            m.addHeader("Content-Transfer-Encoding", "binary");
                                            m.addCaps("+g.3gpp.smsip");
                                            m.send(buffer, len);
                                            m.delete();
                                        }
                                        rpACK.delete();
                                    }

                                    /* Get ascii content */
                                    buffer = ByteBuffer.allocateDirect((int)payLength);
                                    content = new byte[(int)payLength];
                                    smsData.getPayload(buffer, buffer.capacity());
                                    buffer.get(content);
                                }
                                else{
                                    /* Send RP-ERROR */
                                    RPMessage rpError = SMSEncoder.encodeError(smsData.getMR(), SMSCPhoneNumber, origPhoneNumber, false);
                                    if (rpError != null){
                                        long err_len = rpError.getPayloadLength();
                                        if (err_len > 0){
                                        	buffer = ByteBuffer.allocateDirect((int)err_len);
                                            long len = rpError.getPayload(buffer, buffer.capacity());

                                            MessagingSession m = new MessagingSession(mSipService.getSipStack());
                                            m.setToUri(SMSC);
                                            m.addHeader("Content-Type", ContentType.SMS_3GPP);
                                            m.addHeader("Transfer-Encoding", "binary");
                                            m.addCaps("+g.3gpp.smsip");
                                            m.send(buffer, len);
                                            m.delete();
                                        }
                                        rpError.delete();
                                    }
                                }
                            }
                            else{
                            	/* === We have received any non-RP-DATA message === */
                            	if(smsType == twrap_sms_type_t.twrap_sms_type_ack){
                            		/* Find message from the history (by MR) an update it's status */
                            		Log.d(SipService.TAG, "RP-ACK");
                            	}
                            	else if(smsType == twrap_sms_type_t.twrap_sms_type_error){
                            		/* Find message from the history (by MR) an update it's status */
                            		Log.d(SipService.TAG, "RP-ERROR");
                            	}
                            }
                        }
					}
					else{
						/* ==== text/plain or any other  === */
						content = bytes;
					}
					
					/* Alert the user and add the message to the history */
					if(content != null){
						HistorySMSEvent event = new HistorySMSEvent(from, StatusType.Incoming);
						event.setContent(new String(content));
						ServiceManager.getHistoryService().addEvent(event);
						mSipService.broadcastMessagingEvent();
					}
					
					break;
			}
			
			return 0;
		}

		@Override
		public int OnStackEvent(StackEvent e) {
			//final String phrase = e.getPhrase();
			final short code = e.getCode();
			switch(code){
				case tinyWRAPConstants.tsip_event_code_stack_started:
					mSipService.mSipStack.setState(STACK_STATE.STARTED);
					Log.d(SipService.TAG, "Stack started");
					break;
				case tinyWRAPConstants.tsip_event_code_stack_failed_to_start:
					final String phrase = e.getPhrase();
					ServiceManager.getScreenService().runOnUiThread(new Runnable() {
						@Override
						public void run() {
								Toast.makeText(IMSDroid.getContext(), 
										String.format("Please check your connection information. \nAdditional info:\n%s", phrase), 
										Toast.LENGTH_LONG).show();
						}
					});
										
					Log.e(TAG, "Failed to start the stack");
					break;
				case tinyWRAPConstants.tsip_event_code_stack_failed_to_stop:
					Log.e(TAG, "Failed to stop the stack");
					break;
				case tinyWRAPConstants.tsip_event_code_stack_stopped:
					mSipService.mSipStack.setState(STACK_STATE.STOPPED);
					Log.d(TAG, "Stack stoped");
					break;
			}
			return 0;
		}
	}
}