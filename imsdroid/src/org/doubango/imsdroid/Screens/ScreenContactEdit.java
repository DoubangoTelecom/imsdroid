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
import org.doubango.imsdroid.Model.Group;
import org.doubango.imsdroid.Model.Group.Contact;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.Impl.ServiceManager;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ScreenContactEdit  extends Screen {
	
	private final IScreenService screenService;
	private final IContactService contactService;
	
	private static Group.Contact contact;
	private boolean editing;
	
	private TextView tvInfo;
	private ImageView ivAvatar;
	private EditText etAddress;
	private EditText etDisplayName;
	private Button btOK;
	private Button btCancel;
	
	public ScreenContactEdit() {
		super(SCREEN_TYPE.CONTACT_EDIT_T, ScreenContactEdit.class.getCanonicalName());
		
		this.screenService = ServiceManager.getScreenService();
		this.contactService = ServiceManager.getContactService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_contact_edit);
        
        // Get controls
        this.tvInfo = (TextView)this.findViewById(R.id.screen_contact_edit_textView_Info);
        this.ivAvatar = (ImageView)this.findViewById(R.id.screen_contact_edit_imageView_Avatar);
        this.etAddress = (EditText)this.findViewById(R.id.screen_contact_edit_editText_Address);
        this.etDisplayName = (EditText)this.findViewById(R.id.screen_contact_edit_editText_DisplayName);
        this.btOK = (Button)this.findViewById(R.id.screen_contact_edit_button_OK);
        this.btCancel = (Button)this.findViewById(R.id.screen_contact_edit_button_Cancel);
        
        
        this.btOK.setOnClickListener(this.btOK_OnClickListener);
        this.btCancel.setOnClickListener(this.btCancel_OnClickListener);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(ScreenContactEdit.contact != null){
			this.editing = true;
		}
		else{
			this.editing = false;
			ScreenContactEdit.contact = new Group.Contact("sip:", "");
		}
		
		this.tvInfo.setText(this.editing ? "Edit contact" : "Add new Contact");
		
		Bitmap avatar = ScreenContactEdit.contact.getAvatar();
		if(avatar!= null){
			this.ivAvatar.setImageBitmap(avatar);
		}
		String uri = ScreenContactEdit.contact.getUri();
		this.etAddress.setText(uri == null ? "sip:" : uri);
		
		String displayName = ScreenContactEdit.contact.getDisplayName();
		this.etDisplayName.setText(displayName == null ? "" : displayName);
	}
	
	private OnClickListener btOK_OnClickListener = new OnClickListener() {
		public void onClick(View v) {
			
			String group = "rcs";
			ScreenContactEdit.contact.setGroup(group);
			ScreenContactEdit.contact.setUri(ScreenContactEdit.this.etAddress.getText().toString());
			ScreenContactEdit.contact.setDisplayName(ScreenContactEdit.this.etDisplayName.getText().toString());
			
			if(ScreenContactEdit.this.editing){
				
			}
			else{
				ScreenContactEdit.this.contactService.addContact(ScreenContactEdit.contact);
			}
			ScreenContactEdit.this.screenService.back();
		}
	};
	
	private OnClickListener btCancel_OnClickListener = new OnClickListener() {
		public void onClick(View v) {
			ScreenContactEdit.this.screenService.back();
		}
	};
	
	public static void edit(Contact contact){
		if(contact == null){
			return;
		}
		
		ScreenContactEdit.contact = contact;
		ServiceManager.getScreenService().show(ScreenContactEdit.class);
	}
	
	public static void add(){
		ScreenContactEdit.contact = null;
		ServiceManager.getScreenService().show(ScreenContactEdit.class);
	}
}
