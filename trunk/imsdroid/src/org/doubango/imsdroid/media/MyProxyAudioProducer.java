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

import org.doubango.tinyWRAP.ProxyAudioProducer;
import org.doubango.tinyWRAP.ProxyAudioProducerCallback;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


/**
 * MyProxyAudioProducer
 * @author Mamadou Diop
 *
 */
public class MyProxyAudioProducer extends MyProxyPlugin{
	private static final String TAG = MyProxyAudioProducer.class.getCanonicalName();
	private final static int AUDIO_BUFFER_FACTOR = 10;
	
	private final MyProxyAudioProducerCallback callback;
	private final ProxyAudioProducer producer;
	
	private AudioRecord audioRecorder;
	private ByteBuffer audioFrame;
	private boolean prepared;
	
	public MyProxyAudioProducer(BigInteger id, ProxyAudioProducer producer){
		super(id, producer);
		this.producer = producer;
        this.callback = new MyProxyAudioProducerCallback(this);
        this.producer.setCallback(this.callback);
	}
	
	public void setOnPause(boolean pause){
		if(this.paused == pause){
			return;
		}
		try {
			if(this.started){
				
			}
		} catch(Exception e){
			Log.e(MyProxyAudioProducer.TAG, e.toString());
		}
		
		this.paused = pause;
	}
	
	private int prepareCallback(int ptime, int rate, int channels){
    	Log.d(MyProxyAudioProducer.TAG, "prepareCallback("+ptime+","+rate+","+channels+")");
    	
		int minBufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
		int shortsPerNotif = (rate * ptime)/1000;
		int bufferSize = (minBufferSize + (shortsPerNotif - (minBufferSize % shortsPerNotif))) * MyProxyAudioProducer.AUDIO_BUFFER_FACTOR;
		this.audioFrame = ByteBuffer.allocateDirect(shortsPerNotif*2);
		
		this.audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				rate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);
		if(this.audioRecorder.getState() == AudioRecord.STATE_INITIALIZED){
			this.prepared = true;
			return 0;
		}
		else{
			Log.e(MyProxyAudioProducer.TAG, "prepare() failed");
			this.prepared = false;
			return -1;
		}
    }

	private int startCallback(){
    	Log.d(MyProxyAudioProducer.TAG, "startCallback");
    	if(this.prepared && this.audioRecorder != null){
			this.started = true;
			new Thread(this.runnableRecorder).start();
			return 0;
		}
        return -1;
    }

	private int pauseCallback(){
    	Log.d(MyProxyAudioProducer.TAG, "pauseCallback");
    	this.setOnPause(true);
        return 0;
    }

	private int stopCallback(){
    	Log.d(MyProxyAudioProducer.TAG, "stopCallback");
    	this.started = false;
		if(this.audioRecorder != null){
			return 0;
		}
		return -1;
    }
	
	private Runnable runnableRecorder = new Runnable(){
		@Override
		public void run() {
			Log.d(MyProxyAudioProducer.TAG, "===== Audio Recorder (Start) ===== ");
			
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			
			MyProxyAudioProducer.this.audioRecorder.startRecording();
			int size = MyProxyAudioProducer.this.audioFrame.capacity();
			int read;
			
			while(MyProxyAudioProducer.this.valid && MyProxyAudioProducer.this.started){
				if(MyProxyAudioProducer.this.audioRecorder == null){
					break;
				}
				
				// To avoid overrun read data event if on pause
				 if((read = MyProxyAudioProducer.this.audioRecorder.read(MyProxyAudioProducer.this.audioFrame, size)) > 0){
					 if(!MyProxyAudioProducer.this.paused){
						 MyProxyAudioProducer.this.producer.push(MyProxyAudioProducer.this.audioFrame, read);
					 }
				}
			}
			
			if(MyProxyAudioProducer.this.audioRecorder != null){
				MyProxyAudioProducer.this.audioRecorder.stop();
				MyProxyAudioProducer.this.audioRecorder.release();
				MyProxyAudioProducer.this.audioRecorder = null;
			}
			
			Log.d(MyProxyAudioProducer.TAG, "===== Audio Recorder (Stop) ===== ");
		}
	};
	
	/**
	 * MyProxyAudioProducerCallback
	 * @author Mamadou Diop
	 *
	 */
	static class MyProxyAudioProducerCallback extends ProxyAudioProducerCallback
    {
        final MyProxyAudioProducer myProducer;

        public MyProxyAudioProducerCallback(MyProxyAudioProducer producer){
        	super();
            this.myProducer = producer;
        }

        @Override
        public int prepare(int ptime, int rate, int channels){
            return this.myProducer.prepareCallback(ptime, rate, channels);
        }

        @Override
        public int start(){
            return this.myProducer.startCallback();
        }

        @Override
        public int pause(){
            return this.myProducer.pauseCallback();
        }

        @Override
        public int stop(){
            return this.myProducer.stopCallback();
        }
    }
}
