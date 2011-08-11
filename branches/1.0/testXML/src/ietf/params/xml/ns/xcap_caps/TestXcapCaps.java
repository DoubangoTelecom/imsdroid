package ietf.params.xml.ns.xcap_caps;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class TestXcapCaps {
	private static final String TAG = TestXcapCaps.class.getCanonicalName();

	private static final String TEST1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xcap-caps xmlns=\"urn:ietf:params:xml:ns:xcap-caps\""
			+ "		xmlns:xsi=\"http//www.w3.org/2001/XMLSchema-instance\""
			+ "		xsi:schemaLocation=\"urn:ietf:params:xml:ns:xcap-caps xcap-caps.xsd \">"
			+ "	<auids>"
			+ "		<auid>xcap-caps</auid>"
			+ "		<auid>resource-lists</auid>"
			+ "		<auid>rls-services</auid>"
			+ "		<auid>org.openmobilealliance.xcap-directory</auid>"
			+ "  </auids>"
			+ "<extensions>"
			+ "	  <!-- No extensions defined -->"
			+ " </extensions>"
			+ "<namespaces>"
			+ "	  <namespace>urn:ietf:params:xml:ns:xcap-caps</namespace>"
			+ "	  <namespace>urn:ietf:params:xml:ns:xcap-error</namespace>"
			+ "	  <namespace>urn:ietf:params:xml:ns:resource-lists</namespace>"
			+ "	  <namespace>urn:ietf:params:xml:ns:rls-services</namespace>"
			+ "	  <namespace>urn:oma:params:ns:resource-list:oma-uriusage</namespace>"
			+ " </namespaces>" 
			+ "</xcap-caps>";

	public static boolean run() {

		final Serializer serializer = new Persister();
		boolean success = true;

		Log.d(TestXcapCaps.TAG, "running test1...");
		try {
			@SuppressWarnings("unused")
			XcapCaps xcap_caps = serializer.read(XcapCaps.class, TestXcapCaps.TEST1);
			Log.d(TestXcapCaps.TAG, "NNNN-test1: success");
		} catch (Exception e) {
			Log.e(TestXcapCaps.TAG, "YYYY-test1: failed");
			e.printStackTrace();
			success = false;
		}

		return success;
	}
}
