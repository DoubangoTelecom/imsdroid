package org.doubango.imsdroid.Events;

import android.os.Parcel;
import android.os.Parcelable;

public class StringEventArgs extends EventArgs {
	private String mValue;
	
	public StringEventArgs(String value){
		mValue = value;
	}
	
	public StringEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<StringEventArgs> CREATOR = new Parcelable.Creator<StringEventArgs>() {
        public StringEventArgs createFromParcel(Parcel in) {
            return new StringEventArgs(in);
        }

        public StringEventArgs[] newArray(int size) {
            return new StringEventArgs[size];
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
