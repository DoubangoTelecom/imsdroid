/*
* Copyright (C) 2010 Mamadou Diop.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango.org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*
*/
package org.doubango.imsdroid.Services.Impl;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.doubango.imsdroid.CustomDialog;
import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.HistorySMSEvent;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Model.HistoryEvent.StatusType;
import org.doubango.imsdroid.Screens.ScreenAV;
import org.doubango.imsdroid.Screens.ScreenFileTransferView;
import org.doubango.imsdroid.Screens.ScreenMsrpInc;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.events.EventHandler;
import org.doubango.imsdroid.events.IInviteEventHandler;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.ISubscriptionEventHandler;
import org.doubango.imsdroid.events.InviteEventArgs;
import org.doubango.imsdroid.events.InviteEventTypes;
import org.doubango.imsdroid.events.RegistrationEventArgs;
import org.doubango.imsdroid.events.RegistrationEventTypes;
import org.doubango.imsdroid.events.SubscriptionEventArgs;
import org.doubango.imsdroid.events.SubscriptionEventTypes;
import org.doubango.imsdroid.media.MediaType;
import org.doubango.imsdroid.sip.MyAVSession;
import org.doubango.imsdroid.sip.MyInviteSession;
import org.doubango.imsdroid.sip.MyMsrpSession;
import org.doubango.imsdroid.sip.MyPublicationSession;
import org.doubango.imsdroid.sip.MyRegistrationSession;
import org.doubango.imsdroid.sip.MySipStack;
import org.doubango.imsdroid.sip.MySubscriptionSession;
import org.doubango.imsdroid.sip.PresenceStatus;
import org.doubango.imsdroid.sip.MySipStack.STACK_STATE;
import org.doubango.imsdroid.sip.MySubscriptionSession.EVENT_PACKAGE_TYPE;
import org.doubango.imsdroid.utils.ContentType;
import org.doubango.imsdroid.utils.StringUtils;
import org.doubango.imsdroid.utils.UriUtils;
import org.doubango.tinyWRAP.CallSession;
import org.doubango.tinyWRAP.DDebugCallback;
import org.doubango.tinyWRAP.DialogEvent;
import org.doubango.tinyWRAP.InviteEvent;
import org.doubango.tinyWRAP.InviteSession;
import org.doubango.tinyWRAP.MessagingEvent;
import org.doubango.tinyWRAP.MessagingSession;
import org.doubango.tinyWRAP.MsrpSession;
import org.doubango.tinyWRAP.OptionsEvent;
import org.doubango.tinyWRAP.OptionsSession;
import org.doubango.tinyWRAP.PublicationEvent;
import org.doubango.tinyWRAP.RPMessage;
import org.doubango.tinyWRAP.RegistrationEvent;
import org.doubango.tinyWRAP.SMSData;
import org.doubango.tinyWRAP.SMSEncoder;
import org.doubango.tinyWRAP.SdpMessage;
import org.doubango.tinyWRAP.SipCallback;
import org.doubango.tinyWRAP.SipMessage;
import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.SipStack;
import org.doubango.tinyWRAP.StackEvent;
import org.doubango.tinyWRAP.SubscriptionEvent;
import org.doubango.tinyWRAP.SubscriptionSession;
import org.doubango.tinyWRAP.tinyWRAPConstants;
import org.doubango.tinyWRAP.tsip_invite_event_type_t;
import org.doubango.tinyWRAP.tsip_message_event_type_t;
import org.doubango.tinyWRAP.tsip_options_event_type_t;
import org.doubango.tinyWRAP.tsip_subscribe_event_type_t;
import org.doubango.tinyWRAP.twrap_media_type_t;
import org.doubango.tinyWRAP.twrap_sms_type_t;

import android.content.DialogInterface;
import android.os.ConditionVariable;
import android.util.Log;
import android.widget.Toast;

public class SipService extends Service 
implements ISipService, tinyWRAPConstants {

	private final static String TAG = SipService.class.getCanonicalName();

	// Services
	private final IConfigurationService configurationService;
	private final INetworkService networkService;

	// Event Handlers
	private final CopyOnWriteArrayList<IRegistrationEventHandler> registrationEventHandlers;
	private final CopyOnWriteArrayList<ISubscriptionEventHandler> subscriptionEventHandlers;
	private final CopyOnWriteArrayList<IInviteEventHandler> inviteEventHandlers;

	private byte[] reginfo;
	private byte[] winfo;
	
	private MySipStack sipStack;
	private final MySipCallback sipCallback;
	
	private MyRegistrationSession regSession;
	private MySubscriptionSession subReg;
	private MySubscriptionSession subWinfo;
	private MySubscriptionSession subMwi;
	//private MySubscriptionSession subDebug;
	private MyPublicationSession pubPres;
	private final CopyOnWriteArrayList<MySubscriptionSession> subPres;
	
	private final SipPrefrences preferences;
	private final DDebugCallback debugCallback;

	private ConditionVariable condHack;

	public SipService() {
		super();

		this.sipCallback = new MySipCallback(this);
		// FIXME: to be set to null in the release version
		this.debugCallback = new DDebugCallback();

		this.registrationEventHandlers = new CopyOnWriteArrayList<IRegistrationEventHandler>();
		this.subscriptionEventHandlers = new CopyOnWriteArrayList<ISubscriptionEventHandler>();
		this.inviteEventHandlers = new CopyOnWriteArrayList<IInviteEventHandler>();

		this.configurationService = ServiceManager.getConfigurationService();
		this.networkService = ServiceManager.getNetworkService();
		
		this.subPres = new CopyOnWriteArrayList<MySubscriptionSession>();
		
		this.preferences = new SipPrefrences();
	}

	public boolean start() {
		return true;
	}

	public boolean stop() {
		if(this.sipStack != null && this.sipStack.getState() == STACK_STATE.STARTED){
			this.sipStack.stop();
		}
		return true;
	}

	public boolean isRegistered() {
		if (this.regSession != null) {
			return this.regSession.isConnected();
		}
		return false;
	}

	public MySipStack getStack(){
		return this.sipStack;
	}
	
	public byte[] getReginfo(){
		return this.reginfo;
	}
	
	public byte[] getWinfo(){
		return this.winfo;
	}
	
	public MySubscriptionSession createPresenceSession(String toUri, EVENT_PACKAGE_TYPE eventPackage){
		MySubscriptionSession session = new MySubscriptionSession(this.sipStack, toUri, eventPackage);
		this.subPres.add(session);
		return session;
	}
	
	public void clearPresenceSessions(){
		for(MySubscriptionSession session : this.subPres){
			if(session.isConnected()){
				session.unsubscribe();
			}
		}
		//this.subPres.clear();
	}
	
	public void removePresenceSession(MySubscriptionSession session){
		if(session.isConnected()){
			session.unsubscribe();
		}
		//this.subPres.remove(session);
	}
	
	/* ===================== SIP functions ======================== */

	public boolean stopStack(){
		if(this.sipStack != null){
			return this.sipStack.stop();
		}
		return true;
	}
	
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
			this.sipStack.setDebugCallback(this.debugCallback);
			SipStack.setCodecs_2(this.configurationService.getInt(CONFIGURATION_SECTION.MEDIA, 
	        		CONFIGURATION_ENTRY.CODECS, Configuration.DEFAULT_MEDIA_CODECS));
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

		// set the password
		this.sipStack.setPassword(this.configurationService.getString(
				CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.PASSWORD,
				null));
		
		// Set AMF
		this.sipStack.setAMF(this.configurationService.getString(
				CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.IMSAKA_AMF,
				Configuration.DEFAULT_IMSAKA_AMF));
		
		// Set Operator Id
		this.sipStack.setOperatorId(this.configurationService.getString(
				CONFIGURATION_SECTION.SECURITY, CONFIGURATION_ENTRY.IMSAKA_OPID,
				Configuration.DEFAULT_IMSAKA_OPID));
		
		// Check stack validity
		if (!this.sipStack.isValid()) {
			Log.e(this.getClass().getCanonicalName(), "Trying to use invalid stack");
			return false;
		}

		// Set STUN information
		if(this.configurationService.getBoolean(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.USE_STUN, Configuration.DEFAULT_NATT_USE_STUN)){			
			if(this.configurationService.getBoolean(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.STUN_DISCO, Configuration.DEFAULT_NATT_STUN_DISCO)){
				String domain = this.preferences.realm.substring(this.preferences.realm.indexOf(':')+1);
				int []port = new int[1];
				String server = this.sipStack.dnsSrv(String.format("_stun._udp.%s", domain), port);
				if(server == null){
					ServiceManager.getScreenService().setProgressInfoText("STUN discovery has failed");
				}
				this.sipStack.setSTUNServer(server, port[0]);// Needed event if null
			}
			else{
				String server = this.configurationService.getString(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.STUN_SERVER, Configuration.DEFAULT_NATT_STUN_SERVER);
				int port = this.configurationService.getInt(CONFIGURATION_SECTION.NATT, CONFIGURATION_ENTRY.STUN_PORT, Configuration.DEFAULT_NATT_STUN_PORT);
				this.sipStack.setSTUNServer(server, port);
			}
		}
		else{
			this.sipStack.setSTUNServer(null, 0);
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
		this.preferences.ipversion = "ipv4";/*this.configurationService.getString(
				CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.IP_VERSION,
				Configuration.DEFAULT_IP_VERSION);*/

		Log.i(this.getClass().getCanonicalName(), String.format(
				"pcscf-host=%s, pcscf-port=%d, transport=%s, ipversion=%s",
				this.preferences.pcscf_host, this.preferences.pcscf_port, this.preferences.transport, this.preferences.ipversion));

		if (!this.sipStack.setProxyCSCF(this.preferences.pcscf_host, this.preferences.pcscf_port, this.preferences.transport,
				this.preferences.ipversion)) {
			Log.e(this.getClass().getCanonicalName(), "Failed to set Proxy-CSCF parameters");
			return false;
		}

		// Set local IP (If your reusing this code on non-Android platforms, let
		// doubango retrieve the best IP address)
		boolean ipv6 = StringUtils.equals(this.preferences.ipversion, "ipv6", true);
		if ((this.preferences.localIP = this.networkService.getLocalIP(ipv6)) == null) {
			this.preferences.localIP = ipv6 ? "::" : "10.0.2.15"; /* Probably on the emulator */
		}
		if (!this.sipStack.setLocalIP(this.preferences.localIP)) {
			Log.e(this.getClass().getCanonicalName(), "Failed to set the local IP");
			return false;
		}

		// Whether to use DNS NAPTR+SRV for the Proxy-CSCF discovery (even if the DNS requests are sent only when the stack starts,
		// should be done after setProxyCSCF())
		String discoverType = this.configurationService.getString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.PCSCF_DISCOVERY, Configuration.PCSCF_DISCOVERY_NONE);
		this.sipStack.setDnsDiscovery(StringUtils.equals(discoverType, Configuration.PCSCF_DISCOVERY_DNS, true));		
		
		// enable/disable 3GPP early IMS
		this.sipStack.setEarlyIMS(this.configurationService.getBoolean(
				CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.EARLY_IMS,
				Configuration.DEFAULT_EARLY_IMS));
		
		// SigComp (only update compartment Id if changed)
		if(this.configurationService.getBoolean(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.SIGCOMP, Configuration.DEFAULT_SIGCOMP)){
			String compId = String.format("urn:uuid:%s", UUID.randomUUID().toString());
			this.sipStack.setSigCompId(compId);
		}
		else{
			this.sipStack.setSigCompId(null);
		}

		// Start the Stack
		if (!this.sipStack.start()) {
			Toast.makeText(IMSDroid.getContext(), "Failed to start the SIP stack", Toast.LENGTH_LONG).show();
			Log.e(SipService.TAG,
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
		this.preferences.mwi = this.configurationService.getBoolean(
				CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.MWI,
				Configuration.DEFAULT_RCS_MWI);
		
		// Create registration session
		if (this.regSession == null) {
			this.regSession = new MyRegistrationSession(this.sipStack);
		}
		else{
			this.regSession.setSigCompId(this.sipStack.getSigCompId());
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
			// optSession.setToUri(String.format("sip:%s@%s", "hacking_the_aor", this.preferences.realm));
			optSession.send();
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
			optSession.delete();
		}

		if (!this.regSession.register()) {
			Log.e(SipService.TAG, "Failed to send REGISTER request");
			return false;
		}

		return true;
	}

	public boolean unregister() {
		if (this.isRegistered()) {
			new Thread(new Runnable(){
				@Override
				public void run() {
					SipService.this.sipStack.stop();
				}
			}).start();
		}
		Log.d(this.getClass().getCanonicalName(), "Already unregistered");
		return true;
	}
	
	public boolean publish(){
		if(!this.isRegistered() || (this.pubPres == null)){
			return false;
		}
		
		if(!this.preferences.presence_enabled){
			return true; // silently ignore
		}
		
		String freeText = this.configurationService.getString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.FREE_TEXT, Configuration.DEFAULT_RCS_FREE_TEXT);
		PresenceStatus status = Enum.valueOf(PresenceStatus.class, this.configurationService.getString(
				CONFIGURATION_SECTION.RCS,
				CONFIGURATION_ENTRY.STATUS,
				Configuration.DEFAULT_RCS_STATUS.toString()));
		return this.pubPres.publish(status, freeText);
	}
	
	/* ===================== Add/Remove handlers ======================== */

	@Override
	public boolean addRegistrationEventHandler(IRegistrationEventHandler handler) {
		return EventHandler.addEventHandler(this.registrationEventHandlers, handler);
	}

	@Override
	public boolean removeRegistrationEventHandler(IRegistrationEventHandler handler) {
		return EventHandler.removeEventHandler(this.registrationEventHandlers, handler);
	}

	@Override
	public boolean addSubscriptionEventHandler(ISubscriptionEventHandler handler) {
		return EventHandler.addEventHandler(this.subscriptionEventHandlers, handler);
	}

	@Override
	public boolean removeSubscriptionEventHandler(ISubscriptionEventHandler handler) {
		return EventHandler.removeEventHandler(this.subscriptionEventHandlers, handler);
	}
	
	@Override
	public boolean addInviteEventHandler(IInviteEventHandler handler) {
		return EventHandler.addEventHandler(this.inviteEventHandlers, handler);
	}

	@Override
	public boolean removeInviteEventHandler(IInviteEventHandler handler) {
		return EventHandler.removeEventHandler(this.inviteEventHandlers, handler);
	}

	/* ===================== Dispatch events ======================== */
	private synchronized void onRegistrationEvent(final RegistrationEventArgs eargs) {
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
	
	private synchronized void onSubscriptionEvent(final SubscriptionEventArgs eargs) {
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
	
	private synchronized void onInviteEvent(final InviteEventArgs eargs) {
		for(int i = 0; i<this.inviteEventHandlers.size(); i++){
			final IInviteEventHandler handler = this.inviteEventHandlers.get(i);
			if(handler.canHandle(eargs.getSessionId())){
				new Thread(new Runnable() {
					public void run() {
						if (!handler.onInviteEvent(this, eargs)) {
							Log.w(handler.getClass().getName(), "onInviteEvent failed");
						}
					}
				}).start();
			}
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
		
		// Message Waiting Indication
		if(this.preferences.mwi){
			if(this.subMwi == null){
				this.subMwi = new MySubscriptionSession(this.sipStack, this.preferences.impu, EVENT_PACKAGE_TYPE.MESSAGE_SUMMARY); 
			}
			else{
				this.subMwi.setToUri(this.preferences.impu);
				this.subMwi.setFromUri(this.preferences.impu);
				this.subMwi.setSigCompId(this.sipStack.getSigCompId());
			}
			this.subMwi.subscribe();
		}
		
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
					this.subMwi.setSigCompId(this.sipStack.getSigCompId());
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
				this.subMwi.setSigCompId(this.sipStack.getSigCompId());
			}
			
			String freeText = this.configurationService.getString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.FREE_TEXT, Configuration.DEFAULT_RCS_FREE_TEXT);
			PresenceStatus status = Enum.valueOf(PresenceStatus.class, this.configurationService.getString(
					CONFIGURATION_SECTION.RCS,
					CONFIGURATION_ENTRY.STATUS,
					Configuration.DEFAULT_RCS_STATUS.toString()));
			this.pubPres.publish(status, freeText);
		}
	}

	/* ===================== Sip Callback ======================== */
	private class MySipCallback extends SipCallback {

		private final SipService sipService;

		private MySipCallback(SipService sipService) {
			super();

			this.sipService = sipService;
		}

		@Override
		public int OnRegistrationEvent(RegistrationEvent e) {
			return 0;
		}
		
		@Override
		public int OnPublicationEvent(PublicationEvent e) {			
			return 0;
		}

		@Override
		public int OnMessagingEvent(MessagingEvent e){			
			final tsip_message_event_type_t type = e.getType();
			
			switch(type){
				case tsip_ao_message:
					/* String phrase = e.getPhrase(); */
					/* short code = e.getCode(); */
					break;
				case tsip_i_message:
					final SipMessage message = e.getSipMessage();
					MessagingSession session = e.getSession();
					if (session == null){
		             /* "Server-side-session" e.g. Initial MESSAGE sent by the remote party */
		                session = e.takeSessionOwnership();
					}
					
					if(session == null){
						Log.e(SipService.TAG, "Failed to take session ownership");
					}
					
					if(message == null){
						session.reject();
						session.delete();
						return 0;
					}
					
					String from = message.getSipHeaderValue("f");
					final String contentType = message.getSipHeaderValue("c");
					final byte[] bytes = message.getSipContent();
					byte[] content = null;
					
					if(bytes == null || bytes.length ==0){
						Log.e(SipService.TAG, "Invalid MESSAGE");
						session.reject();
						session.delete();
						return 0;
					}
					
					session.accept();
					session.delete();
					
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
                                	SMSC = ServiceManager.getConfigurationService().getString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.SMSC, Configuration.DEFAULT_RCS_SMSC);
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

                                            MessagingSession m = new MessagingSession(SipService.this.sipStack);
                                            m.setToUri(SMSC);
                                            m.addHeader("Content-Type", ContentType.SMS_3GPP);
                                            m.addHeader("Content-Transfer-Encoding", "binary");
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

                                            MessagingSession m = new MessagingSession(SipService.this.sipStack);
                                            m.setToUri(SMSC);
                                            m.addHeader("Content-Type", ContentType.SMS_3GPP);
                                            m.addHeader("Transfer-Encoding", "binary");
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
					
					/* Alert the user a,d add the message to the history */
					if(content != null){
						HistorySMSEvent event = new HistorySMSEvent(from);
						event.setStatus(StatusType.Incoming);
						event.setContent(new String(content));
						ServiceManager.getHistoryService().addEvent(event);
						ServiceManager.showSMSNotif(R.drawable.sms_into_16, "New SMS");
						ServiceManager.getSoundService().playNewEvent();
					}
					
					break;
			}
			
			return 0;
		}
		
		@Override
		public int OnSubscriptionEvent(SubscriptionEvent e) {
			final tsip_subscribe_event_type_t type = e.getType();
			final SubscriptionSession session = e.getSession();
			
			if(session == null){
				return 0;
			}
			
			switch(type){
				case tsip_ao_subscribe:					
				case tsip_ao_unsubscribe:
					break;
					
				case tsip_i_notify:
					final short code = e.getCode();
					final String phrase = e.getPhrase();
					final SipMessage message = e.getSipMessage();
					if(message == null){
						return 0;
					}
					final String contentType = message.getSipHeaderValue("c");
					final byte[] content = message.getSipContent();
					
					if(content != null){
						if(StringUtils.equals(contentType, ContentType.REG_INFO, true)){
							this.sipService.reginfo = content;
						}
						else if(StringUtils.equals(contentType, ContentType.WATCHER_INFO, true)){
							this.sipService.winfo = content;
						}
						
						SubscriptionEventArgs eargs = new SubscriptionEventArgs(SubscriptionEventTypes.INCOMING_NOTIFY, 
								code, phrase, content, contentType);
						eargs.putExtra("session", session);
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
			final SipSession session = e.getBaseSession();
			if(session == null){
				return 0;
			}
			
			final long id = session.getId();
			MyInviteSession invSession;
			
			Log.d(SipService.TAG, String.format("OnDialogEvent (%s)", phrase));
			
			switch(code){
				case tsip_event_code_dialog_connecting:
					// Registration
					if((this.sipService.regSession != null) && (id == this.sipService.regSession.getId())){							
						this.sipService.onRegistrationEvent(new RegistrationEventArgs(
									RegistrationEventTypes.REGISTRATION_INPROGRESS, code, phrase));
					}
					// Audio/Video/Msrp Calls
					else if(((invSession = MyAVSession.getSession(id)) != null) || ((invSession = MyMsrpSession.getSession(id)) != null)){
						InviteEventArgs eargs = new InviteEventArgs(id, InviteEventTypes.INPROGRESS, phrase);
						eargs.putExtra("session", invSession);
						this.sipService.onInviteEvent(eargs); 
					}
					// Subscription
					// Publication
					// ...
					break;
					
				case tsip_event_code_dialog_connected:
					// Registration
					if((this.sipService.regSession != null) && (id == this.sipService.regSession.getId())){
						this.sipService.regSession.setConnected(true);
						new Thread(new Runnable(){
							public void run() {
								SipService.this.doPostRegistrationOp();
							}
						}).start();
						
						this.sipService.onRegistrationEvent(new RegistrationEventArgs(
									RegistrationEventTypes.REGISTRATION_OK, code, phrase));
					}
					// Presence Publication
					else if((this.sipService.pubPres != null) && (id == this.sipService.pubPres.getId())){							
						this.sipService.pubPres.setConnected(true);
					}
					// Audio/Video/Msrp Calls
					else if(((invSession = MyAVSession.getSession(id)) != null) || ((invSession = MyMsrpSession.getSession(id)) != null)){
						invSession.setConnected(true);
						this.sipService.onInviteEvent(new InviteEventArgs(id, InviteEventTypes.CONNECTED, phrase)); 
					}
					// Publication
					// Subscription
					else{
						for(MySubscriptionSession s : this.sipService.subPres){
							if(s.getId() == id){
								s.setConnected(true);
								SubscriptionEventArgs eargs = new SubscriptionEventArgs(SubscriptionEventTypes.SUBSCRIPTION_OK, 
										code, phrase, null, "null");
								eargs.putExtra("session", s);
								this.sipService.onSubscriptionEvent(eargs);
							}
						}
					}
					//..
					break;
					
				case tsip_event_code_dialog_terminating:
					// Registration
					if((this.sipService.regSession != null) && (id == this.sipService.regSession.getId())){						
						this.sipService.onRegistrationEvent(new RegistrationEventArgs(
									RegistrationEventTypes.UNREGISTRATION_INPROGRESS, code, phrase));
					}
					// Audio/Video/Msrp Calls
					if(MyAVSession.getSession(id) != null || MyMsrpSession.getSession(id) != null){
						this.sipService.onInviteEvent(new InviteEventArgs(id, InviteEventTypes.TERMWAIT, phrase)); 
					}
					// Subscription
					// Publication
					// ...
					break;
				
				case tsip_event_code_dialog_terminated:
					if((this.sipService.regSession != null) && (id == this.sipService.regSession.getId())){
						this.sipService.regSession.setConnected(false);
						this.sipService.onRegistrationEvent(new RegistrationEventArgs(
									RegistrationEventTypes.UNREGISTRATION_OK, code, phrase));
						/* Stop the stack (as we are already in the stack-thread, then do it in a new thread) */
						new Thread(new Runnable(){
							public void run() {	
								if(SipService.this.sipStack.getState() == STACK_STATE.STARTED){
									SipService.this.sipStack.stop();
								}
							}
						}).start();
					}
					// Presence Publication
					else if((this.sipService.pubPres != null) && (id == this.sipService.pubPres.getId())){							
						this.sipService.pubPres.setConnected(false);
					}
					// Audio/Video/Msrp Calls
					else if(((invSession = MyAVSession.getSession(id)) != null) || ((invSession = MyMsrpSession.getSession(id)) != null)){
						invSession.setConnected(false);
						this.sipService.onInviteEvent(new InviteEventArgs(id, InviteEventTypes.DISCONNECTED, phrase)); 
					}
					// Publication
					// Subscription
					else{
						for(MySubscriptionSession s : this.sipService.subPres){
							if(s.getId() == id){
								SubscriptionEventArgs eargs = new SubscriptionEventArgs(SubscriptionEventTypes.UNSUBSCRIPTION_OK, 
										code, phrase, null, "null");
								s.setConnected(false);
								eargs.putExtra("session", s);
								this.sipService.onSubscriptionEvent(eargs);
								this.sipService.subPres.remove(s);
							}
						}
					}
					// ...
					break;
					
				default:
					break;
			}
			
			return 0;
		}	
		
		@Override
		public int OnStackEvent(StackEvent e) {
			//final String phrase = e.getPhrase();
			final short code = e.getCode();
			switch(code){
				case tsip_event_code_stack_started:
					this.sipService.sipStack.setState(STACK_STATE.STARTED);
					Log.d(SipService.TAG, "Stack started");
					break;
				case tsip_event_code_stack_failed_to_start:
					final String phrase = e.getPhrase();
					ServiceManager.getScreenService().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try{
								// http://stackoverflow.com/questions/1561803/android-progressdialog-show-crashes-with-getapplicationcontext
								CustomDialog.show(IMSDroid.getContext(), R.drawable.delete_48, "Failed to start the IMS stack", 
										String.format("\nPlease check your connection information. \nAdditional info:\n%s", phrase),
												"OK", new DialogInterface.OnClickListener(){
													@Override
													public void onClick(DialogInterface dialog, int which) {
													}
										}, null, null);
							}
							catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(IMSDroid.getContext(), String.format("Please check your connection information. \nAdditional info:\n%s", phrase), Toast.LENGTH_LONG).show();
							}
						}
					});
										
					Log.e(SipService.TAG, "Failed to start the stack");
					break;
				case tsip_event_code_stack_failed_to_stop:
					Log.e(SipService.TAG, "Failed to stop the stack");
					break;
				case tsip_event_code_stack_stopped:
					this.sipService.sipStack.setState(STACK_STATE.STOPPED);
					Log.d(SipService.TAG, "Stack stoped");
					break;
			}
			return 0;
		}

		@Override
		public int OnInviteEvent(InviteEvent e) {
			//short code = e.getCode();
			String phrase = e.getPhrase();
			tsip_invite_event_type_t type = e.getType();
			//SipMessage message = e.getSipMessage();
			InviteSession session = e.getSession();

			switch(type){
				case tsip_i_newcall:
					if (session != null){ /* As we are not the owner, then the session MUST be null */
                        Log.e(SipService.TAG, "Invalid incoming session");
                        session.hangup();
                        return 0;
                    }
					else{
						SipMessage message = e.getSipMessage();
						twrap_media_type_t sessionType = e.getMediaType();
						if(message == null){
							Log.e(SipService.TAG, "Invalid message");
	                        return 0;
						}
						
						/* === MSRP === */
						if(sessionType == twrap_media_type_t.twrap_media_msrp){
							if ((session = e.takeMsrpSessionOwnership()) == null){
								Log.e(SipService.TAG, "Failed to take sesion ownership");
								return 0;
							}						
							
							final MyMsrpSession msrpSession = MyMsrpSession.takeIncomingSession(this.sipService.sipStack, (MsrpSession)session, message);
							if(msrpSession == null){
								Log.e(SipService.TAG, "Failed to create new session");
								session.hangup();
								session.delete();
								return 0;
							}
							
							switch(msrpSession.getMediaType()){
								case FileTransfer:
								case Chat:
									ScreenMsrpInc.receiveInvite(msrpSession);
									break;
								default:
									break;
							}
							
							InviteEventArgs eargs = new InviteEventArgs(msrpSession.getId(), InviteEventTypes.INCOMING, phrase);
	                    	eargs.putExtra("from", msrpSession.getRemoteParty());
	                    	this.sipService.onInviteEvent(eargs);
						}
						/* === Audio/Video === */
						else{
							if ((session = e.takeCallSessionOwnership()) == null){
								Log.e(SipService.TAG, "Failed to take sesion ownership");
								return 0;
							}
							final MediaType mediaType;
                    		switch(e.getMediaType()){
                    			case twrap_media_audio:
                    				mediaType = MediaType.Audio;
                    				break;
                    			case twrap_media_video:
                    				mediaType = MediaType.Video;
                    				break;
                    			case twrap_media_audiovideo:
                    				mediaType = MediaType.AudioVideo;
                    				break;
                    			default:
                    				session.hangup();
                        			return 0;
                    		}
                    		
                    		
                    		final String fromUri = message.getSipHeaderValue("f");
                    		final MyAVSession avSession = MyAVSession.takeIncomingSession(this.sipService.sipStack, (CallSession)session, mediaType);
                    		avSession.setRemoteParty(fromUri);
							ScreenAV.receiveCall(avSession);
							
							InviteEventArgs eargs = new InviteEventArgs(avSession.getId(), InviteEventTypes.INCOMING, phrase);
	                    	eargs.putExtra("from", fromUri);
	                    	this.sipService.onInviteEvent(eargs);
						}
					}
					break;
				case tsip_ao_request:
					short code = e.getCode();
					if(code == 180 && session!= null){
						InviteEventArgs eargs = new InviteEventArgs(session.getId(), InviteEventTypes.RINGING, phrase);
                    	this.sipService.onInviteEvent(eargs);
					}				
					break;
				case tsip_i_request:
				case tsip_o_ect_ok:
				case tsip_o_ect_nok:
				case tsip_i_ect:
					break;
				case tsip_m_early_media:
					this.sipService.onInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.EARLY_MEDIA, phrase));
					break;
				case tsip_m_local_hold_ok:
					this.sipService.onInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.LOCAL_HOLD_OK, phrase));
					break;
				case tsip_m_local_hold_nok:
					this.sipService.onInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.LOCAL_HOLD_NOK, phrase));
					break;
				case tsip_m_local_resume_ok:
					this.sipService.onInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.LOCAL_RESUME_OK, phrase));
					break;
				case tsip_m_local_resume_nok:
					this.sipService.onInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.LOCAL_RESUME_NOK, phrase));
					break;
				case tsip_m_remote_hold:
					this.sipService.onInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.REMOTE_HOLD, phrase));
					break;
				case tsip_m_remote_resume:
					this.sipService.onInviteEvent(new InviteEventArgs(session.getId(), InviteEventTypes.REMOTE_RESUME, phrase));
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
		//private boolean rcs;
		//private boolean xcapdiff;
		private boolean xcap_enabled;
		//private boolean preslist;
		//private boolean deferredMsg;
		private boolean presence_enabled;
		private boolean mwi;
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
