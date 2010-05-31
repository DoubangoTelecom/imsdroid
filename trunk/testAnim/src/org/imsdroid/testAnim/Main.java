package org.imsdroid.testAnim;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ViewFlipper;

public class Main extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        final Button btn1 = (Button)this.findViewById(R.id.Button01);
        final Button btn2 = (Button)this.findViewById(R.id.Button02);
        
        final ViewFlipper flipper = (ViewFlipper) Main.this.findViewById(R.id.ViewFlipper01);

		Animation s_in = AnimationUtils.loadAnimation(Main.this,
				R.anim.slidein);
		Animation s_out = AnimationUtils.loadAnimation(Main.this,
				R.anim.slideout);
		flipper.setInAnimation(s_in);
		flipper.setOutAnimation(s_out);
		
        btn1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {		
				flipper.showNext();
			}
        });
        
        btn2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {		
				flipper.showPrevious();
			}
        });
    }
}