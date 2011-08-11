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

import java.nio.ByteBuffer;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Group;
import org.doubango.imsdroid.Model.HistorySMSEvent;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Model.HistoryEvent.StatusType;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.Services.Impl.SipService;
import org.doubango.imsdroid.sip.MySipStack;
import org.doubango.imsdroid.utils.ContentType;
import org.doubango.imsdroid.utils.StringUtils;
import org.doubango.imsdroid.utils.UriUtils;
import org.doubango.tinyWRAP.MessagingSession;
import org.doubango.tinyWRAP.RPMessage;
import org.doubango.tinyWRAP.SMSEncoder;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class ScreenSMSCompose  extends Screen {
	
	private static String remoteUri;
	private static int SMS_MR = 0;
	
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
			boolean success = ScreenSMSCompose.this.sendSMS(content, ScreenSMSCompose.remoteUri);
			
			HistorySMSEvent event = new HistorySMSEvent(ScreenSMSCompose.remoteUri);
			event.setStatus(success ? StatusType.Outgoing : StatusType.Failed);
			event.setContent(content);
			ServiceManager.getHistoryService().addEvent(event);
			ServiceManager.getScreenService().back();
			ScreenSMSCompose.this.etMessage.setText("");
			ServiceManager.vibrate(100);
			ServiceManager.getScreenService().show(ScreenHome.class);
		}
	};
	
	private OnClickListener btCancel_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			ServiceManager.getScreenService().back();
			ScreenSMSCompose.this.etMessage.setText("");
		}
	};
	
	private boolean sendSMS(String text, String remoteUri){
		
		boolean success = false;
		if(StringUtils.isNullOrEmpty(text)){
			return false;
		}
		
		byte[] content = text.getBytes();
		
		final MySipStack sipStack = ServiceManager.getSipService().getStack();
		
		final MessagingSession session = new MessagingSession(sipStack);
		final boolean binarySMS = ServiceManager.getConfigurationService().getBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.BINARY_SMS, Configuration.DEFAULT_RCS_BINARY_SMS);
		final String SMSC = ServiceManager.getConfigurationService().getString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.SMSC, Configuration.DEFAULT_RCS_SMSC);
		final String SMSCPhoneNumber;
		final String dstPhoneNumber;
		
		
		if(sipStack.getSigCompId() != null){
			session.addSigCompCompartment(sipStack.getSigCompId());
		}
		
		if(binarySMS && (SMSCPhoneNumber = UriUtils.getValidPhoneNumber(SMSC)) != null && (dstPhoneNumber = UriUtils.getValidPhoneNumber(remoteUri)) != null){
			session.setToUri(SMSC);
			session.addHeader("Content-Type", ContentType.SMS_3GPP);
			session.addHeader("Content-Transfer-Encoding", "binary");
			session.addCaps("+g.3gpp.smsip");
			
			RPMessage rpMessage;
			if(ServiceManager.getConfigurationService().getBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.HACK_SMS, false)){
				rpMessage = SMSEncoder.encodeDeliver(++ScreenSMSCompose.SMS_MR, SMSCPhoneNumber, dstPhoneNumber, new String(content));
				session.addHeader("P-Asserted-Identity", SMSC);
			}
			else{
				rpMessage = SMSEncoder.encodeSubmit(++ScreenSMSCompose.SMS_MR, SMSCPhoneNumber, dstPhoneNumber, new String(content));
			}
			
			long payloadLength = rpMessage.getPayloadLength();
			final ByteBuffer payload = ByteBuffer.allocateDirect((int)payloadLength);
			payloadLength = rpMessage.getPayload(payload, payload.capacity());
			success = session.send(payload, payloadLength);
			rpMessage.delete();
			
			if(ScreenSMSCompose.SMS_MR >= 255){
				ScreenSMSCompose.SMS_MR = 0;
			}
		}
		else{
			remoteUri = UriUtils.makeValidSipUri(remoteUri);
			session.setToUri(remoteUri);
			session.addHeader("Content-Type", "text/plain");
			
			final ByteBuffer payload = ByteBuffer.allocateDirect(content.length);
			payload.put(content);
			success = session.send(payload, content.length);
		}
		session.delete();
		
		return success;
	}
}
