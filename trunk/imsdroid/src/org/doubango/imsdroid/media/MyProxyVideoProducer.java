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

import org.doubango.imsdroid.IMSDroid;
import org.doubango.tinyWRAP.ProxyVideoProducer;
import org.doubango.tinyWRAP.ProxyVideoProducerCallback;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;


/**
 * MyProxyVideoProducer
 * @author Mamadou Diop
 *
 */
public class MyProxyVideoProducer extends MyProxyPlugin{
	private static final String TAG = MyProxyVideoProducer.class.getCanonicalName();
	private static final int DEFAULT_VIDEO_WIDTH = 176;
	private static final int DEFAULT_VIDEO_HEIGHT = 144;
	private static final int DEFAULT_VIDEO_FPS = 15;
	private static final int CALLABACK_BUFFERS_COUNT = 3;
	private static final boolean addCallbackBufferSupported = MyCameraProducer.isAddCallbackBufferSupported();
	
	private final ProxyVideoProducer producer;
	private final MyProxyVideoProducerCallback callback;
	private Context context;
	private MyProxyVideoProducerPreview preview;
	private int width;
	private int height;
	private int fps;
	private ByteBuffer videoFrame;
	
	public MyProxyVideoProducer(BigInteger id, ProxyVideoProducer producer){
		super(id, producer);
        this.callback = new MyProxyVideoProducerCallback(this);
        this.producer = producer;
        this.producer.setCallback(this.callback);
        
     	// Initialize video stream parameters with default values
        this.width = MyProxyVideoProducer.DEFAULT_VIDEO_WIDTH;
		this.height = MyProxyVideoProducer.DEFAULT_VIDEO_HEIGHT;
		this.fps = MyProxyVideoProducer.DEFAULT_VIDEO_FPS;
    }
	
	public void setContext(Context context){
    	this.context = context;
    }
    
	// Very important: Must be done in the UI thread
	public final View startPreview(){
		if(this.preview == null){
			this.preview = new MyProxyVideoProducerPreview(this);
		}
		else {
			this.preview.setVisibility(View.VISIBLE);
			this.preview.getHolder().setSizeFromLayout();
			this.preview.bringToFront();
		}
		return this.preview;
	}
	
	public void pushBlankPacket(){
		if(this.valid && this.producer != null && this.videoFrame != null){
			ByteBuffer buffer = ByteBuffer.allocateDirect(this.videoFrame .capacity());
			this.producer.push(buffer, buffer.capacity());
		}
	}
	
	public void toggleCamera(){
		if(this.valid && this.started && !this.paused && this.producer != null){
			final Camera camera = MyCameraProducer.toggleCamera();
			try{
				this.startCameraPreview(camera);
			}
			catch (Exception exception) {
				Log.e(MyProxyVideoProducer.TAG, exception.toString());
			}
		}
	}
	
	public void setRotation(int rot){
		if(this.producer != null && this.valid){
			this.producer.setRotation(rot);
		}
	}
	
	public void setOnPause(boolean pause){
		if(this.paused == pause){
			return;
		}
		try {
			if(this.started){
				final Camera camera = MyCameraProducer.getCamera();
				if(pause){
					camera.stopPreview();
				}
				else{
					camera.startPreview();
				}
			}
		} catch(Exception e){
			Log.e(MyProxyVideoProducer.TAG, e.toString());
		}
		
		this.paused = pause;
	}
	
	private int prepareCallback(int width, int height, int fps){
		Log.d(MyProxyVideoProducer.TAG, "prepareCallback("+width+","+height+","+fps+")");
		
		this.width = width;
		this.height = height;
		this.fps = fps;
		
		float capacity = (float)(width*height)*1.5f/* (3/2) */;
		this.videoFrame = ByteBuffer.allocateDirect((int)capacity);
		
		return 0;
    }

    private int startCallback(){
    	Log.d(MyProxyVideoProducer.TAG, "startCallback");
		this.started = true;
		return 0;
    }

    private int pauseCallback(){
    	Log.d(MyProxyVideoProducer.TAG, "pauseCallback");
    	this.setOnPause(true);
    	return 0;
    }

    private int stopCallback(){
    	Log.d(MyProxyVideoProducer.TAG, "stopCallback");
    	
		this.preview = null;
		this.context = null;
		this.started = false;
		
		return 0;
    }
	
    private void startCameraPreview(Camera camera){
		if(camera != null && this.producer != null && this.videoFrame != null){
			try{
				Camera.Parameters parameters = camera.getParameters();
				parameters.setPreviewSize(this.width, this.height);
				camera.setParameters(parameters);
			} catch(Exception e){
				Log.e(MyProxyVideoProducer.TAG, e.toString());
			}
								
			android.content.res.Configuration conf = IMSDroid.getContext().getResources().getConfiguration();
			// Camera Orientation
			switch(conf.orientation){
				case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
					MyCameraProducer.setDisplayOrientation(camera, 0);
					Log.d(MyProxyVideoProducer.TAG, "Orientation=landscape");
					break;
				case android.content.res.Configuration.ORIENTATION_PORTRAIT:
					MyCameraProducer.setDisplayOrientation(camera, 90);
					Log.d(MyProxyVideoProducer.TAG, "Orientation=portrait");
					break;
			}
			
			// Callback Buffers
			if(MyProxyVideoProducer.addCallbackBufferSupported){
				for(int i=0; i<MyProxyVideoProducer.CALLABACK_BUFFERS_COUNT; i++){
					MyCameraProducer.addCallbackBuffer(camera, new byte[this.videoFrame.capacity()]);
				}
			}
			
			camera.startPreview();
	    }
    }
    
	private PreviewCallback previewCallback = new PreviewCallback() {
	  public void onPreviewFrame(byte[] _data, Camera _camera) {
		  if(MyProxyVideoProducer.this.valid){
			  MyProxyVideoProducer.this.videoFrame.put(_data);
			  MyProxyVideoProducer.this.producer.push(MyProxyVideoProducer.this.videoFrame, MyProxyVideoProducer.this.videoFrame.capacity());
			  MyProxyVideoProducer.this.videoFrame.rewind();
			
				if(MyProxyVideoProducer.addCallbackBufferSupported){
					MyCameraProducer.addCallbackBuffer(_camera, _data);
				}
			}
	  	}
	};
    
    /***
     * MyProxyVideoProducerPreview
     * @author Mamadou Diop
     *
     */
	class MyProxyVideoProducerPreview extends SurfaceView implements SurfaceHolder.Callback {
		private SurfaceHolder holder;
		private final MyProxyVideoProducer myProducer;
	
		MyProxyVideoProducerPreview(MyProxyVideoProducer _producer) {
			super(_producer.context);
			
			this.myProducer = _producer;
			this.holder = getHolder();
			this.holder.addCallback(this);
			this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				/*final Camera camera =*/ MyCameraProducer.openCamera(this.myProducer.fps, 
						this.myProducer.width, 
						this.myProducer.height, 
						this.holder,
						this.myProducer.previewCallback
						);
				
			} catch (Exception exception) {
				Log.e(MyProxyVideoProducer.TAG, exception.toString());
			}
		}
	
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(MyProxyVideoProducer.TAG,"Destroy Preview");
			try{
				MyCameraProducer.releaseCamera();
			}
			catch (Exception exception) {
				Log.e(MyProxyVideoProducer.TAG, exception.toString());
			}
		}
	
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			Log.d(MyProxyVideoProducer.TAG,"Surface Changed Callback");
			final Camera camera = MyCameraProducer.getCamera();
			try{
				this.myProducer.startCameraPreview(camera);
			}
			catch (Exception exception) {
				Log.e(MyProxyVideoProducer.TAG, exception.toString());
			}
		}
	}
    
	/**
	 * MyProxyVideoProducerCallback
	 * @author Mamadou Diop
	 *
	 */
	static class MyProxyVideoProducerCallback extends ProxyVideoProducerCallback
    {
        final MyProxyVideoProducer myProducer;
        public MyProxyVideoProducerCallback(MyProxyVideoProducer producer){
        	super();
            this.myProducer = producer;
        }

        @Override
        public int prepare(int width, int height, int fps){
            return myProducer.prepareCallback(width, height, fps);
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
