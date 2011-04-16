package org.doubango.ngn.events;

import android.os.Parcel;
import android.os.Parcelable;

public class NgnPublicationEventArgs extends NgnEventArgs{
	private final static String TAG = NgnPublicationEventArgs.class.getCanonicalName();
	
	public static final String ACTION_PUBLICATION_EVENT = TAG + ".ACTION_PUBLICATION_EVENT";
	
	public static final String EXTRA_EMBEDDED = NgnEventArgs.EXTRA_EMBEDDED;
	
	private long mSessionId;
	private NgnPublicationEventTypes mType;
	private short mSipCode;
	private String mPhrase;
	
	public NgnPublicationEventArgs(long sessionId, NgnPublicationEventTypes type, short sipCode, String phrase){
    	super();
    	mSessionId = sessionId;
    	mType = type;
    	mSipCode = sipCode;
    	mPhrase = phrase;
    }
    
    public NgnPublicationEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<NgnPublicationEventArgs> CREATOR = new Parcelable.Creator<NgnPublicationEventArgs>() {
        public NgnPublicationEventArgs createFromParcel(Parcel in) {
            return new NgnPublicationEventArgs(in);
        }

        public NgnPublicationEventArgs[] newArray(int size) {
            return new NgnPublicationEventArgs[size];
        }
    };

    public long getSessionId(){
    	return mSessionId;
    }
    
    public NgnPublicationEventTypes getEventType(){
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
		mType = Enum.valueOf(NgnPublicationEventTypes.class, in.readString());
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
