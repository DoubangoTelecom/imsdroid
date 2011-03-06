package org.doubango.imsdroid.Media;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.tinyWRAP.ProxyAudioProducer;
import org.doubango.tinyWRAP.ProxyAudioProducerCallback;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


/**
 * MyProxyAudioProducer
 */
public class MyProxyAudioProducer extends MyProxyPlugin{
	private static final String TAG = MyProxyAudioProducer.class.getCanonicalName();
	private final static int AUDIO_BUFFER_FACTOR = 10;
	
	private final MyProxyAudioProducerCallback mCallback;
	private final ProxyAudioProducer mProducer;
	private boolean mRoutingChanged;
	
	private AudioRecord mAudioRecorder;
	private ByteBuffer mAudioFrame;
	
	private int mPtime, mRate, mChannels;
	
	public MyProxyAudioProducer(BigInteger id, ProxyAudioProducer producer){
		super(id, producer);
		mProducer = producer;
        mCallback = new MyProxyAudioProducerCallback(this);
        mProducer.setCallback(this.mCallback);
	}
	
	public void setOnPause(boolean pause){
		if(super.mPaused == pause){
			return;
		}
		try {
			if(this.mStarted){
				
			}
		} catch(Exception e){
			Log.e(TAG, e.toString());
		}
		
		super.mPaused = pause;
	}
	
	public void setSpeakerphoneOn(boolean speakerOn){
		Log.d(TAG, "setSpeakerphoneOn("+speakerOn+")");
		if (IMSDroid.getSDKVersion() >= 5){
			mRoutingChanged = true;
		}
	}
	
	public void toggleSpeakerphone(){
		setSpeakerphoneOn(!IMSDroid.getAudioManager().isSpeakerphoneOn());
	}
	
	private int prepareCallback(int ptime, int rate, int channels){
    	Log.d(MyProxyAudioProducer.TAG, "prepareCallback("+ptime+","+rate+","+channels+")");
    	return prepare(ptime, rate, channels);
    }

	private int startCallback(){
    	Log.d(MyProxyAudioProducer.TAG, "startCallback");
    	if(mPrepared && this.mAudioRecorder != null){
			super.mStarted = true;
			new Thread(mRunnableRecorder).start();
			return 0;
		}
        return -1;
    }

	private int pauseCallback(){
    	Log.d(MyProxyAudioProducer.TAG, "pauseCallback");
    	setOnPause(true);
        return 0;
    }

	private int stopCallback(){
    	Log.d(MyProxyAudioProducer.TAG, "stopCallback");
    	super.mStarted = false;
		if(mAudioRecorder != null){
			return 0;
		}
		return -1;
    }
	
	private int prepare(int ptime, int rate, int channels){
		final int minBufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		final int shortsPerNotif = (rate * ptime)/1000;
		final int bufferSize = (minBufferSize + (shortsPerNotif - (minBufferSize % shortsPerNotif))) * MyProxyAudioProducer.AUDIO_BUFFER_FACTOR;
		mAudioFrame = ByteBuffer.allocateDirect(shortsPerNotif*2);
		mPtime = ptime; mRate = rate; mChannels = channels;
		
		mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				rate, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);
		if(mAudioRecorder.getState() == AudioRecord.STATE_INITIALIZED){
			super.mPrepared = true;
			return 0;
		}
		else{
			Log.e(TAG, "prepare() failed");
			super.mPrepared = false;
			return -1;
		}
	}
	
	private void unprepare(){
		if(mAudioRecorder != null){
			mAudioRecorder.stop();
			mAudioRecorder.release();
			mAudioRecorder = null;
		}
		super.mPrepared = false;
	}
	
	private Runnable mRunnableRecorder = new Runnable(){
		@Override
		public void run() {
			Log.d(TAG, "===== Audio Recorder (Start) ===== ");
			
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			
			mAudioRecorder.startRecording();
			int size = mAudioFrame.capacity();
			int read;
			
			while(MyProxyAudioProducer.super.mValid && mStarted){
				if(mAudioRecorder == null){
					break;
				}
				
				if(mRoutingChanged){
					Log.d(TAG, "Routing changed: restart() recorder");
					mRoutingChanged = false;
					unprepare();
					if(prepare(mPtime, mRate, mChannels) != 0){
						break;
					}
					if(!MyProxyAudioProducer.super.mPaused){
						mAudioRecorder.startRecording();
					}
				}
				
				// To avoid overrun read data even if on pause
				if((read = mAudioRecorder.read(mAudioFrame, size)) > 0){
					if(!MyProxyAudioProducer.super.mPaused){
						mProducer.push(mAudioFrame, read);
					}
				}				
			}
			
			unprepare();
			
			Log.d(TAG, "===== Audio Recorder (Stop) ===== ");
		}
	};
	
	
	/**
	 * MyProxyAudioProducerCallback
	 */
	static class MyProxyAudioProducerCallback extends ProxyAudioProducerCallback
    {
        final MyProxyAudioProducer myProducer;

        public MyProxyAudioProducerCallback(MyProxyAudioProducer producer){
        	super();
            myProducer = producer;
        }

        @Override
        public int prepare(int ptime, int rate, int channels){
            return myProducer.prepareCallback(ptime, rate, channels);
        }

        @Override
        public int start(){
            return myProducer.startCallback();
        }

        @Override
        public int pause(){
            return myProducer.pauseCallback();
        }

        @Override
        public int stop(){
            return myProducer.stopCallback();
        }
    }
}

