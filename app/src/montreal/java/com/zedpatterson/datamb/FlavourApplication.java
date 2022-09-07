package ca.itinerum.android;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class FlavourApplication extends DMApplication {

	@Override
    public void onCreate() {
		super.onCreate();
		FacebookSdk.sdkInitialize(getApplicationContext());
		AppEventsLogger.activateApp(this);
    }
}
