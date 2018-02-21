package ca.itinerum.android.recording;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ca.itinerum.android.DMApplication;

public class StartupReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
			((DMApplication) context.getApplicationContext()).startLoggingService();
	}
}