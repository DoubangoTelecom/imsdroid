package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Contact;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class ScreenContactView  extends Screen {

	private static Contact contact;
	
	private Button btContacts;
	private Button btEdit;
	private ImageView ivAvatar;
	private TextView tvDisplayname;
	private TextView tvUri;
	
	private final IScreenService screenService;
	
	public ScreenContactView() {
		super(SCREEN_TYPE.CONTACT_VIEW_T, ScreenContactView.class.getCanonicalName());
		
		this.screenService = ServiceManager.getScreenService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_contact_view);
        
        // Get controls
        this.btContacts = (Button)this.findViewById(R.id.screen_contact_view_button_contacts);
        this.btEdit = (Button)this.findViewById(R.id.screen_contact_view_button_edit);
        this.ivAvatar = (ImageView)this.findViewById(R.id.screen_contact_view_imageView_avatar);
        this.tvDisplayname = (TextView)this.findViewById(R.id.screen_contact_view_textView_displayname);
        this.tvUri = (TextView)this.findViewById(R.id.screen_contact_view_textView_uri);
        
        this.btContacts.setOnClickListener(this.btContacts_OnClickListener);
        this.btEdit.setOnClickListener(this.btEdit_OnClickListener);
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		/*synchronized (ScreenContactView.contact)*/ {
			if(ScreenContactView.contact == null){
				return;
			}
			
			if(ScreenContactView.contact.getAvatar() == null){
				this.ivAvatar.setImageResource(R.drawable.android_48);
			}
			else{
				this.ivAvatar.setImageBitmap(ScreenContactView.contact.getAvatar());
			}
			this.tvDisplayname.setText(ScreenContactView.contact.getDisplayName());
			this.tvUri.setText(ScreenContactView.contact.getUri());
		}
	}
	
	
	private OnClickListener btContacts_OnClickListener = new OnClickListener() {
		public void onClick(View v) {
			ScreenContactView.this.screenService.show(ScreenContacts.class);
		}
	};
	
	private OnClickListener btEdit_OnClickListener = new OnClickListener() {
		public void onClick(View v) {
		}
	};
	
	
	
	
	public static void show(Contact contact){
		if(contact == null){
			return;
		}
		/*synchronized (ScreenContactView.contact)*/ {
			ScreenContactView.contact = contact;
			ServiceManager.getScreenService().show(ScreenContactView.class);
		}
	}
}
