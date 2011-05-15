/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.doubango.ngn.sip;

import org.doubango.ngn.utils.NgnObservableObject;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.SipUri;

import android.util.Log;

/**
 * Abstract class defining a SIP Session (Registration, Subscription, Publication, Call, ...)
 */
public abstract class NgnSipSession extends NgnObservableObject implements Comparable<NgnSipSession>{
	private static final String TAG = NgnSipSession.class.getCanonicalName();
	
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
    
    /**
     * The connection state
     */
    public enum ConnectionState{
        NONE,
        CONNECTING,
        CONNECTED,
        TERMINATING,
        TERMINATED,
    }

    /**
     * Creates new SIP session
     * @param sipStack the sip stack to use to create the session
     */
    protected NgnSipSession(NgnSipStack sipStack){
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

    /**
     * Increments the reference counting
     * @return the new reference counting value
     * @sa @ref decRef()
     */
	public int incRef(){
    	synchronized (this) {
    		if(mRefCount>0){
    			mRefCount++;
    		}
    		Log.d(TAG, "mRefCount="+mRefCount);
    		return mRefCount;
		}
    }
    
	/**
	 * Decrements the reference counting
	 * @return the new reference counting value
	 * @sa @ref incRef()
	 */
    public int decRef(){
    	synchronized (this) {
			if(--mRefCount == 0){
				getSession().delete();
			}
			Log.d(TAG, "mRefCount="+mRefCount);
			return mRefCount;
		}
    }
    
    /**
     * Gets a unique identifier defining a session
     * @return a unique identifier defining the session
     */
    public long getId(){
    	if(mId == -1){
            mId = getSession().getId(); 
        }
        return mId;
    }

    public boolean isOutgoing(){
    	return mOutgoing;
    }
    
    /**
     * Gets the associated SIP stack
     * @return a SIP stack
     */
    public NgnSipStack getStack(){
        return mSipStack;
    }

    /**
     * Adds a new SIP header to the session
     * @param name the name of the header
     * @param value the value of the header
     * @return true if succeed and false otherwise
     * @sa @ref removeHeader()
     * @code
     * mSipSession.addHeader("User-Agent", "IM-OMAv1.0");
     * @endcode
     */
    public boolean addHeader(String name, String value){
    	return getSession().addHeader(name, value);
    }
    
    /**
     * Removes a SIP header from the session
     * @param name the name of the sip header to remove
     * @return true if succeed and false otherwise
     * @sa @ref addHeader()
     * @code
     * mSipSession.removeHeader("User-Agent");
     * @endcode
     */
    public boolean removeHeader(String name){
    	return getSession().removeHeader(name);
    }
    
    /**
     * Adds sip capabilities to the session. The capability will be added in a separate
     * "Accept-Contact" header if the session is dialogless or in the "Contact" header otherwise
     * @param name the name of capability to add
     * @return true if succeed and false otherwise
     * @sa @ref removeCaps()
     * @code
     * mSipSession.addCaps("+g.3gpp.smsip");
     * @endcode
     */
    public boolean addCaps(String name){
    	return getSession().addCaps(name);
    }
    
    /**
     * Adds sip capabilities to the session. The capability will be added in a separate
     * "Accept-Contact" header if the session is dialogless or in the "Contact" header otherwise
     * @param name the name of capability to add
     * @param value the value of the capability
     * @return true if succeed and false otherwise
     * @sa @ref removeCaps()
     * @code
     * mSipSession.addCaps("+g.3gpp.icsi-ref", "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"");
     * @endcode
     */
    public boolean addCaps(String name, String value){
    	return getSession().addCaps(name, value);
    }
    
    /**
     * Removes a sip capability from the session
     * @param name the name of the capability to remove
     * @return true if succeed and false otherwise
     * @sa @ref addCaps()
     * @code
     * mSipSession.removeCaps("+g.3gpp.smsip");
     * @endcode
     */
    public boolean removeCaps(String name){
    	return getSession().removeCaps(name);
    }
    
    /**
     * Checks whether the session established or not. For example, you can only send files when the session
     * is connected. You can use @ref getConnectionState() to have the exact state
     * @return true is session is established and false otherwise
     * @sa @ref getConnectionState()
     */
    public boolean isConnected(){
    	return (mConnectionState == ConnectionState.CONNECTED);
    }
    
    /**
     * Sets the connection state of the session. You should not call this function by yourself
     * @param state the new state
     */
    public void setConnectionState(ConnectionState state){
    	mConnectionState = state;
    }
    
    /**
     * Gets the connection state of the session
     * @return the connection state
     * @sa @ref isConnected()
     */
    public ConnectionState getConnectionState(){
    	return mConnectionState;
    }
    
    /**
     * Gets the sip from uri
     * @return the sip from uri
     */
    public String getFromUri(){
    	return mFromUri;
    }
    
    /**
     * Sets the sip from uri
     * @param uri the new sip from uri
     * @return true if succeed and false otherwise
     * @sa ref setToUri()
     */
    public boolean setFromUri(String uri){
    	if (!getSession().setFromUri(uri)){
            Log.e(TAG, String.format("%s is invalid as FromUri", uri));
            return false;
        }
        mFromUri = uri;
        return true;
    }
    
    public boolean setFromUri(SipUri uri){
    	if (!getSession().setFromUri(uri)){
            Log.e(TAG, "Failed to set FromUri");
            return false;
        }
        mFromUri = String.format("%s:%s@%s", uri.getScheme(), uri.getUserName(), uri.getHost());
        return true;
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
    
    public void setToUri(SipUri uri){
    	if (!getSession().setToUri(uri)){
            Log.e(TAG, "Failed to set ToUri");
            return;
        }
    	mToUri = String.format("%s:%s@%s", uri.getScheme(), uri.getUserName(), uri.getHost());
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
            mRemotePartyDisplayName = NgnStringUtils.isNullOrEmpty(mRemotePartyDisplayName) ? "(null)" : mRemotePartyDisplayName;
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
