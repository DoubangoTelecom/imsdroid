package ietf.params.xml.ns.pidf;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class TestPidf {
	private static final String TAG = TestPidf.class.getCanonicalName();

	private static final String TEST1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "+
						   "<presence xmlns=\"urn:ietf:params:xml:ns:pidf\" "+
						   "     xmlns:im=\"urn:ietf:params:xml:ns:pidf:im\" "+
						   "     xmlns:myex=\"http://id.example.com/presence/\" "+
						   "     entity=\"pres:someone@example.com\"> "+
						   "  <tuple id=\"bs35r9\"> "+
						   "    <status> "+
						   "      <basic>open</basic> "+
						   "      <im:im>busy</im:im> "+
						   "      <myex:location>home</myex:location> "+
						   "    </status> "+
						   "    <contact priority=\"0.8\">im:someone@mobilecarrier.net</contact> "+
						   "    <note xml:lang=\"en\">Don't Disturb Please!</note> "+
						   "     <note xml:lang=\"fr\">Ne derangez pas, s'il vous plait</note> "+
						   "    <timestamp>2001-10-27T16:49:29Z</timestamp> "+
						   "  </tuple> "+
						   "  <tuple id=\"eg92n8\"> "+
						   "     <status> "+
						   "      <basic>closed</basic> "+
						   "    </status> "+
						   "    <contact priority=\"1.0\">mailto:someone@example.com</contact> "+
						   "  </tuple> "+
						   "  <note>I'll be in Tokyo next week</note> "+
						   "</presence>";
	
	
	private static final String TEST2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
			"<presence xmlns=\"urn:ietf:params:xml:ns:pidf\" xmlns:caps=\"urn:ietf:params:xml:ns:pidf:caps\" xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\" xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\" xmlns:op=\"urn:oma:xml:prs:pidf:oma-pres\" entity=\"sip:mamadou@micromethod.com\"><pdm:person id=\"FPNZFGON\"><op:overriding-willingness><op:basic>open</op:basic></op:overriding-willingness><rpid:activities><rpid:onthephone /></rpid:activities><pdm:note>Hello world</pdm:note><pdm:timestamp>2010-07-16T01:08:55Z</pdm:timestamp></pdm:person><pdm:device id=\"d1983\"><status><basic>open</basic></status><caps:devcaps><caps:mobility><caps:supported><caps:mobile /></caps:supported></caps:mobility></caps:devcaps><op:network-availability><op:network id=\"IMS\"><op:active /></op:network></op:network-availability><pdm:timestamp>2010-07-16T01:08:55Z</pdm:timestamp></pdm:device><pdm:person id=\"FPNZFGON\"><op:overriding-willingness><op:basic>open</op:basic></op:overriding-willingness><rpid:activities><rpid:busy /></rpid:activities><pdm:note>Hello world</pdm:note><pdm:timestamp>2010-07-16T01:08:47Z</pdm:timestamp></pdm:person></presence>";


	public static boolean run() {

		final Serializer serializer = new Persister();
		boolean success = true;

//		Log.d(TestPidf.TAG, "running test1...");
//		try {
//			@SuppressWarnings("unused")
//			Presence presence = serializer.read(Presence.class, TestPidf.TEST1);
//			Log.d(TestPidf.TAG, "NNNN-test1: success");
//		} catch (Exception e) {
//			Log.e(TestPidf.TAG, "YYYY-test1: failed");
//			e.printStackTrace();
//			success = false;
//		}
		
		Log.d(TestPidf.TAG, "running test2...");
		try {
			@SuppressWarnings("unused")
			Presence presence = serializer.read(Presence.class, TestPidf.TEST2);
			Log.d(TestPidf.TAG, "NNNN-test2: success");
		} catch (Exception e) {
			Log.e(TestPidf.TAG, "YYYY-test2: failed");
			e.printStackTrace();
			success = false;
		}

		return success;
	}
}
