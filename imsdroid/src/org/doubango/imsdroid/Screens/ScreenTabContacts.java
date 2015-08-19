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

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.doubango.imsdroid.Engine;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.QuickAction.ActionItem;
import org.doubango.imsdroid.QuickAction.QuickAction;
import org.doubango.imsdroid.Utils.SeparatedListAdapter;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnContact;
import org.doubango.ngn.services.INgnContactService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnGraphicsUtils;
import org.doubango.ngn.utils.NgnObservableList;
import org.doubango.ngn.utils.NgnStringUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ScreenTabContacts extends BaseScreen {
	private static String TAG = ScreenTabContacts.class.getCanonicalName();
	private static final int SELECT_CONTENT = 1;
	  
	@SuppressWarnings("unused")
	private final INgnContactService mContactService;
	private final INgnSipService mSipService;
	private MySeparatedListAdapter mAdapter;
	private ListView mListView;
	
	private final ActionItem mAItemVoiceCall;
	private final ActionItem mAItemVideoCall;
	private final ActionItem mAItemChat;
	private final ActionItem mAItemSMS;
	private final ActionItem mAItemShare;
	
	private NgnContact mSelectedContact;
	private QuickAction mLasQuickAction;
	
	public ScreenTabContacts() {
		super(SCREEN_TYPE.TAB_CONTACTS, TAG);
		
		mContactService = getEngine().getContactService();
		mSipService = getEngine().getSipService();
		
		mAItemVoiceCall = new ActionItem();
		mAItemVoiceCall.setTitle("Voice");
		mAItemVoiceCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSelectedContact != null){
					ScreenAV.makeCall(mSelectedContact.getPrimaryNumber(), NgnMediaType.Audio);
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
					ScreenAV.makeCall(mSelectedContact.getPrimaryNumber(), NgnMediaType.AudioVideo);
					if(mLasQuickAction != null){
						mLasQuickAction.dismiss();
					}
				}
			}
		});
		
		mAItemChat = new ActionItem();
		mAItemChat.setTitle("Chat");
		mAItemChat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ScreenChat.startChat(mSelectedContact.getPrimaryNumber(), false);
				if(mLasQuickAction != null){
					mLasQuickAction.dismiss();
				}
			}
		});
		
		mAItemSMS = new ActionItem();
		mAItemSMS.setTitle("SMS");
		mAItemSMS.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ScreenChat.startChat(mSelectedContact.getPrimaryNumber(), true);
				if(mLasQuickAction != null){
					mLasQuickAction.dismiss();
				}
			}
		});
		
		mAItemShare = new ActionItem();
		mAItemShare.setTitle("Share");
		mAItemShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSelectedContact != null){
					Intent intent = new Intent();
                    intent.setType("*/*")
                    	.addCategory(Intent.CATEGORY_OPENABLE)
                    	.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select content"), SELECT_CONTENT);   
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
		setContentView(R.layout.screen_tab_contacts);
		
		mListView = (ListView) findViewById(R.id.screen_tab_contacts_listView);
	    mAdapter = new MySeparatedListAdapter(this);
	    
	    mListView.setAdapter(mAdapter);
	    mListView.setOnItemClickListener(mOnItemListViewClickListener);
	    mListView.setOnItemLongClickListener(mOnItemListViewLongClickListener);
	    registerForContextMenu(mListView);
	    
	    mAItemVoiceCall.setIcon(getResources().getDrawable(R.drawable.voice_call_25));
		mAItemVideoCall.setIcon(getResources().getDrawable(R.drawable.visio_call_25));
		mAItemChat.setIcon(getResources().getDrawable(R.drawable.chat_25));
		mAItemSMS.setIcon(getResources().getDrawable(R.drawable.sms_25));
		mAItemShare.setIcon(getResources().getDrawable(R.drawable.image_gallery_25));  
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case SELECT_CONTENT:
					if (mSelectedContact != null) {
						Uri selectedContentUri = data.getData();
						String selectedContentPath = super.getPath(selectedContentUri);
						ScreenFileTransferView.sendFile(mSelectedContact.getPrimaryNumber(), selectedContentPath);
					}
					break;
			}
		}
	}
	private final OnItemClickListener mOnItemListViewClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mSelectedContact = (NgnContact)parent.getItemAtPosition(position);
			if(mSelectedContact != null){
				mLasQuickAction = new QuickAction(view);
				if(!NgnStringUtils.isNullOrEmpty(mSelectedContact.getPrimaryNumber())){
					mLasQuickAction.addActionItem(mAItemVoiceCall);
					mLasQuickAction.addActionItem(mAItemVideoCall);
					mLasQuickAction.addActionItem(mAItemChat);
					mLasQuickAction.addActionItem(mAItemSMS);
					mLasQuickAction.addActionItem(mAItemShare);
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
			
			mSelectedContact = (NgnContact)parent.getItemAtPosition(position);
			if(mSelectedContact != null){
				mLasQuickAction = new QuickAction(view);
				if(!NgnStringUtils.isNullOrEmpty(mSelectedContact.getPrimaryNumber())){
					mLasQuickAction.addActionItem(mAItemVoiceCall);
					mLasQuickAction.addActionItem(mAItemVideoCall);
					mLasQuickAction.addActionItem(mAItemChat);
					mLasQuickAction.addActionItem(mAItemSMS);
					mLasQuickAction.addActionItem(mAItemShare);
				}
				mLasQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);
				mLasQuickAction.show();
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
		private final NgnObservableList<NgnContact> mContacts;
		
		
		private MySeparatedListAdapter(Context context){
			super(context);
			mContext = context;
			mHandler = new Handler();
			mInflater = LayoutInflater.from(mContext);
			mContacts = Engine.getInstance().getContactService().getObservableContacts();
			mContacts.addObserver(this);
			
			updateSections();
			notifyDataSetChanged();
		}
		
		@Override
		protected void finalize() throws Throwable {
			Engine.getInstance().getContactService().getObservableContacts().deleteObserver(this);
			super.finalize();
		}
		
		private void updateSections(){
			clearSections();
			synchronized(mContacts){
				List<NgnContact> contacts = mContacts.getList();
				String lastGroup = "$", displayName;
				ScreenTabContactsAdapter lastAdapter = null;
				
				for(NgnContact contact : contacts){
					displayName = contact.getDisplayName();
					if(NgnStringUtils.isNullOrEmpty(displayName)){
						continue;
					}
					final String group = displayName.substring(0, 1).toUpperCase();
					if(!group.equalsIgnoreCase(lastGroup)){
						lastGroup = group;
						lastAdapter = new ScreenTabContactsAdapter(mContext, lastGroup);
						addSection(lastGroup, lastAdapter);
					}
					
					if(lastAdapter != null){
						lastAdapter.addContact(contact);
					}
				}
			}
		}
		
		@Override
		protected View getHeaderView(int position, View convertView, ViewGroup parent, final Adapter adapter) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.view_list_header, parent, false);
			}
			TextView tvDisplayName = (TextView)convertView.findViewById(R.id.view_list_header_title);
			tvDisplayName.setText(((ScreenTabContactsAdapter)adapter).getSectionText());
			return convertView;
		}

		@Override
		public void update(Observable observable, Object data) {
			//if(Thread.currentThread() == Looper.getMainLooper().getThread()){
			//	updateSections();
			//	notifyDataSetChanged();
			//}
			//else{
				mHandler.post(new Runnable(){
					@Override
					public void run() {
						updateSections();
						notifyDataSetChanged();
					}
				});
			//}
		}
	}
	
	/**
	 * ScreenTabContactsAdapter
	 */
	static class ScreenTabContactsAdapter extends BaseAdapter {
		private final LayoutInflater mInflater;
		
		private final Context mContext;
		private List<NgnContact> mContacts;
		private final String mSectionText;
		
		private ScreenTabContactsAdapter(Context context, String sectionText) {
			mContext = context;
			mSectionText = sectionText;
			mInflater = LayoutInflater.from(mContext);
		}

		public String getSectionText(){
			return mSectionText;
		}
		
		public void addContact(NgnContact contact){
			if(mContacts == null){
				mContacts = new ArrayList<NgnContact>();
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
			final NgnContact contact = (NgnContact)getItem(position);
			
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
						ivAvatar.setImageBitmap(NgnGraphicsUtils.getResizedBitmap(avatar, NgnGraphicsUtils.getSizeInPixel(48), NgnGraphicsUtils.getSizeInPixel(48)));
					}
				}
			}
			
			return view;
		}
	}
}