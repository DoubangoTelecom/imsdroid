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

package org.doubango.imsdroid.Model;

import java.util.ArrayList;
import java.util.List;

import org.doubango.imsdroid.sip.PresenceStatus;
import org.doubango.imsdroid.utils.StringUtils;
import org.doubango.tinyWRAP.tdav_codec_id_t;
import org.doubango.tinyWRAP.tmedia_qos_strength_t;
import org.doubango.tinyWRAP.tmedia_qos_stype_t;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "configuration")
public class Configuration {

	@ElementList(inline=true, required=false)
	private List<ConfigurationSection> sections;

	public static enum CONFIGURATION_SECTION {
		IDENTITY, GENERAL, LTE, NETWORK, QOS, RCS, SECURITY, SESSIONS, MEDIA, NATT, XCAP
	}

	public static final String PCSCF_DISCOVERY_NONE = "None";
	public static final String PCSCF_DISCOVERY_DNS = "DNS NAPTR+SRV";
	
	// Default values
	public static final String DEFAULT_DISPLAY_NAME = "johndoe";
	public static final String DEFAULT_IMPI = "johndoe@open-ims.test";
	public static final String DEFAULT_IMPU = "sip:johndoe@open-ims.test";
	
	public static final boolean DEFAULT_EARLY_IMS = false;
	public static final String DEFAULT_IP_VERSION = "ipv4";
	public static final String DEFAULT_PCSCF_DISCOVERY = "None";
	public static final String DEFAULT_PCSCF_HOST = "127.0.0.1";
	public static final int DEFAULT_PCSCF_PORT = 5060;
	public static final String DEFAULT_REALM = "sip:open-ims.test";
	public static final boolean DEFAULT_SIGCOMP = false;
	public static final String DEFAULT_TRANSPORT = "udp";
	
	public static final boolean DEFAULT_GENERAL_FULL_SCREEN_VIDEO = true;
	public static final boolean DEFAULT_GENERAL_AUTOSTART = true;
	public static final float DEFAULT_GENERAL_AUDIO_PLAY_LEVEL = 0.25f;
	public static final String DEFAULT_GENERAL_ENUM_DOMAIN = "e164.org";
	
	public static final String DEFAULT_RCS_AVATAR_PATH = "";
	public static final boolean DEFAULT_RCS_BINARY_SMS = true;
	public static final String DEFAULT_RCS_CONF_FACT = "sip:Conference-Factory@open-ims.test";
	public static final String DEFAULT_RCS_FREE_TEXT = "Hello world";
	public static final boolean DEFAULT_RCS_MSRP_FAILURE = true;
	public static final boolean DEFAULT_RCS_MSRP_SUCCESS = false;
	public static final boolean DEFAULT_RCS_MWI = false;
	public static final boolean DEFAULT_RCS_OMAFDR = false;
	public static final boolean DEFAULT_RCS_PARTIAL_PUB = false;
	public static final boolean DEFAULT_RCS_PRESENCE = false;
	public static final boolean DEFAULT_RCS_RLS = true;
	public static final String DEFAULT_RCS_SMSC = "sip:smsc@open-ims.test";
	public static final PresenceStatus DEFAULT_RCS_STATUS = PresenceStatus.Online;
	
	public static final String DEFAULT_QOS_PRECOND_BANDWIDTH = "Low";
	public static final String DEFAULT_QOS_PRECOND_STRENGTH = tmedia_qos_strength_t.tmedia_qos_strength_optional.toString();
	public static final String DEFAULT_QOS_PRECOND_TYPE = tmedia_qos_stype_t.tmedia_qos_stype_segmented.toString();
	public static final String DEFAULT_QOS_REFRESHER = "none";
	public static final int DEFAULT_QOS_SIP_SESSIONS_TIMEOUT = 600000;
	public static final int DEFAULT_QOS_SIP_CALLS_TIMEOUT = 3600;
	public static final boolean DEFAULT_QOS_SESSION_TIMERS = false;
	
	public static final String DEFAULT_TLS_CA_FILE = "";
	public static final String DEFAULT_TLS_PRIV_KEY_FILE = "";
	public static final String DEFAULT_TLS_PUB_KEY_FILE = "";
	public static boolean DEFAULT_TLS_SEC_AGREE = false;
	public static final String DEFAULT_IMSAKA_AMF = "0x0000";
	public static final String DEFAULT_IMSAKA_OPID = "0x00000000000000000000000000000000";
	
	public static final int DEFAULT_NATT_HACK_AOR_TIMEOUT = 2000;
	public static final boolean DEFAULT_NATT_HACK_AOR = false;
	public static final boolean DEFAULT_NATT_USE_STUN = false;
	public static final boolean DEFAULT_NATT_USE_ICE = false;
	public static final boolean DEFAULT_NATT_STUN_DISCO = false;
	public static final String DEFAULT_NATT_STUN_SERVER = "numb.viagenie.ca";
	public static final int DEFAULT_NATT_STUN_PORT = 3478;
	
	public static final boolean DEFAULT_XCAP_ENABLED = false;
	public static final String DEFAULT_XCAP_ROOT = "http://doubango.org:8080/services";
	public static final String DEFAULT_XUI = "sip:johndoe@open-ims.test";
	
	public static final int DEFAULT_MEDIA_CODECS = 
			tdav_codec_id_t.tdav_codec_id_gsm.swigValue() |
			tdav_codec_id_t.tdav_codec_id_pcma.swigValue() |
			tdav_codec_id_t.tdav_codec_id_pcmu.swigValue() |
			tdav_codec_id_t.tdav_codec_id_speex_nb.swigValue() |
			
			tdav_codec_id_t.tdav_codec_id_theora.swigValue() |
			tdav_codec_id_t.tdav_codec_id_h264_bp10.swigValue() |
			tdav_codec_id_t.tdav_codec_id_h263.swigValue() |
			tdav_codec_id_t.tdav_codec_id_h263p.swigValue();
	
	public static enum CONFIGURATION_ENTRY {
		/* === IDENTITY === */
		DISPLAY_NAME, IMPI, IMPU, PASSWORD,

		/* === GENERAL === */
		FULL_SCREEN_VIDEO, AUTOSTART, AUDIO_PLAY_LEVEL, ENUM_DOMAIN,

		/* === LTE === */

		/* === NETWORK === */
		EARLY_IMS, IP_VERSION, PCSCF_DISCOVERY, PCSCF_HOST, PCSCF_PORT, REALM, SIGCOMP, TRANSPORT,

		/* === QOS/QOE === */
		PRECOND_BANDWIDTH, PRECOND_STRENGTH, PRECOND_TYPE, REFRESHER, SIP_CALLS_TIMEOUT, SIP_SESSIONS_TIMEOUT, SESSION_TIMERS,

		/* === RCS (GSMA Rich Communication Suite) === */
		AVATAR_PATH, BINARY_SMS, CONF_FACT, FREE_TEXT, MSRP_FAILURE, MSRP_SUCCESS, MWI, OMAFDR, PARTIAL_PUB, PRESENCE, RLS, SMSC, STATUS,

		/* === SECURITY === */
		TLS_CA_FILE, TLS_PRIV_KEY_FILE, TLS_PUB_KEY_FILE, TLS_SEC_AGREE, IMSAKA_AMF, IMSAKA_OPID,

		/* === SESSIONS === */

		/* === MEDIA === */
		CODECS,

		/* === NATT === */
		HACK_AOR, HACK_AOR_TIMEOUT, USE_STUN, USE_ICE, STUN_DISCO, STUN_SERVER, STUN_PORT, 
		
		/* === XCAP === */
		/* PASSWORD */ ENABLED, XCAP_ROOT, USERNAME
	}

	public Configuration() {
		this.sections = new ArrayList<ConfigurationSection>();
	}

//	public boolean addSection(String name) {
//		if (!StringUtils.isNullOrEmpty(name)) {
//			ConfigurationSection section = new ConfigurationSection(name);
//			if (this.sections.contains(section)) {
//				return false;
//			} else {
//				this.sections.add(section);
//				return true;
//			}
//		}
//		return false;
//	}

	public boolean setEntry(String sectionName, String entryKey, String entryValue) {
		if(StringUtils.isNullOrEmpty(sectionName) || StringUtils.isNullOrEmpty(entryKey)) {
			return false;
		}
		ConfigurationSection section = new ConfigurationSection(sectionName);
		int index = this.sections.indexOf(section);
		if(index == -1){
			section.addEntry(new ConfigurationSectionEntry(entryKey, entryValue));
			return this.sections.add(section);
		}
		else{
			section = this.sections.get(index);
			section.addEntry(new ConfigurationSectionEntry(entryKey, entryValue));
			//this.sections.set(index, section);
			return true;
		}
	}

	public String getValue(String sectionName, String entryKey) {
		if (!StringUtils.isNullOrEmpty(sectionName)
				&& !StringUtils.isNullOrEmpty(entryKey)) {
			for (ConfigurationSection section : this.sections) {
				if (StringUtils.equals(section.getName(), sectionName, false)) {
					ConfigurationSectionEntry entry = section.getEntry(entryKey);
					return (entry == null) ? null : entry.getValue();
				}
			}
		}
		return null;
	}
}
