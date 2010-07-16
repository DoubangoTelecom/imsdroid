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

package org.doubango.imsdroid.Screens;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Group;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.imsdroid.events.ContactsEventArgs;
import org.doubango.imsdroid.events.IContactsEventHandler;
import org.doubango.imsdroid.media.MediaType;

import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class ScreenContacts  extends Screen 
implements IContactsEventHandler 
{
	
	private Spinner spGroups;
	private GridView gridView;
	private ScreenContactsAdapter adapter;
	
	private final IContactService contactService;
	private final IScreenService screenService;
	private final ISipService sipService;
	private final Handler handler;
	
	private String currentGroup = "rcs";
	
	private final static int MENU_NEW_CONTACT = 0;
	private final static int MENU_NEW_GROUP = 1;
	private final static int MENU_REFRESH = 2;
	private final static int MENU_DELETE_CONTACT = 3;
	private final static int MENU_DELETE_GROUP = 4;
	
	private final static int MENU_VOICE_CALL = 10;
	private final static int MENU_VISIO_CALL = 11;
	private final static int MENU_SEND_MESSAGE = 12;
	private final static int MENU_SEND_SMS = 13;
	private final static int MENU_SEND_FILE = 14;
	private final static int MENU_START_CHAT = 15;
	private final static int MENU_CONFERENCE = 16;
	
	
	public ScreenContacts() {
		super(SCREEN_TYPE.CONTACTS_T, ScreenContacts.class.getCanonicalName());
		
		// Services
		this.contactService = ServiceManager.getContactService();
		this.screenService = ServiceManager.getScreenService();
		this.sipService = ServiceManager.getSipService();
		
		this.handler = new Handler();
	}
	
	/* ===================== Activity ======================== */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_contacts);
        
        final List<Group>groups = ScreenContacts.this.contactService.getContacts();
        
        this.spGroups = (Spinner)this.findViewById(R.id.screen_contacts_spinner_Groups);
        ArrayAdapter<Group> adapter = new ArrayAdapter<Group>(ScreenContacts.this, android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ScreenContacts.this.spGroups.setAdapter(adapter);
        this.spGroups.setOnItemSelectedListener(this.spGroups_OnItemSelectedListener);
        
        // gridView
		this.adapter = new ScreenContactsAdapter(groups);
		this.gridView = (GridView) this.findViewById(R.id.screen_contacts_gridview);
		this.gridView.setAdapter(this.adapter);
		this.gridView.setOnItemClickListener(this.gridView_OnItemClickListener);
		this.gridView.setOnItemLongClickListener(this.gridview_OnItemLongClickListener);
		this.registerForContextMenu(this.gridView);
		
		// add event handler
		this.contactService.addContactsEventHandler(this);
	}

	
	@Override
	protected void onResume() {

		if(this.contactService.isLoadingContacts()){
			Toast.makeText(this, "Loading contacts...", Toast.LENGTH_SHORT).show();
		}
		
		super.onResume();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ScreenContacts.MENU_NEW_CONTACT, 0, "New Contact").setIcon(R.drawable.user_add_48);
		menu.add(0, ScreenContacts.MENU_NEW_GROUP, 0, "New Group").setIcon(R.drawable.group_add_48);
		menu.add(0, ScreenContacts.MENU_REFRESH, 0, "Refresh").setIcon(R.drawable.user_refresh_48);
		menu.add(1, ScreenContacts.MENU_DELETE_CONTACT, 0, "Delete Contact").setIcon(R.drawable.user_delete_48);
		menu.add(1, ScreenContacts.MENU_DELETE_GROUP, 0, "Delete Group").setIcon(R.drawable.group_delete_48);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
			case ScreenContacts.MENU_NEW_CONTACT:
				ScreenContactEdit.add();
				break;
			case ScreenContacts.MENU_NEW_GROUP:
				Toast.makeText(this, "New Group", Toast.LENGTH_SHORT).show();
				break;
			case ScreenContacts.MENU_REFRESH:
				Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
				break;
			case ScreenContacts.MENU_DELETE_CONTACT:
				Toast.makeText(this, "Delete Contact", Toast.LENGTH_SHORT).show();
				break;
			case ScreenContacts.MENU_DELETE_GROUP:
				Toast.makeText(this, "Delete Group", Toast.LENGTH_SHORT).show();
				break;
		}
		return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		menu.add(0, ScreenContacts.MENU_VOICE_CALL, 0, "Make Voice Call");
		menu.add(0, ScreenContacts.MENU_VISIO_CALL, 0, "Make Visio Call");
		menu.add(0, ScreenContacts.MENU_SEND_MESSAGE, 0, "Send Short Message");
		menu.add(0, ScreenContacts.MENU_SEND_SMS, 0, "Send SMS");
		menu.add(0, ScreenContacts.MENU_SEND_FILE, 0, "Send File");
		menu.add(0, ScreenContacts.MENU_START_CHAT, 0, "Start Chat");
		menu.add(0, ScreenContacts.MENU_CONFERENCE, 0, "Start Conference");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Group.Contact contact = null;
		
		if((contact = this.adapter.getContact(((AdapterContextMenuInfo) item.getMenuInfo()).position)) == null){ /* should never happen ...but who know? */
			return super.onContextItemSelected(item);
		}
		
		switch(item.getItemId()){
			case ScreenContacts.MENU_VOICE_CALL:
				ScreenAV.makeCall(contact.getUri(), MediaType.Audio);
				break;
			case ScreenContacts.MENU_VISIO_CALL:
				ScreenAV.makeCall(contact.getUri(), MediaType.AudioVideo);
				return true;
//			case ScreenContacts.MENU_SEND_MESSAGE:
//				Toast.makeText(this, "Send Short Message: " + contact.getUri(), Toast.LENGTH_SHORT).show();
//				return true;
//			case ScreenContacts.MENU_SEND_SMS:
//				Toast.makeText(this, "Send SMS: " + contact.getUri(), Toast.LENGTH_SHORT).show();
//				return true;
//			case ScreenContacts.MENU_SEND_FILE:
//				Toast.makeText(this, "Send File: " + contact.getUri(), Toast.LENGTH_SHORT).show();
//				return true;
//			case ScreenContacts.MENU_START_CHAT:
//				Toast.makeText(this, "Start Chat: " + contact.getUri(), Toast.LENGTH_SHORT).show();
//				return true;
//			case ScreenContacts.MENU_CONFERENCE:
//				Toast.makeText(this, "Start Conference: " + contact.getUri(), Toast.LENGTH_SHORT).show();
//				return true;

		}
		
		return true;
	}
	
	private OnItemSelectedListener spGroups_OnItemSelectedListener = new OnItemSelectedListener(){
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			Group group = (Group)parent.getItemAtPosition(position);
			ScreenContacts.this.currentGroup = group.getName();
			ScreenContacts.this.adapter.update();
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};
	
	@Override
	protected void onDestroy() {

		// remove event handler
		this.contactService.removeContactsEventHandler(this);
		
		super.onDestroy();
	}

	/* ===================== UI Events ======================== */
	private OnItemClickListener gridView_OnItemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			//Contact contact = ScreenContacts.this.adapter.getContact(position);
			//if(contact != null){
			//	ScreenContactView.show(contact);
			//}
		}
	};
	
	private OnItemLongClickListener gridview_OnItemLongClickListener = new OnItemLongClickListener(){
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			
			return false;
		}
	};
	
	/* ===================== IScreen (Screen) ======================== */
	@Override
	public boolean haveMenu(){
		return true;
	}
	
	/* ===================== IContactsEventHandler ======================== */
	public boolean onContactsEvent(Object sender, ContactsEventArgs e) {
		// already on its own thread
		switch(e.getType()){
			case CONTACTS_LOADED:
			case CONTACT_ADDED:
			case CONTACT_CHANGED:
				this.handler.post(new Runnable(){
					public void run() {
						final List<Group>groups = ScreenContacts.this.contactService.getContacts();
						
						ArrayAdapter<Group> adapter = new ArrayAdapter<Group>(ScreenContacts.this, android.R.layout.simple_spinner_item, groups);
				        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				        ScreenContacts.this.spGroups.setAdapter(adapter);
				        
						ScreenContacts.this.adapter.update(groups);
					}
				});
				break;
			default:
				break;
		}
		return true;
	}
	
	
	/* ===================== Adapter ======================== */

	class MyDisplayNameComparable implements Comparator<Group.Contact>{
	    @Override
	    public int compare(Group.Contact c1, Group.Contact c2) {
	    	final String d1 = c1.getDisplayName();
	    	final String d2 = c2.getDisplayName();
	    	if(d1 != null && d2 != null){
	    		return d1.compareToIgnoreCase(d2);
	    	}
	    	return 0;
	    }
	}

	private class ScreenContactsAdapter extends BaseAdapter {
		private List<Group> items;
		private List<Group.Contact> contacts;
		
		private ScreenContactsAdapter(List<Group> items) {
			this.items = items;
		}

		private synchronized void update(List<Group> items){
			this.items = items;
			this.notifyDataSetChanged();
		}
		
		private synchronized void update(){
			this.notifyDataSetChanged();
		}
		
		public int getCount() {
			if(this.items != null){
				for(Group group : this.items){
					if(group.getName().equals(ScreenContacts.this.currentGroup)){
						this.contacts = group.getContacts();
						Collections.sort(this.contacts, new MyDisplayNameComparable());
						return this.contacts.size();
					}
				}
			}
			return 0;
		}

		public Object getItem(int position) {
			return null;
		}
		
		public Group.Contact getContact(int position){
			if(this.getCount() > position){
				return this.contacts.get(position);
			}
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			
			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.screen_contacts_item, null);
			}
			
			final Group.Contact contact;
			if (this.contacts == null || (contact = this.contacts.get(position)) == null) {
				return view;
			}

			ImageView ivAvatar = (ImageView) view.findViewById(R.id.screen_contacts_item_imageView_avatar);
			ImageView ivStatus = (ImageView) view.findViewById(R.id.screen_contacts_item_imageView_status);
			TextView tvUri = (TextView) view.findViewById(R.id.screen_contacts_item_textViewUri);
			TextView tvDisplayName = (TextView) view.findViewById(R.id.screen_contacts_item_textView_displayname);
			TextView tvFreeText = (TextView) view.findViewById(R.id.screen_contacts_item_textView_freetext);
			
			if(contact.getAvatar() != null){
				ivAvatar.setImageBitmap(contact.getAvatar());
			}
			if(contact.getUri() != null){
				tvUri.setText(contact.getUri());
			}
			
			if(contact.getDisplayName() != null){
				tvDisplayName.setText(contact.getDisplayName());
			}
			else{
				tvDisplayName.setText("Unknown");
			}
			if(contact.getFreeText() != null){
				tvFreeText.setText(contact.getFreeText());
			}
			else{
				tvFreeText.setText("");
			}
			
			switch(contact.getStatus()){
				case Online:
					ivStatus.setImageResource(R.drawable.user_online_24);
					break;
				case Busy:
					ivStatus.setImageResource(R.drawable.user_busy_24);
					break;
				case Away:
					ivStatus.setImageResource(R.drawable.user_time_24);
					break;
				case BeRightBack:
					ivStatus.setImageResource(R.drawable.user_back_24);
					break;
				case OnThePhone:
					ivStatus.setImageResource(R.drawable.user_onthephone_24);
					break;
				case HyperAvail:
					ivStatus.setImageResource(R.drawable.user_hyper_avail_24);
					break;
				case Offline:
				default:
					ivStatus.setImageResource(R.drawable.user_offline_24);
					break;
			}

			return view;
		}
	}
}
