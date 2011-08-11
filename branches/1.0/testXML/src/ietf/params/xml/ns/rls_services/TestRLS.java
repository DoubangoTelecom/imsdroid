package ietf.params.xml.ns.rls_services;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class TestRLS {
	private static final String TAG = TestRLS.class.getCanonicalName();

	private static final String TEST1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
		"<rls-services xmlns=\"urn:ietf:params:xml:ns:rls-services\">"+
		"<service uri=\"sip:1234578901@gsma.org;pres-list=rcs\">"+
		"<resource-list>http://xcap1.gsma.com/services/resource-lists/users/sip:1234578901@gsma.org/index/~~/resource-lists/list%5B@name=%22rcs%22%5D</resource-list>"+
		"<packages>"+
		"<package>presence</package>"+
		"</packages>"+
		"</service>"+
		"<service uri=\"sip:000000@gsma.org;pres-list=rcs\">"+
		"<resource-list>http://xcap2.gsma.com/services/resource-lists/users/sip:1234578901@gsma.org/index/~~/resource-lists/list%5B@name=%22rcs%22%5D</resource-list>"+
		"<packages>"+
		"<package>mypackage</package>"+
		"</packages>"+
		"</service>"+
		"</rls-services>";

	public static boolean run() {

		final Serializer serializer = new Persister();
		boolean success = true;

		Log.d(TestRLS.TAG, "running test1...");
		try {
			@SuppressWarnings("unused")
			RlsServices rls_services = serializer.read(RlsServices.class, TestRLS.TEST1);
			Log.d(TestRLS.TAG, "NNNN-test1: success");
		} catch (Exception e) {
			Log.e(TestRLS.TAG, "YYYY-test1: failed");
			e.printStackTrace();
			success = false;
		}

		return success;
	}
}
