package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ScreenSettings extends BaseScreen {
	private static String TAG = ScreenSettings.class.getCanonicalName();
	
	private GridView mGridView;
	
	public ScreenSettings() {
		super(SCREEN_TYPE.SETTINGS_T, TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_settings);
		
		mGridView = (GridView)findViewById(R.id.screen_settings_gridview);
		mGridView.setAdapter(new ScreenSettingsAdapter(this));
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final ScreenSettingsItem item = (ScreenSettingsItem)parent.getItemAtPosition(position);
				if (item != null) {
					ServiceManager.getScreenService().show(item.mClass, item.mClass.getCanonicalName());
				}
			}
		});
	}
	
	/**
	 * ScreenSettingsItem
	 */
	static class ScreenSettingsItem{
		final int mIconResId;
		final String mText;
		final Class<? extends Activity> mClass;
		
		private ScreenSettingsItem(int iconResId, String text, Class<? extends Activity> _class) {
			mIconResId = iconResId;
			mText = text;
			mClass = _class;
		}
	}
	
	/**
	 * ScreenSettingsAdapter
	 */
	static class ScreenSettingsAdapter extends BaseAdapter{
		final static ScreenSettingsItem[] sItems =  new ScreenSettingsItem[]{
    		new ScreenSettingsItem(R.drawable.general_48, "General", ScreenGeneral.class),
    		new ScreenSettingsItem(R.drawable.identity_48, "Identity", ScreenIdentity.class ),
    		new ScreenSettingsItem(R.drawable.network_48, "Network", ScreenNetwork.class),
    		new ScreenSettingsItem(R.drawable.lock_48, "Security", ScreenSecurity.class),
    		new ScreenSettingsItem(R.drawable.eab_48, "Contacts", ScreenContacts.class),
    		new ScreenSettingsItem(R.drawable.im_invisible_user_48, "Presence", ScreenPresence.class),
    		new ScreenSettingsItem(R.drawable.codecs_48, "Codecs", ScreenCodecs.class),
    		new ScreenSettingsItem(R.drawable.messaging_48, "Messaging", ScreenMessaging.class),
    		new ScreenSettingsItem(R.drawable.qos_qoe_48, "QoS/QoE", ScreenQoS.class),
    		new ScreenSettingsItem(R.drawable.natt_48, "NATT", ScreenNatt.class),
		};
		
		private final LayoutInflater mInflater;
		
		ScreenSettingsAdapter(Context context){
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			return sItems.length;
		}

		@Override
		public Object getItem(int position) {
			return sItems[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ScreenSettingsItem item = (ScreenSettingsItem)getItem(position);
			
			if(item == null){
				return null;
			}

			if (view == null) {
				view = mInflater.inflate(R.layout.screen_settings_item, null);
			}

			((ImageView) view .findViewById(R.id.screen_settings_item_icon)).setImageResource(item.mIconResId);
			((TextView) view.findViewById(R.id.screen_settings_item_text)).setText(item.mText);
			
			return view;
		}
		
	}
}
