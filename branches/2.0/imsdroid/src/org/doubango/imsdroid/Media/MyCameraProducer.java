package org.doubango.imsdroid.Media;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Utils.ConfigurationUtils;
import org.doubango.imsdroid.Utils.ConfigurationUtils.ConfigurationEntry;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * MyCameraProducer
 */
public class MyCameraProducer {
	private static final String TAG = MyCameraProducer.class.getCanonicalName();
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
		MyCameraProducer.useFrontFacingCamera = ServiceManager
				.getConfigurationService().getBoolean(ConfigurationEntry.GENERAL_USE_FFC,
						ConfigurationUtils.DEFAULT_GENERAL_USE_FFC);
	}
	
	static{
		if(IMSDroid.getSDKVersion() >= MyCameraProducer.MIN_SDKVERSION_addCallbackBuffer){
			// According to http://developer.android.com/reference/android/hardware/Camera.html both addCallbackBuffer and setPreviewCallbackWithBuffer
			// are only available starting API level 8. But it's not true as these functions exist in API level 7 but are hidden.
			try {
				MyCameraProducer.addCallbackBufferMethod = Camera.class.getMethod("addCallbackBuffer", byte[].class);
			} catch (Exception e) {
				Log.e(MyCameraProducer.TAG, e.toString());
			} 
		}
		
		if(IMSDroid.getSDKVersion() >= MyCameraProducer.MIN_SDKVERSION_setPreviewCallbackWithBuffer){
			try {
				MyCameraProducer.setPreviewCallbackWithBufferMethod = Camera.class.getMethod(
					"setPreviewCallbackWithBuffer", PreviewCallback.class);
			}  catch (Exception e) {
				Log.e(MyCameraProducer.TAG, e.toString());
			} 
		}
				
		if(IMSDroid.getSDKVersion() >= MyCameraProducer.MIN_SDKVERSION_setDisplayOrientation){
			try {
				MyCameraProducer.setDisplayOrientationMethod = Camera.class.getMethod("setDisplayOrientation", int.class);
			} catch (Exception e) {
				Log.e(MyCameraProducer.TAG, e.toString());
			} 
		}
	}
	
	public static Camera getCamera(){
		return MyCameraProducer.instance;
	}
	
	public static Camera openCamera(int fps, int width, int height, SurfaceHolder holder, PreviewCallback callback){
		if(MyCameraProducer.instance == null){
			try{
				if(MyCameraProducer.useFrontFacingCamera){
					MyCameraProducer.instance = MyCameraProducer.openFrontFacingCamera();
				}
				else{
					MyCameraProducer.instance = Camera.open();
				}
				
				MyCameraProducer.fps = fps;
				MyCameraProducer.width = width;
				MyCameraProducer.height = height;
				MyCameraProducer.holder = holder;
				MyCameraProducer.callback = callback;
				
				Camera.Parameters parameters = MyCameraProducer.instance.getParameters();
				
				/*
				 * http://developer.android.com/reference/android/graphics/ImageFormat.html#NV21
				 * YCrCb format used for images, which uses the NV21 encoding format. 
				 * This is the default format for camera preview images, when not otherwise set with setPreviewFormat(int). 
				 */
				parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
				parameters.setPreviewFrameRate(MyCameraProducer.fps);
				MyCameraProducer.instance.setParameters(parameters);
				
				try{
					parameters.setPictureSize(MyCameraProducer.width , MyCameraProducer.height);
					MyCameraProducer.instance.setParameters(parameters);
				}
				catch(Exception e){
					// FFMpeg converter will resize the video stream
					Log.d(MyCameraProducer.TAG, e.toString());
				}
				
				MyCameraProducer.instance.setPreviewDisplay(MyCameraProducer.holder);
				MyCameraProducer.initializeCallbacks(MyCameraProducer.callback);
			}
			catch(Exception e){
				MyCameraProducer.releaseCamera();
				Log.e(MyCameraProducer.TAG, e.toString());
			}
		}
		return MyCameraProducer.instance;
	}
	
	public static void releaseCamera(){
		if(MyCameraProducer.instance != null){
			MyCameraProducer.instance.stopPreview();
			MyCameraProducer.deInitializeCallbacks();
			MyCameraProducer.instance.release();
			MyCameraProducer.instance = null;
		}
	}
	
	public static void setDisplayOrientation(int degrees){
		if(MyCameraProducer.instance != null && MyCameraProducer.setDisplayOrientationMethod != null){
			try {
				MyCameraProducer.setDisplayOrientationMethod.invoke(MyCameraProducer.instance, degrees);
			} catch (Exception e) {
				Log.e(MyCameraProducer.TAG, e.toString());
			}
		}
	}
	
	public static void setDisplayOrientation(Camera camera, int degrees){
		if(camera != null && MyCameraProducer.setDisplayOrientationMethod != null){
			try {
				MyCameraProducer.setDisplayOrientationMethod.invoke(camera, degrees);
			} catch (Exception e) {
				Log.e(MyCameraProducer.TAG, e.toString());
			}
		}
	}
	
	public static void addCallbackBuffer(Camera camera, byte[] buffer) {
		try {
			MyCameraProducer.addCallbackBufferMethod.invoke(camera, buffer);
		} catch (Exception e) {
			Log.e(MyCameraProducer.TAG, e.toString());
		}
	}
	
	public static void addCallbackBuffer(byte[] buffer) {
		try {
			MyCameraProducer.addCallbackBufferMethod.invoke(MyCameraProducer.instance, buffer);
		} catch (Exception e) {
			Log.e(MyCameraProducer.TAG, e.toString());
		}
	}

	public static boolean isAddCallbackBufferSupported(){
		return MyCameraProducer.addCallbackBufferMethod != null;
	}
	
	public static boolean isFrontFacingCameraEnabled(){
		return MyCameraProducer.useFrontFacingCamera;
	}
	
	public static void useRearCamera(){
		MyCameraProducer.useFrontFacingCamera = false;
	}
	
	public static void useFrontFacingCamera(){
		MyCameraProducer.useFrontFacingCamera = true;
	}
	
	public static Camera toggleCamera(){
		if(MyCameraProducer.instance != null){
			MyCameraProducer.useFrontFacingCamera = !MyCameraProducer.useFrontFacingCamera;
			MyCameraProducer.releaseCamera();
			MyCameraProducer.openCamera(MyCameraProducer.fps, 
					MyCameraProducer.width, 
					MyCameraProducer.height,
					MyCameraProducer.holder, 
					MyCameraProducer.callback);
		}
		return MyCameraProducer.instance;
	}
	
	private static void initializeCallbacks(PreviewCallback callback){
		if(MyCameraProducer.instance != null){
			if(MyCameraProducer.setPreviewCallbackWithBufferMethod != null){
				try {
					MyCameraProducer.setPreviewCallbackWithBufferMethod.invoke(MyCameraProducer.instance, callback);
				} catch (Exception e) {
					Log.e(MyCameraProducer.TAG, e.toString());
				}
			}
			else{
				MyCameraProducer.instance.setPreviewCallback(callback);
			}
		}
	}
	
	private static void deInitializeCallbacks(){
		if(MyCameraProducer.instance != null){
			if(MyCameraProducer.setPreviewCallbackWithBufferMethod != null){
				try {
					MyCameraProducer.setPreviewCallbackWithBufferMethod.invoke(MyCameraProducer.instance, new Object[]{ null });
				} catch (Exception e) {
					Log.e(MyCameraProducer.TAG, e.toString());
				}
			}
			else{
				MyCameraProducer.instance.setPreviewCallback(null);
			}
		}
	}
	private static Camera openFrontFacingCamera() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Camera camera = null;
		
		//1. From mapper
		if((camera = FrontFacingCameraMapper.getPreferredCamera()) != null){
			return camera;
		}
		
		//2. Use switcher
		if(FrontFacingCameraSwitcher.getSwitcher() != null){
			camera = Camera.open();
			FrontFacingCameraSwitcher.getSwitcher().invoke(camera, (int)1);
			return camera;
		}
		
		//3. Use parameters
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
				Log.d(MyCameraProducer.TAG, e.toString());
			}
		}
		
		static Method getSwitcher(){
			return FrontFacingCameraSwitcher.DualCameraSwitchMethod;
		}
	}
	
	/**
	 * FrontFacingCameraMapper
	 * @author Mamadou Diop
	 *
	 */
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
					Log.d(MyCameraProducer.TAG, e.toString());
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
				Log.e(MyCameraProducer.TAG, e.toString());
			}
			return null;
		}
	}
}
