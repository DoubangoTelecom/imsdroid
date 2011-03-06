package org.doubango.imsdroid.Model;

import org.doubango.imsdroid.Media.MediaType;
import org.doubango.imsdroid.Utils.Predicate;
import org.simpleframework.xml.Root;

@Root
public class HistoryAVCallEvent extends HistoryEvent{

	HistoryAVCallEvent(){
		super(MediaType.AudioVideo, null);
	}
	
	public HistoryAVCallEvent(boolean video, String remoteParty) {
		super(video? MediaType.AudioVideo : MediaType.Audio, remoteParty);
	}
	
	/**
	 * HistoryEventAVFilter
	 */
	public static class HistoryEventAVFilter implements Predicate<HistoryEvent>{
		@Override
		public boolean apply(HistoryEvent event) {
			return (event != null && (event.getMediaType() == MediaType.Audio || event.getMediaType() == MediaType.AudioVideo));
		}
	}
}
