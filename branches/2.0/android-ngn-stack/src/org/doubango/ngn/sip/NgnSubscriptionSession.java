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

import org.doubango.ngn.utils.NgnContentType;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.SubscriptionSession;

public class NgnSubscriptionSession extends NgnSipSession{
	private final SubscriptionSession mSession;
	private final EventPackageType mPackage;
	
	public enum EventPackageType {
		None,
		
	    Conference, 
	    Dialog, 
	    MessageSummary, 
	    Presence, 
	    PresenceList, 
	    Reg, 
	    SipProfile, 
	    UAProfile, 
	    WInfo, 
	    XcapDiff
    }
	
	private final static NgnObservableHashMap<Long, NgnSubscriptionSession> sSessions = new NgnObservableHashMap<Long, NgnSubscriptionSession>(
			true);
	
	public static NgnSubscriptionSession createOutgoingSession(NgnSipStack sipStack, String toUri, EventPackageType eventPackage) {
		synchronized (sSessions) {
			final NgnSubscriptionSession subSession = new NgnSubscriptionSession(sipStack, toUri, eventPackage);
			sSessions.put(subSession.getId(), subSession);
			return subSession;
		}
	}

	public static void releaseSession(NgnSubscriptionSession session) {
		synchronized (sSessions) {
			if (session != null && sSessions.containsKey(session.getId())) {
				long id = session.getId();
				session.decRef();
				sSessions.remove(id);
			}
		}
	}

	public static void releaseSession(long id) {
		synchronized (sSessions) {
			NgnSubscriptionSession session = NgnSubscriptionSession.getSession(id);
			if (session != null) {
				session.decRef();
				sSessions.remove(id);
			}
		}
	}

	public static NgnSubscriptionSession getSession(long id) {
		synchronized (sSessions) {
			if (sSessions.containsKey(id))
				return sSessions.get(id);
			else
				return null;
		}
	}

	public static int getSize() {
		synchronized (sSessions) {
			return sSessions.size();
		}
	}

	public static boolean hasSession(long id) {
		synchronized (sSessions) {
			return sSessions.containsKey(id);
		}
	}
	
	protected NgnSubscriptionSession(NgnSipStack sipStack, String toUri, EventPackageType eventPackage){
		super(sipStack);
		mSession = new SubscriptionSession(sipStack);
		super.init();
		
		switch ((mPackage = eventPackage))
        {
            case Conference:
            	mSession.addHeader("Event", "conference");
            	mSession.addHeader("Accept", NgnContentType.CONFERENCE_INFO);
                break;
            case Dialog:
            	mSession.addHeader("Event", "dialog");
            	mSession.addHeader("Accept", NgnContentType.DIALOG_INFO);
                break;
            case MessageSummary:
            	mSession.addHeader("Event", "message-summary");
            	mSession.addHeader("Accept", NgnContentType.MESSAGE_SUMMARY);
                break;
            case Presence:
            case PresenceList:
            default:
            	mSession.addHeader("Event", "presence");
                if (eventPackage == EventPackageType.PresenceList){
                	mSession.addHeader("Supported", "eventlist");
                }
                mSession.addHeader("Accept",
                        String.format("%s, %s, %s, %s",
                        		NgnContentType.MULTIPART_RELATED,
                                NgnContentType.PIDF,
                                NgnContentType.RLMI,
                                NgnContentType.RPID
                                ));
                break;
            case Reg:
            	mSession.addHeader("Event", "reg");
            	mSession.addHeader("Accept", NgnContentType.REG_INFO);
                // 3GPP TS 24.229 5.1.1.6 User-initiated deregistration
            	mSession.setSilentHangup(true);
                break;
            case SipProfile:
            	mSession.addHeader("Event", "sip-profile");
            	mSession.addHeader("Accept", NgnContentType.OMA_DEFERRED_LIST);
                break;
            case UAProfile:
            	mSession.addHeader("Event", "ua-profile");
            	mSession.addHeader("Accept", NgnContentType.XCAP_DIFF);
                break;
            case WInfo:
            	mSession.addHeader("Event", "presence.winfo");
            	mSession.addHeader("Accept", NgnContentType.WATCHER_INFO);
                break;
            case XcapDiff:
            	mSession.addHeader("Event", "xcap-diff");
            	mSession.addHeader("Accept", NgnContentType.XCAP_DIFF);
                break;
        }
		
		super.setSigCompId(sipStack.getSigCompId());
		super.setToUri(toUri);
		super.setFromUri(toUri);
	}

	@Override
	protected SipSession getSession() {
		return mSession;
	}
	
	public boolean subscribe() {
		return mSession.subscribe();
	}

	public boolean unSubscribe() {
		return mSession.unSubscribe();
	}

	public EventPackageType getEventPackage() {
		return mPackage;
	}
}
