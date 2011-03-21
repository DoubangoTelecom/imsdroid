package org.doubango.ngn.sip;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.tinyWRAP.InviteSession;
import org.doubango.tinyWRAP.MediaSessionMgr;


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

	public NgnInviteSession(NgnSipStack sipStack) {
		super(sipStack);
		
		mState = InviteState.NONE;
	}
	
	 public NgnMediaType getMediaType(){
         return mMediaType;
     }

     public InviteState getState(){
         return mState;
     }
     
     public void setState(InviteState state){
    	 mState = state;
     }

     public boolean isActive(){
    	 return mState != InviteState.NONE
         && mState != InviteState.TERMINATING 
         && mState != InviteState.TERMINATED;
     }

     public MediaSessionMgr getMediaSessionMgr(){
    	 if (mMediaSessionMgr == null){
    		 mMediaSessionMgr = ((InviteSession)getSession()).getMediaMgr();
         }
         return mMediaSessionMgr;
     }
}
