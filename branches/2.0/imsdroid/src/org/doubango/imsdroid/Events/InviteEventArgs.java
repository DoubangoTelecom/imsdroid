package org.doubango.imsdroid.Events;

import android.os.Parcel;
import android.os.Parcelable;

public class InviteEventArgs extends EventArgs{
	private long mSessionId;
    private InviteEventTypes mType;
    private String mPhrase;

    public static final String EXTRA_SESSION = "session";

    public InviteEventArgs(long sessionId, InviteEventTypes type, String phrase){
    	super();
    	mSessionId = sessionId;
    	mType = type;
    	mPhrase = phrase;
    }

    public InviteEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<InviteEventArgs> CREATOR = new Parcelable.Creator<InviteEventArgs>() {
        public InviteEventArgs createFromParcel(Parcel in) {
            return new InviteEventArgs(in);
        }

        public InviteEventArgs[] newArray(int size) {
            return new InviteEventArgs[size];
        }
    };
    
    public long getSessionId(){
        return mSessionId;
    }

    public InviteEventTypes getEventType(){
        return mType;
    }

    public String getPhrase(){
        return mPhrase;
    }

    @Override
	protected void readFromParcel(Parcel in) {
    	mSessionId = (short)in.readLong();
		mType = Enum.valueOf(InviteEventTypes.class, in.readString());
		mPhrase = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mSessionId);
		dest.writeString(mType.toString());
		dest.writeString(mPhrase);
	}
}
