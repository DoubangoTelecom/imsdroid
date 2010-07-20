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

import java.nio.ByteBuffer;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.tinyWRAP.ProxyVideoConsumer;
import org.doubango.tinyWRAP.ProxyVideoFrame;
import org.doubango.tinyWRAP.tmedia_chroma_t;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class VideoConsumer {

	private static String TAG = VideoConsumer.class.getCanonicalName();
	
	private static int WIDTH = 176;
	private static int HEIGHT = 144;
	private static int FPS = 15;
	//private static int MAX_DELAY = 2;
	
	private Context context;
	private int width;
	private int height;
	private int fps;
	private ByteBuffer frame;
	private Bitmap bitmap;
	private Preview preview;
	private boolean fullScreen;
	private boolean fullScreenHonored;
	//private boolean running;
	
	private final MyProxyVideoConsumer videoConsumer;
	//private final ArrayList<ByteBuffer> buffers;
	//private final Semaphore semaphore;
	
	public VideoConsumer(){
		this.videoConsumer = new MyProxyVideoConsumer(this);
		//this.buffers = new ArrayList<ByteBuffer>();
		//this.semaphore = new Semaphore(0);
		
		this.width = VideoConsumer.WIDTH;
		this.height = VideoConsumer.HEIGHT;
		this.fps = VideoConsumer.FPS;
	}
	
	public void setActive(){
		this.videoConsumer.setActivate(true);
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
					this.width,
					this.height, this.fps);
		}
		return this.preview;
	}
	
	public synchronized int pause() {
		Log.d(VideoConsumer.TAG, "pause()");
		return 0;
	}
	
	public synchronized int prepare(int width, int height, int fps) {
		Log.d(VideoConsumer.TAG, "prepare()");
		this.width = width;
		this.height = height;
		this.fps = fps;
		
		this.fullScreen = ServiceManager.getConfigurationService().getBoolean(CONFIGURATION_SECTION.GENERAL, CONFIGURATION_ENTRY.FULL_SCREEN_VIDEO, Configuration.DEFAULT_GENERAL_FULL_SCREEN_VIDEO);
		this.bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.RGB_565);
		this.frame = ByteBuffer.allocateDirect(this.width * this.height * 2);
		return 0;
	}
	
	public synchronized int start() {
		Log.d(VideoConsumer.TAG, "start()");
		if(this.context != null){
			//this.running = true;
			//new Thread(this.runnableDrawer).start();
			return 0;
		}
		else{
			Log.e(VideoConsumer.TAG, "Invalid context");
			return -1;
		}
	}
	
	public int consume(ProxyVideoFrame _frame){
		
		/*if(VideoConsumer.this.buffers.size()< (VideoConsumer.this.fps * VideoConsumer.MAX_DELAY)){
			_frame.getContent(this.frame, this.frame.capacity());
			
			//synchronized(VideoConsumer.this.buffers){
				final ByteBuffer buffer = ByteBuffer.allocate(this.frame.capacity());
				buffer.put(this.frame);
				buffer.flip();
				VideoConsumer.this.buffers.add(buffer);
			//}
			VideoConsumer.this.semaphore.release();
			this.frame.rewind();
		}
		else{
			Log.d(VideoConsumer.TAG, "Too many frames");
			//synchronized(VideoConsumer.this.buffers){
				//VideoConsumer.this.buffers.clear();
			//}
		}*/
		
		if(this.preview == null || this.preview.holder == null || this.bitmap == null){
			Log.e(VideoConsumer.TAG, "Invalid state");
			return -1;
		}
		
		_frame.getContent(this.frame, this.frame.capacity());
		this.bitmap.copyPixelsFromBuffer(this.frame);
				
		final Canvas canvas = this.preview.holder.lockCanvas();
		if (canvas != null){		
			if(this.fullScreen){
				canvas.drawBitmap(this.bitmap, null, this.preview.rect, null);
			}
			else{
				canvas.drawBitmap(this.bitmap, 0, 0, null);
			}
			
			/*canvas.drawBitmap(this.bitmap, 0, 0, null);
			if(this.fullScreen && !this.fullScreenHonored){
				Log.e(VideoConsumer.TAG, "Honoring Full-Screen");
				this.fullScreenHonored = this.videoConsumer.setDisplaySize(this.preview.rect.width(), this.preview.rect.height());
				this.frame = ByteBuffer.allocateDirect(this.preview.rect.width() * this.preview.rect.height() * 2);
				this.bitmap = Bitmap.createBitmap(this.preview.rect.width(), this.preview.rect.height(), Bitmap.Config.RGB_565);
			}*/
			
			this.preview.holder.unlockCanvasAndPost(canvas);
		}
		else{
			Log.e(VideoConsumer.TAG, "Invalid canvas");
		}
		
		
		this.frame.rewind();
		
        
		return 0;
	}
	
	public synchronized int stop() {
		Log.d(VideoConsumer.TAG, "stop()");
		
		this.preview = null;
		this.context = null;
		
		//this.running = false;
		//this.semaphore.release();
		
		return 0;
	}
	
//	private Runnable runnableDrawer = new Runnable(){
//		@Override
//		public void run() {
//			Log.d(VideoConsumer.TAG, "Drawer ===== START");
//			Bitmap bitmap = Bitmap.createBitmap(VideoConsumer.this.width, VideoConsumer.this.height, Bitmap.Config.RGB_565);
//			ByteBuffer buffer;
//			
//			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LESS_FAVORABLE);
//			
//			while(VideoConsumer.this.running){
//				try {
//					VideoConsumer.this.semaphore.acquire();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//					break;
//				}
//				
//				if((VideoConsumer.this.preview == null)){
//					Log.e(VideoConsumer.TAG, "Invalid state");
//					continue;
//				}
//				
//				
//				//synchronized(VideoConsumer.this.buffers){
//					if(!VideoConsumer.this.buffers.isEmpty()){
//						buffer = VideoConsumer.this.buffers.remove(0);
//					}
//					else{
//						continue;
//					}
//				//}
//				if(buffer != null){
//					bitmap.copyPixelsFromBuffer(buffer);
//					try{
//						/*final SurfaceHolder holder = VideoConsumer.this.preview.getHolder();
//						final Canvas canvas = holder.lockCanvas();
//						
//						if (canvas != null){
//							//canvas.drawBitmap(bitmap, 0, 0, null);
//							canvas.drawBitmap(bitmap, null, holder.getSurfaceFrame(), null);
//							holder.unlockCanvasAndPost(canvas);
//						}
//						else{
//							Log.e(VideoConsumer.TAG, "Invalid canvas");
//						}*/
//					}
//					catch(Exception e){
//						e.printStackTrace();
//					}
//				}
//			}
//			
//			VideoConsumer.this.buffers.clear();
//			
//			Log.d(VideoConsumer.TAG, "Drawer ===== STOP");
//		}
//	};
	
	
	/* ==================================================*/
	class Preview extends SurfaceView implements SurfaceHolder.Callback {
		private final SurfaceHolder holder;
		private final Rect rect;
		Preview(Context context, int width, int height, int fps) {
			super(context);
			
			this.holder = getHolder();
			this.holder.addCallback(this);
			
			if(this.holder != null){
				this.rect = this.holder.getSurfaceFrame();
			}
			else{
				this.rect = null;
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		}
	}
	
	
	
	/* ==================================================*/
	class MyProxyVideoConsumer extends ProxyVideoConsumer
	{
		private final VideoConsumer consumer;
		
		private MyProxyVideoConsumer(VideoConsumer consumer){
			super(tmedia_chroma_t.tmedia_rgb565le);
			this.consumer = consumer;
		}
		
		@Override
		public int pause() {
			return this.consumer.pause();
		}

		@Override
		public int prepare(int width, int height, int fps) {
			return this.consumer.prepare(width, height, fps);
		}

		@Override
		public int start() {
			return this.consumer.start();
		}
		
		@Override
		public int consume(ProxyVideoFrame _frame) {
			return this.consumer.consume(_frame);
		}
		
		@Override
		public int stop() {
			return this.consumer.stop();
		}
	}
}
