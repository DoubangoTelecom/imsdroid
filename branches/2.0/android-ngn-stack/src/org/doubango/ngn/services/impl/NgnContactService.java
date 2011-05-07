package org.doubango.ngn.services.impl;

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
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
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
	
	protected final NgnObservableList<NgnContact> mContacts;
	protected boolean mLoading;
	protected boolean mReady;
	protected Looper mLocalContactObserverLooper;
	protected ContentObserver mLocalContactObserver;
	
	private NgnCallbackFunc<Object> mOnBeginLoadCallback;
	private NgnCallbackFunc<String> mOnNewPhoneNumberCallback;
	private NgnCallbackFunc<Object> mOnEndLoadCallback;
	
	public NgnContactService(){
		super();
		
		mContacts = new NgnObservableList<NgnContact>(true);
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "starting...");
		
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
		mLoading = true;
		boolean bOK = false;
		Cursor managedCursor = null;
		
		if(mOnBeginLoadCallback != null){
			mOnBeginLoadCallback.callback(this);
		}
		
		try{
			String phoneNumber, displayName, label;
			NgnContact contact;
			int id, type, photoId;
			final Activity activity = NgnEngine.getInstance().getMainActivity();
			
			if(NgnApplication.getSDKVersion() >=5 && activity != null){
				synchronized(mContacts){
					mContacts.clear();
					final String[] projection = new String[] { 
							android.provider.BaseColumns._ID,
							android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER,
							android.provider.ContactsContract.CommonDataKinds.Phone.TYPE,
							android.provider.ContactsContract.CommonDataKinds.Phone.LABEL,
							android.provider.ContactsContract.Contacts.DISPLAY_NAME,
							android.provider.ContactsContract.Contacts.PHOTO_ID,
							android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
							};
					managedCursor = activity.managedQuery(android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							projection, // Which columns to return
							null,       // Which rows to return (all rows)
							null,       // Selection arguments (none)
							// Put the results in ascending order by name
							"UPPER("+android.provider.ContactsContract.Contacts.DISPLAY_NAME + ") ASC"
						);
					
						
					 while(managedCursor.moveToNext()){
						 id = managedCursor.getInt(managedCursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
						 type = managedCursor.getInt(managedCursor .getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.TYPE));
						 label = managedCursor.getString(managedCursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.LABEL));
						 phoneNumber = managedCursor.getString(managedCursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER));
						 photoId = managedCursor.getInt(managedCursor .getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.PHOTO_ID));
						 contact = NgnListUtils.getFirstOrDefault(mContacts.getList(), new ContactFilterById(id));
						 
						 if(NgnStringUtils.isNullOrEmpty(label)){
							 final int resId = android.provider.ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(type);
							 label = NgnApplication.getContext().getResources().getString(resId);
						 }
						 if(phoneNumber != null){
							 phoneNumber = phoneNumber.replace("-", "");
							 if(contact == null){
								displayName = managedCursor.getString(managedCursor.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME));
								contact = new NgnContact(id, displayName);
								mContacts.add(contact);
							 }
							 contact.addPhoneNumber(NgnPhoneNumber.fromAndroid2LocalType(type), phoneNumber, label);
							 if(photoId != 0){
								 contact.setPhotoId(photoId);
							 }
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
	public NgnObservableList<NgnContact> getObservableContacts() {
		return mContacts;
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
