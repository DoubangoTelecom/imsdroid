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

public class NgnMessagingEventArgs extends NgnEventArgs{
	private final static String TAG = NgnMessagingEventArgs.class.getCanonicalName();
	
	private long mSessionId;
    private NgnMessagingEventTypes mEventType;
    private String mPhrase;
    private byte[] mPayload;
    private String mContentType;
    
    public static final String ACTION_MESSAGING_EVENT = TAG + ".ACTION_MESSAGING_EVENT";
    
    public static final String EXTRA_EMBEDDED = NgnEventArgs.EXTRA_EMBEDDED;
    public static final String EXTRA_SESSION = TAG + "session";
    public static final String EXTRA_CODE = TAG + "code";
    public static final String EXTRA_REMOTE_PARTY = TAG + "from";
    public static final String EXTRA_DATE = TAG + "date";

    public NgnMessagingEventArgs(long sessionId, NgnMessagingEventTypes type, String phrase, byte[] payload, String contentType){
    	super();
        mSessionId = sessionId;
        mEventType = type;
        mPhrase = phrase;
        mPayload = payload;
        mContentType = contentType;
    }

    public NgnMessagingEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<NgnMessagingEventArgs> CREATOR = new Parcelable.Creator<NgnMessagingEventArgs>() {
        public NgnMessagingEventArgs createFromParcel(Parcel in) {
            return new NgnMessagingEventArgs(in);
        }

        public NgnMessagingEventArgs[] newArray(int size) {
            return new NgnMessagingEventArgs[size];
        }
    };
    
    public long getSessionId(){
        return mSessionId;
    }

    public NgnMessagingEventTypes getEventType(){
        return mEventType;
    }

    public String getPhrase(){
        return mPhrase;
    }

    public byte[] getPayload(){
        return mPayload;
    }
    
    public String getContentType() {
    	return mContentType;
    }

	@Override
	protected void readFromParcel(Parcel in) {
		mSessionId = in.readLong();
		mEventType = Enum.valueOf(NgnMessagingEventTypes.class, in.readString());
		mPhrase = in.readString();
		mContentType = in.readString();
		mPayload = in.createByteArray();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mSessionId);
		dest.writeString(mEventType.toString());
		dest.writeString(mPhrase);
		dest.writeString(mContentType);
		dest.writeByteArray(mPayload);
	}
}
