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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnMsrpEventArgs;
import org.doubango.ngn.events.NgnMsrpEventTypes;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryMsrpEvent;
import org.doubango.ngn.utils.NgnContentType;
import org.doubango.ngn.utils.NgnListUtils;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.tinyWRAP.ActionConfig;
import org.doubango.tinyWRAP.MediaContent;
import org.doubango.tinyWRAP.MediaContentCPIM;
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

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * MSRP session used to share content or send large IM
 */
public class NgnMsrpSession extends NgnInviteSession {
	private static String TAG = NgnMsrpSession.class.getCanonicalName();

	private static final String CHAT_ACCEPT_TYPES = "text/plain message/CPIM";
	private static final String CHAT_ACCEPT_WRAPPED_TYPES = "text/plain image/jpeg image/gif image/bmp image/png";
	private static final String FILE_ACCEPT_TYPES = "message/CPIM application/octet-stream";
	private static final String FILE_ACCEPT_WRAPPED_TYPES = "application/octet-stream image/jpeg image/gif image/bmp image/png";
	private static final int CHUNK_DURATION = 50;

	private Context mContext;
	private final MsrpSession mSession;
	private final NgnMsrpCallback mCallback;
	private final long[] mStart, mEnd, mTotal;
	private String mFilePath;
	private String mFileName;
	private String mFileType;
	private boolean mFailureReport;
	private boolean mSuccessReport;
	private boolean mOmaFinalDeliveryReport;
	private OutputStream mOutFileStream;
	private List<PendingMessage> mPendingMessages;
	
	private final NgnHistoryMsrpEvent mHistoryEvent;

	private final static NgnObservableHashMap<Long, NgnMsrpSession> sSessions = new NgnObservableHashMap<Long, NgnMsrpSession>(
			true);
	
	public final static NgnObservableHashMap<Long, NgnMsrpSession> getSessions(){
		synchronized(sSessions){
			return sSessions;
		}
	}

	public static NgnMsrpSession takeIncomingSession(NgnSipStack sipStack, MsrpSession session, SipMessage message){
		NgnMsrpSession msrpSession = null;
        NgnMediaType mediaType;
        SdpMessage sdp = message.getSdpMessage();
        String fromUri = message.getSipHeaderValue("f");
            
        if(NgnStringUtils.isNullOrEmpty(fromUri)){
           Log.e(TAG,"Invalid fromUri");
           return null;
        }

	    if(sdp == null){
	       Log.e(TAG,"Invalid Sdp content");
	       return null;
	    }
            
        String fileSelector = sdp.getSdpHeaderAValue("message", "file-selector");
        mediaType = NgnStringUtils.isNullOrEmpty(fileSelector) ? NgnMediaType.Chat : NgnMediaType.FileTransfer;

        if (mediaType == NgnMediaType.Chat){
            msrpSession = NgnMsrpSession.createIncomingSession(sipStack, session, mediaType, fromUri);
        }
        else{
            String name = null;
            String type = null;
         // file-selector:name:\"Akav1-MD5.7z\" type:application/octet-stream size:14313 hash:sha-1:48:B4:17:55:DE:3D:6F:45:B1:66:4A:B4:B4:B5:BC:01:AB:0C:A9:E8
            // FIXME: name with spaces will fail
            String[] values = fileSelector.split(" ");
            for(String value : values){
                String[] avp = value.split(":");
                if(avp.length >=2){
                    if(NgnStringUtils.equals(avp[0], "name", true)){
                        name = NgnStringUtils.unquote(avp[1], "\"");
                    }
                    else if(NgnStringUtils.equals(avp[0], "type", true)){
                        type = avp[1];
                    }
                }
            }
            if(NgnStringUtils.isNullOrEmpty(name)){
                Log.e(TAG,"Invalid file name");
                return null;
            }

            msrpSession = NgnMsrpSession.createIncomingSession(sipStack, session, mediaType, fromUri);
            msrpSession.mFilePath = String.format("%s/%s", NgnEngine.getInstance().getStorageService().getContentShareDir(), name);
            msrpSession.mFileType = type;
            msrpSession.mFileName = name;
        }

        return msrpSession;
    }

	public static NgnMsrpSession createIncomingSession(NgnSipStack sipStack, MsrpSession session, NgnMediaType mediaType, String remoteUri) {
		if (mediaType == NgnMediaType.FileTransfer || mediaType == NgnMediaType.Chat) {
			NgnMsrpSession msrpSession = new NgnMsrpSession(sipStack, session,mediaType, remoteUri, InviteState.INCOMING);
			sSessions.put(msrpSession.getId(), msrpSession);
			return msrpSession;
		}
		return null;
	}

	public static NgnMsrpSession createOutgoingSession(NgnSipStack sipStack, NgnMediaType mediaType, String remoteUri) {
		if (mediaType == NgnMediaType.FileTransfer || mediaType == NgnMediaType.Chat) {
			NgnMsrpSession msrpSession = new NgnMsrpSession(sipStack, null, mediaType, remoteUri, InviteState.INPROGRESS);
			sSessions.put(msrpSession.getId(), msrpSession);
			return msrpSession;
		}
		return null;
	}

	public static void releaseSession(NgnMsrpSession session) {
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
			NgnMsrpSession session = NgnMsrpSession.getSession(id);
			if (session != null) {
				session.decRef();
				sSessions.remove(id);
			}
		}
	}

	public static NgnMsrpSession getSession(long id) {
		synchronized (sSessions) {
			if (sSessions.containsKey(id))
				return sSessions.get(id);
			else
				return null;
		}
	}
	
	public static NgnMsrpSession getSession(NgnPredicate<NgnMsrpSession> predicate) {
		synchronized (sSessions) {
			return NgnListUtils.getFirstOrDefault(sSessions.values(), predicate);
		}
	}

	public static int getSize() {
		synchronized (sSessions) {
			return sSessions.size();
		}
	}
	
	public static int getSize(NgnPredicate<NgnMsrpSession> predicate) {
		synchronized (sSessions) {
			return NgnListUtils.filter(sSessions.values(), predicate).size();
		}
	}
	
	public static boolean hasActiveSession(NgnPredicate<NgnMsrpSession> predicate){
    	synchronized (sSessions){
    		final List<NgnMsrpSession> mysessions = NgnListUtils.filter(sSessions.values(), predicate);
	    	for(NgnMsrpSession session : mysessions){
	    		if(session.isActive()){
	    			return true;
	    		}
	    	}
    	}
    	return false;
    }

	public static boolean hasSession(long id) {
		synchronized (sSessions) {
			return sSessions.containsKey(id);
		}
	}

	protected NgnMsrpSession(NgnSipStack sipStack, MsrpSession session, NgnMediaType mediaType, String toUri, InviteState callState) {
		super(sipStack);
		mStart = new long[1];
		mEnd = new long[1];
		mTotal = new long[1];
		super.mMediaType = mediaType;
		mCallback = new NgnMsrpCallback(this);
		if (session == null) {
			super.mOutgoing = true;
			mSession = new MsrpSession(sipStack, mCallback);
		} else {
			super.mOutgoing = false;
			mSession = session;
			mSession.setCallback(mCallback);
		}
		
		switch (mediaType){
		    case Chat:
		    default:
		       mHistoryEvent = null;//new NgnHistoryChatEvent(toUri);
		       break;
		    case FileTransfer:
		        mHistoryEvent = null;//new NgnHistoryFileTransferEvent(toUri);
		        break; 
		}
		
		super.init();
		super.setSigCompId(sipStack.getSigCompId());
		super.setToUri(toUri);
		super.setState(callState);
	}

	@Override
	protected void finalize() throws Throwable {
		Log.d(TAG, "finalize()");
		if (mOutFileStream != null) {
			synchronized (mOutFileStream) {
				mOutFileStream.close();
			}
		}
		super.finalize();
	}

	@Override
	protected SipSession getSession() {
		return mSession;
	}
	
	@Override
	protected  NgnHistoryEvent getHistoryEvent(){
		 return mHistoryEvent;
	}
	
	public void setContext(Context context){
		mContext = context;
	}
	
	public Context getContext(Context context){
		return mContext;
	}
	
	public long getStart(){
		return mStart[0];
	}
	
	public long getEnd(){
		return mEnd[0];
	}
	
	public long getTotal(){
		return mTotal[0];
	}
	
	public String getFileName(){
        return mFileName;
	}

	public String getFilePath() {
		return mFilePath;
	}

	public boolean isFailureReport() {
		return mFailureReport;
	}

	public void setFailureReport(boolean bFailureReport) {
		mFailureReport = bFailureReport;
	}

	public boolean isSuccessReport() {
		return mSuccessReport;
	}

	public void setSuccessReport(boolean bSuccessReport) {
		mSuccessReport = bSuccessReport;
	}

	public boolean isOmaFinalDeliveryReport() {
		return mOmaFinalDeliveryReport;
	}

	public void setOmaFinalDeliveryReport(boolean bOmaFinalDeliveryReport) {
		mOmaFinalDeliveryReport = bOmaFinalDeliveryReport;
	}

	public boolean accept() {
		if (super.getState() == InviteState.INCOMING
				&& super.getMediaType() == NgnMediaType.FileTransfer) {
			try {
				final File newFile = new File(mFilePath);
				if (!newFile.exists()) {
					File parentFile = newFile.getParentFile();
					parentFile.mkdirs();
					newFile.createNewFile();
				}
				if (mOutFileStream != null) {
					synchronized (mOutFileStream) {
						mOutFileStream.close();
					}
				}
				mOutFileStream = new FileOutputStream(
						newFile.getAbsolutePath(), false);
			} catch (Exception e) {
				Log.e(TAG, e.toString());
				hangUp();
				return false;
			}
		}
		return mSession.accept();
	}

	public boolean hangUp() {
		if (super.isConnected()) {
			if (mOutFileStream != null) {
				synchronized (mOutFileStream) {
					try {
						mOutFileStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mOutFileStream = null;
				}
			}
			return mSession.hangup();
		} else {
			return mSession.reject();
		}
	}

	public boolean sendFile(String path) {
		if (NgnStringUtils.isNullOrEmpty(path) || !isFileExists(path)) {
			Log.e(TAG, String.format("File (%s) doesn't exist", path));
			return false;
		}

		if (super.getMediaType() != NgnMediaType.FileTransfer) {
			Log.e(TAG, "Invalid media type");
			return false;
		}

		final File file = new File(path);
		mFileName = file.getName();
		mFilePath = file.getAbsolutePath();
		mFileType = getFileType(mFilePath);
		String fileSelector = String.format("name:\"%s\" type:%s size:%d", mFileName, mFileType, file.length());

		ActionConfig config = new ActionConfig();
		config.setMediaString(twrap_media_type_t.twrap_media_msrp, "file-path",mFilePath)
			.setMediaString(twrap_media_type_t.twrap_media_msrp, "file-selector",fileSelector)
			.setMediaString(twrap_media_type_t.twrap_media_msrp, "accept-types",FILE_ACCEPT_TYPES)
			.setMediaString(twrap_media_type_t.twrap_media_msrp, "accept-wrapped-types", FILE_ACCEPT_WRAPPED_TYPES)
			.setMediaString(twrap_media_type_t.twrap_media_msrp, "file-disposition", "attachment")
			.setMediaString(twrap_media_type_t.twrap_media_msrp, "file-icon","cid:test@doubango.org")
			.setMediaString(twrap_media_type_t.twrap_media_msrp, "Failure-Report", mFailureReport ? "yes" : "no")
			.setMediaString(twrap_media_type_t.twrap_media_msrp, "Success-Report", mSuccessReport ? "yes" : "no")
			.setMediaInt(twrap_media_type_t.twrap_media_msrp, "chunck-duration", CHUNK_DURATION);
		boolean ret = mSession.callMsrp(super.getRemotePartyUri(), config);
		config.delete();
		return ret;
	}

	public boolean SendMessage(String message) {
		// if content-type is null, then the application will use the neg. ctype
		return sendMessage(message, null, null);
	}

	public boolean sendMessage(String message, String contentType, String wContentType) {
		if (NgnStringUtils.isNullOrEmpty(message)) {
			Log.e(TAG, "Null or empty message");
			return false;
		}

		if (super.getMediaType() != NgnMediaType.Chat) {
			Log.e(TAG, "Invalid media type");
			return false;
		}

		if (super.isConnected()) {
			ActionConfig config = new ActionConfig();
			if (!NgnStringUtils.isNullOrEmpty(contentType)) {
				config.setMediaString(twrap_media_type_t.twrap_media_msrp, "content-type", contentType);
			}
			if (!NgnStringUtils.isNullOrEmpty(wContentType)) {
				config.setMediaString(twrap_media_type_t.twrap_media_msrp, "w-content-type", wContentType);
			}
			// config.setMediaString(twrap_media_type_t.twrap_media_msrp,
			// "content-type", contentType);
			// == OR ==
			// config.setMediaString(twrap_media_type_t.twrap_media_msrp,
			// "content-type", "message/CPIM")
			// .setMediaString(twrap_media_type_t.twrap_media_msrp,
			// "w-content-type", "text/plain");
			byte[] payload = message.getBytes();
			final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(payload.length);
			byteBuffer.put(payload);
			boolean ret = mSession.sendMessage(byteBuffer, (long) payload.length, config);
			config.delete();
			return ret;
		} else {
			if (mPendingMessages == null) {
				mPendingMessages = new ArrayList<PendingMessage>();
			}
			mPendingMessages.add(new PendingMessage(message, contentType,
					wContentType));

			ActionConfig config = new ActionConfig();
			config.setMediaString(twrap_media_type_t.twrap_media_msrp,
					"accept-types", CHAT_ACCEPT_TYPES).setMediaString(
					twrap_media_type_t.twrap_media_msrp,
					"accept-wrapped-types", CHAT_ACCEPT_WRAPPED_TYPES)
					.setMediaString(twrap_media_type_t.twrap_media_msrp,
							"Failure-Report", mFailureReport ? "yes" : "no")
					.setMediaString(twrap_media_type_t.twrap_media_msrp,
							"Success-Report", mSuccessReport ? "yes" : "no")
					.setMediaInt(twrap_media_type_t.twrap_media_msrp,
							"chunck-duration", CHUNK_DURATION);

			boolean ret = mSession.callMsrp(super.getRemotePartyUri(), config);
			config.delete();
			return ret;
		}
	}

	private boolean isFileExists(String path) {
		if (!NgnStringUtils.isNullOrEmpty(path)) {
			final File file = new File(path);
			return file.exists();
		}
		return false;
	}

	private String getFileType(String path) {
		String type = "application/octet-stream";
		int index = path.lastIndexOf('.');
		if (index != -1) {
			String extension = path.substring(index + 1).toLowerCase();
			if (extension.equals("jpe") || extension.equals("jpeg")
					|| extension.equals("jpg")) {
				type = "image/jpeg";
			} else if (extension.equals("gif") || extension.equals("png")
					|| extension.equals("bmp")) {
				type = String.format("image/%s", extension);
			}
		}
		return type;
	}

	//
	// PendingMessage
	//
	static class PendingMessage {
		final String mMessage;
		final String mContentType;
		final String mWContentType;

		PendingMessage(String message, String contentType, String wContentType) {
			mMessage = message;
			mContentType = contentType;
			mWContentType = wContentType;
		}

		String getMessage() {
			return mMessage;
		}

		String getContentType() {
			return mContentType;
		}

		String getWContentType() {
			return mWContentType;
		}
	}

	//
	// NgnMsrpCallback
	//
	static class NgnMsrpCallback extends MsrpCallback {
		final NgnMsrpSession mSession;
		final Context mAppContext;
		private ByteBuffer mTempBuffer;
		private ByteArrayOutputStream mChatStream;
		private String mContentType;
		private String mWContentType;
		private long mSessionId;
		private byte[]mData;

		NgnMsrpCallback(NgnMsrpSession session) {
			super();
			mSession = session;
			mAppContext = NgnApplication.getContext();
			mSessionId = -1;
		}
		
		private long getSessionId(){
			if(mSessionId == -1 && mSession.getSession() != null){
				mSessionId = mSession.getSession().getId();
			}
			return mSessionId;
		}
			
		private boolean appendData(byte[] data, int len){
            try{
                if(mSession.getMediaType() == NgnMediaType.Chat){
                    if (mChatStream == null){
                    	mChatStream = new ByteArrayOutputStream(); // Expandable memory stream
                    }
                    mChatStream.write(data, 0, len);
                }
                else if(mSession.getMediaType() == NgnMediaType.FileTransfer){
                    if (mSession.mOutFileStream == null){
                        Log.e(TAG,"Null FileStream");
                        return false;
                    }
                    else{
                        synchronized(mSession.mOutFileStream){
                        	mSession.mOutFileStream.write(data, 0, (int)len);
                        }
                    }
                }
            }
            catch (Exception e){
                Log.e(TAG,e.toString());
                return false;
            }
            return true;
		}
		
		private void processResponse(MsrpMessage message) {
			final short code = message.getCode();
			final boolean bIsFileTransfer = mSession.getMediaType() == NgnMediaType.FileTransfer;
			if(mSession.mContext != null){
				synchronized (mSession.mContext) {
					if (code >= 200 && code <= 299) {
						// File Transfer => ProgressBar
						if (bIsFileTransfer) {
							message.getByteRange(mSession.mStart, mSession.mEnd, mSession.mTotal);
							NgnMsrpEventArgs eargs = new NgnMsrpEventArgs(getSessionId(),NgnMsrpEventTypes.SUCCESS_2XX);
							final Intent intent = new Intent(NgnMsrpEventArgs.ACTION_MSRP_EVENT);
							intent.putExtra(NgnMsrpEventArgs.EXTRA_EMBEDDED,eargs);
							intent.putExtra(NgnMsrpEventArgs.EXTRA_BYTE_RANGE_START,mSession.mStart[0]);
							intent.putExtra(NgnMsrpEventArgs.EXTRA_BYTE_RANGE_END,mSession.mEnd[0]);
							intent.putExtra(NgnMsrpEventArgs.EXTRA_BYTE_RANGE_TOTAL,mSession.mTotal[0]);
							intent.putExtra(NgnMsrpEventArgs.EXTRA_RESPONSE_CODE, code);
							mSession.mContext.sendBroadcast(intent);
						}
					} else if (code >= 300) {
						NgnMsrpEventArgs eargs = new NgnMsrpEventArgs(getSessionId(), NgnMsrpEventTypes.ERROR);
						final Intent intent = new Intent(NgnMsrpEventArgs.ACTION_MSRP_EVENT);
						intent.putExtra(NgnMsrpEventArgs.EXTRA_EMBEDDED, eargs);
						intent.putExtra(NgnMsrpEventArgs.EXTRA_RESPONSE_CODE, code);
						mSession.mContext.sendBroadcast(intent);
					}
				}
			}
			// HangUp session if required
			if(code >199 && code <300){                
                if(mSession.mEnd[0]>=0 && mSession.mEnd[0] == mSession.mTotal[0]){
                    if(bIsFileTransfer && mSession.isOutgoing()){
                         mSession.hangUp();
                    }
                }
	        } else if(code>=300){
	        	mSession.hangUp();
	        }
		}
		
		private void processRequest(MsrpMessage message){
            tmsrp_request_type_t type = message.getRequestType();

            switch (type){
                case tmsrp_SEND:
                    {
                        final long clen = message.getMsrpContentLength();
                        long read = 0;
                        if(clen == 0){
                            Log.d(TAG,"Empty MSRP message");
                            return;
                        }

                        if (mTempBuffer == null || mTempBuffer.capacity() < clen){
                        	mTempBuffer = ByteBuffer.allocateDirect((int)clen);
                        }

                        read = message.getMsrpContent(mTempBuffer, mTempBuffer.capacity());
                        if (message.isFirstChunck()){
                            mContentType = message.getMsrpHeaderValue("Content-Type");
                            if (!NgnStringUtils.isNullOrEmpty(mContentType) && NgnStringUtils.startsWith(mContentType,NgnContentType.CPIM, true)) {
                                MediaContentCPIM mediaContent = MediaContent.parse(mTempBuffer, read);
                                if (mediaContent != null){
                                    mWContentType = mediaContent.getHeaderValue("Content-Type");
                                    read = mediaContent.getPayload(mTempBuffer, mTempBuffer.capacity());
                                    mediaContent.delete();
                                }
                            }
                        }
                        if(mData == null || mData.length<read){
                        	mData = new byte[(int)read];
                        }
                        mTempBuffer.get(mData, 0, (int)read);
                        appendData(mData, (int)read);
                        mTempBuffer.rewind();

                        // File Transfer => ProgressBar
                        if (mSession.getMediaType() == NgnMediaType.FileTransfer){
                        	if(mSession.mContext != null){
                        		synchronized (mSession.mContext) {
    	                            message.getByteRange(mSession.mStart, mSession.mEnd, mSession.mTotal);
    	                            NgnMsrpEventArgs eargs = new NgnMsrpEventArgs(getSessionId(),NgnMsrpEventTypes.DATA);
    								final Intent intent = new Intent(NgnMsrpEventArgs.ACTION_MSRP_EVENT);
    								intent.putExtra(NgnMsrpEventArgs.EXTRA_EMBEDDED,eargs);
    								intent.putExtra(NgnMsrpEventArgs.EXTRA_BYTE_RANGE_START, mSession.mStart[0]);
    								intent.putExtra(NgnMsrpEventArgs.EXTRA_BYTE_RANGE_END, mSession.mEnd[0]);
    								intent.putExtra(NgnMsrpEventArgs.EXTRA_BYTE_RANGE_TOTAL, mSession.mTotal[0]);
    								intent.putExtra(NgnMsrpEventArgs.EXTRA_REQUEST_TYPE, "SEND");
    								mSession.mContext.sendBroadcast(intent);
								}
                        	}
                        }

                        if(message.isLastChunck()){
                            if(mSession.getMediaType() == NgnMediaType.Chat && mChatStream != null){
                            	final Context context = mSession.mContext == null ? mAppContext : mSession.mContext;
                            	if(context != null){
                            		synchronized (context) {
                            			NgnMsrpEventArgs eargs = new NgnMsrpEventArgs(getSessionId(),NgnMsrpEventTypes.DATA);
                            			final Intent intent = new Intent(NgnMsrpEventArgs.ACTION_MSRP_EVENT);
        								intent.putExtra(NgnMsrpEventArgs.EXTRA_EMBEDDED,eargs);
        								intent.putExtra(NgnMsrpEventArgs.EXTRA_CONTENT_TYPE, mContentType);
        								intent.putExtra(NgnMsrpEventArgs.EXTRA_WRAPPED_CONTENT_TYPE, mWContentType);
        								intent.putExtra(NgnMsrpEventArgs.EXTRA_DATA, mChatStream.toByteArray());
        								context.sendBroadcast(intent);
                            		}
                            	}
                            	mChatStream.reset();
                            }
                            else if(mSession.getMediaType() == NgnMediaType.FileTransfer){
                                if(mSession.mOutFileStream != null){
                                    synchronized (mSession.mOutFileStream){
                                    	try {
											mSession.mOutFileStream.close();
										} catch (IOException ioe) {
											ioe.printStackTrace();
										}
                                    	mSession.mOutFileStream = null;
                                    }
                                }
                            }
                        }

                        break;
                    }

                case tmsrp_REPORT:
                    {
                        break;
                    }

                case tmsrp_NONE:
                case tmsrp_AUTH:
                default:
                    break;
            }
        }

		private void broadcastEvent(NgnMsrpEventTypes type){
			if(mSession.mContext != null){
        		synchronized (mSession.mContext) {
        			NgnMsrpEventArgs eargs = new NgnMsrpEventArgs(getSessionId(),type);
        			final Intent intent = new Intent(NgnMsrpEventArgs.ACTION_MSRP_EVENT);
					intent.putExtra(NgnMsrpEventArgs.EXTRA_EMBEDDED,eargs);
					mSession.mContext.sendBroadcast(intent);
        		}
        	}
		}
		
		@Override
		public int OnEvent(MsrpEvent e){
            tmsrp_event_type_t type = e.getType();
            SipSession session = e.getSipSession();

            if (session == null || session.getId() != getSessionId()){
                Log.e(TAG,"Invalid session");
                return -1;
            }

            switch (type){
                case tmsrp_event_type_connected:
                    {
                    	broadcastEvent(NgnMsrpEventTypes.CONNECTED);
                        if(mSession.mPendingMessages != null && mSession.mPendingMessages.size() > 0) {
                            if(mSession.isConnected()){
                                for (PendingMessage pendingMsg : mSession.mPendingMessages){
                                    Log.d(TAG,"Sending pending message...");
                                    mSession.sendMessage(pendingMsg.getMessage(), pendingMsg.getContentType(), pendingMsg.getWContentType());
                                }
                                mSession.mPendingMessages.clear();
                            }
                            else{
                                Log.w(TAG,"There are pending messages but we are not connected");
                            }
                        }
                        break;
                    }

                case tmsrp_event_type_disconnected:
                    {
                        if(mSession.mOutFileStream != null){
                            synchronized(mSession.mOutFileStream){
                            	try {
									mSession.mOutFileStream.close();
								} catch (IOException ioe) {
									ioe.printStackTrace();
								}
                            	mSession.mOutFileStream = null;
                            }
                        }
                        broadcastEvent(NgnMsrpEventTypes.DISCONNECTED);
                        break;
                    }

                case tmsrp_event_type_message:
                    {
                        MsrpMessage message = e.getMessage();
                        if(message == null){
                            Log.e(TAG,"Invalid MSRP content");
                            return -1;
                        }

                        if(message.isRequest()){
                            processRequest(message);
                        }
                        else{
                            processResponse(message);
                        }
                        break;
                    }

                default:
                    break;
            }
            mSession.setChangedAndNotifyObservers(null);
            return 0;
        }
    }
}
