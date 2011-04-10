package org.doubango.ngn.events;

import android.os.Parcel;

public class NgnSubscriptionEventArgs extends NgnEventArgs{
	private final static String TAG = NgnSubscriptionEventArgs.class.getCanonicalName();
	
	public static final String ACTION_SUBSCRIBTION_EVENT = TAG + ".ACTION_SUBSCRIBTION_EVENT";
	
	public static final String EXTRA_EMBEDDED = NgnEventArgs.EXTRA_EMBEDDED;
	
	public NgnSubscriptionEventArgs(){
		super();
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
