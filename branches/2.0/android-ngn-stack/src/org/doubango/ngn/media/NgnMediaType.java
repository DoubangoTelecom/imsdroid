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
package org.doubango.ngn.media;

import org.doubango.tinyWRAP.twrap_media_type_t;

public enum NgnMediaType {
	None,
	Audio,
	Video,
	AudioVideo,
	SMS,
	Chat,
	FileTransfer,
	Msrp /* Chat | FileTransfer */,
	T140,
	AudioT140,
	AudioVideoT140,
	VideoT140;
	
	public static boolean isVideoType(NgnMediaType type){
		switch(type){
			case AudioVideo: case Video: case AudioVideoT140: case VideoT140: return true;
			default: return false;
		}
	}
	public static boolean isAudioType(NgnMediaType type){
		switch(type){
			case Audio: case AudioVideo: case AudioT140: case AudioVideoT140: return true;
			default: return false;
		}
	}
	public static boolean isAudioVideoType(NgnMediaType type){
		return isAudioType(type) || isVideoType(type);
	}
	public static boolean isT140Type(NgnMediaType type){
		switch(type){
			case T140: case AudioT140: case AudioVideoT140: case VideoT140: return true;
			default: return false;
		}
	}
	public static boolean isAudioVideoT140Type(NgnMediaType type){
		return isAudioVideoType(type) || isT140Type(type);
	}
	public static boolean isFileTransfer(NgnMediaType type){
		return type == FileTransfer;
	}
	public static boolean isChat(NgnMediaType type){
		return type == Chat;
	}
	public static boolean isMsrpType(NgnMediaType type){
		return type == Msrp || isFileTransfer(type) || isChat(type);
	}
	

    public static NgnMediaType ConvertFromNative(twrap_media_type_t mediaType){
        switch (mediaType){
            case twrap_media_audio:
                return Audio;
            case twrap_media_video:
                return Video;
            case twrap_media_audio_video:
            case twrap_media_audiovideo:
                return AudioVideo;
            case twrap_media_audio_t140:
                return AudioT140;
            case twrap_media_audio_video_t140:
                return AudioVideoT140;
            case twrap_media_t140:
                return T140;
            case twrap_media_video_t140:
                return VideoT140;
            case twrap_media_msrp:
                return Msrp;
            default:
                return None;
        }
    }

    public static twrap_media_type_t ConvertToNative(NgnMediaType mediaType){
        switch (mediaType){
            case Audio:
                return twrap_media_type_t.twrap_media_audio;
            case Video:
                return twrap_media_type_t.twrap_media_video;
            case AudioVideo:
                return twrap_media_type_t.twrap_media_audio_video;
            case AudioT140:
                return twrap_media_type_t.twrap_media_audio_t140;
            case VideoT140:
                return twrap_media_type_t.twrap_media_video_t140;
            case AudioVideoT140:
                return twrap_media_type_t.twrap_media_audio_video_t140;
            case T140:
                return twrap_media_type_t.twrap_media_t140;
            case Chat: 
            case FileTransfer:
                return twrap_media_type_t.twrap_media_msrp;
            default:
                return twrap_media_type_t.twrap_media_none;
        }
    }
}