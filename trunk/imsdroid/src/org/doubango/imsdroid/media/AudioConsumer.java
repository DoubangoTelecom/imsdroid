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

import org.doubango.tinyWRAP.ProxyAudioConsumer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioConsumer{
	
	private static String TAG = AudioConsumer.class.getCanonicalName();
	private static int factor = 5;
	private static int streamType = AudioManager.STREAM_VOICE_CALL; /* STREAM_MUSIC */
	
	private int bufferSize;
	private int shorts_per_notif;
	private final MyProxyAudioConsumer proxyAudioConsumer;
	//private final Semaphore semaphore;
	
	private boolean running;
	private AudioTrack track;
	private ByteBuffer chunck;
	
	public AudioConsumer(){
		this.proxyAudioConsumer = new MyProxyAudioConsumer(this);
		//this.semaphore = new Semaphore(0);
	}	

	public void setActive(){
		this.proxyAudioConsumer.setActivate(true);
	}
	
	private synchronized int prepare(int ptime, int rate) {
		Log.d(AudioConsumer.TAG, "prepare()");
		
		int minBufferSize = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
		this.shorts_per_notif = (rate * ptime)/1000;
		this.bufferSize = minBufferSize + (this.shorts_per_notif - (minBufferSize % this.shorts_per_notif));
		this.chunck = ByteBuffer.allocateDirect(this.shorts_per_notif*2);
		
		this.track = new AudioTrack(AudioConsumer.streamType,
				rate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
				(this.bufferSize * AudioConsumer.factor), AudioTrack.MODE_STREAM);
		if(this.track.getState() == AudioTrack.STATE_INITIALIZED){
			//this.track.setPositionNotificationPeriod(this.shorts_per_notif);
			//this.track.setPlaybackPositionUpdateListener(this.playbackPositionUpdateListener);
			return 0;
		}
		else{
			Log.e(AudioConsumer.TAG, "prepare() failed");
			return -1;
		}
	}

	private synchronized int start() {
		Log.d(AudioConsumer.TAG, "start()");
		if(this.track != null){
			this.running = true;
			new Thread(this.runnablePlayer).start();
			return 0;
		}
		return -1;
	}
	
	private synchronized int pause() {
		Log.d(AudioConsumer.TAG, "pause()");
		if(this.track != null){
			this.track.pause();
			return 0;
		}
		return -1;
	}

	private synchronized int stop() {
		Log.d(AudioConsumer.TAG, "stop()");
		if(this.track != null){
			this.running = false;
			//this.semaphore.release();
			
			return 0;
		}
		return -1;
	}
	
//	private OnPlaybackPositionUpdateListener playbackPositionUpdateListener = new OnPlaybackPositionUpdateListener()
//	{
//		@Override
//		public void onPeriodicNotification(AudioTrack track) {
//			if(AudioConsumer.this.track != null){
//				AudioConsumer.this.semaphore.release();
//			}
//		}
//
//		@Override
//		public void onMarkerReached(AudioTrack track) {
//		}
//	};
	
	private Runnable runnablePlayer = new Runnable(){
		@Override
		public void run() {
			Log.d(AudioConsumer.TAG, "Audio Player ===== START");
			
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO );
			
			AudioConsumer.this.track.play();
			/* Mandatory in order to have first notifications 
			 * FIXME: Why when we write bufferSize bytes (like we do in the recorder) the callback function is not called ?*/
			//AudioConsumer.this.track.write(new byte[AudioConsumer.this.bufferSize*AudioConsumer.factor], 0, AudioConsumer.this.bufferSize*AudioConsumer.factor);
			
			while(true){
				//try {
				//	AudioConsumer.this.semaphore.acquire();
				//} catch (InterruptedException e) {
				//	e.printStackTrace();
				//	break;
				//}
				
				if(!AudioConsumer.this.running || AudioConsumer.this.proxyAudioConsumer == null || AudioConsumer.this.track == null){
					break;
				}
				
				final long sizeInBytes = AudioConsumer.this.proxyAudioConsumer.pull(AudioConsumer.this.chunck, AudioConsumer.this.chunck.capacity());			
				final byte[] bytes = new byte[AudioConsumer.this.chunck.capacity()];		
				if(sizeInBytes >0){ /* Otherwise it's silence */
					AudioConsumer.this.chunck.get(bytes);
				}
				AudioConsumer.this.track.write(bytes, 0, bytes.length);
				AudioConsumer.this.chunck.rewind();
			}
			
			if(AudioConsumer.this.track != null){
				AudioConsumer.this.track.stop();
				AudioConsumer.this.track.release();
				AudioConsumer.this.track = null;
				
				System.gc();
			}
			Log.d(AudioConsumer.TAG, "Audio Player ===== STOP");
		}
	};
	
	class MyProxyAudioConsumer extends ProxyAudioConsumer
    {
		private final AudioConsumer consumer;
		
		private MyProxyAudioConsumer(AudioConsumer consumer){
			super();
			this.consumer = consumer;
		}
		
		@Override
		public int pause() {
			return this.consumer.pause();
		}

		@Override
		public int prepare(int ptime, int rate, int channels) {
			return this.consumer.prepare(ptime, rate);			
		}

		@Override
		public int start() {
			return this.consumer.start();
		}

		@Override
		public int stop() {
			return this.consumer.stop();
		}
    }
}
