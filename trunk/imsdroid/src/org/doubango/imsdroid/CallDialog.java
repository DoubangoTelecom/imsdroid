package org.doubango.imsdroid;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class CallDialog {

	public static void show(Context context, String remoteParty, OnClickListener pickListener, OnClickListener hangListener){
		View layout = CallDialog.getView(context, remoteParty, pickListener, hangListener);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(layout).show();
	}
	
	public static View getView(Context context, String remoteParty, OnClickListener pickListener, OnClickListener hangListener){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.call_dialog, null);
		
		ImageButton ibPick = (ImageButton) layout.findViewById(R.id.call_dialog_imageButton_pick);
		ImageButton ibHang = (ImageButton) layout.findViewById(R.id.call_dialog_imageButton_hang);
		TextView tvRemoteParty = (TextView) layout.findViewById(R.id.call_dialog_textView_remote_party);
		
		if(remoteParty != null){
			tvRemoteParty.setText(remoteParty);
		}
		
		if(pickListener != null){
			ibPick.setOnClickListener(pickListener);
		}
		else{
			ibPick.setVisibility(View.INVISIBLE);
		}
		if(hangListener != null){
			ibHang.setOnClickListener(hangListener);
		}
		else{
			ibHang.setVisibility(View.INVISIBLE);
		}
		
		return layout;
	}
}
