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
