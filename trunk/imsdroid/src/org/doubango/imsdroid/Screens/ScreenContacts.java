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

import java.util.List;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Contact;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.imsdroid.events.ContactsEventArgs;
import org.doubango.imsdroid.events.IContactsEventHandler;

import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ScreenContacts  extends Screen 
implements IContactsEventHandler 
{
	
	private GridView gridView;
	private ScreenContactsAdapter adapter;
	
	private final IContactService contactService;
	private final IScreenService screenService;
	private final ISipService sipService;
	private final Handler handler;
	
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
        
        // gridView
		this.adapter = new ScreenContactsAdapter(this.contactService.getContacts());
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
				Toast.makeText(this, "New Contact", Toast.LENGTH_SHORT).show();
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
		Contact contact = null;
		
		if((contact = this.adapter.getContact(((AdapterContextMenuInfo) item.getMenuInfo()).position)) == null){ /* should never happen ...but who know? */
			return super.onContextItemSelected(item);
		}
		
		switch(item.getItemId()){
			case ScreenContacts.MENU_VOICE_CALL:
				Toast.makeText(this, "Make Voice Call: " + contact.getUri(), Toast.LENGTH_SHORT).show();
				return true;
			case ScreenContacts.MENU_VISIO_CALL:
				Toast.makeText(this, "Make Visio Call: " + contact.getUri(), Toast.LENGTH_SHORT).show();
				return true;
			case ScreenContacts.MENU_SEND_MESSAGE:
				Toast.makeText(this, "Send Short Message: " + contact.getUri(), Toast.LENGTH_SHORT).show();
				return true;
			case ScreenContacts.MENU_SEND_SMS:
				Toast.makeText(this, "Send SMS: " + contact.getUri(), Toast.LENGTH_SHORT).show();
				return true;
			case ScreenContacts.MENU_SEND_FILE:
				Toast.makeText(this, "Send File: " + contact.getUri(), Toast.LENGTH_SHORT).show();
				return true;
			case ScreenContacts.MENU_START_CHAT:
				Toast.makeText(this, "Start Chat: " + contact.getUri(), Toast.LENGTH_SHORT).show();
				return true;
			case ScreenContacts.MENU_CONFERENCE:
				Toast.makeText(this, "Start Conference: " + contact.getUri(), Toast.LENGTH_SHORT).show();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {

		// remove event handler
		this.contactService.removeContactsEventHandler(this);
		
		super.onDestroy();
	}

	/* ===================== UI Events ======================== */
	private OnItemClickListener gridView_OnItemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Contact contact = ScreenContacts.this.adapter.getContact(position);
			if(contact != null){
				ScreenContactView.show(contact);
			}
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
				this.handler.post(new Runnable(){
					public void run() {
						ScreenContacts.this.adapter.update(ScreenContacts.this.contactService.getContacts());
					}
				});
				break;
			default:
				break;
		}
		return true;
	}
	
	
	/* ===================== Adapter ======================== */

	private class ScreenContactsAdapter extends BaseAdapter {
		private List<Contact> items;
		
		private ScreenContactsAdapter(List<Contact> items) {
			this.items = items;
		}

		private synchronized void update(List<Contact> items){
			this.items = items;
			this.notifyDataSetChanged();
		}
		
		public int getCount() {
			return this.items == null ? 0 : this.items.size();
		}

		public Object getItem(int position) {
			return null;
		}
		
		public Contact getContact(int position){
			if(this.getCount() > position){
				return this.items.get(position);
			}
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			Contact contact;

			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.screen_contacts_item, null);
			}
			if ((contact = this.items.get(position)) == null) {
				return view;
			}

			ImageView ivAvatar = (ImageView) view.findViewById(R.id.screen_contacts_item_imageView_avatar);
			TextView tvDisplayName = (TextView) view.findViewById(R.id.screen_contacts_item_textView_displayname);
			TextView tvFreeText = (TextView) view.findViewById(R.id.screen_contacts_item_textView_freetext);
			
			if(contact.getAvatar() != null){
				ivAvatar.setImageBitmap(contact.getAvatar());
			}
			if(contact.getDisplayName() != null){
				tvDisplayName.setText(contact.getDisplayName());
			}
			if(contact.getFreeText() != null){
				tvFreeText.setText(contact.getFreeText());
			}

			return view;
		}
	}
}
