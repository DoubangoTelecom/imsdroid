package org.doubango.imsdroid.Events;

import android.os.Parcel;
import android.os.Parcelable;


public abstract class EventArgs implements Parcelable {
	public static final String EXTRA_NAME = "EXTRA_" + EventArgs.class.getCanonicalName();
	
	public EventArgs(){
		super();
	}

	 protected EventArgs(Parcel in) {
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
