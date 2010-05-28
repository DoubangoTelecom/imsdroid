package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;
import org.doubango.imsdroid.Sevices.Impl.SipService;
import org.doubango.imsdroid.events.ISubscriptionEventHandler;
import org.doubango.imsdroid.events.SubscriptionEventArgs;
import org.doubango.imsdroid.utils.ContentType;
import org.doubango.imsdroid.utils.StringUtils;
import org.doubango.imsdroid.xml.reginfo.Reginfo;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.os.Bundle;
import android.util.Log;

public class ScreenRegistrations  extends Screen
implements ISubscriptionEventHandler
{
	private final static String TAG = ScreenRegistrations.class.getCanonicalName();
	
	private final IConfigurationService configurationService;
	private final ISipService sipService;
	
	public ScreenRegistrations() {
		super(SCREEN_TYPE.REGISTRATIONS_T);
		
		this.configurationService = ServiceManager.getConfigurationService();
		this.sipService = ServiceManager.getSipService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_registrations);
        
		// add event handlers
        this.sipService.addSubscriptionEventHandler(this);
	}

	@Override
	protected void onDestroy() { 
        // remove event handlers
        this.sipService.removeSubscriptionEventHandler(this);
        
        super.onDestroy();
	}
	
	public boolean onSubscriptionEvent(Object sender, SubscriptionEventArgs e) {
		
		switch(e.getType()){
			case INCOMING_NOTIFY:
				final byte[] content = e.getContent();
				final String contentType = e.getContentType();
				if(content != null && StringUtils.equals(contentType, ContentType.REG_INFO, true)){
					this.updateRegInfo(content);
				}
				break;
				
			default:
				break;
		}
		return true;
	}
	
	private void updateRegInfo(byte[] content){
		final Serializer serializer = new Persister();
		try {
			Log.d(ScreenRegistrations.TAG, "start reginfo deserialization...");
			@SuppressWarnings("unused")
			Reginfo reginfo = serializer.read(Reginfo.class, new String(content));
			Log.d(ScreenRegistrations.TAG, "reginfo deserialized");
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
