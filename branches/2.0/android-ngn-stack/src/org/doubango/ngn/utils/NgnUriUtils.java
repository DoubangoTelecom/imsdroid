package org.doubango.ngn.utils;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.model.NgnContact;
import org.doubango.tinyWRAP.SipUri;

//FIXME: THIS IS THE WORST CLASS YOU WILL FIND IN THE PROJECT ;)
public class NgnUriUtils {

	private final static long MAX_PHONE_NUMBER = 1000000000000L;
	private final static String INVALID_SIP_URI = "sip:invalid@open-ims.test";
	
	public static String getDisplayName(String uri){
		String displayname = null;
		if(!NgnStringUtils.isNullOrEmpty(uri)){
			NgnContact contact = NgnEngine.getInstance().getContactService().getContactByUri(uri);
			if(contact != null  && (displayname = contact.getDisplayName()) != null){
				return displayname;
			}
			
			final SipUri sipUri = new SipUri(uri);
			if(sipUri.isValid()){
				displayname = sipUri.getUserName();
				contact = NgnEngine.getInstance().getContactService().getContactByPhoneNumber(displayname);
				if(contact != null && !NgnStringUtils.isNullOrEmpty(contact.getDisplayName())){
					displayname = contact.getDisplayName();
				}
			}
			sipUri.delete();
		}
		
		return displayname == null ? uri : displayname;
	}
	
	public static String getUserName(String validUri){
		final SipUri sipUri = new SipUri(validUri);
		String userName = validUri;
		if(sipUri.isValid()){
			userName = sipUri.getUserName();
		}
		sipUri.delete();
		return userName;
	}
	
	public static boolean isValidSipUri(String uri){
		return SipUri.isValid(uri);
	}
	
	// Very very basic
	public static String makeValidSipUri(String uri){
		if(NgnStringUtils.isNullOrEmpty(uri)){
			return NgnUriUtils.INVALID_SIP_URI;
		}
		if(uri.startsWith("sip:") || uri.startsWith("sip:")){
			return uri.replace("#", "%23");
		}
		else if(uri.startsWith("tel:")){
			return uri;
		}
		else{
			if(uri.contains("@")){
				return String.format("sip:%s", uri);
			}
			else{
				String realm = NgnEngine.getInstance().getConfigurationService().getString(NgnConfigurationEntry.NETWORK_REALM, 
						NgnConfigurationEntry.DEFAULT_NETWORK_REALM);
				if(realm.contains(":")){
					realm = realm.substring(realm.indexOf(":")+1);
				}
				// FIXME: Should be done by doubango
				return String.format("sip:%s@%s", 
						uri.replace("(", "").replace(")", "").replace("-", "").replace("#", "%23"), 
						realm);
			}
		}
	}
	
	public static String getValidPhoneNumber(String uri){
		if(uri != null && (uri.startsWith("sip:") || uri.startsWith("sip:") || uri.startsWith("tel:"))){
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
						if(result < NgnUriUtils.MAX_PHONE_NUMBER){
							return userName;
						}
					}
					catch(NumberFormatException ne){ }
					catch (Exception e){
						e.printStackTrace();
					}
				}
			}
			sipUri.delete();
		}
		else{
			try{
				uri = uri.replace("-", "");
				long result = Long.parseLong(uri.startsWith("+") ? uri.substring(1) : uri);
				if(result < NgnUriUtils.MAX_PHONE_NUMBER){
					return uri;
				}
			}
			catch(NumberFormatException ne){ }
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
}
