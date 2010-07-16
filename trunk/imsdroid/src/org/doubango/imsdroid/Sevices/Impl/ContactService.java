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

import ietf.params.xml.ns.pidf.Basic;
import ietf.params.xml.ns.pidf.Note;
import ietf.params.xml.ns.pidf.Presence;
import ietf.params.xml.ns.pidf.Status;
import ietf.params.xml.ns.pidf.Tuple;
import ietf.params.xml.ns.pidf.data_model.NoteT;
import ietf.params.xml.ns.pidf.data_model.Person;
import ietf.params.xml.ns.pidf.rpid.Activities;
import ietf.params.xml.ns.pidf.rpid.Activities.activity;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import oma.xml.prs.pidf.oma_pres.OverridingWillingness;

import org.doubango.imsdroid.Model.AddressBook;
import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Group;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.events.ContactsEventArgs;
import org.doubango.imsdroid.events.ContactsEventTypes;
import org.doubango.imsdroid.events.EventHandler;
import org.doubango.imsdroid.events.IContactsEventHandler;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.ISubscriptionEventHandler;
import org.doubango.imsdroid.events.IXcapEventHandler;
import org.doubango.imsdroid.events.RegistrationEventArgs;
import org.doubango.imsdroid.events.SubscriptionEventArgs;
import org.doubango.imsdroid.events.XcapEventArgs;
import org.doubango.imsdroid.sip.MySubscriptionSession;
import org.doubango.imsdroid.sip.PresenceStatus;
import org.doubango.imsdroid.sip.MySubscriptionSession.EVENT_PACKAGE_TYPE;
import org.doubango.imsdroid.utils.ContentType;
import org.doubango.imsdroid.utils.StringUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class ContactService  extends Service implements IContactService, IRegistrationEventHandler, ISubscriptionEventHandler, IXcapEventHandler{

	private final static String TAG = ContactService.class.getCanonicalName();
	
	// Event Handlers
	private final CopyOnWriteArrayList<IContactsEventHandler> contactsEventHandlers;
	
	private final static String CONTACTS_FILE = "contacts.xml";
	private File contacts_file;
	private AddressBook addressBook;
	private final Serializer serializer;
	
	private boolean remote;
	private boolean presence;
	private boolean rls;
	private boolean loadingContacts;
	
	public ContactService(){
		super();
		
		this.serializer = new Persister();
		this.addressBook = new AddressBook();
		this.contactsEventHandlers = new CopyOnWriteArrayList<IContactsEventHandler>();
	}
	
	public boolean start() {
		// Creates configuration file if does not exist
		this.contacts_file = new File(String.format("%s/%s", ServiceManager.getStorageService().getCurrentDir(), ContactService.CONTACTS_FILE));
		if(!this.contacts_file.exists()){
			try {
				this.contacts_file.createNewFile();
				this.compute(); /* to create an empty but valid document */
			} catch (IOException e) {
				e.printStackTrace();
				this.contacts_file = null;
				return false;
			}
		}
		
		// add event handlers
		ServiceManager.getSipService().addRegistrationEventHandler(this);
		ServiceManager.getSipService().addSubscriptionEventHandler(this);
		ServiceManager.getXcapService().addXcapEventHandler(this);
		return true;
	}

	public boolean stop() {
		// remove event handlers
		ServiceManager.getSipService().removeRegistrationEventHandler(this);
		ServiceManager.getSipService().removeSubscriptionEventHandler(this);
		ServiceManager.getXcapService().removeXcapEventHandler(this);
		return true;
	}

	public void addContact(Group.Contact contact){
		if(this.remote){
			if(this.presence){
				
			}
		}
		else{
			if(this.addressBook.addContact(contact)){
				new Thread(new Runnable(){
					@Override
					public void run() {
						ContactService.this.compute();
						ContactService.this.onContactsEvent(new ContactsEventArgs(ContactsEventTypes.CONTACT_ADDED));
						if(ContactService.this.presence){
							
						}
					}
				}).start();
			}
		}
	}
	
	public void editContact(Group.Contact contact){
		if(this.remote){
			
		}
		else{
			
		}
	}
	
	public void removeContact(Group.Contact contact){
		if(this.remote){
			
		}
		else{
			
		}
	}
	
	public List<Group> getContacts(){
		return this.addressBook.getGroups();
	}
	
	public boolean isLoadingContacts(){
		return this.loadingContacts;
	}

	public boolean loadContacts() {
		if(this.remote){
			// Wait for XcapService Events
		}
		else{
			if(this.contacts_file == null){
				return false;
			}
			new Thread(this.loadLocalContacts).start();
		}
		
		return true;
	}
	
	/* ===================== Add/Remove handlers ======================== */
	@Override
	public boolean addContactsEventHandler(IContactsEventHandler handler) {
		return EventHandler.addEventHandler(this.contactsEventHandlers, handler);
	}
	@Override
	public boolean removeContactsEventHandler(IContactsEventHandler handler) {
		return EventHandler.removeEventHandler(this.contactsEventHandlers, handler);
	}
	
	/* ===================== Dispatch events ======================== */
	private synchronized void onContactsEvent(final ContactsEventArgs eargs) {
		for(IContactsEventHandler handler : this.contactsEventHandlers){
			if (!handler.onContactsEvent(this, eargs)) {
				Log.w(handler.getClass().getName(), "onContactsEvent failed");
			}
		}
	}
	
	/* ===================== IXcapEventHandler implement ======================== */
	@Override
	public boolean onXcapEvent(Object sender, XcapEventArgs e){
		
		switch(e.getType()){
			case CONTACTS_DOWNLOADED:
				/* Presence */
				this.addressBook.set(ServiceManager.getXcapService().getGroups());
				ContactService.this.onContactsEvent(new ContactsEventArgs(ContactsEventTypes.CONTACTS_LOADED));
				break;
		}
		return true;
	}
	
	/* ===================== Sip Events ========================*/
	@Override
	public boolean onRegistrationEvent(Object sender, RegistrationEventArgs e) {
		/* already in it's own thread */
		switch(e.getType()){
			case REGISTRATION_OK:
				this.remote = ServiceManager.getConfigurationService().getBoolean(
						CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.ENABLED,
						Configuration.DEFAULT_XCAP_ENABLED);
				this.presence = ServiceManager.getConfigurationService().getBoolean(
						CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.PRESENCE,
							Configuration.DEFAULT_RCS_PRESENCE);
				this.rls = ServiceManager.getConfigurationService().getBoolean(
						CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.RLS, 
						Configuration.DEFAULT_RCS_RLS);
				return this.loadContacts();
			case UNREGISTRATION_OK:
				this.addressBook.clear();
				ContactService.this.onContactsEvent(new ContactsEventArgs(ContactsEventTypes.CONTACTS_LOADED));
				break;
			case REGISTRATION_NOK:
			case REGISTRATION_INPROGRESS:
			case UNREGISTRATION_NOK:
			case UNREGISTRATION_INPROGRESS:
				break;
		}
		return true;
	}
	
	public boolean onSubscriptionEvent(Object sender, SubscriptionEventArgs e) {
		/* already in it's own thread */
		switch(e.getType()){
			case INCOMING_NOTIFY:
				final String contentType = e.getContentType();
				if(StringUtils.equals(contentType, ContentType.PIDF, true)){
					final byte[] content = e.getContent();
					if(content == null){
						return false;
					}
					
					final Presence presence;
					final String entity;
					Person person = null;
					Group.Contact contact = null;
					String freeText = null;
					PresenceStatus status = PresenceStatus.Offline;
					
					try {
						presence = this.serializer.read(Presence.class, new String(content));
					} catch (Exception e1) {
						e1.printStackTrace();
						return false;
					}
					
					if(presence == null || (entity = presence.getEntity()) == null){
						Log.e(ContactService.TAG, "Invalid Pidf document");
						return false;
					}
					
					Date timeStamp = null;
					for(Person p : presence.getPersons()){
						if(timeStamp == null || (p.getTimestamp() != null && p.getTimestamp().compareTo(timeStamp)>0)){
							person = p;
							timeStamp = p.getTimestamp();
						}
					}
					
					for(Group g : this.addressBook.getGroups()){
						if((contact = g.getContact(entity)) != null){
							break;
						}
					}
					if(contact == null){
						return false;
					}
					
					if(person == null){
						if(!presence.getTuple().isEmpty()){
							Tuple tuple =  presence.getTuple().get(0);
							if(tuple != null){
								final Note note = tuple.getNote().isEmpty() ? null : tuple.getNote().get(0);
								final Status s = tuple.getStatus();
								if(note != null){
									freeText = note.getValue();
								}
								if(s != null){
									status = (s.getBasic() == Basic.open) ? PresenceStatus.Online : PresenceStatus.Offline;
								}
							}
						}
					}
					else{
						final OverridingWillingness overridingWillingness = person.getOverridingWillingness();
						final Activities activities = person.getActivities();
						
						if(overridingWillingness != null){
							// FIXME
						}
						
						if(activities != null && !activities.getAppointmentOrAwayOrBreakfast().isEmpty()){
							final activity activity = activities.getAppointmentOrAwayOrBreakfast().get(0);
							
							switch(activity.getType()){
								case busy:
									status = PresenceStatus.Busy;
									break;
									
								case worship:
								case working:
								case playing:
								case appointment:
								case presentation:
								case meeting:
								case shopping:
								case sleeping:
								case in_transit:
								case breakfast:
								case meal:						
								case dinner:
								case vacation:
									status = PresenceStatus.BeRightBack;
									break;
									
								case on_the_phone:
									status = PresenceStatus.OnThePhone;
									break;
									
								case tv:
								case travel:
								case away:
								case permanent_absence:
								case holiday:				
									status = PresenceStatus.Away;
									break;
														
								case other:								
								case looking_for_work:
								case steering:
								case spectator:
								case performance:
								default:
									break;
							}
						}
						
						// FIXME: avatar
						// FIXME
						
						if(!person.getNote().isEmpty()){
							final NoteT note = person.getNote().get(0);
							if(note != null){
								freeText = note.getValue();
							}
						}
					}
					
					contact.setStatus(status);
					if(freeText != null){
						contact.setFreeText(freeText);
					}
					final ContactsEventArgs eargs = new ContactsEventArgs(ContactsEventTypes.CONTACT_CHANGED);
					eargs.putExtra("contact", contact);
					ContactService.this.onContactsEvent(eargs);
				}
				break;
				
			default:
				break;
		}
		return true;
	}
	
	/* ===================== Internal functions ========================*/
	private Runnable  loadLocalContacts = new Runnable(){
		public void run() {
			ContactService.this.loadingContacts = true;
			
			try {
				Log.d(ContactService.TAG, "Loading contacts (local)");
				ContactService.this.addressBook = ContactService.this.serializer.read(AddressBook.class, ContactService.this.contacts_file);
				if(ContactService.this.addressBook.getGroup("rcs") == null){
					ContactService.this.addressBook.addGroup("rcs", "Social buddies");
				}
				Log.d(ContactService.TAG, "Contacts loaded(local)");
				
				if(ContactService.this.presence){
					Log.d(ContactService.TAG, "Subscribing to presence(local)");
					Group group = ContactService.this.addressBook.getGroup("rcs");
					if(group != null){
						for(Group.Contact contact : group.getContacts()){
							MySubscriptionSession session = ServiceManager.getSipService().createPresenceSession(contact.getUri(), EVENT_PACKAGE_TYPE.PRESENCE);
							session.subscribe();
						}
					}
				}
			} catch (Exception e) {
				Log.e(ContactService.TAG, "Failed to load contacts(local)");
				e.printStackTrace();
			}
			ContactService.this.loadingContacts = false;
			ContactService.this.onContactsEvent(new ContactsEventArgs(ContactsEventTypes.CONTACTS_LOADED));
		}
	};
	
	private boolean compute(){
		if(this.contacts_file == null){
			return false;
		}
		try{
			this.serializer.write(this.addressBook, this.contacts_file);
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}	
}
