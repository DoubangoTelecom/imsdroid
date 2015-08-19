/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
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
*/
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
