package org.doubango.imsdroid.Screens;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.imsdroid.events.IMsrpEventHandler;
import org.doubango.imsdroid.events.MsrpEventArgs;
import org.doubango.imsdroid.events.MsrpEventTypes;
import org.doubango.imsdroid.media.MediaType;
import org.doubango.imsdroid.sip.MyMsrpSession;
import org.doubango.imsdroid.utils.UriUtils;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class ScreenFileTransferView extends Screen implements IMsrpEventHandler{
	
	private static String TAG = ScreenFileTransferView.class.getCanonicalName();
	
	private Timer timerSuicide;
	
	private MyMsrpSession msrpSession;
	
	private ImageView ivPreview;
	private ProgressBar progressBar;
	private Button btAccept;
	private Button btAbort;
	private TextView tvInfo;
	private TextView tvByteRange;
	private TextView tvFileName;
	private TextView tvRemoteParty;
	private String format;
	
	private long start;
	private long end;
	private long total;
	
	public ScreenFileTransferView() {
		super(SCREEN_TYPE.FILE_TRANSFER_VIEW_T, ScreenFileTransferView.class.getCanonicalName());
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.screen_file_transfer_view);
		
		// retrieve id
        this.id = getIntent().getStringExtra("id");
        this.msrpSession = MyMsrpSession.getSession(Long.parseLong(this.id));
        
        
        /* get controls */
        this.ivPreview = (ImageView) this.findViewById(R.id.screen_file_trans_view_imageView_preview);
        this.progressBar = (ProgressBar) this.findViewById(R.id.screen_file_trans_view_progressBar);
        this.btAccept = (Button) this.findViewById(R.id.screen_file_trans_view_button_accept);
        this.btAbort = (Button) this.findViewById(R.id.screen_file_trans_view_button_abort);
        this.tvInfo = (TextView) this.findViewById(R.id.screen_file_trans_view_textView_info);
        this.tvByteRange = (TextView) this.findViewById(R.id.screen_file_trans_view_textView_byteRange);
        this.tvRemoteParty = (TextView) this.findViewById(R.id.screen_file_trans_view_textView_remoteParty);
        this.tvFileName = (TextView) this.findViewById(R.id.screen_file_trans_view_textView_cname);
        
        this.progressBar.setMax(100);
        
        if(this.msrpSession != null){
        	this.msrpSession.addMsrpEventHandler(this);
        	this.start = this.msrpSession.getStart();
        	this.end = this.msrpSession.getEnd();
        	this.total = this.msrpSession.getTotal();
        	
        	this.tvRemoteParty.setText(this.msrpSession.getRemoteParty());
    		this.tvInfo.setText(this.msrpSession.isOutgoing()? "Sending Content...": "Receiving Content...");
    		this.btAccept.setVisibility(this.msrpSession.isOutgoing()||this.msrpSession.isConnected()? View.GONE : View.VISIBLE);
    		this.btAbort.setText(this.msrpSession.isConnected()? "Abort" : (this.msrpSession.isOutgoing() ? "Cancel" : "Decline"));
    		final String fileName = this.msrpSession.getFileName();
    		final String filePath = this.msrpSession.getFilePath();
        	if(fileName != null){
        		this.tvFileName.setText(fileName);
        	}
        	if(msrpSession.isOutgoing()){
        		this.format = "%d/%d Bytes sent";
	        	if(filePath != null){
	            	try{
	            		this.ivPreview.setImageURI(new Uri.Builder().path(filePath).build());
	            	}
	            	catch (Exception e) {
	            		this.ivPreview.setImageResource(R.drawable.document_up_128);
					}
	        	}
        	}
        	else{
        		this.format = "%d/%d Bytes received";
        		this.ivPreview.setImageResource(R.drawable.document_down_128);
        	}
        }
        else{
        	Log.e(ScreenFileTransferView.TAG, "Invalid MSRP session");
        }        
        
		this.updateProgressBar();
		
		this.btAccept.setOnClickListener(this.btAccept_OnClickListener);
		this.btAbort.setOnClickListener(this.btAbort_OnClickListener);
	}
	
	
	@Override
	protected void onDestroy() {
		if(this.msrpSession != null){
        	this.msrpSession.removeMsrpEventHandler(this);
        }
		super.onDestroy();
	}	

	private void updateProgressBar(){
		if(this.end >= 0 && this.total>0 && this.end<=this.total){
			this.progressBar.setProgress((int)((100*this.end)/this.total));
			this.tvByteRange.setText(String.format(this.format, this.end, this.total));
			this.progressBar.setIndeterminate(false);
		}
		else{
			this.progressBar.setIndeterminate(true);
		}
	}
	
	private Runnable runnableUpdateProgressBar = new Runnable(){
		@Override
		public void run() {
			ScreenFileTransferView.this.updateProgressBar();
		}
	};
	
	private OnClickListener btAccept_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(ScreenFileTransferView.this.msrpSession != null){
				ScreenFileTransferView.this.msrpSession.accept();
			}
		}
	};
	
	private OnClickListener btAbort_OnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(ScreenFileTransferView.this.msrpSession != null){
				ScreenFileTransferView.this.msrpSession.hangUp();
			}
		}
	};
	
	private TimerTask timerTaskSuicide = new TimerTask(){
		@Override
		public void run() {
			ScreenFileTransferView.this.runOnUiThread(new Runnable() {
				public void run() {
					final Screen currentScreen = ServiceManager.getScreenService().getCurrentScreen();
						if(currentScreen != null && currentScreen.getType() == SCREEN_TYPE.FILE_TRANSFER_VIEW_T){
						ServiceManager.getScreenService().show(ScreenHome.class);
						ServiceManager.getScreenService().destroy(ScreenFileTransferView.this.getId());
					}
				}});
		}
	};
	
	public static boolean ShareContent(String remoteUri, String path, boolean showView){
		String validUri = UriUtils.makeValidSipUri(remoteUri);
		if(validUri == null){
			// Show DialogError
			return false;
		}
		
		MyMsrpSession session = MyMsrpSession.createOutgoingSession(ServiceManager.getSipService().getStack(), MediaType.FileTransfer);		
		if(session.sendFile(validUri, path)){
			if(showView){
				return ServiceManager.getScreenService().show(ScreenFileTransferView.class, new Long(session.getId()).toString());
			}
			return true;
		}
		return false;
	}
	
	
	/* ========== IMsrpEventHandler =============== */
	
	@Override
	public boolean onMsrpEvent(Object sender, MsrpEventArgs e){

		if(this.msrpSession == null){
			Log.e(ScreenFileTransferView.TAG, "Invalid MSRP session");
			return false;
		}
				
		MsrpEventTypes type = e.getType();
		switch(type){
			case CONNECTED:
				this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						ScreenFileTransferView.this.btAbort.setText("Abort");
						ScreenFileTransferView.this.btAccept.setVisibility(View.GONE);
					}
				});
				break;
				
			case DATA:		
			case SUCCESS_200OK:
				if((this.msrpSession.isOutgoing() && type == MsrpEventTypes.SUCCESS_200OK) || (!this.msrpSession.isOutgoing() && type == MsrpEventTypes.DATA)){
					this.start = (Long)e.getExtra("start");
					this.end = (Long)e.getExtra("end");
					this.total = (Long)e.getExtra("total");
					this.runOnUiThread(this.runnableUpdateProgressBar);
					//...To be continued
				}
				break;
				
				
			case DISCONNECTED:
				this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ScreenFileTransferView.this.tvByteRange.setText("Done!");
						
						if(ScreenFileTransferView.this.timerSuicide == null){
							ScreenFileTransferView.this.timerSuicide = new Timer();
							ScreenFileTransferView.this.timerSuicide.schedule(ScreenFileTransferView.this.timerTaskSuicide, new Date(new Date().getTime() + 1500));
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
