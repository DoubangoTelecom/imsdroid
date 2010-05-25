package org.doubango.imsdroid.Sevices.Impl;

import java.util.concurrent.CopyOnWriteArrayList;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.events.EventHandler;
import org.doubango.imsdroid.events.INotifPresEventhandler;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.NotifPresEventArgs;
import org.doubango.imsdroid.events.RegistrationEventArgs;
import org.doubango.imsdroid.events.RegistrationEventTypes;
import org.doubango.imsdroid.sip.MyRegistrationSession;
import org.doubango.imsdroid.sip.MySipStack;
import org.doubango.imsdroid.utils.StringUtils;
import org.doubango.tinyWRAP.RegistrationEvent;
import org.doubango.tinyWRAP.SipCallback;
import org.doubango.tinyWRAP.SubscriptionEvent;
import org.doubango.tinyWRAP.tsip_register_event_type_t;

import android.util.Log;

public class SipService extends Service implements ISipService {

	// Services
	private final IConfigurationService configurationService;
	private final INetworkService networkService;
	
	// Event Handlers
	private final CopyOnWriteArrayList<IRegistrationEventHandler> registrationEventHandlers;
	private final CopyOnWriteArrayList<INotifPresEventhandler> notifPresEventhandler;
	
	private MySipStack sipStack;
	private MyRegistrationSession regSession;
	private final MySipCallback sipCallback;

	public SipService() {
		super();

		this.sipCallback = new MySipCallback(this);
		
		this.registrationEventHandlers = new CopyOnWriteArrayList<IRegistrationEventHandler>();
		this.notifPresEventhandler = new CopyOnWriteArrayList<INotifPresEventhandler>();
		
		this.configurationService = ServiceManager.getConfigurationService();
		this.networkService = ServiceManager.getNetworkService();
	}

	public boolean start() {
		return true;
	}

	public boolean stop() {
		return true;
	}
	
	public boolean isRegistered(){
		if(this.regSession != null){
			return this.regSession.isConnected();
		}
		return false;
	}

	/* ===================== SIP functions ======================== */
		
	public boolean register()
	{
		String realm = this.configurationService.getString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.REALM, Configuration.DEFAULT_REALM);
		String impi = this.configurationService.getString(CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.IMPI, Configuration.DEFAULT_IMPI);
		String impu = this.configurationService.getString(CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.IMPU, Configuration.DEFAULT_IMPU);
		
		Log.i(this.getClass().getCanonicalName(), String.format("realm=%s, impu=%s, impi=%s", realm, impu, impi));
		
		if(this.sipStack == null){
			this.sipStack = new MySipStack(this.sipCallback, realm, impi, impu);
		}
		else {
			if(!this.sipStack.setRealm(realm)){
				Log.e(this.getClass().getCanonicalName(), "Failed to set realm");
				return false;
			}
			if(!this.sipStack.setIMPI(impi)){
				Log.e(this.getClass().getCanonicalName(), "Failed to set IMPI");
				return false;
			}
			if(!this.sipStack.setIMPU(impu)){
				Log.e(this.getClass().getCanonicalName(), "Failed to set IMPU");
				return false;
			}
		}
		
		// Check stack validity
		if(!this.sipStack.isValid()){
			Log.e(this.getClass().getCanonicalName(), "Trying to use invalid stack");
			return false;
		}
		
		// Set Proxy-CSCF
		String pcscf_host = this.configurationService.getString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_HOST, null); // null will trigger DNS NAPTR+SRV 
		int pcscf_port = this.configurationService.getInt(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_PORT, Configuration.DEFAULT_PCSCF_PORT);
		String transport = this.configurationService.getString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.TRANSPORT, Configuration.DEFAULT_TRANSPORT);
		String ipversion = this.configurationService.getString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.IP_VERSION, Configuration.DEFAULT_IP_VERSION);
		
		Log.i(this.getClass().getCanonicalName(), String.format("pcscf-host=%s, pcscf-port=%d, transport=%s, ipversion=%s", pcscf_host, pcscf_port, transport, ipversion));
		
		if(!this.sipStack.setProxyCSCF(pcscf_host, pcscf_port, transport, ipversion)){
			Log.e(this.getClass().getCanonicalName(), "Failed to set Proxy-CSCF parameters");
			return false;
		}
		
		// Set local IP (If your reusing this code on non-Android platforms, let doubango retrieve the best IP address)
		String localIP;
		boolean ipv6 = StringUtils.equals(ipversion, "ipv6", true);
		if((localIP = this.networkService.getLocalIP(ipv6)) == null){
			localIP = ipv6 ? "::" : "10.0.2.15"; /* Probably on the emulator */
		}
		if(!this.sipStack.setLocalIP(localIP)){
			Log.e(this.getClass().getCanonicalName(), "Failed to set the local IP");
			return false;
		}
		
		// enable/disable 3GPP early IMS
		this.sipStack.setEarlyIMS(this.configurationService.getBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.EARLY_IMS, Configuration.DEFAULT_EARLY_IMS));
		
		// Set stack-level headers
		// Supported, Access-Network, Preferred-Identity, ...
		
		// Start the Stack
		if(!this.sipStack.start()){
			Log.e(this.getClass().getCanonicalName(), "Failed to start the SIP stack");
			return false;
		}
		
		// Create registration session
		if(this.regSession == null){
			this.regSession = new MyRegistrationSession(this.sipStack);
		}
		
		// Set/update From and To URIs
		this.regSession.setFromUri(impu);
		this.regSession.setToUri(impu);
		
		if(!this.regSession.register()){
			Log.e(this.getClass().getCanonicalName(), "Failed to send REGISTER request");
			return false;
		}
		
		return true;
	}
	
	public boolean unregister()
	{
		if(this.isRegistered()){
			return this.sipStack.stop();
		}
		Log.d(this.getClass().getCanonicalName(), "Already unregistered");
		return true;
	}
	
	
	/* ===================== Add/Remove handlers ======================== */
	
	public boolean addRegistrationEventHandler(IRegistrationEventHandler handler) 
	{
		return EventHandler.addEventHandler(this.registrationEventHandlers,handler);
	}

	public boolean removeRegistrationEventHandler(IRegistrationEventHandler handler) 
	{
		return EventHandler.removeEventHandler(this.registrationEventHandlers, handler);
	}

	public boolean addNotifPresEventhandler(INotifPresEventhandler handler) 
	{
		return EventHandler.addEventHandler(this.notifPresEventhandler, handler);
	}

	public boolean removeNotifPresEventhandler(INotifPresEventhandler handler) 
	{
		return EventHandler.removeEventHandler(this.notifPresEventhandler, handler);
	}
	
	/* ===================== Dispatch events ======================== */
	
	// should be private
	private void onRegistrationChanged(RegistrationEventArgs eargs)
	{
		for(IRegistrationEventHandler handler : this.registrationEventHandlers){
			if(!handler.onRegistrationEvent(this, eargs)){
				Log.w(handler.getClass().getName(), "onRegistrationEvent failed");
			}
		}
	}
	
	// should be private
	public void onTestNotifPresChanged()
	{
		NotifPresEventArgs e = new NotifPresEventArgs();
		for(INotifPresEventhandler handler : this.notifPresEventhandler)
		{
			if(!handler.onNotifPresEvent(this, e))
			{
				Log.w(handler.getClass().getName(), "onNotifPresEvent failed");
			}
		}
	}
	
	/* ===================== Private functions (Sip Events) ======================== */
		
	/* ===================== Sip Callback ======================== */
	private class MySipCallback  extends SipCallback{
		
		private final SipService sipService;
		
		private MySipCallback(SipService sipService){
	        super();
	        
	        this.sipService = sipService;
	    }
		
		private final boolean isSipCode(short code){
			return (code >=100 && code <=699);
		}
		
		private final boolean is2xx(short code){
			return (code >=200 && code <=299);
		}
		
		private final boolean is1xx(short code){
			return (code >=100 && code <=199);
		}
		
		/**
		 * Registration Events
		 */
		public int OnRegistrationChanged(RegistrationEvent e) {
			final short code = e.getCode();
			final String phrase = e.getPhrase();
			final tsip_register_event_type_t type = e.getType();
			
			if(!this.isSipCode(code) || this.is1xx(code)){
				return 0;
			}
			
			switch(type){
				case tsip_ao_register:
				case tsip_ao_unregister:
				{
					boolean registering = (type == tsip_register_event_type_t.tsip_ao_register);
					if(this.is2xx(code)){ /* Success */
						// Update sip service state
						this.sipService.regSession.setConnected(registering ? true : false);
						// raise event
						RegistrationEventArgs eargs = new RegistrationEventArgs(registering ? RegistrationEventTypes.REGISTRATION_OK : RegistrationEventTypes.UNREGISTRATION_OK, 
								code, phrase);
						this.sipService.onRegistrationChanged(eargs);
					}
					else{ /* Failure */
						RegistrationEventArgs eargs = new RegistrationEventArgs(registering ? RegistrationEventTypes.REGISTRATION_NOK : RegistrationEventTypes.UNREGISTRATION_NOK, code, phrase);
						this.sipService.onRegistrationChanged(eargs);
					}
					break;
				}
				
				default:
					break;
			}
			return 0;
		}

		/**
		 * Subscription Events
		 */
		public int OnSubscriptionChanged(SubscriptionEvent e) {
			return 0;
		}
	}
	
	/* ===================== Sip Session Preferences ======================== */
	private class SipPrefrences
	{
		private boolean rcs;
		private boolean xcapdiff;
		private boolean remoteStorage;
		private boolean preslist;
		private boolean deferredMsg;
		private boolean presence;
		private boolean messageSummary;
		
		private SipPrefrences(){
			
		}
	}
}
