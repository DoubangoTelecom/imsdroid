package org.doubango.imsdroid.Media;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.tinyWRAP.ProxyAudioConsumer;
import org.doubango.tinyWRAP.ProxyAudioConsumerCallback;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * MyProxyAudioConsumer
 */
public class MyProxyAudioConsumer extends MyProxyPlugin{
	private static final String TAG = MyProxyAudioConsumer.class.getCanonicalName();
	private static final int AUDIO_BUFFER_FACTOR = 2;
	private static final int AUDIO_STREAM_TYPE = AudioManager.STREAM_VOICE_CALL;
	
	private final MyProxyAudioConsumerCallback mCallback;
	private final ProxyAudioConsumer mConsumer;
	
	private int mBufferSize;
	private AudioTrack mAudioTrack;
	private ByteBuffer mAudioFrame;
	
	public MyProxyAudioConsumer(BigInteger id, ProxyAudioConsumer consumer){
		super(id, consumer);
        mCallback = new MyProxyAudioConsumerCallback(this);
        mConsumer = consumer;
        mConsumer.setCallback(mCallback);
	}
	
	public void setSpeakerphoneOn(boolean speakerOn){
		Log.d(TAG, "setSpeakerphoneOn("+speakerOn+")");
		final AudioManager audiomanager = IMSDroid.getAudioManager();
		if (IMSDroid.getSDKVersion() < 5){
			audiomanager.setRouting(AudioManager.MODE_IN_CALL, 
					speakerOn ? AudioManager.ROUTE_SPEAKER : AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
		}
		else{
			if(IMSDroid.useSetModeToHackSpeaker()){
				audiomanager.setMode(AudioManager.MODE_IN_CALL);
			}
			audiomanager.setSpeakerphoneOn(speakerOn);
			if(IMSDroid.useSetModeToHackSpeaker()){
				audiomanager.setMode(AudioManager.MODE_NORMAL);
			}
		}
	}
	
	public void toggleSpeakerphone(){
		setSpeakerphoneOn(!IMSDroid.getAudioManager().isSpeakerphoneOn());
	}
	
	private int prepareCallback(int ptime, int rate, int channels) {
		Log.d(TAG, "prepareCallback("+ptime+","+rate+","+channels+")");
		
		final int minBufferSize = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		final int shortsPerNotif = (rate * ptime)/1000;
		mBufferSize = ((minBufferSize + (shortsPerNotif - (minBufferSize % shortsPerNotif))) * MyProxyAudioConsumer.AUDIO_BUFFER_FACTOR);
		mAudioFrame = ByteBuffer.allocateDirect(shortsPerNotif*2);
		
		setSpeakerphoneOn(false); // FIXME
		mAudioTrack = new AudioTrack(MyProxyAudioConsumer.AUDIO_STREAM_TYPE,
				rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
				mBufferSize, AudioTrack.MODE_STREAM);
		if(mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED){
			Log.d(TAG, "Consumer BufferSize="+mBufferSize+"MinBufferSize="+minBufferSize);
			//final float audioPlaybackLevel = ServiceManager.getConfigurationService().getFloat(
			//		ConfigurationEntry.GENERAL_AUDIO_PLAY_LEVEL,
			//		ConfigurationUtils.DEFAULT_GENERAL_AUDIO_PLAY_LEVEL);
			//this.audioTrack.setStereoVolume(AudioTrack.getMaxVolume()*audioPlaybackLevel, AudioTrack.getMaxVolume()*0.25f);
			//mAudioTrack.setStereoVolume(audioPlaybackLevel, audioPlaybackLevel);
			super.mPrepared = true;
			return 0;
		}
		else{
			Log.e(TAG, "prepare() failed");
			super.mPrepared = false;
			return -1;
		}
	}
	
	private int startCallback() {
		Log.d(TAG, "startCallback");
		if(mPrepared && this.mAudioTrack != null){
			super.mStarted = true;
			new Thread(this.runnablePlayer).start();
			return 0;
		}
		return -1;
	}
	
	private int pauseCallback() {
		Log.d(TAG, "pauseCallback");
		if(mAudioTrack != null){
			mAudioTrack.pause();
			super.mPaused = true;
			return 0;
		}
		return -1;
	}		

	private int stopCallback() {
		Log.d(TAG, "stopCallback");
		super.mStarted = false;
		if(mAudioTrack != null){
			return 0;
		}
		return -1;
	}
	
	private Runnable runnablePlayer = new Runnable(){
		@Override
		public void run() {
			Log.d(TAG, "===== Audio Player Thread (Start) =====");
			
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			
			int frameLength = MyProxyAudioConsumer.this.mAudioFrame.capacity();
			final int framesCount = 1; // Number of 20ms' to copy
			final byte[] audioBytes = new byte[frameLength*framesCount];
			int i, gapSize;
			long sizeInBytes;
			
			mAudioTrack.play();
			
			while(MyProxyAudioConsumer.super.mValid && MyProxyAudioConsumer.super.mStarted){
				if(mAudioTrack == null){
					break;
				}
				
				for(i=0; i<framesCount; i++){
					sizeInBytes = mConsumer.pull(mAudioFrame, frameLength);
					if(sizeInBytes>0){
						mAudioFrame.get(audioBytes, i*frameLength, (int)sizeInBytes);
						mAudioFrame.rewind();
						gapSize = (frameLength - (int)sizeInBytes);
						if(gapSize != 0){
							// Log.w(TAG, "Completing frame with silence");
							Arrays.fill(audioBytes, i*frameLength + (int)sizeInBytes, (i*frameLength + (int)sizeInBytes) + gapSize, (byte)0);
						}
					}
					else{
						Arrays.fill(audioBytes, i*frameLength, (i*frameLength)+frameLength, (byte)0);
					}
				}
				MyProxyAudioConsumer.this.mAudioTrack.write(audioBytes, 0, audioBytes.length);
			}
			
			if(mAudioTrack != null){
				mAudioTrack.stop();
				mAudioTrack.release();
				mAudioTrack = null;
			}
			Log.d(TAG, "===== Audio Player Thread (Stop) =====");
		}
	};
	
	/**
	 * MyProxyAudioConsumerCallback
	 */
	static class MyProxyAudioConsumerCallback extends ProxyAudioConsumerCallback
	{
		final MyProxyAudioConsumer myConsumer;
		
		MyProxyAudioConsumerCallback(MyProxyAudioConsumer consumer){
			super();
			myConsumer = consumer;
		}

		@Override
		public int prepare(int ptime, int rate, int channels) {
			return myConsumer.prepareCallback(ptime, rate, channels);
		}
		
		@Override
		public int start() {
			return myConsumer.startCallback();
		}
		
		@Override
		public int pause() {
			return myConsumer.pauseCallback();
		}		

		@Override
		public int stop() {
			return myConsumer.stopCallback();
		}
	}
}