package org.doubango.imsdroid.xcap;

public class Xcap {
	/*== xcap-caps ==*/
	public static final String XCAP_AUID_IETF_XCAP_CAPS_ID = "xcap-caps";
	public static final String XCAP_AUID_IETF_XCAP_CAPS_MIME_TYPE = "application/xcap-caps+xml";
	public static final String XCAP_AUID_IETF_XCAP_CAPS_NS = "urn:ietf:params:xml:ns:xcap-caps";
	public static final String XCAP_AUID_IETF_XCAP_CAPS_DOC = "index";
	
	/*== resource-lists ==*/
	public static final String XCAP_AUID_IETF_RESOURCE_LISTS_ID = "resource-lists";
	public static final String XCAP_AUID_IETF_RESOURCE_LISTS_MIME_TYPE = "application/resource-lists+xml";
	public static final String XCAP_AUID_IETF_RESOURCE_LISTS_NS = "urn:ietf:params:xml:ns:resource-lists";
	public static final String XCAP_AUID_IETF_RESOURCE_LISTS_DOC = "index";
	
	/*== rls-services ==*/
	public static final String XCAP_AUID_IETF_RLS_SERVICES_ID = "rls-services";
	public static final String XCAP_AUID_IETF_RLS_SERVICES_MIME_TYPE = "application/rls-services+xml";
	public static final String XCAP_AUID_IETF_RLS_SERVICES_NS = "urn:ietf:params:xml:ns:resource-lists";
	public static final String XCAP_AUID_IETF_RLS_SERVICES_DOC = "index";
	
	/*== pres-rules ==*/
	public static final String XCAP_AUID_IETF_PRES_RULES_ID = "pres-rules";
	public static final String XCAP_AUID_IETF_PRES_RULES_MIME_TYPE = "application/auth-policy+xml";
	public static final String XCAP_AUID_IETF_PRES_RULES_NS = "urn:ietf:params:xml:ns:pres-rules";
	public static final String XCAP_AUID_IETF_PRES_RULES_DOC = "index";
	
	/*== org.openmobilealliance.pres-rules ==*/
	public static final String XCAP_AUID_OMA_PRES_RULES_ID = "org.openmobilealliance.pres-rules";
	public static final String XCAP_AUID_OMA_PRES_RULES_MIME_TYPE = "application/auth-policy+xml";
	public static final String XCAP_AUID_OMA_PRES_RULES_NS = "urn:ietf:params:xml:ns:common-policy";
	public static final String XCAP_AUID_OMA_PRES_RULES_DOC = "pres-rules";
	
	/*== directory ==*/
	public static final String XCAP_AUID_IETF_DIRECTORY_ID = "directory";
	public static final String XCAP_AUID_IETF_DIRECTORY_MIME_TYPE = "application/directory+xml";
	public static final String XCAP_AUID_IETF_DIRECTORY_NS = "urn:ietf:params:xml:ns:xcap-directory";
	public static final String XCAP_AUID_IETF_DIRECTORY_DOC = "directory.xml";
	
	/*== org.openmobilealliance.xcap-directory ==*/
	public static final String XCAP_AUID_OMA_DIRECTORY_ID = "org.openmobilealliance.xcap-directory";
	public static final String XCAP_AUID_OMA_DIRECTORY_MIME_TYPE = "application/vnd.oma.xcap-directory+xml";
	public static final String XCAP_AUID_OMA_DIRECTORY_NS = "urn:oma:xml:xdm:xcap-directory";
	public static final String XCAP_AUID_OMA_DIRECTORY_DOC = "directory.xml";
	
	/*== org.openmobilealliance.pres-content ==*/
	public static final String XCAP_AUID_OMA_PRES_CONTENT_ID = "org.openmobilealliance.pres-content";
	public static final String XCAP_AUID_OMA_PRES_CONTENT_MIME_TYPE = "application/vnd.oma.pres-content+xml";
	public static final String XCAP_AUID_OMA_PRES_CONTENT_NS = "urn:oma:xml:prs:pres-content";
	public static final String XCAP_AUID_OMA_PRES_CONTENT_DOC = "oma_status-icon/rcs_status_icon";
	
	/*== org.openmobilealliance.conv-history ==*/
	public static final String XCAP_AUID_OMA_CONV_HISTORY_ID = "org.openmobilealliance.conv-history";
	public static final String XCAP_AUID_OMA_CONV_HISTORY_MIME_TYPE = "application/vnd.oma.im.history-list+xml";
	public static final String XCAP_AUID_OMA_CONV_HISTORY_NS = "urn:oma:xml:im:history-list";
	public static final String XCAP_AUID_OMA_CONV_HISTORY_DOC = "conv-history";
	
	/*== org.openmobilealliance.deferred-list ==*/
	public static final String XCAP_AUID_OMA_DEFERRED_LIST_ID = "org.openmobilealliance.deferred-list";
	public static final String XCAP_AUID_OMA_DEFERRED_LIST_MIME_TYPE = "application/vnd.oma.im.deferred-list+xml";
	public static final String XCAP_AUID_OMA_DEFERRED_LIST_NS = "urn:oma:xml:im:history-list";
	public static final String XCAP_AUID_OMA_DEFERRED_LIST_DOC = "deferred-list";
	
	/*== org.openmobilealliance.group-usage-list ==*/
	public static final String XCAP_AUID_OMA_SHARED_GROUPS_ID = "org.openmobilealliance.group-usage-list";
	public static final String XCAP_AUID_OMA_SHARED_GROUPS_MIME_TYPE = "application/vnd.oma.group-usage-list+xml";
	public static final String XCAP_AUID_OMA_SHARED_GROUPS_NS = "urn:ietf:params:xml:ns:resource-lists";
	public static final String XCAP_AUID_OMA_SHARED_GROUPS_DOC = "index";
	
	public enum State{
		GET_XCAP_CAPS,
		GET_OMA_DIRECTORY,
		GET_RESOURCE_LISTS,
		GET_RLS
	}
	
	public static boolean isSuccess(short code){
		return (code >199 && code<300);
	}
}
