package org.doubango.imsdroid.Events;

import android.os.Parcel;

public class StackEventArgs extends EventArgs{
	private final StackEventTypes mType;
    private final String mPhrase;

    public StackEventArgs(StackEventTypes type, String phrase){
    	super();
        mType = type;
        mPhrase = phrase;
    }

    public StackEventTypes getEventType(){
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
