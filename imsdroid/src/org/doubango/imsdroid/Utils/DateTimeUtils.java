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
