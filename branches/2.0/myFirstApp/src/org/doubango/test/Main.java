package org.doubango.test;

import org.doubango.ngn.NgnEngine;

import android.app.Activity;
import android.os.Bundle;

public class Main extends Activity {
    
	// Load native libraries (the shared libraries are from 'android-ngn-stack' project)
	static {
		System.load(String.format("/data/data/%s/lib/libtinyWRAP.so", Main.class.getPackage().getName()));
		NgnEngine.initialize();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}