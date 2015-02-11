/* Copyright (C) 2010-2015, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*  Copyright (C) 2011, Philippe Verney <verney(dot)philippe(AT)gmail(dot)com>
*  Copyright (C) 2011, Tiscali
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
*/
package org.doubango.ngn.media;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.doubango.ngn.NgnApplication;
import org.doubango.tinyWRAP.ProxyVideoProducer;
import org.doubango.tinyWRAP.ProxyVideoProducerCallback;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;


/**
 * MyProxyVideoProducer
 */
public class NgnProxyVideoProducer extends NgnProxyPlugin{
	private static final String TAG = NgnProxyVideoProducer.class.getCanonicalName();
	private static final int DEFAULT_VIDEO_WIDTH = 176;
	private static final int DEFAULT_VIDEO_HEIGHT = 144;
	private static final int DEFAULT_VIDEO_FPS = 15;
	private static final int CALLABACK_BUFFERS_COUNT = 3;
	private static final boolean sAddCallbackBufferSupported = NgnCameraProducer.isAddCallbackBufferSupported();
	
	private final ProxyVideoProducer mProducer;
	private final MyProxyVideoProducerCallback mCallback;
	private Context mContext;
	private MyProxyVideoProducerPreview mPreview;
	private int mWidth; // negotiated width
	private int mHeight; // negotiated height
	private int mFps;
	private int mFrameWidth; // camera picture output width
	private int mFrameHeight; // camera picture output height
	private final boolean mCheckFps; // make sure we're sending what we negotiated
	private long mFrameDuration;
	private long mNextFrameTime;
	
	private ByteBuffer mVideoFrame;
	private byte[] mVideoCallbackData;
	
	private Thread mProducerPushThread;
	private final Lock mLock;
	private final Condition mConditionPushBuffer;
	private final boolean mAsyncPush;
	
	public NgnProxyVideoProducer(BigInteger id, ProxyVideoProducer producer){
		super(id, producer);
        mCallback = new MyProxyVideoProducerCallback(this);
        mProducer = producer;
        mProducer.setCallback(mCallback);
        
     	// Initialize video stream parameters with default values
        mFrameWidth = mWidth = NgnProxyVideoProducer.DEFAULT_VIDEO_WIDTH;
        mFrameHeight = mHeight = NgnProxyVideoProducer.DEFAULT_VIDEO_HEIGHT;
		mFps = NgnProxyVideoProducer.DEFAULT_VIDEO_FPS;
		
		mCheckFps = NgnApplication.isHovis();
		mFrameDuration = 1000/mFps;
		mNextFrameTime = 0;
		
		mAsyncPush = NgnApplication.isHovis();
		mLock = mAsyncPush ? new ReentrantLock() : null;
		mConditionPushBuffer = (mLock != null) ? mLock.newCondition() : null;
    }
	
	@Override
	public void finalize(){
		
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		
		mVideoFrame = null;
		System.gc();
	}
	
	public void setContext(Context context){
    	mContext = context;
    }
    
	// Very important: Must be done in the UI thread
	public final View startPreview(Context context){
		mContext = context == null ? mContext : context;
		if(mPreview == null && mContext != null){
			mPreview = new MyProxyVideoProducerPreview(this);
		}
		if(mPreview != null){
			mPreview.setVisibility(View.VISIBLE);
			mPreview.getHolder().setSizeFromLayout();
			mPreview.bringToFront();
		}
		
		return mPreview;
	}
	
	public final View startPreview(){
		return startPreview(null);
	}
	
	public void pushBlankPacket(){
		if(super.mValid && mProducer != null){
			if(mVideoFrame == null){
				mVideoFrame = ByteBuffer.allocateDirect((mWidth * mHeight * 3) >> 1);
			}
			//final ByteBuffer buffer = ByteBuffer.allocateDirect(mVideoFrame.capacity());
			//mProducer.push(buffer, buffer.capacity());
			mProducer.push(mVideoFrame, mVideoFrame.capacity());
		}
	}
	
	public void toggleCamera(){
		if(super.mValid && super.mStarted && !super.mPaused && mProducer != null){
			final Camera camera = NgnCameraProducer.toggleCamera();
			try{
				startCameraPreview(camera);
			}
			catch (Exception exception) {
				Log.e(TAG, exception.toString());
			}
		}
	}
	
	public int getTerminalRotation(){
		final android.content.res.Configuration conf = NgnApplication.getContext().getResources().getConfiguration();
		int     terminalRotation  = 0 ;
		switch(conf.orientation){
			case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
				terminalRotation = 0;//The starting position is 0 (landscape).
				break;
			case android.content.res.Configuration.ORIENTATION_PORTRAIT:
				terminalRotation = 90 ;
				break;
		}
		return terminalRotation;
	}

	public int getNativeCameraHardRotation(boolean preview){
		// only for 2.3 and above
		if(NgnApplication.getSDKVersion() >= 9){			
			try {
				
				int orientation = 0;
				int cameraId = 0;
				int numOfCameras = NgnCameraProducer.getNumberOfCameras();
				if (numOfCameras > 1) {
					if (NgnCameraProducer.isFrontFacingCameraEnabled()) {
						cameraId = numOfCameras-1;
					}
				}
				
				Class<?> clsCameraInfo = null;

				final Class<?>[] classes = android.hardware.Camera.class.getDeclaredClasses();
				for (Class<?> c : classes) {
					if (c.getSimpleName().equals("CameraInfo")) {
						clsCameraInfo = c;
						break;
					}
				}
				
				final Object info = clsCameraInfo.getConstructor((Class[]) null).newInstance((Object[]) null);
				Method getCamInfoMthd = android.hardware.Camera.class.getDeclaredMethod("getCameraInfo", int.class, clsCameraInfo);
				getCamInfoMthd.invoke(null, cameraId, info);
				
				Display display = NgnApplication.getDefaultDisplay();
				if (display != null) {
					orientation = display.getOrientation();
				}
				orientation = (orientation + 45) / 90 * 90;     
				int rotation = 0;

				final Field fieldFacing = clsCameraInfo.getField("facing");
				final Field fieldOrient = clsCameraInfo.getField("orientation");
				final Field fieldFrontFacingConst = clsCameraInfo.getField("CAMERA_FACING_FRONT");
								
				if (fieldFacing.getInt(info) == fieldFrontFacingConst.getInt(info)) {
					rotation = (fieldOrient.getInt(info) - orientation + 360) % 360;     					
				}
				else {
					// back-facing camera         
					rotation = (fieldOrient.getInt(info) + orientation) % 360;
				}
				
				return rotation;
			} 
			catch (Exception e) {
				e.printStackTrace();
				return 0;
			} 
		}
		else {
			int     terminalRotation   = getTerminalRotation();
			boolean isFront            = NgnCameraProducer.isFrontFacingCameraEnabled();
			if (NgnApplication.isSamsung() && !NgnApplication.isSamsungGalaxyMini()){
				if (preview){
					if (isFront){
						if (terminalRotation == 0) return 0;
						else return 90;
					}
					else return 0 ;
				}
				else{
					if (isFront){
						if (terminalRotation == 0) return -270;
						else return 90;
					}
					else{
						if (terminalRotation == 0) return 0;
						else return 0;
					}
				}
			}
			else if (NgnApplication.isToshiba()){
				if (preview){
					if (terminalRotation == 0) return 0;
					else return 270;
				}
				else{
					return 0;
				}
			}
			else{
				return 0;
			}
		}
	}

	public int compensCamRotation(boolean preview){

		final int cameraHardRotation = getNativeCameraHardRotation(preview);
		final android.content.res.Configuration conf = NgnApplication.getContext().getResources().getConfiguration();
		if(conf.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE){
			return 0;
		}

		if (NgnApplication.getSDKVersion() >= 9) {
			if (preview) {
				return cameraHardRotation;
			}
			else{
				switch (cameraHardRotation) {
					case 0:
					case 180:
					default:
						return 0;
					case 90:
					case 270:
						return 90;
				}
			}			
		}
		else {
			int     terminalRotation   = getTerminalRotation();
			int rotation = 0;
			rotation = (terminalRotation-cameraHardRotation) % 360;
			return rotation;
		}
	}

	public boolean isFrontFacingCameraEnabled() {
		return NgnCameraProducer.isFrontFacingCameraEnabled();
	}

	public boolean setRotation(int rot){
		if(mProducer != null && super.mValid){
			return mProducer.setRotation(rot);
		}
		return false;
	}
	
	public boolean setMirror(boolean mirror){
		if(mProducer != null && super.mValid){
			return mProducer.setMirror(mirror);
		}
		return false;
	}
	
	public void setOnPause(boolean pause){
		if(super.mPaused == pause){
			return;
		}
		try {
			if(super.mStarted){
				final Camera camera = NgnCameraProducer.getCamera();
				if(pause){
					camera.stopPreview();
				}
				else{
					camera.startPreview();
				}
			}
		} catch(Exception e){
			Log.e(TAG, e.toString());
		}
		
		super.mPaused = pause;
	}
	
	private synchronized int prepareCallback(int width, int height, int fps){
		Log.d(NgnProxyVideoProducer.TAG, "prepareCallback("+width+","+height+","+fps+")");
		
		mFrameWidth = mWidth = width;
		mFrameHeight = mHeight = height;
		mFps = fps;
		
		mFrameDuration = 100/mFps;
		mNextFrameTime = 0;
		
		super.mPrepared = true;
		
		return 0;
    }
	
	private Runnable mRunnablePush = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "===== Video Producer AsynThread (Start) ===== ");
			
			while (mValid && mStarted) {
				try {
					synchronized(mConditionPushBuffer) {
						mConditionPushBuffer.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
				if (!mValid || !mStarted) {
					break;
				}
				mLock.lock();
				mProducer.push(mVideoFrame, mVideoFrame.capacity());
				mLock.unlock();
			}
			
			Log.d(TAG, "===== Video Producer AsyncThread (Stop) ===== ");
		}
	};

    private synchronized int startCallback(){
    	Log.d(TAG, "startCallback");
		mStarted = true;
		
		if (mAsyncPush)
		{
			mProducerPushThread = new Thread(mRunnablePush,
				"VideoProducerPushThread");
			// FIXME
			//mProducerPushThread.setPriority(Thread.MAX_PRIORITY);
			mProducerPushThread.start();
		}
		
		if (mPreview != null) {
			startCameraPreview(mPreview.getCamera());
    	}
		return 0;
    }

    private synchronized int pauseCallback(){
    	Log.d(TAG, "pauseCallback");
    	setOnPause(true);
    	return 0;
    }

    private synchronized int stopCallback(){
    	Log.d(TAG, "stopCallback");
    	
    	if (mPreview != null) {
    		stopCameraPreview(mPreview.getCamera());
    	}
    	
		mStarted = false;
		
		if (mConditionPushBuffer != null) {
			synchronized(mConditionPushBuffer) {
				mConditionPushBuffer.notifyAll(); // must be after "mStarted=false" to break endless loop
			}
		}
		
		if (mProducerPushThread != null) {
			try {
				synchronized(mProducerPushThread) {
					mProducerPushThread.join();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mProducerPushThread = null;
		}
		
		return 0;
    }
	    
    private Size getCameraBestPreviewSize(Camera camera){
    	final List<Size> prevSizes = camera.getParameters().getSupportedPreviewSizes();
    	
    	Size minSize = null;
    	int minScore = Integer.MAX_VALUE;
    	for(Size size : prevSizes){
    		final int score = Math.abs(size.width - mWidth) + Math.abs(size.height - mHeight);
    		if(minScore > score){
    			minScore = score;
    			minSize = size;
    		}
    	}
    	return minSize;
    }
    
    private synchronized void startCameraPreview(Camera camera){
    	if(!mStarted){
    		Log.w(TAG, "Someone requested to start camera preview but producer not ready ...delaying");
    		return;
    	}
		if(camera != null && mProducer != null){
			try{				
				Camera.Parameters parameters = camera.getParameters();
				final Size prevSize = getCameraBestPreviewSize(camera);
				parameters.setPreviewSize(prevSize.width, prevSize.height);
				camera.setParameters(parameters);
				
				if(prevSize != null && super.isValid() && (mWidth != prevSize.width || mHeight != prevSize.height)){
					mFrameWidth = prevSize.width;
					mFrameHeight = prevSize.height;
				}
				
				// alert the framework that we cannot respect the negotiated size
				mProducer.setActualCameraOutputSize(mFrameWidth, mFrameHeight);
				
				// allocate buffer
				Log.d(TAG, String.format("setPreviewSize [%d x %d ]", mFrameWidth, mFrameHeight));
				mVideoFrame = ByteBuffer.allocateDirect((mFrameWidth * mFrameHeight * 3) >> 1);				
			} catch(Exception e){
				Log.e(TAG, e.toString());
			}
								
			try {
				int terminalRotation = getTerminalRotation();
								
				Camera.Parameters parameters = camera.getParameters();
				
				if (terminalRotation == 0) {
					parameters.set("orientation", "landscape");
				} else {
					parameters.set("orientation", "portrait");
				}

				camera.setParameters(parameters);
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
			
			// Camera Orientation
			int rotation = compensCamRotation(false);
			Log.d(TAG, String.format("setDisplayOrientation [%d] ",rotation ));
			NgnCameraProducer.setDisplayOrientation(camera, rotation);
			
			// Callback Buffers
			if(NgnProxyVideoProducer.sAddCallbackBufferSupported){
				for(int i=0; i<NgnProxyVideoProducer.CALLABACK_BUFFERS_COUNT; i++){
					if(i == 0 || (mVideoCallbackData == null)){
						mVideoCallbackData = new byte[mVideoFrame.capacity()];
					}
					NgnCameraProducer.addCallbackBuffer(camera, new byte[mVideoFrame.capacity()]);
				}
			}
			
			try{
    			camera.startPreview();
    		}catch (Exception e) {
				Log.e(TAG, e.toString());
			}
	    }
    }
    
    private synchronized void stopCameraPreview(Camera camera){
    	if(camera != null){
    		try{
    			camera.stopPreview();
    		}catch (Exception e) {
				Log.e(TAG, e.toString());
			}
    	}
    }
    
	private PreviewCallback previewCallback = new PreviewCallback() {
	  public void onPreviewFrame(byte[] _data, Camera _camera) {
		  if(mStarted){
			  if(NgnProxyVideoProducer.super.mValid && mVideoFrame != null && _data != null){
				  boolean pushFrame = true;
				  if (mCheckFps)
				  {
					  long now = System.currentTimeMillis();
					  pushFrame = (mNextFrameTime == 0 || (now - mNextFrameTime) >= mFrameDuration);
					  mNextFrameTime = now + mFrameDuration;
				  }
				  if (pushFrame)
				  {
					  if (mAsyncPush)
					  {
						  	mLock.lock();
							mVideoFrame.rewind();
							mVideoFrame.put(_data);
						  	mLock.unlock();
						  	synchronized(mConditionPushBuffer){
						  		mConditionPushBuffer.notify();
						  	}
					  }
					  else
					  {
						  mVideoFrame.put(_data);
						  mProducer.push(mVideoFrame, mVideoFrame.capacity());
						  mVideoFrame.rewind();
					  }
				  }
				}
			  if(NgnProxyVideoProducer.sAddCallbackBufferSupported){
				  // do not use "_data" which could be null (e.g. on GSII)
				  NgnCameraProducer.addCallbackBuffer(_camera, mVideoCallbackData);
			  }
		 }
	 }
	};
    
    /***
     * MyProxyVideoProducerPreview
     */
	class MyProxyVideoProducerPreview extends SurfaceView implements SurfaceHolder.Callback {
		private SurfaceHolder mHolder;
		private final NgnProxyVideoProducer myProducer;
		private Camera mCamera;
	
		MyProxyVideoProducerPreview(NgnProxyVideoProducer _producer) {
			super(_producer.mContext);
			
			myProducer = _producer;
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		
		public Camera getCamera(){
			return mCamera;
		}
	
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG,"surfaceCreated()");
			try {
				mCamera = NgnCameraProducer.openCamera(myProducer.mFps, 
						myProducer.mWidth, 
						myProducer.mHeight, 
						mHolder,
						myProducer.previewCallback
						);
				
			} catch (Exception exception) {
				Log.e(TAG, exception.toString());
			}
		}
	
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG,"surfaceDestroyed()");
			try{
				NgnCameraProducer.releaseCamera(mCamera);
			}
			catch (Exception exception) {
				Log.e(TAG, exception.toString());
			}
		}
	
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			Log.d(TAG,"Surface Changed Callback");
			try{
				if(mCamera != null){
					myProducer.startCameraPreview(mCamera);
				}
			}
			catch (Exception exception) {
				Log.e(TAG, exception.toString());
			}
		}
	}
    
	/**
	 * MyProxyVideoProducerCallback
	 */
	static class MyProxyVideoProducerCallback extends ProxyVideoProducerCallback
    {
        final NgnProxyVideoProducer myProducer;
        public MyProxyVideoProducerCallback(NgnProxyVideoProducer producer){
        	super();
            myProducer = producer;
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
