package org.doubango.ngn.events;

import android.os.Parcel;
import android.os.Parcelable;

public class NgnMessagingEventArgs extends NgnEventArgs{
	private final static String TAG = NgnMessagingEventArgs.class.getCanonicalName();
	private long mSessionId;
    private NgnMessagingEventTypes mType;
    private String mPhrase;
    private byte[] mPayload;

    public static final String ACTION_MESSAGING_EVENT = TAG + ".ACTION_MESSAGING_EVENT";
    
    public static final String EXTRA_SESSION = TAG + "session";
    public static final String EXTRA_CODE = TAG + "code";
    public static final String EXTRA_REMOTE_PARTY = TAG + "from";
    public static final String EXTRA_DATE = TAG + "date";
    public static final String EXTRA_CONTENT_TYPE = TAG + "content-Type";

    public NgnMessagingEventArgs(long sessionId, NgnMessagingEventTypes type, String phrase, byte[] payload){
    	super();
        mSessionId = sessionId;
        mType = type;
        mPhrase = phrase;
        mPayload = payload;
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
        return mType;
    }

    public String getPhrase(){
        return mPhrase;
    }

    public byte[] getPayload(){
        return mPayload;
    }

	@Override
	protected void readFromParcel(Parcel in) {
		mSessionId = (short)in.readLong();
		mType = Enum.valueOf(NgnMessagingEventTypes.class, in.readString());
		mPhrase = in.readString();
		mPayload = in.createByteArray();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mSessionId);
		dest.writeString(mType.toString());
		dest.writeString(mPhrase);
		dest.writeByteArray(mPayload);
	}
}
