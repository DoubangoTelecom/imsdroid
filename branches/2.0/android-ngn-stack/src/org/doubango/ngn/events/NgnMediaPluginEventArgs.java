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

import java.math.BigInteger;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.media.NgnMediaType;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class NgnMediaPluginEventArgs extends NgnEventArgs{
	private final static String TAG = NgnMediaPluginEventArgs.class.getCanonicalName();
	
	private BigInteger mPluginId;
	private NgnMediaType mMediaType;
	private NgnMediaPluginEventTypes mEventType;
	
	public static final String ACTION_MEDIA_PLUGIN_EVENT = TAG + ".ACTION_MEDIA_PLUGIN_EVENT";
	
	public static final String EXTRA_EMBEDDED = NgnEventArgs.EXTRA_EMBEDDED;
	
	public NgnMediaPluginEventArgs(BigInteger pluginId, NgnMediaType mediaType, NgnMediaPluginEventTypes eventType){
		super();
		mPluginId = pluginId;
		mMediaType = mediaType;
		mEventType = eventType;
	}
	
	public NgnMediaPluginEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<NgnMediaPluginEventArgs> CREATOR = new Parcelable.Creator<NgnMediaPluginEventArgs>() {
        public NgnMediaPluginEventArgs createFromParcel(Parcel in) {
            return new NgnMediaPluginEventArgs(in);
        }

        public NgnMediaPluginEventArgs[] newArray(int size) {
            return new NgnMediaPluginEventArgs[size];
        }
    };
    
    public BigInteger getPluginId(){
        return mPluginId;
    }

    public NgnMediaType getMediaType(){
        return mMediaType;
    }

    public NgnMediaPluginEventTypes getEventType(){
        return mEventType;
    }
    
    @Override
	protected void readFromParcel(Parcel in) {
    	mPluginId = BigInteger.valueOf(in.readLong());
    	mMediaType = Enum.valueOf(NgnMediaType.class, in.readString());
    	mEventType = Enum.valueOf(NgnMediaPluginEventTypes.class, in.readString());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mPluginId.longValue());
		dest.writeString(mMediaType.toString());
		dest.writeString(mEventType.toString());
	}
	
	public static void broadcastEvent(NgnMediaPluginEventArgs args){
		if(NgnApplication.getContext() == null){
			Log.e(TAG,"Null application context");
			return;
		}
		final Intent intent = new Intent(NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT);
		intent.putExtra(NgnMediaPluginEventArgs.EXTRA_EMBEDDED, args);
		NgnApplication.getContext().sendBroadcast(intent);
	}
}
