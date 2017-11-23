package org.doubango.ngn.events;

import android.os.Parcel;
import android.os.Parcelable;

public class NgnNetworkEventArgs extends NgnEventArgs {
    private final static String TAG = NgnNetworkEventArgs.class.getCanonicalName();

    public static final String ACTION_NETWORK_EVENT = TAG + ".ACTION_NETWORK_EVENT";

    private NgnNetworkEventTypes mType;

    public NgnNetworkEventArgs(NgnNetworkEventTypes type){
        super();
        mType = type;
    }

    public NgnNetworkEventArgs(Parcel in){
        super(in);
    }

    public static final Parcelable.Creator<NgnNetworkEventArgs> CREATOR = new Parcelable.Creator<NgnNetworkEventArgs>() {
        public NgnNetworkEventArgs createFromParcel(Parcel in) {
            return new NgnNetworkEventArgs(in);
        }

        public NgnNetworkEventArgs[] newArray(int size) {
            return new NgnNetworkEventArgs[size];
        }
    };

    public NgnNetworkEventTypes getEventType(){
        return mType;
    }

    @Override
    protected void readFromParcel(Parcel in) {
        mType = Enum.valueOf(NgnNetworkEventTypes.class, in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mType.toString());
    }
}
