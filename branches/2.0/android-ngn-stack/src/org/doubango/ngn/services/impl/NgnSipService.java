/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
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
*/
package org.doubango.ngn.services.impl;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnInviteEventTypes;
import org.doubango.ngn.events.NgnMessagingEventArgs;
import org.doubango.ngn.events.NgnMessagingEventTypes;
import org.doubango.ngn.events.NgnPublicationEventArgs;
import org.doubango.ngn.events.NgnPublicationEventTypes;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventTypes;
import org.doubango.ngn.events.NgnSubscriptionEventArgs;
import org.doubango.ngn.events.NgnSubscriptionEventTypes;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnNetworkService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnInviteSession;
import org.doubango.ngn.sip.NgnMessagingSession;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.sip.NgnPresenceStatus;
import org.doubango.ngn.sip.NgnPublicationSession;
import org.doubango.ngn.sip.NgnRegistrationSession;
import org.doubango.ngn.sip.NgnSipPrefrences;
import org.doubango.ngn.sip.NgnSipSession;
import org.doubango.ngn.sip.NgnSipStack;
import org.doubango.ngn.sip.NgnSubscriptionSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.sip.NgnSipSession.ConnectionState;
import org.doubango.ngn.sip.NgnSipStack.STACK_STATE;
import org.doubango.ngn.sip.NgnSubscriptionSession.EventPackageType;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnContentType;
import org.doubango.ngn.utils.NgnDateTimeUtils;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;
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
import org.doubango.tinyWRAP.RPMessage;
import org.doubango.tinyWRAP.SMSData;
import org.doubango.tinyWRAP.SMSEncoder;
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

import android.content.Context;
import android.content.Intent;
import android.os.ConditionVariable;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


public class NgnSipService extends NgnBaseService 
implements INgnSipService, tinyWRAPConstants {
	private final static String TAG = NgnSipService.class.getCanonicalName();
	
	private NgnRegistrationSession mRegSession;
	private NgnSipStack mSipStack;
	private final MySipCallback mSipCallback;
	private final NgnSipPrefrences mPreferences;
	
	private final INgnConfigurationService mConfigurationService;
	private final INgnNetworkService mNetworkService;
	
	private ConditionVariable mCondHackAoR;
	
	public NgnSipService() {
		super();
		
		mSipCallback = new MySipCallback(this);
		mPreferences = new NgnSipPrefrences();
		
		mConfigurationService = NgnEngine.getInstance().getConfigurationService();
		mNetworkService = NgnEngine.getInstance().getNetworkService();
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
	public NgnSipStack getSipStack() {
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
	public ConnectionState getRegistrationState(){
		if (mRegSession != null) {
			return mRegSession.getConnectionState();
		}
		return ConnectionState.NONE;
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
		if(mSipStack != null){
			mSipStack.stop();
		}
		return false;
	}

	@Override
	public boolean register(Context context) {
		Log.d(TAG,"register()");
		mPreferences.setRealm(mConfigurationService.getString(NgnConfigurationEntry.NETWORK_REALM, 
				NgnConfigurationEntry.DEFAULT_NETWORK_REALM));
		mPreferences.setIMPI(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, 
				NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI));
		mPreferences.setIMPU(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPU, 
				NgnConfigurationEntry.DEFAULT_IDENTITY_IMPU));
		
		Log.d(TAG, String.format(
				"realm='%s', impu='%s', impi='%s'", mPreferences.getRealm(), mPreferences.getIMPU(), mPreferences.getIMPI()));
		
		if (mSipStack == null) {
			mSipStack = new NgnSipStack(mSipCallback, mPreferences.getRealm(), mPreferences.getIMPI(), mPreferences.getIMPU());	
			mSipStack.setDebugCallback(new DDebugCallback());
			SipStack.setCodecs_2(mConfigurationService.getInt(NgnConfigurationEntry.MEDIA_CODECS, 
					NgnConfigurationEntry.DEFAULT_MEDIA_CODECS));
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
				NgnConfigurationEntry.IDENTITY_PASSWORD, NgnConfigurationEntry.DEFAULT_IDENTITY_PASSWORD));
		// Set AMF
		mSipStack.setAMF(mConfigurationService.getString(
				NgnConfigurationEntry.SECURITY_IMSAKA_AMF, NgnConfigurationEntry.DEFAULT_SECURITY_IMSAKA_AMF));
		// Set Operator Id
		mSipStack.setOperatorId(mConfigurationService.getString(
				NgnConfigurationEntry.SECURITY_IMSAKA_OPID, NgnConfigurationEntry.DEFAULT_SECURITY_IMSAKA_OPID));
		
		// Check stack validity
		if (!mSipStack.isValid()) {
			Log.e(TAG, "Trying to use invalid stack");
			return false;
		}
		
		// Set STUN information
		if(mConfigurationService.getBoolean(NgnConfigurationEntry.NATT_USE_STUN, NgnConfigurationEntry.DEFAULT_NATT_USE_STUN)){			
			Log.d(TAG, "STUN=yes");
			if(mConfigurationService.getBoolean(NgnConfigurationEntry.NATT_STUN_DISCO, NgnConfigurationEntry.DEFAULT_NATT_STUN_DISCO)){
				final String realm = mPreferences.getRealm();
				String domain = realm.substring(realm.indexOf(':')+1);
				int []port = new int[1];
				String server = mSipStack.dnsSrv(String.format("_stun._udp.%s", domain), port);
				if(server == null){
					Log.e(TAG, "STUN discovery has failed");
				}
				Log.d(TAG, String.format("STUN1 - server=%s and port=%d", server, port[0]));
				mSipStack.setSTUNServer(server, port[0]);// Needed event if null
			}
			else{
				String server = mConfigurationService.getString(NgnConfigurationEntry.NATT_STUN_SERVER, 
						NgnConfigurationEntry.DEFAULT_NATT_STUN_SERVER);
				int port = mConfigurationService.getInt(NgnConfigurationEntry.NATT_STUN_PORT, 
						NgnConfigurationEntry.DEFAULT_NATT_STUN_PORT);
				Log.d(NgnSipService.TAG, String.format("STUN2 - server=%s and port=%d", server, port));
				mSipStack.setSTUNServer(server, port);
			}
		}
		else{
			Log.d(TAG, "STUN=no");
			mSipStack.setSTUNServer(null, 0);
		}
		
		// Set Proxy-CSCF
		mPreferences.setPcscfHost(mConfigurationService.getString(NgnConfigurationEntry.NETWORK_PCSCF_HOST,
				null)); // null will trigger DNS NAPTR+SRV
		mPreferences.setPcscfPort(mConfigurationService.getInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT,
				NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT));
		mPreferences.setTransport(mConfigurationService.getString(NgnConfigurationEntry.NETWORK_TRANSPORT,
				NgnConfigurationEntry.DEFAULT_NETWORK_TRANSPORT));
		mPreferences.setIPVersion(mConfigurationService.getString(NgnConfigurationEntry.NETWORK_IP_VERSION,
				NgnConfigurationEntry.DEFAULT_NETWORK_IP_VERSION));
		
		Log.d(TAG, String.format(
				"pcscf-host='%s', pcscf-port='%d', transport='%s', ipversion='%s'",
				mPreferences.getPcscfHost(), 
				mPreferences.getPcscfPort(),
				mPreferences.getTransport(),
				mPreferences.getIPVersion()));

		if (!mSipStack.setProxyCSCF(mPreferences.getPcscfHost(), mPreferences.getPcscfPort(), mPreferences.getTransport(),
				mPreferences.getIPVersion())) {
			Log.e(NgnSipService.TAG, "Failed to set Proxy-CSCF parameters");
			return false;
		}
		
		// Set local IP (If your reusing this code on non-Android platforms (iOS, Symbian, WinPhone, ...),
		// let Doubango retrieve the best IP address)
		boolean ipv6 = NgnStringUtils.equals(mPreferences.getIPVersion(), "ipv6", true);
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
		String discoverType = mConfigurationService.getString(NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY, NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_DISCOVERY);
		mSipStack.setDnsDiscovery(NgnStringUtils.equals(discoverType, NgnConfigurationEntry.PCSCF_DISCOVERY_DNS_SRV, true));		
		
		// enable/disable 3GPP early IMS
		mSipStack.setEarlyIMS(mConfigurationService.getBoolean(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS,
				NgnConfigurationEntry.DEFAULT_NETWORK_USE_EARLY_IMS));
		
		// SigComp (only update compartment Id if changed)
		if(mConfigurationService.getBoolean(NgnConfigurationEntry.NETWORK_USE_SIGCOMP, NgnConfigurationEntry.DEFAULT_NETWORK_USE_SIGCOMP)){
			String compId = String.format("urn:uuid:%s", UUID.randomUUID().toString());
			mSipStack.setSigCompId(compId);
		}
		else{
			mSipStack.setSigCompId(null);
		}
		
		// Start the Stack
		if (!mSipStack.start()) {
			if(context != null && Thread.currentThread() == Looper.getMainLooper().getThread()){
				Toast.makeText(context, "Failed to start the SIP stack", Toast.LENGTH_LONG).show();
			}
			Log.e(TAG, "Failed to start the SIP stack");
			return false;
		}
		
		// Preference values
		mPreferences.setXcapEnabled(mConfigurationService.getBoolean(NgnConfigurationEntry.XCAP_ENABLED,
				NgnConfigurationEntry.DEFAULT_XCAP_ENABLED));
		mPreferences.setPresenceEnabled(mConfigurationService.getBoolean(NgnConfigurationEntry.RCS_USE_PRESENCE,
				NgnConfigurationEntry.DEFAULT_RCS_USE_PRESENCE));
		mPreferences.setMWI(mConfigurationService.getBoolean(NgnConfigurationEntry.RCS_USE_MWI,
				NgnConfigurationEntry.DEFAULT_RCS_USE_MWI));
		
		// Create registration session
		if (mRegSession == null) {
			mRegSession = new NgnRegistrationSession(mSipStack);
		}
		else{
			mRegSession.setSigCompId(mSipStack.getSigCompId());
		}
		
		// Set/update From URI. For Registration ToUri should be equals to realm
		// (done by the stack)
		mRegSession.setFromUri(mPreferences.getIMPU());
		
		/* Before registering, check if AoR hacking id enabled */
		mPreferences.setHackAoR(mConfigurationService.getBoolean(NgnConfigurationEntry.NATT_HACK_AOR, 
				NgnConfigurationEntry.DEFAULT_NATT_HACK_AOR));
		if (mPreferences.isHackAoR()) {
			if (mCondHackAoR == null) {
				mCondHackAoR = new ConditionVariable();
			}
			final OptionsSession optSession = new OptionsSession(mSipStack);
			// optSession.setToUri(String.format("sip:%s@%s", "hacking_the_aor", this.preferences.realm));
			optSession.send();
			try {
				synchronized (mCondHackAoR) {
					mCondHackAoR.wait(mConfigurationService.getInt(NgnConfigurationEntry.NATT_HACK_AOR_TIMEOUT,
							NgnConfigurationEntry.DEFAULT_NATT_HACK_AOR_TIMEOUT));
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
	public boolean PresencePublish(NgnPresenceStatus status) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void broadcastRegistrationEvent(NgnRegistrationEventArgs args){
		final Intent intent = new Intent(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
		intent.putExtra(NgnRegistrationEventArgs.EXTRA_EMBEDDED, args);
		NgnApplication.getContext().sendBroadcast(intent);
	}
	
	private void broadcastInviteEvent(NgnInviteEventArgs args, short sipCode){
		final Intent intent = new Intent(NgnInviteEventArgs.ACTION_INVITE_EVENT);
		intent.putExtra(NgnInviteEventArgs.EXTRA_EMBEDDED, args);
		intent.putExtra(NgnInviteEventArgs.EXTRA_SIPCODE, sipCode);
		NgnApplication.getContext().sendBroadcast(intent);
	}
	
	private void broadcastInviteEvent(NgnInviteEventArgs args){
		final Intent intent = new Intent(NgnInviteEventArgs.ACTION_INVITE_EVENT);
		intent.putExtra(NgnInviteEventArgs.EXTRA_EMBEDDED, args);
		intent.putExtra(NgnInviteEventArgs.EXTRA_SIPCODE, 0);
		NgnApplication.getContext().sendBroadcast(intent);
	}
	
	private void broadcastMessagingEvent(NgnMessagingEventArgs args, String remoteParty, String date){
		final Intent intent = new Intent(NgnMessagingEventArgs.ACTION_MESSAGING_EVENT);
		intent.putExtra(NgnMessagingEventArgs.EXTRA_REMOTE_PARTY, remoteParty);
		intent.putExtra(NgnMessagingEventArgs.EXTRA_DATE, date);
		intent.putExtra(NgnMessagingEventArgs.EXTRA_EMBEDDED, args);
		NgnApplication.getContext().sendBroadcast(intent);
	}
	
	private void broadcastPublicationEvent(NgnPublicationEventArgs args){
		final Intent intent = new Intent(NgnPublicationEventArgs.ACTION_PUBLICATION_EVENT);
		intent.putExtra(NgnPublicationEventArgs.EXTRA_EMBEDDED, args);
		NgnApplication.getContext().sendBroadcast(intent);
	}
	
	private void broadcastSubscriptionEvent(NgnSubscriptionEventArgs args){
		final Intent intent = new Intent(NgnSubscriptionEventArgs.ACTION_SUBSCRIBTION_EVENT);
		intent.putExtra(NgnSubscriptionEventArgs.EXTRA_EMBEDDED, args);
		NgnApplication.getContext().sendBroadcast(intent);
	}
	
	/**
	 * MySipCallback
	 */
	static class MySipCallback extends SipCallback{
		private final NgnSipService mSipService;

		private MySipCallback(NgnSipService sipService) {
			super();

			mSipService = sipService;
		}
		
		@Override
		public int OnDialogEvent(DialogEvent e){
			final String phrase = e.getPhrase();
			final short eventCode = e.getCode();
			final short sipCode;
			final SipSession session = e.getBaseSession();
			
			if(session == null){
				return 0;
			}
			
			final long sessionId = session.getId();
			final SipMessage message = e.getSipMessage();
			NgnSipSession mySession = null;
			
			sipCode = (message != null && message.isResponse()) ? message.getResponseCode() : eventCode;
			
			Log.d(TAG, String.format("OnDialogEvent (%s,%d)", phrase,sessionId));
			
			switch (eventCode){
				//== Connecting ==
				case tinyWRAPConstants.tsip_event_code_dialog_connecting:
				{
					// Registration
                    if (mSipService.mRegSession != null && mSipService.mRegSession.getId() == sessionId){
                    	mSipService.mRegSession.setConnectionState(ConnectionState.CONNECTING);
                    	mSipService.broadcastRegistrationEvent(new NgnRegistrationEventArgs(sessionId, NgnRegistrationEventTypes.REGISTRATION_INPROGRESS, 
                    			eventCode, phrase));
                    }
                    // Audio/Video/MSRP(Chat, FileTransfer)
                    else if (((mySession = NgnAVSession.getSession(sessionId)) != null) || ((mySession = NgnMsrpSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTING);
                        ((NgnInviteSession)mySession).setState(InviteState.INPROGRESS);
                        mSipService.broadcastInviteEvent(new NgnInviteEventArgs(sessionId, NgnInviteEventTypes.INPROGRESS, ((NgnInviteSession)mySession).getMediaType(), phrase));
                    } 
                    // Publication
                    else if(((mySession = NgnPublicationSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTING);
                    	mSipService.broadcastPublicationEvent(new NgnPublicationEventArgs(sessionId, NgnPublicationEventTypes.PUBLICATION_INPROGRESS, 
                    			eventCode, phrase));
                    }
                    // Subscription
                    else if(((mySession = NgnSubscriptionSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTING);
                    	mSipService.broadcastSubscriptionEvent(new NgnSubscriptionEventArgs(sessionId, NgnSubscriptionEventTypes.SUBSCRIPTION_INPROGRESS, 
                    			eventCode, phrase, null, null, ((NgnSubscriptionSession)mySession).getEventPackage()));
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
                        if (!NgnStringUtils.isNullOrEmpty(_defaultIdentity)){
                        	mSipService.setDefaultIdentity(_defaultIdentity);
                        }
                        mSipService.broadcastRegistrationEvent(new NgnRegistrationEventArgs(sessionId, NgnRegistrationEventTypes.REGISTRATION_OK, 
                        		sipCode, phrase));
                    }
                    // Audio/Video/MSRP(Chat, FileTransfer)
                    else if (((mySession = NgnAVSession.getSession(sessionId)) != null) || ((mySession = NgnMsrpSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTED);
                    	((NgnInviteSession)mySession).setState(InviteState.INCALL);
                        mSipService.broadcastInviteEvent(new NgnInviteEventArgs(sessionId, NgnInviteEventTypes.CONNECTED, ((NgnInviteSession)mySession).getMediaType(), phrase));
                    }
                    // Publication
                    else if(((mySession = NgnPublicationSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTED);
                    	mSipService.broadcastPublicationEvent(new NgnPublicationEventArgs(sessionId, NgnPublicationEventTypes.PUBLICATION_OK, 
                    			sipCode, phrase));
                    }
                    // Subscription
                    else if(((mySession = NgnSubscriptionSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTED);
                    	mSipService.broadcastSubscriptionEvent(new NgnSubscriptionEventArgs(sessionId, NgnSubscriptionEventTypes.SUBSCRIPTION_OK, 
                    			sipCode, phrase, null, null, ((NgnSubscriptionSession)mySession).getEventPackage()));
                    }

					break;
				}
				
				//== Terminating == //
				case tinyWRAPConstants.tsip_event_code_dialog_terminating:
				{
					// Registration
					if (mSipService.mRegSession != null && mSipService.mRegSession.getId() == sessionId){
						mSipService.mRegSession.setConnectionState(ConnectionState.TERMINATING);
						mSipService.broadcastRegistrationEvent(new NgnRegistrationEventArgs(sessionId, NgnRegistrationEventTypes.UNREGISTRATION_INPROGRESS, 
								eventCode, phrase));
					}
					// Audio/Video/MSRP(Chat, FileTransfer)
                    else if (((mySession = NgnAVSession.getSession(sessionId)) != null) || ((mySession = NgnMsrpSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.TERMINATING);
                    	((NgnInviteSession)mySession).setState(InviteState.TERMINATING);
                    	mSipService.broadcastInviteEvent(new NgnInviteEventArgs(sessionId, NgnInviteEventTypes.TERMWAIT, ((NgnInviteSession)mySession).getMediaType(), phrase));
                    }
					// Publication
                    else if(((mySession = NgnPublicationSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.TERMINATING);
                    	mSipService.broadcastPublicationEvent(new NgnPublicationEventArgs(sessionId, NgnPublicationEventTypes.UNPUBLICATION_INPROGRESS, 
                    			eventCode, phrase));
                    }
                    // Subscription
                    else if(((mySession = NgnSubscriptionSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.TERMINATING);
                    	mSipService.broadcastSubscriptionEvent(new NgnSubscriptionEventArgs(sessionId, NgnSubscriptionEventTypes.UNSUBSCRIPTION_INPROGRESS, 
                    			eventCode, phrase, null, null, ((NgnSubscriptionSession)mySession).getEventPackage()));
                    }

					break;
				}
				
				//== Terminated == //
				case tinyWRAPConstants.tsip_event_code_dialog_terminated:
				{
					// Registration
					if (mSipService.mRegSession != null && mSipService.mRegSession.getId() == sessionId){
						mSipService.mRegSession.setConnectionState(ConnectionState.TERMINATED);
						mSipService.broadcastRegistrationEvent(new NgnRegistrationEventArgs(sessionId, NgnRegistrationEventTypes.UNREGISTRATION_OK, sipCode, phrase));
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
					else if(NgnMessagingSession.hasSession(sessionId)){
						NgnMessagingSession.releaseSession(sessionId);
					}
					// Audio/Video/MSRP(Chat, FileTransfer)
                    else if (((mySession = NgnAVSession.getSession(sessionId)) != null) || ((mySession = NgnMsrpSession.getSession(sessionId)) != null)){
                        mySession.setConnectionState(ConnectionState.TERMINATED);
                        ((NgnInviteSession)mySession).setState(InviteState.TERMINATED);
                        mSipService.broadcastInviteEvent(new NgnInviteEventArgs(sessionId, NgnInviteEventTypes.TERMINATED, ((NgnInviteSession)mySession).getMediaType(), phrase));
                        if(mySession instanceof NgnAVSession){
                        	NgnAVSession.releaseSession((NgnAVSession)mySession);
                        }
                        else if(mySession instanceof NgnMsrpSession){
                        	NgnMsrpSession.releaseSession((NgnMsrpSession)mySession);
                        }
                    }
					// Publication
                    else if(((mySession = NgnPublicationSession.getSession(sessionId)) != null)){
                    	ConnectionState previousConnState = mySession.getConnectionState();
                    	mySession.setConnectionState(ConnectionState.TERMINATED);
                    	mSipService.broadcastPublicationEvent(new NgnPublicationEventArgs(sessionId, 
                    			(previousConnState == ConnectionState.TERMINATING) ? NgnPublicationEventTypes.UNPUBLICATION_OK : NgnPublicationEventTypes.PUBLICATION_NOK, 
                    			sipCode, phrase));
                    }
                    // Subscription
                    else if(((mySession = NgnSubscriptionSession.getSession(sessionId)) != null)){
                    	ConnectionState previousConnState = mySession.getConnectionState();
                    	
                    	mySession.setConnectionState(ConnectionState.TERMINATED);
                    	mSipService.broadcastSubscriptionEvent(new NgnSubscriptionEventArgs(sessionId, 
                    			(previousConnState == ConnectionState.TERMINATING) ? NgnSubscriptionEventTypes.UNSUBSCRIPTION_OK : NgnSubscriptionEventTypes.SUBSCRIPTION_NOK, 
                    			sipCode, phrase, null, null, ((NgnSubscriptionSession)mySession).getEventPackage()));
                    }
					break;
				}
			}
			
			return 0;
		}
		
		@Override
		public int OnInviteEvent(InviteEvent e) {
			 final tsip_invite_event_type_t type = e.getType();
			 final short code = e.getCode();
			 final String phrase = e.getPhrase();
			 InviteSession session = e.getSession();
			 NgnSipSession mySession = null;
			
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
                            	if ((session = e.takeMsrpSessionOwnership()) == null){
                                    Log.e(TAG,"Failed to take MSRP session ownership");
                                    return -1;
                                }

                                NgnMsrpSession msrpSession = NgnMsrpSession.takeIncomingSession(mSipService.getSipStack(), 
                                		(MsrpSession)session, message);
                                if (msrpSession == null){
                                	Log.e(TAG,"Failed to create new session");
                                    session.hangup();
                                    session.delete();
                                    return 0;
                                }
                                mSipService.broadcastInviteEvent(new NgnInviteEventArgs(msrpSession.getId(), NgnInviteEventTypes.INCOMING, msrpSession.getMediaType(), phrase));
                                break;
                            }

                        case twrap_media_audio:
                        case twrap_media_audiovideo:
                        case twrap_media_video:
                            {
                                if ((session = e.takeCallSessionOwnership()) == null){
                                    Log.e(TAG,"Failed to take audio/video session ownership");
                                    return -1;
                                }
                                final NgnAVSession avSession = NgnAVSession.takeIncomingSession(mSipService.getSipStack(), (CallSession)session, sessionType, message); 
                                mSipService.broadcastInviteEvent(new NgnInviteEventArgs(avSession.getId(), NgnInviteEventTypes.INCOMING, avSession.getMediaType(), phrase));
                                break;
                            }

                        default:
                            Log.e(TAG,"Invalid media type");
                            return 0;
                        
                    }
                    break;

                case tsip_ao_request:
                	// For backward compatibility keep both "RINGING" and "SIP_RESPONSE"
                    if (code == 180 && session != null){
                    	if (((mySession = NgnAVSession.getSession(session.getId())) != null) || ((mySession = NgnMsrpSession.getSession(session.getId())) != null)){
                    		mSipService.broadcastInviteEvent(new NgnInviteEventArgs(mySession.getId(), NgnInviteEventTypes.RINGING, ((NgnInviteSession)mySession).getMediaType(), phrase));
                    	}
                    }
                    if(session != null){
                    	if (((mySession = NgnAVSession.getSession(session.getId())) != null) || ((mySession = NgnMsrpSession.getSession(session.getId())) != null)){
                    		mSipService.broadcastInviteEvent(new NgnInviteEventArgs(mySession.getId(), NgnInviteEventTypes.SIP_RESPONSE, ((NgnInviteSession)mySession).getMediaType(), phrase), code);
                    	}
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
                    	if (((mySession = NgnAVSession.getSession(session.getId())) != null) || ((mySession = NgnMsrpSession.getSession(session.getId())) != null)){
                    		((NgnInviteSession)mySession).setState(InviteState.EARLY_MEDIA);
                    		mSipService.broadcastInviteEvent(new NgnInviteEventArgs(session.getId(), NgnInviteEventTypes.EARLY_MEDIA, ((NgnInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
                case tsip_m_local_hold_ok:
                    {
                    	if (((mySession = NgnAVSession.getSession(session.getId())) != null) || ((mySession = NgnMsrpSession.getSession(session.getId())) != null)){
                    		((NgnInviteSession)mySession).setLocalHold(true);
                    		mSipService.broadcastInviteEvent(new NgnInviteEventArgs(session.getId(), NgnInviteEventTypes.LOCAL_HOLD_OK, ((NgnInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
                case tsip_m_local_hold_nok:
                    {
                    	if (((mySession = NgnAVSession.getSession(session.getId())) != null) || ((mySession = NgnMsrpSession.getSession(session.getId())) != null)){
                    		mSipService.broadcastInviteEvent(new NgnInviteEventArgs(session.getId(), NgnInviteEventTypes.LOCAL_HOLD_NOK, ((NgnInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
                case tsip_m_local_resume_ok:
                    {
                    	if (((mySession = NgnAVSession.getSession(session.getId())) != null) || ((mySession = NgnMsrpSession.getSession(session.getId())) != null)){
                    		((NgnInviteSession)mySession).setLocalHold(false);
                    		mSipService.broadcastInviteEvent(new NgnInviteEventArgs(session.getId(), NgnInviteEventTypes.LOCAL_RESUME_OK, ((NgnInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
                case tsip_m_local_resume_nok:
                    {
                    	if (((mySession = NgnAVSession.getSession(session.getId())) != null) || ((mySession = NgnMsrpSession.getSession(session.getId())) != null)){
                    		mSipService.broadcastInviteEvent(new NgnInviteEventArgs(session.getId(), NgnInviteEventTypes.LOCAL_RESUME_NOK, ((NgnInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
                case tsip_m_remote_hold:
                    {
                    	if (((mySession = NgnAVSession.getSession(session.getId())) != null) || ((mySession = NgnMsrpSession.getSession(session.getId())) != null)){
                    		((NgnInviteSession)mySession).setRemoteHold(true);
                    		mSipService.broadcastInviteEvent(new NgnInviteEventArgs(session.getId(), NgnInviteEventTypes.REMOTE_HOLD, ((NgnInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
                case tsip_m_remote_resume:
                    {
                    	if (((mySession = NgnAVSession.getSession(session.getId())) != null) || ((mySession = NgnMsrpSession.getSession(session.getId())) != null)){
                    		((NgnInviteSession)mySession).setRemoteHold(false);
                    		mSipService.broadcastInviteEvent(new NgnInviteEventArgs(session.getId(), NgnInviteEventTypes.REMOTE_RESUME, ((NgnInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
            }
			
			return 0;
		}
		
		@Override
		public int OnMessagingEvent(MessagingEvent e) {
			final tsip_message_event_type_t type = e.getType();
			MessagingSession _session;
			final SipMessage message;
			
			switch(type){
				case tsip_ao_message:
					_session = e.getSession();
					message = e.getSipMessage();
					short code = e.getCode();
					if(_session != null && code>=200 && message != null){
						mSipService.broadcastMessagingEvent(new NgnMessagingEventArgs(_session.getId(), 
								(code >=200 && code<=299) ? NgnMessagingEventTypes.SUCCESS : NgnMessagingEventTypes.FAILURE, 
								e.getPhrase(), new byte[0], null), message.getSipHeaderValue("f"), NgnDateTimeUtils.now());
					}
					break;
				case tsip_i_message:
					message = e.getSipMessage();
					_session = e.getSession();
					NgnMessagingSession imSession;
					if (_session == null){
		             /* "Server-side-session" e.g. Initial MESSAGE sent by the remote party */
						_session = e.takeSessionOwnership();
					}
					
					if(_session == null){
						Log.e(NgnSipService.TAG, "Failed to take session ownership");
						return -1;
					}
					imSession = NgnMessagingSession.takeIncomingSession(mSipService.mSipStack, _session, message);
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
						Log.e(NgnSipService.TAG, "Invalid MESSAGE");
						imSession.reject();
						imSession.decRef();
						return 0;
					}
					
					imSession.accept();
					
					if(NgnStringUtils.equals(contentType, NgnContentType.SMS_3GPP, true)){
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
                                	from = NgnUriUtils.makeValidSipUri(origPhoneNumber);
                                }
                                else if((origPhoneNumber = NgnUriUtils.getValidPhoneNumber(from)) == null){
                                	Log.e(NgnSipService.TAG, "Invalid destination address");
                                	return 0;
                                }
                                
                                /* SMS Center 
                                 * 3GPP TS 24.341 - 5.3.2.4	Sending a delivery report
                                 * The address of the IP-SM-GW is received in the P-Asserted-Identity header in the SIP MESSAGE 
                                 * request including the delivered short message.
                                 * */
                                if((SMSCPhoneNumber = NgnUriUtils.getValidPhoneNumber(SMSC)) == null){
                                	SMSC = NgnEngine.getInstance().getConfigurationService().getString(NgnConfigurationEntry.RCS_SMSC, NgnConfigurationEntry.DEFAULT_RCS_SMSC);
                                	if((SMSCPhoneNumber = NgnUriUtils.getValidPhoneNumber(SMSC)) == null){
                                		Log.e(NgnSipService.TAG, "Invalid IP-SM-GW address");
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
                                            m.addHeader("Content-Type", NgnContentType.SMS_3GPP);
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
                                            m.addHeader("Content-Type", NgnContentType.SMS_3GPP);
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
                            		Log.d(NgnSipService.TAG, "RP-ACK");
                            	}
                            	else if(smsType == twrap_sms_type_t.twrap_sms_type_error){
                            		/* Find message from the history (by MR) an update it's status */
                            		Log.d(NgnSipService.TAG, "RP-ERROR");
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
						mSipService.broadcastMessagingEvent(new NgnMessagingEventArgs(_session.getId(), NgnMessagingEventTypes.INCOMING, 
								e.getPhrase(), content, contentType), from, NgnDateTimeUtils.now());
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
					Log.d(NgnSipService.TAG, "Stack started");
					break;
				case tinyWRAPConstants.tsip_event_code_stack_failed_to_start:
					final String phrase = e.getPhrase();
					Log.e(TAG,String.format("Failed to start the stack. \nAdditional info:\n%s", phrase));
					break;
				case tinyWRAPConstants.tsip_event_code_stack_failed_to_stop:
					Log.e(TAG, "Failed to stop the stack");
					break;
				case tinyWRAPConstants.tsip_event_code_stack_stopped:
					mSipService.mSipStack.setState(STACK_STATE.STOPPED);
					Log.d(TAG, "Stack stopped");
					break;
			}
			return 0;
		}

		@Override
		public int OnSubscriptionEvent(SubscriptionEvent e) {
			final tsip_subscribe_event_type_t type = e.getType();
			SubscriptionSession _session = e.getSession();
			
			switch(type)
			{
				case tsip_i_notify:
				{
					 final short code = e.getCode();
                     final String phrase = e.getPhrase();
                     final SipMessage message = e.getSipMessage();
                     if(message == null || _session == null){
                         return 0;
                     }
                     final String contentType = message.getSipHeaderValue("c");
                     final byte[] content = message.getSipContent();
                     
                     if(NgnStringUtils.equals(contentType, NgnContentType.REG_INFO, true)){
                          //mReginfo = content;
                     }
                     else if(NgnStringUtils.equals(contentType, NgnContentType.WATCHER_INFO, true)){
                          // mWInfo = content;
                     }
                     
                     NgnSubscriptionSession ngnSession = NgnSubscriptionSession.getSession(_session.getId());
                     NgnSubscriptionEventArgs eargs = new NgnSubscriptionEventArgs(_session.getId(), 
                    		 NgnSubscriptionEventTypes.INCOMING_NOTIFY, 
                    		 code, 
                    		 phrase, 
                    		 content, 
                    		 contentType, 
                    		 ngnSession==null ? EventPackageType.None : ngnSession.getEventPackage());
                     mSipService.broadcastSubscriptionEvent(eargs);
                     
                     break;
				}
				
				case tsip_ao_notify:
				case tsip_i_subscribe:
				case tsip_ao_subscribe:
				case tsip_i_unsubscribe:
				case tsip_ao_unsubscribe:
				default:
				{
					break;
				}
			}
			
			return 0;
		}
		
		@Override
		public int OnOptionsEvent(OptionsEvent e) {
			final tsip_options_event_type_t type = e.getType();
			OptionsSession ptSession = e.getSession();

            switch (type){
                case tsip_i_options:
                    if (ptSession == null){ // New session
                        if ((ptSession = e.takeSessionOwnership()) != null){
                            ptSession.accept();
                            ptSession.delete();
                        }
                    }
                    break;
                default:
                    break;
            }
			return 0;
		}
		
	}
}
