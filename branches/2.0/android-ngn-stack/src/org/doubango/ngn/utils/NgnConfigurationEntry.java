/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*  Copyright (C) 2011, Philippe Verney <verney(dot)philippe(AT)gmail(dot)com>
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
package org.doubango.ngn.utils;

import org.doubango.ngn.sip.NgnPresenceStatus;
import org.doubango.tinyWRAP.tdav_codec_id_t;
import org.doubango.tinyWRAP.tmedia_bandwidth_level_t;
import org.doubango.tinyWRAP.tmedia_qos_strength_t;
import org.doubango.tinyWRAP.tmedia_qos_stype_t;


public class NgnConfigurationEntry {
	private static final String TAG = NgnConfigurationEntry.class.getCanonicalName();
	
	public final static String  SHARED_PREF_NAME = TAG;
	public static final String PCSCF_DISCOVERY_DNS_SRV = "DNS NAPTR+SRV";
	
	// General
	public static final String GENERAL_AUTOSTART = "GENERAL_AUTOSTART." + TAG;
	public static final String GENERAL_SHOW_WELCOME_SCREEN = "GENERAL_SHOW_WELCOME_SCREEN." + TAG;
	public static final String GENERAL_FULL_SCREEN_VIDEO = "GENERAL_FULL_SCREEN_VIDEO." + TAG;
	public static final String GENERAL_USE_FFC = "GENERAL_USE_FFC." + TAG;
	public static final String GENERAL_INTERCEPT_OUTGOING_CALLS = "GENERAL_INTERCEPT_OUTGOING_CALLS." + TAG;
	public static final String GENERAL_AUDIO_PLAY_LEVEL = "GENERAL_AUDIO_PLAY_LEVEL." + TAG;
	public static final String GENERAL_ENUM_DOMAIN = "GENERAL_ENUM_DOMAIN." + TAG;
	public static final String GENERAL_AEC = "GENERAL_AEC."+ TAG ;
	public static final String GENERAL_VAD = "GENERAL_VAD."+ TAG ;	
	public static final String GENERAL_NR = "GENERAL_NR."+ TAG ;	
	public static final String GENERAL_ECHO_TAIL = "GENERAL_ECHO_TAIL."+ TAG ;
	// Identity
	public static final String IDENTITY_DISPLAY_NAME = "IDENTITY_DISPLAY_NAME." + TAG;
	public static final String IDENTITY_IMPU = "IDENTITY_IMPU." + TAG;
	public static final String IDENTITY_IMPI = "IDENTITY_IMPI." + TAG;
	public static final String IDENTITY_PASSWORD = "IDENTITY_PASSWORD." + TAG;
	
	// Network
	public static final String NETWORK_REGISTRATION_TIMEOUT = "NETWORK_REGISTRATION_TIMEOUT." + TAG;
	public static final String NETWORK_REALM = "NETWORK_REALM." + TAG;
	public static final String NETWORK_USE_WIFI = "NETWORK_USE_WIFI." + TAG;
	public static final String NETWORK_USE_3G = "NETWORK_USE_3G." + TAG;
	public static final String NETWORK_USE_EARLY_IMS = "NETWORK_USE_EARLY_IMS." + TAG;
	public static final String NETWORK_IP_VERSION = "NETWORK_IP_VERSION." + TAG;
	public static final String NETWORK_PCSCF_DISCOVERY = "NETWORK_PCSCF_DISCOVERY." + TAG;
	public static final String NETWORK_PCSCF_HOST = "NETWORK_PCSCF_HOST." + TAG;
	public static final String NETWORK_PCSCF_PORT = "NETWORK_PCSCF_PORT." + TAG;
	public static final String NETWORK_USE_SIGCOMP = "NETWORK_USE_SIGCOMP." + TAG;
	public static final String NETWORK_TRANSPORT = "NETWORK_TRANSPORT." + TAG;
	
	// NAT Traversal
	public static final String NATT_HACK_AOR = "NATT_HACK_AOR." + TAG;
	public static final String NATT_HACK_AOR_TIMEOUT = "NATT_HACK_AOR_TIMEOUT." + TAG;
	public static final String NATT_USE_STUN = "NATT_USE_STUN." + TAG;
	public static final String NATT_USE_ICE = "NATT_USE_ICE." + TAG;
	public static final String NATT_STUN_DISCO = "NATT_STUN_DISCO." + TAG;
	public static final String NATT_STUN_SERVER = "NATT_STUN_SERVER." + TAG;
	public static final String NATT_STUN_PORT = "NATT_STUN_PORT." + TAG;
	
	// QoS
	public static final String QOS_PRECOND_BANDWIDTH_LEVEL = "QOS_PRECOND_BANDWIDTH_LEVEL." + TAG;
	public static final String QOS_PRECOND_STRENGTH = "QOS_PRECOND_STRENGTH." + TAG;
    public static final String QOS_PRECOND_TYPE = "QOS_PRECOND_TYPE." + TAG;
    public static final String QOS_REFRESHER = "QOS_REFRESHER." + TAG;
    public static final String QOS_SIP_CALLS_TIMEOUT = "QOS_SIP_CALLS_TIMEOUT." + TAG;
    public static final String QOS_SIP_SESSIONS_TIMEOUT = "QOS_SIP_SESSIONS_TIMEOUT" + TAG;
    public static final String QOS_USE_SESSION_TIMERS = "QOS_USE_SESSION_TIMERS." + TAG;

	
	// Media
	public static final String MEDIA_CODECS = "MEDIA_CODECS." + TAG;
	public static final String MEDIA_AUDIO_RESAMPLER_QUALITY = "MEDIA_AUDIO_RESAMPLER_QUALITY." + TAG;
	public static final String MEDIA_AUDIO_CONSUMER_GAIN = "MEDIA_AUDIO_CONSUMER_GAIN." + TAG;
	public static final String MEDIA_AUDIO_PRODUCER_GAIN = "MEDIA_AUDIO_PRODUCER_GAIN." + TAG;
	public static final String MEDIA_AUDIO_CONSUMER_ATTENUATION = "MEDIA_AUDIO_CONSUMER_ATTENUATION." + TAG;
	public static final String MEDIA_AUDIO_PRODUCER_ATTENUATION = "MEDIA_AUDIO_PRODUCER_ATTENUATION." + TAG;
	
	// Security
	public static final String SECURITY_IMSAKA_AMF = "SECURITY_IMSAKA_AMF." + TAG;
	public static final String SECURITY_IMSAKA_OPID = "SECURITY_IMSAKA_OPID." + TAG;
	
	// XCAP
	public static final String XCAP_PASSWORD = "XCAP_PASSWORD." + TAG;
	public static final String XCAP_USERNAME = "XCAP_USERNAME." + TAG;
	public static final String XCAP_ENABLED = "XCAP_ENABLED." + TAG;
	public static final String XCAP_XCAP_ROOT = "XCAP_XCAP_ROOT." + TAG;
	
	// RCS (Rich Communication Suite)
	public static final String RCS_AVATAR_PATH = "RCS_AVATAR_PATH." + TAG;
	public static final String RCS_USE_BINARY_SMS = "RCS_USE_BINARY_SMS." + TAG;
	public static final String RCS_CONF_FACT = "RCS_CONF_FACT." + TAG;
	public static final String RCS_FREE_TEXT = "RCS_FREE_TEXT." + TAG;
	public static final String RCS_HACK_SMS = "RCS_HACK_SMS." + TAG;
	public static final String RCS_USE_MSRP_FAILURE = "RCS_USE_MSRP_FAILURE." + TAG;
	public static final String RCS_USE_MSRP_SUCCESS = "RCS_USE_MSRP_SUCCESS." + TAG;
	public static final String RCS_USE_MWI = "RCS_USE_MWI." + TAG;
	public static final String RCS_USE_OMAFDR = "RCS_USE_OMAFDR." + TAG;
	public static final String RCS_USE_PARTIAL_PUB = "RCS_USE_PARTIAL_PUB." + TAG;
	public static final String RCS_USE_PRESENCE = "RCS_USE_PRESENCE." + TAG;
	public static final String RCS_USE_RLS = "RCS_USE_RLS." + TAG;
	public static final String RCS_SMSC = "RCS_SMSC." + TAG;
	public static final String RCS_STATUS  = "RCS_STATUS." + TAG;
	
	
	//
	//	Default values
	//
	
	// General
	public static final boolean DEFAULT_GENERAL_SHOW_WELCOME_SCREEN = true;
	public static final boolean DEFAULT_GENERAL_FULL_SCREEN_VIDEO = true;
	public static final boolean DEFAULT_GENERAL_INTERCEPT_OUTGOING_CALLS = true;
	public static final boolean DEFAULT_GENERAL_USE_FFC = true;
	public static final boolean DEFAULT_GENERAL_AUTOSTART = true;
	public static final float DEFAULT_GENERAL_AUDIO_PLAY_LEVEL = 0.25f;
	public static final String DEFAULT_GENERAL_ENUM_DOMAIN = "e164.org";
	public static final boolean DEFAULT_GENERAL_AEC = true;
	public static final boolean DEFAULT_GENERAL_VAD = false; // speex-dsp don't support VAD for fixed-point implementation
	public static final boolean DEFAULT_GENERAL_NR = true;
	public static final int DEFAULT_GENERAL_ECHO_TAIL = 500;
	
	//	Identity
	public static final String DEFAULT_IDENTITY_DISPLAY_NAME = "John Doe";
	public static final String DEFAULT_IDENTITY_IMPU = "sip:johndoe@doubango.org";
	public static final String DEFAULT_IDENTITY_IMPI = "johndoe";
	public static final String DEFAULT_IDENTITY_PASSWORD = null;
	
	// Network
	public static final int DEFAULT_NETWORK_REGISTRATION_TIMEOUT = 1700;
	public static final String DEFAULT_NETWORK_REALM = "doubango.org";
	public static final boolean DEFAULT_NETWORK_USE_WIFI = true;
	public static final boolean DEFAULT_NETWORK_USE_3G = false;
	public static final String DEFAULT_NETWORK_PCSCF_DISCOVERY = "None";
	public static final String DEFAULT_NETWORK_PCSCF_HOST = "127.0.0.1";
	public static final int DEFAULT_NETWORK_PCSCF_PORT = 5060;
	public static final boolean DEFAULT_NETWORK_USE_SIGCOMP = false;
	public static final String DEFAULT_NETWORK_TRANSPORT = "udp";
	public static final String DEFAULT_NETWORK_IP_VERSION = "ipv4";
	public static final boolean DEFAULT_NETWORK_USE_EARLY_IMS = false;
	
	
	// NAT Traversal
	public static final int DEFAULT_NATT_HACK_AOR_TIMEOUT = 2000;
	public static final boolean DEFAULT_NATT_HACK_AOR = false;
	public static final boolean DEFAULT_NATT_USE_STUN = false;
	public static final boolean DEFAULT_NATT_USE_ICE = false;
	public static final boolean DEFAULT_NATT_STUN_DISCO = false;
	public static final String DEFAULT_NATT_STUN_SERVER = "numb.viagenie.ca";
	public static final int DEFAULT_NATT_STUN_PORT = 3478;
	
	// QoS
    public static final int DEFAULT_QOS_PRECOND_BANDWIDTH_LEVEL = tmedia_bandwidth_level_t.tmedia_bl_unrestricted.swigValue();
    public static final String DEFAULT_QOS_PRECOND_STRENGTH = tmedia_qos_strength_t.tmedia_qos_strength_none.toString();
    public static final String DEFAULT_QOS_PRECOND_TYPE = tmedia_qos_stype_t.tmedia_qos_stype_none.toString();
    public static final String DEFAULT_QOS_REFRESHER = "none";
    public static final int DEFAULT_QOS_SIP_SESSIONS_TIMEOUT = 600000;
    public static final int DEFAULT_QOS_SIP_CALLS_TIMEOUT = 3600;
    public static final boolean DEFAULT_QOS_USE_SESSION_TIMERS = false;
	
	// Media
	public static final int DEFAULT_MEDIA_CODECS = 
		tdav_codec_id_t.tdav_codec_id_pcma.swigValue() |
		tdav_codec_id_t.tdav_codec_id_pcmu.swigValue() |
		
		tdav_codec_id_t.tdav_codec_id_mp4ves_es.swigValue() |
		tdav_codec_id_t.tdav_codec_id_h263p.swigValue() |
		tdav_codec_id_t.tdav_codec_id_h263.swigValue();
	public static final int DEFAULT_MEDIA_AUDIO_RESAMPLER_QUALITY = 0;
	public static final int DEFAULT_MEDIA_AUDIO_CONSUMER_GAIN = 0; // disabled
	public static final int DEFAULT_MEDIA_AUDIO_PRODUCER_GAIN = 0; // disabled
	public static final float DEFAULT_MEDIA_AUDIO_CONSUMER_ATTENUATION = 1f; // disabled
	public static final float DEFAULT_MEDIA_AUDIO_PRODUCER_ATTENUATION = 1f; // disabled
	
	// Security
	public static final String DEFAULT_SECURITY_IMSAKA_AMF = "0x0000";
	public static final String DEFAULT_SECURITY_IMSAKA_OPID = "0x00000000000000000000000000000000";
	
	// XCAP
	public static final boolean DEFAULT_XCAP_ENABLED = false;
	public static final String DEFAULT_XCAP_ROOT = "http://doubango.org:8080/services";
	public static final String DEFAULT_XCAP_USERNAME = "sip:johndoe@doubango.org";
	public static final String DEFAULT_XCAP_PASSWORD = null;
	
	// RCS (Rich Communication Suite)
	public static final String DEFAULT_RCS_AVATAR_PATH = "";
	public static final boolean DEFAULT_RCS_USE_BINARY_SM = false; 
	public static final String DEFAULT_RCS_CONF_FACT = "sip:Conference-Factory@doubango.org";
	public static final String DEFAULT_RCS_FREE_TEXT = "Hello world";
	public static final boolean DEFAULT_RCS_HACK_SMS = false;
	public static final boolean DEFAULT_RCS_USE_MSRP_FAILURE = true;
	public static final boolean DEFAULT_RCS_USE_MSRP_SUCCESS = false;
	public static final boolean DEFAULT_RCS_USE_BINARY_SMS = false;
	public static final boolean DEFAULT_RCS_USE_MWI = false;
	public static final boolean DEFAULT_RCS_USE_OMAFDR = false;
	public static final boolean DEFAULT_RCS_USE_PARTIAL_PUB = false;
	public static final boolean DEFAULT_RCS_USE_PRESENCE = false;
	public static final boolean DEFAULT_RCS_USE_RLS = false;
	public static final String DEFAULT_RCS_SMSC = "sip:+331000000000@doubango.org";
	public static final NgnPresenceStatus DEFAULT_RCS_STATUS = NgnPresenceStatus.Online;
	
}
