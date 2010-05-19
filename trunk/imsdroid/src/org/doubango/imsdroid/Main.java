/*
 * Performance: http://developer.android.com/guide/practices/design/performance.html
 * Activity LifeCycle: http://developer.android.com/reference/android/app/Activity.html
 * 
 * 
 * SMS Manager: http://www.damonkohler.com/2009/02/android-recipes.html
 */
package org.doubango.imsdroid;

import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.imsdroid.events.INotifPresEventhandler;
import org.doubango.imsdroid.events.IRegistrationEventHandler;
import org.doubango.imsdroid.events.NotifPresEventArgs;
import org.doubango.imsdroid.events.RegistrationEventArgs;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity
implements IRegistrationEventHandler,
INotifPresEventhandler
{
    private final ISipService SipService;
    
    private Button btnRegister;
    private Button btnSubscribe;
    
    public Main()
    {
    	super();
    	
    	// Gets services to avoid calling ServiceManager.get*() each time we want one
    	this.SipService = ServiceManager.getSipService();
    }
    
    /* ===================== Activity ========================*/
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Gets controls
        this.btnRegister = (Button)this.findViewById(R.id.btnRegister);
        this.btnSubscribe = (Button)this.findViewById(R.id.btnSubscribe);
        
        // Sets Controls events
        this.btnRegister.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Main.this.btnRegister_Click(v);
			}
        });
        this.btnSubscribe.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Main.this.btnSubscribe_Click(v);
			}
        });
        
        // Starts services
        if(!ServiceManager.start())
        {
        	Log.e(this.getClass().getName(), "Failed to start services");
        	return;
        }
        
        // add event handlers
        this.SipService.addRegistrationEventHandler(this);
        this.SipService.addNotifPresEventhandler(this);
    }
    
    @Override
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

	/* ===================== Controls Events ========================*/
    private void btnRegister_Click(View v)
    {
    	this.SipService.onTestRegistrationChanged();
    }
    
    private void btnSubscribe_Click(View v)
    {
    	this.SipService.onTestNotifPresChanged();
    }
    
    /* ===================== Application Events ========================*/
    
	@Override
	public boolean onRegistrationEvent(Object sender, RegistrationEventArgs e) {
		Log.i(this.getClass().getName(), "onRegistrationEvent");
		return true;
	}

	@Override
	public boolean onNotifPresEvent(Object sender, NotifPresEventArgs e) {
		Log.i(this.getClass().getName(), "onNotifPresEvent");
		return true;
	}
}