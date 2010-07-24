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
package org.doubango.imsdroid.Services.Impl;

import ietf.params.xml.ns.resource_lists.EntryType;
import ietf.params.xml.ns.resource_lists.ListType;
import ietf.params.xml.ns.resource_lists.ResourceLists;
import ietf.params.xml.ns.rls_services.RlsServices;
import ietf.params.xml.ns.rls_services.ServiceType;
import ietf.params.xml.ns.xcap_caps.XcapCaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import oma.xml.xdm.xcap_directory.XcapDirectory;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Group;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Services.IXcapService;
import org.doubango.imsdroid.events.EventHandler;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.IXcapEventHandler;
import org.doubango.imsdroid.events.RegistrationEventArgs;
import org.doubango.imsdroid.events.XcapEventArgs;
import org.doubango.imsdroid.events.XcapEventTypes;
import org.doubango.imsdroid.utils.StringUtils;
import org.doubango.imsdroid.xcap.Xcap;
import org.doubango.tinyWRAP.XcapCallback;
import org.doubango.tinyWRAP.XcapEvent;
import org.doubango.tinyWRAP.XcapMessage;
import org.doubango.tinyWRAP.XcapSelector;
import org.doubango.tinyWRAP.XcapStack;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class XcapService  extends Service implements IXcapService, IRegistrationEventHandler{

	private final static String TAG = XcapService.class.getCanonicalName();
	
	private final IConfigurationService configurationService;
	private final INetworkService networkService;
	
	// Event Handlers
	private final CopyOnWriteArrayList<IXcapEventHandler> xcapEventHandlers;
	
	private final MyXcapCallback callback;
	private final HashMap<String, String> documentsUris;
	private final ArrayList<Group> groups;
	
	private MyXcapStack stack;
	private XcapSelector selector;
	private boolean prepared;
	
	private Xcap.State currentSate;
	private boolean enabled;
	
	private boolean haveOMADirectory;
	private boolean haveResourceLists;
	private boolean haveRLS;
	private boolean havePresRules;
	private boolean haveOMAPresenceContent;
	
	
	public XcapService(){
		
		this.callback = new MyXcapCallback();
		this.documentsUris = new HashMap<String, String>();
		this.groups = new ArrayList<Group>();
		
		this.currentSate = Xcap.State.GET_XCAP_CAPS;
		
		this.xcapEventHandlers = new CopyOnWriteArrayList<IXcapEventHandler>();
		
		this.configurationService = ServiceManager.getConfigurationService();
		this.networkService = ServiceManager.getNetworkService();
	}
	
	public boolean start() {
		// add sip event handlers
		ServiceManager.getSipService().addRegistrationEventHandler(this);
		return true;
	}

	public boolean stop() {
		// remove sip event handlers
		ServiceManager.getSipService().removeRegistrationEventHandler(this);
		return true;
	}
	
	public List<Group> getGroups(){
		return this.groups;
	}
	
	/* ===================== Sip Events ========================*/
	public boolean onRegistrationEvent(Object sender, RegistrationEventArgs e) {
		switch(e.getType()){
			case REGISTRATION_OK:
				this.enabled = ServiceManager.getConfigurationService().getBoolean(
						CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.ENABLED,
						Configuration.DEFAULT_XCAP_ENABLED);
				
				if(this.enabled && this.prepare()){
					this.downloadDocuments();
				}
				break;
			case UNREGISTRATION_OK:
				if(this.enabled){
					this.unPrepare();
				}
				break;
			default:
				break;
		}
		return true;
	}
	
	/* ===================== Add/Remove handlers ======================== */
	public boolean addXcapEventHandler(IXcapEventHandler handler) {
		return EventHandler.addEventHandler(this.xcapEventHandlers, handler);
	}

	public boolean removeXcapEventHandler(IXcapEventHandler handler) {
		return EventHandler.removeEventHandler(this.xcapEventHandlers, handler);
	}
	
	/* ===================== Dispatch events ======================== */
	private synchronized void onXcapEvent(final XcapEventArgs eargs) {
		for(IXcapEventHandler handler : this.xcapEventHandlers){
			if (!handler.onXcapEvent(this, eargs)) {
				Log.w(handler.getClass().getName(), "onXcapEvent failed");
			}
		}
	}
	
	private boolean prepare(){
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
		else{
			this.stack.setCredentials(xcap_ui, xcap_password);
			this.stack.setXcapRoot(xcap_root);
		}
		
		this.stack.setLocalIP(localIP);
		if((this.prepared = this.stack.start())){
			this.stack.addHeader("Connection", "Keep-Alive");
			this.stack.addHeader("User-Agent", "XDM-client/OMA1.1");
			this.stack.addHeader("X-3GPP-Intended-Identity", xcap_ui);
		}
		else{
			Log.e(XcapService.TAG, "Failed to start the XCAP stack");
		}
		
		return prepared;
	}
	
	private boolean unPrepare(){
		if(!this.prepared){
			Log.d(XcapService.TAG, "Not prepared");
			return true;
		}
		
		this.documentsUris.clear();
		
		if(this.stack.stop()){
			this.prepared = false;
		}
		else{
			Log.e(XcapService.TAG, "Failed to stop the XCAP stack");
		}
		
		return !this.prepared;
	}
	
	private void downloadDocuments(){
		if(!this.prepared){
			Log.e(XcapService.TAG, "Not prepared");
			return;
		}
		
		this.currentSate = Xcap.State.GET_XCAP_CAPS;
		
		synchronized(this.selector){
			this.selector.reset();
			this.selector.setAUID(Xcap.XCAP_AUID_IETF_XCAP_CAPS_ID);
			this.stack.getDocument(this.selector.getString());
		}
	}
	
	private void downloadDocument(String auid){
		String documentUrl;
		
		Log.d(XcapService.TAG, "Downloading '"+ auid +"' document...");
		
		if((documentUrl = this.documentsUris.get(auid)) == null){
			synchronized(this.selector){
				this.selector.reset();
				this.selector.setAUID(auid);
				documentUrl = this.selector.getString();
			}
		}
		
		this.stack.getDocument(documentUrl);
	}
	
	private void contactsFromXcap(List<ListType> lists){
		if(lists == null){
			return;
		}
		this.groups.clear();
		
		for(ListType list : lists){
			Group group = new Group(list.getName(), list.getDisplayName().getValue());
			for(EntryType entry : list.getEntries()){
				EntryType.DisplayName displayName = entry.getDisplayName();
				String Uri = entry.getUri();
				Group.Contact contact = new Group.Contact(Uri, displayName == null ? Uri : displayName.getValue());
				group.addContact(contact);
			}
			this.groups.add(group);
		}
		
		this.onXcapEvent(new XcapEventArgs(XcapEventTypes.CONTACTS_DOWNLOADED));
	}
	
	private void handleEvent(short code, String phrase, String contentType, byte[]content){
		final Serializer serializer;
		
		try{
			switch(this.currentSate){
				case GET_XCAP_CAPS:
					if(Xcap.isSuccess(code) && StringUtils.equals(Xcap.XCAP_AUID_IETF_XCAP_CAPS_MIME_TYPE, contentType, true) && content != null){
						serializer = new Persister();
						final XcapCaps xcap_caps = serializer.read(XcapCaps.class, new String(content));
						final List<String> auids = xcap_caps.getAuids().getAuid();
						
						this.haveOMADirectory = auids.contains(Xcap.XCAP_AUID_OMA_DIRECTORY_ID);
						this.haveResourceLists = auids.contains(Xcap.XCAP_AUID_IETF_RESOURCE_LISTS_ID);
						this.haveRLS = auids.contains(Xcap.XCAP_AUID_IETF_RLS_SERVICES_ID);
						
						if(this.haveOMADirectory){
							this.currentSate = Xcap.State.GET_OMA_DIRECTORY;
							this.downloadDocument(Xcap.XCAP_AUID_OMA_DIRECTORY_ID);
						}
						else if(this.haveResourceLists){
							this.currentSate = Xcap.State.GET_RESOURCE_LISTS;
							this.downloadDocument(Xcap.XCAP_AUID_IETF_RESOURCE_LISTS_ID);
						}
					}
					break;
				case GET_OMA_DIRECTORY:
					if(Xcap.isSuccess(code) && StringUtils.equals(Xcap.XCAP_AUID_OMA_DIRECTORY_MIME_TYPE, contentType, true) && content != null){
						serializer = new Persister();
						final XcapDirectory xcap_dir= serializer.read(XcapDirectory.class, new String(content));
						final List<XcapDirectory.Folder> folders = xcap_dir.getFolder();
						for(final XcapDirectory.Folder folder : folders){
							final List<XcapDirectory.Folder.Entry> entries = folder.getEntry();
							if(entries.size()>0){
								this.documentsUris.put(folder.getAuid(), entries.get(0).getUri());
							}
						}
					}
					if(this.haveResourceLists){
						this.currentSate = Xcap.State.GET_RESOURCE_LISTS;
						this.downloadDocument(Xcap.XCAP_AUID_IETF_RESOURCE_LISTS_ID);
					}
					break;
				case GET_RESOURCE_LISTS:
					if(Xcap.isSuccess(code) && StringUtils.equals(Xcap.XCAP_AUID_IETF_RESOURCE_LISTS_MIME_TYPE, contentType, true) && content != null){
						serializer = new Persister();
						final ResourceLists resourceLists = serializer.read(ResourceLists.class, new String(content));
						final List<ListType> lists = resourceLists.getList();
						new Thread(new Runnable(){
							@Override
							public void run() {
								XcapService.this.contactsFromXcap(lists);
							}
						}).start();
					}
					if(this.haveRLS){
						this.currentSate = Xcap.State.GET_RLS;
						this.downloadDocument(Xcap.XCAP_AUID_IETF_RLS_SERVICES_ID);
					}
					break;
				case GET_RLS:
					if(Xcap.isSuccess(code) && StringUtils.equals(Xcap.XCAP_AUID_IETF_RLS_SERVICES_MIME_TYPE, contentType, true) && content != null){
						serializer = new Persister();
						final RlsServices rls_services = serializer.read(RlsServices.class, new String(content));
						final List<ServiceType> lists = rls_services.getService();
						for(ServiceType service : lists){
							
						}
					}
					break;
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

			if (message == null || (content_type = message.getXcapHeaderValue("Content-Type")) == null) {
				Log.e(XcapService.TAG, "Invalid Xcap message");
				return -1;
			}
			
			final byte[] content = message.getXcapContent();
			final short code = message.getCode();
			final String phrase = message.getPhrase();
			
			new Thread(new Runnable(){
				@Override
				public void run() {
					XcapService.this.handleEvent(code, phrase, content_type, content);
				}
			}).start();
			
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
