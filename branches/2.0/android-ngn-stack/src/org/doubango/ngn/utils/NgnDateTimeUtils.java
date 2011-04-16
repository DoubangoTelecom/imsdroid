package org.doubango.ngn.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NgnDateTimeUtils {
	static final DateFormat sDefaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String now(String dateFormat) {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	    return sdf.format(cal.getTime());
	}
	
	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    return sDefaultDateFormat.format(cal.getTime());
	}
	
	public static Date parseDate(String date, DateFormat format){
		if(!NgnStringUtils.isNullOrEmpty(date)){
			try {
				return format == null ? sDefaultDateFormat.parse(date) : format.parse(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return new Date();
	}
	
	public static Date parseDate(String date){
		return parseDate(date, null);
	}
	
	public static boolean isSameDay(Date d1, Date d2){
		return d1.getDay() == d2.getDay() && d1.getMonth() == d2.getMonth() && d1.getYear() == d2.getYear();
	}
}
