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

import org.doubango.imsdroid.CustomDialog;
import org.doubango.imsdroid.Engine;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.ngn.utils.NgnStringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public abstract class BaseScreen extends Activity implements IBaseScreen {
	private static final String TAG = BaseScreen.class.getCanonicalName();
	public static enum SCREEN_TYPE {
		// Well-Known
		ABOUT_T,
		AV_QUEUE_T,
		CHAT_T,
		CHAT_QUEUE_T,
		CODECS_T,
		CONTACTS_T,
		DIALER_T,
		FILETRANSFER_QUEUE_T,
		FILETRANSFER_VIEW_T,
		HOME_T,
		IDENTITY_T,
		INTERCEPT_CALL_T,
		GENERAL_T,
		MESSAGING_T,
		NATT_T,
		NETWORK_T,
		PRESENCE_T,
		QOS_T,
		SETTINGS_T,
		SECURITY_T,
		SPLASH_T,
		
		TAB_CONTACTS, 
		TAB_HISTORY_T, 
		TAB_INFO_T, 
		TAB_ONLINE,
		TAB_MESSAGES_T,
		
		
		// All others
		AV_T
	}
	
	protected String mId;
	protected final SCREEN_TYPE mType;
	protected boolean mComputeConfiguration;
	protected ProgressDialog mProgressDialog;
	protected Handler mHanler;
	
	protected final IScreenService mScreenService;

	protected BaseScreen(SCREEN_TYPE type, String id) {
		super();
		mType = type;
		mId = id;
		mScreenService = ((Engine)Engine.getInstance()).getScreenService();
	}

	protected Engine getEngine(){
		return (Engine)Engine.getInstance();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHanler = new Handler();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!processKeyDown(keyCode, event)) {
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public SCREEN_TYPE getType() {
		return mType;
	}

	@Override
	public boolean hasMenu() {
		return false;
	}

	@Override
	public boolean hasBack() {
		return false;
	}

	@Override
	public boolean back() {
		return mScreenService.back();
	}

	@Override
	public boolean createOptionsMenu(Menu menu) {
		return false;
	}

	protected void addConfigurationListener(RadioButton radioButton) {
		radioButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mComputeConfiguration = true;
			}
		});
	}

	protected void addConfigurationListener(EditText editText) {
		editText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mComputeConfiguration = true;
			}
		});
	}

	protected void addConfigurationListener(CheckBox checkBox) {
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mComputeConfiguration = true;
			}
		});
	}

	protected void addConfigurationListener(Spinner spinner) {
		// setOnItemClickListener not supported by Spinners
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mComputeConfiguration = true;
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	protected int getSpinnerIndex(String value, String[] values) {
		for (int i = 0; i < values.length; i++) {
			if (NgnStringUtils.equals(value, values[i], true)) {
				return i;
			}
		}
		return 0;
	}
	
	protected int getSpinnerIndex(int value, int[] values) {
		for (int i = 0; i < values.length; i++) {
			if (value == values[i]) {
				return i;
			}
		}
		return 0;
	}

	protected void showInProgress(String text, boolean bIndeterminate,
			boolean bCancelable) {
		synchronized (this) {
			if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(this);
				mProgressDialog.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						mProgressDialog = null;
					}
				});
				mProgressDialog.setMessage(text);
				mProgressDialog.setIndeterminate(bIndeterminate);
				mProgressDialog.setCancelable(bCancelable);
				mProgressDialog.show();
			}
		}
	}

	protected void cancelInProgress() {
		synchronized (this) {
			if (mProgressDialog != null) {
				mProgressDialog.cancel();
				mProgressDialog = null;
			}
		}
	}

	protected void cancelInProgressOnUiThread() {
		mHanler.post(new Runnable() {
			@Override
			public void run() {
				cancelInProgress();
			}
		});
	}

	protected void showInProgressOnUiThread(final String text,
			final boolean bIndeterminate, final boolean bCancelable) {
		mHanler.post(new Runnable() {
			@Override
			public void run() {
				showInProgress(text, bIndeterminate, bCancelable);
			}
		});
	}

	protected void showMsgBox(String title, String message) {
		final AlertDialog dialog = CustomDialog.create(this, R.drawable.icon, title, message, "OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}, null, null);
		dialog.show();
	}

	protected void showMsgBoxOnUiThread(final String title, final String message) {
		mHanler.post(new Runnable() {
			@Override
			public void run() {
				showMsgBox(title, message);
			}
		});
	}

    protected String getPath(Uri uri) {
    	try{
	        String[] projection = { MediaStore.Images.Media.DATA };
	        Cursor cursor = managedQuery(uri, projection, null, null, null);
	        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        cursor.moveToFirst();
	        final String path = cursor.getString(column_index);
	        cursor.close();
	        return path;
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }
    
	public static boolean processKeyDown(int keyCode, KeyEvent event) {
		final IScreenService screenService = ((Engine)Engine.getInstance()).getScreenService();
		final IBaseScreen currentScreen = screenService.getCurrentScreen();
		if (currentScreen != null) {
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0
					&& currentScreen.getType() != SCREEN_TYPE.HOME_T) {
				if (currentScreen.hasBack()) {
					if (!currentScreen.back()) {
						return false;
					}
				} else {
					screenService.back();
				}
				return true;
			}
			else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP){
				if(currentScreen.getType() == SCREEN_TYPE.AV_T){
					Log.d(TAG, "intercepting volume changed event");
					if(((ScreenAV)currentScreen).onVolumeChanged((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))){
						return true;
					}
				}
			}
			else if (keyCode == KeyEvent.KEYCODE_MENU
					&& event.getRepeatCount() == 0) {
				if (currentScreen instanceof Activity
						&& currentScreen.hasMenu()) {
					return false;
					// return ((Activity)currentScreen).onKeyDown(keyCode,
					// event);
				}
				/*
				 * if(!currentScreen.hasMenu()){
				 * screenService.show(ScreenHome.class); return true; } else
				 * if(currentScreen instanceof Activity){ return
				 * ((Activity)currentScreen).onKeyDown(keyCode, event); }
				 */
				return true;
			}
		}
		return false;
	}
}

