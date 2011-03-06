
package org.doubango.imsdroid.Services.Impl;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Model.Contact;
import org.doubango.imsdroid.Model.PhoneNumber;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.Utils.ListUtils;
import org.doubango.imsdroid.Utils.ObservableList;
import org.doubango.imsdroid.Utils.Predicate;
import org.doubango.imsdroid.Utils.StringUtils;
import org.doubango.tinyWRAP.SipUri;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;


public class ContactService  extends BaseService implements IContactService{
	private final static String TAG = ContactService.class.getCanonicalName();
	
	private final ObservableList<Contact> mContacts;
	private boolean mLoading;
	private Looper mLocalContactObserverLooper;
	private ContentObserver mLocalContactObserver;
	
	public ContactService(){
		super();
		
		mContacts = new ObservableList<Contact>(true);
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
							IMSDroid.getContext().getContentResolver().registerContentObserver(CommonDataKinds.Phone.CONTENT_URI, 
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
				IMSDroid.getContext().getContentResolver().unregisterContentObserver(mLocalContactObserver);
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
	public boolean load() {
		mLoading = true;
		
		try{
			String phoneNumber, displayName, label;
			Contact contact;
			int id, type, photoId;
			
			if(IMSDroid.getSDKVersion() >=5){
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
					Cursor managedCursor = ServiceManager.getMainActivity().managedQuery(android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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
						 contact = ListUtils.getFirstOrDefault(mContacts.getList(), new ContactFilterById(id));
						 
						 if(StringUtils.isNullOrEmpty(label)){
							 final int resId = android.provider.ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(type);
							 label = IMSDroid.getContext().getResources().getString(resId);
						 }
						 if(phoneNumber != null){
							 phoneNumber = phoneNumber.replace("-", "");
							 if(contact == null){
								displayName = managedCursor.getString(managedCursor.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME));
								contact = new Contact(id, displayName);
								mContacts.add(contact);
							 }
							 contact.addPhoneNumber(PhoneNumber.fromAndroid2LocalType(type), phoneNumber, label);
							 if(photoId != 0){
								 contact.setPhotoId(photoId);
							 }
						 }
					 }
					 
					 mLoading = false;
					 return true;
				}
			}
			return false;
		}
		catch(java.lang.IllegalStateException e){
			e.printStackTrace();
			
			mLoading = false;
			return false;
		}
	}

	@Override
	public boolean isLoading() {
		return mLoading;
	}
	
	@Override
	public ObservableList<Contact> getObservableContacts() {
		return mContacts;
	}

	@Override
	public Contact getContactByUri(String uri) {
		final SipUri sipUri = new SipUri(uri);
		Contact contact = null;
		if(sipUri.isValid()){
			contact = getContactByPhoneNumber(sipUri.getUserName());
		}
		sipUri.delete();
		return contact;
	}

	@Override
	public Contact getContactByPhoneNumber(String anyPhoneNumber) {
		return ListUtils.getFirstOrDefault(mContacts.getList(), new Contact.ContactFilterByAnyPhoneNumber(anyPhoneNumber));
	}
	
	/**
	 * ContactFilterById
	 */
	static class ContactFilterById implements Predicate<Contact>{
		private final int mId;
		ContactFilterById(int id){
			mId = id;
		}
		@Override
		public boolean apply(Contact contact) {
			return (contact != null && contact.getId() == mId);
		}
	}
}

