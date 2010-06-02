package org.doubango.imsdroid.Sevices.Impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Contact;
import org.doubango.imsdroid.Model.ContactList;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.events.ContactsEventArgs;
import org.doubango.imsdroid.events.ContactsEventTypes;
import org.doubango.imsdroid.events.EventHandler;
import org.doubango.imsdroid.events.IContactsEventHandler;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.RegistrationEventArgs;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class ContactService  extends Service implements IContactService, IRegistrationEventHandler{

	private final static String TAG = ContactService.class.getCanonicalName();
	
	// Event Handlers
	private final CopyOnWriteArrayList<IContactsEventHandler> contactsEventHandlers;
	
	private final static String CONTACTS_FILE = "contacts.xml";
	private File contacts_file;
	private ContactList contacts;
	private final Serializer serializer;
	
	private boolean loadingContacts;
	
	public ContactService(){
		super();
		
		this.serializer = new Persister();
		this.contacts = new ContactList();
		this.contactsEventHandlers = new CopyOnWriteArrayList<IContactsEventHandler>();
		
		
//		for(int i=0; i<5; i++){
//			Contact c = new Contact();
//			c.setUri("sip:" + i + "@open-ims.test");
//			c.setDisplayName(Integer.toString(i));
//			c.setFreeText("Hello world(" + i + ")");
//			this.contacts.addContact(c);
//		}
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
		
		// add sip event handlers
		ServiceManager.getSipService().addRegistrationEventHandler(this);
		return true;
	}

	public boolean stop() {
		
		// remove sip event handlers
		ServiceManager.getSipService().removeRegistrationEventHandler(this);
		return true;
	}

	public List<Contact> getContacts(){
		return this.contacts.getList();
	}
	
	public boolean isLoadingContacts(){
		return this.loadingContacts;
	}

	public boolean loadContacts() {		
		boolean remote = ServiceManager.getConfigurationService().getBoolean(
				CONFIGURATION_SECTION.XCAP, CONFIGURATION_ENTRY.ENABLED,
				Configuration.DEFAULT_XCAP_ENABLED);
		if(remote){
			new Thread(this.loadRemoteContacts).start();
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
	public boolean addContactsEventHandler(IContactsEventHandler handler) {
		return EventHandler.addEventHandler(this.contactsEventHandlers, handler);
	}

	public boolean removeContactsEventHandler(IContactsEventHandler handler) {
		return EventHandler.removeEventHandler(this.contactsEventHandlers, handler);
	}
	
	/* ===================== Dispatch events ======================== */
	private synchronized void onContactsEvent(final ContactsEventArgs eargs) {
//		for(int i = 0; i<this.contactsEventHandlers.size(); i++){
//			final IContactsEventHandler handler = this.contactsEventHandlers.get(i);
//			new Thread(new Runnable() {
//				public void run() {
//					if (!handler.onContactsEvent(this, eargs)) {
//						Log.w(handler.getClass().getName(), "onContactsEvent failed");
//					}
//				}
//			}).start();
//		}
		for(IContactsEventHandler handler : this.contactsEventHandlers){
			if (!handler.onContactsEvent(this, eargs)) {
				Log.w(handler.getClass().getName(), "onContactsEvent failed");
			}
		}
	}
	
	/* ===================== Sip Events ========================*/
	public boolean onRegistrationEvent(Object sender, RegistrationEventArgs e) {
		switch(e.getType()){
			case REGISTRATION_OK:
				return this.loadContacts();
			case REGISTRATION_NOK:
			case REGISTRATION_INPROGRESS:
			case UNREGISTRATION_OK:
			case UNREGISTRATION_NOK:
			case UNREGISTRATION_INPROGRESS:
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
				ContactService.this.contacts = ContactService.this.serializer.read(ContactService.this.contacts.getClass(), ContactService.this.contacts_file);
				Log.d(ContactService.TAG, "Contacts loaded(local)");
			} catch (Exception e) {
				Log.d(ContactService.TAG, "Failed to load contacts(local)");
				e.printStackTrace();
			}
			ContactService.this.loadingContacts = false;
			ContactService.this.onContactsEvent(new ContactsEventArgs(ContactsEventTypes.CONTACTS_LOADED));
		}
	};
	
	private Runnable  loadRemoteContacts = new Runnable(){
		public void run() {
			
		}
	};
	
	private boolean compute(){
		if(this.contacts_file == null){
			return false;
		}
		try{
			this.serializer.write(this.contacts, this.contacts_file);
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}	
}
