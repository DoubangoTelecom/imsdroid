package org.doubango.ngn.model;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.utils.NgnPredicate;
import org.simpleframework.xml.Root;

@Root
public class NgnHistoryAVCallEvent extends NgnHistoryEvent{

	NgnHistoryAVCallEvent(){
		super(NgnMediaType.AudioVideo, null);
	}
	
	public NgnHistoryAVCallEvent(boolean video, String remoteParty) {
		super(video? NgnMediaType.AudioVideo : NgnMediaType.Audio, remoteParty);
	}
	
	/**
	 * HistoryEventAVFilter
	 */
	public static class HistoryEventAVFilter implements NgnPredicate<NgnHistoryEvent>{
		@Override
		public boolean apply(NgnHistoryEvent event) {
			return (event != null && (event.getMediaType() == NgnMediaType.Audio || event.getMediaType() == NgnMediaType.AudioVideo));
		}
	}
}
