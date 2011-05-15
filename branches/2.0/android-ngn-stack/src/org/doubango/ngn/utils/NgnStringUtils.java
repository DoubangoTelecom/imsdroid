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
package org.doubango.ngn.utils;

import java.math.BigInteger;
import java.security.MessageDigest;

public class NgnStringUtils {
	private static MessageDigest sMD5Digest;
	
	public static String emptyValue(){
		return "";
	}
	
	public static String nullValue(){
		return "(null)";
	}
	
	public static boolean isNullOrEmpty(String s){
		return ((s == null) || ("".equals(s)));
	}
	
	public static boolean startsWith(String s, String prefix, boolean ignoreCase){
		if(s != null && prefix != null){
			if(ignoreCase){
				return s.toLowerCase().startsWith(prefix.toLowerCase());
			}
			else{
				return s.startsWith(prefix);
			}
		}
		return s==null && prefix==null;
	}
	
	public static boolean equals(String s1, String s2, boolean ignoreCase){
		if(s1 != null && s2 != null){
			if(ignoreCase){
				return s1.equalsIgnoreCase(s2);
			}
			else{
				return s1.equals(s2);
			}
		}
		else{
			return ((s1 == null && s2 == null)? true : false);
		}
	}
	
	public static String unquote(String s, String quote){
		if(!NgnStringUtils.isNullOrEmpty(s) && !NgnStringUtils.isNullOrEmpty(quote)){
			if(s.startsWith(quote) && s.endsWith(quote)){
				return s.substring(1, s.length()-quote.length());
			}
		}
		return s;
	}
	
	public static String quote(String s, String quote){
		if(!NgnStringUtils.isNullOrEmpty(s) && !NgnStringUtils.isNullOrEmpty(quote)){
			return quote.concat(s).concat(quote);
		}
		return s;
	}
	
	public static long parseLong(String value, long defaultValue){
		try{
			if(NgnStringUtils.isNullOrEmpty(value)){
				return defaultValue;
			}
			return Long.parseLong(value);
		}
		catch(NumberFormatException e){
			e.printStackTrace();
		}
		return defaultValue;
	}
	
	public static int parseInt(String value, int defaultValue){
		try{
			if(NgnStringUtils.isNullOrEmpty(value)){
				return defaultValue;
			}
			return Integer.parseInt(value);
		}
		catch(NumberFormatException e){
			e.printStackTrace();
		}
		return defaultValue;
	}
	
	public static String getMD5(String str){
		if(str != null){
			try{
				final BigInteger bigInt;
				if(sMD5Digest == null){
					sMD5Digest = MessageDigest.getInstance("MD5");
				}
				synchronized (sMD5Digest) {
					sMD5Digest.reset();
					bigInt = new BigInteger(1, sMD5Digest.digest(str.getBytes("UTF-8")));
				}
				String hash = bigInt.toString(16);
				while(hash.length() < 32 ){
					hash = "0" + hash;
				}
				return hash;
			}
			catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}
	
	public static byte[] getMD5Digest(String str){
		if(str != null){
			try{
				if(sMD5Digest == null){
					sMD5Digest = MessageDigest.getInstance("MD5");
				}
				synchronized (sMD5Digest) {
					sMD5Digest.reset();
					return sMD5Digest.digest(str.getBytes("UTF-8"));
				}
			}
			catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}
	
	public static boolean isValidMD5String(String md5String){
		if(md5String != null){
			return md5String.length() == 32;
		}
		return false;
	}
}
