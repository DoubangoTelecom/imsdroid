package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.R;

import android.os.Bundle;
import android.widget.TextView;

public class ScreenAbout extends BaseScreen {
	private static final String TAG = ScreenAbout.class.getCanonicalName();
	
	public ScreenAbout() {
		super(SCREEN_TYPE.ABOUT_T, TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_about);
                
        
        TextView textView = (TextView)this.findViewById(R.id.screen_about_textView_copyright);
        String copyright = this.getString(R.string.copyright);
		textView.setText(String.format(copyright,
				IMSDroid.getVersionName(), this.getString(R.string.doubango_revision)));
	}
}
