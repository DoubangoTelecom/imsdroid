package org.doubango.imsdroid.Screens;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.imsdroid.sip.PresenceStatus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ScreenPresence  extends Screen {
	
	private CheckBox cbEnablePresence;
	private CheckBox cbEnableRLS;
	private CheckBox cbEnablePartialPub;
	private EditText etFreeText;
	private ImageView ivAvatar;
	private ImageButton btCamera;
	private ImageButton btChooseFile;
	private FrameLayout flPreview;
	private RelativeLayout rlPresence;
	private Spinner spStatus;
	
	private static final String TAG = ScreenPresence.class.getCanonicalName();
	private Camera camera;
	private Preview preview;
	
	private final static StatusItem[] spinner_status_items = new StatusItem[] {
		new StatusItem(R.drawable.user_online_24, PresenceStatus.Online, PresenceStatus.Online.toString()),
		new StatusItem(R.drawable.user_busy_24, PresenceStatus.Busy, "Busy"),
		new StatusItem(R.drawable.user_back_24, PresenceStatus.BeRightBack, "Be Right Back"),
		new StatusItem(R.drawable.user_time_24, PresenceStatus.Away, "Away"),
		new StatusItem(R.drawable.user_onthephone_24, PresenceStatus.OnThePhone, "On the phone"),
		new StatusItem(R.drawable.user_hyper_avail_24, PresenceStatus.HyperAvail, "HyperAvailable"),
		new StatusItem(R.drawable.user_offline_24, PresenceStatus.Offline, "Offline"),
	};
	
	private final IConfigurationService configurationService;
	private final ISipService sipService;
	
	public ScreenPresence() {
		super(SCREEN_TYPE.PRESENCE_T, ScreenPresence.class.getCanonicalName());
		
		this.configurationService = ServiceManager.getConfigurationService();
		this.sipService = ServiceManager.getSipService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_presence);
        
        // get controls
        this.cbEnablePresence = (CheckBox)this.findViewById(R.id.screen_presence_checkBox_enable_presence);
        this.cbEnableRLS = (CheckBox)this.findViewById(R.id.screen_presence_checkBox_rls);
        this.cbEnablePartialPub = (CheckBox)this.findViewById(R.id.screen_presence_checkBox_partial_pub);
        this.etFreeText = (EditText)this.findViewById(R.id.screen_presence_editText_freetext);
        this.ivAvatar = (ImageView)this.findViewById(R.id.screen_presence_imageView);
        this.btCamera = (ImageButton)this.findViewById(R.id.screen_presence_imageButton_cam);
        this.btChooseFile = (ImageButton)this.findViewById(R.id.screen_presence_imageButton_file);
        this.flPreview = (FrameLayout)this.findViewById(R.id.screen_presence_frameLayout_preview);
        this.rlPresence = (RelativeLayout)this.findViewById(R.id.screen_presence_relativeLayout_presence);
        this.spStatus = (Spinner)this.findViewById(R.id.screen_presence_spinner_status);
        
        // load spinner values
        this.spStatus.setAdapter(new ScreenOptionsAdapter(ScreenPresence.spinner_status_items));
        
        // load values from configuration file (do it before adding UI listeners)
        this.cbEnablePresence.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.PRESENCE, Configuration.DEFAULT_RCS_PRESENCE));
        this.cbEnableRLS.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.RLS, Configuration.DEFAULT_RCS_RLS));
        this.cbEnablePartialPub.setChecked(this.configurationService.getBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.PARTIAL_PUB, Configuration.DEFAULT_RCS_PARTIAL_PUB));
        this.etFreeText.setText(this.configurationService.getString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.FREE_TEXT, Configuration.DEFAULT_RCS_FREE_TEXT));
        this.rlPresence.setVisibility(this.cbEnablePresence.isChecked()? View.VISIBLE : View.INVISIBLE);
        this.spStatus.setSelection(this.getSpinnerIndex(
				Enum.valueOf(PresenceStatus.class, this.configurationService.getString(
						CONFIGURATION_SECTION.RCS,
						CONFIGURATION_ENTRY.STATUS,
						Configuration.DEFAULT_RCS_STATUS.toString()))));
        
        // add local listeners
        this.spStatus.setOnItemSelectedListener(this.spStatus_OnItemSelectedListener);
        this.cbEnablePresence.setOnCheckedChangeListener(this.cbEnablePresence_OnCheckedChangeListener);
        
        // add listeners (for the configuration)
        /* this.addConfigurationListener(this.cbEnablePresence); */
        this.addConfigurationListener(this.cbEnableRLS);
        this.addConfigurationListener(this.cbEnablePartialPub);
        this.addConfigurationListener(this.etFreeText);
        /* this.addConfigurationListener(this.spStatus); */
        
        // Camera
        this.preview = new Preview(ServiceManager.getMainActivity());
        this.flPreview.addView(this.preview);
        
        this.btCamera.setOnClickListener(this.btCamera_OnClickListener);
        this.btChooseFile.setOnClickListener(this.btChooseFile_OnClickListener);
	}
	
	protected void onPause() {
		if(this.computeConfiguration){
			String oldFreeText = this.configurationService.getString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.FREE_TEXT, Configuration.DEFAULT_RCS_FREE_TEXT);
			
			this.configurationService.setBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.PRESENCE, this.cbEnablePresence.isChecked());
			this.configurationService.setBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.RLS, this.cbEnableRLS.isChecked());
			this.configurationService.setBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.PARTIAL_PUB, this.cbEnablePartialPub.isChecked());
			this.configurationService.setString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.FREE_TEXT, 
					this.etFreeText.getText().toString());
			//this.configurationService.setString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.STATUS, 
			//		this.spinner_status_items[this.spStatus.getSelectedItemPosition()].status.toString());
			
			// update main activity info (status is done below)
			ServiceManager.getMainActivity().setFreeText(this.etFreeText.getText().toString());
			
			// publish if needed (Status is done below)
			if(!oldFreeText.equals(this.etFreeText.getText())){
				if(this.sipService.isRegistered()){
					this.sipService.publish();
				}
			}
			
			// Compute
			if(!this.configurationService.compute()){
				Log.e(this.getClass().getCanonicalName(), "Failed to Compute() configuration");
			}
			
			this.computeConfiguration = false;
		}
		super.onPause();
	}
	
	private OnClickListener btCamera_OnClickListener = new OnClickListener(){
		public void onClick(View v) {
			
			// ScreenPresence.this.preview.camera.takePicture(null, null, ScreenPresence.this.jpegCallback);
			// ScreenPresence.this.computeConfiguration = true;
		}
	};
	
	private OnCheckedChangeListener cbEnablePresence_OnCheckedChangeListener = new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			ScreenPresence.this.rlPresence.setVisibility(isChecked? View.VISIBLE : View.INVISIBLE);
			ScreenPresence.this.computeConfiguration = true;
		}
	};
	
	private OnItemSelectedListener spStatus_OnItemSelectedListener = new OnItemSelectedListener(){
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			
			ScreenPresence.this.configurationService.setString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.STATUS, 
					ScreenPresence.spinner_status_items[position].status.toString());
			ServiceManager.getMainActivity().setStatus(ScreenPresence.spinner_status_items[position].drawableId);
			if(ScreenPresence.this.sipService.isRegistered()){
				ScreenPresence.this.sipService.publish();
			}
		}
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};
	
	private PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				// write to local sandbox file system
				// outStream =
				// CameraDemo.this.openFileOutput(String.format("%d.jpg",
				// System.currentTimeMillis()), 0);
				// Or write to sdcard
				outStream = new FileOutputStream(String.format(
						"/sdcard/%d.jpg", System.currentTimeMillis()));
				outStream.write(data);
				outStream.close();
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};
	
	private OnClickListener btChooseFile_OnClickListener = new OnClickListener(){
		public void onClick(View v) {
			
			ScreenPresence.this.computeConfiguration = true;
		}
	};
	
	public static int getStatusDrawableId(PresenceStatus status){
		int i;
		for(i = 0; i< ScreenPresence.spinner_status_items.length; i++){
			if(ScreenPresence.spinner_status_items[i].status == status){
				return ScreenPresence.spinner_status_items[i].drawableId;
			}
		}
		return ScreenPresence.spinner_status_items[0].drawableId;
	}
	
	private int getSpinnerIndex(PresenceStatus status){
		int i;
		for(i = 0; i< ScreenPresence.spinner_status_items.length; i++){
			if(ScreenPresence.spinner_status_items[i].status == status){
				return i;
			}
		}
		return 0;
	}
	
	/* ===================== Adapter ======================== */

	private static class StatusItem {
		private final int drawableId;
		private final PresenceStatus status;
		private final String text;

		private StatusItem(int drawableId, PresenceStatus status, String text) {
			this.drawableId = drawableId;
			this.status = status;
			this.text = text;
		}
	}
	
	private class ScreenOptionsAdapter extends BaseAdapter {

		private StatusItem[] items;
		private ScreenOptionsAdapter(StatusItem[] items){
			this.items = items;
		}
		
		public int getCount() {
			return this.items.length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			StatusItem item;

			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.screen_presence_status_item, null);
			}

			if ((this.items.length <= position) || ((item = this.items[position]) == null)) {
				return view;
			}

			ImageView iv = (ImageView) view .findViewById(R.id.screen_presence_status_item_imageView);
			iv.setImageResource(item.drawableId);
			TextView tv = (TextView) view.findViewById(R.id.screen_presence_status_item_textView);
			tv.setText(item.text);

			return view;
		}
	}
	
	
	
	/* ===================== Preview ======================== 
	 * Copyright: http://marakana.com/forums/android/android_examples/39.html.
	 * */
	
	private class Preview extends SurfaceView implements SurfaceHolder.Callback {
		private static final String TAG = "Preview";

		private SurfaceHolder mHolder;
		public Camera camera;

		private Preview(Context context) {
			super(context);

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			
			setFocusable(true);
		}

		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, acquire the camera and tell it where
			// to draw.
			camera = Camera.open();
			try {
				camera.setPreviewDisplay(holder);

				camera.setPreviewCallback(new PreviewCallback() {

					public void onPreviewFrame(byte[] data, Camera arg1) {
						FileOutputStream outStream = null;
						try {
							outStream = new FileOutputStream(String.format(
									"/sdcard/%d.jpg", System.currentTimeMillis()));
							outStream.write(data);
							outStream.close();
							Log.d(TAG, "onPreviewFrame - wrote bytes: "
									+ data.length);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
						}
						Preview.this.invalidate();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// Surface will be destroyed when we return, so stop the preview.
			// Because the CameraDevice object is not a shared resource, it's very
			// important to release it when the activity is paused.
			camera.stopPreview();
			camera = null;
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			// Now that the size is known, set up the camera parameters and begin
			// the preview.
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(w, h);
			camera.setParameters(parameters);
			camera.startPreview();
		}

		public void draw(Canvas canvas) {
			super.draw(canvas);
			Paint p = new Paint(Color.RED);
			Log.d(TAG, "draw");
			canvas.drawText("PREVIEW", canvas.getWidth() / 2,
					canvas.getHeight() / 2, p);
		}
	}
}
