package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Sevices.Impl.ServiceManager;

import android.os.Bundle;

public class ScreenHistory  extends Screen{

	private final IConfigurationService configurationService;
	
	public ScreenHistory() {
		super(SCREEN_TYPE.HISTORY_T, ScreenHistory.class.getCanonicalName());
		
		this.configurationService = ServiceManager.getConfigurationService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_history);
	}
}
