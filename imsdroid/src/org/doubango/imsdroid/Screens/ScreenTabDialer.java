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
import org.doubango.imsdroid.Utils.DialerUtils;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnStringUtils;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

public class ScreenTabDialer  extends BaseScreen {
	private static String TAG = ScreenTabDialer.class.getCanonicalName();
	
	private EditText mEtNumber;
	private ImageButton mIbInputType;
	
	static enum PhoneInputType{
		Numbers,
		Text
	}
	
	private PhoneInputType mInputType;
	private InputMethodManager mInputMethodManager;
	
	private final INgnSipService mSipService;
	
	public ScreenTabDialer() {
		super(SCREEN_TYPE.DIALER_T, TAG);
		
		mSipService = getEngine().getSipService();
		
		mInputType = PhoneInputType.Numbers;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_dialer);
		
		mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		
		mEtNumber = (EditText)findViewById(R.id.screen_tab_dialer_editText_number);
		mIbInputType = (ImageButton) findViewById(R.id.screen_tab_dialer_imageButton_input_type);
		
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_0, "0", "+", DialerUtils.TAG_0, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_1, "1", "", DialerUtils.TAG_1, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_2, "2", "ABC", DialerUtils.TAG_2, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_3, "3", "DEF", DialerUtils.TAG_3, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_4, "4", "GHI", DialerUtils.TAG_4, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_5, "5", "JKL", DialerUtils.TAG_5, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_6, "6", "MNO", DialerUtils.TAG_6, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_7, "7", "PQRS", DialerUtils.TAG_7, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_8, "8", "TUV", DialerUtils.TAG_8, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_9, "9", "WXYZ", DialerUtils.TAG_9, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_star, "*", "", DialerUtils.TAG_STAR, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_sharp, "#", "", DialerUtils.TAG_SHARP, mOnDialerClick);
		
		DialerUtils.setDialerImageButton(this, R.id.screen_tab_dialer_button_audio, R.drawable.voice_call_48, DialerUtils.TAG_AUDIO_CALL, mOnDialerClick);
		DialerUtils.setDialerImageButton(this, R.id.screen_tab_dialer_button_video, R.drawable.visio_call_48, DialerUtils.TAG_VIDEO_CALL, mOnDialerClick);
		DialerUtils.setDialerImageButton(this, R.id.screen_tab_dialer_button_del, R.drawable.ic_input_delete_48, DialerUtils.TAG_DELETE, mOnDialerClick);
		
		mEtNumber.setInputType(InputType.TYPE_NULL);
		mEtNumber.setFocusable(false);
		mEtNumber.setFocusableInTouchMode(false);
		mEtNumber.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(mInputType == PhoneInputType.Numbers){
					final boolean bShowCaret = mEtNumber.getText().toString().length() > 0;
					mEtNumber.setFocusableInTouchMode(bShowCaret);
					mEtNumber.setFocusable(bShowCaret);
				}
			}
        });
		
		findViewById(R.id.screen_tab_dialer_button_0).setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				appendText("+");
				return true;
			}
		});
		
		mIbInputType.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(mInputType){
					case Numbers:
						mInputType = PhoneInputType.Text;
						mIbInputType.setImageResource(R.drawable.input_text);
						mEtNumber.setInputType(InputType.TYPE_CLASS_TEXT);
						
						mEtNumber.setFocusableInTouchMode(true);
						mEtNumber.setFocusable(true);
						mInputMethodManager.showSoftInput(mEtNumber, 0);
						break;
						
					case Text:
					default:
						mInputType = PhoneInputType.Numbers;
						mIbInputType.setImageResource(R.drawable.input_numbers);
						mEtNumber.setInputType(InputType.TYPE_NULL);
						
						final boolean bShowCaret = mEtNumber.getText().toString().length() > 0;
						mEtNumber.setFocusableInTouchMode(bShowCaret);
						mEtNumber.setFocusable(bShowCaret);
						mInputMethodManager.hideSoftInputFromWindow(mEtNumber.getWindowToken(), 0);
						break;
				}
			}
		});
	}
	
	@Override
	protected void onDestroy() {
       super.onDestroy();
	}
	
	private final View.OnClickListener mOnDialerClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int tag = Integer.parseInt(v.getTag().toString());
			final String number = mEtNumber.getText().toString();
			if(tag == DialerUtils.TAG_CHAT){
				if(mSipService.isRegistered() && !NgnStringUtils.isNullOrEmpty(number)){
					// ScreenChat.startChat(number);
					mEtNumber.setText(NgnStringUtils.emptyValue());
				}
			}
			else if(tag == DialerUtils.TAG_DELETE){
				final int selStart = mEtNumber.getSelectionStart();
				if(selStart >0){
					final StringBuffer sb = new StringBuffer(number);
					sb.delete(selStart-1, selStart);
					mEtNumber.setText(sb.toString());
					mEtNumber.setSelection(selStart-1);
				}
			}
			else if(tag == DialerUtils.TAG_AUDIO_CALL){
				if(mSipService.isRegistered() && !NgnStringUtils.isNullOrEmpty(number)){
					ScreenAV.makeCall(number, NgnMediaType.Audio);
					mEtNumber.setText(NgnStringUtils.emptyValue());
				}
			}
			else if(tag == DialerUtils.TAG_VIDEO_CALL){
				if(mSipService.isRegistered() && !NgnStringUtils.isNullOrEmpty(number)){
					ScreenAV.makeCall(number, NgnMediaType.AudioVideo);
					mEtNumber.setText(NgnStringUtils.emptyValue());
				}
			}
			else{
				final String textToAppend = tag == DialerUtils.TAG_STAR ? "*" : (tag == DialerUtils.TAG_SHARP ? "#" : Integer.toString(tag));
				appendText(textToAppend);
			}
		}
	};
	
	@Override
	public boolean hasBack(){
		return true;
	}
	
	@Override
	public boolean back(){
		boolean ret = mScreenService.show(ScreenHome.class);
		if(ret){
			mScreenService.destroy(getId());
		}
		return ret;
	}
	
	private void appendText(String textToAppend){
		final int selStart = mEtNumber.getSelectionStart();
		final StringBuffer sb = new StringBuffer(mEtNumber.getText().toString());
		sb.insert(selStart, textToAppend);
		mEtNumber.setText(sb.toString());
		mEtNumber.setSelection(selStart+1);
	}
}

