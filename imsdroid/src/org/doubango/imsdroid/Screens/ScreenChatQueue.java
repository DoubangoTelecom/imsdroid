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

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.doubango.imsdroid.R;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.utils.NgnListUtils;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ScreenChatQueue extends BaseScreen{
	private static final String TAG = ScreenChatQueue.class.getCanonicalName();
	
	private ListView mListView;
    private ScreenChatQueueAdapter mAdapter;
    
	public ScreenChatQueue() {
		super(SCREEN_TYPE.CHAT_QUEUE_T, TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_chat_queue);
        
        mListView = (ListView)findViewById(R.id.screen_chat_queue_listView);
        mAdapter = new ScreenChatQueueAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final NgnMsrpSession session = (NgnMsrpSession)mAdapter.getItem(position);
                if(session != null){
                	ScreenChat.startChat(session.getRemotePartyUri(), false);
                }
			}
		});
	}
	
	//
	// ScreenChatQueueAdapter
	//
    private class ScreenChatQueueAdapter extends BaseAdapter implements Observer { 
        private List<NgnMsrpSession> mSessions;
        private final LayoutInflater mInflater;
        private final Handler mHandler;
        private final NgnPredicate<NgnMsrpSession> mFilter;
        
        ScreenChatQueueAdapter(Context context) {
        	mHandler = new Handler();
            mInflater = LayoutInflater.from(context);
            NgnMsrpSession.getSessions().addObserver(this);
            mFilter = new NgnPredicate<NgnMsrpSession>(){
				@Override
				public boolean apply(NgnMsrpSession session) {
					return session != null && NgnMediaType.isChat(session.getMediaType());
				}
            };
            mSessions = NgnListUtils.filter(NgnMsrpSession.getSessions().values(), mFilter);
        }
        
        @Override
        public int getCount() {
            return mSessions == null ? 0 : mSessions.size();
        }
        
        @Override
        public Object getItem(int position) {
            return mSessions == null || mSessions.size() <= position ? null : mSessions.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public void update(Observable observable, Object data) {
        	mSessions = NgnListUtils.filter(NgnMsrpSession.getSessions().values(), mFilter);
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
        public View getView(int position, View convertView, ViewGroup parent) {    
            View view = convertView;
            NgnMsrpSession session;
            
            if (view == null) {
                view = mInflater.inflate(R.layout.screen_chat_queue_item, null);
            }
            session = (NgnMsrpSession)getItem(position);
            
            if(session != null){
                final TextView tvRemoteParty = (TextView) view.findViewById(R.id.screen_chat_queue_item_textView_remote);
                final TextView tvInfo = (TextView) view.findViewById(R.id.screen_chat_queue_item_textView_info);
				switch (session.getState()) {
					case INCOMING:
						tvInfo.setText("Incoming");
						break;
					case INPROGRESS:
						tvInfo.setText("In Progress");
						break;
					case INCALL:
						tvInfo.setText("Connected");
						break;
					case TERMINATED:
						tvInfo.setText("Terminated");
						break;
					default:
						tvInfo.setText(NgnStringUtils.emptyValue());
						break;
				}        
                
                final String remoteParty = session.getRemotePartyDisplayName();
                if(remoteParty != null){
                    tvRemoteParty.setText(remoteParty);
                }
                else{
                   tvRemoteParty.setText(NgnStringUtils.nullValue());
                }
            }
            
            return view;
        }
    }
}
