package org.doubango.imsdroid.Screens;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.doubango.imsdroid.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeScreen extends Screen{
	
/* ===================== Activity ========================*/
    
	private GridView gridView;
	private ArrayList<HomeScreenItem> items;
	
    public HomeScreen() {
		super(SCREEN_TYPE.HOME);
	}

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_home);
        
        // default items
        this.items = new ArrayList<HomeScreenItem>();
        for(int titi=0; titi<10;titi++)
        this.items.add(new HomeScreenItem(BitmapFactory.decodeResource(getResources(), R.drawable.sign_in), "Sign In"));
        this.items.add(new HomeScreenItem(BitmapFactory.decodeResource(getResources(), R.drawable.options), "Options"));
        this.items.add(new HomeScreenItem(BitmapFactory.decodeResource(getResources(), R.drawable.about), "About"));
        
        // gridView
        this.gridView = (GridView)this.findViewById(R.id.screen_home_gridview);
        this.gridView.setAdapter(new HomeScreenAdapter(this.items));
    }
	
	
	/* ===================== IScreen ========================*/
	public String getId(){
		return Screen.SCREEN_ID_HOME;
	}
	
	
	
	/* ===================== Adapter ========================*/
	
	private class HomeScreenItem
	{
		private Bitmap icon;
		private String text;
		
		private HomeScreenItem(Bitmap icon, String text)
		{
			this.icon = icon;
			this.text = text;
		}
	}
	
	private class HomeScreenAdapter extends BaseAdapter
	{
		private ArrayList<HomeScreenItem> items;
		
		private HomeScreenAdapter(ArrayList<HomeScreenItem> items)
		{
			this.items = items;
		}
		
		private boolean addItem(Bitmap icon, String text){
			boolean ret = this.items.add(new HomeScreenItem(icon, text));
			this.notifyDataSetChanged();
			return ret;
		}
		
		private boolean removeItem(HomeScreenItem item){
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
			
			if (view == null) {  // if it's not recycled, initialize some attributes
				view = getLayoutInflater().inflate(R.layout.screen_home_item, null);
			}
			
			if((item = this.items.get(position)) == null){
				return view;
			}
			
			ImageView iv = (ImageView)view.findViewById(R.id.screen_home_item_icon);
			//imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			iv.setImageBitmap(item.icon);
			TextView tv = (TextView)view.findViewById(R.id.screen_home_item_text);
			tv.setText(item.text);
						
			return view;
		}
	}
}
