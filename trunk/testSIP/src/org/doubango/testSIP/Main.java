package org.doubango.testSIP;

import org.doubango.tinyWRAP.RegistrationEvent;
import org.doubango.tinyWRAP.RegistrationSession;
import org.doubango.tinyWRAP.SipCallback;
import org.doubango.tinyWRAP.SipDebugCallback;
import org.doubango.tinyWRAP.SipStack;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity {
    
	private Button button1;
	

	private static String REALM = "ericsson.com";
	private static String IMPI = "alice@ericsson.com";
	private static String IMPU = "sip:alice@ericsson.com";
	private static String PROXY_CSCF_IP = "192.168.0.13";
	private static int PROXY_CSCF_PORT = 5081;
	
    private static RegistrationSession regSession;
    private static MySipCallback sipCallback;
    private static MySipDebugCallback sipDebugCallback;
    private static SipStack sipStack;
	
    public Main(){
    	sipCallback = new MySipCallback();
    	sipDebugCallback = new MySipDebugCallback();
    	sipStack = new SipStack(sipCallback, REALM, IMPI, IMPU);
    	regSession = new RegistrationSession(sipStack);
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /* Set debug callback */
        //this.sipStack.setDebugCallback(this.sipDebugCallback);
        
        /* Set Proxy-CSCF parameters */
        sipStack.addHeader("Privacy", "INVITE, ACK, CANCEL, BYE, MESSAGE, OPTIONS, NOTIFY, PRACK, UPDATE, REFER");
        sipStack.addHeader("P-Access-Network-Info", "ADSL;utran-cell-id-3gpp=00000000");
        sipStack.addHeader("User-Agent", "IM-client/OMA1.0 doubango/v1.0.0");            
        
        @SuppressWarnings("unused")
		boolean success = sipStack.setProxyCSCF(PROXY_CSCF_IP, PROXY_CSCF_PORT, "udp", "ipv4");
        
        /* Set local IP to bind to (Mandatory when running on the emulator) */
        String localIP = getLocalIPv4();
        success = sipStack.setLocalIP(localIP == null ? "10.0.2.15" : localIP);
        //success = sipStack.setLocalPort(60089);
        //this.sipStack.setDebugCallback(this.sipDebugCallback);
        /* Start the stack */
        success = sipStack.start();
        
        
        this.button1 = (Button)this.findViewById(R.id.Button01);
        
        this.button1.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				 /* Send REGISTER */
				regSession.addCaps("+g.oma.sip-im");
				regSession.addCaps("+g.3gpp.smsip");
				regSession.addCaps("language", "\"en,fr\"");
				regSession.setExpires(30);
		        if((regSession.Register())){
		        	Log.i(Main.class.getCanonicalName(),"REGISTER sent");
		        }
		        else{
		        	Log.e(Main.class.getCanonicalName(),"Failed to send REGISTER");
		        }
			}

        });
    }
    
    private String getLocalIPv4(){
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress(); /*what about IPv6?*/
		
		if(ipAddress != 0){
			return String.format("%d.%d.%d.%d",
					(ipAddress>>0)&0xFF,
					(ipAddress>>8)&0xFF,
					(ipAddress>>16)&0xFF,
					(ipAddress>>24)&0xFF
					);
		}
		else{
			return null;
		}
	}
    

	private class MySipCallback extends SipCallback
    {
      public MySipCallback(){
        super();
      }

      public int OnRegistrationChanged(RegistrationEvent e)
      {
    	  short code = e.getCode();
          //tsip_event_type_t type = e.getType();

          //RegistrationSession session = (RegistrationSession) e.getSession();
          
          System.out.println("Code=" + code);
          System.out.println("Phrase=" + e.getPhrase());
          
          return 0;
      }
    }
    
    private class MySipDebugCallback extends SipDebugCallback
    {
    	 public int OnDebugInfo(String message) {
    		Log.i(Main.class.getCanonicalName(), message);
		    return 0;
		  }

		  public int OnDebugWarn(String message) {
			 Log.w(Main.class.getCanonicalName(), message);
			 return 0;
		  }

		  public int OnDebugError(String message) {
			 Log.e(Main.class.getCanonicalName(), message);
			 return 0;
		  }

		  public int OnDebugFatal(String message) {
			Log.e(Main.class.getCanonicalName(), message);
		    return 0;
		  }
    }

    
    static {
        try {
        	// Do not use "System.loadLibrary" which will always load libraries from "/system/lib"
            //System.loadLibrary("tinyWRAP");
            System.load(String.format("/data/data/%s/lib/tinyWRAP", Main.class.getPackage().getName()));
            
        } catch (UnsatisfiedLinkError e) {
          System.err.println("Native code library failed to load.\n" + e);
          System.exit(1);
        }
        catch(Exception e){
        	e.printStackTrace();
        }
      }
}

