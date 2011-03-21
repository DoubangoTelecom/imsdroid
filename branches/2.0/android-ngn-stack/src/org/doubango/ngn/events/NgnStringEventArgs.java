package org.doubango.ngn.events;

import android.os.Parcel;
import android.os.Parcelable;

public class NgnStringEventArgs extends NgnEventArgs {
	private String mValue;
	
	public NgnStringEventArgs(String value){
		mValue = value;
	}
	
	public NgnStringEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<NgnStringEventArgs> CREATOR = new Parcelable.Creator<NgnStringEventArgs>() {
        public NgnStringEventArgs createFromParcel(Parcel in) {
            return new NgnStringEventArgs(in);
        }

        public NgnStringEventArgs[] newArray(int size) {
            return new NgnStringEventArgs[size];
        }
    };
    
	public String getValue(){
		return mValue;
	}
	
	
	@Override
	public String toString() {
		return mValue;
	}

	@Override
	protected void readFromParcel(Parcel in) {
		mValue = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mValue);
	}
}
