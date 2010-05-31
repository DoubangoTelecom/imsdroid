package org.doubango.imsdroid.Sevices.Impl;

import java.util.HashMap;

import org.doubango.imsdroid.Main;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Screens.Screen;
import org.doubango.imsdroid.Screens.ScreenAbout;
import org.doubango.imsdroid.Screens.ScreenChatQueue;
import org.doubango.imsdroid.Screens.ScreenContacts;
import org.doubango.imsdroid.Screens.ScreenFileTransferQueue;
import org.doubango.imsdroid.Screens.ScreenGeneral;
import org.doubango.imsdroid.Screens.ScreenHistory;
import org.doubango.imsdroid.Screens.ScreenHome;
import org.doubango.imsdroid.Screens.ScreenIdentity;
import org.doubango.imsdroid.Screens.ScreenMessaging;
import org.doubango.imsdroid.Screens.ScreenNatt;
import org.doubango.imsdroid.Screens.ScreenNetwork;
import org.doubango.imsdroid.Screens.ScreenOptions;
import org.doubango.imsdroid.Screens.ScreenOptionsContacts;
import org.doubango.imsdroid.Screens.ScreenPresence;
import org.doubango.imsdroid.Screens.ScreenQoS;
import org.doubango.imsdroid.Screens.ScreenRegistrations;
import org.doubango.imsdroid.Screens.ScreenSecurity;
import org.doubango.imsdroid.Screens.Screen.SCREEN_ID;
import org.doubango.imsdroid.Services.IScreenService;

import android.app.ActivityGroup;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScreenService extends Service implements IScreenService {

	private Screen currentScreen;
	private final HashMap<String, Screen> screens;
	private int lastScreensIndex = -1; // ring cursor
	private final Screen[] lastScreens =  new Screen[]{ // ring
    		null,
    		null,
    		null,
    		null
	};

	public ScreenService() {
		this.screens = new HashMap<String, Screen>();
	}

	public boolean start() {
		return true;
	}

	public boolean stop() {
		return true;
	}	

	public boolean back(){
		Screen screen;
		
		// no screen in the stack
		if(this.lastScreensIndex < 0){
			return true;
		}
		
		// zero is special case
		if(this.lastScreensIndex == 0){
			if((screen = this.lastScreens[this.lastScreens.length-1]) == null){
				// goto home
				return this.show(Screen.SCREEN_ID.HOME_I);
			}
			else{
				return this.show(screen);
			}
		}
		// all other cases
		screen = this.lastScreens[this.lastScreensIndex-1];
		this.lastScreens[this.lastScreensIndex-1] = null;
		this.lastScreensIndex--;
		if(screen == null){
			return this.show(SCREEN_ID.HOME_I);
		}
		else{
			return this.show(screen);
		}
	}
	
	public boolean show(Screen screen) {
		if (screen == null) {
			Log.e(this.getClass().getCanonicalName(), "Null Screen");
			return false;
		}

		Main mainActivity = ServiceManager.getMainActivity();
		
		Intent intent = new Intent(mainActivity, screen.getClass());
		View view = mainActivity.getLocalActivityManager().startActivity(
				screen.getId().toString(), intent).getDecorView();
		
		LinearLayout layout = (LinearLayout) mainActivity
				.findViewById(R.id.main_linearLayout_principal);
		layout.removeAllViews();
		layout.addView(view);
		
		// title
		mainActivity.setScreenTitle(screen.getScreenTitle());
		
		// add to stack
		this.lastScreens[(++this.lastScreensIndex % this.lastScreens.length)] = screen;
		this.lastScreensIndex %= this.lastScreens.length;
		
		// update current screen
		this.currentScreen = screen;
		
		return true;
	}

	public boolean show(String id) {
		Screen screen;

		/* already exist? */
		if ((screen = this.screens.get(id)) == null) {
			/* does not exist: is it a well-known screen? */
			Screen.SCREEN_ID type;
			try {
				type = Screen.SCREEN_ID.valueOf(id);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			}
			catch(NullPointerException e){
				e.printStackTrace();
				return false;
			}

			switch (type) {
				case ABOUT_I:
					screen = new ScreenAbout();
					break;
				case CHAT_QUEUE_I:
					screen = new ScreenChatQueue();
					break;
				case CONTACTS_I:
					screen = new ScreenContacts();
					break;
				case CONTACTS_OPTIONS_I:
					screen = new ScreenOptionsContacts();
					break;
				case FILE_TRANSFER_QUEUE_I:
					screen = new ScreenFileTransferQueue();
					break;
				case GENERAL_I:
					screen = new ScreenGeneral();
					break;
				case HISTORY_I:
					screen = new ScreenHistory();
					break;
				case HOME_I:
					screen = new ScreenHome();
					break;
				case IDENTITY_I:
					screen = new ScreenIdentity();
					break;
				case MESSAGING_I:
					screen = new ScreenMessaging();
					break;
				case NATT_I:
					screen = new ScreenNatt();
					break;
				case NETWORK_I:
					screen = new ScreenNetwork();
					break;
				case OPTIONS_I:
					screen = new ScreenOptions();
					break;
				case PRESENCE_I:
					screen = new ScreenPresence();
					break;
				case QOS_I:
					screen = new ScreenQoS();
					break;
				case REGISTRATIONS_I:
					screen = new ScreenRegistrations();
					break;
				case SECURITY_I:
					screen = new ScreenSecurity();
					break;
			}

			/* adds the newly created well-know screen */
			if (screen != null) {
				this.screens.put(screen.getId().toString(), screen);
			}
		}

		if (screen == null) {
			Log.e(this.getClass().getCanonicalName(), String.format(
					"Failed to retrieve the Screen with id=%s", id));
			return false;
		} else {
			return this.show(screen);
		}
	}

	public boolean show(Screen.SCREEN_ID id) {
		return this.show(id.toString());
	}
	
	public void setProgressInfoText(String text)
	{
		ServiceManager.getMainActivity().setProgressInfo(text);
	}
	
	public Screen getCurrentScreen(){
		return this.currentScreen;
	}
}
