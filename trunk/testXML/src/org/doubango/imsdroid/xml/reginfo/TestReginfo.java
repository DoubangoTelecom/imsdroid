package org.doubango.imsdroid.xml.reginfo;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class TestReginfo {
	
	private static final String TAG = TestReginfo.class.getCanonicalName();
	
	private static final String TEST1 =  "<?xml version=\"1.0\"?>"+
			   "<reginfo xmlns=\"urn:ietf:params:xml:ns:reginfo\" version=\"1\" state=\"partial\">"+
	   "<registration aor=\"sip:joe@example.com\" id=\"a7\" state=\"active\">"+
	     "<contact id=\"76\" state=\"active\" event=\"registered\" duration-registered=\"0\">"+
	        "<uri>sip:joe@pc34.example.com</uri>"+
	     "</contact>"+
	   "</registration>"+
	 "</reginfo>";
	
	private static final String TEST2 =  "<?xml version=\"1.0\"?>"+
		       "<reginfo xmlns=\"urn:ietf:params:xml:ns:reginfo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"0\" state=\"full\">"+
	         "<registration aor=\"sip:user@example.com\" id=\"as9\" state=\"active\">"+
	           "<contact id=\"76\" state=\"active\" event=\"registered\" duration-registered=\"7322\" q=\"0.8\">"+
	                    "<uri>sip:user@pc887.example.com</uri>"+
	           "</contact>"+
	           "<contact id=\"77\" state=\"terminated\" event=\"expired\" duration-registered=\"3600\" q=\"0.5\">"+
	                     "<uri>sip:user@university.edu</uri>"+
	           "</contact>"+
	         "</registration>"+
	       "</reginfo>";
	
	public static boolean run(){
		
		final Serializer serializer = new Persister();
		boolean success = true;
		
		Log.d(TestReginfo.TAG, "running test1...");
		try {
			@SuppressWarnings("unused")
			Reginfo reginfo = serializer.read(Reginfo.class, TestReginfo.TEST1);
			Log.d(TestReginfo.TAG, "NNNN-test1: success");
		} catch (Exception e) {
			Log.e(TestReginfo.TAG, "YYYY-test1: failed");
			e.printStackTrace();
			success = false;
		}
		
		
		Log.d(TestReginfo.TAG, "running test2...");
		try {
			@SuppressWarnings("unused")
			Reginfo reginfo = serializer.read(Reginfo.class, TestReginfo.TEST2);
			Log.d(TestReginfo.TAG, "NNNN-test1: success");
		} catch (Exception e) {
			Log.e(TestReginfo.TAG, "YYYY-test1: failed");
			e.printStackTrace();
			success = false;
		}
		
		return success;
	}

}
