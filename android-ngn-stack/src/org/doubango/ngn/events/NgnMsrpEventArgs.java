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
package org.doubango.ngn.events;

import android.os.Parcel;
import android.os.Parcelable;

public class NgnMsrpEventArgs extends NgnEventArgs{
	private final static String TAG = NgnMsrpEventArgs.class.getCanonicalName();
	
	private long mSessionId;
    private NgnMsrpEventTypes mEventType;
    
    public static final String ACTION_MSRP_EVENT = TAG + ".ACTION_MSRP_EVENT";
    
    public static final String EXTRA_EMBEDDED = NgnEventArgs.EXTRA_EMBEDDED;
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_CONTENT_TYPE = "content-type";
    public static final String EXTRA_WRAPPED_CONTENT_TYPE = "w-content-type";
    public static final String EXTRA_BYTE_RANGE_START = "byte-start";
    public static final String EXTRA_BYTE_RANGE_END = "byte-end";
    public static final String EXTRA_BYTE_RANGE_TOTAL = "byte-total";
    public static final String EXTRA_RESPONSE_CODE = "response-code";
    public static final String EXTRA_REQUEST_TYPE = "request-type";

    public NgnMsrpEventArgs(long sessionId, NgnMsrpEventTypes type){
    	super();
    	mSessionId = sessionId;
    	mEventType = type;
    }

    public NgnMsrpEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<NgnMsrpEventArgs> CREATOR = new Parcelable.Creator<NgnMsrpEventArgs>() {
        public NgnMsrpEventArgs createFromParcel(Parcel in) {
            return new NgnMsrpEventArgs(in);
        }

        public NgnMsrpEventArgs[] newArray(int size) {
            return new NgnMsrpEventArgs[size];
        }
    };
    
    public long getSessionId(){
        return mSessionId;
    }

    public NgnMsrpEventTypes getEventType(){
        return mEventType;
    }

	@Override
	protected void readFromParcel(Parcel in) {
		mSessionId = in.readLong();
		mEventType = Enum.valueOf(NgnMsrpEventTypes.class, in.readString());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mSessionId);
		dest.writeString(mEventType.toString());
	}
}
