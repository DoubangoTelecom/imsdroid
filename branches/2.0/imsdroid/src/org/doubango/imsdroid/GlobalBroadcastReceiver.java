package org.doubango.imsdroid;

import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class GlobalBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			SharedPreferences settings = context.getSharedPreferences(NgnConfigurationEntry.SHARED_PREF_NAME, 0);
			if (settings != null && settings.getBoolean(NgnConfigurationEntry.GENERAL_AUTOSTART.toString(), NgnConfigurationEntry.DEFAULT_GENERAL_AUTOSTART)) {
				Intent i = new Intent(context, NativeService.class);
				i.putExtra("autostarted", true);
				context.startService(i);
			}
		} else if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action) && Engine.getInstance().getSipService().isRegistered()) {
			final String number = getResultData();
			if (NgnStringUtils.isNullOrEmpty(number)) {
				return;
			}
			final boolean intercept = Engine.getInstance().getConfigurationService().getBoolean(NgnConfigurationEntry.GENERAL_INTERCEPT_OUTGOING_CALLS, NgnConfigurationEntry.DEFAULT_GENERAL_INTERCEPT_OUTGOING_CALLS);
			if (intercept) {
				//ServiceManager.getScreenService().bringToFront(Main.ACTION_INTERCEPT_OUTGOING_CALL, new String[] { "number", number });
				//setResultData(null);
				//return;
			}

			setResultData(number);
		}

	}
}