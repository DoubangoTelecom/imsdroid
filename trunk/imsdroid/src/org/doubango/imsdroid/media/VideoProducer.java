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

import java.nio.BufferOverflowException;
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
	private static float MAX_DELAY = 0.5f;
	
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
	boolean skipFrames = false;
	
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
		Log.d(VideoProducer.TAG, String.format("prepare(%d, %d, %d)", width, height, fps));
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
			Log.d(VideoProducer.TAG, "Video Sender ===== START");
			
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT);
			
			byte[] data;
			final int capacity = VideoProducer.this.frame.capacity();
			while(VideoProducer.this.running){
				try {
					VideoProducer.this.semaphore.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
				
				synchronized (VideoProducer.this.buffers) {			
					if(!VideoProducer.this.buffers.isEmpty()){
						data = VideoProducer.this.buffers.remove(0);
					}
					else{
						continue;
					}
				}
				
				if(data != null){
					try{
						VideoProducer.this.frame.put(data);
						VideoProducer.this.videoProducer.push(VideoProducer.this.frame, capacity);
						VideoProducer.this.frame.rewind();
					}
					catch(BufferOverflowException e){
						e.printStackTrace();
						break;
					}				
				}
			}
			
			VideoProducer.this.buffers.clear();
			
			Log.d(VideoProducer.TAG, "Video Sender ===== STOP");
		}
	};
	
	private PreviewCallback previewCallback = new PreviewCallback() {
  	  public void onPreviewFrame(byte[] _data, Camera _camera) {
			if (VideoProducer.this.videoProducer != null) {	
				if(VideoProducer.this.skipFrames){
					//Log.d(VideoProducer.TAG, "Frame skipped");
					synchronized (VideoProducer.this.buffers) {
						if (VideoProducer.this.buffers.size() == 0) {
							VideoProducer.this.skipFrames = false;
						}
					}
					return;
				}
				else if (VideoProducer.this.buffers.size() >= VideoProducer.this.fps * VideoProducer.MAX_DELAY) {
					//Log.d(VideoProducer.TAG, "....Too Many Frames");
					VideoProducer.this.skipFrames = true;
				}
				
				synchronized (VideoProducer.this.buffers) {
					VideoProducer.this.buffers.add(_data);
				}
				VideoProducer.this.semaphore.release();
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
			try {
				this.camera = Camera.open();
				
				Camera.Parameters parameters = camera.getParameters();
				
				/*
				 * http://developer.android.com/reference/android/graphics/ImageFormat.html#NV21
				 * YCrCb format used for images, which uses the NV21 encoding format. 
				 * This is the default format for camera preview images, when not otherwise set with setPreviewFormat(int). 
				 */
				parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
				parameters.setPreviewFrameRate(this.fps);
				this.camera.setParameters(parameters);
				
				try{
					parameters.setPictureSize(this.width, this.height);
					this.camera.setParameters(parameters);
				}
				catch(Exception e){
					// FFMpeg converter will resize the video stream
					e.printStackTrace();
				}

				// layout(0, 0, this.width, this.height);

				this.camera.setPreviewDisplay(holder);
				this.camera.setPreviewCallback(this.callback);
			} catch (Exception exception) {
				if(this.camera != null){
					this.camera.release();
					this.camera = null;
				}
				exception.printStackTrace();
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			if(this.camera != null){
				this.camera.stopPreview();
				this.camera.setPreviewCallback(null);
				this.camera.release();
				this.camera = null;
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			if(this.camera != null){
				Camera.Parameters parameters = camera.getParameters();
				parameters.setPreviewSize(this.width, this.height);
				this.camera.setParameters(parameters);
				this.camera.startPreview();
			}
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
