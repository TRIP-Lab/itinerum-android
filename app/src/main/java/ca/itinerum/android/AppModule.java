package ca.itinerum.android;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by stewjacks on 16-01-17.
 */
@Module
public class AppModule {

	private final Application mApplication;

	public AppModule(Application application) {
		mApplication = application;
	}

	@Provides
	@Singleton
	Application provideApplication() {
		return mApplication;
	}

	@Provides @Singleton LocationManager provideLocationManager() {
		return (android.location.LocationManager) mApplication.getSystemService(Context.LOCATION_SERVICE);
	}
}
