package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.imsdroid.events.ISubscriptionEventHandler;
import org.doubango.imsdroid.events.SubscriptionEventArgs;
import org.doubango.imsdroid.utils.ContentType;
import org.doubango.imsdroid.utils.StringUtils;
import org.doubango.imsdroid.xml.reginfo.Contact;
import org.doubango.imsdroid.xml.reginfo.Reginfo;
import org.doubango.imsdroid.xml.reginfo.Registration;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.ProgressDialog;
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

public class ScreenRegistrations  extends Screen
implements ISubscriptionEventHandler
{
	private final static String TAG = ScreenRegistrations.class.getCanonicalName();
	
	private GridView gridView;
	private ScreenRegistrationsAdapter adapter;
	
	ProgressDialog progressDialog;
	private final Handler handler;
	private Reginfo reginfo;
	
	private final ISipService sipService;
	//private ProgressDialog progressDialog;
	
	public ScreenRegistrations() {
		super(SCREEN_TYPE.REGISTRATIONS_T);
		
		this.sipService = ServiceManager.getSipService();
		
		this.handler = new Handler();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_registrations);
        
		// add event handlers
        this.sipService.addSubscriptionEventHandler(this);
        
		// gridView
		this.adapter = new ScreenRegistrationsAdapter(null);
		this.gridView = (GridView) this.findViewById(R.id.screen_registrations_gridView);
		this.gridView.setAdapter(this.adapter);
		this.gridView.setOnItemClickListener(this.gridView_OnItemClickListener);
		
		// update registartion info
		if(this.sipService.getReginfo() != null){
			this.progressDialog = ProgressDialog.show(ScreenRegistrations.this, "Please wait...", "Loading registrations", true, true);
			this.progressDialog.setCanceledOnTouchOutside(true);
			new Thread(this.deserializeReginfo).start();
		}
	}

	@Override
	protected void onDestroy() { 
        // remove event handlers
        this.sipService.removeSubscriptionEventHandler(this);
        
        super.onDestroy();
	}
	
	private OnItemClickListener gridView_OnItemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		}
	};
	
	public boolean onSubscriptionEvent(Object sender, SubscriptionEventArgs e) {
		
		switch(e.getType()){
			case INCOMING_NOTIFY:
				// Can also get the sip service cached version
				final byte[] content = e.getContent();
				final String contentType = e.getContentType();
				if(content != null && StringUtils.equals(contentType, ContentType.REG_INFO, true)){
					if(this.reginfo == null){
						this.handler.post(new Runnable(){
							public void run() {
								ScreenRegistrations.this.progressDialog = ProgressDialog.show(ScreenRegistrations.this, "Please wait...", "Loading registrations", true, true);
								ScreenRegistrations.this.progressDialog.setCanceledOnTouchOutside(true);
							}
						});
					}
					// FIXME: new Thread() not needed (already in its own thread)
					new Thread(ScreenRegistrations.this.deserializeReginfo).start();
				}
				break;
				
			default:
				break;
		}
		return true;
	}
	
	private Runnable displayReginfo = new Runnable(){
		public void run() {
			ScreenRegistrations.this.adapter.update(ScreenRegistrations.this.reginfo);
			ScreenRegistrations.this.progressDialog.dismiss();
		}
	};
	
	private Runnable deserializeReginfo = new Runnable(){
		private Registration getRegistration(String regId, String contactId){
			if(ScreenRegistrations.this.reginfo == null || ScreenRegistrations.this.reginfo.getRegistration() == null){
				return null;
			}
			for(Registration registration : ScreenRegistrations.this.reginfo.getRegistration()){
				if(registration.getContact() == null){
					continue;
				}
				for(Contact contact : registration.getContact()){
					if(StringUtils.equals(registration.getId(), regId, false) && StringUtils.equals(contact.getId(), contactId, false)){
						return registration;
					}
				}
			}
			return null;
		}
		
		public void run() {
			final Serializer serializer = new Persister();
			final Reginfo newReginfo;
			Log.d(ScreenRegistrations.TAG, "start reginfo deserialization...");
			try {
				newReginfo = serializer.read(Reginfo.class, new String(ScreenRegistrations.this.sipService.getReginfo()));
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			
			
			if(ScreenRegistrations.this.reginfo == null || StringUtils.equals(newReginfo.getState(), "full", true)){
				ScreenRegistrations.this.reginfo = newReginfo;
			}
			else {
				for(Registration registration : newReginfo.getRegistration()){
					if(registration.getContact() == null){
						continue;
					}
					for(Contact contact : registration.getContact()){
						Registration oldRegistration = this.getRegistration(registration.getId(), contact.getId());
						if(oldRegistration == null){
							ScreenRegistrations.this.reginfo.getRegistration().add(registration);
						}
						else{
							oldRegistration.setState(registration.getState());
							Contact oldContact = oldRegistration.getContact().get(0);
							if(oldContact != null){
								oldContact.setEvent(contact.getEvent());
								oldContact.setExpires(contact.getExpires());
								oldContact.setState(contact.getState());
							}
						}
					}
				}
			}
			
			
			// display reginfo
			ScreenRegistrations.this.runOnUiThread(ScreenRegistrations.this.displayReginfo);
		}
	};
	
	/* ===================== Adapter ======================== */
	
	private class ScreenRegistrationsAdapter extends BaseAdapter {

		private Reginfo reginfo;
		private ScreenRegistrationsAdapter(Reginfo reginfo) {
			this.reginfo = reginfo;
		}

		private synchronized void update(Reginfo reginfo){
			this.reginfo = reginfo;
			this.notifyDataSetChanged();
		}
		
		public int getCount() {
			/*synchronized(this.reginfo)*/{
				return (this.reginfo == null || this.reginfo.getRegistration() == null) ? 0 
						: this.reginfo.getRegistration().size();
			}
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final Registration registartion;
			final Contact contact;
			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.screen_registrations_item, null);
			}
			if(this.reginfo == null || this.reginfo.getRegistration() == null){
				return view;
			}
			if ((registartion = this.reginfo.getRegistration().get(position)) == null ||
					(registartion.getContact() == null) || (contact = registartion.getContact().get(0)) == null) {
				return view;
			}

			ImageView ivRegState = (ImageView) view.findViewById(R.id.screen_registrations_item_imageView_regstate);
			TextView tvRegAoR = (TextView) view.findViewById(R.id.screen_registrations_item_textView_regaor);
			TextView tvContactUri = (TextView) view.findViewById(R.id.screen_registrations_item_textView_contacturi);
			TextView tvContactState = (TextView) view.findViewById(R.id.screen_registrations_item_textView_contactstate);
			TextView tvContactEvent = (TextView) view.findViewById(R.id.screen_registrations_item_textView_contacteven);
			
			// Registration
			if(registartion.getState().equals("active")){
				ivRegState.setImageResource(R.drawable.bullet_ball_glass_green_16);
			}
			else if(registartion.getState().equals("terminated")){
				ivRegState.setImageResource(R.drawable.bullet_ball_glass_red_16);
			}
			else {
				ivRegState.setImageResource(R.drawable.bullet_ball_glass_grey_16);
			}
			tvRegAoR.setText(registartion.getAor());
			
			// Contact
			tvContactUri.setText(contact.getUri());
			tvContactState.setText(String.format("State: %s", contact.getState()));
			tvContactEvent.setText(String.format("Event: %s", contact.getEvent()));

			return view;
		}
	}
}
