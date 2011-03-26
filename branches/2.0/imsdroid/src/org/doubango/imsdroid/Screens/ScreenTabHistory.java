package org.doubango.imsdroid.Screens;

import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.QuickAction.ActionItem;
import org.doubango.imsdroid.QuickAction.QuickAction;
import org.doubango.imsdroid.Utils.DateTimeUtils;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryAVCallEvent.HistoryEventAVFilter;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ScreenTabHistory extends BaseScreen {
	private static String TAG = ScreenTabHistory.class.getCanonicalName();
	
	private final INgnHistoryService mHistorytService;
	private final INgnSipService mSipService;
	
	private ScreenTabHistoryAdapter mAdapter;
	private ListView mListView;
	
	private final ActionItem mAItemVoiceCall;
	private final ActionItem mAItemVideoCall;
	private final ActionItem mAItemMessaging;
	
	private NgnHistoryEvent mSelectedEvent;
	private QuickAction mLasQuickAction;
	
	public ScreenTabHistory() {
		super(SCREEN_TYPE.TAB_HISTORY_T, TAG);
		
		mHistorytService = getEngine().getHistoryService();
		mSipService = getEngine().getSipService();
		
		mAItemVoiceCall = new ActionItem();
		mAItemVoiceCall.setTitle("Voice");
		mAItemVoiceCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSelectedEvent != null){
					ScreenAV.makeCall(mSelectedEvent.getRemoteParty(), NgnMediaType.Audio);
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
					ScreenAV.makeCall(mSelectedEvent.getRemoteParty(), NgnMediaType.AudioVideo);
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
				if(mSelectedEvent != null){
					// ScreenChat.startChat(mSelectedEvent.getRemoteParty());
					if(mLasQuickAction != null){
						mLasQuickAction.dismiss();
					}
				}
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_history);
		
		mAdapter = new ScreenTabHistoryAdapter(this);
		mListView = (ListView) findViewById(R.id.screen_tab_history_listView);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mOnItemListViewClickListener);
		mListView.setOnItemLongClickListener(mOnItemListViewLongClickListener);
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
			
			mSelectedEvent = (NgnHistoryEvent)parent.getItemAtPosition(position);
			if(mSelectedEvent != null){
				mLasQuickAction = new QuickAction(view);
				if(!NgnStringUtils.isNullOrEmpty(mSelectedEvent.getRemoteParty())){
					if(!NgnAVSession.hasActiveSession()){
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
	
	/**
	 * ScreenTabHistoryAdapter
	 */
	static class ScreenTabHistoryAdapter extends BaseAdapter implements Observer {
		private List<NgnHistoryEvent> mEvents;
		private final LayoutInflater mInflater;
		private final Handler mHandler;
		private final ScreenTabHistory mBaseScreen;
		
		private final static int TYPE_ITEM_AV = 0;
		private final static int TYPE_ITEM_SMS = 1;
		private final static int TYPE_ITEM_FILE_TRANSFER = 2;
		private final static int TYPE_COUNT = 3;
		
		ScreenTabHistoryAdapter(ScreenTabHistory baseSceen) {
			mBaseScreen = baseSceen;
			mHandler = new Handler();
			mInflater = LayoutInflater.from(mBaseScreen);
			mEvents = mBaseScreen.mHistorytService.getObservableEvents()
					.filter(new HistoryEventAVFilter());
			mBaseScreen.mHistorytService.getObservableEvents().addObserver(this);
		}
		
		@Override
		protected void finalize() throws Throwable {
			mBaseScreen.mHistorytService.getObservableEvents().deleteObserver(this);
			super.finalize();
		}
		
		@Override
		public int getViewTypeCount() {
			return TYPE_COUNT;
		}
		
		@Override
		public int getItemViewType(int position) {
			final NgnHistoryEvent event = (NgnHistoryEvent)getItem(position);
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
		public void update(Observable observable, Object data) {
			mEvents = mBaseScreen.mHistorytService.getObservableEvents()
					.filter(new HistoryEventAVFilter());
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
			
			final NgnHistoryEvent event = (NgnHistoryEvent)getItem(position);
			if(event == null){
				return null;
			}
			if (view == null) {
				switch(event.getMediaType()){
					case Audio:
					case AudioVideo:
						view = mInflater.inflate(R.layout.screen_tab_history_item_av, null);
						break;
					case FileTransfer:
					case SMS:
					default:
						Log.e(TAG, "Invalid media type");
						return null;
				}
			}
			
			String remoteParty = NgnUriUtils.getDisplayName(event.getRemoteParty());
			
			if(event != null){
				switch(event.getMediaType()){
					case Audio:
					case AudioVideo:
						final ImageView ivType = (ImageView)view.findViewById(R.id.screen_tab_history_item_av_imageView_type);
						final TextView tvRemote = (TextView)view.findViewById(R.id.screen_tab_history_item_av_textView_remote);
						final TextView tvDate = (TextView)view.findViewById(R.id.screen_tab_history_item_av_textView_date);
						final String date = DateTimeUtils.getFriendlyDateString(new Date(event.getStartTime()));
						tvDate.setText(date);
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
