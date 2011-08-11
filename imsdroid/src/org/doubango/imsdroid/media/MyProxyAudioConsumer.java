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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.tinyWRAP.ProxyAudioConsumer;
import org.doubango.tinyWRAP.ProxyAudioConsumerCallback;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * MyProxyAudioConsumer
 * @author root
 *
 */
public class MyProxyAudioConsumer extends MyProxyPlugin{
	private static final String TAG = MyProxyAudioConsumer.class.getCanonicalName();
	private static final int AUDIO_BUFFER_FACTOR = 2;
	private static final int AUDIO_STREAM_TYPE = AudioManager.STREAM_VOICE_CALL;
	
	private final MyProxyAudioConsumerCallback callback;
	private final ProxyAudioConsumer consumer;
	
	private int bufferSize;
	private AudioTrack audioTrack;
	private ByteBuffer audioFrame;
	private boolean prepared;
	
	public MyProxyAudioConsumer(BigInteger id, ProxyAudioConsumer consumer){
		super(id, consumer);
        this.callback = new MyProxyAudioConsumerCallback(this);
        this.consumer = consumer;
        this.consumer.setCallback(this.callback);
	}
	
	private int prepareCallback(int ptime, int rate, int channels) {
		Log.d(MyProxyAudioConsumer.TAG, "prepareCallback("+ptime+","+rate+","+channels+")");
		
		int minBufferSize = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
		int shortsPerNotif = (rate * ptime)/1000;
		this.bufferSize = ((minBufferSize + (shortsPerNotif - (minBufferSize % shortsPerNotif))) * MyProxyAudioConsumer.AUDIO_BUFFER_FACTOR);
		this.audioFrame = ByteBuffer.allocateDirect(shortsPerNotif*2);
		
		this.audioTrack = new AudioTrack(MyProxyAudioConsumer.AUDIO_STREAM_TYPE,
				rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
				bufferSize, AudioTrack.MODE_STREAM);
		if(this.audioTrack.getState() == AudioTrack.STATE_INITIALIZED){
			float audioPlaybackLevel = ServiceManager.getConfigurationService().getFloat(
					CONFIGURATION_SECTION.GENERAL,
					CONFIGURATION_ENTRY.AUDIO_PLAY_LEVEL,
					Configuration.DEFAULT_GENERAL_AUDIO_PLAY_LEVEL);
			//this.audioTrack.setStereoVolume(AudioTrack.getMaxVolume()*audioPlaybackLevel, AudioTrack.getMaxVolume()*0.25f);
			this.audioTrack.setStereoVolume(audioPlaybackLevel, audioPlaybackLevel);
			this.prepared = true;
			return 0;
		}
		else{
			Log.e(MyProxyAudioConsumer.TAG, "prepare() failed");
			this.prepared = false;
			return -1;
		}
	}
	
	private int startCallback() {
		Log.d(MyProxyAudioConsumer.TAG, "startCallback");
		if(this.prepared && this.audioTrack != null){
			this.started = true;
			new Thread(this.runnablePlayer).start();
			return 0;
		}
		return -1;
	}
	
	private int pauseCallback() {
		Log.d(MyProxyAudioConsumer.TAG, "pauseCallback");
		if(this.audioTrack != null){
			this.audioTrack.pause();
			this.paused = true;
			return 0;
		}
		return -1;
	}		

	private int stopCallback() {
		Log.d(MyProxyAudioConsumer.TAG, "stopCallback");
		this.started = false;
		if(this.audioTrack != null){
			return 0;
		}
		return -1;
	}
	
	private Runnable runnablePlayer = new Runnable(){
		@Override
		public void run() {
			Log.d(MyProxyAudioConsumer.TAG, "===== Audio Player Thread (Start) =====");
			
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			
			int frameLength = MyProxyAudioConsumer.this.audioFrame.capacity();
			final int framesCount = 1; // Number of 20ms' to copy
			final byte[] audioBytes = new byte[frameLength*framesCount];
			int i;
			long sizeInBytes;
			
			MyProxyAudioConsumer.this.audioTrack.play();
			
			while(MyProxyAudioConsumer.this.valid && MyProxyAudioConsumer.this.started){
				if(MyProxyAudioConsumer.this.audioTrack == null){
					break;
				}
				
				for(i=0; i<framesCount; i++){
					sizeInBytes = MyProxyAudioConsumer.this.consumer.pull(MyProxyAudioConsumer.this.audioFrame, frameLength);
					if(sizeInBytes>0){
						MyProxyAudioConsumer.this.audioFrame.get(audioBytes, i*frameLength, frameLength);
						MyProxyAudioConsumer.this.audioFrame.rewind();
						//if(sizeInBytes<frameLength){
						// TODO
						//}
					}
					else{
						Arrays.fill(audioBytes, i*frameLength, (i*frameLength)+frameLength, (byte)0);
					}
				}
				
				
				/*long avg=0;
				for(i=0;i<audioBytes.length;i+=1){
					avg+=Math.abs(audioBytes[i]);
				}
				if(Math.abs(avg)>=18000)
					Log.d(MyProxyAudioConsumer.TAG, "avg="+avg);
				else
					Arrays.fill(audioBytes, 0, audioBytes.length, (byte)0);
				*/
				MyProxyAudioConsumer.this.audioTrack.write(audioBytes, 0, audioBytes.length);
				
				/* get sound data from the jitter buffer */
//				final long sizeInBytes = MyProxyAudioConsumer.this.consumer.pull(MyProxyAudioConsumer.this.audioFrame, audioBytes.length);				
//				if(sizeInBytes >0){ 
//					MyProxyAudioConsumer.this.audioFrame.get(audioBytes);
//					/* writtenBytes +=*/ MyProxyAudioConsumer.this.audioTrack.write(audioBytes, 0, audioBytes.length);
//					MyProxyAudioConsumer.this.audioFrame.rewind();
//				}
//				else{ // silence
//					MyProxyAudioConsumer.this.audioTrack.write(silenceBytes, 0, silenceBytes.length);
//				}
			}
			
			if(MyProxyAudioConsumer.this.audioTrack != null){
				MyProxyAudioConsumer.this.audioTrack.stop();
				MyProxyAudioConsumer.this.audioTrack.release();
				MyProxyAudioConsumer.this.audioTrack = null;
			}
			Log.d(MyProxyAudioConsumer.TAG, "===== Audio Player Thread (Stop) =====");
		}
	};
	
	/*
	 * MyProxyAudioConsumerCallback
	 */
	static class MyProxyAudioConsumerCallback extends ProxyAudioConsumerCallback
	{
		final MyProxyAudioConsumer myConsumer;
		
		MyProxyAudioConsumerCallback(MyProxyAudioConsumer consumer){
			super();
			this.myConsumer = consumer;
		}

		@Override
		public int prepare(int ptime, int rate, int channels) {
			return this.myConsumer.prepareCallback(ptime, rate, channels);
		}
		
		@Override
		public int start() {
			return this.myConsumer.startCallback();
		}
		
		@Override
		public int pause() {
			return this.myConsumer.pauseCallback();
		}		

		@Override
		public int stop() {
			return this.myConsumer.stopCallback();
		}
	}
}
