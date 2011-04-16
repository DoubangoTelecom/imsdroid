package org.doubango.ngn.events;

import android.os.Parcel;
import android.os.Parcelable;

public class NgnMsrpEventArgs extends NgnEventArgs{
	private final static String TAG = NgnMsrpEventArgs.class.getCanonicalName();
	
	private long mSessionId;
    private NgnMsrpEventTypes mEventType;
    
    public static final String ACTION_MSRP_EVENT = TAG + ".ACTION_MSRP_EVENT";
    
    public static final String EXTRA_EMBEDDED = NgnEventArgs.EXTRA_EMBEDDED;
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_CONTENT_TYPE = "content-type";
    public static final String EXTRA_WRAPPED_CONTENT_TYPE = "w-content-type";
    public static final String EXTRA_BYTE_RANGE_START = "byte-start";
    public static final String EXTRA_BYTE_RANGE_END = "byte-end";
    public static final String EXTRA_BYTE_RANGE_TOTAL = "byte-total";
    public static final String EXTRA_RESPONSE_CODE = "response-code";
    public static final String EXTRA_REQUEST_TYPE = "request-type";

    public NgnMsrpEventArgs(long sessionId, NgnMsrpEventTypes type){
    	super();
    	mSessionId = sessionId;
    	mEventType = type;
    }

    public NgnMsrpEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<NgnMsrpEventArgs> CREATOR = new Parcelable.Creator<NgnMsrpEventArgs>() {
        public NgnMsrpEventArgs createFromParcel(Parcel in) {
            return new NgnMsrpEventArgs(in);
        }

        public NgnMsrpEventArgs[] newArray(int size) {
            return new NgnMsrpEventArgs[size];
        }
    };
    
    public long getSessionId(){
        return mSessionId;
    }

    public NgnMsrpEventTypes getEventType(){
        return mEventType;
    }

	@Override
	protected void readFromParcel(Parcel in) {
		mSessionId = (short)in.readLong();
		mEventType = Enum.valueOf(NgnMsrpEventTypes.class, in.readString());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mSessionId);
		dest.writeString(mEventType.toString());
	}
}
