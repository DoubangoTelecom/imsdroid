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

import java.util.Collection;
import java.util.Iterator;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.events.IInviteEventHandler;
import org.doubango.imsdroid.events.IMsrpEventHandler;
import org.doubango.imsdroid.events.InviteEventArgs;
import org.doubango.imsdroid.events.MsrpEventArgs;
import org.doubango.imsdroid.sip.MyMsrpSession;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ScreenFileTransferQueue  extends Screen implements IMsrpEventHandler{
		
	private GridView gridView;
	private ScreenFileTransAdapter adapter;
	private MsrpInviteEventHandler inviteHandler;
	
	public ScreenFileTransferQueue() {
		super(SCREEN_TYPE.FILE_TRANSFER_QUEUE_T, ScreenFileTransferQueue.class.getCanonicalName());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_file_transfer_queue);
        
        final Collection<MyMsrpSession> sessions = MyMsrpSession.getSessions();
        
        // gridView
		this.gridView = (GridView) this.findViewById(R.id.screen_file_trans_queue_gridView);
		this.gridView.setAdapter((this.adapter = new ScreenFileTransAdapter(sessions)));
		this.gridView.setOnItemClickListener(this.gridView_OnItemClickListener);	

		this.inviteHandler = new MsrpInviteEventHandler(this);
		for(Iterator<MyMsrpSession> iter = sessions.iterator(); iter.hasNext();) {
		   MyMsrpSession session = iter.next();
		   session.addMsrpEventHandler(this);
		}
	}
	
	@Override
	protected void onDestroy() {
		this.inviteHandler = null;
		final Collection<MyMsrpSession> sessions = MyMsrpSession.getSessions();
		for(Iterator<MyMsrpSession> iter = sessions.iterator(); iter.hasNext();) {
		   MyMsrpSession session = iter.next();
		   session.removeMsrpEventHandler(this);
		}
		
		super.onDestroy();
	}



	private OnItemClickListener gridView_OnItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final MyMsrpSession session;
			if ((ScreenFileTransferQueue.this.adapter.sessions.length > position) && ((session = ScreenFileTransferQueue.this.adapter.sessions[position]) != null)) {
				ServiceManager.getScreenService().show(ScreenFileTransferView.class, new Long(session.getId()).toString());
			}
		}
	};
	
	private Runnable updateList = new Runnable() {
		@Override
		public void run() {
			ScreenFileTransferQueue.this.adapter.updateView(MyMsrpSession.getSessions());
		}
	};
	
	private Runnable notifyListChanged = new Runnable() {
		@Override
		public void run() {
			ScreenFileTransferQueue.this.adapter.updateView();
		}
	};
	
	/* ===================== IMsrpEventHandler ==================*/
	@Override
	public boolean canHandle(long id) {
		return MyMsrpSession.contains(id);
	}

	@Override
	public boolean onMsrpEvent(Object sender, MsrpEventArgs e) {
		
		final MyMsrpSession session;
		switch(e.getType()){
			case DATA:
			case SUCCESS_200OK:
					this.runOnUiThread(this.notifyListChanged);
				break;
				
			case DISCONNECTED:
					if((session = (MyMsrpSession)e.getExtra("session")) != null){
						session.removeMsrpEventHandler(this);
						this.runOnUiThread(this.updateList);
					}
				break;
			
				
		}
		
		return true;
	}
	
	
	/* ============================ IInviteEventHandler =========================*/
	static class MsrpInviteEventHandler implements IInviteEventHandler
	{
		private final static String TAG = MsrpInviteEventHandler.class.getCanonicalName();
		final ScreenFileTransferQueue activity;
		
		MsrpInviteEventHandler(ScreenFileTransferQueue activity){
			ServiceManager.getSipService().addInviteEventHandler(this);
			this.activity = activity;
		}
		
		@Override
		protected void finalize() throws Throwable {
			Log.d(MsrpInviteEventHandler.TAG, "finalize()");
			ServiceManager.getSipService().removeInviteEventHandler(this);
			super.finalize();
		}
		
		@Override
		public long getId() {
			return -1;
		}
		
		@Override
		public boolean canHandle(long id) {
			return MyMsrpSession.contains(id);
		}

		@Override
		public boolean onInviteEvent(Object sender, InviteEventArgs e) {
			final MyMsrpSession session;
			switch(e.getType()){
				case INPROGRESS:
					if((session = (MyMsrpSession)e.getExtra("session")) != null){
						session.addMsrpEventHandler(this.activity);
						this.activity.runOnUiThread(this.activity.updateList);
					}
					break;
					
				case CONNECTED:			
				case DISCONNECTED:
				case TERMWAIT:
					// See onMsrpEvent() ==> Do not implement (VERY IMPORTANT!)
					break;
				default:
					break;
			}
			
			return true;
		}
	}
	
	/* ===================== Adapter ======================== */

	 class ScreenFileTransAdapter extends BaseAdapter {
		
		private MyMsrpSession[] sessions;
		private ScreenFileTransAdapter(Collection<MyMsrpSession> sessions) {
			this.sessions = (MyMsrpSession[])sessions.toArray(new MyMsrpSession[sessions.size()]); 
		}

		public int getCount() {
			return this.sessions.length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		private void updateView(Collection<MyMsrpSession> sessions){
			this.sessions = (MyMsrpSession[])sessions.toArray(new MyMsrpSession[sessions.size()]);
			this.notifyDataSetChanged();
		}
		
		private void updateView(){
			this.notifyDataSetChanged();
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			MyMsrpSession session;

			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.screen_file_transfer_queue_item, null);
			}
			
			if ((this.sessions.length <= position) || ((session = this.sessions[position]) == null)) {
				return view;
			}
			
			final ImageView ivIcon = (ImageView) view .findViewById(R.id.screen_file_trans_queue_item_imageView);
			final TextView tvFileName = (TextView) view .findViewById(R.id.screen_file_trans_queue_item_textView_name);
			final TextView tvRemoteUri = (TextView) view .findViewById(R.id.screen_file_trans_queue_item_textView_remoteUri);
			final ProgressBar progressBar = (ProgressBar) view .findViewById(R.id.screen_file_trans_queue_item_progressBar);
			
			ivIcon.setImageResource(session.isOutgoing() ? R.drawable.document_up_48 : R.drawable.document_down_48);
			final String fileName = session.getFileName();
			if(fileName == null){
				tvFileName.setText("UNKNOWN.3GP");
			}
			else{
				tvFileName.setText(fileName);
			}
			tvRemoteUri.setText(session.getRemoteParty());
			
			final long end = session.getEnd();
			final long total = session.getTotal();
			progressBar.setMax(100);
			if(end >= 0 && total>0 && end<=total){
				progressBar.setProgress((int)((100*end)/total));
				progressBar.setIndeterminate(false);
			}
			else{
				progressBar.setIndeterminate(true);
			}

			return view;
		}
	}
}
