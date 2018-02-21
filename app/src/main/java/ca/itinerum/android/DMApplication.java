package ca.itinerum.android;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.ContextCompat;

import com.evernote.android.job.JobManager;
import com.facebook.drawee.backends.pipeline.Fresco;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

import ca.itinerum.android.recording.LocationLoggingService;
import ca.itinerum.android.recording.RecordingJobScheduler;
import ca.itinerum.android.recording.RecordingUtils;
import ca.itinerum.android.utilities.LocationLoggingEvent;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.SharedPreferenceManager;

@SuppressWarnings("HardCodedStringLiteral")
public class DMApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

	private boolean mActivityMapActive;
	private SharedPreferenceManager mSharedPreferenceManager;

	public boolean isActivityMapActive() {
		return mActivityMapActive;
	}

	@Override
    public void onCreate() {
		super.onCreate();

		mSharedPreferenceManager = SharedPreferenceManager.getInstance(this);

		Fresco.initialize(this);

		JobManager.create(this).addJobCreator(new RecordingJobScheduler());

		updateVersion();

		configureUUID();

        startLoggingService();

		registerActivityLifecycleCallbacks(this);
    }

    private void configureUUID() {
		// initialize UUID if necessary
		String uuid = mSharedPreferenceManager.getUUID();
		if (uuid == null) {
			uuid = UUID.randomUUID().toString();
			mSharedPreferenceManager.setUUID(uuid);
		}

		Logger.l.d("uuid:", uuid);
	}

	private void updateVersion() {

		int oldVersion = mSharedPreferenceManager.getCurrentVersion();
		int newVersion = 0;

		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			newVersion = info.versionCode % 1000000;
			mSharedPreferenceManager.setCurrentVersion(newVersion);
		} catch (PackageManager.NameNotFoundException e) {
			Logger.l.e("Couldn't get package name", e);
		}

		Logger.l.d("old version", oldVersion, "new version", newVersion);

		if (oldVersion >= newVersion) return;

		/** any upgrade path stuff can go here **/

		// Finally update the current version
		if (newVersion != 0) mSharedPreferenceManager.setCurrentVersion(newVersion);

	}

	/***
	 * This function is the global access point for starting the GPS logging service.
	 *
	 * This is safe to call multiple times, as it contains checks to ensure
	 */

	public void startLoggingService() {

		// The sync job updates, so we need to check if it exists.
		RecordingUtils.schedulePeriodicUpdates(this);

		// If the survey is defined as having a finite interval
		if (!RecordingUtils.isOngoing(this)) {
			if (!RecordingUtils.isComplete(this)) {
				// schedule a cutoff this the survey is not past complete
				RecordingUtils.scheduleSurveyCutoffJob(this);
			} else {
				// otherwise completely stop recording and kill the background service
				stopLoggingService();
				return;
			}
		}

		if (LocationLoggingService.isRunning(this) || !SharedPreferenceManager.getInstance(this).hasCompletedQuestionnaire()) {
			return;
		}

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}

		startService(new Intent(this, LocationLoggingService.class));
		EventBus.getDefault().post(new LocationLoggingEvent.StartStop(true));
	}

	public void stopLoggingService() {
		stopService(new Intent(this, LocationLoggingService.class));
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

	@Override
	public void onActivityStarted(Activity activity) {}

	@Override
	public void onActivityResumed(Activity activity) {
		if (activity instanceof MapActivity) {
			mActivityMapActive = true;
		}
	}

	@Override
	public void onActivityPaused(Activity activity) {
		if (activity instanceof MapActivity) {
			mActivityMapActive = false;
		}
	}

	@Override
	public void onActivityStopped(Activity activity) {}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

	@Override
	public void onActivityDestroyed(Activity activity) {}
}
