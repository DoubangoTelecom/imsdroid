package org.doubango.ngn.media;

public enum NgnMediaType {
	Audio,
	Video,
	AudioVideo,
	SMS,
	Chat,
	FileTransfer;
	
	public static boolean isAudioVideoType(NgnMediaType type){
		return type == Audio || type == AudioVideo || type == Video;
	}
	public static boolean isFileTransfer(NgnMediaType type){
		return type == FileTransfer;
	}
	public static boolean isChat(NgnMediaType type){
		return type == Chat;
	}
	public static boolean isMsrpType(NgnMediaType type){
		return isFileTransfer(type) || isChat(type);
	}
}