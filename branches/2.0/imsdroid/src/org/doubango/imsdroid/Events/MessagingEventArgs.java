package org.doubango.imsdroid.Events;

import android.os.Parcel;

public class MessagingEventArgs extends EventArgs{
	private final long mSessionId;
    private final MessagingEventTypes mType;
    private final String mPhrase;
    private final byte[] mPayload;

    public final String EXTRA_SESSION = "session";
    public final String EXTRA_CODE = "code";
    public final String EXTRA_REMOTE_PARTY = "from";
    public final String EXTRA_CONTENT_TYPE = "content-Type";

    public MessagingEventArgs(long sessionId, MessagingEventTypes type, String phrase, byte[] payload){
    	super();
        mSessionId = sessionId;
        mType = type;
        mPhrase = phrase;
        mPayload = payload;
    }

    public long getSessionId(){
        return mSessionId;
    }

    public MessagingEventTypes getEventType(){
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
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}
}
