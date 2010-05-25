package org.doubango.imsdroid.utils;

public class StringUtils {

	public static boolean isNullOrEmpty(String s){
		return ((s == null) || (s == ""));
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
}
