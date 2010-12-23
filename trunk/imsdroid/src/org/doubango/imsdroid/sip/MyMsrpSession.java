package org.doubango.imsdroid.sip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.doubango.imsdroid.Model.HistoryMsrpEvent;
import org.doubango.imsdroid.Model.HistoryEvent.StatusType;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.events.EventHandler;
import org.doubango.imsdroid.events.IInviteEventHandler;
import org.doubango.imsdroid.events.IMsrpEventDispatcher;
import org.doubango.imsdroid.events.IMsrpEventHandler;
import org.doubango.imsdroid.events.InviteEventArgs;
import org.doubango.imsdroid.events.MsrpEventArgs;
import org.doubango.imsdroid.events.MsrpEventTypes;
import org.doubango.imsdroid.media.MediaType;
import org.doubango.imsdroid.utils.StringUtils;
import org.doubango.tinyWRAP.ActionConfig;
import org.doubango.tinyWRAP.MsrpCallback;
import org.doubango.tinyWRAP.MsrpEvent;
import org.doubango.tinyWRAP.MsrpMessage;
import org.doubango.tinyWRAP.MsrpSession;
import org.doubango.tinyWRAP.SdpMessage;
import org.doubango.tinyWRAP.SipMessage;
import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.tmsrp_event_type_t;
import org.doubango.tinyWRAP.tmsrp_request_type_t;
import org.doubango.tinyWRAP.twrap_media_type_t;

import android.util.Log;

public class MyMsrpSession extends MyInviteSession implements IMsrpEventDispatcher{

	private static final String TAG = MyMsrpSession.class.getCanonicalName();
	private final MsrpSession session;
	private final MediaType mediaType;
	private final boolean outgoing;
	private String remoteParty;
	private static HashMap<Long, MyMsrpSession> sessions;
	
	private final HistoryMsrpEvent historyEvent;
	
	private OutputStream fileOutputStream;
	private File file;
	private String fileName;
	private long fileLength;
	private String filePath;
	private final MyMsrpCallback callback;
	private final MsrpInviteEventHandler inviteHandler;
	private final long []start;
	private final long []end;
	private final long []total;
	
	// Event Handlers
	private final CopyOnWriteArrayList<IMsrpEventHandler> msrpEventHandlers;
	
	//private static final String CHAT_ACCEPT_TYPES = "text/plain";
	//private static final String FILE_ACCEPT_TYPES = "audio/3gpp video/3gpp application/octet-stream image/jpeg, image/gif image/bmp image/png";
	private static final String FILE_ACCEPT_TYPES = "*";
	
	static {
		MyMsrpSession.sessions = new HashMap<Long, MyMsrpSession>();
	}
	
	public static MyMsrpSession takeIncomingSession(MySipStack sipStack, MsrpSession session, SipMessage message){
		MyMsrpSession msrpSession = null;
		final MediaType mediaType;
		final SdpMessage sdp = message.getSdpMessage();
		final String fromUri = message.getSipHeaderValue("f");
		
		if(sdp == null){
			Log.e(MyMsrpSession.TAG, "Invalid Sdp content");
			return null;
		}
		
		final String fileSelector = sdp.getSdpHeaderAValue("message", "file-selector");
		if(fileSelector == null){
			mediaType = MediaType.Chat;
			// FIXME: This beta version does not support MSRP Chat sessions
			Log.e(MyMsrpSession.TAG, "Chat session rejected");
			return null;
		}
		else{
			mediaType = MediaType.FileTransfer;
		}
		
		switch(mediaType){
			case FileTransfer:
				String name = null;
				long size = 0;
				if(fileSelector != null){
					// file-selector:name:\"Akav1-MD5.7z\" type:application/octet-stream size:14313 hash:sha-1:48:B4:17:55:DE:3D:6F:45:B1:66:4A:B4:B4:B5:BC:01:AB:0C:A9:E8
					// FIXME: name with spaces will fail
					String[] values = fileSelector.split(" ");
					for(String value : values){
						String[] avp = value.split(":");
						if(avp.length >=2){
							if(StringUtils.equals(avp[0], "name", true)){
								name = StringUtils.unquote(avp[1], "\"");
							}
							else if(StringUtils.equals(avp[0], "size", true)){
								try{
									size = Long.parseLong(avp[1]);
								}
								catch(NumberFormatException e){}
							}
						}
					}
					if(name != null){
						final String filePath = String.format("%s/%s", ServiceManager.getStorageService().getContentShareDir(), name);
						final File file = new File(filePath);
						if(!file.exists()){
							try{
								File parent = file.getParentFile();
								parent.mkdirs();
								file.createNewFile();
							}
							catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}
						
						msrpSession = new MyMsrpSession(sipStack, session, mediaType);
						msrpSession.file = file;
						msrpSession.fileName = name;
						msrpSession.fileLength = size;
						msrpSession.historyEvent.setFilePath(filePath);
						msrpSession.setRemoteParty(fromUri); // also update HistoryEvent
						MyMsrpSession.sessions.put(msrpSession.getId(), msrpSession);
					}
				}
			break;
			case Chat:
				break;
		}
		return msrpSession;
	}
	
	public static MyMsrpSession createOutgoingSession(MySipStack sipStack, MediaType mediaType){
		if(mediaType == MediaType.FileTransfer || mediaType == MediaType.Chat){
			MyMsrpSession msrpSession = new MyMsrpSession(sipStack, null, mediaType);
			MyMsrpSession.sessions.put(msrpSession.getId(), msrpSession);
			
			return msrpSession;
		}
		return null;
	}
	
	public static void releaseSession(MyAVSession session){
		if(session != null){
			synchronized(MyMsrpSession.sessions){
				long id = session.getId();
				session.delete();
				MyMsrpSession.sessions.remove(id);
			}
		}
	}
	
	public static void releaseSession(long id){
		synchronized(MyMsrpSession.sessions){
			MyMsrpSession.sessions.remove(id);
			if(MyMsrpSession.sessions.isEmpty()){
				ServiceManager.cancelContShareNotif();
			}
		}
	}
	
	public static Collection<MyMsrpSession> getSessions(){
		synchronized(MyMsrpSession.sessions){
			return MyMsrpSession.sessions.values();
		}
	}
	
	public static MyMsrpSession getSession(long id){
		synchronized(MyMsrpSession.sessions){
			return MyMsrpSession.sessions.get(id);
		}
	}
	
	public static boolean contains(long id){
		synchronized(MyMsrpSession.sessions){
			return MyMsrpSession.sessions.containsKey(id);
		}
	}
	
	private MyMsrpSession(MySipStack sipStack, MsrpSession session, MediaType mediaType){
		super(sipStack);
		
		this.callback = new MyMsrpCallback(this);
		this.inviteHandler = new MsrpInviteEventHandler(this);
		this.mediaType = mediaType;
		this.remoteParty = "sip:unknown@open-ims.test";
		this.historyEvent = new HistoryMsrpEvent(this.remoteParty, this.mediaType==MediaType.FileTransfer);
		if(session == null){
			this.outgoing = true;
			this.historyEvent.setStatus(StatusType.Outgoing);
			this.session = new MsrpSession(sipStack, this.callback);
		}
		else{
			this.outgoing = false;
			this.historyEvent.setStatus(StatusType.Incoming);
			this.session = session;
			this.session.setCallback(this.callback);
		}
		
		
		
		// commons
		this.init();
		
		this.start = new long[1];
		this.end = new long[1];
		this.total = new long[1];
		
		this.msrpEventHandlers = new CopyOnWriteArrayList<IMsrpEventHandler>();
	}
	
	@Override
	protected SipSession getSession() {
		return this.session;
	}
	
	public MediaType getMediaType(){
		return this.mediaType;
	}
	
	public boolean isOutgoing(){
		return this.outgoing;
	}
	
	public String getFileName(){
		return this.fileName;
	}
	
	public String getFilePath(){
		return this.filePath;
	}
	
	public long getFileLength(){
		return this.fileLength;
	}
	
	public long getStart(){
		return this.start[0];
	}
	
	public long getEnd(){
		return this.end[0];
	}
	
	public long getTotal(){
		return this.total[0];
	}
	
	public String getRemoteParty(){
		return this.remoteParty;
	}
	
	public void setRemoteParty(String remoteParty){
		this.remoteParty = remoteParty;
		this.historyEvent.setRemoteParty(remoteParty);
	}
	
	public boolean sendFile(String remoteUri, String path){
		final boolean ret;
		
		this.setRemoteParty(remoteUri);
		
		if(this.mediaType != MediaType.FileTransfer){
			Log.e(MyMsrpSession.TAG, "Invalid media type");
			return false;
		}
		
		if(remoteUri == null || path == null){
			Log.e(MyMsrpSession.TAG, "Invalid parameter");
			return false;
		}
		
		if((this.file = new File(path)) == null || !this.file.exists()){
			Log.e(MyMsrpSession.TAG, String.format("%s not valid path", path));
			return false;
		}
		
		this.fileName = this.file.getName();
		this.fileLength = this.file.length();
		this.filePath = file.getAbsolutePath();
		this.historyEvent.setFilePath(this.filePath);
		final String fileSelector = String.format("name:\"%s\" type:%s size:%d",
				this.fileName, this.getFileType(path), this.fileLength);
		
		 ActionConfig actionConfig = new ActionConfig();
         actionConfig
             .setMediaString(twrap_media_type_t.twrap_media_msrp, "file-path", path)
             .setMediaString(twrap_media_type_t.twrap_media_msrp, "file-selector", fileSelector)
             .setMediaString(twrap_media_type_t.twrap_media_msrp, "accept-types", MyMsrpSession.FILE_ACCEPT_TYPES)
             .setMediaString(twrap_media_type_t.twrap_media_msrp, "file-disposition", "attachment")
             .setMediaString(twrap_media_type_t.twrap_media_msrp, "file-icon", "cid:test@doubango.org")
             .setMediaInt(twrap_media_type_t.twrap_media_msrp, "chunck-duration", 50)
             ;
         ret = this.session.callMsrp(remoteUri, actionConfig);
         actionConfig.delete();
         return ret;
	}
	
	public boolean sendLMessage(byte[] payload, String ContentType){
		if(this.mediaType != MediaType.Chat){
			Log.e(MyMsrpSession.TAG, "Invalid media type");
			return false;
		}
		
		ActionConfig actionConfig = new ActionConfig();
		actionConfig
        .setMediaString(twrap_media_type_t.twrap_media_msrp, "content-type", ContentType);
		boolean ret = this.session.callMsrp(this.remoteParty, actionConfig);
		actionConfig.delete();
		
		return ret;
	}
	
	public void setCallback(MsrpCallback callback){
		this.session.setCallback(callback);
	}
	
	public boolean accept(){		
		return this.session.accept();
	}
	
	public boolean hangUp(){
		if(this.connected){
			return this.session.hangup();
		}
		else{
			return this.session.reject();
		}
	}
	
	private String getFileType(String path){
		String type = "application/octet-stream";
		int index = path.lastIndexOf('.');
		if(index != -1){
			 String extension = path.substring(index+1).toLowerCase();
			 if(extension.equals("jpe") || extension.equals("jpeg") || extension.equals("jpg")){
                type = "image/jpeg";
            }
            else if(extension.equals("gif") || extension.equals("png") || extension.equals("bmp")){
                type = String.format("image/%s", extension);
            }
		}
		return type;
	}
	
	private boolean appendData(byte[] data, int len){
		if(this.mediaType == MediaType.FileTransfer){
			if(!this.outgoing){
				// Create OutputStream
				if(this.fileOutputStream == null){
					try {
						this.fileOutputStream = new FileOutputStream(this.file, false);
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
						return false;
					}
				}
				// Write to the OutputStream
				try {
					this.fileOutputStream.write(data, 0, len);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		
		return true;
	}
	
	/* ============== MSRP Callback ================ */
	static class MyMsrpCallback extends MsrpCallback
    {
		private final static String TAG = MyMsrpCallback.class.getCanonicalName();
		final MyMsrpSession session;
		ByteBuffer buffer;
		byte[]bytes;
		
		MyMsrpCallback(MyMsrpSession session){
			this.session = session;			
		}	 
		
		@Override
        public int OnEvent(MsrpEvent e){
            final MsrpSession _session = e.getSipSession();
            final MsrpMessage _message = e.getMessage();
            
            if(_session == null){
            	Log.e(MyMsrpCallback.TAG, "Invalid event");
            	return -1;
            }
            
            if(_session.getId() != this.session.getId()){ /* MUST never happen ...but who know? */
            	Log.e(MyMsrpCallback.TAG, "Invalid session id");
            	return -2;
            }
            
            if(e.getType() == tmsrp_event_type_t.tmsrp_event_type_disconnected && this.session.isConnected()){
            	this.session.hangUp();
            	return 0;
            }
            
            if(_message == null){
            	return 0;
            }
            
            if(_message.isRequest()){
            	tmsrp_request_type_t type = _message.getRequestType();
            	switch(type){
            		case tmsrp_SEND:
            			long len = _message.getMsrpContentLength();
            			if(len >0){
            				 if(this.buffer ==null || this.buffer.capacity() <len){
            					 // resize or create
            					 this.buffer = ByteBuffer.allocateDirect((int)len);
            					 this.bytes = new byte[(int)len];
            				 }
            				 int read = (int)_message.getMsrpContent(this.buffer, len);
            				 this.buffer.get(this.bytes, 0, read);
            				 this.buffer.rewind();
            				 if(this.session.appendData(this.bytes, read)){
            					 _message.getByteRange(this.session.start, this.session.end, this.session.total);
        	            		MsrpEventArgs eargs = new MsrpEventArgs(this.session.getId(), MsrpEventTypes.DATA);
        	            		eargs.putExtra("start", this.session.start[0]).
        	            		putExtra("end", this.session.end[0]).
        	            		putExtra("total", this.session.total[0]).
        	            		putExtra("session", this.session);
        	            		this.session.onMsrpEvent(eargs);
            				 }
            				 else{
            					 this.session.hangUp();
            				 }
            			}
            			break;
            		case tmsrp_REPORT:
            			Log.d(MyMsrpCallback.TAG, "MSRP REPORT");
            			break;
            		case tmsrp_AUTH:
            			Log.d(MyMsrpCallback.TAG, "MSRP AUTH");
            			break;
            		default:
            			break;
            	}
            }
            else{
            	short code = _message.getCode();
            	if(code >199 && code <300){
            		_message.getByteRange(this.session.start, this.session.end, this.session.total);
            		MsrpEventArgs eargs = new MsrpEventArgs(this.session.getId(), MsrpEventTypes.SUCCESS_200OK);
            		eargs.putExtra("start", this.session.start[0]).
            		putExtra("end", this.session.end[0]).
            		putExtra("total", this.session.total[0]).
            		putExtra("session", this.session);
            		this.session.onMsrpEvent(eargs);
            		
            		if(this.session.end[0]!=-1 && this.session.end[0] == this.session.total[0]){
            			if(this.session.mediaType == MediaType.FileTransfer){
            				if(this.session.outgoing){
            					this.session.hangUp();
            				}
            			}
            			else{
            				
            			}
            		}
            	}
            	else if(code>=300){
            		this.session.hangUp();
            	}
            } 
            return 0;
        }
    }		
	

	/* ============================ IInviteEventHandler =========================*/
	static class MsrpInviteEventHandler implements IInviteEventHandler
	{
		private final static String TAG = MsrpInviteEventHandler.class.getCanonicalName();
		final MyMsrpSession session;
		
		MsrpInviteEventHandler(MyMsrpSession session){
			ServiceManager.getSipService().addInviteEventHandler(this);
			this.session = session;
		}
		
		@Override
		protected void finalize() throws Throwable {
			Log.d(MsrpInviteEventHandler.TAG, "finalize()");
			ServiceManager.getSipService().removeInviteEventHandler(this);
			super.finalize();
		}
		
		@Override
		public long getSessionId() {
			return this.session.getId();
		}
		
		@Override
		public boolean canHandle(long id) {
			return (this.session.getId() == id);
		}

		@Override
		public boolean onInviteEvent(Object sender, InviteEventArgs e) {
			final MsrpEventArgs eargs;
			switch(e.getType()){
				case INPROGRESS:
					break;
				case CONNECTED:
					eargs = new MsrpEventArgs(this.session.getId(), MsrpEventTypes.CONNECTED);
            		eargs.putExtra("session", this.session);
            		this.session.onMsrpEvent(eargs);
					break;
				case DISCONNECTED:
				case TERMWAIT:
						if(!MyMsrpSession.contains(e.getSessionId())){
							return true; // already released by termwait
						}
						eargs = new MsrpEventArgs(this.session.getId(), MsrpEventTypes.DISCONNECTED);
	            		eargs.putExtra("session", this.session);
	            		this.session.onMsrpEvent(eargs);
	            		
	            		if(this.session.fileOutputStream != null){
	            			try {
								this.session.fileOutputStream.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
	            			this.session.fileOutputStream = null;
	            		}
	            		
	            		if(this.session.mediaType == MediaType.FileTransfer && this.session.end[0] != this.session.total[0]){
	            			this.session.historyEvent.setStatus(StatusType.Failed);
	            		}
	            		
	            		MyMsrpSession.releaseSession(e.getSessionId());
	            		ServiceManager.getHistoryService().addEvent(this.session.historyEvent);
					break;
				default:
					break;
			}
			
			return true;
		}
	}
	
	/* ================ IMsrpEventDispatcher ============== */
	@Override
	public boolean addMsrpEventHandler(IMsrpEventHandler handler) {
		return EventHandler.addEventHandler(this.msrpEventHandlers, handler);
	}

	@Override
	public boolean removeMsrpEventHandler(IMsrpEventHandler handler) {
		return EventHandler.removeEventHandler(this.msrpEventHandlers, handler);
	}
	
	private synchronized void onMsrpEvent(final MsrpEventArgs eargs) {
		/* DO NOT Create new thread */
		for(IMsrpEventHandler handler : this.msrpEventHandlers){
			if(handler.canHandle(eargs.getId())){
				if (!handler.onMsrpEvent(this, eargs)) {
					Log.w(MyMsrpSession.TAG, "onMsrpEvent failed");
				}
			}
		}
	}
}