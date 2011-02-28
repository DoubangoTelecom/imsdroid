package org.doubango.imsdroid.Events;

import android.os.Parcel;
import android.os.Parcelable;

public class RegistrationEventArgs extends EventArgs {
	private RegistrationEventTypes mType;
	private short mSipCode;
	private String mPhrase;
    
    public RegistrationEventArgs(RegistrationEventTypes type, short sipCode, String phrase){
    	super();
    	mType = type;
    	mSipCode = sipCode;
    	mPhrase = phrase;
    }
    
    public RegistrationEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<RegistrationEventArgs> CREATOR = new Parcelable.Creator<RegistrationEventArgs>() {
        public RegistrationEventArgs createFromParcel(Parcel in) {
            return new RegistrationEventArgs(in);
        }

        public RegistrationEventArgs[] newArray(int size) {
            return new RegistrationEventArgs[size];
        }
    };

    public RegistrationEventTypes getEventType(){
        return mType;
    }

    public short getSipCode(){
        return mSipCode;
    }

    public String getPhrase(){
        return mPhrase;
    }

	@Override
	protected void readFromParcel(Parcel in) {
		mType = Enum.valueOf(RegistrationEventTypes.class, in.readString());
		mSipCode = (short)in.readInt();
		mPhrase = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mType.toString());
		dest.writeInt(mSipCode);
		dest.writeString(mPhrase);
	}
}
