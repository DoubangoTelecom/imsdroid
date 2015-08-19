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
package org.doubango.ngn.services;

import org.doubango.ngn.sip.NgnPresenceStatus;
import org.doubango.ngn.sip.NgnSipStack;
import org.doubango.ngn.sip.NgnSipSession.ConnectionState;

import android.content.Context;

/**@page NgnSipService_page SIP/IMS Service
 * This service is used to manage the SIP/IMS stack. You should never create or start this service by yourself. <br />
 * An instance of this service could be retrieved like this:
 * @code
 * final INgnSipService mSipService = NgnEngine.getInstance().getSipService();
 * @endcode
 * 
 * <h1>Audio/Video calls</h1>
 * <h2>Making audio call</h2>
 * To get notified about the audio/video call state please see @ref anchor_Listening_for_audio_video_call_state "here"<br/>
 * @code
 * final String remoteUri = "+33600000000";
 * final String validUri = NgnUriUtils.makeValidSipUri(remoteUri); // sip:+33600000000"@doubango.org
 * NgnAVSession avSession = NgnAVSession.createOutgoingSession(mSipService.getSipStack(), NgnMediaType.Audio);
 * if(avSession.makeCall(validUri)){
 * 	Log.d(TAG,"all is ok");
 * }
 * else{
 * Log.e(TAG,"Failed to place the call");
 * }
 * @endcode
 * <h2>Making video call</h2>
 * To get notified about the audio/video call state please see @ref anchor_Listening_for_audio_video_call_state "here"<br/>
 * @code
 * final String remoteUri = "+33600000000";
 * final String validUri = NgnUriUtils.makeValidSipUri(remoteUri); // sip:+33600000000"@doubango.org
 * NgnAVSession avSession = NgnAVSession.createOutgoingSession(mSipService.getSipStack(), NgnMediaType.AudioVideo);
 * if(avSession.makeCall(validUri)){
 * 	Log.d(TAG,"all is ok");
 * }
 * else{
 * Log.e(TAG,"Failed to place the call");
 * }
 * @endcode
 * 
 * <h1>SMS and Chat</h1>
 * <h2>3GPP Binary SMS</h2>
 * @code
 * final String SMSC = "sip:+3310000000@doubango.org"; // SMS Center
 * final String remotePartyUri = "sip:+336000000@doubango.org"; // remote party
 * final String textToSend = "hello world!";
 * final NgnMessagingSession imSession = NgnMessagingSession.createOutgoingSession(mSipService.getSipStack(), 
 * remotePartyUri);
 * if(!imSession.SendBinaryMessage(textToSend,SMSC)){
 * 	Log.e(TAG,"Failed to send");
 * }
 * else{
 * Log.d(TAG,"Message sent");
 * }
 * // release session
 * NgnMessagingSession.releaseSession(imSession);
 * @endcode
 * 
 * <h2>Pager Mode IM</h2>
 * @code
 * final String textToSend = "hello world!";
 * final String remotePartyUri = "sip:+336000000@doubango.org"; // remote party
 * final NgnMessagingSession imSession = NgnMessagingSession.createOutgoingSession(mSipService.getSipStack(), 
 * remotePartyUri);
 * if(!imSession.sendTextMessage(textToSend)){
 * 	Log.e(TAG,"Failed to send");
 * }
 * else{
 * Log.d(TAG,"Message sent");
 * }
 * // release session
 * NgnMessagingSession.releaseSession(imSession);
 * @endcode
 * 
 * <h1>Listening to events</h1>
 * The SIP/IMS service is responsible for all task related to the SIP protocol (Registration, audio/video calls, Pager mode IM, Presence, ...) and you can subscribe
 * to the event changed in order to get notified when the registration state change, new SIP MESSAGE is received, new incoming audio/video call, ...<br />
 * All notifications are sent to you in asynchronous way which mean that you don't need to query for them more than once.
 * 
 * <h2>Listening for registration state change</h2>
 * You can listen to the registration state change in order to get notified when you are logged in/out.
 * @code
 * final TextView mTvInfo = (TextView)findViewById(R.id.textViewInfo);
 * final BroadcastReceiver mSipBroadCastRecv = new BroadcastReceiver() {
 * 			@Override
 * 			public void onReceive(Context context, Intent intent) {
 * 				final String action = intent.getAction();
 * 				
 * 				// Registration Event
 * 				if(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
 * 					NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
 * 					if(args == null){
 * 						Log.e(TAG, "Invalid event args");
 * 						return;
 * 					}
 * 					switch(args.getEventType()){
 * 						case REGISTRATION_NOK:
 * 							mTvInfo.setText("Failed to register :(");
 * 							break;
 * 						case UNREGISTRATION_OK:
 * 							mTvInfo.setText("You are now unregistered :)");
 * 							break;
 * 						case REGISTRATION_OK:
 * 							mTvInfo.setText("You are now registered :)");
 * 							break;
 * 						case REGISTRATION_INPROGRESS:
 * 							mTvInfo.setText("Trying to register...");
 * 							break;
 * 						case UNREGISTRATION_INPROGRESS:
 * 							mTvInfo.setText("Trying to unregister...");
 * 							break;
 * 						case UNREGISTRATION_NOK:
 * 							mTvInfo.setText("Failed to unregister :(");
 * 							break;
 * 					}
 * 				}
 * 			}
 * 		};
 * 		final IntentFilter intentFilter = new IntentFilter();
 * 		intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
 * 	    registerReceiver(mSipBroadCastRecv, intentFilter);
 * @endcode
 * 
 * <h2>Listening for audio/video call state change</h2>
 *  @anchor anchor_Listening_for_audio_video_call_state
 * You can listen to the audio/video call state change in order to get notified when the call state change (incoming, incall, outgoing, terminated, ...).
 * @code
 * final BroadcastReceiver mSipBroadCastRecv = new BroadcastReceiver() {
 * 		@Override
 * 		public void onReceive(Context context, Intent intent) {
 * 			InviteState state;
 * 			final String action = intent.getAction();
 * 			if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)){
 * 				NgnInviteEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
 * 				if(args == null){
 * 					Log.e(TAG, "Invalid event args");
 * 					return;
 * 				}
 * 				Log.d(TAG, "This is an event for session number "+args.getSessionId());		
 * 				// Retrieve the session from the store
 * 				NgnAVSession avSession = NgnAVSession.getSession(args.getSessionId());
 * 				if(avSession == null){
 * 					Log.e(TAG, "Cannot find session");
 * 					return;
 * 				}
 * 				switch((state = avSession.getState())){
 * 					case NONE:
 * 					default:
 * 						break;
 * 						
 * 					case INCOMING:
 * 						Log.i(TAG, "Incoming call");
 * 						break;
 * 						
 * 					case INPROGRESS:
 * 						Log.i(TAG, "Call in progress");
 * 						break;
 * 						
 * 					case REMOTE_RINGING:
 * 						Log.i(TAG, "Remote party is ringing");
 * 						break;
 * 						
 * 					case EARLY_MEDIA:
 * 						Log.i(TAG, "Early media started");
 * 						break;
 * 						
 * 					case INCALL:
 * 						Log.i(TAG, "Call connected");
 * 						break;
 * 						
 * 					case TERMINATING:
 * 						Log.i(TAG, "Call terminating");
 * 						break;
 * 						
 * 					case TERMINATED:
 * 						Log.i(TAG, "Call terminated");
 * 						break;
 * 				}
 * 			}
 * 		}
 * 	};
 * @endcode
 * 
 * <h1>Configuration</h1>
 * Before trying to register to the SIP/IMS server you must configure the stack with your credentials.<br />
 * The configuration service is responsible of this task. <b>All preferences defined using the configuration service
 * are persistent which means that you can retrieve them when the application/device restarts</b>.
 * To configure the stack you must get an instance of the configuration service from the engine like this:
 * @code
 * final INgnConfigurationService mConfigurationService = NgnEngine.getInstance().getConfigurationService();
 * @endcode
 * 
 * <h3>Realm</h3>
 * The <b>realm</b> is the name of the domain to authenticate to. It should be a valid SIP URI (e.g.
 * <i>sip:open-ims.test</i> or <i>sip:10.0.0.1</i>). <br />
 * The <b>realm</b> is mandatory and should be set before the stack starts. You should never change its
 * value once the stack is started. If the address of the Proxy-CSCF is missing, then the stack will
 * automatically use DNS NAPTR+SRV and/or DHCP mechanisms for dynamic discovery. <br />
 * The value of the <b>realm</b> will be used as domain name for the DNS NAPTR query. For more information about
 * how to set the Proxy-CSCF IP address and port, please refer to section 22.1.8.
 * @code 
 * final String myRealm = "sip:doubango.org"; 
 * final boolean bSaveNow = true;
 * mConfigurationService(ConfigurationEntry.NETWORK_REALM, myRealm, bSaveNow);
 * @endcode
 * 
 * <h3>IMS Private Identity (IMPI)</h3>
 * The IMS Private Identity (a.k.a <b>IMPI</b>) is a unique identifier assigned to a user (or UE) by the home network. It could be either a
 * SIP URI (e.g. <i>sip:bob@open-ims.test</i>), a tel URI (e.g. <i>tel:+33100000</i>) or any alphanumeric string
 * (e.g. <i>bob@open-ims.test</i> or <i>bob</i>). It is used to authenticate the UE (username field in SIP Digest
 * Authorization/Proxy-Authorization header). <br />
 * In the real world, it should be stored in an UICC (Universal Integrated Circuit Card).
 * For those using this IMS stack as a basic (IETF) SIP stack, the IMPU should coincide with their
 * authentication name. <br />
 * The <b>IMPI is mandatory</b> and should be set before the stack starts. You should never change the
 * <b>IMPI</b> once the stack is started.
 * @code 
 * final String myIMPI = "33446677887"; 
 * final boolean bSaveNow = true;
 * mConfigurationService(ConfigurationEntry.IDENTITY_IMPI, myIMPI, bSaveNow);
 * @endcode
 * 
 * <h3>IMS Public Identity (IMPU)</h3>
 * As its name says, it’s you public visible identifier where you are willing to receive calls or any
 * demands. An IMPU could be either a SIP or tel URI (e.g. <i>tel:+33100000</i> or <i>sip:bob@open-ims.test</i>).
 * In IMS world, a user can have multiple IMPUs associated to its unique IMPI.<br />
 * For those using this IMS stack as basic SIP stack, the IMPU should coincide with their SIP URI (a.k.a
 * SIP address).<br />
 * The <b>IMPU is mandatory</b> and should be set before the stack starts. You should never change the
 * IMPU once the stack is started (instead, change the P-Preferred-Identity if you want to change your
 * default public identifier).
 * @code
 * final boolean bSaveNow = true; 
 * final String myIMPU = "sip:33446677887@doubango.org"; 
 * mConfigurationService(ConfigurationEntry.IDENTITY_IMPU, myIMPU, bSaveNow);
 * @endcode
 * 
 * <h3>Preferred Identity</h3>
 * As a user has multiple IMPUs, it can for each outgoing request, defines which IMPU to use by
 * setting the preferred identity. The user should check that this IPMU is not barred. An IMPU is
 * barred if it doesn’t appear in the associated URIs returned in the 200 OK REGISTER. <br />
 * By default, the preferred identity is the first URI in the list of the associated identities. If the IMPU
 * used to REGISTER the user is barred, then the stack will use the default URI returned by the SCSCF. <br />
 * You should never manually set this SIP header (P-Preferred-Identity); it’s up to the stack.
 * 
 * <h3>Proxy-CSCF Host address </h3>
 * The Proxy-CSCF Host is the IP address (192.168.0.1) or FQDN (doubango.org) of the SIP registrar. <br />
 * You should set the Proxy-CSCF address and IP only if required. Dynamic discovery mechanisms
 * (DNS NAPTR and/or DHCPv4/v6) should be used.
 * The code below shows how to set the Proxy-CSCF IP address and Port. If the port is missing, then
 * its default value will be 5060.
 * @code
 * // Sets IP address
 * final String proxyHost = "192.168.0.1";
 * mConfigurationService(ConfigurationEntry.NETWORK_PCSCF_HOST, proxyHost);
 * // Sets port 
 * final int proxyPort = 5060;
 * mConfigurationService.putInt(ConfigurationEntry.NETWORK_PCSCF_PORT, proxyPort);
 * Save changes
 * mConfigurationService.commit();
 * @endcode
 */
public interface INgnSipService extends INgnBaseService {
	String getDefaultIdentity();
	void setDefaultIdentity(String identity);
	/**
	 * Gets the underlaying SIP/IMS stack managed by this service. This function should only be called after
	 * successful registration.
	 * @return a valid SIP/IMS stack if succeed and null otherwise
	 * @sa @ref register()
	 */
    NgnSipStack getSipStack();
    /**
     * Checks whether we are already registered or not.
     * @return
     */
    boolean isRegistered();
    /**
     * Gets the registration state
     * @return the registration state
     */
    ConnectionState getRegistrationState();
    boolean isXcapEnabled();
    boolean isPublicationEnabled();
    boolean isSubscriptionEnabled();
    boolean isSubscriptionToRLSEnabled();
    /**
     * Gets the list of all active codecs
     * @return the list of all active codecs
     */
    int getCodecs();
    /**
     * Sets the list of all active codecs
     * @param coddecs the new codecs to activate
     */
    void setCodecs(int coddecs);

    byte[] getSubRLSContent();
    byte[] getSubRegContent();
    byte[] getSubMwiContent();
    byte[] getSubWinfoContent();

    /**
     * Stops the SIP/IMS stack. Before stopping the stack we the engine will hangup all calls and 
     * shutdown all active sip sessions.
     * @return true if succeed and false otherwise
     */
    boolean stopStack();
    /**
     * Sends a Sip REGISTER request to the Proxy-CSCF
     * @param context the context associated to this request. Could be null.
     * @return true if succeed and false otherwise
     * @sa @ref unRegister()
     */
    boolean register(Context context);
    /**
     * Deregisters the user by sending a Sip REGISTER request with an expires value equal to zero
     * @return true if succeed and false otherwise
     * @sa register
     */
    boolean unRegister();

    boolean PresencePublish();
    boolean PresencePublish(NgnPresenceStatus status);
}
