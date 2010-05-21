/*
 * Performance: http://developer.android.com/guide/practices/design/performance.html
 * Activity LifeCycle: http://developer.android.com/reference/android/app/Activity.html
 * 
 * 
 * SMS Manager: http://www.damonkohler.com/2009/02/android-recipes.html
 */
package org.doubango.imsdroid;

import org.doubango.imsdroid.Screens.Screen;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.imsdroid.events.INotifPresEventhandler;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.NotifPresEventArgs;
import org.doubango.imsdroid.events.RegistrationEventArgs;

import android.app.ActivityGroup;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class Main extends ActivityGroup
implements IRegistrationEventHandler,
INotifPresEventhandler
{
    private final ISipService SipService;
    private final IScreenService ScreenService;
    
    private ImageButton ibHome;
    private TextView tvProgressInfo;
    private Spinner spPresenceStatus;
    private ImageView ivConnState;
    private ImageView ivHourGlass;
    private ImageView ivCallState;
    
    public Main()
    {
    	super();
    	
    	// Gets services to avoid calling ServiceManager.get*() each time we want one
    	this.SipService = ServiceManager.getSipService();
    	this.ScreenService = ServiceManager.getScreenService();
    	
    	// Sets main activity
    	this.ScreenService.setMainActivity(this);
    }
    
    /* ===================== Activity ========================*/
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Gets controls
        this.ibHome = (ImageButton)this.findViewById(R.id.main_imageButton_home);
        this.tvProgressInfo = (TextView)this.findViewById(R.id.main_textView_progressinfo);
        this.spPresenceStatus = (Spinner)this.findViewById(R.id.main_spinner_status);
        this.ivConnState = (ImageView)this.findViewById(R.id.main_imageView_connstate);
        this.ivHourGlass = (ImageView)this.findViewById(R.id.main_imageView_hourclass);
        this.ivCallState = (ImageView)this.findViewById(R.id.main_imageView_callstate);
        
        // Sets controls listners
        this.ibHome_setOnClickListener();
        
        // Starts services
        if(!ServiceManager.start()){
        	Log.e(this.getClass().getName(), "Failed to start services");
        	return; // Should exit
        }
        
        // add event handlers
        this.SipService.addRegistrationEventHandler(this);
        this.SipService.addNotifPresEventhandler(this);
        
        /* shows the home screen */
        this.ScreenService.show(Screen.SCREEN_ID_HOME);
    }
    
	protected void onDestroy() {
		// Stops services
        if(!ServiceManager.stop())
        {
        	Log.e(this.getClass().getName(), "Failed to stop services");
        }
        
        // remove event handlers : do it after stop() to continue to receive Sip events
        this.SipService.removeRegistrationEventHandler(this);
        this.SipService.removeNotifPresEventhandler(this);
        
        super.onDestroy();
	}

	/* ===================== UI Events ======================== */
	private void ibHome_setOnClickListener(){
		this.ibHome.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Main.this.ScreenService.show(Screen.SCREEN_ID_HOME);
			}
		});
	}
	
    
    /* ===================== Sip Events ========================*/
    
	public boolean onRegistrationEvent(Object sender, RegistrationEventArgs e) {
		Log.i(this.getClass().getName(), "onRegistrationEvent");
		return true;
	}

	public boolean onNotifPresEvent(Object sender, NotifPresEventArgs e) {
		Log.i(this.getClass().getName(), "onNotifPresEvent");
		return true;
	}
}