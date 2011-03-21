package org.doubango.ngn.services;

public interface INgnSoundService extends INgnBaseService {
	void startDTMF(int number);
	void stopDTMF();
	
	void startRingTone();
	void stopRingTone();
	
	void startRingBackTone();
	void stopRingBackTone();
	
	void startNewEvent();
	void stopNewEvent();
	
	void startConnectionChanged(boolean connected);
	void stopConnectionChanged(boolean connected);
}
