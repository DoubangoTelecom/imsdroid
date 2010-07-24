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
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.media.MediaType;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

public class ScreenDialer extends Screen {
	
	private EditText etAddress;
	private ImageButton ibAudioCall;
	private ImageButton ibVideoCall;
	private ImageButton ibChat;
	private ImageButton ibMessage;
	
	
	private final IScreenService screenService;
	
	public ScreenDialer() {
		super(SCREEN_TYPE.DIALER_T, ScreenDialer.class.getCanonicalName());
		
		this.screenService = ServiceManager.getScreenService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_dialer);
        
        // get controls
        this.etAddress = (EditText)this.findViewById(R.id.screen_dialer_editText_Address);
        this.ibAudioCall = (ImageButton)this.findViewById(R.id.screen_dialer_imageButton_Audio);
        this.ibVideoCall = (ImageButton)this.findViewById(R.id.screen_dialer_imageButton_Video);
        this.ibChat = (ImageButton)this.findViewById(R.id.screen_dialer_imageButton_Chat);
        this.ibMessage = (ImageButton)this.findViewById(R.id.screen_dialer_imageButton_IM);
        
        
        this.ibAudioCall.setOnClickListener(this.ibAudioCall_OnClickListener);
        this.ibVideoCall.setOnClickListener(this.ibVideoCall_OnClickListener);
        this.ibChat.setOnClickListener(this.ibChat_OnClickListener);
        this.ibMessage.setOnClickListener(this.ibMessage_OnClickListener);
	}
	
	
	
	private OnClickListener ibAudioCall_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			ScreenAV.makeCall(ScreenDialer.this.etAddress.getText().toString(), MediaType.Audio);
		}
	};
	
	private OnClickListener ibVideoCall_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			ScreenAV.makeCall(ScreenDialer.this.etAddress.getText().toString(), MediaType.AudioVideo);
		}
	};
	
	private OnClickListener ibChat_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
		}
	};
	
	private OnClickListener ibMessage_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			ScreenSMSCompose.sendSMS(ScreenDialer.this.etAddress.getText().toString());
		}
	};
	
}
