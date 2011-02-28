package org.doubango.imsdroid.Screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Media.MediaType;
import org.doubango.imsdroid.Model.Contact;
import org.doubango.imsdroid.QuickAction.ActionItem;
import org.doubango.imsdroid.QuickAction.QuickAction;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sip.MyAVSession;
import org.doubango.imsdroid.Utils.GraphicsUtils;
import org.doubango.imsdroid.Utils.ObservableList;
import org.doubango.imsdroid.Utils.SeparatedListAdapter;
import org.doubango.imsdroid.Utils.StringUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ScreenTabContacts extends BaseScreen {
	private static String TAG = ScreenTabContacts.class.getCanonicalName();
	  
	@SuppressWarnings("unused")
	private final IContactService mContactService;
	private final ISipService mSipService;
	private MySeparatedListAdapter mAdapter;
	private ListView mListView;
	
	private final ActionItem mAItemVoiceCall;
	private final ActionItem mAItemVideoCall;
	private final ActionItem mAItemMessaging;
	
	private Contact mSelectedContact;
	private QuickAction mLasQuickAction;
	
	public ScreenTabContacts() {
		super(SCREEN_TYPE.TAB_CONTACTS, TAG);
		
		mContactService = ServiceManager.getContactService();
		mSipService = ServiceManager.getSipService();
		
		mAItemVoiceCall = new ActionItem();
		mAItemVoiceCall.setTitle("Voice");
		mAItemVoiceCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSelectedContact != null){
					ScreenAV.makeCall(mSelectedContact.getPrimaryNumber(), MediaType.Audio);
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
				if(mSelectedContact != null){
					ScreenAV.makeCall(mSelectedContact.getPrimaryNumber(), MediaType.AudioVideo);
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
		setContentView(R.layout.screen_tab_contacts);
		
		mListView = (ListView) findViewById(R.id.screen_tab_contacts_listView);
	    mAdapter = new MySeparatedListAdapter(this);
	    
	    mListView.setAdapter(mAdapter);
	    mListView.setOnItemClickListener(mOnItemListViewClickListener);
	    mListView.setOnItemLongClickListener(mOnItemListViewLongClickListener);
	    registerForContextMenu(mListView);
	    
	    // mAItemVoiceCall.setIcon(getResources().getDrawable(R.drawable.call_onscreen_24));
	}
	
	private final OnItemClickListener mOnItemListViewClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mSelectedContact = (Contact)parent.getItemAtPosition(position);
			if(mSelectedContact != null){
				mLasQuickAction = new QuickAction(view);
				if(!StringUtils.isNullOrEmpty(mSelectedContact.getPrimaryNumber())){
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
			if(!mSipService.isRegistered()){
				Log.e(TAG, "Not registered yet");
				return true;
			}
			
			
			return true;
		}
	};
	
	/**
	 * MySeparatedListAdapter
	 */
	static class MySeparatedListAdapter extends SeparatedListAdapter implements Observer{
		private final LayoutInflater mInflater;
		private final Context mContext;
		private final Handler mHandler;
		private final ObservableList<Contact> mContacts;
		
		
		public MySeparatedListAdapter(Context context){
			super(context);
			mContext = context;
			mHandler = new Handler();
			mInflater = LayoutInflater.from(mContext);
			mContacts = ServiceManager.getContactService().getObservableContacts();
			mContacts.addObserver(this);
			
			updateSections();
			notifyDataSetChanged();
		}
		
		@Override
		protected void finalize() throws Throwable {
			ServiceManager.getContactService().getObservableContacts().deleteObserver(this);
			super.finalize();
		}
		
		private void updateSections(){
			clearSections();
			List<Contact> contacts = mContacts.getList();
			String lastGroup = "$", displayName;
			ScreenTabContactsAdapter lastAdapter = null;
			
			for(Contact contact : contacts){
				displayName = contact.getDisplayName();
				if((!StringUtils.isNullOrEmpty(displayName) && !displayName.startsWith(lastGroup))){
					lastGroup = displayName.substring(0, 1);
					lastAdapter = new ScreenTabContactsAdapter(mContext, lastGroup);
					addSection(lastGroup, lastAdapter);
				}
				
				if(lastAdapter != null){
					lastAdapter.addContact(contact);
				}
			}
		}
		
		@Override
		protected View getHeaderView(int position, View convertView, ViewGroup parent, final Adapter adapter) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.screen_tab_contacts_group_item, parent, false);
			}
			TextView tvDisplayName = (TextView)convertView.findViewById(R.id.list_header_title);
			tvDisplayName.setText(((ScreenTabContactsAdapter)adapter).getSectionText());
			return convertView;
		}

		@Override
		public void update(Observable observable, Object data) {
			updateSections();
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
	}
	
	/**
	 * ScreenTabContactsAdapter
	 */
	static class ScreenTabContactsAdapter extends BaseAdapter {
		private final LayoutInflater mInflater;
		
		private final Context mContext;
		private List<Contact> mContacts;
		private final String mSectionText;
		
		private ScreenTabContactsAdapter(Context context, String sectionText) {
			mContext = context;
			mSectionText = sectionText;
			mInflater = LayoutInflater.from(mContext);
		}

		public String getSectionText(){
			return mSectionText;
		}
		
		public void addContact(Contact contact){
			if(mContacts == null){
				mContacts = new ArrayList<Contact>();
			}
			mContacts.add(contact);
		}
		
		@Override
		public int getCount() {
			return mContacts==null ? 0: mContacts.size();
		}

		@Override
		public Object getItem(int position) {
			if(mContacts != null && mContacts.size()>position){
				return mContacts.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			
			if (view == null) {
				view = mInflater.inflate(R.layout.screen_tab_contacts_contact_item, null);
			}
			final Contact contact = (Contact)getItem(position);
			
			if(contact != null){
				final ImageView ivAvatar = (ImageView) view.findViewById(R.id.screen_tab_contacts_item_imageView_avatar);
				if(ivAvatar != null){
					final TextView tvDisplayName = (TextView) view.findViewById(R.id.screen_tab_contacts_item_textView_displayname);
					tvDisplayName.setText(contact.getDisplayName());
					final Bitmap avatar = contact.getPhoto();
					if(avatar == null){
						ivAvatar.setImageResource(R.drawable.avatar_48);
					}
					else{
						ivAvatar.setImageBitmap(GraphicsUtils.getResizedBitmap(avatar, GraphicsUtils.getSizeInPixel(48), GraphicsUtils.getSizeInPixel(48)));
					}
				}
			}
			
			return view;
		}
	}
}
