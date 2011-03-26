package org.doubango.ngn.media;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.tinyWRAP.ProxyAudioConsumer;
import org.doubango.tinyWRAP.ProxyAudioConsumerCallback;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * MyProxyAudioConsumer
 */
public class NgnProxyAudioConsumer extends NgnProxyPlugin{
	private static final String TAG = NgnProxyAudioConsumer.class.getCanonicalName();
	private static final int AUDIO_BUFFER_FACTOR = 2;
	private static final int AUDIO_STREAM_TYPE = AudioManager.STREAM_VOICE_CALL;
	
	private final INgnConfigurationService mConfigurationService;
	private final MyProxyAudioConsumerCallback mCallback;
	private final ProxyAudioConsumer mConsumer;
	private boolean mRoutingChanged;
	
	private int mBufferSize;
	private AudioTrack mAudioTrack;
	private int mPtime, mInputRate, mChannels;
	
	private int mOutputRate;
	private ByteBuffer mOutputBuffer;
	
	public NgnProxyAudioConsumer(BigInteger id, ProxyAudioConsumer consumer){
		super(id, consumer);
        mCallback = new MyProxyAudioConsumerCallback(this);
        mConsumer = consumer;
        mConsumer.setCallback(mCallback);
        mConfigurationService = NgnEngine.getInstance().getConfigurationService();
	}
	
	public void setSpeakerphoneOn(boolean speakerOn){
		Log.d(TAG, "setSpeakerphoneOn("+speakerOn+")");
		final AudioManager audiomanager = NgnApplication.getAudioManager();
		if (NgnApplication.getSDKVersion() < 5){
			audiomanager.setRouting(AudioManager.MODE_IN_CALL, 
					speakerOn ? AudioManager.ROUTE_SPEAKER : AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
		}
		else if(NgnApplication.useSetModeToHackSpeaker()){
			audiomanager.setMode(AudioManager.MODE_IN_CALL);
			audiomanager.setSpeakerphoneOn(speakerOn);
			audiomanager.setMode(AudioManager.MODE_NORMAL);
		}
		else{
			audiomanager.setSpeakerphoneOn(speakerOn);
		}
		
		if (super.mPrepared){
			mRoutingChanged = NgnApplication.isAudioRecreateRequired();
			changeVolume(false,false);// disable attenuation
		}
	}
	
	public void toggleSpeakerphone(){
		setSpeakerphoneOn(!NgnApplication.getAudioManager().isSpeakerphoneOn());
	}
	
	public boolean onVolumeChanged(boolean bDown){
		if(!mPrepared || mAudioTrack == null){
			return false;
		}
		return changeVolume(bDown, true);
	}
	
	private boolean changeVolume(boolean bDown, boolean bVolumeChanged){
		final AudioManager audioManager = NgnApplication.getAudioManager();
		if(audioManager != null){
			if(bVolumeChanged){
				audioManager.adjustStreamVolume(AUDIO_STREAM_TYPE, bDown ? AudioManager.ADJUST_LOWER : AudioManager.ADJUST_RAISE, 
					AudioManager.FLAG_SHOW_UI);
			}
			if(audioManager.isSpeakerphoneOn()){
				return mAudioTrack.setStereoVolume(AudioTrack.getMaxVolume()*1.0f, AudioTrack.getMaxVolume()*1.0f) == AudioTrack.SUCCESS;
			}
			else{
				final float attenuation = mConfigurationService.getFloat(NgnConfigurationEntry.MEDIA_AUDIO_CONSUMER_ATTENUATION, 
						NgnConfigurationEntry.DEFAULT_MEDIA_AUDIO_CONSUMER_ATTENUATION);
				Log.d(TAG, "Consumer audio attenuation "+attenuation);
				return mAudioTrack.setStereoVolume(AudioTrack.getMaxVolume()*attenuation, AudioTrack.getMaxVolume()*attenuation) == AudioTrack.SUCCESS;
			}
		}
		return false;
	}
	
	
	private int prepareCallback(int ptime, int rate, int channels) {
		Log.d(TAG, "prepareCallback("+ptime+","+rate+","+channels+")");
		return prepare(ptime, rate, channels);
	}
	
	private int startCallback() {
		Log.d(TAG, "startCallback");
		if(mPrepared && this.mAudioTrack != null){
			super.mStarted = true;
			final Thread t = new Thread(mRunnablePlayer, "AudioConsumerThread");
			// t.setPriority(Thread.MAX_PRIORITY);
			t.start();
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
	
	private synchronized int prepare(int ptime, int rate, int channels){
		if(super.mPrepared){
			Log.e(TAG,"already prepared");
			return -1;
		}
		
		mPtime = ptime; mInputRate = rate; mChannels = channels; 
		mOutputRate = AudioTrack.getNativeOutputSampleRate(AUDIO_STREAM_TYPE);
		if(super.mValid && mConsumer != null && mInputRate != mOutputRate){
			if(mConsumer.queryForResampler(mInputRate, mOutputRate, mPtime, mChannels, 
					mConfigurationService.getInt(NgnConfigurationEntry.MEDIA_AUDIO_RESAMPLER_QUALITY, NgnConfigurationEntry.DEFAULT_MEDIA_AUDIO_RESAMPLER_QUALITY))){
				Log.d(TAG, "queryForResampler("+mOutputRate+") succeed");
			}
			else{
				Log.e(TAG, "queryForResampler("+mOutputRate+") failed. Using "+mInputRate);
				mOutputRate = mInputRate;
			}
		}
		
		final int minBufferSize = AudioTrack.getMinBufferSize(mOutputRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		final int shortsPerNotif = (mOutputRate * mPtime)/1000;
		mBufferSize = ((minBufferSize + (shortsPerNotif - (minBufferSize % shortsPerNotif))) * AUDIO_BUFFER_FACTOR);
		mOutputBuffer = ByteBuffer.allocateDirect(shortsPerNotif*2);
		
		// setSpeakerphoneOn(false);
		mAudioTrack = new AudioTrack(AUDIO_STREAM_TYPE,
				mOutputRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
				mBufferSize, AudioTrack.MODE_STREAM);
		if(mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED){
			Log.d(TAG, "Consumer BufferSize="+mBufferSize+",MinBufferSize="+minBufferSize+",TrueSampleRate="+mAudioTrack.getSampleRate());
			changeVolume(false, false);
			super.mPrepared = true;
			return 0;
		}
		else{
			Log.e(TAG, "prepare() failed");
			super.mPrepared = false;
			return -1;
		}
	}
	
	private synchronized void unprepare(){
		if(mAudioTrack != null){
			if(super.mPrepared){
				mAudioTrack.stop();
			}
			mAudioTrack.release();
			mAudioTrack = null;
		}
		super.mPrepared = false;
	}
	
	private Runnable mRunnablePlayer = new Runnable(){
		@Override
		public void run() {
			Log.d(TAG, "===== Audio Player Thread (Start) =====");
			
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			
			int nFrameLength = mOutputBuffer.capacity();
			final int nFramesCount = 1; // Number of 20ms' to copy
			final byte[] aAudioBytes = new byte[nFrameLength*nFramesCount];
			int i, nGapSize;
			long lSizeInBytes;
			boolean bPlaying = false;
			int nWritten = 0;
			
			if(NgnProxyAudioConsumer.super.mValid){
				mConsumer.setPullBuffer(mOutputBuffer, mOutputBuffer.capacity());
				mConsumer.setGain(NgnEngine.getInstance().getConfigurationService().getInt(NgnConfigurationEntry.MEDIA_AUDIO_CONSUMER_GAIN, 
						NgnConfigurationEntry.DEFAULT_MEDIA_AUDIO_CONSUMER_GAIN));
			}
			
			while(NgnProxyAudioConsumer.super.mValid && NgnProxyAudioConsumer.super.mStarted){
				if(mAudioTrack == null){
					break;
				}
				
				if(mRoutingChanged){
					Log.d(TAG, "Routing changed: restart() player");
					mRoutingChanged = false;
					unprepare();
					if(prepare(mPtime, mOutputRate, mChannels) != 0){
						break;
					}
					if(!NgnProxyAudioConsumer.super.mPaused){
						bPlaying = false;
						nWritten = 0;
					}
				}
				
				for(i=0; i<nFramesCount; i++){
					lSizeInBytes = mConsumer.pull();
					if(lSizeInBytes>0){
						mOutputBuffer.get(aAudioBytes, i*nFrameLength, (int)lSizeInBytes);
						mOutputBuffer.rewind();
						nGapSize = (nFrameLength - (int)lSizeInBytes);
						if(nGapSize != 0){
							Arrays.fill(aAudioBytes, i*nFrameLength + (int)lSizeInBytes, (i*nFrameLength + (int)lSizeInBytes) + nGapSize, (byte)0);
						}
					}
					else{
						Arrays.fill(aAudioBytes, i*nFrameLength, (i*nFrameLength)+nFrameLength, (byte)0);
					}
					nWritten += nFrameLength;
				}
				
				mAudioTrack.write(aAudioBytes, 0, aAudioBytes.length);
				if(!bPlaying && nWritten>mBufferSize){
					mAudioTrack.play();
					bPlaying = true;
				}
			}
			
			unprepare();
			Log.d(TAG, "===== Audio Player Thread (Stop) =====");
		}
	};
	
	/**
	 * MyProxyAudioConsumerCallback
	 */
	static class MyProxyAudioConsumerCallback extends ProxyAudioConsumerCallback
	{
		final NgnProxyAudioConsumer myConsumer;
		
		MyProxyAudioConsumerCallback(NgnProxyAudioConsumer consumer){
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
