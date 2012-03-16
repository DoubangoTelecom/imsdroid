/* Copyright (C) 2012, Doubango Telecom.
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

import java.util.Date;

public class NgnDeviceInfo {
	private String mLang;
	private String mCountry;
	private Date mDate;
	private Orientation mOrientation;

	public enum Orientation {
		PORTRAIT,
		LANDSCAPE
	};
	
	public NgnDeviceInfo(String lang, String country, Date date, Orientation orientation){
		mLang = lang;
		mCountry = country;
		mDate = date;
		mOrientation = orientation;
	}
	
	public NgnDeviceInfo(){
		this(null, null, null, Orientation.PORTRAIT);
	}
	
	public String getLang(){
		return mLang;
	}
	
	public void setLang(String lang){
		mLang = lang;
	}
	
	public String getCountry(){
		return mCountry;
	}
	
	public void setCountry(String country){
		mCountry = country;
	}
	
	public Date getDate(){
		return mDate;
	}
	
	public void setDate(Date date){
		mDate = date;
	}
	
	public Orientation getOrientation(){
		return mOrientation;
	}
	
	public void setOrientation(Orientation orientation){
		mOrientation = orientation;
	}
}
