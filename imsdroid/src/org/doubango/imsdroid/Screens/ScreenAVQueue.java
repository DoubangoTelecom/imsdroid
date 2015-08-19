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

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.doubango.imsdroid.R;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.ngn.utils.NgnStringUtils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;


public class ScreenAVQueue extends BaseScreen{
	private static final String TAG = ScreenAVQueue.class.getCanonicalName();
	
	private final static int MENU_OPEN_CALL = 0;
    private final static int MENU_HANGUP_CALL = 1;
    private final static int MENU_HANGUP_ALLCALLS = 2;
    
    private ListView mListView;
    private ScreenAVQueueAdapter mAdapter;

    
	public ScreenAVQueue() {
		super(SCREEN_TYPE.AV_QUEUE_T, TAG);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_av_queue);
        
        mListView = (ListView)findViewById(R.id.screen_av_queue_listView);
        registerForContextMenu(mListView);
        mAdapter = new ScreenAVQueueAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final NgnAVSession session = (NgnAVSession)mAdapter.getItem(position);
                if(session != null){
                	resumeAVSession(session);
                }
			}
		});
	}
	
	@Override
    public boolean createOptionsMenu(Menu menu){
		menu.add(0, MENU_HANGUP_ALLCALLS, 0, "Hang Up all calls").setIcon(R.drawable.phone_hang_up_48);
		return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {    
		switch (item.getItemId()) {
			case ScreenAVQueue.MENU_HANGUP_ALLCALLS:
				final NgnObservableHashMap<Long, NgnAVSession> sessions = NgnAVSession.getSessions();
				NgnAVSession session;
				for (Map.Entry<Long, NgnAVSession> entry : sessions.entrySet()) {
					session = entry.getValue();
					if (session.isActive()) {
						session.hangUpCall();
					}
				}
				break;
		}
		return true;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.add(0, MENU_OPEN_CALL, Menu.NONE, "Open");
		menu.add(0, MENU_HANGUP_CALL, Menu.NONE, "Hang Up");
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
		final NgnAVSession session;
		final int location = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
		if ((session = (NgnAVSession) mAdapter.getItem(location)) == null) {
			return super.onContextItemSelected(item);
		}

		switch (item.getItemId()) {
			case MENU_OPEN_CALL:
				resumeAVSession(session);
				return true;
			case ScreenAVQueue.MENU_HANGUP_CALL:
				session.hangUpCall();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
    }
    
    @Override
    public boolean hasMenu(){
        return true;
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
    
	private void resumeAVSession(NgnAVSession session){
        // Hold the active call
        final NgnAVSession activeSession = NgnAVSession.getFirstActiveCallAndNot(session.getId());
        if(activeSession != null){
            activeSession.holdCall();
        }
        // Resume the selected call and display it to the screen
        mScreenService.show(ScreenAV.class, Long.toString(session.getId()));
        if(session.isLocalHeld()){
            session.resumeCall();
        }
	}

	
	//
	// ScreenAVQueueAdapter
	//
    private class ScreenAVQueueAdapter extends BaseAdapter implements Observer { 
        private NgnObservableHashMap<Long, NgnAVSession> mAVSessions;
        private final LayoutInflater mInflater;
        private final Handler mHandler;
        
        ScreenAVQueueAdapter(Context context) {
        	mHandler = new Handler();
            mInflater = LayoutInflater.from(context);
            mAVSessions = NgnAVSession.getSessions();
            mAVSessions.addObserver(this);
        }
        
        @Override
        public int getCount() {
                return mAVSessions.size();
        }
        
        @Override
        public Object getItem(int position) {
                return mAVSessions.getAt(position);
        }
        
        @Override
        public long getItemId(int position) {
                return position;
        }
        
        @Override
        public void update(Observable observable, Object data) {
        	mAVSessions = NgnAVSession.getSessions();
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
            NgnAVSession session;
            
            if (view == null) {
                view = mInflater.inflate(R.layout.screen_av_queue_item, null);
            }
            session = (NgnAVSession)getItem(position);
            
            if(session != null){
                final ImageView imageView = (ImageView) view.findViewById(R.id.screen_av_queue_item_imageView);
                final TextView tvRemoteParty = (TextView) view.findViewById(R.id.screen_av_queue_item_textView_remote);
                final TextView tvInfo = (TextView) view.findViewById(R.id.screen_av_queue_item_textView_info);
                
                if(session.isLocalHeld() || session.isRemoteHeld()){
                    imageView.setImageResource(R.drawable.phone_hold_48);
                    tvInfo.setText("Held");
                }
                else{
                    imageView.setImageResource(R.drawable.phone_resume_48);
					switch (session.getState()) {
						case INCOMING:
							tvInfo.setText("Incoming");
							break;
						case INPROGRESS:
							tvInfo.setText("In Progress");
							break;
						case INCALL:
						default:
							tvInfo.setText("In Call");
							break;
						case TERMINATED:
							tvInfo.setText("Terminated");
							break;
					}
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
