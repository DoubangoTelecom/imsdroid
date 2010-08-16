package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.Main;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.events.IMsrpEventHandler;
import org.doubango.imsdroid.events.MsrpEventArgs;
import org.doubango.imsdroid.media.MediaType;
import org.doubango.imsdroid.sip.MyMsrpSession;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class ScreenMsrpInc extends Screen implements IMsrpEventHandler {

	private MyMsrpSession msrpSession;
	private KeyguardLock keyguardLock;
	private PowerManager.WakeLock wakeLock;
	
	private static String TAG = ScreenMsrpInc.class.getCanonicalName();
	
	private ImageView ivIcon;
	private TextView tvRemoteParty;
	private TextView tvInfo;
	private Button btAccept;
	private Button btDecline;
	
	public ScreenMsrpInc() {
		super(SCREEN_TYPE.MSRP_INC_T, ScreenMsrpInc.class.getCanonicalName());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_msrp_inc);
		
		 // retrieve id
        this.id = getIntent().getStringExtra("id");
        this.msrpSession = MyMsrpSession.getSession(Long.parseLong(this.id));
        
        /* get controls */
        this.ivIcon = (ImageView) this.findViewById(R.id.screen_msrp_inc_imageView);
        this.tvRemoteParty = (TextView) this.findViewById(R.id.screen_msrp_inc_textView_remote);
        this.tvInfo = (TextView) this.findViewById(R.id.screen_msrp_inc_textView_info);
        this.btAccept = (Button) this.findViewById(R.id.screen_msrp_inc_button_accept);
        this.btDecline = (Button) this.findViewById(R.id.screen_msrp_inc_button_decline);
        
        if(this.msrpSession != null){
        	this.msrpSession.addMsrpEventHandler(this);
        	this.tvRemoteParty.setText(this.msrpSession.getRemoteParty());
        	if(this.msrpSession.getMediaType() == MediaType.FileTransfer){
        		this.tvInfo.setText(String.format("name: %s\nsize: %d", this.msrpSession.getFileName(), this.msrpSession.getFileLength()));
        		this.ivIcon.setImageResource(R.drawable.image_gallery_48);
        	}
        }
        
        this.btAccept.setOnClickListener(this.btAccept_OnClickListener);
		this.btDecline.setOnClickListener(this.btDecline_OnClickListener);
		
		PowerManager pm = (PowerManager) IMSDroid.getContext().getSystemService(Context.POWER_SERVICE);
		this.wakeLock = pm == null ? null : pm.newWakeLock(PowerManager.ON_AFTER_RELEASE | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, ScreenMsrpInc.TAG);
	}

	@Override
	protected void onDestroy() {
		if(this.msrpSession != null){
        	this.msrpSession.removeMsrpEventHandler(this);
        }
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		if(keyguardManager != null && keyguardManager.inKeyguardRestrictedInputMode()){
			if(this.keyguardLock == null){
				this.keyguardLock = keyguardManager.newKeyguardLock(ScreenMsrpInc.TAG);
			}
			this.keyguardLock.disableKeyguard();
		}
		
		if(this.wakeLock !=null && !this.wakeLock.isHeld()){
			this.wakeLock.acquire(3000);
		}
	}
	
	@Override
	protected void onStop() {
		if(this.keyguardLock != null){
			this.keyguardLock.reenableKeyguard();
		}
		//http://groups.google.com/group/android-developers/browse_thread/thread/2a412a925d80a46b/c76b298779e7caec?show_docid=c76b298779e7caec
		//if(this.wakeLock !=null && this.wakeLock.isHeld()){
		//	this.wakeLock.release();
		//}
		super.onStop();
	}
	
	
	private OnClickListener btAccept_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(ScreenMsrpInc.this.msrpSession != null){
				ScreenMsrpInc.this.msrpSession.accept();
				ServiceManager.getScreenService().show(ScreenFileTransferView.class, new Long(ScreenMsrpInc.this.msrpSession.getId()).toString());
			}
		}
	};
	
	private OnClickListener btDecline_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(ScreenMsrpInc.this.msrpSession != null){
				ScreenMsrpInc.this.msrpSession.hangUp();
				ServiceManager.getScreenService().back();
			}
		}
	};
	
	
	public static boolean receiveInvite(MyMsrpSession msrpSession){
		ServiceManager.showContShareNotif(R.drawable.image_gallery_25, "Content Sharing");
		ServiceManager.getSoundService().playNewEvent();
		ServiceManager.getScreenService().bringToFront(Main.ACTION_SHOW_MSRP_INC_SCREEN,
				new String[] {"session-id", new Long(msrpSession.getId()).toString()}
		);
		return true;
	}
	
	/* ========== IMsrpEventHandler =============== */
	
	@Override
	public boolean onMsrpEvent(Object sender, MsrpEventArgs e){

		if(this.msrpSession == null){
			Log.e(ScreenMsrpInc.TAG, "Invalid MSRP session");
			return false;
		}
				
		
		switch(e.getType()){				
			case SUCCESS_200OK:
				break;
				
			case CONNECTED:
			case DISCONNECTED:
				ScreenMsrpInc.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						final boolean gotoHome = ServiceManager.getScreenService().getCurrentScreen().getType() == SCREEN_TYPE.MSRP_INC_T;
						ServiceManager.getScreenService().show(ScreenHome.class);
						if(gotoHome){
							ServiceManager.getScreenService().destroy(ScreenMsrpInc.this.id);
						}
					}
				});
				break;
		}
		
		return true;
	}

	@Override
	public boolean canHandle(long id) {
		return this.msrpSession != null ? (this.msrpSession.getId() == id) : false;
	}
}
