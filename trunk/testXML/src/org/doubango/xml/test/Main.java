package org.doubango.xml.test;

import org.doubango.imsdroid.Model.TestContacts;

import android.app.Activity;
import android.os.Bundle;

public class Main extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        /* reginfo */
        //TestReginfo.run();
        
        /* contacts */
        TestContacts.run();
    }
}