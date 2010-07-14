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
package org.doubango.imsdroid.Sevices.Impl;

import ietf.params.xml.ns.xcap_caps.XcapCaps;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Services.IXcapService;
import org.doubango.tinyWRAP.XcapCallback;
import org.doubango.tinyWRAP.XcapEvent;
import org.doubango.tinyWRAP.XcapMessage;
import org.doubango.tinyWRAP.XcapSelector;
import org.doubango.tinyWRAP.XcapStack;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class XcapService  extends Service implements IXcapService{

	private final static String TAG = XcapService.class.getCanonicalName();
	
	private final IConfigurationService configurationService;
	private final INetworkService networkService;
	
	private final MyXcapCallback callback;
	
	private MyXcapStack stack;
	private XcapSelector selector;
	private boolean prepared;
	
	public XcapService(){
		
		this.callback = new MyXcapCallback();
		
		this.configurationService = ServiceManager.getConfigurationService();
		this.networkService = ServiceManager.getNetworkService();
	}
	
	public boolean start() {
		return true;
	}

	public boolean stop() {
		return true;
	}
	
	public boolean prepare(){
		if(this.prepared){
			Log.d(XcapService.TAG, "Already prepared");
			return true;
		}
				
		String impu = this.configurationService.getString(
				CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.IMPU,
				Configuration.DEFAULT_IMPU);		
		String xcap_ui = this.configurationService.getString(
				CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.USERNAME,
				impu);
		String xcap_root = this.configurationService.getString(
				CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.XCAP_ROOT,
				Configuration.DEFAULT_XCAP_ROOT);
		String xcap_password = this.configurationService.getString(
				CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.PASSWORD,
				Configuration.DEFAULT_XCAP_ROOT);
		String localIP = this.networkService.getLocalIP(false);
		if(localIP == null){
			localIP = "10.0.2.15";
		}
		
		if(this.stack == null){
			this.stack = new MyXcapStack(this.callback, xcap_ui, xcap_password, xcap_root);
			this.selector = new XcapSelector(this.stack);
		}
		
		this.stack.setLocalIP(localIP);
		if((this.prepared = this.stack.start())){
			this.stack.addHeader("Connection", "Keep-Alive");
			this.stack.addHeader("User-Agent", "XDM-client/OMA1.1");
			this.stack.addHeader("X-3GPP-Intended-Identity", xcap_ui);
		}
		else{
			Log.e(XcapService.TAG, "Failed to start the stack");
		}
		
		return prepared;
	}
	
	public boolean unPrepare(){
		if(!this.prepared){
			Log.d(XcapService.TAG, "Not prepared");
			return true;
		}
		
		if(this.stack.stop()){
			this.prepared = false;
		}
		
		return !this.prepared;
	}
	
	public boolean isPrepared(){
		return this.prepared;
	}
	
	
	public void downloadContacts(){
		if(!this.prepared){
			Log.e(XcapService.TAG, "Not prepared");
			return;
		}
		
		this.selector.reset();
		this.selector.setAUID("xcap-caps");
		this.stack.getDocument(this.selector.getString());
	}
	
	
	class MyXcapCallback extends XcapCallback
	{
		MyXcapCallback(){
			super();
		}

		@Override
		public int onEvent(XcapEvent e) {
			final XcapMessage message = e.getXcapMessage();
			final String content_type;
			final byte[] content;

			if (message == null || (content_type = message.getXcapHeaderValue("Content-Type")) == null) {
				Log.e(XcapService.TAG, "Invalid Xcap message");
				return -1;
			}		

			content = message.getXcapContent();
			
			Log.i(XcapService.TAG, String.format("code=%d and Phrase=%s",
					message.getCode(), message.getPhrase()));
			
			if(content != null){
				//if(StringUtils.equals(ContentType.XCAP_CAPS, content_type, true)){
					final Serializer serializer = new Persister();
					final XcapCaps xcap_caps;
					try {
						xcap_caps = serializer.read(XcapCaps.class, new String(content));
						for(String auid : xcap_caps.getAuids().getAuid()){
							Log.i(XcapService.TAG, String.format("auid=%s", auid));
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				//}
			}
			
			return 0;
		}
	}
	
	class MyXcapStack extends XcapStack
	{
		MyXcapStack(MyXcapCallback callback, String xui, String password, String xcap_root)
		{
			super(callback, xui, password, xcap_root);
		}
	}
}
