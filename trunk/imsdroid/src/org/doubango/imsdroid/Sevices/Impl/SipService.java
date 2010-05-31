package org.doubango.imsdroid.Sevices.Impl;

import java.util.concurrent.CopyOnWriteArrayList;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.events.EventHandler;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.ISubscriptionEventHandler;
import org.doubango.imsdroid.events.RegistrationEventArgs;
import org.doubango.imsdroid.events.RegistrationEventTypes;
import org.doubango.imsdroid.events.SubscriptionEventArgs;
import org.doubango.imsdroid.events.SubscriptionEventTypes;
import org.doubango.imsdroid.sip.MyPublicationSession;
import org.doubango.imsdroid.sip.MyRegistrationSession;
import org.doubango.imsdroid.sip.MySipStack;
import org.doubango.imsdroid.sip.MySubscriptionSession;
import org.doubango.imsdroid.sip.MySubscriptionSession.EVENT_PACKAGE_TYPE;
import org.doubango.imsdroid.utils.ContentType;
import org.doubango.imsdroid.utils.StringUtils;
import org.doubango.tinyWRAP.DialogEvent;
import org.doubango.tinyWRAP.OptionsEvent;
import org.doubango.tinyWRAP.OptionsSession;
import org.doubango.tinyWRAP.RegistrationEvent;
import org.doubango.tinyWRAP.SipCallback;
import org.doubango.tinyWRAP.SipMessage;
import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.SubscriptionEvent;
import org.doubango.tinyWRAP.SubscriptionSession;
import org.doubango.tinyWRAP.tinyWRAPConstants;
import org.doubango.tinyWRAP.tsip_options_event_type_t;
import org.doubango.tinyWRAP.tsip_register_event_type_t;
import org.doubango.tinyWRAP.tsip_subscribe_event_type_t;

import android.os.ConditionVariable;
import android.util.Log;

public class SipService extends Service 
implements ISipService, tinyWRAPConstants {

	private final static String TAG = SipService.class.getCanonicalName();

	// Services
	private final IConfigurationService configurationService;
	private final INetworkService networkService;

	// Event Handlers
	private final CopyOnWriteArrayList<IRegistrationEventHandler> registrationEventHandlers;
	private final CopyOnWriteArrayList<ISubscriptionEventHandler> subscriptionEventHandlers;

	private byte[] reginfo;
	private byte[] winfo;
	
	private MySipStack sipStack;
	private final MySipCallback sipCallback;
	
	private MyRegistrationSession regSession;
	private MySubscriptionSession subReg;
	private MySubscriptionSession subWinfo;
	private MySubscriptionSession subMwi;
	private MySubscriptionSession subDebug;
	private MyPublicationSession pubPres;
	
	private final SipPrefrences preferences;

	private ConditionVariable condHack;

	public SipService() {
		super();

		this.sipCallback = new MySipCallback(this);

		this.registrationEventHandlers = new CopyOnWriteArrayList<IRegistrationEventHandler>();
		this.subscriptionEventHandlers = new CopyOnWriteArrayList<ISubscriptionEventHandler>();
		

		this.configurationService = ServiceManager.getConfigurationService();
		this.networkService = ServiceManager.getNetworkService();
		
		this.preferences = new SipPrefrences();
	}

	public boolean start() {
		return true;
	}

	public boolean stop() {
		return true;
	}

	public boolean isRegistered() {
		if (this.regSession != null) {
			return this.regSession.isConnected();
		}
		return false;
	}

	public byte[] getReginfo(){
		return this.reginfo;
	}
	
	public byte[] getWinfo(){
		return this.winfo;
	}
	
	/* ===================== SIP functions ======================== */

	public boolean register() {
		this.preferences.realm = this.configurationService.getString(
				CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.REALM,
				Configuration.DEFAULT_REALM);
		this.preferences.impi = this.configurationService.getString(
				CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.IMPI,
				Configuration.DEFAULT_IMPI);
		this.preferences.impu = this.configurationService.getString(
				CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.IMPU,
				Configuration.DEFAULT_IMPU);

		Log.i(this.getClass().getCanonicalName(), String.format(
				"realm=%s, impu=%s, impi=%s", this.preferences.realm, this.preferences.impu, this.preferences.impi));

		if (this.sipStack == null) {
			this.sipStack = new MySipStack(this.sipCallback, this.preferences.realm, this.preferences.impi, this.preferences.impu);
		} else {
			if (!this.sipStack.setRealm(this.preferences.realm)) {
				Log.e(this.getClass().getCanonicalName(), "Failed to set realm");
				return false;
			}
			if (!this.sipStack.setIMPI(this.preferences.impi)) {
				Log.e(this.getClass().getCanonicalName(), "Failed to set IMPI");
				return false;
			}
			if (!this.sipStack.setIMPU(this.preferences.impu)) {
				Log.e(this.getClass().getCanonicalName(), "Failed to set IMPU");
				return false;
			}
		}

		// Check stack validity
		if (!this.sipStack.isValid()) {
			Log.e(this.getClass().getCanonicalName(), "Trying to use invalid stack");
			return false;
		}

		// Set Proxy-CSCF
		this.preferences.pcscf_host = this.configurationService.getString(
				CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_HOST,
				null); // null will trigger DNS NAPTR+SRV
		this.preferences.pcscf_port = this.configurationService.getInt(
				CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_PORT,
				Configuration.DEFAULT_PCSCF_PORT);
		this.preferences.transport = this.configurationService.getString(
				CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.TRANSPORT,
				Configuration.DEFAULT_TRANSPORT);
		this.preferences.ipversion = this.configurationService.getString(
				CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.IP_VERSION,
				Configuration.DEFAULT_IP_VERSION);

		Log.i(this.getClass().getCanonicalName(), String.format(
				"pcscf-host=%s, pcscf-port=%d, transport=%s, ipversion=%s",
				this.preferences.pcscf_host, this.preferences.pcscf_port, this.preferences.transport, this.preferences.ipversion));

		if (!this.sipStack.setProxyCSCF(this.preferences.pcscf_host, this.preferences.pcscf_port, this.preferences.transport,
				this.preferences.ipversion)) {
			Log.e(this.getClass().getCanonicalName(),
					"Failed to set Proxy-CSCF parameters");
			return false;
		}

		// Set local IP (If your reusing this code on non-Android platforms, let
		// doubango retrieve the best IP address)
		boolean ipv6 = StringUtils.equals(this.preferences.ipversion, "ipv6", true);
		if ((this.preferences.localIP = this.networkService.getLocalIP(ipv6)) == null) {
			this.preferences.localIP = ipv6 ? "::" : "10.0.2.15"; /* Probably on the emulator */
		}
		if (!this.sipStack.setLocalIP(this.preferences.localIP)) {
			Log.e(this.getClass().getCanonicalName(),
					"Failed to set the local IP");
			return false;
		}

		// enable/disable 3GPP early IMS
		this.sipStack.setEarlyIMS(this.configurationService.getBoolean(
				CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.EARLY_IMS,
				Configuration.DEFAULT_EARLY_IMS));
		

		// Set stack-level headers
		// Supported, Access-Network, Preferred-Identity, ...

		// Start the Stack
		if (!this.sipStack.start()) {
			Log.e(this.getClass().getCanonicalName(),
					"Failed to start the SIP stack");
			return false;
		}
		
		// Preference values
		this.preferences.xcap_enabled = this.configurationService.getBoolean(
				CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.ENABLED,
				Configuration.DEFAULT_XCAP_ENABLED);
		this.preferences.presence_enabled = this.configurationService.getBoolean(
				CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.PRESENCE,
				Configuration.DEFAULT_RCS_PRESENCE);

		// Create registration session
		if (this.regSession == null) {
			this.regSession = new MyRegistrationSession(this.sipStack);
		}

		// Set/update From URI. For Registration ToUri should be equals to realm
		// (done by the stack)
		this.regSession.setFromUri(this.preferences.impu);
		/* this.regSession.setToUri(this.preferences.impu); */

		/* Before registering, check if AoR hacking id enabled */
		this.preferences.hackAoR = this.configurationService.getBoolean(
				CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.HACK_AOR,
				Configuration.DEFAULT_NATT_HACK_AOR);
		if (this.preferences.hackAoR) {
			if (this.condHack == null) {
				this.condHack = new ConditionVariable();
			}
			final OptionsSession optSession = new OptionsSession(this.sipStack);
			optSession.setToUri(String.format("sip:{0}@{1}", "hacking_the_aor", this.preferences.realm));
			optSession.Send();
			try {
				synchronized (this.condHack) {
					this.condHack.wait(this.configurationService.getInt(
							CONFIGURATION_SECTION.NATT,
							CONFIGURATION_ENTRY.HACK_AOR_TIMEOUT,
							Configuration.DEFAULT_NATT_HACK_AOR_TIMEOUT));
				}
			} catch (InterruptedException e) {
				Log.e(SipService.TAG, e.getMessage());
			}
			this.condHack = null;
		}

		if (!this.regSession.register()) {
			Log.e(this.getClass().getCanonicalName(),
					"Failed to send REGISTER request");
			return false;
		}

		return true;
	}

	public boolean unregister() {
		if (this.isRegistered()) {
			return this.sipStack.stop();
		}
		Log.d(this.getClass().getCanonicalName(), "Already unregistered");
		return true;
	}

	/* ===================== Add/Remove handlers ======================== */

	public boolean addRegistrationEventHandler(IRegistrationEventHandler handler) {
		return EventHandler.addEventHandler(this.registrationEventHandlers, handler);
	}

	public boolean removeRegistrationEventHandler(IRegistrationEventHandler handler) {
		return EventHandler.removeEventHandler(this.registrationEventHandlers, handler);
	}

	public boolean addSubscriptionEventHandler(ISubscriptionEventHandler handler) {
		return EventHandler.addEventHandler(this.subscriptionEventHandlers, handler);
	}

	public boolean removeSubscriptionEventHandler(ISubscriptionEventHandler handler) {
		return EventHandler.removeEventHandler(this.subscriptionEventHandlers, handler);
	}

	/* ===================== Dispatch events ======================== */
	private void onRegistrationEvent(final RegistrationEventArgs eargs) {
		for(int i = 0; i<this.registrationEventHandlers.size(); i++){
			final IRegistrationEventHandler handler = this.registrationEventHandlers.get(i);
			new Thread(new Runnable() {
				public void run() {
					if (!handler.onRegistrationEvent(this, eargs)) {
						Log.w(handler.getClass().getName(), "onRegistrationEvent failed");
					}
				}
			}).start();
		}
	}
	
	private void onSubscriptionEvent(final SubscriptionEventArgs eargs) {
		for(int i = 0; i<this.subscriptionEventHandlers.size(); i++){
			final ISubscriptionEventHandler handler = this.subscriptionEventHandlers.get(i);
			new Thread(new Runnable() {
				public void run() {
					if (!handler.onSubscriptionEvent(this, eargs)) {
						Log.w(handler.getClass().getName(), "onSubscriptionEvent failed");
					}
				}
			}).start();
		}
	}

	

	/* ===================== Private functions ======================== */
	private void doPostRegistrationOp()
	{
		// guard
		if(!this.isRegistered()){
			return;
		}
		
		Log.d(SipService.TAG, "Doing post registration operations");
		
		/*
		 * 3GPP TS 24.229 5.1.1.3 Subscription to registration-state event package
		 * Upon receipt of a 2xx response to the initial registration, the UE shall subscribe to the reg event package for the public
		 * user identity registered at the user's registrar (S-CSCF) as described in RFC 3680 [43].
		 */
		if(this.subReg == null){
			this.subReg = new MySubscriptionSession(this.sipStack, this.preferences.impu, EVENT_PACKAGE_TYPE.REG);
		}
		else{
			this.subReg.setToUri(this.preferences.impu);
			this.subReg.setFromUri(this.preferences.impu);
		}
		this.subReg.subscribe();
		
		// Subscribe to "message-summary" (Message waiting indication)
		if(this.subMwi == null){
			this.subMwi = new MySubscriptionSession(this.sipStack, this.preferences.impu, EVENT_PACKAGE_TYPE.MESSAGE_SUMMARY); 
		}
		else{
			this.subMwi.setToUri(this.preferences.impu);
			this.subMwi.setFromUri(this.preferences.impu);
		}
		this.subMwi.subscribe();
		
		// Presence
		if(this.preferences.presence_enabled){
			// Subscribe to "watcher-info" and "presence"
			if(this.preferences.xcap_enabled){
				// "watcher-info"
				if(this.subWinfo == null){
					this.subWinfo = new MySubscriptionSession(this.sipStack, this.preferences.impu, EVENT_PACKAGE_TYPE.WINFO); 
				}
				else{
					this.subWinfo.setToUri(this.preferences.impu);
					this.subWinfo.setFromUri(this.preferences.impu);
				}
				this.subWinfo.subscribe();
				// "eventlist"
			}
			else{
				
			}
			
			// Publish presence
			if(this.pubPres == null){
				this.pubPres = new MyPublicationSession(this.sipStack, this.preferences.impu);
			}
			else{
				this.pubPres.setFromUri(this.preferences.impu);
				this.pubPres.setToUri(this.preferences.impu);
			}
			this.pubPres.publish("open", "busy", this.configurationService.getString(
					CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.FREE_TEXT,
					Configuration.DEFAULT_RCS_FREE_TEXT));
		}
		
	}
	

	/* ===================== Sip Callback ======================== */
	private class MySipCallback extends SipCallback {

		private final SipService sipService;

		private MySipCallback(SipService sipService) {
			super();

			this.sipService = sipService;
		}

		@SuppressWarnings("unused")
		private final boolean isSipCode(short code) {
			return (code >= 100 && code <= 699);
		}

		private final boolean is2xx(short code) {
			return (code >= 200 && code <= 299);
		}

		private final boolean is1xx(short code) {
			return (code >= 100 && code <= 199);
		}

		@Override
		public int OnRegistrationEvent(RegistrationEvent e) {
			final short code = e.getCode();
			final String phrase = e.getPhrase();
			final tsip_register_event_type_t type = e.getType();

			Log.d(SipService.TAG, String.format("OnRegistrationEvent (%s)", phrase));
			
			if (this.is1xx(code)) {
				return 0;
			}

			switch (type) {
			case tsip_ao_register:
				if (this.is2xx(code)) { /* Success */
					// see OnDialogEvent()
				} else { /* Failure */
					// see OnDialogEvent()
				}
				break;
			case tsip_ao_unregister:
				if (this.is2xx(code)) { /* Success */
					// see OnDialogEvent()
				} else { /* Failure */
					// see OnDialogEvent()
				}
				break;

			default:
				break;
			}
			return 0;
		}

		@Override
		public int OnSubscriptionEvent(SubscriptionEvent e) {
			short code = e.getCode();
			String phrase = e.getPhrase();
			tsip_subscribe_event_type_t type = e.getType();
			SipMessage message = e.getSipMessage();
			SubscriptionSession session = e.getSession();
			
			if(session == null){
				return 0;
			}
			
			switch(type){
				case tsip_ao_subscribe:
					break;
					
				case tsip_ao_unsubscribe:
					break;
					
				case tsip_i_notify:
					if(message == null){
						return 0;
					}
					String contentType = message.getSipHeaderValue("c");
					byte[] content = message.getSipContent();
					
					if(content != null){
						if(StringUtils.equals(contentType, ContentType.REG_INFO, true)){
							this.sipService.reginfo = content;
						}
						else if(StringUtils.equals(contentType, ContentType.WATCHER_INFO, true)){
							this.sipService.winfo = content;
						}
						
						SubscriptionEventArgs eargs = new SubscriptionEventArgs(SubscriptionEventTypes.INCOMING_NOTIFY, 
								code, phrase, content, contentType);
						this.sipService.onSubscriptionEvent(eargs);
					}
					break;
				}
			
			return 0;
		}

		@Override
		public int OnDialogEvent(DialogEvent e){
			final String phrase = e.getPhrase();
			final short code = e.getCode();
			SipSession session = e.getBaseSession();
			
			if(session == null){
				return 0;
			}
			
			Log.d(SipService.TAG, String.format("OnDialogEvent (%s)", phrase));
			
			switch(code){
				case tsip_event_code_dialog_connecting:
					// Registration
					if((this.sipService.regSession != null) && (session.getId() == this.sipService.regSession.getId())){							
						this.sipService.onRegistrationEvent(new RegistrationEventArgs(
									RegistrationEventTypes.REGISTRATION_INPROGRESS, code, phrase));
					}
					// Subscription
					// Publication
					// ...
					break;
					
				case tsip_event_code_dialog_connected:
					// Registration
					if((this.sipService.regSession != null) && (session.getId() == this.sipService.regSession.getId())){
						this.sipService.regSession.setConnected(true);
						this.sipService.doPostRegistrationOp();
						this.sipService.onRegistrationEvent(new RegistrationEventArgs(
									RegistrationEventTypes.REGISTRATION_OK, code, phrase));
					}
					else if((this.sipService.pubPres != null) && (session.getId() == this.sipService.pubPres.getId())){							
						this.sipService.pubPres.setConnected(true);
					}
					// Subscription
					// Publication
					// ...
					break;
					
				case tsip_event_code_dialog_terminating:
					// Registration
					if((this.sipService.regSession != null) && (session.getId() == this.sipService.regSession.getId())){						
						this.sipService.onRegistrationEvent(new RegistrationEventArgs(
									RegistrationEventTypes.UNREGISTRATION_INPROGRESS, code, phrase));
					}
					// Subscription
					// Publication
					// ...
					break;
				
				case tsip_event_code_dialog_terminated:
					if((this.sipService.regSession != null) && (session.getId() == this.sipService.regSession.getId())){
						this.sipService.regSession.setConnected(false);
						this.sipService.onRegistrationEvent(new RegistrationEventArgs(
									RegistrationEventTypes.UNREGISTRATION_OK, code, phrase));
					}
					else if((this.sipService.pubPres != null) && (session.getId() == this.sipService.pubPres.getId())){							
						this.sipService.pubPres.setConnected(false);
					}
					// Subscription
					// Publication
					// ...
					break;
					
				default:
					break;
			}
			
			return 0;
		}
		
		@Override
		public int OnOptionsEvent(OptionsEvent e) {
			//short code = e.getCode();
			tsip_options_event_type_t type = e.getType();
			//OptionsSession session = e.getSession();
			SipMessage message = e.getSipMessage();

			if (message == null) {
				return 0;
			}

			switch (type) {
			case tsip_ao_options:
				String rport = message.getSipHeaderParamValue("v", "rport");
				String received = message.getSipHeaderParamValue("v","received");
				if (rport == null || rport.equals("0")) { // FIXME: change tsip_header_Via_get_special_param_value() to return "tsk_null" instead of "0"
					rport = message.getSipHeaderParamValue("v", "received_port_ext");
				}
				if (SipService.this.condHack != null && SipService.this.preferences.hackAoR) {
					SipService.this.sipStack.setAoR(received, Integer.parseInt(rport));
					SipService.this.condHack.open();
				}
				break;
			case tsip_i_options:
			default:
				break;
			}

			return 0;
		}
	}

	/* ===================== Sip Session Preferences ======================== */
	private class SipPrefrences {
		private boolean rcs;
		private boolean xcapdiff;
		private boolean xcap_enabled;
		private boolean preslist;
		private boolean deferredMsg;
		private boolean presence_enabled;
		private boolean messageSummary;
		private String impi;
		private String impu;
		private String realm;
		private String pcscf_host;
		private int pcscf_port;
		private String transport;
		private String ipversion;
		private String localIP;
		private boolean hackAoR;

		private SipPrefrences() {

		}
	}
}
