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
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Utils.DateTimeUtils;
import org.doubango.ngn.model.NgnContact;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.model.NgnHistorySMSEvent.HistoryEventSMSFilter;
import org.doubango.ngn.model.NgnHistorySMSEvent.HistoryEventSMSIntelligentFilter;
import org.doubango.ngn.services.INgnContactService;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ScreenTabMessages extends BaseScreen {
	private static String TAG = ScreenTabMessages.class.getCanonicalName();
	
	private static final int MENU_CLEAR_MESSSAGES = 1;
	
	private final INgnHistoryService mHistoryService;
	private final INgnContactService mContactService;
	
	private ListView mListView;
	private ScreenTabMessagesAdapter mAdapter;
	
	public ScreenTabMessages() {
		super(SCREEN_TYPE.TAB_MESSAGES_T, TAG);
		
		mHistoryService = (INgnHistoryService)getEngine().getHistoryService();
		mContactService = (INgnContactService)getEngine().getContactService();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_messages);
		Log.d(TAG,"onCreate()");
		
		mAdapter = new ScreenTabMessagesAdapter(this);
		mListView = (ListView) findViewById(R.id.screen_tab_messages_listView);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mOnItemListViewClickListener);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG,"onResume()");
	}
	
	@Override
	public boolean hasBack(){
		return true;
	}
	
	@Override
	public boolean back(){
		return mScreenService.show(ScreenHome.class);
	}
	
	@Override
	public boolean hasMenu() {
		return true;
	}
	
	@Override
	public boolean createOptionsMenu(Menu menu) {
		menu.add(0, MENU_CLEAR_MESSSAGES, 0, "Clear entries");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case MENU_CLEAR_MESSSAGES:
				mHistoryService.deleteEvents(new HistoryEventSMSFilter());
				break;
		}
		return true;
	}
	
	private final OnItemClickListener mOnItemListViewClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final NgnHistoryEvent event = (NgnHistoryEvent)parent.getItemAtPosition(position);
			if(event != null){
				ScreenChat.startChat(event.getRemoteParty(), true);
			}
		}
	};
	
	/**
	 * ScreenTabMessagesAdapter
	 */
	static class ScreenTabMessagesAdapter extends BaseAdapter implements Observer {
		private List<NgnHistoryEvent> mEvents;
		private final LayoutInflater mInflater;
		private final Handler mHandler;
		private final ScreenTabMessages mBaseScreen;
		private final MyHistoryEventSMSIntelligentFilter mFilter;
		
		ScreenTabMessagesAdapter(ScreenTabMessages baseSceen) {
			mBaseScreen = baseSceen;
			mHandler = new Handler();
			mInflater = LayoutInflater.from(mBaseScreen);
			mFilter = new MyHistoryEventSMSIntelligentFilter();
			mEvents = mBaseScreen.mHistoryService.getObservableEvents().filter(mFilter);
			mBaseScreen.mHistoryService.getObservableEvents().addObserver(this);
		}
		
		@Override
		protected void finalize() throws Throwable {
			mBaseScreen.mHistoryService.getObservableEvents().deleteObserver(this);
			super.finalize();
		}

		void refresh(){
			mFilter.reset();
			mEvents = mBaseScreen.mHistoryService.getObservableEvents().filter(mFilter);
			if(Thread.currentThread() == Looper.getMainLooper().getThread()){
				notifyDataSetChanged();
			}
			else{
				mHandler.post(new Runnable(){
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
						view = mInflater.inflate(R.layout.screen_tab_messages_item, null);
						break;
				}
			}
			
			String remoteParty = event.getRemoteParty();
			NgnContact contact = mBaseScreen.mContactService.getContactByPhoneNumber(remoteParty);
			if(contact == null){
				remoteParty = NgnUriUtils.getDisplayName(remoteParty);
			}
			else{
				remoteParty = contact.getDisplayName();
			}
			
			final TextView tvRemote = (TextView)view.findViewById(R.id.screen_tab_messages_item_textView_remote);
			final TextView tvDate = (TextView)view.findViewById(R.id.screen_tab_messages_item_textView_date);
			final TextView tvContent = (TextView)view.findViewById(R.id.screen_tab_messages_item_textView_content);
			final TextView tvUnSeen = (TextView)view.findViewById(R.id.screen_tab_messages_item_textView_unseen);
			
			final NgnHistorySMSEvent SMSEvent = (NgnHistorySMSEvent)event;
			tvRemote.setText(remoteParty);
			tvDate.setText(DateTimeUtils.getFriendlyDateString(new Date(event.getStartTime())));
			final String SMSContent = SMSEvent.getContent();
			tvContent.setText(NgnStringUtils.isNullOrEmpty(SMSContent) 
					? NgnStringUtils.emptyValue() : SMSContent);
			tvUnSeen.setText(Integer.toString(mFilter.getUnSeen()));
			
			return view;
		}

		@Override
		public void update(Observable observable, Object data) {
			Log.d(TAG, "update()");
			refresh();
		}
	}
	
	//
	// MyHistoryEventSMSIntelligentFilter
	//
	static class MyHistoryEventSMSIntelligentFilter extends HistoryEventSMSIntelligentFilter{
		private int mUnSeen;
		
		int getUnSeen(){
			return mUnSeen;
		}

		@Override
		protected void reset(){
			super.reset();
			mUnSeen = 0;
		}
		
		@Override
		public boolean apply(NgnHistoryEvent event) {
			if(super.apply(event)){
				mUnSeen += event.isSeen() ? 0 : 1;
				return true;
			}
			return false;
		}
	}
}
