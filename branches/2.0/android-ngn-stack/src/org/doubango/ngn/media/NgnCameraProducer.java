/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.utils.NgnConfigurationEntry;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;

public class NgnCameraProducer {
	private static final String TAG = NgnCameraProducer.class.getCanonicalName();
	private static Camera instance;
	private static boolean useFrontFacingCamera;
	
	// Default values
	private static int fps = 15;
	private static int width = 176;
	private static int height = 144;
	private static SurfaceHolder holder = null;
	private static PreviewCallback callback = null;
	
	private static final int MIN_SDKVERSION_addCallbackBuffer = 7;
	private static final int MIN_SDKVERSION_setPreviewCallbackWithBuffer = 7;
	private static final int MIN_SDKVERSION_setDisplayOrientation = 8;
	//private static final int MIN_SDKVERSION_getSupportedPreviewSizes = 5;
	
	private static Method addCallbackBufferMethod = null;
	private static Method setDisplayOrientationMethod = null;
	private static Method setPreviewCallbackWithBufferMethod = null;
	
	static{
		NgnCameraProducer.useFrontFacingCamera = NgnEngine
				.getInstance().getConfigurationService().getBoolean(NgnConfigurationEntry.GENERAL_USE_FFC,
						NgnConfigurationEntry.DEFAULT_GENERAL_USE_FFC);
	}
	
	static{
		if(NgnApplication.getSDKVersion() >= NgnCameraProducer.MIN_SDKVERSION_addCallbackBuffer){
			// According to http://developer.android.com/reference/android/hardware/Camera.html both addCallbackBuffer and setPreviewCallbackWithBuffer
			// are only available starting API level 8. But it's not true as these functions exist in API level 7 but are hidden.
			try {
				NgnCameraProducer.addCallbackBufferMethod = Camera.class.getMethod("addCallbackBuffer", byte[].class);
			} catch (Exception e) {
				Log.e(NgnCameraProducer.TAG, e.toString());
			} 
		}
		
		if(NgnApplication.getSDKVersion() >= NgnCameraProducer.MIN_SDKVERSION_setPreviewCallbackWithBuffer){
			try {
				NgnCameraProducer.setPreviewCallbackWithBufferMethod = Camera.class.getMethod(
					"setPreviewCallbackWithBuffer", PreviewCallback.class);
			}  catch (Exception e) {
				Log.e(NgnCameraProducer.TAG, e.toString());
			}
		}
				
		if(NgnApplication.getSDKVersion() >= NgnCameraProducer.MIN_SDKVERSION_setDisplayOrientation){
			try {
				NgnCameraProducer.setDisplayOrientationMethod = Camera.class.getMethod("setDisplayOrientation", int.class);
			} catch (Exception e) {
				Log.e(NgnCameraProducer.TAG, e.toString());
			} 
		}
	}
	
	public static Camera getCamera(){
		return NgnCameraProducer.instance;
	}
	
	public static Camera openCamera(int fps, int width, int height, SurfaceHolder holder, PreviewCallback callback){
		if(NgnCameraProducer.instance == null){
			try{
				if(NgnCameraProducer.useFrontFacingCamera){
					NgnCameraProducer.instance = NgnCameraProducer.openFrontFacingCamera();
				}
				else{
					NgnCameraProducer.instance = Camera.open();
				}
				
				NgnCameraProducer.fps = fps;
				NgnCameraProducer.width = width;
				NgnCameraProducer.height = height;
				NgnCameraProducer.holder = holder;
				NgnCameraProducer.callback = callback;
				
				Camera.Parameters parameters = NgnCameraProducer.instance.getParameters();
				
				/*
				 * http://developer.android.com/reference/android/graphics/ImageFormat.html#NV21
				 * YCrCb format used for images, which uses the NV21 encoding format. 
				 * This is the default format for camera preview images, when not otherwise set with setPreviewFormat(int). 
				 */
				parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
				parameters.setPreviewFrameRate(NgnCameraProducer.fps);
				NgnCameraProducer.instance.setParameters(parameters);
				
				try{
					parameters.setPictureSize(NgnCameraProducer.width , NgnCameraProducer.height);
					NgnCameraProducer.instance.setParameters(parameters);
				}
				catch(Exception e){
					// FFMpeg converter will resize the video stream
					Log.d(NgnCameraProducer.TAG, e.toString());
				}
				
				NgnCameraProducer.instance.setPreviewDisplay(NgnCameraProducer.holder);
				NgnCameraProducer.initializeCallbacks(NgnCameraProducer.callback);
			}
			catch(Exception e){
				NgnCameraProducer.releaseCamera();
				Log.e(NgnCameraProducer.TAG, e.toString());
			}
		}
		return NgnCameraProducer.instance;
	}
	
	public static void releaseCamera(){
		if(NgnCameraProducer.instance != null){
			NgnCameraProducer.instance.stopPreview();
			NgnCameraProducer.deInitializeCallbacks();
			NgnCameraProducer.instance.release();
			NgnCameraProducer.instance = null;
		}
	}
	
	public static void setDisplayOrientation(int degrees){
		if(NgnCameraProducer.instance != null && NgnCameraProducer.setDisplayOrientationMethod != null){
			try {
				NgnCameraProducer.setDisplayOrientationMethod.invoke(NgnCameraProducer.instance, degrees);
			} catch (Exception e) {
				Log.e(NgnCameraProducer.TAG, e.toString());
			}
		}
	}
	
	public static void setDisplayOrientation(Camera camera, int degrees){
		if(camera != null && NgnCameraProducer.setDisplayOrientationMethod != null){
			try {
				NgnCameraProducer.setDisplayOrientationMethod.invoke(camera, degrees);
			} catch (Exception e) {
				Log.e(NgnCameraProducer.TAG, e.toString());
			}
		}
	}
	
	public static void addCallbackBuffer(Camera camera, byte[] buffer) {
		try {
			NgnCameraProducer.addCallbackBufferMethod.invoke(camera, buffer);
		} catch (Exception e) {
			Log.e(NgnCameraProducer.TAG, e.toString());
		}
	}
	
	public static void addCallbackBuffer(byte[] buffer) {
		try {
			NgnCameraProducer.addCallbackBufferMethod.invoke(NgnCameraProducer.instance, buffer);
		} catch (Exception e) {
			Log.e(NgnCameraProducer.TAG, e.toString());
		}
	}

	public static boolean isAddCallbackBufferSupported(){
		return NgnCameraProducer.addCallbackBufferMethod != null;
	}
	
	public static boolean isFrontFacingCameraEnabled(){
		return NgnCameraProducer.useFrontFacingCamera;
	}
	
	public static void useRearCamera(){
		NgnCameraProducer.useFrontFacingCamera = false;
	}
	
	public static void useFrontFacingCamera(){
		NgnCameraProducer.useFrontFacingCamera = true;
	}
	
	public static Camera toggleCamera(){
		if(NgnCameraProducer.instance != null){
			NgnCameraProducer.useFrontFacingCamera = !NgnCameraProducer.useFrontFacingCamera;
			NgnCameraProducer.releaseCamera();
			NgnCameraProducer.openCamera(NgnCameraProducer.fps, 
					NgnCameraProducer.width, 
					NgnCameraProducer.height,
					NgnCameraProducer.holder, 
					NgnCameraProducer.callback);
		}
		return NgnCameraProducer.instance;
	}
	
	private static void initializeCallbacks(PreviewCallback callback){
		if(NgnCameraProducer.instance != null){
			if(NgnCameraProducer.setPreviewCallbackWithBufferMethod != null){
				try {
					NgnCameraProducer.setPreviewCallbackWithBufferMethod.invoke(NgnCameraProducer.instance, callback);
				} catch (Exception e) {
					Log.e(NgnCameraProducer.TAG, e.toString());
				}
			}
			else{
				NgnCameraProducer.instance.setPreviewCallback(callback);
			}
		}
	}
	
	private static void deInitializeCallbacks(){
		if(NgnCameraProducer.instance != null){
			if(NgnCameraProducer.setPreviewCallbackWithBufferMethod != null){
				try {
					NgnCameraProducer.setPreviewCallbackWithBufferMethod.invoke(NgnCameraProducer.instance, new Object[]{ null });
				} catch (Exception e) {
					Log.e(NgnCameraProducer.TAG, e.toString());
				}
			}
			else{
				NgnCameraProducer.instance.setPreviewCallback(null);
			}
		}
	}
	
	public static int getNumberOfCameras() {
		// 1. Android 2.3 or later
		if (NgnApplication.getSDKVersion() >= 9) {
			try {
				Method getNumberOfCamerasMethod = Camera.class.getDeclaredMethod("getNumberOfCameras");
				if (getNumberOfCamerasMethod != null) {
					return (Integer) getNumberOfCamerasMethod.invoke(null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 1;
	}

	private static Camera openFrontFacingCamera() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Camera camera = null;
		
		// 1. Android 2.3 or later
		if(NgnApplication.getSDKVersion() >= 9){
			try {
				Method getNumberOfCamerasMethod = Camera.class.getDeclaredMethod("getNumberOfCameras");
				if(getNumberOfCamerasMethod != null){
					Integer numberOfCameras = (Integer)getNumberOfCamerasMethod.invoke(null);
					if(numberOfCameras > 1){
						Method openMethod = Camera.class.getDeclaredMethod("open", int.class);
						if((camera = (Camera)openMethod.invoke(null, (numberOfCameras - 1))) != null){
							return camera;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//2. From mapper
		if((camera = FrontFacingCameraMapper.getPreferredCamera()) != null){
			return camera;
		}
		
		//3. Use switcher
		if(FrontFacingCameraSwitcher.getSwitcher() != null){
			camera = Camera.open();
			FrontFacingCameraSwitcher.getSwitcher().invoke(camera, (int)1);
			return camera;
		}
		
		//4. Use parameters
		camera = Camera.open();
		Camera.Parameters parameters = camera.getParameters();
		parameters.set("camera-id", 2);
		camera.setParameters(parameters);
		return camera;
	}
	
	/***
	 * FrontFacingCameraSwitcher
	 * @author Mamadou Diop
	 *
	 */
	static class FrontFacingCameraSwitcher
	{
		private static Method DualCameraSwitchMethod;
		
		static{
			try{
				FrontFacingCameraSwitcher.DualCameraSwitchMethod = Class.forName("android.hardware.Camera").getMethod("DualCameraSwitch",int.class);
			}
			catch(Exception e){
				Log.d(NgnCameraProducer.TAG, e.toString());
			}
		}
		
		static Method getSwitcher(){
			return FrontFacingCameraSwitcher.DualCameraSwitchMethod;
		}
	}
	
	static class FrontFacingCameraMapper
	{
		private static int preferredIndex = -1;
		
		static FrontFacingCameraMapper Map[] = {
			new FrontFacingCameraMapper("android.hardware.HtcFrontFacingCamera", "getCamera"),
			// Sprint: HTC EVO 4G and Samsung Epic 4G
			// DO not forget to change the manifest if you are using OS 1.6 and later
			new FrontFacingCameraMapper("com.sprint.hardware.twinCamDevice.FrontFacingCamera", "getFrontFacingCamera"),
			// Huawei U8230
            new FrontFacingCameraMapper("android.hardware.CameraSlave", "open"),
			// Default: Used for test reflection
			// new FrontFacingCameraMapper("android.hardware.Camera", "open"),
		};
		
		static{
			int index = 0;
			for(FrontFacingCameraMapper ffc: FrontFacingCameraMapper.Map){
				try{
					Class.forName(ffc.className).getDeclaredMethod(ffc.methodName);
					FrontFacingCameraMapper.preferredIndex = index;
					break;
				}
				catch(Exception e){
					Log.d(NgnCameraProducer.TAG, e.toString());
				}
				
				++index;
			}
		}
		
		private final String className;
		private final String methodName;
		
		FrontFacingCameraMapper(String className, String methodName){
			this.className = className;
			this.methodName = methodName;
		}
		
		static Camera getPreferredCamera(){
			if(FrontFacingCameraMapper.preferredIndex == -1){
				return null;
			}
			
			try{				
				Method method = Class.forName(FrontFacingCameraMapper.Map[FrontFacingCameraMapper.preferredIndex].className)
				.getDeclaredMethod(FrontFacingCameraMapper.Map[FrontFacingCameraMapper.preferredIndex].methodName);
				return (Camera)method.invoke(null);
			}
			catch(Exception e){
				Log.e(NgnCameraProducer.TAG, e.toString());
			}
			return null;
		}
	}
}
