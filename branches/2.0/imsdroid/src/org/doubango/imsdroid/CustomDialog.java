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
package org.doubango.imsdroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomDialog {
	
	public static void show(Context context, int icon, String title, String msg, String positiveText, DialogInterface.OnClickListener positive, String negativeText, DialogInterface.OnClickListener negative){
		AlertDialog.Builder builder;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.custom_dialog, null);

		ImageView ivIcon = (ImageView) layout.findViewById(R.id.custom_dialog_imageView_icon);
		ivIcon.setImageResource(icon);
		TextView tvTitle = (TextView) layout.findViewById(R.id.custom_dialog_textView_title);
		tvTitle.setText((title == null) ? "" : title);
		TextView tvMsg = (TextView) layout.findViewById(R.id.custom_dialog_textView_msg);
		tvMsg.setText(msg);

		builder = new AlertDialog.Builder(context);
		builder.setView(layout);
		if(positive != null && positiveText != null){
			builder.setPositiveButton(positiveText, positive);
		}
		if(negative != null && negativeText != null){
			builder.setNegativeButton(negativeText, negative);
		}
		
		builder.create().show();
	}
}
