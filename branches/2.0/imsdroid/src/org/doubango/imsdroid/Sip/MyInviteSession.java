package org.doubango.imsdroid.Sip;

import org.doubango.imsdroid.Media.MediaType;
import org.doubango.tinyWRAP.InviteSession;
import org.doubango.tinyWRAP.MediaSessionMgr;


public abstract class MyInviteSession extends MySipSession{
	protected MediaType mMediaType;
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

	public MyInviteSession(MySipStack sipStack) {
		super(sipStack);
		
		mState = InviteState.NONE;
	}
	
	 public MediaType getMediaType(){
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
