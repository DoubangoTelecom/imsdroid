package ietf.params.xml.ns.resource_lists;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;


public class TestResourceLists {
	private static final String TAG = TestResourceLists.class.getCanonicalName();
	
	private static final String TEST1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\" xmlns:xd=\"urn:oma:xml:xdm:xcap-directory\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance3\">"+
		"<!-- The list oma_buddylist contains references to any individual list used according to OMA IG for presence subscriptions. -->"+
		"<list name=\"oma_buddylist\">"+
		"<external anchor=\"http://xcap.example.org/resource-lists/users/sip:RCSUser@example.org/index/~~/resource-lists/list%5B@name=%22rcs%22%5D\"/>"+
		"</list>"+
		"<!-- The list oma_grantedcontacts contains the list of all granted contacts -->"+
		"<list name=\"oma_grantedcontacts\">"+
		"<external anchor=\"http://xcap.example.org/resource-lists/users/sip:RCSUser@example.org/index/~~/resource-lists/list%5B@name=%22rcs%22%5D\"/>"+
		"</list>"+
		"<!-- The list oma_blockedcontacts contains the list of all blocked contacts. -->"+
		"<list name=\"oma_blockedcontacts\">"+
		"<external anchor=\"http://xcap.example.org/resource-lists/users/sip:RCSUser@example.org/index/~~/resource-lists/list%5B@name=%22rcs_blockedcontacts%22%5D\"/>"+
		"<external anchor=\"http://xcap.example.org/resource-lists/users/sip:RCSUser@example.org/index/~~/resource-lists/list%5B@name=%22rcs_revokedcontacts%22%5D\"/>"+
		"</list>"+
		"<!-- The list of buddies the owner wants be able to get presence information for -->"+
		"<!-- The RCS presentity is always part of this list, refer to e 11.4 -->"+
		"<list name=\"rcs\">"+
		"<display-name>My presence buddies</display-name>"+
		"<!-- The URI below is just an example of the own's user Id -->"+
		"<entry uri=\"tel:+1234578901\" />"+
		"</list>"+
		"<!-- The list of blocked contacts -->"+
		"<list name=\"rcs_blockedcontacts\">"+
		"<display-name>My blocked contacts</display-name>"+
		"</list>"+
		"<!-- The list of revoked contacts -->"+
		"<list name=\"rcs_revokedcontacts\">"+
		"<display-name>My revoked contacts</display-name>"+
		"<entry uri=\"tel:+123457\" xd:last-modified=\"2008-12-24T14:32:14Z\">"+
		"<display-name>123457's display name</display-name>"+
		"</entry>"+
		"<entry uri=\"tel:+123456\" xd:last-modified=\"2008-12-24T14:32:13Z\"/>"+
		"<entry uri=\"tel:+123458\" xd:last-modified=\"2008-12-24T14:32:13Z\"/>"+
		"<entry uri=\"tel:+123458\" test=\"3\" xd:last-modified=\"2008-12-24T14:32:13Z\"/>"+
		"<entry2 uri=\"tel:+123458\" xd:last-modified=\"2008-12-24T14:32:13Z\"/>"+
		"</list>"+
		"</resource-lists>";
	
public static boolean run(){
		
		final Serializer serializer = new Persister();
		boolean success = true;
		
		Log.d(TestResourceLists.TAG, "running test1...");
		try {
			@SuppressWarnings("unused")
			ResourceLists resourceLists = serializer.read(ResourceLists.class, TestResourceLists.TEST1);
			Log.d(TestResourceLists.TAG, "NNNN-test1: success");
		} catch (Exception e) {
			Log.e(TestResourceLists.TAG, "YYYY-test1: failed");
			e.printStackTrace();
			success = false;
		}
		
		return success;
	}
}
