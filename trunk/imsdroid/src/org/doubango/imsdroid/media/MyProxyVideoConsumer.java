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

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
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
 * @author Mamadou Diop
 *
 */
public class MyProxyVideoConsumer extends MyProxyPlugin{
	private static final String TAG = MyProxyVideoConsumer.class.getCanonicalName();
	private static final int DEFAULT_VIDEO_WIDTH = 176;
	private static final int DEFAULT_VIDEO_HEIGHT = 144;
	private static final int DEFAULT_VIDEO_FPS = 15;
	
	private final MyProxyVideoConsumerCallback callback;
	private Context context;
	private MyProxyVideoConsumerPreview preview;
	private int width;
	private int height;
	private int fps;
	private ByteBuffer videoFrame;
	private Bitmap rgb565Bitmap;
	private boolean isFullScreenRequired;

    public MyProxyVideoConsumer(BigInteger id, ProxyVideoConsumer consumer){
    	super(id, consumer);
        this.callback = new MyProxyVideoConsumerCallback(this);
        consumer.setCallback(this.callback);
        
        // Initialize video stream parameters with default values
        this.width = MyProxyVideoConsumer.DEFAULT_VIDEO_WIDTH;
		this.height = MyProxyVideoConsumer.DEFAULT_VIDEO_HEIGHT;
		this.fps = MyProxyVideoConsumer.DEFAULT_VIDEO_FPS;
    }
    
    public void setContext(Context context){
    	this.context = context;
    }
    
    // Very important: Must be done from the UI thread
	public final View startPreview(){
		if(this.preview == null && this.context != null){
			this.preview = new MyProxyVideoConsumerPreview(
					this.context,
					this.width,
					this.height, this.fps);
		}
		else{
			Log.e(MyProxyVideoConsumer.TAG, "Invalid state");
		}
		return this.preview;
	}
    
    private int prepareCallback(int width, int height, int fps){
    	Log.d(MyProxyVideoConsumer.TAG, "prepareCallback("+width+","+height+","+fps+")");
    	
    	// Update video stream parameters with real values (negotiated)
		this.width = width;
		this.height = height;
		this.fps = fps;
		
		this.isFullScreenRequired = ServiceManager.getConfigurationService().getBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.FULL_SCREEN_VIDEO, Configuration.DEFAULT_GENERAL_FULL_SCREEN_VIDEO);
		this.rgb565Bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.RGB_565);
		this.videoFrame = ByteBuffer.allocateDirect(this.width * this.height * 2);
		return 0;
    }
    
    private int startCallback(){
    	Log.d(MyProxyVideoConsumer.TAG, "startCallback");
    	this.started = true;
    	return 0;
    }

    private int consumeCallback(ProxyVideoFrame _frame){    	
		if(!this.valid || this.rgb565Bitmap == null){
			Log.e(MyProxyVideoConsumer.TAG, "Invalid state");
			return -1;
		}
		if(this.preview == null || this.preview.holder == null){
			// Not on the top
			return 0;
		}
		
		// Get video frame content from native code
		_frame.getContent(this.videoFrame, this.videoFrame.capacity());
		this.rgb565Bitmap.copyPixelsFromBuffer(this.videoFrame);
		
		// Get canvas for drawing
		final Canvas canvas = this.preview.holder.lockCanvas();
		if (canvas != null){
			if(this.isFullScreenRequired){
				//canvas.drawBitmap(this.rgb565Bitmap, null, this.preview.surfDisplay, null);
				canvas.drawBitmap(this.rgb565Bitmap, null, this.preview.surfFrame, null);
			}
			else{
				canvas.drawBitmap(this.rgb565Bitmap, 0, 0, null);
			}					
			this.preview.holder.unlockCanvasAndPost(canvas);
		}
		else{
			Log.d(MyProxyVideoConsumer.TAG, "Invalid canvas");
		}
		
		this.videoFrame.rewind();
	    
		return 0;
    }

    private int pauseCallback(){
    	Log.d(MyProxyVideoConsumer.TAG, "pauseCallback");
    	this.paused = true;
    	return 0;
    }
    
    private int stopCallback(){
    	Log.d(MyProxyVideoConsumer.TAG, "stopCallback");
    	this.started = false;
    	return 0;
    }
	
	
	/**
	 * MyProxyVideoConsumerCallback
	 * @author Mamadou Diop
	 *
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
	 * @author Mamadou Diop
	 *
	 */
	static class MyProxyVideoConsumerPreview extends SurfaceView implements SurfaceHolder.Callback {
		private final SurfaceHolder holder;
		private Rect surfFrame;
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
