package org.doubango.ngn.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.model.NgnPhoneNumber.PhoneNumberFilterByAnyValid;
import org.doubango.ngn.model.NgnPhoneNumber.PhoneNumberFilterOnlyWiPhone;
import org.doubango.ngn.model.NgnPhoneNumber.PhoneType;
import org.doubango.ngn.utils.NgnListUtils;
import org.doubango.ngn.utils.NgnObservableObject;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;

/**
 * Contact class defining an entity from the native address book or XCAP server.
 */
public class NgnContact extends NgnObservableObject{
	
	private final int mId;
	private String mDisplayName;
	private final List<NgnPhoneNumber> mPhoneNumbers;
	private int mPhotoId;
	private Bitmap mPhoto;
	
	/**
	 * Creates new address book
	 * @param id a unique id defining this contact
	 * @param displayName the contact's display name
	 */
	public NgnContact(int id, String displayName){
		super();
		mId = id;
		mDisplayName = displayName;
		mPhoneNumbers = new ArrayList<NgnPhoneNumber>();
	}
	
	/**
	 * Gets the id of the contact
	 * @return a unique id defining this contact
	 */
	public int getId(){
		return mId;
	}
	
	/**
	 * Gets all phone numbers associated to this contact
	 * @return list of all numbers associated to this contact
	 */
	public List<NgnPhoneNumber> getPhoneNumbers(){
		return mPhoneNumbers;
	}
	
	/**
	 * Gets the default/primary phone number value. Most likely the mobile number
	 * @return the contact's primary number
	 */
	public String getPrimaryNumber(){
		final NgnPhoneNumber primaryNumber = NgnListUtils.getFirstOrDefault(mPhoneNumbers, new PhoneNumberFilterByAnyValid());
		if(primaryNumber != null){
			return primaryNumber.getNumber();
		}
		return null;
	}
	
	public String getWiPhoneNumber(){
		final NgnPhoneNumber primaryNumber = NgnListUtils.getFirstOrDefault(mPhoneNumbers, new PhoneNumberFilterOnlyWiPhone());
		if(primaryNumber != null){
			return primaryNumber.getNumber();
		}
		return null;
	}
	
	/**
	 * Attach a new phone number to this contact
	 * @param type the type of the phone number to add
	 * @param number the actual value of the phone number
	 * @param description a short description
	 */
	public void addPhoneNumber(PhoneType type, String number, String description){
		final NgnPhoneNumber phoneNumber = new NgnPhoneNumber(type, number, description);
		if(type == PhoneType.MOBILE){
			mPhoneNumbers.add(0, phoneNumber);
		}
		else{
			mPhoneNumbers.add(phoneNumber);
		}
	}
	
	/**
	 * Sets the contact's display name value
	 * @param displayName the new display name to assign to the contact
	 */
	public void setDisplayName(String displayName){
		mDisplayName = displayName;
		super.setChangedAndNotifyObservers(displayName);
	}
	
	/**
	 * Gets the contact's display name
	 * @return the contact's display name
	 */
	public String getDisplayName(){
		return NgnStringUtils.isNullOrEmpty(mDisplayName) ? NgnStringUtils.nullValue() : mDisplayName;
	}
	
	public void setPhotoId(int photoId){
		mPhotoId = photoId;
	}
	
	/**
	 * Gets the photo associated to this contact
	 * @return a bitmap representing the contact's photo
	 */
	public Bitmap getPhoto(){
		if(mPhotoId != 0 && mPhoto == null){
			try{
				Uri contactPhotoUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, mId);
			    InputStream photoDataStream = Contacts.openContactPhotoInputStream(NgnApplication.getContext().getContentResolver(), contactPhotoUri);
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
	
	public static class ContactFilterByAnyPhoneNumber implements NgnPredicate<NgnContact>{
		private final String mPhoneNumber;
		public ContactFilterByAnyPhoneNumber(String phoneNumber){
			mPhoneNumber = phoneNumber;
		}
		@Override
		public boolean apply(NgnContact contact) {
			for(NgnPhoneNumber phoneNumer : contact.getPhoneNumbers()){
				if(NgnStringUtils.equals(phoneNumer.getNumber(), mPhoneNumber, false)){
					return true;
				}
			}
			return false;
		}
	}
	
	public static class ContactFilterByWiPhoneNumber implements NgnPredicate<NgnContact>{
		private final String mWiPhoneNumber;
		public ContactFilterByWiPhoneNumber(String wiPhoneNumber){
			mWiPhoneNumber = wiPhoneNumber;
		}
		@Override
		public boolean apply(NgnContact contact) {
			return (contact != null && NgnStringUtils.equals(contact.getWiPhoneNumber(), mWiPhoneNumber, false));
		}
	}
}
