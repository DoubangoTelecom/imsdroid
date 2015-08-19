/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
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
*/
package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.ngn.media.NgnMediaType;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class ScreenInterceptCall extends BaseScreen{
	private final static String TAG = ScreenInterceptCall.class.getCanonicalName();
	
	public ScreenInterceptCall() {
		super(SCREEN_TYPE.INTERCEPT_CALL_T, TAG);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Intent i = getIntent();
		final Uri uri = i.getData();
		final String items[] = { getString(R.string.app_name), "Mobile" };
		Log.d(TAG,"Intent Uri "+uri.toString());
		
		final String phone = uri.getSchemeSpecificPart();
//		if (uri.getScheme().contains("tel")) {
//			getIntent().setData(uri);
//			return;
//		}
		
		new AlertDialog.Builder(this)
		.setTitle("Calling "+phone)
        .setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	switch (whichButton) {
                	case 0:
                		ScreenAV.makeCall(phone.trim(), NgnMediaType.Audio);
            			finish();
                		break;
                	case 1:
                		/*
                		//Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", Uri.decode(phone)+"+",null));
                		//Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", Uri.decode(phone),null));
                		Intent intent = new Intent(Intent.ACTION_DIAL);
                		intent.setData(Uri.parse("tel:" + "123"));
                		//intent.setData(Uri.fromParts("tel", phone, null));
        		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		        startActivity(intent);*/
                		startNextMatchingActivity(i);
            			finish();
            			break;
                	}
                }
            })
		.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		})
		.show();

	}
}
