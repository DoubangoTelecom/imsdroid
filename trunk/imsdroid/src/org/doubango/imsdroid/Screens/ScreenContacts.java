package org.doubango.imsdroid.Screens;

import java.util.List;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Contact;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ScreenContacts  extends Screen{
	
	private GridView gridView;
	private ScreenContactsAdapter adapter;
	
	private final IContactService contactService;
	private final IScreenService screenService;
	private final ISipService sipService;
	private final Handler handler;
	
	public ScreenContacts() {
		super(SCREEN_TYPE.CONTACTS_T);
		
		// Services
		this.contactService = ServiceManager.getContactService();
		this.screenService = ServiceManager.getScreenService();
		this.sipService = ServiceManager.getSipService();
		
		this.handler = new Handler();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_contacts);
        
        // gridView
		this.adapter = new ScreenContactsAdapter(this.contactService.getContacts());
		this.gridView = (GridView) this.findViewById(R.id.screen_contacts_gridview);
		this.gridView.setAdapter(this.adapter);
		this.gridView.setOnItemClickListener(this.gridView_OnItemClickListener);
	}
	
	
	/* ===================== UI Events ======================== */
	private OnItemClickListener gridView_OnItemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		}
	};
	
	
	
	
	/* ===================== Adapter ======================== */

	private class ScreenContactsAdapter extends BaseAdapter {
		private List<Contact> items;
		
		private ScreenContactsAdapter(List<Contact> items) {
			this.items = items;
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
