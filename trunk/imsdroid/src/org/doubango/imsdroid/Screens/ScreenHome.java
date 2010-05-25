package org.doubango.imsdroid.Screens;

import java.util.ArrayList;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.RegistrationEventArgs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ScreenHome extends Screen 
implements IRegistrationEventHandler
{

	/* ===================== Activity ======================== */

	private GridView gridView;
	private ArrayList<ScreenHomeItem> items;
	private static int itemSignInOutPosition = 0;
	private static int itemExitPosition = 1;
	private final IScreenService screenService;
	private final ISipService sipService;
	private final Handler handler;
	private ScreenHomeAdapter adapter;

	public ScreenHome() {
		super(SCREEN_TYPE.HOME_T);

		// Services
		this.screenService = ServiceManager.getScreenService();
		this.sipService = ServiceManager.getSipService();
		
		this.handler = new Handler();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_home);

		// default items
		this.items = new ArrayList<ScreenHomeItem>();

		this.items.add(new ScreenHomeItem(BitmapFactory.decodeResource(
				getResources(), R.drawable.sign_in_48), "Sign In", null));
		this.items.add(new ScreenHomeItem(BitmapFactory.decodeResource(
				getResources(), R.drawable.exit_48), "Exit/Quit", null));
		
		this.items.add(new ScreenHomeItem(BitmapFactory.decodeResource(
				getResources(), R.drawable.options_48), "Options", Screen.SCREEN_ID.OPTIONS_I.toString()));
		this.items.add(new ScreenHomeItem(BitmapFactory.decodeResource(
				getResources(), R.drawable.about_48), "About", Screen.SCREEN_ID.ABOUT_I.toString()));

		// gridView
		this.adapter = new ScreenHomeAdapter(this.items);
		this.gridView = (GridView) this.findViewById(R.id.screen_home_gridview);
		this.gridView.setAdapter(this.adapter);
		this.gridView_setOnItemClickListener();
		
		// add event handlers
        this.sipService.addRegistrationEventHandler(this);
	}

	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("SignInOutEnabled", this.items.get(ScreenHome.itemSignInOutPosition).enabled);
		
		super.onSaveInstanceState(outState);
	}
	
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		if(this.sipService.isRegistered()){
			this.adapter.updateItem(ScreenHome.itemSignInOutPosition, BitmapFactory.decodeResource(
							getResources(), R.drawable.sign_out_48), "Sign Out", null);
		}
		else{
			this.adapter.updateItem(ScreenHome.itemSignInOutPosition, BitmapFactory.decodeResource(
					getResources(), R.drawable.sign_in_48), "Sign In", null);
		}
		this.adapter.setItemEnabled(ScreenHome.itemSignInOutPosition, savedInstanceState.getBoolean("SignInOutEnabled"));
	}
	
	
	protected void onDestroy() { 
        // remove event handlers
        this.sipService.removeRegistrationEventHandler(this);
        
        super.onDestroy();
	}
	
	/* ===================== IScreen ======================== */
	

	/* ===================== UI Events ======================== */
	private void gridView_setOnItemClickListener() {
		this.gridView.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ScreenHomeItem item = ScreenHome.this.items.get(position);
				if(item != null){
					if(position == ScreenHome.itemSignInOutPosition){
						if(ScreenHome.this.sipService.isRegistered()){
							ScreenHome.this.sipService.unregister();
						}
						else{
							ScreenHome.this.sipService.register();
						}
					}
					else if(position == ScreenHome.itemExitPosition){
						AlertDialog.Builder builder = new AlertDialog.Builder(ScreenHome.this);
						builder.setMessage("Are you sure you want to exit?")
						       .setCancelable(false)
						       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						           public void onClick(DialogInterface dialog, int id) {
						                ServiceManager.getMainActivity().finish();
						           }
						       })
						       .setNegativeButton("No", new DialogInterface.OnClickListener() {
						           public void onClick(DialogInterface dialog, int id) {
						                dialog.cancel();
						           }
						       });
						builder.create().show();
					}
					else{
						ScreenHome.this.screenService.show(item.screenId);
					}
				}
			}
		});
	}

	/* ===================== Sip Events ========================*/
	public boolean onRegistrationEvent(Object sender, RegistrationEventArgs e) {
		
		Log.i(this.getClass().getName(), "onRegistrationEvent");
		
		switch(e.getType()){
			case REGISTRATION_OK:
			case UNREGISTRATION_OK:
				this.handler.post(new Runnable() {
					public void run() {
						if(ScreenHome.this.sipService.isRegistered()){
							ScreenHome.this.adapter.updateItem(ScreenHome.itemSignInOutPosition, BitmapFactory.decodeResource(
											getResources(), R.drawable.sign_out_48), "Sign Out", null);
						}
						else{
							ScreenHome.this.adapter.updateItem(ScreenHome.itemSignInOutPosition, BitmapFactory.decodeResource(
									getResources(), R.drawable.sign_in_48), "Sign In", null);
						}
						ScreenHome.this.adapter.setItemEnabled(ScreenHome.itemSignInOutPosition, true);
					}});
				break;
				
			case REGISTRATION_INPROGRESS:
			case UNREGISTRATION_INPROGRESS:
				this.handler.post(new Runnable() {
					public void run() {
						ScreenHome.this.adapter.setItemEnabled(ScreenHome.itemSignInOutPosition, false);
				}});
				break;
				
			case REGISTRATION_NOK:
			case UNREGISTRATION_NOK:
			default:
				break;
			
		}
		return true;
	}
	
	
	
	/* ===================== Adapter ======================== */

	private class ScreenHomeItem {
		private Bitmap icon;
		private String text;
		private String screenId;
		private boolean enabled;

		private ScreenHomeItem(Bitmap icon, String text, String screenId) {
			this.icon = icon;
			this.text = text;
			this.screenId = screenId;
		}
	}

	private class ScreenHomeAdapter extends BaseAdapter {
		private ArrayList<ScreenHomeItem> items;

		private ScreenHomeAdapter(ArrayList<ScreenHomeItem> items) {
			this.items = items;
		}

		private boolean setItemEnabled(int location, boolean enabled) {
			if(this.items.size()>location){
				this.items.get(location).enabled = enabled;
				this.notifyDataSetChanged();
				return true;
			}
			return false;
		}
		
		private boolean updateItem(int location, Bitmap icon, String text, String screenId) {
			if(this.items.size()>location){
				this.items.set(location, new ScreenHomeItem(icon, text, screenId));
				this.notifyDataSetChanged();
				return true;
			}
			return false;
		}
		
		private boolean addItem(Bitmap icon, String text, String screenId) {
			boolean ret = this.items.add(new ScreenHomeItem(icon, text, screenId));
			this.notifyDataSetChanged();
			return ret;
		}

		private boolean removeItem(ScreenHomeItem item) {
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
			ScreenHomeItem item;

			if (view == null) { // if it's not recycled, initialize some
								// attributes
				view = getLayoutInflater().inflate(R.layout.screen_home_item,
						null);
			}

			if ((item = this.items.get(position)) == null) {
				return view;
			}

			ImageView iv = (ImageView) view.findViewById(R.id.screen_home_item_icon);
			// imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			iv.setImageBitmap(item.icon);
			TextView tv = (TextView) view.findViewById(R.id.screen_home_item_text);
			tv.setText(item.text);
			
			view.setEnabled(item.enabled);

			return view;
		}
	}
}
