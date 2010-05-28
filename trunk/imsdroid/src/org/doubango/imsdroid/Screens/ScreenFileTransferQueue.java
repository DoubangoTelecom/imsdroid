package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;

import android.os.Bundle;

public class ScreenFileTransferQueue  extends Screen{

	private final IConfigurationService configurationService;
	
	public ScreenFileTransferQueue() {
		super(SCREEN_TYPE.FILE_TRANSFER_QUEUE_T);
		
		this.configurationService = ServiceManager.getConfigurationService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_file_transfer_queue);
	}
}
