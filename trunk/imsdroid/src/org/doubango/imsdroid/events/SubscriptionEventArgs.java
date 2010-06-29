/*
* Copyright (C) 2010 Mamadou Diop.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango.org>
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
*
*/

package org.doubango.imsdroid.events;

public class SubscriptionEventArgs  extends EventArgs{

	private final SubscriptionEventTypes type;
	private final short sipCode;
	private final String phrase;
	private final byte[] content;
	private final String contentType;
	
	public SubscriptionEventArgs(SubscriptionEventTypes type, short sipCode, String phrase, byte[] content, String contentType){
		super();
		
		this.type = type;
		this.sipCode = sipCode;
		this.phrase = phrase;
		this.content = content;
		this.contentType = contentType;
	}
	
	public SubscriptionEventTypes getType(){
		return this.type;
	}
	
	public short getSipCode(){
		return this.sipCode;
	}
	
	public String getPhrase(){
		return this.phrase;
	}
	
	public byte[] getContent(){
		return this.content;
	}
	
	public String getContentType(){
		return this.contentType;
	}
}
