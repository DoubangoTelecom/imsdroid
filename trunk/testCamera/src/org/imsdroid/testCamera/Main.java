package org.imsdroid.testCamera;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class Main extends Activity {
	
	private final static String TAG = Main.class.getCanonicalName();
	private Preview mPreview;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mPreview = new Preview(this, this.previewCallback);
        
        setContentView(mPreview);
    }
    
    
    PreviewCallback previewCallback = new PreviewCallback() {
    	  public void onPreviewFrame(byte[] _data, Camera _camera) {
    		  int i = 0;
    		  i++;
    		  Log.d(Main.TAG, "i="+i);
    	  }
    	};
    
    
 // ----------------------------------------------------------------------

    class Preview extends SurfaceView implements SurfaceHolder.Callback {
        SurfaceHolder mHolder;
        Camera mCamera;
        PreviewCallback callback;
        
        Preview(Context context, PreviewCallback callback) {
            super(context);
            
            this.callback = callback;
            
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, acquire the camera and tell it where
            // to draw.
            mCamera = Camera.open();
            try {
            	
            	Camera.Parameters parameters = mCamera.getParameters(); 
            	parameters.setPreviewFrameRate(60);
            	parameters.setPictureSize(176, 144);
            	mCamera.setParameters(parameters); 
            	
               mCamera.setPreviewDisplay(holder);
               mCamera.setPreviewCallback(this.callback);
            } catch (IOException exception) {
                mCamera.release();
                mCamera = null;
                // TODO: add more exception handling logic here
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview.
            // Because the CameraDevice object is not a shared resource, it's very
            // important to release it when the activity is paused.
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null; 
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // Now that the size is known, set up the camera parameters and begin
            // the preview.
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(w, h);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }
    }
}