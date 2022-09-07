package ca.itinerum.android;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.FirebaseApp;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.recording.LocationLoggingService;
import ca.itinerum.android.recording.RecordingJobScheduler;
import ca.itinerum.android.recording.RecordingUtils;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.CutoffScheduler;
import ca.itinerum.android.utilities.LocationLoggingEvent;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.SystemUtils;
import ca.itinerum.android.utilities.db.ItinerumDatabase;
import ca.itinerum.android.utilities.db.ModePromptHelper;
import io.fabric.sdk.android.Fabric;

@SuppressWarnings("HardCodedStringLiteral")
public class DMApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

	private boolean mActivityMapActive;
	private SharedPreferenceManager mSharedPreferenceManager;

	public boolean isActivityMapActive() {
		return mActivityMapActive;
	}

	static { AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); }

	@Override
    public void onCreate() {
		super.onCreate();

		mSharedPreferenceManager = SharedPreferenceManager.getInstance(this);

		FirebaseApp.initializeApp(this);

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

		if (oldVersion < 86 && newVersion >= 86 && BuildConfig.FLAVOR.equals("vanilla")) {
			// migrate modeprompt.db to location.db
			List<PromptAnswer> oldPrompts = ModePromptHelper.getInstance(this).getAllMigrationPromptAnswers();
			if (oldPrompts.size() > 0) {
				Collections.sort(oldPrompts);
				int i = 0;
				int numberOfPrompts = SharedPreferenceManager.getInstance(this).getNumberOfPrompts();
				int numberOfRecordedPrompts = 0;

				for (PromptAnswer promptAnswer: oldPrompts) {
					if (i % numberOfPrompts == 0)
						numberOfRecordedPrompts++;
					promptAnswer.setPromptNumber(numberOfRecordedPrompts - 1);
					promptAnswer.setCancelled(false);
					promptAnswer.setUploaded(false);
					i++;
				}

				PromptAnswer[] spread = new PromptAnswer[oldPrompts.size()];
				spread = oldPrompts.toArray(spread);
				ItinerumDatabase.getInstance(this).promptDao().insert(spread);
				SharedPreferenceManager.getInstance(this).setHasDwelledOnce(true);
			}

			deleteDatabase(ModePromptHelper.DATABASE_NAME);
		}

		// Itinerum Montreal version should do a hard reset for upgrades
		if (oldVersion < 105 && BuildConfig.FLAVOR.equals("montreal")) {
			SystemUtils.leaveCurrentSurvey(this);
			deleteDatabase(ModePromptHelper.DATABASE_NAME);
			Logger.l.w("ItinerumMTL upgraded from", oldVersion, "to", newVersion, "and dumped all existing data");
		}


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

		// if the conditions for completion have been met, don't start the service
		if (RecordingUtils.isComplete(this)) return;

		switch (CutoffScheduler.Schedule(this)) {
			case SCHEDULED:
				Logger.l.d("Cutoff is in future. Recording cutoff scheduled");
				break;
			case COMPLETE:
				Logger.l.d("Cutoff is in past. Recording cutoff not scheduled");
				stopLoggingService();
				return;
			case ONGOING:
				Logger.l.d("No cutoff scheduled because survey is ongoing");
				break;
		}

		if (LocationLoggingService.isRunning(this) || !SharedPreferenceManager.getInstance(this).hasCompletedQuestionnaire()) {
			return;
		}

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}

		ContextCompat.startForegroundService(this, new Intent(this, LocationLoggingService.class));
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
