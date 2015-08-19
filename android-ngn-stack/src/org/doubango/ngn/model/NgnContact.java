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
package org.doubango.ngn.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.model.NgnEmail.EmailType;
import org.doubango.ngn.model.NgnPhoneNumber.PhoneNumberFilterByAnyValid;
import org.doubango.ngn.model.NgnPhoneNumber.PhoneType;
import org.doubango.ngn.utils.NgnListUtils;
import org.doubango.ngn.utils.NgnObservableObject;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;

/**
 * Contact class defining an entity from the native address book or XCAP server.
 */
public class NgnContact extends NgnObservableObject{
	
	private final int mId;
	private String mDisplayName;
	private final List<NgnPhoneNumber> mPhoneNumbers;
	private List<NgnEmail> mEmails;
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
	 * Gets all emails associated to this contact
	 * @return list of all emails associated to this contact
	 */
	public List<NgnEmail> getEmails(){
		// For performance reasons we only query for emails if requested
		final Activity activity = NgnEngine.getInstance().getMainActivity();
		if(mEmails == null && activity != null){
			mEmails = new ArrayList<NgnEmail>();
			Cursor emailCursor = activity.managedQuery( 
	                ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
	                null,
	                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", 
	                new String[]{ Integer.toString(mId) }, null); 
			
			while (emailCursor.moveToNext()) { 
				 String emailValue = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
	             if(!NgnStringUtils.isNullOrEmpty(emailValue)){
	            	 String description = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME));
	            	 this.addEmail(EmailType.None, emailValue, description);
	             }
			} 
			emailCursor.close();
		}
		
		return mEmails;
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
	 * Attach a new email to this contact
	 * @param type the type of the email to add
	 * @param value the actual value of the email
	 * @param description a short description
	 */
	public void addEmail(NgnEmail.EmailType type, String value, String description){
		mEmails.add(new NgnEmail(type, value, description));
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
}
