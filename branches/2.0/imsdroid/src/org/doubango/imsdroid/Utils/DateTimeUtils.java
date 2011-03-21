package org.doubango.imsdroid.Utils;

import java.text.DateFormat;
import java.util.Date;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.R;
import org.doubango.ngn.utils.NgnDateTimeUtils;

public class DateTimeUtils extends NgnDateTimeUtils{

	static final DateFormat sDateFormat = DateFormat.getInstance();
	static final DateFormat sDateTimeFormat = DateFormat.getDateTimeInstance();
	static final DateFormat sTimeFormat = DateFormat.getTimeInstance();
	
	static String sTodayName;
	static String sYesterdayName;
	
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
