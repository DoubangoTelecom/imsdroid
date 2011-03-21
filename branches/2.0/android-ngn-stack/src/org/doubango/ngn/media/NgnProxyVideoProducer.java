package org.doubango.ngn.media;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.doubango.ngn.NgnApplication;
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
	private int mWidth;
	private int mHeight;
	private int mFps;
	private ByteBuffer mVideoFrame;
	
	public NgnProxyVideoProducer(BigInteger id, ProxyVideoProducer producer){
		super(id, producer);
        mCallback = new MyProxyVideoProducerCallback(this);
        mProducer = producer;
        mProducer.setCallback(mCallback);
        
     	// Initialize video stream parameters with default values
        mWidth = NgnProxyVideoProducer.DEFAULT_VIDEO_WIDTH;
		mHeight = NgnProxyVideoProducer.DEFAULT_VIDEO_HEIGHT;
		mFps = NgnProxyVideoProducer.DEFAULT_VIDEO_FPS;
    }
	
	public void setContext(Context context){
    	mContext = context;
    }
    
	// Very important: Must be done in the UI thread
	public final View startPreview(){
		if(mPreview == null){
			mPreview = new MyProxyVideoProducerPreview(this);
		}
		
		mPreview.setVisibility(View.VISIBLE);
		mPreview.getHolder().setSizeFromLayout();
		mPreview.bringToFront();
		
		return mPreview;
	}
	
	public void pushBlankPacket(){
		if(super.mValid && mProducer != null && mVideoFrame != null){
			final ByteBuffer buffer = ByteBuffer.allocateDirect(mVideoFrame .capacity());
			mProducer.push(buffer, buffer.capacity());
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
	
	public void setRotation(int rot){
		if(mProducer != null && super.mValid){
			mProducer.setRotation(rot);
		}
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
	
	private int prepareCallback(int width, int height, int fps){
		Log.d(NgnProxyVideoProducer.TAG, "prepareCallback("+width+","+height+","+fps+")");
		
		mWidth = width;
		mHeight = height;
		mFps = fps;
		
		final float capacity = (float)(width*height)*1.5f/* (3/2) */;
		mVideoFrame = ByteBuffer.allocateDirect((int)capacity);
		super.mPrepared = true;
		
		return 0;
    }

    private int startCallback(){
    	Log.d(TAG, "startCallback");
		this.mStarted = true;
		return 0;
    }

    private int pauseCallback(){
    	Log.d(TAG, "pauseCallback");
    	setOnPause(true);
    	return 0;
    }

    private int stopCallback(){
    	Log.d(TAG, "stopCallback");
    	
		mPreview = null;
		mContext = null;
		super.mStarted = false;
		
		return 0;
    }
	
    private void startCameraPreview(Camera camera){
		if(camera != null && mProducer != null && mVideoFrame != null){
			try{
				Camera.Parameters parameters = camera.getParameters();
				parameters.setPreviewSize(mWidth, mHeight);
				camera.setParameters(parameters);
			} catch(Exception e){
				Log.e(TAG, e.toString());
			}
								
			android.content.res.Configuration conf = NgnApplication.getContext().getResources().getConfiguration();
			// Camera Orientation
			switch(conf.orientation){
				case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
					NgnCameraProducer.setDisplayOrientation(camera, 0);
					Log.d(TAG, "Orientation=landscape");
					break;
				case android.content.res.Configuration.ORIENTATION_PORTRAIT:
					NgnCameraProducer.setDisplayOrientation(camera, 90);
					Log.d(TAG, "Orientation=portrait");
					break;
			}
			
			// Callback Buffers
			if(NgnProxyVideoProducer.sAddCallbackBufferSupported){
				for(int i=0; i<NgnProxyVideoProducer.CALLABACK_BUFFERS_COUNT; i++){
					NgnCameraProducer.addCallbackBuffer(camera, new byte[mVideoFrame.capacity()]);
				}
			}
			
			camera.startPreview();
	    }
    }
    
	private PreviewCallback previewCallback = new PreviewCallback() {
	  public void onPreviewFrame(byte[] _data, Camera _camera) {
		  if(NgnProxyVideoProducer.super.mValid){
			  mVideoFrame.put(_data);
			  mProducer.push(mVideoFrame, mVideoFrame.capacity());
			  mVideoFrame.rewind();
			
				if(NgnProxyVideoProducer.sAddCallbackBufferSupported){
					NgnCameraProducer.addCallbackBuffer(_camera, _data);
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
	
		MyProxyVideoProducerPreview(NgnProxyVideoProducer _producer) {
			super(_producer.mContext);
			
			myProducer = _producer;
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				/*final Camera camera =*/ NgnCameraProducer.openCamera(myProducer.mFps, 
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
			Log.d(TAG,"Destroy Preview");
			try{
				NgnCameraProducer.releaseCamera();
			}
			catch (Exception exception) {
				Log.e(TAG, exception.toString());
			}
		}
	
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			Log.d(TAG,"Surface Changed Callback");
			final Camera camera = NgnCameraProducer.getCamera();
			try{
				this.myProducer.startCameraPreview(camera);
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
