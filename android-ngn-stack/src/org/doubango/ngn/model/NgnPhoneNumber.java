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

import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;

public class NgnPhoneNumber {	
	public static enum PhoneType{
	    CUSTOM, //Put the actual type in LABEL.
	    
	    HOME,
	    MOBILE,
	    WORK,
	    FAX_WORK,
	    FAX_HOME,
	    PAGER,
	    OTHER,
	    CALLBACK,
	    CAR,
	    COMPANY_MAIN,
	    ISDN,
	    MAIN,
	    OTHER_FAX,
	    RADIO,
	    TELEX,
	    TTY_TDD,
	    WORK_MOBILE,
	    WORK_PAGER,
	    ASSISTANT,
	    MMS
	}
	
	private final String mNumber;
	private final PhoneType mType;
	private final String mDescription;
	
	public NgnPhoneNumber(PhoneType type, String number, String description){
		mType = type;
		mNumber = number;
		mDescription = description;
	}
	
	public String getNumber(){
		return mNumber;
	}
	
	public PhoneType getPhoneType(){
		return mType;
	}
	
	public String getDescription(){
		return mDescription;
	}
	
	public static PhoneType fromAndroid2LocalType(int androidType){
		switch(androidType){
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
				default:
				return PhoneType.CUSTOM;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
				return PhoneType.HOME;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
				return PhoneType.MOBILE;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
				return PhoneType.WORK;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
				return PhoneType.FAX_WORK;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
				return PhoneType.FAX_HOME;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
				return PhoneType.PAGER;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
				return PhoneType.OTHER;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK:
				return PhoneType.CALLBACK;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_CAR:
				return PhoneType.CAR;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
				return PhoneType.MAIN;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_ISDN:
				return PhoneType.ISDN;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
				return PhoneType.MAIN;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
				return PhoneType.OTHER_FAX;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_RADIO:
				return PhoneType.RADIO;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_TELEX:
				return PhoneType.TELEX;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD:
				return PhoneType.TTY_TDD;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
				return PhoneType.WORK_MOBILE;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
				return PhoneType.WORK_PAGER;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT:
				return PhoneType.ASSISTANT;
			case android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MMS:
				return PhoneType.MMS;
		}
	}
	
	/**
	 * PhoneNumberFilterByType
	 */
	public static class PhoneNumberFilterByType implements NgnPredicate<NgnPhoneNumber>{
		private final PhoneType mType;
		PhoneNumberFilterByType(PhoneType type){
			mType = type;
		}
		@Override
		public boolean apply(NgnPhoneNumber phoneNumber) {
			return (phoneNumber != null && phoneNumber.getPhoneType() == mType);
		}
	}
	
	/**
	 * PhoneNumberFilterByAnyValid
	 */
	public static class PhoneNumberFilterByAnyValid implements NgnPredicate<NgnPhoneNumber>{
		PhoneNumberFilterByAnyValid(){
		}
		@Override
		public boolean apply(NgnPhoneNumber phoneNumber) {
			return (phoneNumber != null && !NgnStringUtils.isNullOrEmpty(phoneNumber.getNumber()));
		}
	}
}
