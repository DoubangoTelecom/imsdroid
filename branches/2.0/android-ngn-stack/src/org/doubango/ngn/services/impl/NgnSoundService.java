package org.doubango.ngn.services.impl;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.services.INgnSoundService;

import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.util.Log;

public class NgnSoundService extends NgnBaseService implements INgnSoundService{
	private final static String TAG = NgnSoundService.class.getCanonicalName();
	
	/** The DTMF tone volume relative to other sounds in the stream */
	private static final int TONE_RELATIVE_VOLUME = 50;
	
	private ToneGenerator mRingbackPlayer;
	private Ringtone mRingtonePlayer;
	
	@Override
	public boolean start() {
		Log.d(TAG, "starting...");
		return true;
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "stopping...");
		
		if(mRingbackPlayer != null){
			synchronized(mRingbackPlayer){
				mRingbackPlayer.release();
				mRingbackPlayer = null;
			}
		}
		
		if(mRingtonePlayer != null){
			synchronized(mRingtonePlayer){
				if(mRingtonePlayer.isPlaying()){
					mRingtonePlayer.stop();
				}
				mRingtonePlayer = null;
			}
		}
		return true;
	}

	@Override
	public void startDTMF(int number) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopDTMF() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startRingTone() {
		if(mRingtonePlayer == null){
			try{
				mRingtonePlayer = RingtoneManager.getRingtone(NgnApplication.getContext(), android.provider.Settings.System.DEFAULT_RINGTONE_URI);
			}catch(Exception e){
				e.printStackTrace();
				return;
			}
		}
		
		if(mRingtonePlayer != null){
			synchronized(mRingtonePlayer){
				mRingtonePlayer.play();
			}
		}
	}

	@Override
	public void stopRingTone() {
		if(mRingtonePlayer != null){
			synchronized(mRingtonePlayer){
				mRingtonePlayer.stop();
			}
		}
	}

	@Override
	public void startRingBackTone() {
		if (mRingbackPlayer == null) {
			try {
				mRingbackPlayer = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, TONE_RELATIVE_VOLUME);
			} catch (RuntimeException e) {
				Log.w(TAG, "Exception caught while creating local tone generator: " + e);
				mRingbackPlayer = null;
			}
		}

		if(mRingbackPlayer != null){
			synchronized(mRingbackPlayer){
				mRingbackPlayer.startTone(ToneGenerator.TONE_SUP_RINGTONE);
			}
		}
	}

	@Override
	public void stopRingBackTone() {
		if(mRingbackPlayer != null){
			synchronized(mRingbackPlayer){
				mRingbackPlayer.stopTone();
			}
		}
	}

	@Override
	public void startNewEvent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopNewEvent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startConnectionChanged(boolean connected) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopConnectionChanged(boolean connected) {
		// TODO Auto-generated method stub
		
	}

}
