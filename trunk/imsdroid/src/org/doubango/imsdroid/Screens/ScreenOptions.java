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
		super(SCREEN_TYPE.OPTIONS_T);
		
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
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.general_48), "General", Screen.SCREEN_ID.GENERAL_I.toString()),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.identity_48), "Identity", Screen.SCREEN_ID.IDENTITY_I.toString()),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.network_48), "Network", Screen.SCREEN_ID.NETWORK_I.toString()),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.lock_48), "Security", Screen.SCREEN_ID.SECURITY_I.toString()),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.eab_48), "Contacts", Screen.SCREEN_ID.CONTACTS_OPTIONS_I.toString()),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.people_48), "Presence", Screen.SCREEN_ID.PRESENCE_I.toString()),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.codecs_48), "Codecs", null),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.messaging_48), "Messaging", Screen.SCREEN_ID.MESSAGING_I.toString()),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.qos_qoe_48), "QoS/QoE", Screen.SCREEN_ID.QOS_I.toString()),
        		new ScreenOptionsItem(BitmapFactory.decodeResource(getResources(), R.drawable.natt_48), "NATT", Screen.SCREEN_ID.NATT_I.toString()),
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
					ScreenOptions.this.ScreenService.show(item.screenId);
				}
			}
		});
	}
	
	/* ===================== Adapter ======================== */

	private class ScreenOptionsItem {
		private Bitmap icon;
		private String text;
		private String screenId;

		private ScreenOptionsItem(Bitmap icon, String text, String screenId) {
			this.icon = icon;
			this.text = text;
			this.screenId = screenId;
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
