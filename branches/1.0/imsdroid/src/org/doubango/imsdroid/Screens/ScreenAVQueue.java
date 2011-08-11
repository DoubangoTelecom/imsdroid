/*
* Copyright (C) 2010 Mamadou Diop.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango.org>
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
*
*/
package org.doubango.imsdroid.Screens;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.ObservableHashMap;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.sip.MyAVSession;

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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;


/**
 * ScreenAVQueue
 * @author Mamadou Diop
 *
 */
public class ScreenAVQueue  extends Screen{
	
	private final static int MENU_OPEN_CALL = 1;
	private final static int MENU_HANGUP_CALL = 2;
	
	private final static int MENU_HANGUP_ALLCALLS = 10;
	
	private GridView gridView;
	private ScreenAVQueueAdapter adapter;
	
	public ScreenAVQueue() {
		super(SCREEN_TYPE.AV_QUEUE_T, ScreenAVQueue.class.getCanonicalName());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_av_queue);
        
        // gridView
		this.gridView = (GridView) this.findViewById(R.id.screen_av_queue_gridView);
		this.gridView.setAdapter((this.adapter = new ScreenAVQueueAdapter(this)));
		this.registerForContextMenu(this.gridView);
		this.gridView.setOnItemClickListener(this.gridView_OnItemClickListener);	
	}
	
	@Override
	protected void onDestroy() {		
		super.onDestroy();
	}
	
	@Override
	public boolean createOptionsMenu(Menu menu){
		menu.add(0, ScreenAVQueue.MENU_HANGUP_ALLCALLS, 0, "Hang Up all calls").setIcon(R.drawable.phone_hang_up_48);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
			case ScreenAVQueue.MENU_HANGUP_ALLCALLS:
				ObservableHashMap<Long, MyAVSession> sessions = MyAVSession.getSessions();
				MyAVSession session;
				for(Map.Entry<Long, MyAVSession> entry : sessions.entrySet()) {
					session = entry.getValue();
					if(session.isConnected()){
						session.hangUp();
					}
				}
				break;
		}
		return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		menu.add(0, ScreenAVQueue.MENU_OPEN_CALL, Menu.NONE, "Open");
		menu.add(0, ScreenAVQueue.MENU_HANGUP_CALL, Menu.NONE, "Hang Up");
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final MyAVSession session;
		final int location = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
		if((session = (MyAVSession)this.adapter.getItem(location)) == null){ /* should never happen ...but who know? */
			return super.onContextItemSelected(item);
		}
		
		switch(item.getItemId()){
			case ScreenAVQueue.MENU_OPEN_CALL:
				this.resumeSession(session);
				return true;
			case ScreenAVQueue.MENU_HANGUP_CALL:
				session.hangUp();
				return true;
				
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public boolean haveMenu(){
		return true;
	}
	
	private OnItemClickListener gridView_OnItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final MyAVSession session = (MyAVSession)ScreenAVQueue.this.adapter.getItem(position);
			if(session != null){
				ScreenAVQueue.this.resumeSession(session);
			}
		}
	};
	
	private void resumeSession(MyAVSession session){
		// Hold the active call
		final MyAVSession activeSession = MyAVSession.getFirstActiveCallAndNot(session.getId());
		if(activeSession != null){
			activeSession.holdCall();
		}
		
		// Resume the selected call and display it to the screen
		ServiceManager.getScreenService().show(ScreenAV.class, Long.toString(session.getId()));
		if(session.isLocalHeld()){
			session.resumeCall();
		}
	}

	/***
	 * ScreenAVQueueAdapter
	 * @author Mamadou Diop
	 *
	 */
	 class ScreenAVQueueAdapter extends BaseAdapter implements Observer {
		
		private ObservableHashMap<Long, MyAVSession> sessions;
		private final LayoutInflater inflater;
		private final Handler handler;
		
		private ScreenAVQueueAdapter(Context context) {
			this.handler = new Handler();
			this.inflater = LayoutInflater.from(context);
			this.sessions = MyAVSession.getSessions();
			this.sessions.addObserver(this);
		}

		public int getCount() {
			return this.sessions.size();
		}

		public Object getItem(int position) {
			return this.sessions.getAt(position);
		}

		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public void update(Observable observable, Object data) {
			if(Thread.currentThread() == Looper.getMainLooper().getThread()){
				this.sessions = MyAVSession.getSessions();
				this.notifyDataSetChanged();
			}
			else{
				this.handler.post(new Runnable(){
					@Override
					public void run() {
						ScreenAVQueueAdapter.this.sessions = MyAVSession.getSessions();
						ScreenAVQueueAdapter.this.notifyDataSetChanged();
					}
				});
			}
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View view = convertView;
			MyAVSession session;
			
			if (view == null) {
				view = this.inflater.inflate(R.layout.screen_av_queue_item, null);
			}
			session = this.sessions.getAt(position);
			
			if((session = this.sessions.getAt(position)) != null){
				ImageView imageView = (ImageView) view.findViewById(R.id.screen_av_queue_item_imageView);
				TextView tvRemoteUri = (TextView) view.findViewById(R.id.screen_av_queue_item_textView_remote);
				TextView tvInfo = (TextView) view.findViewById(R.id.screen_av_queue_item_textView_info);
				
				if(session.isLocalHeld() || session.isRemoteHeld()){
					imageView.setImageResource(R.drawable.phone_hold_48);
					tvInfo.setText("On Hold");
				}
				else{
					imageView.setImageResource(R.drawable.phone_resume_48);
					switch(session.getState()){
						case CALL_INCOMING:
							tvInfo.setText("Incoming");
							break;
						case CALL_INPROGRESS:
							tvInfo.setText("In Progress");
							break;
						case INCALL:
						default:
							tvInfo.setText("In Call");
							break;
						case CALL_TERMINATED:
							tvInfo.setText("Terminated");
							break;
					}
				}				
				
				final String remoteParty = session.getRemoteParty();
				if(remoteParty != null){
					tvRemoteUri.setText(remoteParty);
				}
				else{
					tvRemoteUri.setText("unknown");
				}
			}
			
			return view;
		}
	}
	
}
