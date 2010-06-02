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
import org.doubango.imsdroid.xml.reginfo.Reginfo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

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
	
	public ScreenContacts() {
		super(SCREEN_TYPE.CONTACTS_T);
		
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
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case ScreenContacts.MENU_NEW_CONTACT:
				break;
			case ScreenContacts.MENU_NEW_GROUP:
				break;
			case ScreenContacts.MENU_REFRESH:
				break;
		}
		return true;
	}
	
	@Override
	protected void onDestroy() {

		// add event handler
		this.contactService.removeContactsEventHandler(this);
		
		super.onDestroy();
	}

	/* ===================== UI Events ======================== */
	private OnItemClickListener gridView_OnItemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		}
	};
	
	/* ===================== IScreen (Screen) ======================== */
	@Override
	public boolean haveMenu(){
		return true;
	}
	
	/* ===================== IContactsEventHandler ======================== */
	@Override
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
