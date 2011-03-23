package org.doubango.ngn.events;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Base class for all events
 */
public abstract class NgnEventArgs implements Parcelable {
	public static final String EXTRA_EMBEDDED = "EXTRA_" + NgnEventArgs.class.getCanonicalName();
	
	public NgnEventArgs(){
		super();
	}

	 protected NgnEventArgs(Parcel in) {
	      readFromParcel(in);
	 }
	 
    abstract protected void readFromParcel(Parcel in);
	    
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	abstract public void writeToParcel(Parcel dest, int flags);
}
