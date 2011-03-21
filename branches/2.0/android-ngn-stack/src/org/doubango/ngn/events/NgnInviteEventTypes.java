package org.doubango.ngn.events;

public enum NgnInviteEventTypes {
	INCOMING,
    INPROGRESS,
    RINGING,
    EARLY_MEDIA,
    CONNECTED,
    TERMWAIT,
    TERMINATED,
    LOCAL_HOLD_OK,
    LOCAL_HOLD_NOK,
    LOCAL_RESUME_OK,
    LOCAL_RESUME_NOK,
    REMOTE_HOLD,
    REMOTE_RESUME
}
