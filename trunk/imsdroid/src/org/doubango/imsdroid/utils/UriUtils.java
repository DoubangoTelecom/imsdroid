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

package org.doubango.imsdroid.utils;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Group;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.tinyWRAP.SipUri;

public class UriUtils {

	private final static long MAX_PHONE_NUMBER = 1000000000000L;
	private final static String INVALID_SIP_URI = "sip:invalid@open-ims.tes";
	
	public static String getDisplayName(String uri){
		String displayname = null;
		Group.Contact contact = ServiceManager.getContactService().getContact(uri);
		if(contact != null  && (displayname = contact.getDisplayName()) != null){
			return displayname;
		}
		
		SipUri sipUri = new SipUri(uri);
		if(sipUri.isValid()){
			displayname = sipUri.getUserName();
		}
		sipUri.delete();
		
		return displayname;
	}
	
	public static boolean isValidSipUri(String uri){
		return SipUri.isValid(uri);
	}
	
	// Very very basic
	public static String makeValidSipUri(String uri){
		if(StringUtils.isNullOrEmpty(uri)){
			return UriUtils.INVALID_SIP_URI;
		}
		if(uri.startsWith("sip:") || uri.startsWith("sip:") || uri.startsWith("tel:")){
			//FIXME
			return uri;
		}
		else{
			if(uri.contains("@")){
				return String.format("sip:%s", uri);
			}
			else{
				String realm = ServiceManager.getConfigurationService().getString(CONFIGURATION_SECTION.NETWORK, CONFIGURATION_ENTRY.REALM, Configuration.DEFAULT_REALM);
				if(realm.contains(":")){
					realm = realm.substring(realm.indexOf(":")+1);
				}
				return String.format("sip:%s@%s", uri, realm);
			}
		}
	}
	
	public static String getValidPhoneNumber(String uri){
		SipUri sipUri = new SipUri(uri);
		if(sipUri.isValid()){
			String userName = sipUri.getUserName();
			if(userName != null){
				try{
					String scheme = sipUri.getScheme();
					if(scheme != null && scheme.equals("tel")){
						userName = userName.replace("-", "");
					}
					long result = Long.parseLong(userName.startsWith("+") ? userName.substring(1) : userName);
					if(result < UriUtils.MAX_PHONE_NUMBER){
						return userName;
					}
				}
				catch (Exception e){
					e.printStackTrace();
				}
				finally{
					sipUri.delete();
				}
			}
		}
		return null;
	}
}
