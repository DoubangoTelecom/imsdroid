package org.doubango.imsdroid.Screens;

import java.util.ArrayList;

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

public class ScreenHome extends Screen {

	/* ===================== Activity ======================== */

	private GridView gridView;
	private ArrayList<HomeScreenItem> items;
	private final IScreenService ScreenService;
	private final ISipService SipService;

	public ScreenHome() {
		super(SCREEN_TYPE.HOME);

		// Services
		this.ScreenService = ServiceManager.getScreenService();
		this.SipService = ServiceManager.getSipService();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_home);

		// default items
		this.items = new ArrayList<HomeScreenItem>();

		this.items.add(new HomeScreenItem(BitmapFactory.decodeResource(
				getResources(), R.drawable.sign_in), "Sign In", Screen.SCREEN_ID_ABOUT));
		this.items.add(new HomeScreenItem(BitmapFactory.decodeResource(
				getResources(), R.drawable.options), "Options", Screen.SCREEN_ID_ABOUT));
		this.items.add(new HomeScreenItem(BitmapFactory.decodeResource(
				getResources(), R.drawable.about), "About", Screen.SCREEN_ID_ABOUT));

		// gridView
		this.gridView = (GridView) this.findViewById(R.id.screen_home_gridview);
		this.gridView.setAdapter(new HomeScreenAdapter(this.items));
		this.gridView_setOnItemClickListener();
	}

	/* ===================== IScreen ======================== */
	public String getId() {
		return Screen.SCREEN_ID_HOME;
	}

	/* ===================== UI Events ======================== */
	private void gridView_setOnItemClickListener() {
		this.gridView.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				HomeScreenItem item = ScreenHome.this.items.get(position);
				if(item != null){
					ScreenHome.this.ScreenService.show(item.screenId);
				}
			}
		});
	}

	/* ===================== Adapter ======================== */

	private class HomeScreenItem {
		private Bitmap icon;
		private String text;
		private String screenId;

		private HomeScreenItem(Bitmap icon, String text, String screenId) {
			this.icon = icon;
			this.text = text;
			this.screenId = screenId;
		}
	}

	private class HomeScreenAdapter extends BaseAdapter {
		private ArrayList<HomeScreenItem> items;

		private HomeScreenAdapter(ArrayList<HomeScreenItem> items) {
			this.items = items;
		}

		private boolean addItem(Bitmap icon, String text, String screenId) {
			boolean ret = this.items.add(new HomeScreenItem(icon, text, screenId));
			this.notifyDataSetChanged();
			return ret;
		}

		private boolean removeItem(HomeScreenItem item) {
			boolean ret = this.items.remove(item);
			this.notifyDataSetChanged();
			return ret;
		}

		public int getCount() {
			return this.items.size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			HomeScreenItem item;

			if (view == null) { // if it's not recycled, initialize some
								// attributes
				view = getLayoutInflater().inflate(R.layout.screen_home_item,
						null);
			}

			if ((item = this.items.get(position)) == null) {
				return view;
			}

			ImageView iv = (ImageView) view
					.findViewById(R.id.screen_home_item_icon);
			// imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			iv.setImageBitmap(item.icon);
			TextView tv = (TextView) view
					.findViewById(R.id.screen_home_item_text);
			tv.setText(item.text);

			return view;
		}
	}
}
