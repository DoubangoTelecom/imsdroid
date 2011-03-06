package org.doubango.imsdroid.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.R;

public class DateTimeUtils {
	static final DateFormat sDateFormat = DateFormat.getInstance();
	static final DateFormat sDateTimeFormat = DateFormat.getDateTimeInstance();
	static final DateFormat sTimeFormat = DateFormat.getTimeInstance();
	
	static String sTodayName;
	static String sYesterdayName;
	
	public static String now(String dateFormat) {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	    return sdf.format(cal.getTime());
	}
	
	public static String getTodayName(){
		if(sTodayName == null){
			sTodayName = IMSDroid.getContext().getResources().getString(R.string.day_today);
		}
		return sTodayName;
	}
	
	public static String getYesterdayName(){
		if(sYesterdayName == null){
			sYesterdayName = IMSDroid.getContext().getResources().getString(R.string.day_yesterday);
		}
		return sYesterdayName;
	}
	
	public static boolean isSameDay(Date d1, Date d2){
		return d1.getDay() == d2.getDay() && d1.getMonth() == d2.getMonth() && d1.getYear() == d2.getYear();
	}
	
	public static String getFriendlyDateString(final Date date){
		final Date today = new Date();
        if (DateTimeUtils.isSameDay(date, today)){
            return String.format("%s %s", getTodayName(), sTimeFormat.format(date));
        }
        else if ((today.getDay() - date.getDay()) == 1){
            return String.format("%s %s", getYesterdayName(), sTimeFormat.format(date));
        }
        else{
            return sDateTimeFormat.format(date);
        }
	}
}