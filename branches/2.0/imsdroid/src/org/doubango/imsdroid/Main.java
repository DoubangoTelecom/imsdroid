package org.doubango.imsdroid;

import org.doubango.imsdroid.Screens.BaseScreen.SCREEN_TYPE;
import org.doubango.imsdroid.Screens.IBaseScreen;
import org.doubango.imsdroid.Screens.ScreenAV;
import org.doubango.imsdroid.Screens.ScreenHome;
import org.doubango.imsdroid.Screens.ScreenSplash;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Sip.MyAVSession;
import org.doubango.imsdroid.Utils.StringUtils;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class Main extends ActivityGroup {
	private static String TAG = Main.class.getCanonicalName();
	
	public static final int ACTION_NONE = 0;
	public static final int ACTION_RESTORE_LAST_STATE = 1;
	public static final int ACTION_SHOW_AVSCREEN = 2;
	
	private static final int RC_SPLASH = 0;
	
	private Handler mHanler;
	
	public Main(){
		super();
		
		// Sets main activity (should be done before starting services)
    	ServiceManager.setMainActivity(this);
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        mHanler = new Handler();
        
        if(!ServiceManager.isStarted()){
        	startActivityForResult(new Intent(this, ScreenSplash.class), Main.RC_SPLASH);
        }
        
        Bundle bundle = savedInstanceState;
        final IScreenService screenService = ServiceManager.getScreenService();
        if(bundle == null){
	        Intent intent = getIntent();
	        bundle = intent == null ? null : intent.getExtras();
        }
        if(bundle != null && bundle.getInt("action", Main.ACTION_NONE) != Main.ACTION_NONE){
        	handleAction(bundle);
        }
        else if(screenService != null){
        	screenService.show(ScreenHome.class);
        }
        
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }
    
    @Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		Bundle bundle = intent.getExtras();
		if(bundle != null){
			handleAction(bundle);
		}
	}
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(ServiceManager.getScreenService().getCurrentScreen().hasMenu()){
			return ServiceManager.getScreenService().getCurrentScreen().createOptionsMenu(menu);
		}
		
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		if(ServiceManager.getScreenService().getCurrentScreen().hasMenu()){
			menu.clear();
			return ServiceManager.getScreenService().getCurrentScreen().createOptionsMenu(menu);
		}
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		IBaseScreen baseScreen = ServiceManager.getScreenService().getCurrentScreen();
		if(baseScreen instanceof Activity){
			return ((Activity)baseScreen).onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		final IScreenService screenService = ServiceManager.getScreenService();
		if(screenService == null){
			super.onSaveInstanceState(outState);
			return;
		}
		
		IBaseScreen screen = screenService.getCurrentScreen();
		if(screen != null){
			outState.putInt("action", Main.ACTION_RESTORE_LAST_STATE);
			outState.putString("screen-id", screen.getId());
			outState.putString("screen-type", screen.getType().toString());
		}
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		this.handleAction(savedInstanceState);
	}
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Log.d(TAG, "onActivityResult("+requestCode+","+resultCode+")");
		if(resultCode == RESULT_OK){
			if(requestCode == Main.RC_SPLASH){
				Log.d(TAG, "Result from splash screen");
			}
		}
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	final IScreenService screenService = ServiceManager.getScreenService();
		final IBaseScreen currentScreen = screenService.getCurrentScreen();
		if(currentScreen != null){
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && currentScreen.getType() != SCREEN_TYPE.HOME_T) {
				if(currentScreen.hasBack()){
					if(!currentScreen.back()){
						super.onKeyDown(keyCode, event);
					}
				}
				else {
					screenService.back();
				}
				return true;
			}
			else if(keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0){
				if(!currentScreen.hasMenu()){
					screenService.show(ScreenHome.class);
					return true;
				}
				else if(currentScreen instanceof Activity){
					return ((Activity)currentScreen).onKeyDown(keyCode, event);
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
    
    public void exit(){
    	mHanler.post(new Runnable() {
			public void run() {
				if (!ServiceManager.stop()) {
					Log.e(TAG, "Failed to stop services");
				}				
				finish();
			}
		});
	}
    
    private void handleAction(Bundle bundle){
		final String id;
		switch(bundle.getInt("action", Main.ACTION_NONE)){
			// Default or ACTION_RESTORE_LAST_STATE
			default:
			case Main.ACTION_RESTORE_LAST_STATE:
				id = bundle.getString("screen-id");
				final String screenTypeStr = bundle.getString("screen-type");
				final SCREEN_TYPE screenType = StringUtils.isNullOrEmpty(screenTypeStr) ? SCREEN_TYPE.HOME_T :
						SCREEN_TYPE.valueOf(screenTypeStr);
				switch(screenType){
					case AV_T:
						ServiceManager.getScreenService().show(ScreenAV.class, id);
						break;
					default:
						if(!ServiceManager.getScreenService().show(id)){
							ServiceManager.getScreenService().show(ScreenHome.class);
						}
						break;
				}
				break;
				
			// Show Audio/Video Calls
			case Main.ACTION_SHOW_AVSCREEN:
				id = bundle.getString("session-id");
				final MyAVSession avSession = StringUtils.isNullOrEmpty(id) ? MyAVSession.getFirstActiveCallAndNot(-1):
					MyAVSession.getSession(StringUtils.parseLong(id, -1));
				if(avSession != null){
					if(!ServiceManager.getScreenService().show(ScreenAV.class, Long.toString(avSession.getId()))){
						ServiceManager.getScreenService().show(ScreenHome.class);
					}
				}
				break;
		}
	}
}