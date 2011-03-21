
package org.doubango.imsdroid.Services;

import org.doubango.imsdroid.Screens.IBaseScreen;
import org.doubango.ngn.services.INgnBaseService;

import android.app.Activity;

public interface IScreenService extends INgnBaseService{
	boolean back();
	boolean bringToFront(int action, String[]... args);
	boolean bringToFront(String[]... args);
	boolean show(Class<? extends Activity> cls, String id);
	boolean show(Class<? extends Activity> cls);
	boolean show(String id);
	void runOnUiThread(Runnable r);
	boolean destroy(String id);
	void setProgressInfoText(String text);
	IBaseScreen getCurrentScreen();
	IBaseScreen getScreen(String id);
}
