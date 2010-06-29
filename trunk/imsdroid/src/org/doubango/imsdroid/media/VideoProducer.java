package org.doubango.imsdroid.media;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import org.doubango.tinyWRAP.ProxyVideoProducer;
import org.doubango.tinyWRAP.tmedia_chroma_t;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class VideoProducer {
	
	private static String TAG = VideoProducer.class.getCanonicalName();
	
	private static int WIDTH = 176;
	private static int HEIGHT = 144;
	private static int FPS = 15;
	private static int MAX_DELAY = 2;
	
	private final MyProxyVideoProducer videoProducer;
	private final ArrayList<byte[]> buffers;
	private final Semaphore semaphore;
	
	private Context context;
	private int width;
	private int height;
	private int fps;
	private ByteBuffer frame;
	private Preview preview;
	private boolean running;
	
	
	public VideoProducer(){
		this.videoProducer = new MyProxyVideoProducer(this);
		this.buffers = new ArrayList<byte[]>();
		this.semaphore = new Semaphore(0);
		
		this.width = VideoProducer.WIDTH;
		this.height = VideoProducer.HEIGHT;
		this.fps = VideoProducer.FPS;
	}
	
	public void setActive(){
		this.videoProducer.setActivate(true);
	}
	
	public void setContext(Context context){
		this.context = context;
		if(this.context == null){
			// FIXME
		}
	}
	
	// Must be done in the UI thread
	public final View startPreview(){
		if(this.preview == null){
			this.preview = new Preview(
					this.context,
					this.previewCallback,
					this.width,
					this.height, this.fps);
		}
		return this.preview;
	}
	
	private synchronized int pause(){
		Log.d(VideoProducer.TAG, "pause()");
		return 0;
	}
	
	private synchronized int start() {
		Log.d(VideoProducer.TAG, "start()");
		if(this.context != null){
			this.running = true;
			new Thread(this.runnableSender).start();
			return 0;
		}
		else{
			Log.e(VideoProducer.TAG, "Invalid context");
			return -1;
		}
	}
	
	private synchronized int stop() {
		Log.d(VideoProducer.TAG, "stop()");
		
		this.preview = null;
		this.context = null;
		
		this.running = false;
		this.semaphore.release();
		
		return 0;
	}
	
	private synchronized int prepare(int width, int height, int fps){
		Log.d(VideoProducer.TAG, "prepare()");
		this.width = width;
		this.height = height;
		this.fps = fps;
		
		float capacity = (float)(width*height)*1.5f/* (3/2) */;
		this.frame = ByteBuffer.allocateDirect((int)capacity);
		
		return 0;
	}
	
	private Runnable runnableSender = new Runnable(){
		@Override
		public void run() {
			Log.d(VideoProducer.TAG, "Sender ===== START");
			
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
			
			while(true){
				try {
					VideoProducer.this.semaphore.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
				
				if(!VideoProducer.this.running || (VideoProducer.this.videoProducer == null)){
					break;
				}
				
				final byte[] data;
				synchronized (VideoProducer.this.buffers) {
					if(!VideoProducer.this.buffers.isEmpty()){
						data = VideoProducer.this.buffers.remove(0);
					}
					else{
						continue;
					}
				}
				
				if(data != null){
					VideoProducer.this.frame.put(data);
					VideoProducer.this.videoProducer.push(VideoProducer.this.frame, data.length);
					VideoProducer.this.frame.rewind();
				}
			}
			Log.d(VideoProducer.TAG, "Sender ===== STOP");
		}
	};
	
	PreviewCallback previewCallback = new PreviewCallback() {
  	  public void onPreviewFrame(byte[] _data, Camera _camera) {
			if (VideoProducer.this.videoProducer != null) {
				if (VideoProducer.this.buffers.size() < VideoProducer.this.fps * VideoProducer.MAX_DELAY) {
					synchronized (VideoProducer.this.buffers) {
						VideoProducer.this.buffers.add(_data);
					}
					VideoProducer.this.semaphore.release();
				}
				else{
					//synchronized (VideoProducer.this.buffers) {
						//VideoProducer.this.buffers.clear();
					//}
				}
			}
		}
  	};
	
	/* ==================================================*/
	class Preview extends SurfaceView implements SurfaceHolder.Callback {
		private SurfaceHolder holder;
		private Camera camera;
		
		private final PreviewCallback callback;
		private final int width;
		private final int height;
		private final int fps;

		Preview(Context context, PreviewCallback callback, int width, int height, int fps) {
			super(context);

			this.callback = callback;
			this.width = width;
			this.height = height;
			this.fps = fps;
			
			this.holder = getHolder();
			this.holder.addCallback(this);
			this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // display
		}

		public void surfaceCreated(SurfaceHolder holder) {
			this.camera = Camera.open();
			try {

				Camera.Parameters parameters = camera.getParameters();
				
				parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
				parameters.setPreviewFrameRate(this.fps);
				parameters.setPictureSize(this.width, this.height);
				this.camera.setParameters(parameters);

				// layout(0, 0, this.width, this.height);

				this.camera.setPreviewDisplay(holder);
				this.camera.setPreviewCallback(this.callback);
			} catch (IOException exception) {
				this.camera.release();
				this.camera = null;
				// TODO: add more exception handling logic here
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			Camera.Parameters parameters = camera.getParameters();
			// parameters.setPreviewSize(w, h);
			// layout(0, 0, this.width, this.height);
			parameters.setPreviewSize(this.width, this.height);
			this.camera.setParameters(parameters);
			this.camera.startPreview();
		}
	}
	
	/* ==================================================*/
	class MyProxyVideoProducer extends ProxyVideoProducer
	{
		private final VideoProducer producer;
		
		private MyProxyVideoProducer(VideoProducer producer){
			super(tmedia_chroma_t.tmedia_nv21);
			this.producer = producer;
		}
		
		@Override
		public int pause() {
			return this.producer.pause();
		}

		@Override
		public int prepare(int width, int height, int fps) {
			return this.producer.prepare(width, height, fps);
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
