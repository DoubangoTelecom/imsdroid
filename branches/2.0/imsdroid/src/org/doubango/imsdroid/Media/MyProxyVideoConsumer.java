package org.doubango.imsdroid.Media;

import java.math.BigInteger;
import java.nio.ByteBuffer;


import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Utils.ConfigurationUtils;
import org.doubango.imsdroid.Utils.ConfigurationUtils.ConfigurationEntry;
import org.doubango.tinyWRAP.ProxyVideoConsumer;
import org.doubango.tinyWRAP.ProxyVideoConsumerCallback;
import org.doubango.tinyWRAP.ProxyVideoFrame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * MyProxyVideoConsumer
 */
public class MyProxyVideoConsumer extends MyProxyPlugin{
	private static final String TAG = MyProxyVideoConsumer.class.getCanonicalName();
	private static final int DEFAULT_VIDEO_WIDTH = 176;
	private static final int DEFAULT_VIDEO_HEIGHT = 144;
	private static final int DEFAULT_VIDEO_FPS = 15;
	
	private final MyProxyVideoConsumerCallback mCallback;
	private Context mContext;
	private MyProxyVideoConsumerPreview mPreview;
	private int mWidth;
	private int mHeight;
	private int mFps;
	private ByteBuffer mVideoFrame;
	private Bitmap mRGB565Bitmap;
	private boolean mFullScreenRequired;

    public MyProxyVideoConsumer(BigInteger id, ProxyVideoConsumer consumer){
    	super(id, consumer);
        mCallback = new MyProxyVideoConsumerCallback(this);
        consumer.setCallback(mCallback);
        
        // Initialize video stream parameters with default values
        mWidth = MyProxyVideoConsumer.DEFAULT_VIDEO_WIDTH;
		mHeight = MyProxyVideoConsumer.DEFAULT_VIDEO_HEIGHT;
		mFps = MyProxyVideoConsumer.DEFAULT_VIDEO_FPS;
    }
    
    public void setContext(Context context){
    	mContext = context;
    }
    
    // Very important: Must be done from the UI thread
	public final View startPreview(){
		if(mPreview == null && mContext != null){
			mPreview = new MyProxyVideoConsumerPreview(mContext, mWidth, mHeight, mFps);
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
		
		mFullScreenRequired = ServiceManager.getConfigurationService().getBoolean(
				ConfigurationEntry.GENERAL_FULL_SCREEN_VIDEO, 
				ConfigurationUtils.DEFAULT_GENERAL_FULL_SCREEN_VIDEO);
		mRGB565Bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
		mVideoFrame = ByteBuffer.allocateDirect(mWidth * mHeight * 2);
		super.mPrepared = true;
		return 0;
    }
    
    private int startCallback(){
    	Log.d(MyProxyVideoConsumer.TAG, "startCallback");
    	super.mStarted = true;
    	return 0;
    }

    private int consumeCallback(ProxyVideoFrame _frame){    	
		if(!super.mValid || mRGB565Bitmap == null){
			Log.e(TAG, "Invalid state");
			return -1;
		}
		if(mPreview == null || mPreview.holder == null){
			// Not on the top
			return 0;
		}
		
		// Get video frame content from native code
		_frame.getContent(mVideoFrame, mVideoFrame.capacity());
		mRGB565Bitmap.copyPixelsFromBuffer(mVideoFrame);
		
		// Get canvas for drawing
		final Canvas canvas = mPreview.holder.lockCanvas();
		if (canvas != null){
			if(mFullScreenRequired){
				//canvas.drawBitmap(this.rgb565Bitmap, null, this.preview.surfDisplay, null);
				canvas.drawBitmap(mRGB565Bitmap, null, mPreview.surfFrame, null);
			}
			else{
				canvas.drawBitmap(mRGB565Bitmap, 0, 0, null);
			}					
			mPreview.holder.unlockCanvasAndPost(canvas);
		}
		else{
			Log.d(TAG, "Invalid canvas");
		}
		
		mVideoFrame.rewind();
	    
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
    	return 0;
    }
	
	
	/**
	 * MyProxyVideoConsumerCallback
	 */
	static class MyProxyVideoConsumerCallback extends ProxyVideoConsumerCallback
    {
        final MyProxyVideoConsumer myConsumer;

        public MyProxyVideoConsumerCallback(MyProxyVideoConsumer consumer){
        	super();
            this.myConsumer = consumer;
        }
        
        @Override
        public int prepare(int width, int height, int fps){
            return this.myConsumer.prepareCallback(width, height, fps);
        }
        
        @Override
        public int start(){
            return this.myConsumer.startCallback();
        }

        @Override
        public int consume(ProxyVideoFrame frame){
            return this.myConsumer.consumeCallback(frame);
        }

        @Override
        public int pause(){
            return this.myConsumer.pauseCallback();
        }
        
        @Override
        public int stop(){
            return this.myConsumer.stopCallback();
        }
    }
	
	/**
	 * MyProxyVideoConsumerPreview
	 */
	static class MyProxyVideoConsumerPreview extends SurfaceView implements SurfaceHolder.Callback {
		private final SurfaceHolder holder;
		private Rect surfFrame;
		@SuppressWarnings("unused")
		private Rect surfDisplay;
		private final float ratio;
		MyProxyVideoConsumerPreview(Context context, int width, int height, int fps) {
			super(context);
			
			this.holder = getHolder();
			this.holder.addCallback(this);
			this.ratio = (float)width/(float)height;
			
			if(this.holder != null){
				this.surfFrame = this.holder.getSurfaceFrame();
			}
			else{
				this.surfFrame = null;
			}
			this.surfDisplay = this.surfFrame;
		}
	
		public void surfaceCreated(SurfaceHolder holder) {
		}
	
		public void surfaceDestroyed(SurfaceHolder holder) {
		}
	
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			if(holder != null){
				this.surfFrame = holder.getSurfaceFrame();
				
				// (w/h)=ratio => 
				// 1) h=w/ratio 
				// and 
				// 2) w=h*ratio
				int newW = (int)(w/ratio) > h ? (int)(h * ratio) : w;
				int newH = (int)(newW/ratio) > h ? h : (int)(newW/ratio);
				
				this.surfDisplay = new Rect(0, 0, newW, newH);
			}
		}
	}
}
