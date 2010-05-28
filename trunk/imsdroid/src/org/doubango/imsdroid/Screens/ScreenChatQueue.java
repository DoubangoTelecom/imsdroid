package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;

import android.os.Bundle;

public class ScreenChatQueue  extends Screen{

	private final IConfigurationService configurationService;
	
	public ScreenChatQueue() {
		super(SCREEN_TYPE.CHAT_QUEUE_T);
		
		this.configurationService = ServiceManager.getConfigurationService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_chat_queue);
	}
}
