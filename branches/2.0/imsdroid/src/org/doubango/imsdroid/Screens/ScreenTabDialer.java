package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Media.MediaType;
import org.doubango.imsdroid.Services.ISipService;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ScreenTabDialer  extends BaseScreen {
	private static String TAG = ScreenTabDialer.class.getCanonicalName();
	
	private EditText mEtNumber;
	
	private Button mBt0;
	private Button mBt1;
	private Button mBt2;
	private Button mBt3;
	private Button mBt4;
	private Button mBt5;
	private Button mBt6;
	private Button mBt7;
	private Button mBt8;
	private Button mBt9;
	private Button mBtStar;
	private Button mBtSharp;
	private Button mBtChat;
	private Button mBtCall;
	private Button mBtDel;
	
	private final ISipService mSipService;
	
	public ScreenTabDialer() {
		super(SCREEN_TYPE.HOME_T, TAG);
		
		mSipService = ServiceManager.getSipService();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_dialer);
		
		mEtNumber = (EditText)findViewById(R.id.screen_tab_dialer_editText_number);
		
		mBt0 = (Button)findViewById(R.id.screen_tab_dialer_button_0);
		mBt1 = (Button)findViewById(R.id.screen_tab_dialer_button_1);
		mBt2 = (Button)findViewById(R.id.screen_tab_dialer_button_2);
		mBt3 = (Button)findViewById(R.id.screen_tab_dialer_button_3);
		mBt4 = (Button)findViewById(R.id.screen_tab_dialer_button_4);
		mBt5 = (Button)findViewById(R.id.screen_tab_dialer_button_5);
		mBt6 = (Button)findViewById(R.id.screen_tab_dialer_button_6);
		mBt7 = (Button)findViewById(R.id.screen_tab_dialer_button_7);
		mBt8 = (Button)findViewById(R.id.screen_tab_dialer_button_8);
		mBt9 = (Button)findViewById(R.id.screen_tab_dialer_button_9);
		mBtStar = (Button)findViewById(R.id.screen_tab_dialer_button_star);
		mBtSharp = (Button)findViewById(R.id.screen_tab_dialer_button_sharp);
		mBtDel = (Button)findViewById(R.id.screen_tab_dialer_button_del);
		mBtCall = (Button)findViewById(R.id.screen_tab_dialer_button_call);
		mBtChat = (Button)findViewById(R.id.screen_tab_dialer_button_chat);
		
		mEtNumber.setInputType(InputType.TYPE_NULL);
		
		mBt0.setOnClickListener(mOnDialerClick);
		mBt1.setOnClickListener(mOnDialerClick);
		mBt2.setOnClickListener(mOnDialerClick);
		mBt3.setOnClickListener(mOnDialerClick);
		mBt4.setOnClickListener(mOnDialerClick);
		mBt5.setOnClickListener(mOnDialerClick);
		mBt6.setOnClickListener(mOnDialerClick);
		mBt7.setOnClickListener(mOnDialerClick);
		mBt8.setOnClickListener(mOnDialerClick);
		mBt9.setOnClickListener(mOnDialerClick);
		mBtStar.setOnClickListener(mOnDialerClick);
		mBtSharp.setOnClickListener(mOnDialerClick);
		mBtDel.setOnClickListener(mOnDialerClick);
		mBtCall.setOnClickListener(mOnDialerClick);
		mBtChat.setOnClickListener(mOnDialerClick);
	}
	
	@Override
	protected void onDestroy() {
       
        
       super.onDestroy();
	}
	
	private final View.OnClickListener mOnDialerClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(v == mBtChat){
				if(mSipService.isRegistered()){
				}
			}
			else if(v == mBtDel){
				final int selStart = mEtNumber.getSelectionStart();
				if(selStart >0){
					final StringBuffer sb = new StringBuffer(mEtNumber.getText().toString());
					sb.delete(selStart-1, selStart);
					mEtNumber.setText(sb.toString());
					mEtNumber.setSelection(selStart-1);
				}
			}
			else if(v == mBtCall){
				if(mSipService.isRegistered()){
					ScreenAV.makeCall(mEtNumber.getText().toString(), MediaType.Audio);
				}
			}
			else{
				final int selStart = mEtNumber.getSelectionStart();
				final StringBuffer sb = new StringBuffer(mEtNumber.getText().toString());
				sb.insert(selStart, v.getTag().toString());
				mEtNumber.setText(sb.toString());
				mEtNumber.setSelection(selStart+1);
			}
		}
	};
}
