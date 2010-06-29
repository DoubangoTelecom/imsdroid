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


/*
 * Performance: http://developer.android.com/guide/practices/design/performance.html
 * Services: http://developer.android.com/reference/android/app/Service.html
 * Activity LifeCycle: http://developer.android.com/reference/android/app/Activity.html
 * Application Fundamentals: http://developer.android.com/guide/topics/fundamentals.html#acttask
 * Activity Launch Modes: http://www.justinlee.sg/2010/03/13/android-activity-launch-modes/
 * Hello Views: http://developer.android.com/guide/tutorials/views/index.html
 * Dialogs: http://developer.android.com/guide/topics/ui/dialogs.html#AlertDialog
 * Hard Keys: http://android-developers.blogspot.com/2009/12/back-and-other-hard-keys-three-stories.html
 * Menus: http://developer.android.com/guide/topics/ui/menus.html
 * Toasts: http://developer.android.com/guide/topics/ui/notifiers/toasts.html
 * 
 * SMS Manager: http://www.damonkohler.com/2009/02/android-recipes.html
 * 
 * Audio Recorder/Trac: http://stackoverflow.com/questions/2416365/android-how-to-add-my-own-audio-codec-to-audiorecord
 * 						http://androidforums.com/android-media/8974-can-system-sleep-while-audiorecord-recording.html
 * 						-->http://groups.google.co.in/group/android-developers/browse_thread/thread/1bf74961d3480bde
 * Video Recoder:		onPreviewFrame(byte[] data, Camera camera) 
 * 
 * Media Formats: http://developer.android.com/guide/appendix/media-formats.html
 * Android FFMPeg: http://oo-androidnews.blogspot.com/2010/02/ffmpeg-and-androidmk.html
 * 					http://groups.google.com/group/android-ndk/browse_thread/thread/f25d5c7f519bf0c5
 * 
 */
package org.doubango.imsdroid;

import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Screens.ScreenHome;
import org.doubango.imsdroid.Screens.ScreenPresence;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.RegistrationEventArgs;
import org.doubango.imsdroid.events.RegistrationEventTypes;
import org.doubango.imsdroid.sip.PresenceStatus;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class Main extends ActivityGroup
implements IRegistrationEventHandler
{
    private final ISipService sipService;
    private final IScreenService screenService;
    private final IConfigurationService configurationService;
    
    private volatile String progressInfoText = "";
    
    //private RelativeLayout rlTop;
    private TextView tvTitle;
    private TextView tvDisplayName;
    private TextView tvFreeText;
    private TextView tvProgressInfo;
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
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
		}
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
		}
    };

    /* ===================== Activity ========================*/
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        // Gets controls
        //this.rlTop = (RelativeLayout)this.findViewById(R.id.main_relativeLayout_top);
        this.tvTitle = (TextView)this.findViewById(R.id.main_textView_title);
        this.tvDisplayName = (TextView)this.findViewById(R.id.main_textView_displayname);
        this.tvFreeText = (TextView)this.findViewById(R.id.main_textView_freetext);
        this.tvProgressInfo = (TextView)this.findViewById(R.id.main_textView_progressinfo);
        this.ivStatus = (ImageView)this.findViewById(R.id.main_imageView_status);
        this.ivAvatar = (ImageView)this.findViewById(R.id.main_imageView_avatar);
        
        // starts our services (will do nothing if already started)
        if(!ServiceManager.start()){
        	Log.e(this.getClass().getName(), "Failed to start services");
        	return; // Should exit
        }
        
        // bind to service manager
        this.bindService(new Intent(this, ServiceManager.class),
                this.connection, Context.BIND_AUTO_CREATE);
        
        // set values
        //this.rlTop.setVisibility(this.sipService.isRegistered() ? View.VISIBLE : View.INVISIBLE);
		this.tvDisplayName.setText(this.configurationService.getString(
				CONFIGURATION_SECTION.IDENTITY, CONFIGURATION_ENTRY.DISPLAY_NAME, Configuration.DEFAULT_DISPLAY_NAME));
        this.tvFreeText.setText(this.configurationService.getString(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.FREE_TEXT, Configuration.DEFAULT_RCS_FREE_TEXT));
        this.ivStatus.setImageResource(ScreenPresence.getStatusDrawableId(Enum.valueOf(PresenceStatus.class, this.configurationService.getString(
						CONFIGURATION_SECTION.RCS,
						CONFIGURATION_ENTRY.STATUS,
						Configuration.DEFAULT_RCS_STATUS.toString()))));
        
        // set event listeners
        this.ivStatus.setOnClickListener(this.ivStatus_OnClickListener);
        
        // add event handlers
        this.sipService.addRegistrationEventHandler(this);
        
        /* shows the home screen */
        this.screenService.show(ScreenHome.class);
    }

	protected void onDestroy() {
        // remove event handlers : do it after stop() to continue to receive Sip events
        this.sipService.removeRegistrationEventHandler(this);
        
        // unbind to service manager
        this.unbindService(this.connection);
        
        super.onDestroy();
	}
	
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.screenService.back();
			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0){
			if(!this.screenService.getCurrentScreen().haveMenu()){
				this.screenService.show(ScreenHome.class);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return this.screenService.getCurrentScreen().onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.screenService.getCurrentScreen().onOptionsItemSelected(item);
	}
    
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("SCREEN_ID", this.screenService.getCurrentScreen().getId().toString());
		outState.putString("progressInfoText", this.progressInfoText);
		
		super.onSaveInstanceState(outState);
	}
	
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String ID = savedInstanceState.getString("SCREEN_ID");
		if(ID != null){
			this.screenService.show(ID);
		}
		
		//this.rlTop.setVisibility(this.sipService.isRegistered() ? View.VISIBLE : View.INVISIBLE);
		this.progressInfoText = savedInstanceState.getString("progressInfoText");
		this.screenService.setProgressInfoText(this.progressInfoText);
		this.ivStatus.setImageResource(ScreenPresence.getStatusDrawableId(Enum.valueOf(PresenceStatus.class, this.configurationService.getString(
				CONFIGURATION_SECTION.RCS,
				CONFIGURATION_ENTRY.STATUS,
				Configuration.DEFAULT_RCS_STATUS.toString()))));
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
	
	public void setStatus(int drawableId){
		this.ivStatus.setImageResource(drawableId);
	}
	
	public void exit(){
		// stops all services
        if(!ServiceManager.stop()){
        	Log.e(this.getClass().getName(), "Failed to stop services");
        }
		this.finish();
	}
	
	/* ===================== UI Events ======================== */	
	private OnClickListener ivStatus_OnClickListener = new OnClickListener() {
		public void onClick(View v) {
			Main.this.screenService.show(ScreenPresence.class);
		}
	};
	
	
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
					Main.this.progressInfoText = String.format("Registration: %s", phrase);
					Main.this.screenService.setProgressInfoText(Main.this.progressInfoText);
					
					//Main.this.rlTop.setVisibility(View.VISIBLE);
					ServiceManager.showRegistartionNotif(R.drawable.bullet_ball_glass_green_16, "You are connected");
				}});
				break;
			
			case UNREGISTRATION_OK:
				this.handler.post(new Runnable() {
					public void run() {
						Main.this.progressInfoText = String.format("Unregistration: %s", phrase);
						Main.this.screenService.setProgressInfoText(Main.this.progressInfoText);
						
						ServiceManager.showRegistartionNotif(R.drawable.bullet_ball_glass_red_16, "You are disconnected");
						if(!Main.this.screenService.getCurrentScreen().getId().equals(ScreenHome.class.getCanonicalName())){
							Main.this.screenService.show(ScreenHome.class);
						}
					}});
				break;
				
			case REGISTRATION_INPROGRESS:
			case UNREGISTRATION_INPROGRESS:
				this.handler.post(new Runnable() {
					public void run() {
						Main.this.progressInfoText = String.format("Trying to %s...", (type == RegistrationEventTypes.REGISTRATION_INPROGRESS) ? "register" : "unregister");
						Main.this.screenService.setProgressInfoText(Main.this.progressInfoText);
						ServiceManager.showRegistartionNotif(R.drawable.bullet_ball_glass_grey_16, String.format("Trying to %s...", (type == RegistrationEventTypes.REGISTRATION_INPROGRESS) ? "connect" : "disconnect"));
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