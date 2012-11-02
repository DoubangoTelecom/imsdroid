package org.doubango.test.call;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CallScreen extends Activity {
	private static final String TAG = CallScreen.class.getCanonicalName();
	
	private final NgnEngine mEngine;
	private TextView mTvInfo;
	private TextView mTvRemote;
	private Button mBtHangUp;
	
	private NgnAVSession mSession;
	private BroadcastReceiver mSipBroadCastRecv;
	
	public CallScreen(){
		super();
		mEngine = NgnEngine.getInstance();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callscreen);
        
        Bundle extras = getIntent().getExtras();
        if(extras != null){
        	mSession = NgnAVSession.getSession(extras.getLong(Main.EXTRAT_SIP_SESSION_ID));
        }
        
        if(mSession == null){
        	Log.e(TAG, "Null session");
        	finish();
        	return;
        }
        mSession.incRef();
        mSession.setContext(this);
        
        // listen for audio/video session state
        mSipBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				handleSipEvent(intent);
			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
	    registerReceiver(mSipBroadCastRecv, intentFilter);
        
        mTvInfo = (TextView)findViewById(R.id.call_screen_textView_info);
        mTvRemote = (TextView)findViewById(R.id.callscreen_textView_remote);
        mBtHangUp = (Button)findViewById(R.id.callscreen_button_hangup);
        
        mBtHangUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSession != null){
					mSession.hangUpCall();
				}
			}
		});
        
        mTvRemote.setText(mSession.getRemotePartyDisplayName());
        mTvInfo.setText(getStateDesc(mSession.getState()));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG,"onResume()");
		if(mSession != null){
			final InviteState callState = mSession.getState();
			mTvInfo.setText(getStateDesc(callState));
			if(callState == InviteState.TERMINATING || callState == InviteState.TERMINATED){
				finish();
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG,"onDestroy()");
       if(mSipBroadCastRecv != null){
    	   unregisterReceiver(mSipBroadCastRecv);
    	   mSipBroadCastRecv = null;
       }
       
       if(mSession != null){
    	   mSession.setContext(null);
    	   mSession.decRef();
       }
       super.onDestroy();
	}
	
	private String getStateDesc(InviteState state){
		switch(state){
			case NONE:
			default:
				return "Unknown";
			case INCOMING:
				return "Incoming";
			case INPROGRESS:
				return "Inprogress";
			case REMOTE_RINGING:
				return "Ringing";
			case EARLY_MEDIA:
				return "Early media";
			case INCALL:
				return "In Call";
			case TERMINATING:
				return "Terminating";
			case TERMINATED:
				return "termibated";
		}
	}
	
	private void handleSipEvent(Intent intent){
		if(mSession == null){
			Log.e(TAG, "Invalid session object");
			return;
		}
		final String action = intent.getAction();
		if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)){
			NgnInviteEventArgs args = intent.getParcelableExtra(NgnInviteEventArgs.EXTRA_EMBEDDED);
			if(args == null){
				Log.e(TAG, "Invalid event args");
				return;
			}
			if(args.getSessionId() != mSession.getId()){
				return;
			}
			
			final InviteState callState = mSession.getState();
			mTvInfo.setText(getStateDesc(callState));
			switch(callState){
				case REMOTE_RINGING:
					mEngine.getSoundService().startRingBackTone();
					break;
				case INCOMING:
					mEngine.getSoundService().startRingTone();
					break;
				case EARLY_MEDIA:
				case INCALL:
					mEngine.getSoundService().stopRingTone();
					mEngine.getSoundService().stopRingBackTone();
					mSession.setSpeakerphoneOn(false);
					break;
				case TERMINATING:
				case TERMINATED:
					mEngine.getSoundService().stopRingTone();
					mEngine.getSoundService().stopRingBackTone();
					finish();
					break;
				default:
						break;
			}
		}
	}
}
