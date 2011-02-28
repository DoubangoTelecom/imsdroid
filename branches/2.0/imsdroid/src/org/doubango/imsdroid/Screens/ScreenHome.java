package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.ServiceManager;
import org.doubango.imsdroid.Screens.BaseScreen.SCREEN_TYPE;
import org.doubango.imsdroid.Utils.StringUtils;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;


public class ScreenHome extends TabActivity 
implements 
IBaseScreen,
OnTabChangeListener
{
	private static String TAG = ScreenHome.class.getCanonicalName();
	
	private static final int MENU_EXIT = 0;
	private static final int MENU_SETTINGS = 1;
	
	private TabHost mTabHost;
	
	public ScreenHome() {
		super();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_home);
		
		Resources res = getResources();
		mTabHost = getTabHost();
	    TabHost.TabSpec spec;
	    Intent intent;
	    
	    // History
	    intent = new Intent().setClass(this, ScreenTabHistory.class);
	    spec = mTabHost.newTabSpec("history").setIndicator(StringUtils.empty(),
	                      res.getDrawable(R.drawable.history_48))
	                  .setContent(intent);
	    mTabHost.addTab(spec);
	    
	    // dialer
	    intent = new Intent().setClass(this, ScreenTabDialer.class);
	    spec = mTabHost.newTabSpec("dialer").setIndicator(StringUtils.empty(),
	                      res.getDrawable(R.drawable.dialer_48))
	                  .setContent(intent);
	    mTabHost.addTab(spec);
	    
	    // contacts
	    intent = new Intent().setClass(this, ScreenTabContacts.class);
	    spec = mTabHost.newTabSpec("contacts").setIndicator(StringUtils.empty(),
	                      res.getDrawable(R.drawable.eab2_48))
	                  .setContent(intent);
	    mTabHost.addTab(spec);
	    
	    // Messages
	    intent = new Intent().setClass(this, ScreenTabMessages.class);
	    spec = mTabHost.newTabSpec("messages").setIndicator(StringUtils.empty(),
	                      res.getDrawable(R.drawable.chat_48))
	                  .setContent(intent);
	    mTabHost.addTab(spec);
	    
	    mTabHost.setCurrentTab(2);
	    mTabHost.setOnTabChangedListener(this);
	}

	@Override
	protected void onDestroy() {

       super.onDestroy();
	}
	
	@Override
	public String getId() {
		return TAG;
	}

	@Override
	public SCREEN_TYPE getType(){
		return SCREEN_TYPE.HOME_T;
	}
	
	@Override
	public boolean hasMenu() {
		return true;
	}

	@Override
	public boolean hasBack(){
		return true;
	}
	
	@Override
	public boolean back(){
		return false;
	}
	
	@Override
	public boolean createOptionsMenu(Menu menu) {
		menu.add(0, ScreenHome.MENU_SETTINGS, 0, "Settings");
		/*MenuItem itemExit =*/ menu.add(0, ScreenHome.MENU_EXIT, 0, "Exit");
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case ScreenHome.MENU_EXIT:
				ServiceManager.getMainActivity().exit();
				break;
			case ScreenHome.MENU_SETTINGS:
				ServiceManager.getScreenService().show(ScreenSettings.class);
				break;
		}
		return true;
	}
	
	@Override
	public void onTabChanged(String tabId) {
		//setTabColor(mTabHost);
	}
}
