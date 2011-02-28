package org.doubango.imsdroid.Model;

import org.doubango.imsdroid.Media.MediaType;
import org.simpleframework.xml.Root;

@Root
public class HistoryAVCallEvent extends HistoryEvent{

	HistoryAVCallEvent(){
		super(MediaType.AudioVideo, null);
	}
	
	public HistoryAVCallEvent(boolean video, String remoteParty) {
		super(video? MediaType.AudioVideo : MediaType.Audio, remoteParty);
	}
}
