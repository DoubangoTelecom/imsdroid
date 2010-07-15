package oma.xml.xdm.xcap_directory;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class TestXcapDirectory {

		private static final String TAG = TestXcapDirectory.class.getCanonicalName();

		private static final String TEST1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
			"<xcap-directory xmlns=\"urn:oma:xml:xdm:xcap-directory\" >"+
			"<folder auid=\"resource-lists\">"+
			"<entry uri=\"http://xcap.example.com/resource-lists/users/sip:joebloggs@example.com/index\" etag=\"pqr999\" last-modified=\"1279068803188\" size=\"2738\"/>"+
			"</folder>"+
			"<folder auid=\"groups\">"+
			"<entry "+
			"uri=\"http://xcap.example.com/org.openmobilealliance.groups/users/sip:joebloggs@example.com/skiing\" etag=\"abc123\"/>"+
			"<entry "+
			"uri=\"http://xcap.example.com/org.openmobilealliance.groups/users/sip:joebloggs@example.com/shopping\" etag=\"def456\"/>"+
			"</folder>"+
			"</xcap-directory>";;

		public static boolean run() {

			final Serializer serializer = new Persister();
			boolean success = true;

			Log.d(TestXcapDirectory.TAG, "running test1...");
			try {
				@SuppressWarnings("unused")
				XcapDirectory xcap_dir = serializer.read(XcapDirectory.class, TestXcapDirectory.TEST1);
				Log.d(TestXcapDirectory.TAG, "NNNN-test1: success");
			} catch (Exception e) {
				Log.e(TestXcapDirectory.TAG, "YYYY-test1: failed");
				e.printStackTrace();
				success = false;
			}

			return success;
		}
	}