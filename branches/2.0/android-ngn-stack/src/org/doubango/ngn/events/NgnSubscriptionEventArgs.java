package org.doubango.ngn.events;

import org.doubango.ngn.sip.NgnSubscriptionSession.EventPackageType;

import android.os.Parcel;
import android.os.Parcelable;

public class NgnSubscriptionEventArgs extends NgnEventArgs{
	private final static String TAG = NgnSubscriptionEventArgs.class.getCanonicalName();
	
	public static final String ACTION_SUBSCRIBTION_EVENT = TAG + ".ACTION_SUBSCRIBTION_EVENT";
	
	private long mSessionId;
	private NgnSubscriptionEventTypes mType;
    private short mSipCode;
    private String mPhrase;
    private byte[] mContent;
    private String mContentType;
    private EventPackageType mEventPackage;

    public static final String EXTRA_EMBEDDED = NgnEventArgs.EXTRA_EMBEDDED;
    public final String EXTRA_CONTENTYPE_TYPE = "ContentTypeType";
    public final String EXTRA_CONTENTYPE_START = "ContentTypeStart";
    public final String EXTRA_CONTENTYPE_BOUNDARY = "ContentTypeBoundary";
	
	public NgnSubscriptionEventArgs(long sessionId, NgnSubscriptionEventTypes type, short sipCode, String phrase, 
			byte[] content, String contentType, EventPackageType eventPackage){
		super();
		mSessionId = sessionId;
		mType = type;
		mSipCode = sipCode;
		mPhrase = phrase;
		mContent = content;
		mContentType = contentType;
		mEventPackage = eventPackage;
	}

	public NgnSubscriptionEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<NgnSubscriptionEventArgs> CREATOR = new Parcelable.Creator<NgnSubscriptionEventArgs>() {
        public NgnSubscriptionEventArgs createFromParcel(Parcel in) {
            return new NgnSubscriptionEventArgs(in);
        }

        public NgnSubscriptionEventArgs[] newArray(int size) {
            return new NgnSubscriptionEventArgs[size];
        }
    };
    
    public long getSessionId(){
        return mSessionId;
    }

    public NgnSubscriptionEventTypes getEventType(){
        return mType;
    }

    public String getPhrase(){
        return mPhrase;
    }

    public byte[] getContent(){
        return mContent;
    }
    
    public String getContentType(){
        return mContentType;
    }
    
    public EventPackageType getEventPackage(){
        return mEventPackage;
    }
    
	@Override
	protected void readFromParcel(Parcel in) {
		mSessionId = in.readLong();
		mType = Enum.valueOf(NgnSubscriptionEventTypes.class, in.readString());
		mSipCode = (short)in.readInt();
		mPhrase = in.readString();
		mContent = in.createByteArray();
		mContentType = in.readString();
		mEventPackage = Enum.valueOf(EventPackageType.class, in.readString());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mSessionId);
		dest.writeString(mType.toString());
		dest.writeInt(mSipCode);
		dest.writeString(mPhrase);
		dest.writeByteArray(mContent);
		dest.writeString(mContentType);
		dest.writeString(mEventPackage.toString());
	}
}
