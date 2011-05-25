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
package org.doubango.ngn.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.model.NgnContact;
import org.doubango.ngn.model.NgnPhoneNumber;
import org.doubango.ngn.services.INgnContactService;
import org.doubango.ngn.utils.NgnCallbackFunc;
import org.doubango.ngn.utils.NgnListUtils;
import org.doubango.ngn.utils.NgnObservableList;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.tinyWRAP.SipUri;

import android.app.Activity;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;

/**@page NgnContactService_page Contact Service
 * The Contact service is used to retrieve contacts from the native address book.
 * 
 */

/**
 * Service used to retrieve contacts from the native address book.
 */
public class NgnContactService  extends NgnBaseService implements INgnContactService{
	private final static String TAG = NgnContactService.class.getCanonicalName();
	public final static String PARAM_ARG_CONTACT = "NgnContact";
	
	protected NgnObservableList<NgnContact> mContacts;
	protected boolean mLoading;
	protected boolean mReady;
	protected Looper mLocalContactObserverLooper;
	protected ContentObserver mLocalContactObserver;
	
	private NgnCallbackFunc<Object> mOnBeginLoadCallback;
	private NgnCallbackFunc<String> mOnNewPhoneNumberCallback;
	private NgnCallbackFunc<Object> mOnEndLoadCallback;
	
	public NgnContactService(){
		super();
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "starting...");
		
		if(mContacts == null){
			mContacts = getObservableContacts();
		}
		
		if(mLocalContactObserver == null && mLocalContactObserverLooper == null){
			new Thread(new Runnable() { // avoid locking calling thread
				@Override
				public void run() {
					Log.d(TAG, "Observer Looper enter()");
					Looper.prepare();
					mLocalContactObserverLooper = Looper.myLooper();
					final Handler handler = new Handler();
					handler.post(new Runnable() { // Observer thread. Will allow us to get notifications even if the application is on background
						@Override
						public void run() {
							mLocalContactObserver = new ContentObserver(handler) {
								@Override
								public void onChange(boolean selfChange) {
									super.onChange(selfChange);
									Log.d(TAG, "Native address book changed");
									load();
								}
							};
							NgnApplication.getContext().getContentResolver().registerContentObserver(CommonDataKinds.Phone.CONTENT_URI, 
									true, mLocalContactObserver);
						}
					});
					Looper.loop();// loop() until quit() is called
					Log.d(TAG, "Observer Looper exit()");
				}
			}).start();
		};
		
		return true;
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "stopping...");
		
		try{
			if(mLocalContactObserver != null){
				NgnApplication.getContext().getContentResolver().unregisterContentObserver(mLocalContactObserver);
				mLocalContactObserver = null;
			}
			if(mLocalContactObserverLooper != null){
				mLocalContactObserverLooper.quit();
				mLocalContactObserverLooper = null;
			}
		}
		catch(Exception e){
			Log.e(TAG, e.toString());
		}
		return true;
	}
	
	@Override
	public void setOnBeginLoadCallback(NgnCallbackFunc<Object> callback){
		mOnBeginLoadCallback = callback;
	}
	
	@Override
	public void setOnNewPhoneNumberCallback(NgnCallbackFunc<String> callback){
		mOnNewPhoneNumberCallback = callback;
	}
	
	@Override
	public void setOnEndLoadCallback(NgnCallbackFunc<Object> callback){
		mOnEndLoadCallback = callback;
	}
	
	@Override
	public boolean load(){
		/*new Thread(new Runnable() {
			@Override
			public void run() {
				load2();
			}
		}).start();*/
		return load2();
	}
	
	public boolean load2(){
		mLoading = true;
		boolean bOK = false;
		Cursor managedCursor = null;
		final List<NgnContact> contactsCopy = new ArrayList<NgnContact>();
		
		if(mOnBeginLoadCallback != null){
			mOnBeginLoadCallback.callback(this);
		}
		
		try{
			String phoneNumber, displayName, label;
			NgnContact contact = null;
			int id, type, photoId;
			final Activity activity = NgnEngine.getInstance().getMainActivity();
			final Resources res = NgnApplication.getContext().getResources();
			
			if(NgnApplication.getSDKVersion() >=5 && activity != null){
				/*synchronized(mContacts)*/{
					final String[] projectionContacts = new String[] { 
							android.provider.BaseColumns._ID,
							android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER,
							android.provider.ContactsContract.CommonDataKinds.Phone.TYPE,
							android.provider.ContactsContract.CommonDataKinds.Phone.LABEL,
							android.provider.ContactsContract.Contacts.DISPLAY_NAME,
							android.provider.ContactsContract.Contacts.PHOTO_ID,
							android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
							};
					
					managedCursor = activity.managedQuery(android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							projectionContacts, // Which columns to return
							null,       // Which rows to return (all rows)
							null,       // Selection arguments (none)
							// Put the results in ascending order by name
							"UPPER(" + android.provider.ContactsContract.Contacts.DISPLAY_NAME + ") ASC"
						);
					
					
					int indexPhoneContactId = managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
					int indexPhoneType = managedCursor .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
					int indexPhoneLabel = managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
					int indexPhoneNumber = managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
					int indexPhonePhotoId = managedCursor .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID);
					
					int indexContactDisplayName = managedCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
					
					 while(managedCursor.moveToNext()){
						 id = managedCursor.getInt(indexPhoneContactId);
						 type = managedCursor.getInt(indexPhoneType);
						 label = managedCursor.getString(indexPhoneLabel);
						 phoneNumber = managedCursor.getString(indexPhoneNumber);
						 photoId = managedCursor.getInt(indexPhonePhotoId);
						 
						 if(phoneNumber != null){
							 phoneNumber = phoneNumber.replace("-", "");
							if(NgnStringUtils.isNullOrEmpty(label)){
								final int resId = android.provider.ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(type);
								label = res.getString(resId);
							}
							 if(contact == null || contact.getId() != id){
								displayName = managedCursor.getString(indexContactDisplayName);
								contact = newContact(id, displayName);
								if(photoId != 0){
									 contact.setPhotoId(photoId);
								}
								contactsCopy.add(contact);
							 }
							 contact.addPhoneNumber(NgnPhoneNumber.fromAndroid2LocalType(type), phoneNumber, label);
							 if(mOnNewPhoneNumberCallback != null){
								 mOnNewPhoneNumberCallback.callback(phoneNumber);
							 }
						 }
					 }
					 
					 mLoading = false;
					 mReady = true;
					 bOK = true;
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			
			mLoading = false;
			mReady = false;
		}
		finally{
			if(managedCursor != null){
				managedCursor.close();
			}
		}
		
		if(bOK){
			synchronized(mContacts){
				mContacts.clear();
				mContacts.add(contactsCopy);
			}
		}
		
		if(mOnEndLoadCallback != null){
			mOnEndLoadCallback.callback(this);
		}
		
		return bOK;
	}

	@Override
	public boolean isLoading() {
		return mLoading;
	}

	@Override
	public boolean isReady(){
		return mReady;
	}
	
	@Override
	public NgnObservableList <NgnContact> getObservableContacts() {
		if(mContacts == null){
			mContacts = new NgnObservableList<NgnContact>(true);
		}
		return mContacts;
	}

	@Override
	public NgnContact newContact(int id, String displayName){
		return new NgnContact(id, displayName);
	}
	
	@Override
	public NgnContact getContactByUri(String uri) {
		final SipUri sipUri = new SipUri(uri);
		NgnContact contact = null;
		if(sipUri.isValid()){
			contact = getContactByPhoneNumber(sipUri.getUserName());
		}
		sipUri.delete();
		return contact;
	}

	@Override
	public NgnContact getContactByPhoneNumber(String anyPhoneNumber) {
		return NgnListUtils.getFirstOrDefault(mContacts.getList(), new NgnContact.ContactFilterByAnyPhoneNumber(anyPhoneNumber));
	}
	
	/**
	 * ContactFilterById
	 */
	static class ContactFilterById implements NgnPredicate<NgnContact>{
		private final int mId;
		ContactFilterById(int id){
			mId = id;
		}
		@Override
		public boolean apply(NgnContact contact) {
			return (contact != null && contact.getId() == mId);
		}
	}
}
