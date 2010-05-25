package org.doubango.imsdroid.Model;

import java.util.ArrayList;
import java.util.List;

import org.doubango.imsdroid.utils.StringUtils;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "configuration")
public class Configuration {

	@ElementList(inline=true, required=false)
	private List<ConfigurationSection> sections;

	public static enum CONFIGURATION_SECTION {
		IDENTITY, GENERAL, LTE, NETWORK, QOS, RCS, SECURITY, SESSIONS, MMTEL, XCAP
	}

	// Default values
	public static String DEFAULT_DISPLAY_NAME = "alice";
	public static String DEFAULT_IMPI = "alice@open-ims.test";
	public static String DEFAULT_IMPU = "sip:alice@open-ims.test";
	
	public static boolean DEFAULT_EARLY_IMS = false;
	public static String DEFAULT_IP_VERSION = "ipv4";
	public static String DEFAULT_PCSCF_DISCOVERY = "None";
	public static String DEFAULT_PCSCF_HOST = "127.0.0.1";
	public static int DEFAULT_PCSCF_PORT = 5060;
	public static String DEFAULT_REALM = "sip:open-ims.test";
	public static boolean DEFAULT_SIGCOMP = false;
	public static String DEFAULT_TRANSPORT = "udp";
	
	public static int DEFAULT_SIP_SESSIONS_TIMEOUT = 36; // For debug. FIXME: change to 3600 in release versions
	public static int DEFAULT_SIP_CALLS_TIMEOUT = 3600;
	
	public static boolean DEFAULT_XCAP_ENABLED = false;
	public static String DEFAULT_XCAP_ROOT = "http://doubango.org:8080/services";
	public static String DEFAULT_XUI = "sip:alice@open-ims.test";
	
	public static boolean DEFAULT_RCS_PRESENCE = true;
	public static boolean DEFAULT_RCS_RLS = true;
	public static boolean DEFAULT_RCS_PARTIAL_PUB = false;
	public static String DEFAULT_RCS_FREE_TEXT = "Hello world";
	public static String DEFAULT_RCS_AVATAR_PATH = "";
	
	public static enum CONFIGURATION_ENTRY {
		/* === IDENTITY === */
		DISPLAY_NAME, IMPI, IMPU, PASSWORD,

		/* === GENERAL === */

		/* === LTE === */

		/* === NETWORK === */
		EARLY_IMS, IP_VERSION, PCSCF_DISCOVERY, PCSCF_HOST, PCSCF_PORT, REALM, SIGCOMP, TRANSPORT,

		/* === QOS/QOE === */
		SIP_CALLS_TIMEOUT, SIP_SESSIONS_TIMEOUT,

		/* === RCS === */
		PRESENCE, RLS, PARTIAL_PUB, FREE_TEXT, AVATAR_PATH,

		/* === SECURITY === */

		/* === SESSIONS === */

		/* === MMTEL === */

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
