package org.doubango.ngn.media;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.tinyWRAP.ProxyVideoConsumer;
import org.doubango.tinyWRAP.ProxyVideoConsumerCallback;
import org.doubango.tinyWRAP.ProxyVideoFrame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class NgnProxyVideoConsumer extends NgnProxyPlugin{
	private static final String TAG = NgnProxyVideoConsumer.class.getCanonicalName();
	private static final int DEFAULT_VIDEO_WIDTH = 176;
	private static final int DEFAULT_VIDEO_HEIGHT = 144;
	private static final int DEFAULT_VIDEO_FPS = 15;
	
	private final MyProxyVideoConsumerCallback mCallback;
	private final ProxyVideoConsumer mConsumer;
	private Context mContext;
	private MyProxyVideoConsumerPreview mPreview;
	private int mWidth;
	private int mHeight;
	private int mFps;
	private ByteBuffer mVideoFrame;
	private Bitmap mRGB565Bitmap;
	private boolean mFullScreenRequired;
	private Looper mLooper;
    private Handler mHandler;

    public NgnProxyVideoConsumer(BigInteger id, ProxyVideoConsumer consumer){
    	super(id, consumer);
    	mConsumer = consumer;
        mCallback = new MyProxyVideoConsumerCallback(this);
        mConsumer.setCallback(mCallback);
        
        // Initialize video stream parameters with default values
        mWidth = NgnProxyVideoConsumer.DEFAULT_VIDEO_WIDTH;
		mHeight = NgnProxyVideoConsumer.DEFAULT_VIDEO_HEIGHT;
		mFps = NgnProxyVideoConsumer.DEFAULT_VIDEO_FPS;
    }
    
    public void setContext(Context context){
    	mContext = context;
    }
    
	public final View startPreview(){
		if(mPreview == null && mContext != null){
			if(mLooper != null){
				mLooper.quit();
				mLooper = null;
			}
			
			final Thread previewTread = new Thread() {
				@Override
				public void run() {
					android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
					Looper.prepare();
					mLooper = Looper.myLooper();
					
					synchronized (this) {
						mPreview = new MyProxyVideoConsumerPreview(mContext, mWidth, mHeight, mFps);
						notify();
					}
					
					mHandler = new Handler() {
						public void handleMessage(Message message) {
							final int nCopiedSize = message.arg1;
							final int nAvailableSize = message.arg2;
							if(mVideoFrame == null || mVideoFrame.capacity() != nAvailableSize){
								long newWidth = mConsumer.getDisplayWidth();
								long newHeight = mConsumer.getDisplayHeight();
								if(newWidth<=0 || newHeight<=0){
									Log.e(TAG,"nCopiedSize="+nCopiedSize+" and newWidth="+newWidth+" and newHeight="+newHeight);
									return;
								}
								Log.d(TAG,"resizing the buffer nAvailableSize="+nAvailableSize+" and newWidth="+newWidth+" and newHeight="+newHeight);
								mRGB565Bitmap = Bitmap.createBitmap((int)newWidth, (int)newHeight, Bitmap.Config.RGB_565);
								mVideoFrame = ByteBuffer.allocateDirect((int)nAvailableSize);
								mConsumer.setConsumeBuffer(mVideoFrame, mVideoFrame.capacity());
								return; // Draw the picture next time
							}
							
							drawFrame();
						}
					};
					
					Looper.loop();
					mHandler = null;
					Log.d(TAG, "VideoConsumer::Looper::exit");
				}
			};
			previewTread.setPriority(Thread.MAX_PRIORITY);
			synchronized(previewTread) {
				previewTread.start();
				try {
					previewTread.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return null;
				}
	        }
		}
		else{
			Log.e(TAG, "Invalid state");
		}
		return mPreview;
	}
    
    private int prepareCallback(int width, int height, int fps){
    	Log.d(TAG, "prepareCallback("+width+","+height+","+fps+")");
    	
    	// Update video stream parameters with real values (negotiated)
		mWidth = width;
		mHeight = height;
		mFps = fps;
		
		mFullScreenRequired = NgnEngine.getInstance().getConfigurationService().getBoolean(
				NgnConfigurationEntry.GENERAL_FULL_SCREEN_VIDEO, 
				NgnConfigurationEntry.DEFAULT_GENERAL_FULL_SCREEN_VIDEO);
		mRGB565Bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
		mVideoFrame = ByteBuffer.allocateDirect(mWidth * mHeight * 2);
		
		mConsumer.setConsumeBuffer(mVideoFrame, mVideoFrame.capacity());
		
		super.mPrepared = true;
		return 0;
    }
    
    private int startCallback(){
    	Log.d(NgnProxyVideoConsumer.TAG, "startCallback");
    	super.mStarted = true;
    	return 0;
    }

    private int bufferCopiedCallback(long nCopiedSize, long nAvailableSize) {
    	if(!super.mValid || mRGB565Bitmap == null){
			Log.e(TAG, "Invalid state");
			return -1;
		}
		if(mPreview == null || mPreview.mHolder == null){
			// Not on the top
			return 0;
		}
		
		if(mHandler != null){
			final Message message = mHandler.obtainMessage();
			message.arg1 = (int)nCopiedSize;
			message.arg2 = (int)nAvailableSize;
			mHandler.sendMessage(message);
			
		}
		
		return 0;
    }
    
    private int consumeCallback(ProxyVideoFrame _frame){    	
		if(!super.mValid || mRGB565Bitmap == null){
			Log.e(TAG, "Invalid state");
			return -1;
		}
		if(mPreview == null || mPreview.mHolder == null){
			// Not on the top
			return 0;
		}
		
		// Get video frame content from native code
		_frame.getContent(mVideoFrame, mVideoFrame.capacity());
		mRGB565Bitmap.copyPixelsFromBuffer(mVideoFrame);
		drawFrame();
	    
		return 0;
    }

    private int pauseCallback(){
    	Log.d(TAG, "pauseCallback");
    	super.mPaused = true;
    	return 0;
    }
    
    private int stopCallback(){
    	Log.d(TAG, "stopCallback");
    	super.mStarted = false;
    	if(mLooper != null){
			mLooper.quit();
			mLooper = null;
		}
    	return 0;
    }
	
    private void drawFrame(){
    	final Canvas canvas = mPreview.mHolder.lockCanvas();
		if (canvas != null){
			mRGB565Bitmap.copyPixelsFromBuffer(mVideoFrame);
			if(mFullScreenRequired){
				canvas.drawBitmap(mRGB565Bitmap, null, mPreview.mSurfFrame, null);
			}
			else{
				// display while keeping the ratio
				canvas.drawBitmap(mRGB565Bitmap, null, mPreview.mSurfDisplay, null);
				// Or display "as is"
				//canvas.drawBitmap(mRGB565Bitmap, 0, 0, null);
			}					
			mPreview.mHolder.unlockCanvasAndPost(canvas);
		}
    }
    
	
	/**
	 * MyProxyVideoConsumerCallback
	 */
	static class MyProxyVideoConsumerCallback extends ProxyVideoConsumerCallback
    {
        final NgnProxyVideoConsumer myConsumer;

        public MyProxyVideoConsumerCallback(NgnProxyVideoConsumer consumer){
        	super();
            myConsumer = consumer;
        }
        
        @Override
        public int prepare(int width, int height, int fps){
            return myConsumer.prepareCallback(width, height, fps);
        }
        
        @Override
        public int start(){
            return myConsumer.startCallback();
        }

        @Override
        public int consume(ProxyVideoFrame frame){
            return myConsumer.consumeCallback(frame);
        }        
        
        @Override
		public int bufferCopied(long nCopiedSize, long nAvailableSize) {
			return myConsumer.bufferCopiedCallback(nCopiedSize, nAvailableSize);
		}

		@Override
        public int pause(){
            return myConsumer.pauseCallback();
        }
        
        @Override
        public int stop(){
            return myConsumer.stopCallback();
        }
    }
	
	/**
	 * MyProxyVideoConsumerPreview
	 */
	static class MyProxyVideoConsumerPreview extends SurfaceView implements SurfaceHolder.Callback {
		private final SurfaceHolder mHolder;
		private Rect mSurfFrame;
		private Rect mSurfDisplay;
		private final float mRatio;
		MyProxyVideoConsumerPreview(Context context, int width, int height, int fps) {
			super(context);
			
			mHolder = getHolder();
			mHolder.addCallback(this);
			// You don't need to enable GPU or Hardware acceleration by yourself
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
			mRatio = (float)width/(float)height;
			
			if(mHolder != null){
				mSurfFrame = mHolder.getSurfaceFrame();
			}
			else{
				mSurfFrame = null;
			}
			mSurfDisplay = mSurfFrame;
		}
	
		public void surfaceCreated(SurfaceHolder holder) {
		}
	
		public void surfaceDestroyed(SurfaceHolder holder) {
		}
	
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			if(holder != null){
				mSurfFrame = holder.getSurfaceFrame();
				
				// (w/h)=ratio => 
				// 1) h=w/ratio 
				// and 
				// 2) w=h*ratio
				int newW = (int)(w/mRatio) > h ? (int)(h * mRatio) : w;
				int newH = (int)(newW/mRatio) > h ? h : (int)(newW/mRatio);
				
				mSurfDisplay = new Rect(0, 0, newW, newH);
			}
		}
	}
}
