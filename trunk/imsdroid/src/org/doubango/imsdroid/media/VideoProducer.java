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

import java.lang.reflect.Method;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
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
	private boolean skipFrames = false;
	
	
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
	
	private void addCallbackBuffer(Camera camera, byte[] buffer) {
		try {
			APILevel8.addCallbackBufferMethod.invoke(camera, buffer);
		} catch (Exception e) {
			Log.e(VideoProducer.TAG, e.toString());
		}
	}
	
	private void setPreviewCallbackWithBuffer(Camera camera, PreviewCallback callback) {
		try {
			APILevel8.setPreviewCallbackWithBufferMethod.invoke(camera, callback);
		} catch (Exception e) {
			Log.e(VideoProducer.TAG, e.toString());
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private List<Camera.Size> getSupportedPreviewSizes(Camera.Parameters params){
		List<Camera.Size> list = null;
		try {
			list = (List<Camera.Size>)APILevel5.getSupportedPreviewSizesMethod.invoke(params);
		} catch (Exception e) {
			Log.e(VideoProducer.TAG, e.toString());
		}
		return list;
	}
	
	// Must be done in the UI thread
	public final View startPreview(){
		if(this.preview == null){
			this.preview = new Preview(this);
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
  		  	VideoProducer.this.frame.put(_data);
  		  	VideoProducer.this.videoProducer.push(VideoProducer.this.frame, VideoProducer.this.frame.capacity());
			VideoProducer.this.frame.rewind();
		
	  		if(APILevel8.isAvailable()){
				VideoProducer.this.addCallbackBuffer(_camera, _data);
			}
	  		
			/*if (VideoProducer.this.videoProducer != null) {	
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
			}*/
		}
  	};
	
	/* ==================================================*/
	class Preview extends SurfaceView implements SurfaceHolder.Callback {
		private SurfaceHolder holder;
		private Camera camera;
		
		private final VideoProducer producer;

		Preview(VideoProducer _producer) {
			super(_producer.context);
			
			this.producer = _producer;
			this.holder = getHolder();
			this.holder.addCallback(this);
			
			this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // display
		}

		public void surfaceCreated(SurfaceHolder holder) {
			try {
				boolean useFFC = ServiceManager.getConfigurationService().getBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.FFC, Configuration.DEFAULT_GENERAL_FFC);
				Log.d(VideoProducer.TAG, useFFC ? "Using FFC" : "Not using FFC");
				
				if(useFFC && FFC.isAvailable()){
					this.camera = FFC.getCamera(); // Get FFC
				}
				if(this.camera == null){
					this.camera = Camera.open();
				}
				
				// Switch to Front Facing Camera
				if(useFFC){
					FFC.switchToFFC(this.camera);
				}
				
				Camera.Parameters parameters = this.camera.getParameters();
				
				/*
				 * http://developer.android.com/reference/android/graphics/ImageFormat.html#NV21
				 * YCrCb format used for images, which uses the NV21 encoding format. 
				 * This is the default format for camera preview images, when not otherwise set with setPreviewFormat(int). 
				 */
				parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
				parameters.setPreviewFrameRate(this.producer.fps);
				// parameters.set("rotation", degree)
				this.camera.setParameters(parameters);
				
				try{
					parameters.setPictureSize(this.producer.width, this.producer.height);
					this.camera.setParameters(parameters);
				}
				catch(Exception e){
					// FFMpeg converter will resize the video stream
					Log.d(VideoProducer.TAG, e.toString());
				}

				this.camera.setPreviewDisplay(holder);
				
				if(APILevel8.isAvailable()){
					this.producer.setPreviewCallbackWithBuffer(this.camera, this.producer.previewCallback);
				}
				else{
					this.camera.setPreviewCallback(this.producer.previewCallback);
				}
				
			} catch (Exception exception) {
				if(this.camera != null){
					this.camera.release();
					this.camera = null;
				}
				Log.e(VideoProducer.TAG, exception.toString());
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			if(this.camera != null){
				// stop preview
				this.camera.stopPreview();
				if(APILevel8.isAvailable()){
					this.producer.setPreviewCallbackWithBuffer(this.camera, null);
				}
				else{
					this.camera.setPreviewCallback(null);
				}
				this.camera.release();
				this.camera = null;
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			if(this.camera != null && this.producer != null && this.producer.frame != null){
				try{
					Camera.Parameters parameters = this.camera.getParameters();
					parameters.setPreviewSize(this.producer.width, this.producer.height);
					this.camera.setParameters(parameters);
				}
				catch(Exception e){
					Log.e(VideoProducer.TAG, e.toString());
				}
				
				/*if(APILevel5.isAvailable()){
					List<Camera.Size> list = this.producer.getSupportedPreviewSizes(parameters);
					if(list != null){
						for(Camera.Size size : list){
							Log.d(VideoProducer.TAG, "size="+size.toString());
						}
					}
				}*/
				
				if(APILevel8.isAvailable()){	
					for(int i=0; i<1; i++){
						this.producer.addCallbackBuffer(this.camera, new byte[this.producer.frame.capacity()]);
					}
				}
				
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
	
	
	
	
	/* ==================================================*/
	static class APILevel8{
		static Method addCallbackBufferMethod = null;
		static Method setPreviewCallbackWithBufferMethod = null;
		static boolean isOK = false;
		
		static {
			try {
				APILevel8.addCallbackBufferMethod = Class.forName(
						"android.hardware.Camera").getMethod("addCallbackBuffer",
						byte[].class);
				APILevel8.setPreviewCallbackWithBufferMethod = Class.forName(
						"android.hardware.Camera").getMethod(
						"setPreviewCallbackWithBuffer", PreviewCallback.class);
				
				APILevel8.isOK = true;
			} catch (Exception e) {
				Log.d(VideoProducer.TAG, e.toString());
			}
		}
		
		static boolean isAvailable(){
			return APILevel8.isOK;
		}
	}
	
	/* ==================================================*/
	static class APILevel5{
		static boolean hasAPILeve5Functions = false;
		static Method getSupportedPreviewSizesMethod = null;
		static boolean isOK = false;
		
		static {
			try {
				APILevel5.getSupportedPreviewSizesMethod = Camera.Parameters.class.getDeclaredMethod("getSupportedPreviewSizes");
				
				APILevel5.isOK = true;
			} catch (Exception e) {
				Log.d(VideoProducer.TAG, e.toString());
			}
		}
		
		static boolean isAvailable(){
			return APILevel5.isOK;
		}
	}
	
	/* ==================================================*/
	static class FFC{
		private final String className;
		private final String methodName;
		
		private static Method DualCameraSwitchMethod;
		private static int ffc_index = -1;
		static FFC FFC_VALUES[] = {
			new FFC("android.hardware.HtcFrontFacingCamera", "getCamera"),
			// Sprint: HTC EVO 4G and Samsung Epic 4G
			// DO not forget to change the manifest if you are using OS 1.6 and later
			new FFC("com.sprint.hardware.twinCamDevice.FrontFacingCamera", "getFrontFacingCamera"),
			// Huawei U8230
            new FFC("android.hardware.CameraSlave", "open"),
			// To be continued...
			// Default: Used for test reflection
			//--new FFC("android.hardware.Camera", "open"),
		};
		
		static{
			
			//
			//
			//
			int index = 0;
			for(FFC ffc: FFC.FFC_VALUES){
				try{
					Class.forName(ffc.className).getDeclaredMethod(ffc.methodName);
					FFC.ffc_index = index;
					break;
				}
				catch(Exception e){
					Log.e(VideoProducer.TAG, e.toString());
				}
				
				++index;
			}
			
			//
			//
			//
			try{
				FFC.DualCameraSwitchMethod = Class.forName("android.hardware.Camera").getMethod("DualCameraSwitch",int.class);
			}
			catch(Exception e){
				Log.e(VideoProducer.TAG, e.toString());
			}
		}
		
		private FFC(String className, String methodName){
			this.className = className;
			this.methodName = methodName;
		}
		
		static boolean isAvailable(){
			return (FFC.ffc_index != -1);
		}
		
		static Camera getCamera(){
			try{
				Method method = Class.forName(FFC.FFC_VALUES[FFC.ffc_index].className).getDeclaredMethod(FFC.FFC_VALUES[FFC.ffc_index].methodName);
				return (Camera)method.invoke(null);
			}
			catch(Exception e){
				Log.e(VideoProducer.TAG, e.toString());
			}
			return null;
		}
		
		static void switchToFFC(Camera camera){
			try{
				if(FFC.DualCameraSwitchMethod == null){ // Samsung Galaxy S, Epic 4G, ...
					Camera.Parameters parameters = camera.getParameters();
					parameters.set("camera-id", 2);
					camera.setParameters(parameters);
				}
				else{ // Dell Streak, ...
					FFC.DualCameraSwitchMethod.invoke(camera, (int)1);
				}
			}
			catch(Exception e){
				Log.e(VideoProducer.TAG, e.toString());
			}
		
		}
	}
}
