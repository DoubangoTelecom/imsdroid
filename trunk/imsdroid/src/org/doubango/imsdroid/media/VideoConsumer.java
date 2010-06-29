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
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

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
	private static int MAX_DELAY = 2;
	
	private Context context;
	private int width;
	private int height;
	private int fps;
	private ByteBuffer frame;
	private Preview preview;
	private boolean running;
	
	private final MyProxyVideoConsumer videoConsumer;
	private final ArrayList<IntBuffer> buffers;
	private final Semaphore semaphore;
	
	public VideoConsumer(){
		this.videoConsumer = new MyProxyVideoConsumer(this);
		this.buffers = new ArrayList<IntBuffer>();
		this.semaphore = new Semaphore(0);
		
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
		
		this.frame = ByteBuffer.allocateDirect(this.width * this.height * 2);
		return 0;
	}
	
	public synchronized int start() {
		Log.d(VideoConsumer.TAG, "start()");
		if(this.context != null){
			this.running = true;
			new Thread(this.runnableDrawer).start();
			return 0;
		}
		else{
			Log.e(VideoConsumer.TAG, "Invalid context");
			return -1;
		}
	}
	
	public int consume(ProxyVideoFrame _frame){
		if(VideoConsumer.this.buffers.size()< (VideoConsumer.this.fps * VideoConsumer.MAX_DELAY)){
			_frame.getContent(this.frame, this.frame.capacity());
			final IntBuffer buffer = this.frame.asIntBuffer();
			synchronized(VideoConsumer.this.buffers){
				VideoConsumer.this.buffers.add(buffer);
			}
			VideoConsumer.this.semaphore.release();
			this.frame.rewind();
		}
		else{
			//synchronized(VideoConsumer.this.buffers){
				//VideoConsumer.this.buffers.clear();
			//}
		}
        
		return 0;
	}
	
	public synchronized int stop() {
		Log.d(VideoConsumer.TAG, "stop()");
		
		this.preview = null;
		this.context = null;
		
		this.running = false;
		this.semaphore.release();
		
		return 0;
	}
	
	private Runnable runnableDrawer = new Runnable(){
		@Override
		public void run() {
			Log.d(VideoConsumer.TAG, "Drawer ===== START");
			Bitmap bitmap = Bitmap.createBitmap(VideoConsumer.this.width, VideoConsumer.this.height, Bitmap.Config.RGB_565);
			Rect CIF = new Rect(0, 0, 352, 288); // CIF
			
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
			
			while(true){
				try {
					VideoConsumer.this.semaphore.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
				
				if(!VideoConsumer.this.running || (VideoConsumer.this.videoConsumer == null) || (VideoConsumer.this.preview == null)){
					break;
				}
				
				final IntBuffer buffer;
				synchronized(VideoConsumer.this.buffers){
					if(!VideoConsumer.this.buffers.isEmpty()){
						buffer = VideoConsumer.this.buffers.remove(0);
					}
					else{
						continue;
					}
				}
				if(buffer != null){
					bitmap.copyPixelsFromBuffer(buffer);
					try{
						final SurfaceHolder holder = VideoConsumer.this.preview.getHolder();
						final Canvas canvas = holder.lockCanvas();
						if (canvas != null){
							//canvas.drawBitmap(bitmap, 0, 0, null);
							canvas.drawBitmap(bitmap, null, CIF, null);
							holder.unlockCanvasAndPost(canvas);
						}
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			Log.d(VideoConsumer.TAG, "Drawer ===== STOP");
		}
	};
	
	
	/* ==================================================*/
	class Preview extends SurfaceView implements SurfaceHolder.Callback {
		private SurfaceHolder holder;
		
		//private final int width;
		//private final int height;

		Preview(Context context, int width, int height, int fps) {
			super(context);

			//this.width = width;
			//this.height = height;
			
			this.holder = getHolder();
			this.holder.addCallback(this);
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
