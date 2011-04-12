package org.doubango.ngn.events;

import org.doubango.ngn.media.NgnMediaType;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Event argument for SIP INVITE sessions
 */
public class NgnInviteEventArgs extends NgnEventArgs{
	private final static String TAG = NgnInviteEventArgs.class.getCanonicalName();
	
	private long mSessionId;
    private NgnInviteEventTypes mEventType;
    private NgnMediaType mMediaType;
    private String mPhrase;
    
    public static final String ACTION_INVITE_EVENT = TAG + ".ACTION_INVITE_EVENT";
    
    public static final String EXTRA_EMBEDDED = NgnEventArgs.EXTRA_EMBEDDED;
    public static final String EXTRA_SESSION = "session";

    public NgnInviteEventArgs(long sessionId, NgnInviteEventTypes eventType, NgnMediaType mediaType, String phrase){
    	super();
    	mSessionId = sessionId;
    	mEventType = eventType;
    	mMediaType = mediaType;
    	mPhrase = phrase;
    }

    public NgnInviteEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<NgnInviteEventArgs> CREATOR = new Parcelable.Creator<NgnInviteEventArgs>() {
        public NgnInviteEventArgs createFromParcel(Parcel in) {
            return new NgnInviteEventArgs(in);
        }

        public NgnInviteEventArgs[] newArray(int size) {
            return new NgnInviteEventArgs[size];
        }
    };
    
    public long getSessionId(){
        return mSessionId;
    }

    public NgnInviteEventTypes getEventType(){
        return mEventType;
    }
    
    public NgnMediaType getMediaType(){
        return mMediaType;
    }

    public String getPhrase(){
        return mPhrase;
    }

    @Override
	protected void readFromParcel(Parcel in) {
    	mSessionId = (short)in.readLong();
		mEventType = Enum.valueOf(NgnInviteEventTypes.class, in.readString());
		mMediaType = Enum.valueOf(NgnMediaType.class, in.readString());
		mPhrase = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mSessionId);
		dest.writeString(mEventType.toString());
		dest.writeString(mMediaType.toString());
		dest.writeString(mPhrase);
	}
}
