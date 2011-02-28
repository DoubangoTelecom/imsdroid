package org.doubango.imsdroid.Sip;

import java.nio.ByteBuffer;

import org.doubango.imsdroid.Utils.ContentType;
import org.doubango.imsdroid.Utils.ObservableHashMap;
import org.doubango.imsdroid.Utils.UriUtils;
import org.doubango.tinyWRAP.MessagingSession;
import org.doubango.tinyWRAP.RPMessage;
import org.doubango.tinyWRAP.SMSEncoder;
import org.doubango.tinyWRAP.SipMessage;
import org.doubango.tinyWRAP.SipSession;

import android.util.Log;

public class MyMessagingSession extends MySipSession {
	private static String TAG = MyMessagingSession.class.getCanonicalName();
	
	private final MessagingSession mSession;
	private static int SMS_MR = 0;
	
	private final static ObservableHashMap<Long, MyMessagingSession> sessions = new ObservableHashMap<Long, MyMessagingSession>(true);
	
	public static MyMessagingSession takeIncomingSession(MySipStack sipStack, MessagingSession session, SipMessage sipMessage){
		final String toUri = sipMessage==null ? null: sipMessage.getSipHeaderValue("f");
		MyMessagingSession imSession = new MyMessagingSession(sipStack, session, toUri);
		MyMessagingSession.sessions.put(imSession.getId(), imSession);
        return imSession;
    }

    public static MyMessagingSession createOutgoingSession(MySipStack sipStack, String toUri){
        synchronized (MyMessagingSession.sessions){
            final MyMessagingSession imSession = new MyMessagingSession(sipStack, null, toUri);
            MyMessagingSession.sessions.put(imSession.getId(), imSession);
            return imSession;
        }
    }
    
    public static void releaseSession(MyMessagingSession session){
		synchronized (MyMessagingSession.sessions){
            if (session != null && MyMessagingSession.sessions.containsKey(session.getId())){
                long id = session.getId();
                session.decRef();
                MyMessagingSession.sessions.remove(id);
            }
        }
    }

    public static void releaseSession(long id){
		synchronized (MyMessagingSession.sessions){
			MyMessagingSession session = MyMessagingSession.getSession(id);
            if (session != null){
                session.decRef();
                MyMessagingSession.sessions.remove(id);
            }
        }
    }
    
	public static MyMessagingSession getSession(long id) {
		synchronized (MyMessagingSession.sessions) {
			if (MyMessagingSession.sessions.containsKey(id))
				return MyMessagingSession.sessions.get(id);
			else
				return null;
		}
	}

	public static int getSize(){
        synchronized (MyMessagingSession.sessions){
            return MyMessagingSession.sessions.size();
        }
    }
	
    public static boolean hasSession(long id){
        synchronized (MyMessagingSession.sessions){
            return MyMessagingSession.sessions.containsKey(id);
        }
    }
    
	protected MyMessagingSession(MySipStack sipStack, MessagingSession session, String toUri) {
		super(sipStack);
        mSession = session == null ? new MessagingSession(sipStack) : session;

        super.init();
        super.setSigCompId(sipStack.getSigCompId());
        super.setToUri(toUri);
	}

	@Override
	protected SipSession getSession() {
		return mSession;
	}
	
	public boolean SendBinaryMessage(String text, String SMSC){
        String SMSCPhoneNumber;
        String dstPhoneNumber;
        String dstSipUri = super.getToUri();

        if ((SMSCPhoneNumber = UriUtils.getValidPhoneNumber(SMSC)) != null && (dstPhoneNumber = UriUtils.getValidPhoneNumber(dstSipUri)) != null){
            super.setToUri(SMSC);
            super.addHeader("Content-Type", ContentType.SMS_3GPP);
            super.addHeader("Content-Transfer-Encoding", "binary");
            super.addCaps("+g.3gpp.smsip");       
                    
            RPMessage rpMessage;
            //if(ServiceManager.getConfigurationService().getBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.HACK_SMS, false)){
                //    rpMessage = SMSEncoder.encodeDeliver(++ScreenSMSCompose.SMS_MR, SMSCPhoneNumber, dstPhoneNumber, new String(content));
                //    session.addHeader("P-Asserted-Identity", SMSC);
            //}
            //else{
                    rpMessage = SMSEncoder.encodeSubmit(++MyMessagingSession.SMS_MR, SMSCPhoneNumber, dstPhoneNumber, text);
            //}
        
            long rpMessageLen = rpMessage.getPayloadLength();
            ByteBuffer payload = ByteBuffer.allocateDirect((int)rpMessageLen);
            long payloadLength = rpMessage.getPayload(payload, (long)payload.capacity());
            boolean ret = mSession.send(payload, payloadLength);
            rpMessage.delete();
            if(MyMessagingSession.SMS_MR >= 255){
                 MyMessagingSession.SMS_MR = 0;
            }

            return ret;
        }
        else{
            Log.e(TAG, String.format("SMSC=%s or RemoteUri=%s is invalid", SMSC, dstSipUri));
            return sendTextMessage(text);
        }
    }

    public boolean sendTextMessage(String text){
        super.addHeader("Content-Type", ContentType.TEXT_PLAIN);
        byte[] bytes = text.getBytes();
        ByteBuffer payload = ByteBuffer.allocateDirect(bytes.length);
        payload.put(bytes);
        return mSession.send(payload, payload.capacity());
    }
    
    public boolean accept() {
        return mSession.accept();
      }

      public boolean reject() {
        return mSession.reject();
      }
}
