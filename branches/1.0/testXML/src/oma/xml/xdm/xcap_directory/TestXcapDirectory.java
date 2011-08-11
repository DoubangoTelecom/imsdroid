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
			
			private static final String TEST2 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
					"<xcap-directory xmlns=\"urn:oma:xml:xdm:xcap-directory\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
				    "<folder auid=\"org.openmobilealliance.group-usage-list\"/>" +
				    "<folder auid=\"simservs.ngn.etsi.org\"/>" +
				    "<folder auid=\"resource-lists\">" +
				        "<entry uri=\"http://siptest.colibria.com:8080/services/resource-lists/users/sip%3Amercuro1%40colibria.com/properties-resource-list.xml\" etag=\"W/'1650-1270759572696'\" last-modified=\"2010-04-08T22:46:12.000+02:00\" size=\"1650\"/>" +
				        "<entry uri=\"http://siptest.colibria.com:8080/services/resource-lists/users/sip%3Amercuro1%40colibria.com/index\" etag=\"W/'2553-1270761710572'\" last-modified=\"2010-04-08T23:21:50.000+02:00\" size=\"2553\"/>" +
				    "</folder>" +
				    "<folder auid=\"org.openmobilealliance.user-profile\"/>" +
				    "<folder auid=\"pidf-manipulation\"/>" +
				    "<folder auid=\"pres-rules\"/>" +
				    "<folder auid=\"rls-services\">" +
				        "<entry uri=\"http://siptest.colibria.com:8080/services/rls-services/users/sip%3Amercuro1%40colibria.com/index\" etag=\"W/'1162-1268303218301'\" last-modified=\"2010-03-11T11:26:58.000+01:00\" size=\"1162\"/>" +
				    "</folder>" +
				    "<folder auid=\"org.openmobilealliance.pres-rules\">" +
				        "<entry uri=\"http://siptest.colibria.com:8080/services/org.openmobilealliance.pres-rules/users/sip%3Amercuro1%40colibria.com/pres-rules\" etag=\"W/'2710-1268303220286'\" last-modified=\"2010-03-11T11:27:00.000+01:00\" size=\"2710\"/>" +
				    "</folder>" +
				    "<folder auid=\"org.openmobilealliance.pres-content\">" +
				        "<entry uri=\"http://siptest.colibria.com:8080/services/org.openmobilealliance.pres-content/users/sip%3Amercuro1%40colibria.com/oma_status-icon/rcs_status_icon\" etag=\"W/'20503-1265194858818'\" last-modified=\"2010-02-03T12:00:58.000+01:00\" size=\"20503\"/>" +
				    "</folder>" +
				    "<folder auid=\"org.openmobilealliance.access-rules\"/>" +
				    "<folder auid=\"org.openmobilealliance.groups\"/>" +
				"</xcap-directory>";
			
	private static final String TEST3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xcap-directory xmlns=\"urn:oma:xml:xdm:xcap-directory\">"
			+ "<folder auid=\"resource-lists\">"
			+ "<entry etag=\"76695\" uri=\"http://10.173.159.53:7077/xcap-ap-service/resource-lists/users/test@cims.aupm.ete.ericsson.se/index\"/>"
			+ "</folder>"
			+ "<folder auid=\"org.openmobilealliance.pres-content\">"
			+ "<entry etag=\"666\" uri=\"http://10.173.159.53:7077/xcap-ap-service/org.openmobilealliance.pres-content/users/sip:test@test/oma_status-icon/preseImage\"/>"
			+ "</folder>"
			+ "<folder auid=\"rls-services\">"
			+ "<entry etag=\"33742\" uri=\"http://10.173.159.53:7077/xcap-ap-service/rls-services/users/sip:test@test/index\"/>"
			+ "</folder>"
			+ "<folder auid=\"org.openmobilealliance.pres-rules\">"
			+ "<entry etag=\"94255\" uri=\"http://10.173.159.53:7077/xcap-ap-service/org.openmobilealliance.pres-rules/users/test@test/pres-rules\"/>"
			+ "</folder>"
			+ "<folder auid=\"simservs.ngn.etsi.org\">"
			+ "<error-code>404 Not Found</error-code>"
			+ "</folder>"
			+ "</xcap-directory>";

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
			
			Log.d(TestXcapDirectory.TAG, "running test2...");
			try {
				@SuppressWarnings("unused")
				XcapDirectory xcap_dir = serializer.read(XcapDirectory.class, TestXcapDirectory.TEST2);
				Log.d(TestXcapDirectory.TAG, "NNNN-test2: success");
			} catch (Exception e) {
				Log.e(TestXcapDirectory.TAG, "YYYY-test2: failed");
				e.printStackTrace();
				success = false;
			}
			
			Log.d(TestXcapDirectory.TAG, "running test3...");
			try {
				@SuppressWarnings("unused")
				XcapDirectory xcap_dir = serializer.read(XcapDirectory.class, TestXcapDirectory.TEST3);
				for(XcapDirectory.Folder folder : xcap_dir.folder){
					Log.d(TestXcapDirectory.TAG, "auid=" +folder.auid);
				}
				Log.d(TestXcapDirectory.TAG, "NNNN-test3: success");
			} catch (Exception e) {
				Log.e(TestXcapDirectory.TAG, "YYYY-test3: failed");
				e.printStackTrace();
				success = false;
			}

			return success;
		}
	}