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
import org.doubango.imsdroid.Model.HistorySMSEvent;
import org.doubango.imsdroid.Model.HistoryEvent.StatusType;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class ScreenSMSCompose  extends Screen {
	
	private static String remoteUri;
	
	private TextView tvInfo;
	private EditText etMessage;
	private Button btSend;
	private Button btCancel;
	
	public ScreenSMSCompose() {
		super(SCREEN_TYPE.SMS_COMPOSE_T, ScreenSMSCompose.class.getCanonicalName());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_smscomp);
        
        this.tvInfo = (TextView)this.findViewById(R.id.screen_smscomp_textView_info);
        this.etMessage = (EditText)this.findViewById(R.id.screen_smscomp_editText_Message);
        this.btSend = (Button)this.findViewById(R.id.screen_smscomp_button_send);
        this.btCancel = (Button)this.findViewById(R.id.screen_smscomp_button_cancel);
        
        this.btSend.setOnClickListener(this.btSend_OnClickListener);
        this.btCancel.setOnClickListener(this.btCancel_OnClickListener);
	}
	
	@Override
	protected void onResume() {
		Group.Contact contact = ServiceManager.getContactService().getContact(ScreenSMSCompose.remoteUri);
		if(contact == null || contact.getDisplayName() == null){
			this.tvInfo.setText(String.format("Sending SMS to %s", ScreenSMSCompose.remoteUri));
		}
		else{
			this.tvInfo.setText(String.format("Sending SMS to %s", contact.getDisplayName()));
		}
		
		super.onResume();
	}

	public static void sendSMS(String remoteUri){
		ScreenSMSCompose.remoteUri = remoteUri;
		ServiceManager.getScreenService().show(ScreenSMSCompose.class);
	}
	
	private OnClickListener btSend_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			String content = ScreenSMSCompose.this.etMessage.getText().toString();
			boolean success = ServiceManager.getSipService().sendSMS(content.getBytes(), ScreenSMSCompose.remoteUri,  "text/plain");
			HistorySMSEvent event = new HistorySMSEvent(ScreenSMSCompose.remoteUri);
			event.setStatus(success ? StatusType.Outgoing : StatusType.Failed);
			event.setContent(content);
			ServiceManager.getHistoryService().addEvent(event);
			ServiceManager.getScreenService().back();
			ScreenSMSCompose.this.etMessage.setText("");
			ServiceManager.vibrate(100);
		}
	};
	
	private OnClickListener btCancel_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			ServiceManager.getScreenService().back();
			ScreenSMSCompose.this.etMessage.setText("");
		}
	};
}
