package org.doubango.ngn.sip;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.tinyWRAP.InviteSession;
import org.doubango.tinyWRAP.MediaSessionMgr;


/**
 * Generic INVITE session. Could be either audio/video or MSRP session.
 * This is an abstract class and you should only used it if you want to define
 * you own session.
 */
public abstract class NgnInviteSession extends NgnSipSession{
	protected NgnMediaType mMediaType;
    protected MediaSessionMgr mMediaSessionMgr = null;
    protected InviteState mState;

    public enum InviteState{
        NONE,
        INCOMING,
        INPROGRESS,
        REMOTE_RINGING,
        EARLY_MEDIA,
        INCALL,
        TERMINATING,
        TERMINATED,
    }

    /**
     * Creates new Invite session
     * @param sipStack the stack to use
     */
	public NgnInviteSession(NgnSipStack sipStack) {
		super(sipStack);
		
		mState = InviteState.NONE;
	}
	
	/**
	 * Gets the media type
	 * @return the media type
	 */
	 public NgnMediaType getMediaType(){
         return mMediaType;
     }

	 /**
	  * Gets the session state
	  * @return the session state
	  */
     public InviteState getState(){
         return mState;
     }
     
     /**
      * Sets the session state
      * @param state the new session state
      */
     public void setState(InviteState state){
    	 mState = state;
     }

     /**
      * Checks whether the session is active or not
      * @return
      */
     public boolean isActive(){
    	 return mState != InviteState.NONE
         && mState != InviteState.TERMINATING 
         && mState != InviteState.TERMINATED;
     }

     /**
      * Gets the media session manager associated to this session
      * @return the media session manager
      */
     public MediaSessionMgr getMediaSessionMgr(){
    	 if (mMediaSessionMgr == null){
    		 mMediaSessionMgr = ((InviteSession)getSession()).getMediaMgr();
         }
         return mMediaSessionMgr;
     }
}
