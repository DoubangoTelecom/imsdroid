
package org.doubango.imsdroid.Services;

public interface ISoundService extends IBaseService {
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
