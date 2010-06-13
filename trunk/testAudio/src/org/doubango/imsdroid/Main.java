/*
* Copyright (C) 2010 Mamadou Diop.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango.org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*
*/
package org.doubango.imsdroid;

import org.doubango.tinyWRAP.CallSession;
import org.doubango.tinyWRAP.DDebugCallback;
import org.doubango.tinyWRAP.RegistrationEvent;
import org.doubango.tinyWRAP.RegistrationSession;
import org.doubango.tinyWRAP.SipCallback;
import org.doubango.tinyWRAP.SipStack;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity {
    
	DDebugCallback debugCallBack;
	MySipCallback sipCallBack;
	SipStack sipStack;
	RegistrationSession regSession;
	CallSession callSession;
	
	AudioConsumer audioConsumer;
	AudioProducer audioProducer;
	
	Button btnCall;
	Button btnRegister;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        @SuppressWarnings("unused")
		boolean success;
       
        
        //audioConsumer = new AudioConsumer();
        //audioConsumer.prepare(20, 8000, AudioManager.STREAM_MUSIC);
        //audioConsumer.start();
        
        this.debugCallBack = new DDebugCallback();
        this.sipCallBack = new MySipCallback();
        this.sipStack = new SipStack(this.sipCallBack, "sip:ericsson.com", "mamadou", "sip:mamadou@ericsson.com");
        this.sipStack.setDebugCallback(this.debugCallBack);
        
        success = this.sipStack.isValid();
        success = this.sipStack.setProxyCSCF("192.168.0.13", 5081, "tcp", "ipv4");
        this.sipStack.setLocalIP(this.getLocalIP(false));
        
        success = this.sipStack.start();
        
        // Audio Consumer/Producer
        audioConsumer = new AudioConsumer();
        audioProducer = new AudioProducer();
              
        this.btnRegister = (Button)this.findViewById(R.id.ButtonRegister);
        this.btnRegister.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(regSession == null){
					regSession = new RegistrationSession(sipStack);
				}
			    regSession.setExpires(30);
			    regSession.Register();
			}
        });
        
        this.btnCall = (Button)this.findViewById(R.id.ButtonCall);
        this.btnCall.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(callSession == null){
					callSession = new CallSession(sipStack);
				}
				audioConsumer.setActive();
				audioProducer.setActive();
				callSession.Call("sip:bob@ericsson.com");
			}
        });
        
        
        
        //AudioConsumer consumer = new AudioConsumer();
        //consumer.prepare(20, 8000, AudioManager.STREAM_MUSIC); /* STREAM_VOICE_CALL */
        //consumer.start();
    }
    
    
    public String getLocalIP(boolean ipv6){
		
		if(ipv6){
			return null;
		}
		
		WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		
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
    	public MySipCallback()
    	{
    		super();
    	}
		@Override
		public int OnRegistrationEvent(RegistrationEvent e) {
			Log.d("Test Audio ()", String.format("OnRegistrationEvent (code=%d) and (phrase=%s)", e.getCode(), e.getPhrase()));
			
			return 0;
		}
    }
    
    static {
		try {
			//System.loadLibrary("tinyWRAP");
			System.load(String.format("/data/data/%s/lib/libtinyWRAP.so", Main.class
					.getPackage().getName()));

		} catch (UnsatisfiedLinkError e) {
			Log.e(Main.class.getCanonicalName(),
					"Native code library failed to load.\n" + e.getMessage());
		} catch (Exception e) {
			Log.e(Main.class.getCanonicalName(),
					"Native code library failed to load.\n" + e.getMessage());
		}
	}
}