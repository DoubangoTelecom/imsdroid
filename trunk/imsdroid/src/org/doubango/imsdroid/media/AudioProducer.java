/*
* Copyright (C) 2010 Mamadou Diop.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango.org>
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
*
*/
package org.doubango.imsdroid.media;

import java.nio.ByteBuffer;

import org.doubango.tinyWRAP.ProxyAudioProducer;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.util.Log;

public class AudioProducer {

	private static String TAG = AudioProducer.class.getCanonicalName();
	private static int factor = 5;
	
	private int bufferSize;
	private int shorts_per_notif;
	private final MyProxyAudioProducer proxyAudioProducer;
	
	private AudioRecord recorder;
	private ByteBuffer chunck;
	
	public AudioProducer(){
		this.proxyAudioProducer = new MyProxyAudioProducer(this);
	}
	
	public void setActive(){
		this.proxyAudioProducer.setActivate(true);
	}
	
	private synchronized int prepare(int ptime, int rate) {
		int minBufferSize = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
		this.shorts_per_notif = (rate * ptime)/1000;
		this.bufferSize = minBufferSize + (this.shorts_per_notif - (minBufferSize % this.shorts_per_notif));
		this.chunck = ByteBuffer.allocateDirect(this.shorts_per_notif*2);
		
		this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				rate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, (this.bufferSize * AudioProducer.factor));
		if(this.recorder.getState() == AudioRecord.STATE_INITIALIZED){
			this.recorder.setPositionNotificationPeriod(this.shorts_per_notif);
			this.recorder.setRecordPositionUpdateListener(this.recordPositionUpdateListener);
			return 0;
		}
		else{
			Log.e(AudioProducer.TAG, "prepare() failed");
			return -1;
		}
	}
	
	private synchronized int start() {
		Log.d(AudioProducer.TAG, "start()");
		if(this.recorder != null){
			this.recorder.startRecording();
			/* Mandatory in order to have first notifications */
			this.recorder.read(new byte[this.bufferSize], 0, this.bufferSize);
			return 0;
		}
		return -1;
	}
	
	private synchronized int pause() {
		Log.d(AudioProducer.TAG, "pause()");
		return 0;
	}

	private synchronized int stop() {
		Log.d(AudioProducer.TAG, "stop()");
		if(this.recorder != null){
			this.recorder.stop();
			this.recorder.release();
			this.recorder = null;
			
			System.gc();
			return 0;
		}
		return -1;
	}
	
	private OnRecordPositionUpdateListener recordPositionUpdateListener = new OnRecordPositionUpdateListener()
	{
		@Override
		public void onPeriodicNotification(AudioRecord recorder) {
			if(AudioProducer.this.recorder != null){
				try{
					int read = AudioProducer.this.recorder.read(AudioProducer.this.chunck, shorts_per_notif*2);
					if(read > 0){
						AudioProducer.this.proxyAudioProducer.push(AudioProducer.this.chunck, read);
					}
					AudioProducer.this.chunck.rewind();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void onMarkerReached(AudioRecord recorder) {
		}
	};
	
	class MyProxyAudioProducer extends ProxyAudioProducer
	{
		private final AudioProducer producer;
		
		private MyProxyAudioProducer(AudioProducer producer){
			super();
			this.producer = producer;
		}
		
		@Override
		public int pause() {
			return this.producer.pause();
		}

		@Override
		public int prepare(int ptime, int rate, int channels) {
			return this.producer.prepare(ptime, rate);
		}

		@Override
		public int start() {
			return this.producer.start();
		}

		@Override
		public int stop() {
			return this.producer.stop();
		}
	}
}
