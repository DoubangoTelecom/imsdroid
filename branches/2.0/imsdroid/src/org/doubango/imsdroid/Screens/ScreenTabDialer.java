package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Utils.DialerUtils;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnStringUtils;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public class ScreenTabDialer  extends BaseScreen {
	private static String TAG = ScreenTabDialer.class.getCanonicalName();
	
	private EditText mEtNumber;
	
	private final INgnSipService mSipService;
	
	public ScreenTabDialer() {
		super(SCREEN_TYPE.HOME_T, TAG);
		
		mSipService = getEngine().getSipService();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_dialer);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		mEtNumber = (EditText)findViewById(R.id.screen_tab_dialer_editText_number);
		
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
		
		DialerUtils.setDialerImageButton(this, R.id.screen_tab_dialer_button_chat, R.drawable.sym_action_chat_48, DialerUtils.TAG_CHAT, mOnDialerClick);
		DialerUtils.setDialerImageButton(this, R.id.screen_tab_dialer_button_call, R.drawable.ic_menu_call_48, DialerUtils.TAG_AUDIO_CALL, mOnDialerClick);
		DialerUtils.setDialerImageButton(this, R.id.screen_tab_dialer_button_del, R.drawable.ic_input_delete_48, DialerUtils.TAG_DELETE, mOnDialerClick);
		
		mEtNumber.setInputType(InputType.TYPE_NULL);
		mEtNumber.setFocusable(false);
		mEtNumber.setFocusableInTouchMode(false);
		mEtNumber.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				final boolean bShowCaret = mEtNumber.getText().toString().length() > 0;
				mEtNumber.setFocusableInTouchMode(bShowCaret);
				mEtNumber.setFocusable(bShowCaret);
			}
        });
		
		findViewById(R.id.screen_tab_dialer_button_0).setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				appendText("+");
				return true;
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
			else{
				final String textToAppend = tag == DialerUtils.TAG_STAR ? "*" : (tag == DialerUtils.TAG_SHARP ? "#" : Integer.toString(tag));
				appendText(textToAppend);
			}
		}
	};
	
	private void appendText(String textToAppend){
		final int selStart = mEtNumber.getSelectionStart();
		final StringBuffer sb = new StringBuffer(mEtNumber.getText().toString());
		sb.insert(selStart, textToAppend);
		mEtNumber.setText(sb.toString());
		mEtNumber.setSelection(selStart+1);
	}
}

