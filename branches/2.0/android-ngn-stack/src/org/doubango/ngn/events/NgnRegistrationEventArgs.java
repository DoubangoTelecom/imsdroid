package org.doubango.ngn.events;

import android.os.Parcel;
import android.os.Parcelable;

public class NgnRegistrationEventArgs extends NgnEventArgs {
	private final static String TAG = NgnRegistrationEventArgs.class.getCanonicalName();
	
	private long mSessionId;
	private NgnRegistrationEventTypes mType;
	private short mSipCode;
	private String mPhrase;
    
	public static final String ACTION_REGISTRATION_EVENT = TAG + ".ACTION_REGISTRATION_CHANGED";
	
	public static final String EXTRA_EMBEDDED = NgnEventArgs.EXTRA_EMBEDDED;
	
    public NgnRegistrationEventArgs(long sessionId, NgnRegistrationEventTypes type, short sipCode, String phrase){
    	super();
    	mSessionId = sessionId;
    	mType = type;
    	mSipCode = sipCode;
    	mPhrase = phrase;
    }
    
    public NgnRegistrationEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<NgnRegistrationEventArgs> CREATOR = new Parcelable.Creator<NgnRegistrationEventArgs>() {
        public NgnRegistrationEventArgs createFromParcel(Parcel in) {
            return new NgnRegistrationEventArgs(in);
        }

        public NgnRegistrationEventArgs[] newArray(int size) {
            return new NgnRegistrationEventArgs[size];
        }
    };

    public long getSessionId(){
    	return mSessionId;
    }
    
    public NgnRegistrationEventTypes getEventType(){
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
		mSessionId = in.readLong();
		mType = Enum.valueOf(NgnRegistrationEventTypes.class, in.readString());
		mSipCode = (short)in.readInt();
		mPhrase = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mSessionId);
		dest.writeString(mType.toString());
		dest.writeInt(mSipCode);
		dest.writeString(mPhrase);
	}
}
