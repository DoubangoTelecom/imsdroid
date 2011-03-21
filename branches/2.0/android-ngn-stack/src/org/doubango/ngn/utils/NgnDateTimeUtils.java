package org.doubango.ngn.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NgnDateTimeUtils {
	
	public static String now(String dateFormat) {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	    return sdf.format(cal.getTime());
	}
	
	public static boolean isSameDay(Date d1, Date d2){
		return d1.getDay() == d2.getDay() && d1.getMonth() == d2.getMonth() && d1.getYear() == d2.getYear();
	}
}
