package org.doubango.imsdroid.Sevices.Impl;

import java.io.File;
import java.io.IOException;

import org.doubango.imsdroid.Services.IContactService;

public class ContactService  extends Service implements IContactService{

	private final static String CONTACTS_FILE = "contacts.xml";
	private File contacts_file;
	
	public boolean start() {
		// Creates configuration file if does not exist
		this.contacts_file = new File(String.format("%s/%s", ServiceManager.getStorageService().getCurrentDir(), ContactService.CONTACTS_FILE));
		if(!this.contacts_file.exists()){
			try {
				this.contacts_file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public boolean stop() {
		return true;
	}

}
