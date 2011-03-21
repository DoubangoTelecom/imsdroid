package org.doubango.ngn.sip;

import org.doubango.ngn.utils.NgnObservableObject;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.tinyWRAP.SipSession;

import android.util.Log;

public abstract class NgnSipSession extends NgnObservableObject implements Comparable<NgnSipSession>{
	protected static final String TAG = NgnSipSession.class.getCanonicalName();
	
	protected NgnSipStack mSipStack;
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

    public NgnSipSession(NgnSipStack sipStack){
        mSipStack = sipStack;
        mOutgoing = false;
        mConnectionState = ConnectionState.NONE;
        /* init must be called by the child class after session_create() */
        /* this.init(); */
    }
    
    @Override
	protected void finalize() throws Throwable {
		Log.d(TAG, "finalize()");
		delete();
		super.finalize();
	}

	public int incRef(){
    	synchronized (this) {
    		if(mRefCount>0){
    			mRefCount++;
    		}
    		Log.d(TAG, "mRefCount="+mRefCount);
    		return mRefCount;
		}
    }
    
    public int decRef(){
    	synchronized (this) {
			if(--mRefCount == 0){
				getSession().delete();
			}
			Log.d(TAG, "mRefCount="+mRefCount);
			return mRefCount;
		}
    }
    
    public long getId(){
    	if(mId == -1){
            mId = getSession().getId(); 
        }
        return mId;
    }

    public NgnSipStack getStack(){
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
            Log.e(TAG, String.format("%s is invalid as FromUri", uri));
            return;
        }
        mFromUri = uri;
    }
    
    public String getToUri(){
    	return mToUri;
    }
    
    public void setToUri(String uri){
    	if (!getSession().setToUri(uri)){
            Log.e(TAG, String.format("%s is invalid as toUri", uri));
            return;
        }
    	mToUri = uri;
    }
    
    public String getRemotePartyUri(){
    	if (NgnStringUtils.isNullOrEmpty(mRemotePartyUri)){
            mRemotePartyUri =  mOutgoing ? mToUri : mFromUri;
        }
        return NgnStringUtils.isNullOrEmpty(mRemotePartyUri) ? "(null)" : mRemotePartyUri;
    }
    
    public void setRemotePartyUri(String uri){
    	mRemotePartyUri = uri;
    }
    
    public String getRemotePartyDisplayName(){
    	if (NgnStringUtils.isNullOrEmpty(mRemotePartyDisplayName)){
            mRemotePartyDisplayName = NgnUriUtils.getDisplayName(getRemotePartyUri());
            mRemotePartyDisplayName = NgnStringUtils.isNullOrEmpty(this.mRemotePartyDisplayName) ? "(null)" : this.mRemotePartyDisplayName;
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
	public int compareTo(NgnSipSession arg0) {
		return (int)(getId() - arg0.getId());
	}
}
