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

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class ScreenOptions extends Screen {
	
	/* ===================== Activity ======================== */

	private GridView gridView;
	private final IScreenService ScreenService;
	private final ISipService SipService;
	
	private ScreenOptionsItem[] items;
	
	public ScreenOptions() {
		super(SCREEN_TYPE.OPTIONS_T, ScreenOptions.class.getCanonicalName());
		
		// Services
		this.ScreenService = ServiceManager.getScreenService();
		this.SipService = ServiceManager.getSipService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_options);
        
        // items
        this.items =  new ScreenOptionsItem[]{
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.general_48), "General", ScreenGeneral.class),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.identity_48), "Identity", ScreenIdentity.class ),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.network_48), "Network", ScreenNetwork.class),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.lock_48), "Security", ScreenSecurity.class),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.eab_48), "Contacts", ScreenContacts.class),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.people_48), "Presence", ScreenPresence.class),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.codecs_48), "Codecs", null),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.messaging_48), "Messaging", ScreenMessaging.class),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.qos_qoe_48), "QoS/QoE", ScreenQoS.class),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.natt_48), "NATT", ScreenNatt.class),
        };
        
        // gridView
		this.gridView = (GridView) this.findViewById(R.id.screen_options_gridview);
		this.gridView.setAdapter(new ScreenOptionsAdapter());
		this.gridView_setOnItemClickListener();
	}
	
	
/* ===================== IScreen ======================== */
	

	/* ===================== UI Events ======================== */
	private void gridView_setOnItemClickListener() {
		this.gridView.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ScreenOptionsItem item;
				if ((ScreenOptions.this.items.length > position) && ((item = ScreenOptions.this.items[position]) != null)) {
					ScreenOptions.this.ScreenService.show(item.screenCls, item.screenCls.getCanonicalName());
				}
			}
		});
	}
	
	/* ===================== Adapter ======================== */

	private class ScreenOptionsItem {
		private Bitmap icon;
		private String text;
		private Class<? extends Screen> screenCls;

		private ScreenOptionsItem(Bitmap icon, String text, Class<? extends Screen> screenCls) {
			this.icon = icon;
			this.text = text;
			this.screenCls = screenCls;
		}
	}
	
	private class ScreenOptionsAdapter extends BaseAdapter {

		public int getCount() {
			return ScreenOptions.this.items.length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ScreenOptionsItem item;

			if (view == null) { // if it's not recycled, initialize some
								// attributes
				view = getLayoutInflater().inflate(R.layout.screen_options_item, null);
			}

			if ((ScreenOptions.this.items.length <= position) || ((item = ScreenOptions.this.items[position]) == null)) {
				return view;
			}

			ImageView iv = (ImageView) view .findViewById(R.id.screen_options_item_icon);
			// imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			iv.setImageBitmap(item.icon);
			TextView tv = (TextView) view.findViewById(R.id.screen_options_item_text);
			tv.setText(item.text);

			return view;
		}
	}
}
