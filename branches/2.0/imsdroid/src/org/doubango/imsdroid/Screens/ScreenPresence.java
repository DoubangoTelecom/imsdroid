package org.doubango.imsdroid.Screens;

import java.io.IOException;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sip.PresenceStatus;
import org.doubango.imsdroid.Utils.ConfigurationUtils;
import org.doubango.imsdroid.Utils.ConfigurationUtils.ConfigurationEntry;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class ScreenPresence  extends BaseScreen{
	private static String TAG = ScreenPresence.class.getCanonicalName();
	
	private CheckBox mCbEnablePresence;
	private CheckBox mCbEnableRLS;
	private CheckBox mCbEnablePartialPub;
	private EditText mEtFreeText;
	@SuppressWarnings("unused")
	private ImageView mIvAvatar;
	private ImageButton mBtCamera;
	private ImageButton mBtChooseFile;
	private RelativeLayout mRlPresence;
	private Spinner mSpStatus;
	
	private Preview mPreview;
	
	private final static StatusItem[] sSpinnerStatusItems = new StatusItem[] {
		new StatusItem(R.drawable.user_online_24, PresenceStatus.Online, PresenceStatus.Online.toString()),
		new StatusItem(R.drawable.user_busy_24, PresenceStatus.Busy, "Busy"),
		new StatusItem(R.drawable.user_back_24, PresenceStatus.BeRightBack, "Be Right Back"),
		new StatusItem(R.drawable.user_time_24, PresenceStatus.Away, "Away"),
		new StatusItem(R.drawable.user_onthephone_24, PresenceStatus.OnThePhone, "On the phone"),
		new StatusItem(R.drawable.user_hyper_avail_24, PresenceStatus.HyperAvailable, "HyperAvailable"),
		new StatusItem(R.drawable.user_offline_24, PresenceStatus.Offline, "Offline"),
	};
	
	private final IConfigurationService mConfigurationService;
	private final ISipService mSipService;
	
	public ScreenPresence() {
		super(SCREEN_TYPE.PRESENCE_T, ScreenPresence.class.getCanonicalName());
		
		mConfigurationService = ServiceManager.getConfigurationService();
		mSipService = ServiceManager.getSipService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_presence);
        
        mCbEnablePresence = (CheckBox)findViewById(R.id.screen_presence_checkBox_enable_presence);
        mCbEnableRLS = (CheckBox)findViewById(R.id.screen_presence_checkBox_rls);
        mCbEnablePartialPub = (CheckBox)findViewById(R.id.screen_presence_checkBox_partial_pub);
        mEtFreeText = (EditText)findViewById(R.id.screen_presence_editText_freetext);
        mIvAvatar = (ImageView)findViewById(R.id.screen_presence_imageView);
        mBtCamera = (ImageButton)findViewById(R.id.screen_presence_imageButton_cam);
        mBtChooseFile = (ImageButton)findViewById(R.id.screen_presence_imageButton_file);
        mRlPresence = (RelativeLayout)findViewById(R.id.screen_presence_relativeLayout_presence);
        mSpStatus = (Spinner)findViewById(R.id.screen_presence_spinner_status);
        
        mSpStatus.setAdapter(new ScreenOptionsAdapter(this));
        
        mCbEnablePresence.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.RCS_USE_PRESENCE, ConfigurationUtils.DEFAULT_RCS_USE_PRESENCE));
        mCbEnableRLS.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.RCS_USE_RLS, ConfigurationUtils.DEFAULT_RCS_USE_RLS));
        mCbEnablePartialPub.setChecked(mConfigurationService.getBoolean(ConfigurationEntry.RCS_USE_PARTIAL_PUB, ConfigurationUtils.DEFAULT_RCS_USE_PARTIAL_PUB));
        mEtFreeText.setText(mConfigurationService.getString(ConfigurationEntry.RCS_FREE_TEXT, ConfigurationUtils.DEFAULT_RCS_FREE_TEXT));
        mRlPresence.setVisibility(mCbEnablePresence.isChecked()? View.VISIBLE : View.INVISIBLE);
        mSpStatus.setSelection(getSpinnerIndex(
				Enum.valueOf(PresenceStatus.class, mConfigurationService.getString(
						ConfigurationEntry.RCS_STATUS,
						ConfigurationUtils.DEFAULT_RCS_STATUS.toString()))));
        
        // add local listeners
        mSpStatus.setOnItemSelectedListener(spStatus_OnItemSelectedListener);
        mCbEnablePresence.setOnCheckedChangeListener(cbEnablePresence_OnCheckedChangeListener);
        
        // add listeners (for the configuration)
        /* addConfigurationListener(cbEnablePresence); */
        addConfigurationListener(mCbEnableRLS);
        addConfigurationListener(mCbEnablePartialPub);
        addConfigurationListener(mEtFreeText);
        /* addConfigurationListener(spStatus); */
        
        // Camera
        mPreview = new Preview(this);
        
        mBtCamera.setOnClickListener(mBtCamera_OnClickListener);
        mBtChooseFile.setOnClickListener(mBtChooseFile_OnClickListener);
	}

	@Override
	protected void onPause() {
		if(super.mComputeConfiguration){
			final String oldFreeText = mConfigurationService.getString(ConfigurationEntry.RCS_FREE_TEXT, ConfigurationUtils.DEFAULT_RCS_FREE_TEXT);
			final String newFreeText = mEtFreeText.getText().toString();
			
			mConfigurationService.putBoolean(ConfigurationEntry.RCS_USE_PRESENCE, mCbEnablePresence.isChecked());
			mConfigurationService.putBoolean(ConfigurationEntry.RCS_USE_RLS, mCbEnableRLS.isChecked());
			mConfigurationService.putBoolean(ConfigurationEntry.RCS_USE_PARTIAL_PUB, mCbEnablePartialPub.isChecked());
			mConfigurationService.putString(ConfigurationEntry.RCS_FREE_TEXT, newFreeText);
			
			// publish if needed (Status is done below)
			if(!oldFreeText.equals(newFreeText)){
				if(mSipService.isRegistered()){
					// -- mSipService.publish();
				}
			}
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
	
	private OnClickListener mBtCamera_OnClickListener = new OnClickListener(){
		public void onClick(View v) {
			setContentView(mPreview);
			//ScreenPresence.flPreview.removeAllViews();
			//ScreenPresence.flPreview.addView(ScreenPresence.preview);
		}
	};
	
	private OnCheckedChangeListener cbEnablePresence_OnCheckedChangeListener = new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			mRlPresence.setVisibility(isChecked? View.VISIBLE : View.INVISIBLE);
			mComputeConfiguration = true;
		}
	};
	
	private OnItemSelectedListener spStatus_OnItemSelectedListener = new OnItemSelectedListener(){
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			mConfigurationService.putString(ConfigurationEntry.RCS_STATUS, 
					sSpinnerStatusItems[position].mStatus.toString());
			// ServiceManager.getMainActivity().setStatus(ScreenPresence.sSpinnerStatusItems[position].mDrawableId);
			if(mSipService.isRegistered()){
				//-- mSipService.publish();
			}
		}
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};
		
	private OnClickListener mBtChooseFile_OnClickListener = new OnClickListener(){
		public void onClick(View v) {
			mComputeConfiguration = true;
		}
	};
	
	public static int getStatusDrawableId(PresenceStatus status){
		int i;
		for(i = 0; i< sSpinnerStatusItems.length; i++){
			if(sSpinnerStatusItems[i].mStatus == status){
				return sSpinnerStatusItems[i].mDrawableId;
			}
		}
		return sSpinnerStatusItems[0].mDrawableId;
	}
	
	private int getSpinnerIndex(PresenceStatus status){
		int i;
		for(i = 0; i< sSpinnerStatusItems.length; i++){
			if(sSpinnerStatusItems[i].mStatus == status){
				return i;
			}
		}
		return 0;
	}
	

	

	static class StatusItem {
		private final int mDrawableId;
		private final PresenceStatus mStatus;
		private final String mText;

		private StatusItem(int drawableId, PresenceStatus status, String text) {
			mDrawableId = drawableId;
			mStatus = status;
			mText = text;
		}
	}
	
	static class ScreenOptionsAdapter extends BaseAdapter {

		private final LayoutInflater mInflater;
		
		private ScreenOptionsAdapter(Context context){
			mInflater = LayoutInflater.from(context);
		}
		
		public int getCount() {
			return sSpinnerStatusItems.length;
		}

		@Override
		public Object getItem(int position) {
			return sSpinnerStatusItems[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final StatusItem item = (StatusItem)getItem(position);

			if (view == null) {
				view = mInflater.inflate(R.layout.screen_presence_status_item, null);
			}

			if (item == null) {
				return view;
			}

			((ImageView) view .findViewById(R.id.screen_presence_status_item_imageView))
					.setImageResource(item.mDrawableId);
			((TextView) view.findViewById(R.id.screen_presence_status_item_textView))
				.setText(item.mText);

			return view;
		}
	}
	
	
	
	
	private class Preview extends SurfaceView implements SurfaceHolder.Callback {
		//private static final String TAG = "Preview";

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
			
			try {
				camera = Camera.open();
				camera.setPreviewDisplay(holder);
				
//				camera.setPreviewCallback(new PreviewCallback() {
//
//					public void onPreviewFrame(byte[] data, Camera arg1) {
//						FileOutputStream outStream = null;
//						try {
//							outStream = new FileOutputStream(String.format(
//									"/sdcard/%d.jpg", System.currentTimeMillis()));
//							outStream.write(data);
//							outStream.close();
//							Log.d(TAG, "onPreviewFrame - wrote bytes: "
//									+ data.length);
//						} catch (FileNotFoundException e) {
//							e.printStackTrace();
//						} catch (IOException e) {
//							e.printStackTrace();
//						} finally {
//						}
//						Preview.invalidate();
//					}
//				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// Surface will be destroyed when we return, so stop the preview.
			// Because the CameraDevice object is not a shared resource, it's very
			// important to release it when the activity is paused.
			camera.stopPreview();
			camera.release();
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
	}



//	@Override
//	public void onPictureTaken(byte[] data, Camera camera) {
//		int i = 0;
//		i++;
//	}
}
