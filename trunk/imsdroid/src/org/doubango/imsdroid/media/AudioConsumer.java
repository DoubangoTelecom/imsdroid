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

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.tinyWRAP.ProxyAudioConsumer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioConsumer{
	
	private static final String TAG = AudioConsumer.class.getCanonicalName();
	private static final int factor = 3;
	private static final int streamType = AudioManager.STREAM_VOICE_CALL;
	
	private int bufferSize;
	private int shorts_per_notif;
	private final MyProxyAudioConsumer proxyAudioConsumer;
	
	private boolean running;
	private AudioTrack track;
	private ByteBuffer chunck;
	
	public AudioConsumer(){
		this.proxyAudioConsumer = new MyProxyAudioConsumer(this);
	}	

	public void setActive(){
		this.proxyAudioConsumer.setActivate(true);
	}
	
	private synchronized int prepare(int ptime, int rate) {
		Log.d(AudioConsumer.TAG, "prepare()");
		
		int minBufferSize = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
		this.shorts_per_notif = (rate * ptime)/1000;
		this.bufferSize = ((minBufferSize + (this.shorts_per_notif - (minBufferSize % this.shorts_per_notif))) * AudioConsumer.factor);
		this.chunck = ByteBuffer.allocateDirect(this.shorts_per_notif*2);
		
		this.track = new AudioTrack(AudioConsumer.streamType,
				rate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
				this.bufferSize, AudioTrack.MODE_STREAM);
		if(this.track.getState() == AudioTrack.STATE_INITIALIZED){
			float audioPlaybackLevel = ServiceManager.getConfigurationService().getFloat(
					CONFIGURATION_SECTION.GENERAL,
					CONFIGURATION_ENTRY.AUDIO_PLAY_LEVEL,
					Configuration.DEFAULT_GENERAL_AUDIO_PLAY_LEVEL);
			this.track.setStereoVolume(AudioTrack.getMaxVolume()*audioPlaybackLevel, AudioTrack.getMaxVolume()*0.25f);
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
			
			return 0;
		}
		return -1;
	}
	
	private Runnable runnablePlayer = new Runnable(){
		@Override
		public void run() {
			Log.d(AudioConsumer.TAG, "===== Audio Player ===== START");
			
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			
			boolean playing = false;
			int writtenBytes = 0;
			final byte[] audioBytes = new byte[AudioConsumer.this.chunck.capacity()];
			final byte[] silenceBytes = new byte[audioBytes.length];
			
			while(AudioConsumer.this.running){
				if(AudioConsumer.this.track == null){
					break;
				}
				
				/* get sound data from the jitter buffer */
				final long sizeInBytes = AudioConsumer.this.proxyAudioConsumer.pull(AudioConsumer.this.chunck, audioBytes.length);				
				if(sizeInBytes >0){ 
					AudioConsumer.this.chunck.get(audioBytes);
					/* writtenBytes +=*/ AudioConsumer.this.track.write(audioBytes, 0, audioBytes.length);
					writtenBytes += audioBytes.length;
				}
				else{ // silence
					AudioConsumer.this.track.write(silenceBytes, 0, silenceBytes.length);
					writtenBytes += silenceBytes.length;
				}
				
				if(!playing && writtenBytes>= AudioConsumer.this.bufferSize){
					Log.d(AudioConsumer.TAG, "===== Audio Player ===== PLAY");
					playing = true;
					AudioConsumer.this.track.play();
				}
				
				AudioConsumer.this.chunck.rewind();
			}
			
			if(AudioConsumer.this.track != null){
				AudioConsumer.this.track.stop();
				AudioConsumer.this.track.release();
				AudioConsumer.this.track = null;
				
				System.gc();
			}
			Log.d(AudioConsumer.TAG, "===== Audio Player ===== STOP");
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
