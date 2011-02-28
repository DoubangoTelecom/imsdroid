package org.doubango.imsdroid.Screens;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Media.MediaType;
import org.doubango.imsdroid.Model.Contact;
import org.doubango.imsdroid.Model.HistoryEvent;
import org.doubango.imsdroid.QuickAction.ActionItem;
import org.doubango.imsdroid.QuickAction.QuickAction;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.Services.IHistoryService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sip.MyAVSession;
import org.doubango.imsdroid.Utils.ObservableList;
import org.doubango.imsdroid.Utils.StringUtils;
import org.doubango.imsdroid.Utils.UriUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ScreenTabHistory extends BaseScreen {
	private static String TAG = ScreenTabHistory.class.getCanonicalName();
	
	private final IHistoryService mHistorytService;
	private final IContactService mContactService;
	private final ISipService mSipService;
	
	private ScreenTabHistoryAdapter mAdapter;
	private GridView mGridView;
	
	private final ActionItem mAItemVoiceCall;
	private final ActionItem mAItemVideoCall;
	private final ActionItem mAItemMessaging;
	
	private HistoryEvent mSelectedEvent;
	private QuickAction mLasQuickAction;
	
	public ScreenTabHistory() {
		super(SCREEN_TYPE.TAB_HISTORY_T, TAG);
		
		mHistorytService = ServiceManager.getHistoryService();
		mContactService = ServiceManager.getContactService();
		mSipService = ServiceManager.getSipService();
		
		mAItemVoiceCall = new ActionItem();
		mAItemVoiceCall.setTitle("Voice");
		mAItemVoiceCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSelectedEvent != null){
					ScreenAV.makeCall(mSelectedEvent.getRemoteParty(), MediaType.Audio);
					if(mLasQuickAction != null){
						mLasQuickAction.dismiss();
					}
				}
			}
		});
		
		mAItemVideoCall = new ActionItem();
		mAItemVideoCall.setTitle("Video");
		mAItemVideoCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSelectedEvent != null){
					ScreenAV.makeCall(mSelectedEvent.getRemoteParty(), MediaType.AudioVideo);
					if(mLasQuickAction != null){
						mLasQuickAction.dismiss();
					}
				}
			}
		});
		
		mAItemMessaging = new ActionItem();
		mAItemMessaging.setTitle("Chat");
		mAItemMessaging.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mLasQuickAction != null){
					mLasQuickAction.dismiss();
				}
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_history);
		
		mAdapter = new ScreenTabHistoryAdapter(this);
		mGridView = (GridView) findViewById(R.id.screen_tab_history_gridView);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(mOnItemListViewClickListener);
		mGridView.setOnItemLongClickListener(mOnItemListViewLongClickListener);
	    registerForContextMenu(mGridView);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(mHistorytService.isLoading()){
			Toast.makeText(this, "Loading history...", Toast.LENGTH_SHORT).show();
		}
	}
	
	private final OnItemClickListener mOnItemListViewClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(!mSipService.isRegistered()){
				Log.e(TAG, "Not registered yet");
				return;
			}
			
			mSelectedEvent = (HistoryEvent)parent.getItemAtPosition(position);
			if(mSelectedEvent != null){
				mLasQuickAction = new QuickAction(view);
				if(!StringUtils.isNullOrEmpty(mSelectedEvent.getRemoteParty())){
					if(!MyAVSession.hasActiveSession()){
						mLasQuickAction.addActionItem(mAItemVoiceCall);
						mLasQuickAction.addActionItem(mAItemVideoCall);
					}
					mLasQuickAction.addActionItem(mAItemMessaging);
				}
				mLasQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);
				mLasQuickAction.show();
			}
		}
	};
	
	private final OnItemLongClickListener mOnItemListViewLongClickListener = new OnItemLongClickListener(){
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			return false;
		}
	};
	
	static class ScreenTabHistoryAdapter extends BaseAdapter implements Observer {
		private final ObservableList<HistoryEvent> mEvents;
		private final LayoutInflater mInflater;
		private final Handler mHandler;
		private final ScreenTabHistory mBaseScreen;
		
		private final static int TYPE_ITEM_AV = 0;
		private final static int TYPE_ITEM_SMS = 1;
		private final static int TYPE_ITEM_FILE_TRANSFER = 2;
		private final static int TYPE_COUNT = 3;
		private final static SimpleDateFormat durationFormat = new SimpleDateFormat("mm:ss");
		private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MMM dd hh:mm aaa");
		
		private ScreenTabHistoryAdapter(ScreenTabHistory baseSceen) {
			mBaseScreen = baseSceen;
			mHandler = new Handler();
			mInflater = LayoutInflater.from(mBaseScreen);
			mEvents = ServiceManager.getHistoryService().getObservableEvents();
			mEvents.addObserver(this);
		}
		
		@Override
		public int getViewTypeCount() {
			return TYPE_COUNT;
		}
		
		@Override
		public int getItemViewType(int position) {
			final HistoryEvent event = (HistoryEvent)getItem(position);
			if(event != null){
				switch(event.getMediaType()){
					case Audio:
					case AudioVideo:
						default:
						return TYPE_ITEM_AV;
					case FileTransfer:
						return TYPE_ITEM_FILE_TRANSFER;
					case SMS:
						return TYPE_ITEM_SMS;
				}
			}
			return TYPE_ITEM_AV;
		}
		
		@Override
		public int getCount() {
			return mEvents.getList().size();
		}

		@Override
		public Object getItem(int position) {
			return mEvents.getList().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public void update(Observable observable, Object data) {
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
			
			final HistoryEvent event = (HistoryEvent)getItem(position);
			if(event == null){
				return null;
			}
			if (view == null) {
				switch(event.getMediaType()){
					case Audio:
					case AudioVideo:
						view = mInflater.inflate(R.layout.screen_tab_history_item_av, null);
						default:
						break;
					case FileTransfer:
						break;
					case SMS:
						break;
				}
			}
			
			String remoteParty = event.getRemoteParty();
			if(remoteParty != null){
				final Contact contact = mBaseScreen.mContactService.getContactByUri(remoteParty);
				if(contact != null && contact.getDisplayName() != null){
					remoteParty = contact.getDisplayName();
				}
				else{
					remoteParty = UriUtils.getDisplayName(remoteParty);
				}
			}
			else{
				remoteParty = "(null)";
			}
			
			if(event != null){
				switch(event.getMediaType()){
					case Audio:
					case AudioVideo:
						final ImageView ivType = (ImageView)view.findViewById(R.id.screen_tab_history_item_av_imageView_type);
						final TextView tvRemote = (TextView)view.findViewById(R.id.screen_tab_history_item_av_textView_remote);
						final TextView tvDate = (TextView)view.findViewById(R.id.screen_tab_history_item_av_textView_date);
						final String duration = ScreenTabHistoryAdapter.durationFormat.format(new Date(event.getEndTime() - event.getStartTime()));
						final String date = ScreenTabHistoryAdapter.dateFormat.format(new Date(event.getStartTime()));
						tvDate.setText(String.format("%s (%s)", date, duration));
						tvRemote.setText(remoteParty);
						switch(event.getStatus()){
							case Outgoing:
								ivType.setImageResource(R.drawable.call_outgoing_45);
								break;
							case Incoming:
								ivType.setImageResource(R.drawable.call_incoming_45);
								break;
							case Failed:
							case Missed:
								ivType.setImageResource(R.drawable.call_missed_45);
								break;
						}
						break;
				}
			}
			
			return view;
		}
	}
}
