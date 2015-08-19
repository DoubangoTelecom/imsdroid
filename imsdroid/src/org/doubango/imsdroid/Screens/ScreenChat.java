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

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.doubango.imsdroid.Engine;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Utils.DateTimeUtils;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnMessagingSession;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


public class ScreenChat extends BaseScreen{
	private static String TAG = ScreenChat.class.getCanonicalName();
	
	private final INgnHistoryService mHistorytService;
	private final INgnSipService mSipService;
	
private InputMethodManager mInputMethodManager;
	
	private static String sRemoteParty;
	
	private NgnMsrpSession mSession;
	private NgnMediaType mMediaType;
	private ScreenChatAdapter mAdapter;
	private EditText mEtCompose;
	private ListView mLvHistoy;
	private Button mBtVoiceCall;
	private Button mBtVisioCall;
	private Button mBtShare;
	private Button mBtSend;
	
	public ScreenChat() {
		super(SCREEN_TYPE.CHAT_T, TAG);
		
		mMediaType = NgnMediaType.None;
		mHistorytService = getEngine().getHistoryService();
		mSipService = getEngine().getSipService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_chat);
		
		mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		
		mEtCompose = (EditText) findViewById(R.id.screen_chat_editText_compose);
		mBtVoiceCall = (Button) findViewById(R.id.screen_chat_button_voice_call);
		mBtVisioCall = (Button) findViewById(R.id.screen_chat_button_visio_call);
		mBtShare = (Button) findViewById(R.id.screen_chat_button_share_content);
		mBtSend =(Button) findViewById(R.id.screen_chat_button_send);
		mLvHistoy = (ListView) findViewById(R.id.screen_chat_listView);
		
		mAdapter = new ScreenChatAdapter(this);
		mLvHistoy.setAdapter(mAdapter);
		mLvHistoy.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mLvHistoy.setStackFromBottom(true);
		
		mBtVoiceCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSipService.isRegistered()){
					if(!NgnStringUtils.isNullOrEmpty(sRemoteParty)){
						ScreenAV.makeCall(sRemoteParty, NgnMediaType.Audio);
					}
				}
			}
		});
		mBtVisioCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSipService.isRegistered()){
					if(!NgnStringUtils.isNullOrEmpty(sRemoteParty)){
						ScreenAV.makeCall(sRemoteParty, NgnMediaType.AudioVideo);
					}
				}
			}
		});
		mBtShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSipService.isRegistered()){
					if(!NgnStringUtils.isNullOrEmpty(sRemoteParty)){
						// TODO
					}
				}
			}
		});
				
		mBtSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSipService.isRegistered() && !NgnStringUtils.isNullOrEmpty(mEtCompose.getText().toString())){
					sendMessage();
				}
				
				if(mInputMethodManager != null){
					mInputMethodManager.hideSoftInputFromWindow(mEtCompose.getWindowToken(), 0);
				}
			}
		});
		
		mEtCompose.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mBtSend.setEnabled(!NgnStringUtils.isNullOrEmpty(mEtCompose.getText().toString()));
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override
			public void afterTextChanged(Editable s) { }
		});
		
		// BugFix: http://code.google.com/p/android/issues/detail?id=7189
		mEtCompose.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(mMediaType != NgnMediaType.None){
			initialize(mMediaType);
		}
		mAdapter.refresh();
	}
	
	@Override
	protected void onPause(){
		if(mInputMethodManager != null){
			mInputMethodManager.hideSoftInputFromWindow(mEtCompose.getWindowToken(), 0);
		}
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mSession != null){
			mSession.decRef();
			mSession = null;
		}
	}

	@Override
	public boolean hasBack(){
		return true;
	}
	
	@Override
	public boolean back(){
		boolean ret =  mScreenService.show(ScreenTabMessages.class);
		if(ret){
			mScreenService.destroy(getId());
		}
		return ret;
	}
	
	private void initialize(NgnMediaType mediaType){
		final boolean bIsNewScreen = mMediaType == NgnMediaType.None;
		mMediaType = mediaType;
		if(mMediaType == NgnMediaType.Chat){
			final String validUri = NgnUriUtils.makeValidSipUri(sRemoteParty);
			if(!NgnStringUtils.isNullOrEmpty(validUri)){
				mSession = NgnMsrpSession.getSession(new NgnPredicate<NgnMsrpSession>() {
					@Override
					public boolean apply(NgnMsrpSession session) {
						if(session != null && session.getMediaType() == NgnMediaType.Chat){
							return NgnStringUtils.equals(session.getRemotePartyUri(), validUri, false);
						}
						return false;
					}
				});
				if(mSession == null){
					if((mSession = NgnMsrpSession.createOutgoingSession(mSipService.getSipStack(), NgnMediaType.Chat, validUri)) == null){
						Log.e(TAG, "Failed to create MSRP session");
						finish();
						return;
					}
				}
				if(bIsNewScreen && mSession != null){
					mSession.incRef();
				}
			}
			else{
				Log.e(TAG, "makeValidSipUri("+sRemoteParty+") has failed");
				finish();
				return;
			}
		}
	}
	
	private boolean sendMessage(){
		boolean ret = false;
		final String content = mEtCompose.getText().toString();
		final NgnHistorySMSEvent e = new NgnHistorySMSEvent(sRemoteParty, StatusType.Outgoing);
		e.setContent(content);
		
		if(!mSipService.isRegistered()){
			Log.e(TAG, "Not registered");
			return false;
		}
		if(mMediaType == NgnMediaType.Chat){
			if(mSession != null){
				ret = mSession.SendMessage(content);
			}else{
				Log.e(TAG,"MSRP session is null");
				return false;
			}
		}
		else{
			final String remotePartyUri = NgnUriUtils.makeValidSipUri(sRemoteParty);
			final NgnMessagingSession imSession = NgnMessagingSession.createOutgoingSession(mSipService.getSipStack(), 
					remotePartyUri);
			if(!(ret = imSession.sendTextMessage(mEtCompose.getText().toString()))){
				e.setStatus(StatusType.Failed);
			}
			NgnMessagingSession.releaseSession(imSession);
		}
		
		mHistorytService.addEvent(e);
		mEtCompose.setText(NgnStringUtils.emptyValue());
		return ret;
	}
	
	public static void startChat(String remoteParty, boolean bIsPagerMode){
		final Engine engine = (Engine)NgnEngine.getInstance();
		if(!NgnStringUtils.isNullOrEmpty(remoteParty) && remoteParty.startsWith("sip:")){
			remoteParty = NgnUriUtils.getUserName(remoteParty);
		}
		
		if(NgnStringUtils.isNullOrEmpty((sRemoteParty = remoteParty))){
			Log.e(TAG, "Null Uri");
			return;
		}
		
		if(engine.getScreenService().show(ScreenChat.class)){
			final IBaseScreen screen = engine.getScreenService().getScreen(TAG);
			if(screen instanceof ScreenChat){
				((ScreenChat)screen).initialize(bIsPagerMode ? NgnMediaType.SMS : NgnMediaType.Chat);
			}
		}
	}
	
	//
	// HistoryEventSMSFilter
	//
	static class HistoryEventChatFilter implements NgnPredicate<NgnHistoryEvent>{
		@Override
		public boolean apply(NgnHistoryEvent event) {
			if (event != null && (event.getMediaType() == NgnMediaType.SMS)){
				return NgnStringUtils.equals(sRemoteParty, event.getRemoteParty(), false);
			}
			return false;
		}
	}
	
	//
	// DateComparator
	//
	static class DateComparator implements Comparator<NgnHistoryEvent>{
	    @Override
	    public int compare(NgnHistoryEvent e1, NgnHistoryEvent e2) {
	    	return (int)(e1.getStartTime() - e2.getStartTime());
	    }
	}
	
	/**
	 * ScreenChatAdapter
	 */
	static class ScreenChatAdapter extends BaseAdapter implements Observer {
		private List<NgnHistoryEvent> mEvents;
		private final LayoutInflater mInflater;
		private final Handler mHandler;
		private final ScreenChat mBaseScreen;
		
		ScreenChatAdapter(ScreenChat baseSceen) {
			mBaseScreen = baseSceen;
			mHandler = new Handler();
			mInflater = LayoutInflater.from(mBaseScreen);
			mEvents = mBaseScreen.mHistorytService.getObservableEvents().filter(new HistoryEventChatFilter());
			Collections.sort(mEvents, new DateComparator());
			mBaseScreen.mHistorytService.getObservableEvents().addObserver(this);
		}
		
		@Override
		protected void finalize() throws Throwable {
			mBaseScreen.mHistorytService.getObservableEvents().deleteObserver(this);
			super.finalize();
		}
		
		public void refresh(){
			mEvents = mBaseScreen.mHistorytService.getObservableEvents()
					.filter(new HistoryEventChatFilter());
			Collections.sort(mEvents, new DateComparator());
			if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
				notifyDataSetChanged();
			} else {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
			}
		}
		
		@Override
		public int getCount() {
			return mEvents.size();
		}

		@Override
		public Object getItem(int position) {
			return mEvents.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			
			final NgnHistoryEvent event = (NgnHistoryEvent)getItem(position);
			if(event == null){
				return null;
			}
			if (view == null) {
				switch(event.getMediaType()){
					case Audio:
					case AudioVideo:
					case FileTransfer:
					default:
						Log.e(TAG, "Invalid media type");
						return null;
					case SMS:
						view = mInflater.inflate(R.layout.screen_chat_item, null);
						break;
				}
			}
			
			final NgnHistorySMSEvent SMSEvent = (NgnHistorySMSEvent)event;
			final String content = SMSEvent.getContent();
			final boolean bIncoming = SMSEvent.getStatus() == StatusType.Incoming;
			
			TextView textView = (TextView) view.findViewById(R.id.screen_chat_item_textView);
			textView.setText(content == null ? NgnStringUtils.emptyValue() : content);
			textView.setBackgroundResource(bIncoming ? R.drawable.baloon_in_middle_center : R.drawable.baloon_out_middle_center);
			
			((TextView)view.findViewById(R.id.screen_chat_item_textView_date))
				.setText(DateTimeUtils.getFriendlyDateString(new Date(event.getStartTime())));
			
			view.findViewById(R.id.screen_chat_item_imageView_top_left)
			.setBackgroundResource(bIncoming? R.drawable.baloon_in_top_left : R.drawable.baloon_out_top_left);
			view.findViewById(R.id.screen_chat_item_imageView_top_center)
			.setBackgroundResource(bIncoming ? R.drawable.baloon_in_top_center : R.drawable.baloon_out_top_center);
			view.findViewById(R.id.screen_chat_item_imageView_top_right)
			.setBackgroundResource(bIncoming ? R.drawable.baloon_in_top_right : R.drawable.baloon_out_top_right);
			
			view.findViewById(R.id.screen_chat_item_imageView_middle_left)
			.setBackgroundResource(bIncoming ? R.drawable.baloon_in_middle_left : R.drawable.baloon_out_middle_left);
			view.findViewById(R.id.screen_chat_item_imageView_middle_right)
			.setBackgroundResource(bIncoming ? R.drawable.baloon_in_middle_right : R.drawable.baloon_out_middle_right);
			
			view.findViewById(R.id.screen_chat_item_imageView_bottom_left)
			.setBackgroundResource(bIncoming ? R.drawable.baloon_in_bottom_left : R.drawable.baloon_out_bottom_left);
			view.findViewById(R.id.screen_chat_item_imageView_bottom_center)
			.setBackgroundResource(bIncoming ? R.drawable.baloon_in_bottom_center : R.drawable.baloon_out_bottom_center);
			view.findViewById(R.id.screen_chat_item_imageView_bottom_right)
			.setBackgroundResource(bIncoming ? R.drawable.baloon_in_bottom_right : R.drawable.baloon_out_bottom_right);
			
			view.findViewById(R.id.screen_chat_item_linearLayout_left)
			.setVisibility(bIncoming ? View.VISIBLE : View.GONE);
			view.findViewById(R.id.screen_chat_item_linearLayout_right)
			.setVisibility(bIncoming ? View.GONE : View.VISIBLE);
			
			return view;
		}

		@Override
		public void update(Observable observable, Object data) {
			refresh();
		}
	}
}
