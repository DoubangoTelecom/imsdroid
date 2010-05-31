/*
 * Performance: http://developer.android.com/guide/practices/design/performance.html
 * Services: http://developer.android.com/reference/android/app/Service.html
 * Activity LifeCycle: http://developer.android.com/reference/android/app/Activity.html
 * Application Fundamentals: http://developer.android.com/guide/topics/fundamentals.html#acttask
 * Activity Launch Modes: http://www.justinlee.sg/2010/03/13/android-activity-launch-modes/
 * Hello Views: http://developer.android.com/guide/tutorials/views/index.html
 * Dialogs: http://developer.android.com/guide/topics/ui/dialogs.html#AlertDialog
 * Hard Keys: http://android-developers.blogspot.com/2009/12/back-and-other-hard-keys-three-stories.html
 * 
 * 
 * SMS Manager: http://www.damonkohler.com/2009/02/android-recipes.html
 */
package org.doubango.imsdroid;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Screens.Screen;
import org.doubango.imsdroid.Screens.Screen.SCREEN_ID;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.RegistrationEventArgs;
import org.doubango.imsdroid.events.RegistrationEventTypes;

import android.app.ActivityGroup;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

public class Main extends ActivityGroup
implements IRegistrationEventHandler
{
    private final ISipService sipService;
    private final IScreenService screenService;
    private final IConfigurationService configurationService;
    
    private volatile int connStateDrawableId = -1;
    private volatile String progressInfoText = "";
    
    private TextView tvTitle;
    private TextView tvDisplayName;
    private TextView tvFreeText;
    private TextView tvProgressInfo;
    private ImageView ivConnState;
    private ImageView ivStatus;
    private ImageView ivAvatar;
   
    private final Handler handler;
    
    public Main()
    {
    	super();
    	
    	// Sets main activity (should be done before starting services)
    	ServiceManager.setMainActivity(this);    
    	
    	// Gets services to avoid calling ServiceManager.get*() each time we want one
    	this.sipService = ServiceManager.getSipService();
    	this.screenService = ServiceManager.getScreenService();	
    	this.configurationService = ServiceManager.getConfigurationService();
    	
    	this.handler = new Handler();
    }
    
    private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
		}
    };

    /* ===================== Activity ========================*/
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Gets controls
        this.tvTitle = (TextView)this.findViewById(R.id.main_textView_title);
        this.tvDisplayName = (TextView)this.findViewById(R.id.main_textView_displayname);
        this.tvFreeText = (TextView)this.findViewById(R.id.main_textView_freetext);
        this.tvProgressInfo = (TextView)this.findViewById(R.id.main_textView_progressinfo);
        this.ivConnState = (ImageView)this.findViewById(R.id.main_imageView_connstate);
        this.ivStatus = (ImageView)this.findViewById(R.id.main_imageView_status);
        this.ivAvatar = (ImageView)this.findViewById(R.id.main_imageView_avatar);
        
        // starts our services
        if(!ServiceManager.start()){
        	Log.e(this.getClass().getName(), "Failed to start services");
        	return; // Should exit
        }
        
        // bind to service manager
        this.bindService(new Intent(this, ServiceManager.class),
                this.connection, Context.BIND_AUTO_CREATE);
        
        // set values
		this.tvDisplayName.setText(this.configurationService.getString(
				CONFIGURATION_SECTION.IDENTITY,
				CONFIGURATION_ENTRY.DISPLAY_NAME, 
				this.configurationService.getString(CONFIGURATION_SECTION.IDENTITY,
								CONFIGURATION_ENTRY.IMPU,
								Configuration.DEFAULT_IMPU)));
        this.tvFreeText.setText(this.configurationService.getString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.FREE_TEXT, Configuration.DEFAULT_RCS_FREE_TEXT));
        
        // add event handlers
        this.sipService.addRegistrationEventHandler(this);
        
        /* shows the home screen */
        this.screenService.show(Screen.SCREEN_ID.HOME_I);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.screenService.back();
			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0){
			ServiceManager.getScreenService().show(SCREEN_ID.HOME_I);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
    
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("SCREEN_ID", this.screenService.getCurrentScreen().getId().toString());
		outState.putString("progressInfoText", this.progressInfoText);
		outState.putInt("connStateDrawableId", this.connStateDrawableId);
		
		super.onSaveInstanceState(outState);
	}
	
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String ID = savedInstanceState.getString("SCREEN_ID");
		if(ID != null){
			this.screenService.show(ID);
		}
		if((this.connStateDrawableId = savedInstanceState.getInt("connStateDrawableId")) != -1){
			Main.this.ivConnState.setImageDrawable(getResources().getDrawable(Main.this.connStateDrawableId));
		}
		this.progressInfoText = savedInstanceState.getString("progressInfoText");
		this.screenService.setProgressInfoText(this.progressInfoText);
	}

	protected void onDestroy() {        
        // remove event handlers : do it after stop() to continue to receive Sip events
        this.sipService.removeRegistrationEventHandler(this);
        
     // unbind to service manager
        this.unbindService(this.connection);
        
        super.onDestroy();
	}

	/* ===================== Public functions ======================== */
	public void setScreenTitle(String value){
		this.tvTitle.setText(value);
	}
	
	public void setProgressInfo(String value){
		this.tvProgressInfo.setText(value);
	}
	
	public void setDisplayName(String value){
		this.tvDisplayName.setText(value);
	}
	
	public void setFreeText(String value){
		this.tvFreeText.setText(value);
	}
	
	public void exit(){
		// stops all services
        if(!ServiceManager.stop()){
        	Log.e(this.getClass().getName(), "Failed to stop services");
        }
		this.finish();
	}
	
	/* ===================== UI Events ======================== */	
	/*private OnClickListener ibBack_OnClickListener = new OnClickListener() {
		public void onClick(View v) {
			Main.this.screenService.back();
			Main.this.screenService.setProgressInfoText(new Random().toString());
		}
	};*/
	
	
    /* ===================== Sip Events ========================*/
	
	public boolean onRegistrationEvent(Object sender, RegistrationEventArgs e) {
		Log.i(this.getClass().getName(), "onRegistrationEvent");
		
		final RegistrationEventTypes type = e.getType();
		final short code = e.getSipCode();
		final String phrase = e.getPhrase();
		
		switch(type){
			case REGISTRATION_OK:
			this.handler.post(new Runnable() {
				public void run() {
					Main.this.connStateDrawableId = R.drawable.bullet_ball_glass_green_16;
					Main.this.progressInfoText = String.format("Registration: %s", phrase);
					Main.this.ivConnState.setImageDrawable(getResources().getDrawable(Main.this.connStateDrawableId));
					Main.this.screenService.setProgressInfoText(Main.this.progressInfoText);
				}});
				break;
			
			case UNREGISTRATION_OK:
				this.handler.post(new Runnable() {
					public void run() {
						Main.this.connStateDrawableId = R.drawable.bullet_ball_glass_red_16;
						Main.this.progressInfoText = String.format("Unregistration: %s", phrase);
						Main.this.ivConnState.setImageDrawable(getResources().getDrawable(Main.this.connStateDrawableId));
						Main.this.screenService.setProgressInfoText(Main.this.progressInfoText);
					}});
				break;
				
			case REGISTRATION_INPROGRESS:
			case UNREGISTRATION_INPROGRESS:
				this.handler.post(new Runnable() {
					public void run() {
						Main.this.connStateDrawableId = R.drawable.bullet_ball_glass_grey_16;
						Main.this.progressInfoText = String.format("Trying to %s...", (type == RegistrationEventTypes.REGISTRATION_INPROGRESS) ? "register" : "unregister");
						Main.this.ivConnState.setImageDrawable(getResources().getDrawable(Main.this.connStateDrawableId));
						Main.this.screenService.setProgressInfoText(Main.this.progressInfoText);
					}});
				break;
				
			case REGISTRATION_NOK:
			case UNREGISTRATION_NOK:
			default:
			{
				Log.d(this.getClass().getName(), String.format("Registration/unregistration failed. code=%d and phrase=%s", code, phrase));
				break;
			}
		}
		
		return true;
	}
	
	
	/* ===================== UI Actions ======================== */	
	
	
	
	
	static {
		try {
			//System.loadLibrary("tinyWRAP");
			System.load(String.format("/data/data/%s/lib/libtinyWRAP.so", Main.class
					.getPackage().getName()));

		} catch (UnsatisfiedLinkError e) {
			Log.e(Main.class.getCanonicalName(),
					"Native code library failed to load.\n" + e.getMessage());
		} catch (Exception e) {
			Log.e(Main.class.getCanonicalName(),
					"Native code library failed to load.\n" + e.getMessage());
		}
	}
}