package org.doubango.ngn.events;

import android.os.Parcel;

/**
 * Event argument associated to the stack
 */
public class NgnStackEventArgs extends NgnEventArgs{
	private final static String TAG = NgnStackEventArgs.class.getCanonicalName();
	
	private final NgnStackEventTypes mType;
    private final String mPhrase;

    public static final String ACTION_STACK_EVENT = TAG + ".ACTION_STACK_EVENT";
    
    public static final String EXTRA_EMBEDDED = NgnEventArgs.EXTRA_EMBEDDED;
    
    public NgnStackEventArgs(NgnStackEventTypes type, String phrase){
    	super();
        mType = type;
        mPhrase = phrase;
    }

    public NgnStackEventTypes getEventType(){
        return mType;
    }

    public String getPhrase(){
        return mPhrase;
    }

	@Override
	protected void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
}
