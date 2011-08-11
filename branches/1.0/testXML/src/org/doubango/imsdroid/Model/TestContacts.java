package org.doubango.imsdroid.Model;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class TestContacts {

	private static final String TAG = TestContacts.class.getCanonicalName();
	
	private static final String TEST1 =  "<?xml version=\"1.0\"?>" +
	"<contact uri=\"sip:alice@open-ims.test\">" +
	"<group>g1</group>" +
	"<freeText>Hello world!</freeText>" +
	"</contact>";
	
	private static final String TEST2 =  "<?xml version=\"1.0\"?>" +
	"<group name=\"g1\" displayName=\"g1_displayname\">" +
		"<contact uri=\"sip:alice@open-ims.test\">" +
		"<group>g1</group>" +
		"<freeText>Hello from Paris</freeText>" +
		"</contact>" +
		"<contact uri=\"bob@open-ims.test\">" +
		"<group>g1</group>" +
		"<freeText>Hello from Madrid</freeText>" +
		"</contact>" +
	"</group>";

	public static boolean run() {

		final Serializer serializer = new Persister();
		boolean success = true;

		Log.d(TestContacts.TAG, "running test1...");
		try {
			@SuppressWarnings("unused")
			Contact contact = serializer.read(Contact.class, TestContacts.TEST1);
			Log.d(TestContacts.TAG, "NNNN-test1: success");
		} catch (Exception e) {
			Log.e(TestContacts.TAG, "YYYY-test1: failed");
			e.printStackTrace();
			success = false;
		}
		
		Log.d(TestContacts.TAG, "running test2...");
		try {
			@SuppressWarnings("unused")
			Group group = serializer.read(Group.class, TestContacts.TEST2);
			Log.d(TestContacts.TAG, "NNNN-test2: success");
		} catch (Exception e) {
			Log.e(TestContacts.TAG, "YYYY-test2: failed");
			e.printStackTrace();
			success = false;
		}
		
		return success;
	}
	
}
