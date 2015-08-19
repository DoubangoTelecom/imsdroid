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

import java.util.Date;
import java.util.TimerTask;

import org.doubango.imsdroid.Engine;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnMsrpEventArgs;
import org.doubango.ngn.events.NgnMsrpEventTypes;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class ScreenFileTransferView extends BaseScreen{
	private static final String TAG = ScreenFileTransferView.class.getCanonicalName();
	
	private NgnMsrpSession mSession;
	private BroadcastReceiver mSipAndMsrpBroadCastRecv;
	
	private RelativeLayout mRelativeLayout;
	private ImageView mIvPreview;
    private ProgressBar mProgressBar;
    private Button mBtAccept;
    private Button mBtAbort;
    private TextView mTvInfo;
    private TextView mTvByteRange;
    private TextView mTvFileName;
    private TextView mTvRemoteParty;
    private String mStringFormat;
    private NgnTimer mTimerSuicide;
    
	public ScreenFileTransferView() {
		super(SCREEN_TYPE.FILETRANSFER_VIEW_T, TAG);		
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_filetrans_view);
        
        super.mId = getIntent().getStringExtra("id");
        if(NgnStringUtils.isNullOrEmpty(super.mId)){
			Log.e(TAG, "Invalid MSRP session");
			finish(); 
			mScreenService.show(ScreenHome.class);
			return;
		}
        mSession = NgnMsrpSession.getSession(NgnStringUtils.parseLong(super.mId, -1));
		if(mSession == null){
			Log.e(TAG, String.format("Cannot find MSRP session with id=%s", super.mId));
			finish(); 
			mScreenService.show(ScreenHome.class);
			return;
		}
		mSession.incRef();
		mSession.setContext(this);
		
		mRelativeLayout = (RelativeLayout) findViewById(R.id.screen_filetrans_view_relativeLayout);
		mIvPreview = (ImageView) findViewById(R.id.screen_filetrans_view_imageView_preview);
        mProgressBar = (ProgressBar) findViewById(R.id.screen_filetrans_view_progressBar);
        mBtAccept = (Button) findViewById(R.id.screen_filetrans_view_button_accept);
        mBtAbort = (Button) findViewById(R.id.screen_filetrans_view_button_abort);
        mTvInfo = (TextView) findViewById(R.id.screen_filetrans_view_textView_info);
        mTvByteRange = (TextView) findViewById(R.id.screen_filetrans_view_textView_byteRange);
        mTvRemoteParty = (TextView) findViewById(R.id.screen_filetrans_view_textView_remoteParty);
        mTvFileName = (TextView) findViewById(R.id.screen_filetrans_view_textView_cname);
		
        String remoteParty = NgnUriUtils.getDisplayName(mSession.getRemotePartyUri());
        if(NgnStringUtils.isNullOrEmpty(remoteParty)){
        	remoteParty = NgnStringUtils.nullValue();
        }
        mTvRemoteParty.setText(remoteParty);
        mTvInfo.setText(mSession.isOutgoing()? "Sending Content...": "Receiving Content...");
        mBtAccept.setVisibility(mSession.isOutgoing()||mSession.isConnected()? View.GONE : View.VISIBLE);
        mBtAbort.setText(mSession.isConnected()? "Abort" : (mSession.isOutgoing() ? "Cancel" : "Decline"));
        final String fileName = mSession.getFileName();
        final String filePath = mSession.getFilePath();
        if(!NgnStringUtils.isNullOrEmpty(fileName)){
        	mTvFileName.setText(fileName);
        }
		if (mSession.isOutgoing()) {
			mStringFormat = "%d/%d Bytes sent";
			if (filePath != null) {
				try {
					mIvPreview.setImageURI(new Uri.Builder().path(filePath).build());
				} catch (Exception e) {
					mIvPreview.setImageResource(R.drawable.document_up_128);
				}
			}
		} else {
			mStringFormat = "%d/%d Bytes received";
			mIvPreview.setImageResource(R.drawable.document_down_128);
		}
		updateProgressBar(mSession.getStart(), mSession.getEnd(), mSession.getTotal());
		if(mSession.isConnected()){
			mRelativeLayout.setBackgroundResource(R.drawable.grad_bkg_incall);
		}
        
		mSipAndMsrpBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				handleSipAndMsrpEvent(intent);
			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
		intentFilter.addAction(NgnMsrpEventArgs.ACTION_MSRP_EVENT);
	    registerReceiver(mSipAndMsrpBroadCastRecv, intentFilter);
	    
	    mBtAbort.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mSession.hangUp()){
					scheduleSuicide();
				}
			}
		});
	    mBtAccept.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSession.accept();
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG,"onDestroy()");
       if(mSipAndMsrpBroadCastRecv != null){
    	   unregisterReceiver(mSipAndMsrpBroadCastRecv);
    	   mSipAndMsrpBroadCastRecv = null;
       }
       
       if(mSession != null){
    	   mSession.setContext(null);
    	   mSession.decRef();
       }
       super.onDestroy();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
	}
	
	@Override
	public boolean hasBack(){
		return true;
	}
	
	@Override
	public boolean back(){
		boolean ret =  mScreenService.back();
		if(ret){
			mScreenService.destroy(getId());
		}
		return ret;
	}
	
	private final TimerTask mTimerTaskSuicide = new TimerTask(){
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				public void run() {
					final IBaseScreen currentScreen = mScreenService.getCurrentScreen();
					if (currentScreen != null && currentScreen.getType() == SCREEN_TYPE.FILETRANSFER_VIEW_T) {
						mScreenService.show(ScreenHome.class);
						mScreenService.destroy(getId());
					}
				}
			});
		}
	};

	private synchronized void scheduleSuicide(){
		if(mTimerSuicide == null){
			mRelativeLayout.setBackgroundResource(R.drawable.grad_bkg_termwait);
       	 	mTimerSuicide = new NgnTimer();
       	 	mTimerSuicide.schedule(mTimerTaskSuicide, new Date(new Date().getTime() + 1500));
        }
	}
	
	private void updateProgressBar(long start, long end, long total){
		if(end >= 0 && total>0 && end<=total){
            mProgressBar.setProgress((int)((100*end)/total));
            mTvByteRange.setText(String.format(mStringFormat, end, total));
            mProgressBar.setIndeterminate(false);
	    }
	    else{
	    	mProgressBar.setIndeterminate(true);
	    }
	}
	
	private void handleSipAndMsrpEvent(Intent intent){
		@SuppressWarnings("unused")
		InviteState state;
		if(mSession == null){
			Log.e(TAG, "Invalid session object");
			return;
		}
		final String action = intent.getAction();
		
		// SIP Invite events
		if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)){
			final NgnInviteEventArgs args = intent.getParcelableExtra(NgnInviteEventArgs.EXTRA_EMBEDDED);
			if(args == null){
				Log.e(TAG, "Invalid event args");
				return;
			}
			if(args.getSessionId() != mSession.getId()){
				return;
			}
			
			switch((state = mSession.getState())){
				case NONE:
				default:
					break;
					
				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
					mTvByteRange.setText("Trying...");
					break;
					
				case EARLY_MEDIA:
					break;
				case INCALL:
					mRelativeLayout.setBackgroundResource(R.drawable.grad_bkg_incall);
					mTvByteRange.setText("Connected!");
					break;
					
				case TERMINATING:
				case TERMINATED:
					mTvByteRange.setText("Terminated!");
					scheduleSuicide();
					break;
			}
		}
		// MSRP events
		else if(NgnMsrpEventArgs.ACTION_MSRP_EVENT.equals(action)){
			final NgnMsrpEventArgs args = intent.getParcelableExtra(NgnMsrpEventArgs.EXTRA_EMBEDDED);
			final NgnMsrpEventTypes type;
			if(args == null){
				Log.e(TAG, "Invalid event args");
				return;
			}
			if(args.getSessionId() != mSession.getId()){
				return;
			}
			
			switch((type = args.getEventType())){
				case CONNECTED:
					mBtAbort.setText("Abort");
                    mBtAccept.setVisibility(View.GONE);
					break;
				case SUCCESS_2XX:
				case SUCCESS_REPORT:
				case DATA:
					if((mSession.isOutgoing() && type == NgnMsrpEventTypes.SUCCESS_2XX) || (!mSession.isOutgoing() && type == NgnMsrpEventTypes.DATA)){
                        updateProgressBar(intent.getLongExtra(NgnMsrpEventArgs.EXTRA_BYTE_RANGE_START, -1L), 
                        		intent.getLongExtra(NgnMsrpEventArgs.EXTRA_BYTE_RANGE_END, -1L), 
                        		intent.getLongExtra(NgnMsrpEventArgs.EXTRA_BYTE_RANGE_TOTAL, -1L));
					}
					break;
				case ERROR:
					mTvByteRange.setText("ERROR!");
					break;
				case DISCONNECTED:
					mTvByteRange.setText("Terminated!");
					scheduleSuicide();
					break;
			}
		}
	}
	
	static boolean sendFile(String remoteUri, String filePath){
		final Engine engine = (Engine)Engine.getInstance();
		final INgnSipService sipService = engine.getSipService();
		final IScreenService screenService = engine.getScreenService();
		final String validUri = NgnUriUtils.makeValidSipUri(remoteUri);
		if(validUri == null){
			Log.e(TAG, "failed to normalize sip uri '" + remoteUri + "'");
			return false;
		}
		final NgnMsrpSession msrpSession = NgnMsrpSession.createOutgoingSession(sipService.getSipStack(), 
				NgnMediaType.FileTransfer, validUri);
		if(msrpSession == null){
			Log.e(TAG,"Failed to create MSRP session");
			return false;
		}
		if(msrpSession.sendFile(filePath)){
			screenService.show(ScreenFileTransferView.class, Long.toString(msrpSession.getId()));
			return true;
		}
		else{
			Log.e(TAG, "Failed to send file");
			return false;
		}
	}
}
