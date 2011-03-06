package org.doubango.imsdroid.Model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.Model.PhoneNumber.PhoneNumberFilterByAnyValid;
import org.doubango.imsdroid.Model.PhoneNumber.PhoneType;
import org.doubango.imsdroid.Utils.ListUtils;
import org.doubango.imsdroid.Utils.ObservableObject;
import org.doubango.imsdroid.Utils.Predicate;
import org.doubango.imsdroid.Utils.StringUtils;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;

public class Contact extends ObservableObject{
	
	private final int mId;
	private String mDisplayName;
	private final List<PhoneNumber> mPhoneNumbers;
	private int mPhotoId;
	private Bitmap mPhoto;
	
	public Contact(int id, String displayName){
		super();
		mId = id;
		mDisplayName = displayName;
		mPhoneNumbers = new ArrayList<PhoneNumber>();
	}
	
	public int getId(){
		return mId;
	}
	
	public List<PhoneNumber> getPhoneNumbers(){
		return mPhoneNumbers;
	}
	
	public String getPrimaryNumber(){
		final PhoneNumber primaryNumber = ListUtils.getFirstOrDefault(mPhoneNumbers, new PhoneNumberFilterByAnyValid());
		if(primaryNumber != null){
			return primaryNumber.getNumber();
		}
		return null;
	}
	
	public void addPhoneNumber(PhoneType type, String number, String description){
		final PhoneNumber phoneNumber = new PhoneNumber(type, number, description);
		if(type == PhoneType.MOBILE){
			mPhoneNumbers.add(0, phoneNumber);
		}
		else{
			mPhoneNumbers.add(phoneNumber);
		}
	}
	
	public void setDisplayName(String displayName){
		mDisplayName = displayName;
		super.setChangedAndNotifyObservers(displayName);
	}
	
	public String getDisplayName(){
		return StringUtils.isNullOrEmpty(mDisplayName) ? "(null)" : mDisplayName;
	}
	
	public void setPhotoId(int photoId){
		mPhotoId = photoId;
	}
	
	public Bitmap getPhoto(){
		if(mPhotoId != 0 && mPhoto == null){
			try{
				Uri contactPhotoUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, mId);
			    InputStream photoDataStream = Contacts.openContactPhotoInputStream(IMSDroid.getContext().getContentResolver(), contactPhotoUri);
			    if(photoDataStream != null){
			    	mPhoto = BitmapFactory.decodeStream(photoDataStream);
			    	photoDataStream.close();
			    }
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return mPhoto;
	}
	
	/**
	 * PhoneNumberFilterByType
	 */
	public static class ContactFilterByAnyPhoneNumber implements Predicate<Contact>{
		private final String mPhoneNumber;
		public ContactFilterByAnyPhoneNumber(String phoneNumber){
			mPhoneNumber = phoneNumber;
		}
		@Override
		public boolean apply(Contact contact) {
			for(PhoneNumber phoneNumer : contact.getPhoneNumbers()){
				if(StringUtils.equals(phoneNumer.getNumber(), mPhoneNumber, false)){
					return true;
				}
			}
			return false;
		}
	}
}