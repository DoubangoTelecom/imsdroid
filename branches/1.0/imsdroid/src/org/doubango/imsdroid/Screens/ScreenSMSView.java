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
import org.doubango.imsdroid.Model.HistorySMSEvent;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.utils.UriUtils;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ScreenSMSView  extends Screen{

	private Button btOK;
	private TextView tvInfo;
	private TextView tvMessage;
	
	private static HistorySMSEvent event;
	
	public ScreenSMSView() {
		super(SCREEN_TYPE.SMS_VIEW_T, ScreenSMSCompose.class.getCanonicalName());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_smsview);
        
        this.btOK = (Button)this.findViewById(R.id.screen_smsview_button_ok);
        this.tvInfo = (TextView)this.findViewById(R.id.screen_smsview_textView_info);
        this.tvMessage = (TextView)this.findViewById(R.id.screen_smsview_textView_message);
        
        
        this.btOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ServiceManager.getScreenService().back();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(ScreenSMSView.event != null){
			String remoteParty = UriUtils.getDisplayName(ScreenSMSView.event.getRemoteParty());
			String content = ScreenSMSView.event.getContent();
			this.tvInfo.setText(String.format("SMS from %s", remoteParty==null ? "unknown" : remoteParty));
			this.tvMessage.setText(content==null?"":content);
		}
	}

	public static void showSMS(HistorySMSEvent event){
		ScreenSMSView.event = event;
		
		ServiceManager.getScreenService().show(ScreenSMSView.class);
	}
}
