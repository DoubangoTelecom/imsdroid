package org.doubango.imsdroid;

import android.app.Activity;
import android.os.Bundle;

public class Main extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        AudioConsumer consumer = new AudioConsumer();
        consumer.start();
        
        int i = 0;
        i++;
    }
}