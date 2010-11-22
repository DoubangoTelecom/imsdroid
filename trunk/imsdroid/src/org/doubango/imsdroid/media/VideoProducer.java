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
* 	@author Mamadou Diop <diopmamadou(at)doubango.org>
* 	@author Alex Vishnev 
* 		- Add support for rotation
* 		- Camera toggle
*/

package org.doubango.imsdroid.media;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;

import org.doubango.imsdroid.IMSDroid;
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
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

public class VideoProducer {
	
	private static String TAG = VideoProducer.class.getCanonicalName();
	
	private static int WIDTH = 176;
	private static int HEIGHT = 144;
	private static int FPS = 15;
	private static final int CALLABACK_BUFFERS_COUNT = 3;
	
	private final MyProxyVideoProducer videoProducer;
	
	private Context context;
	private int width;
	private int height;
	private int fps;
	private ByteBuffer frame;
	private Preview preview;
	private Camera camera;
	@SuppressWarnings("unused")
	private boolean running;
	private boolean toggle;
	
	
	public VideoProducer(){
		this.videoProducer = new MyProxyVideoProducer(this);
		
		this.width = VideoProducer.WIDTH;
		this.height = VideoProducer.HEIGHT;
		this.fps = VideoProducer.FPS;
		this.toggle = false;
	}
	
	public void setActive(){
		this.videoProducer.setActivate(true);
	}
	
	public ProxyVideoProducer getProxyVideoProducer(){
		return this.videoProducer;
	}
	
	public Camera getCamera(){
		return this.camera;
	}
	
	public void pushBlankPacket(){
		if(this.videoProducer != null && this.frame != null){
			ByteBuffer buffer = ByteBuffer.allocateDirect(this.frame.capacity());
			this.videoProducer.push(buffer, buffer.capacity());
		}
	}
	
	public void setCamera(Camera cam) {
		this.camera = cam;
	}
	
	public void toggleCamera(LinearLayout llVideoLocal){
		if (this.preview != null) {
			this.toggle = !this.toggle;
			this.reset();
			this.start();
			final View local_preview = startPreview();
			if(local_preview != null){
				final ViewParent viewParent = local_preview.getParent();
				if(viewParent != null && viewParent instanceof ViewGroup){
					((ViewGroup)(viewParent)).removeView(local_preview);
				}
				llVideoLocal.addView(local_preview);
				llVideoLocal.setVisibility(View.VISIBLE);
			}
		}
	
	}
	public void setContext(Context context){
		this.context = context;
		if(this.context == null){
			// FIXME
		}
	}
	
	private void addCallbackBuffer(Camera camera, byte[] buffer) {
		try {
			APILevel7.addCallbackBufferMethod.invoke(camera, buffer);
		} catch (Exception e) {
			Log.e(VideoProducer.TAG, e.toString());
		}
	}
	
	private void setPreviewCallbackWithBuffer(Camera camera, PreviewCallback callback) {
		try {
			APILevel7.setPreviewCallbackWithBufferMethod.invoke(camera, callback);
		} catch (Exception e) {
			Log.e(VideoProducer.TAG, e.toString());
		}
	}
	
	private void setDisplayOrientation(Camera camera, int degrees) {
		try {
			if(APILevel8.setDisplayOrientationMethod != null)
				APILevel8.setDisplayOrientationMethod.invoke(camera, degrees);
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
		else {
			this.preview.setVisibility(View.VISIBLE);
			this.preview.getHolder().setSizeFromLayout();
			this.preview.bringToFront();
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
		
		return 0;
	}
	
	private synchronized int reset() {
		Log.d(VideoProducer.TAG, "reset()");
				
	    this.preview.setVisibility(View.INVISIBLE);
		this.running = false;
		
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
	
	private PreviewCallback previewCallback = new PreviewCallback() {
  	  public void onPreviewFrame(byte[] _data, Camera _camera) {
  		  	VideoProducer.this.frame.put(_data);
  		  	VideoProducer.this.videoProducer.push(VideoProducer.this.frame, VideoProducer.this.frame.capacity());
			VideoProducer.this.frame.rewind();
		
	  		if(APILevel7.isAvailable()){
				VideoProducer.this.addCallbackBuffer(_camera, _data);
			}
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
				final boolean useFFC = ServiceManager.getConfigurationService().getBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.FFC, Configuration.DEFAULT_GENERAL_FFC);
				Log.d(VideoProducer.TAG, useFFC ? "Using FFC" : "Not using FFC");
				if (!this.producer.toggle) {
					if(useFFC && FFC.isAvailable()){
						this.camera = FFC.getCamera(); // Get FFC camera
					}
					if(this.camera == null){
						this.camera = Camera.open();
					}
					// Switch to Front Facing Camera
					if(useFFC){
						FFC.switchToFFC(this.camera);
					}
				}
				else {
					this.camera = Camera.open();
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
				
				if(APILevel7.isAvailable()){
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
			Log.d(VideoProducer.TAG,"Destroy Preview");
			if(this.camera != null){
				// stop preview
				Log.d(VideoProducer.TAG,"Close Camera");
				this.camera.stopPreview();
				if(APILevel7.isAvailable()){
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
			Log.d(VideoProducer.TAG,"Surface Changed Callback");
			if(this.camera != null && this.producer != null && this.producer.frame != null){
				try{
					Camera.Parameters parameters = this.camera.getParameters();
					parameters.setPreviewSize(this.producer.width, this.producer.height);
					this.camera.setParameters(parameters);
				}
				catch(Exception e){
					Log.e(VideoProducer.TAG, e.toString());
				}
												
				android.content.res.Configuration conf = IMSDroid.getContext().getResources().getConfiguration();
				if(APILevel7.isAvailable()){
					// Camera Orientation
					switch(conf.orientation){
						case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
							this.producer.setDisplayOrientation(this.camera, 0);
							Log.d(VideoProducer.TAG, "Orientation=landscape");
							break;
						case android.content.res.Configuration.ORIENTATION_PORTRAIT:
							this.producer.setDisplayOrientation(this.camera, 90);
							Log.d(VideoProducer.TAG, "Orientation=portrait");
							break;
					}
					// Callback Buffers
					for(int i=0; i<VideoProducer.CALLABACK_BUFFERS_COUNT; i++){
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
		static Method setDisplayOrientationMethod = null;
		static boolean isOK = false;
		
		static {
			try {
				APILevel8.setDisplayOrientationMethod = Camera.class.getMethod(
						"setDisplayOrientation", int.class);
				
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
	static class APILevel7{
		static Method addCallbackBufferMethod = null;
		static Method setPreviewCallbackWithBufferMethod = null;
		static boolean isOK = false;
		
		static {
			try {
				// According to http://developer.android.com/reference/android/hardware/Camera.html both addCallbackBuffer and setPreviewCallbackWithBuffer
				// are only available starting API level 8. But it's not true as these functions exist in API level 7 but are hidden.
				APILevel7.addCallbackBufferMethod = Camera.class.getMethod(
						"addCallbackBuffer", byte[].class);
				APILevel7.setPreviewCallbackWithBufferMethod = Camera.class.getMethod(
						"setPreviewCallbackWithBuffer", PreviewCallback.class);
				
				APILevel7.isOK = true;
			} catch (Exception e) {
				Log.d(VideoProducer.TAG, e.toString());
			}
		}
		
		static boolean isAvailable(){
			return APILevel7.isOK;
		}
	}
	
	/* ==================================================*/
	static class APILevel5{
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
					Log.d(VideoProducer.TAG, e.toString());
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
