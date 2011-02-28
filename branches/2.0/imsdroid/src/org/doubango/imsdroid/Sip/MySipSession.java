package org.doubango.imsdroid.Sip;

import org.doubango.imsdroid.Utils.ObservableObject;
import org.doubango.imsdroid.Utils.StringUtils;
import org.doubango.imsdroid.Utils.UriUtils;
import org.doubango.tinyWRAP.SipSession;

import android.util.Log;
public abstract class MySipSession extends ObservableObject implements Comparable<MySipSession>{
	protected static final String TAG = MySipSession.class.getCanonicalName();
	
	protected MySipStack mSipStack;
    protected boolean mOutgoing;
    protected String mFromUri;
    protected String mToUri;
    protected String mCompId;
    protected String mRemotePartyUri;
    protected String mRemotePartyDisplayName = null;
    protected long mId = -1;
    protected int mRefCount = 1;
    protected ConnectionState mConnectionState;
    
    public enum ConnectionState{
        NONE,
        CONNECTING,
        CONNECTED,
        TERMINATING,
        TERMINATED,
    }

    public MySipSession(MySipStack sipStack){
        mSipStack = sipStack;
        mOutgoing = false;
        mConnectionState = ConnectionState.NONE;
        /* init must be called by the child class after session_create() */
        /* this.init(); */
    }
    
    public int incRef(){
    	synchronized (this) {
    		if(mRefCount>0){
    			return mRefCount++;
    		}
    		return 0;
		}
    }
    
    public int decRef(){
    	synchronized (this) {
			if(--mRefCount == 0){
				getSession().delete();
			}
			return mRefCount;
		}
    }
    
    public long getId(){
    	if(mId == -1){
            mId = getSession().getId(); 
        }
        return mId;
    }

    public MySipStack getStack(){
        return mSipStack;
    }

    public boolean addHeader(String name, String value){
    	return getSession().addHeader(name, value);
    }
    
    public boolean removeHeader(String name){
    	return getSession().removeHeader(name);
    }
    
    public boolean addCaps(String name){
    	return getSession().addCaps(name);
    }
    
    public boolean addCaps(String name, String value){
    	return getSession().addCaps(name, value);
    }
    
    public boolean removeCaps(String name){
    	return getSession().removeCaps(name);
    }
    
    public boolean isConnected(){
    	return (mConnectionState == ConnectionState.CONNECTED);
    }
    
    public void setConnectionState(ConnectionState state){
    	mConnectionState = state;
    }
    
    public ConnectionState getConnectionState(){
    	return mConnectionState;
    }
    
    public String getFromUri(){
    	return mFromUri;
    }
    
    public void setFromUri(String uri){
    	if (!getSession().setFromUri(uri)){
            Log.e(TAG, String.format("{0} is invalid for as FromUri", uri));
            return;
        }
        mFromUri = uri;
    }
    
    public String getToUri(){
    	return mToUri;
    }
    
    public void setToUri(String uri){
    	if (!getSession().setToUri(uri)){
            Log.e(TAG, String.format("{0} is invalid for as toUri", uri));
            return;
        }
    	mToUri = uri;
    }
    
    public String getRemotePartyUri(){
    	if (StringUtils.isNullOrEmpty(mRemotePartyUri)){
            mRemotePartyUri =  mOutgoing ? mToUri : mFromUri;
        }
        return StringUtils.isNullOrEmpty(mRemotePartyUri) ? "(null)" : mRemotePartyUri;
    }
    
    public void setRemotePartyUri(String uri){
    	mRemotePartyUri = uri;
    }
    
    public String getRemotePartyDisplayName(){
    	if (StringUtils.isNullOrEmpty(mRemotePartyDisplayName)){
            mRemotePartyDisplayName = UriUtils.getDisplayName(getRemotePartyUri());
            mRemotePartyDisplayName = StringUtils.isNullOrEmpty(this.mRemotePartyDisplayName) ? "(null)" : this.mRemotePartyDisplayName;
        }
        return mRemotePartyDisplayName;
    }

    public void setSigCompId(String compId){
		if(compId != null && mCompId != compId){
			getSession().removeSigCompCompartment();
		}
		if((mCompId = compId) != null){
			getSession().addSigCompCompartment(mCompId);
		}
	}
    
    public void delete(){
		getSession().delete();
	}

    protected abstract SipSession getSession();

    protected void init(){
        // Sip Headers (common to all sessions)
        getSession().addCaps("+g.oma.sip-im");
        getSession().addCaps("language", "\"en,fr\"");
    }

	@Override
	public int compareTo(MySipSession arg0) {
		return (int)(getId() - arg0.getId());
	}
}
