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
	None(0x00000000),
	Audio(0x00000001 << 0),
    Video(0x00000001 << 1),
    Msrp(0x00000001 << 2),
    T140(0x00000001 << 3),
    BFCP(0x00000001 << 4),
    Audiobfcp((0x00000001 << 5) | BFCP.getValue()),
    Videobfcp((0x00000001 << 6) | BFCP.getValue()),

    // == Types without native mappings  == //
    SMS(0x00000001 << 16),
    ShortMessage(0x00000001 << 17),
    Chat(0x00000001 << 18),
    FileTransfer(0x00000001 << 19),
    Messaging(SMS.getValue() | Chat.getValue() | ShortMessage.getValue()),
    AudioT140(Audio.getValue() | T140.getValue()),
    AudioVideo(Audio.getValue() | Video.getValue()),

    All(~0)
	;
	
	private final int mValue;
	
	private static class media_type_bind_s {
		private NgnMediaType twrap;
		private twrap_media_type_t tnative;
		private media_type_bind_s(NgnMediaType _twrap, twrap_media_type_t _tnative) {
            this.twrap = _twrap;
            this.tnative = _tnative;
        }
        
		private static media_type_bind_s[] __media_type_binds =  {
            new media_type_bind_s(NgnMediaType.Msrp, twrap_media_type_t.twrap_media_msrp),
            new media_type_bind_s(NgnMediaType.Audio, twrap_media_type_t.twrap_media_audio),
            new media_type_bind_s(NgnMediaType.Video, twrap_media_type_t.twrap_media_video),
            new media_type_bind_s(NgnMediaType.T140, twrap_media_type_t.twrap_media_t140),
            new media_type_bind_s(NgnMediaType.BFCP, twrap_media_type_t.twrap_media_bfcp),
            new media_type_bind_s(NgnMediaType.Audiobfcp, twrap_media_type_t.twrap_media_bfcp_audio),
            new media_type_bind_s(NgnMediaType.Videobfcp, twrap_media_type_t.twrap_media_bfcp_video),
        };
		static int ConvertFromNative(twrap_media_type_t mediaType) {
			int t = NgnMediaType.None.getValue();
            for (int i = 0; i < __media_type_binds.length; ++i) {
                if ((__media_type_binds[i].tnative.swigValue() & mediaType.swigValue()) == __media_type_binds[i].tnative.swigValue()) {
                    t |= __media_type_binds[i].twrap.getValue();
                }
            }
            return t;
        }

		static int ConvertToNative(NgnMediaType mediaType) {
			int t = twrap_media_type_t.twrap_media_none.swigValue();
            for (int i = 0; i < __media_type_binds.length; ++i)  {
                if ((__media_type_binds[i].twrap.getValue() & mediaType.getValue()) == __media_type_binds[i].twrap.getValue()) {
                    t |= __media_type_binds[i].tnative.swigValue();
                }
            }
            return t;
        }
    };
    
	
	private NgnMediaType(final int value) {
		mValue = value;
	}
	
	public int getValue() {
		return mValue; 
	}
	
	public static boolean isVideoType(NgnMediaType type){
		return ((type.getValue() & NgnMediaType.Video.getValue()) == NgnMediaType.Video.getValue()) || (type == NgnMediaType.Videobfcp);
	}
	public static boolean isAudioType(NgnMediaType type){
		return ((type.getValue() & NgnMediaType.Audio.getValue()) == NgnMediaType.Audio.getValue()) || (type == NgnMediaType.Audiobfcp);
	}
	public static boolean isAudioVideoType(NgnMediaType type){
		return isAudioType(type) || isVideoType(type);
	}
	public static boolean isT140Type(NgnMediaType type){
		return ((type.getValue() & NgnMediaType.T140.getValue()) == NgnMediaType.T140.getValue());
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
        int t = media_type_bind_s.ConvertFromNative(mediaType);
        for (NgnMediaType _t : NgnMediaType.values()) {
    	   if(_t.getValue() == t) {
    		   return _t;
    	   }
    	}
        return NgnMediaType.None;
    }

    public static twrap_media_type_t ConvertToNative(NgnMediaType mediaType){
    	int t = media_type_bind_s.ConvertToNative(mediaType);
        for (twrap_media_type_t _t : twrap_media_type_t.values()) {
    	   if(_t.swigValue() == t) {
    		   return _t;
    	   }
    	}
        return twrap_media_type_t.twrap_media_none;
    }
}