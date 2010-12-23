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

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.media.MediaType;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

public class ScreenDialer extends Screen {
	
	private EditText etAddress;
	private ImageButton ibAudioCall;
	private ImageButton ibVideoCall;
	private ImageButton ibShare;
	private ImageButton ibMessage;
	private ImageButton ibContact;
	
	private static final int SELECT_CONTENT = 1;
	private static final int SELECT_CONTACT = 2;
	
	private String fixmeRemoteParty;
	
	public ScreenDialer() {
		super(SCREEN_TYPE.DIALER_T, ScreenDialer.class.getCanonicalName());
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_dialer);
        
        // get controls
        this.etAddress = (EditText)this.findViewById(R.id.screen_dialer_editText_Address);
        this.ibAudioCall = (ImageButton)this.findViewById(R.id.screen_dialer_imageButton_Audio);
        this.ibVideoCall = (ImageButton)this.findViewById(R.id.screen_dialer_imageButton_Video);
        this.ibShare = (ImageButton)this.findViewById(R.id.screen_dialer_imageButton_share);
        this.ibMessage = (ImageButton)this.findViewById(R.id.screen_dialer_imageButton_IM);
        this.ibContact = (ImageButton)this.findViewById(R.id.screen_dialer_imageButton_contact);
        
        
        this.ibAudioCall.setOnClickListener(this.ibAudioCall_OnClickListener);
        this.ibVideoCall.setOnClickListener(this.ibVideoCall_OnClickListener);
        this.ibShare.setOnClickListener(this.ibShare_OnClickListener);
        this.ibMessage.setOnClickListener(this.ibMessage_OnClickListener);
        this.ibContact.setOnClickListener(this.ibContact_OnClickListener);
	}
	
	
	
	private OnClickListener ibAudioCall_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			ScreenAV.makeCall(ScreenDialer.this.etAddress.getText().toString().trim(), MediaType.Audio);
		}
	};
	
	private OnClickListener ibVideoCall_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			ScreenAV.makeCall(ScreenDialer.this.etAddress.getText().toString().trim(), MediaType.AudioVideo);
		}
	};
	
	private OnClickListener ibShare_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setType("*/*")
			.addCategory(Intent.CATEGORY_OPENABLE)
			.setAction(Intent.ACTION_GET_CONTENT);
			//.addCategory(Intent.CATEGORY_OPENABLE);
			//.putExtra("remoteUri", ScreenDialer.this.etAddress.getText().toString().trim());
			ScreenDialer.this.fixmeRemoteParty = ScreenDialer.this.etAddress.getText().toString().trim();

			startActivityForResult(Intent.createChooser(intent, "Select content"), ScreenDialer.SELECT_CONTENT);		
		}
	};
	
	private OnClickListener ibMessage_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			ScreenSMSCompose.sendSMS(ScreenDialer.this.etAddress.getText().toString().trim());
		}
	};
	
	private OnClickListener ibContact_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			try{
				if (IMSDroid.getSDKVersion()<5) {
					startActivityForResult(new Intent(Intent.ACTION_PICK, People.CONTENT_URI), 
							ScreenDialer.SELECT_CONTACT);
				}
				else{
					startActivityForResult(new  Intent(Intent.ACTION_PICK, Uri.parse("content://contacts/people/")), 
							ScreenDialer.SELECT_CONTACT);
				}
			}
			catch(Exception e){
				Log.e("ScreenDialer", e.toString());
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			switch(requestCode){
				case ScreenDialer.SELECT_CONTENT:
					if (this.fixmeRemoteParty != null) {
			            Uri selectedContentUri = data.getData();
			            String selectedContentPath = this.getPath(selectedContentUri);
			            ScreenFileTransferView.ShareContent(this.fixmeRemoteParty, selectedContentPath, true);
			        }
					break;
					
				case ScreenDialer.SELECT_CONTACT:
					Uri contactData = data.getData();
			        Cursor cursor =  managedQuery(contactData, null, null, null, null);
			        if (cursor.moveToFirst()) {			        	
			          String phoneId = cursor.getString(cursor.getColumnIndexOrThrow(People.NUMBER));
			          if(phoneId != null){
			        	  this.etAddress.setText(phoneId);
			          }
			        }
					break;
			}
		}
	}
}
