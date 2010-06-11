package org.doubango.imsdroid;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.util.Log;

// recorder : http://markmail.org/message/zf5iafifjfstjcwa#query:setNotificationMarkerPosition+page:1+mid:gnz4dcuvd5477qt5+state:results

public class AudioConsumer extends Thread{
	
	private static String TAG = AudioConsumer.class.getCanonicalName();
	
	public static final int sampleRateInHz = 8000;
	public static final int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	public static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	public static final int avgBytesPerSec = 0;
	public static final int ptime = 20;
	
	private int bytes_per_notif;
	private int bufferSize;
	private AudioTrack audioTrack;

	
	private boolean running;
	
	public AudioConsumer(){
		this.bufferSize = AudioTrack.getMinBufferSize(AudioConsumer.sampleRateInHz, AudioConsumer.channelConfig, AudioConsumer.audioFormat);
		this.bytes_per_notif = (AudioConsumer.sampleRateInHz * AudioConsumer.ptime)/1000;

	}	

//	 public void StartRecord() { recorder.startRecording(); }
//	 public void StopRecord() { recorder.stop(); }
//	 public void ReleaseRecord() { recorder.release(); } 
	 
	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		super.start();
	}

	public synchronized void stopMe() {
		this.running = false;
	}
	
	
	private OnPlaybackPositionUpdateListener playbackPositionUpdateListener = new OnPlaybackPositionUpdateListener()
	{
		@Override
		public void onMarkerReached(AudioTrack track) {
			int i = 0;
			i++;
			Log.d(AudioConsumer.TAG, "onMarkerReached");
		}

		@Override
		public void onPeriodicNotification(AudioTrack track) {
			Log.d(AudioConsumer.TAG, "onPeriodicNotification");
		}
	};
	
	Runnable rrr = new Runnable()
	{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			byte []bytes = new byte[bufferSize];
			new Random().nextBytes(bytes);
			
			
			audioTrack.write(bytes, 0, bytes.length);
		}
	};
	
	@Override
	public void run() {
		/* Set Thread Priority */
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
		
		this.audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, /* AudioManager.STREAM_MUSIC */
				AudioConsumer.sampleRateInHz,
				AudioConsumer.channelConfig,
				AudioConsumer.audioFormat,
                this.bufferSize,
                AudioTrack.MODE_STREAM);
		
		int ret = this.audioTrack.setPositionNotificationPeriod(160/2);
		ret = this.audioTrack.setNotificationMarkerPosition(160);
		this.audioTrack.setPlaybackPositionUpdateListener(this.playbackPositionUpdateListener); 
		
		byte []bytes = new byte[742];
		new Random().nextBytes(bytes);
		
		Log.d(AudioConsumer.TAG, "before write");
		audioTrack.write(bytes, 0, bytes.length);
		Log.d(AudioConsumer.TAG, "after write");
		
		//audioTrack.setPlaybackHeadPosition(0);

		//this.audioTrack.flush();
		this.audioTrack.play();
		Log.d(AudioConsumer.TAG, "after play");
		
		while(true){
			try {
				
				//new Thread(this.rrr).start();
				sleep(5000);
				
				//bytes = new byte[bytes_per_notif];
				//new Random().nextBytes(bytes);
				//audioTrack.write(bytes, 0, bytes.length);
				//audioTrack.play();				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
}
