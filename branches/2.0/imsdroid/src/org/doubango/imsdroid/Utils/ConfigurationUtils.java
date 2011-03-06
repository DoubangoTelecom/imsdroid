package org.doubango.imsdroid.Utils;


import org.doubango.imsdroid.Sip.PresenceStatus;
import org.doubango.tinyWRAP.tdav_codec_id_t;
import org.doubango.tinyWRAP.tmedia_qos_strength_t;
import org.doubango.tinyWRAP.tmedia_qos_stype_t;

public class ConfigurationUtils {
	
	public static final String PCSCF_DISCOVERY_DNS_SRV = "DNS NAPTR+SRV";
	
	// General
	public static final boolean DEFAULT_GENERAL_AUTOSTART = true;
	public static final boolean DEFAULT_GENERAL_FULL_SCREEN_VIDEO = true;
	public static final boolean DEFAULT_GENERAL_INTERCEPT_OUTGOING_CALLS = true;
	public static final boolean DEFAULT_GENERAL_USE_FFC = true;
	public static final boolean DEFAULT_GENERAL_FLIP_VIDEO = false;
	public static final float DEFAULT_GENERAL_AUDIO_PLAY_LEVEL = 0.25f;
	public static final String DEFAULT_GENERAL_ENUM_DOMAIN = "e164.org";
	
	//	Identity
	public static final String DEFAULT_IDENTITY_DISPLAY_NAME = "John Doe";
	public static final String DEFAULT_IDENTITY_IMPU = "sip:johndoe@doubango.org";
	public static final String DEFAULT_IDENTITY_IMPI = "johndoe";
	public static final String DEFAULT_IDENTITY_CODE = null;
	public static final String DEFAULT_IDENTITY_PASSWORD = null;
	
	// Network
	public static final int DEFAULT_NETWORK_REGISTRATION_TIMEOUT = 1700;//FIXME
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
	public static final String DEFAULT_QOS_PRECOND_BANDWIDTH = "Low";
	public static final String DEFAULT_QOS_PRECOND_STRENGTH = tmedia_qos_strength_t.tmedia_qos_strength_none.toString();
	public static final String DEFAULT_QOS_PRECOND_TYPE = tmedia_qos_stype_t.tmedia_qos_stype_none.toString();
	public static final String DEFAULT_QOS_REFRESHER = "none";
	public static final int DEFAULT_QOS_SIP_SESSIONS_TIMEOUT = 600000;
	public static final int DEFAULT_QOS_SIP_CALLS_TIMEOUT = 3600;
	public static final boolean DEFAULT_QOS_USE_SESSION_TIMERS = false;
	
	// Media
	public static final int DEFAULT_MEDIA_CODECS = 
		tdav_codec_id_t.tdav_codec_id_gsm.swigValue() |
		tdav_codec_id_t.tdav_codec_id_pcma.swigValue() |
		tdav_codec_id_t.tdav_codec_id_pcmu.swigValue() |
		tdav_codec_id_t.tdav_codec_id_speex_nb.swigValue() |
		
		tdav_codec_id_t.tdav_codec_id_mp4ves_es.swigValue() |
		tdav_codec_id_t.tdav_codec_id_theora.swigValue() |
		tdav_codec_id_t.tdav_codec_id_h264_bp10.swigValue() |
		tdav_codec_id_t.tdav_codec_id_h263p.swigValue() |
		tdav_codec_id_t.tdav_codec_id_h263.swigValue();
	
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
	public static final boolean DEFAULT_RCS_USE_BINARY_SMS = false; 
	public static final String DEFAULT_RCS_CONF_FACT = "sip:Conference-Factory@doubango.org";
	public static final String DEFAULT_RCS_FREE_TEXT = "Hello world";
	public static final boolean DEFAULT_RCS_HACK_SMS = false;
	public static final boolean DEFAULT_RCS_USE_MSRP_FAILURE = true;
	public static final boolean DEFAULT_RCS_USE_MSRP_SUCCESS = false;
	public static final boolean DEFAULT_RCS_USE_MWI = false;
	public static final boolean DEFAULT_RCS_USE_OMAFDR = false;
	public static final boolean DEFAULT_RCS_USE_PARTIAL_PUB = false;
	public static final boolean DEFAULT_RCS_USE_PRESENCE = false;
	public static final boolean DEFAULT_RCS_USE_RLS = false;
	public static final String DEFAULT_RCS_SMSC = "sip:+331000000000@doubango.org";
	public static final PresenceStatus DEFAULT_RCS_STATUS = PresenceStatus.Offline;
	
	public static enum ConfigurationEntry{
		// General
		GENERAL_AUTOSTART,
		GENERAL_FULL_SCREEN_VIDEO, 
		GENERAL_USE_FFC, 
		GENERAL_INTERCEPT_OUTGOING_CALLS, 
		GENERAL_VIDEO_FLIP, 
		GENERAL_AUDIO_PLAY_LEVEL, 
		GENERAL_ENUM_DOMAIN,
		
		// Identity
		IDENTITY_DISPLAY_NAME,
		IDENTITY_IMPU,
		IDENTITY_IMPI,
		IDENTITY_CODE,
		IDENTITY_PASSWORD,
		
		// Network
		NETWORK_REGISTRATION_TIMEOUT,
		NETWORK_REALM,
		NETWORK_USE_WIFI,
		NETWORK_USE_3G,
		NETWORK_USE_EARLY_IMS, 
		NETWORK_IP_VERSION, 
		NETWORK_PCSCF_DISCOVERY, 
		NETWORK_PCSCF_HOST, 
		NETWORK_PCSCF_PORT, 
		NETWORK_USE_SIGCOMP, 
		NETWORK_TRANSPORT,
		
		// NAT Traversal
		NATT_HACK_AOR, 
		NATT_HACK_AOR_TIMEOUT, 
		NATT_USE_STUN, 
		NATT_USE_ICE, 
		NATT_STUN_DISCO, 
		NATT_STUN_SERVER, 
		NATT_STUN_PORT,
		
		// QoS
		QOS_PRECOND_BANDWIDTH, 
		QOS_PRECOND_STRENGTH, 
		QOS_PRECOND_TYPE, 
		QOS_REFRESHER, 
		QOS_SIP_CALLS_TIMEOUT, 
		QOS_SIP_SESSIONS_TIMEOUT, 
		QOS_USE_SESSION_TIMERS,
		
		// Media
		MEDIA_CODECS,
		
		// Security
		SECURITY_IMSAKA_AMF,
		SECURITY_IMSAKA_OPID,
		
		// XCAP
		XCAP_PASSWORD,
		XCAP_USERNAME,
		XCAP_ENABLED, 
		XCAP_ROOT,
		
		// RCS (Rich Communication Suite)
		RCS_AVATAR_PATH, 
		RCS_USE_BINARY_SMS, 
		RCS_CONF_FACT,
		RCS_FREE_TEXT,
		RCS_HACK_SMS,
		RCS_USE_MSRP_FAILURE, 
		RCS_USE_MSRP_SUCCESS, 
		RCS_USE_MWI, 
		RCS_USE_OMAFDR, 
		RCS_USE_PARTIAL_PUB, 
		RCS_USE_PRESENCE, 
		RCS_USE_RLS, 
		RCS_SMSC, 
		RCS_STATUS
	}
}
