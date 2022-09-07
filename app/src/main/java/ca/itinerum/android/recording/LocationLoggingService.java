/*
 * LocationLoggingService is an unbound service that manages the LocationManager and
 * ActivityRecognitionManager interactions. A single instance of both should be running
 * at all times. This is created onBoot or onCreate of the MapActivity, whichever happens 
 * first
 *
 * This is influenced by the structure of GPSLogger project: https://github.com/mendhak/gpslogger/
 */

package ca.itinerum.android.recording;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.NotImplementedException;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.DMApplication;
import ca.itinerum.android.MapActivity;
import ca.itinerum.android.activityrecognition.ActivityRecognitionUtils;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.Constants;
import ca.itinerum.android.utilities.DateUtils;
import ca.itinerum.android.utilities.LocationLoggingEvent;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.ServiceEvents;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.db.ItinerumDatabase;
import ca.itinerum.android.utilities.db.LocationPoint;

@SuppressWarnings("HardCodedStringLiteral")
public class LocationLoggingService extends Service {
	public static final int GEOFENCE_NOTIFICATION_ID = 920323;
	public static final String GEOFENCE_INTENT_EXTRA = "geofence_notification";
	public static final String LOCATION_ACTION = "LocationLoggingService.ACTION_LOCATION_INTENT";
	public static final String GEOFENCE_ACTION = "LocationLoggingService.ACTION_GEOFENCE_INTENT";
	public static final String NOTIFICATION_DISMISS_ACTION = "LocationLoggingService.ACTION_DISMISS_NOTIFICATION";
	private static final String ACTIVITY_RECOGNITION_ACTION = "LocationLoggingService.ACTION_ACTIVITY_RECOGNITION";
	private static final String WAKEUP_ALARM_ACTION = "LocationLoggingService.ACTION_WAKEUP_ALARM";

	private static final String WAKE_LOCK_TAG = "LocationLoggingService.WAKE_LOCK";

	private static final int notification_id = 28535;

	private GpsLoggingBinder mBinder;

	private PendingIntent mActivityRecognitionPendingIntent;
	private PendingIntent mWakeupAlarmPendingIntent;

	@NonNull private LocationManager mLocationManager;
	@NonNull private GeofenceManager mGeofenceManager;

	private long mFirstRetryTimestamp;
	private Location mBestBadPoint;

	@NonNull private PowerManager.WakeLock mWakeLock;

	private static final long[] mPattern = {0, 100, 100, 100, 100, 100, 100, 1000, 500};
	private AlarmManager mAlarmManager;

	/**
	 * Can be used from calling classes as the go-between for methods and
	 * properties.
	 */
	public class GpsLoggingBinder extends Binder {
		public LocationLoggingService getService() {
			return LocationLoggingService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Logger.l.d("onCreate()");

		registerDozeListener();

		mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
		mWakeLock.acquire();

		mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		mLocationManager = new LocationManager(this);
		mGeofenceManager = new GeofenceManager(this);

		initNotificationChannel(this);

		EventBus.getDefault().register(this);

		mBinder = new GpsLoggingBinder();

		Session.getInstance().setRecording(false);

		mGeofenceManager.removeGeofenceForCurrentLocation();
		
		EventBus.getDefault().post(new LocationLoggingEvent.StartStop(true));

	}

	@Override
	public void onDestroy() {
		Logger.l.d("onDestroy called by system");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			mAlarmManager.cancel(getWakeupAlarmPendingIntent());
		}

		stopLocationUpdates();

		// unregister eventbus
		try {
			EventBus.getDefault().unregister(this);
		} catch (Throwable t) {
			Logger.l.e(t.toString());
			//this may crash if registration did not go through. just be safe
		}

		try {
			stopActiveLogging();
		} catch (Exception e) {
			Logger.l.e(e.toString());
		}

		try {
			mLocationManager.stopUpdates();
		} catch (Exception e) {
			Logger.l.e(e.toString());
		}

		try {
			stopService();
		} catch (java.lang.IllegalArgumentException e) {
			Logger.l.e(e.toString());
		}
		if (mWakeLock.isHeld()) mWakeLock.release();

		stopForeground(true);

		super.onDestroy();

	}

	private void registerDozeListener() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			BroadcastReceiver receiver = new BroadcastReceiver() {
				@RequiresApi(api = Build.VERSION_CODES.M)
				@Override
				public void onReceive(Context context, Intent intent) {
					PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

					if (pm.isDeviceIdleMode()) {
						Logger.l.d("Doze mode on");
						mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10), getWakeupAlarmPendingIntent());
					} else {
						mAlarmManager.cancel(getWakeupAlarmPendingIntent());
						Logger.l.d("Doze mode off");
					}
				}
			};

			registerReceiver(receiver, new IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED));
		}
	}

	private PendingIntent getWakeupAlarmPendingIntent() {
		if (mWakeupAlarmPendingIntent == null) {
			Intent intent = new Intent(this, LocationLoggingService.class);
			intent.setAction(WAKEUP_ALARM_ACTION);
			mWakeupAlarmPendingIntent = PendingIntent.getService(LocationLoggingService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		return mWakeupAlarmPendingIntent;
	}

	private PendingIntent getActivityRecognitionPendingIntent() {
		if (mActivityRecognitionPendingIntent == null) {
			Intent intent = new Intent(this, LocationLoggingService.class);
			intent.setAction(ACTIVITY_RECOGNITION_ACTION);
			mActivityRecognitionPendingIntent = PendingIntent.getService(LocationLoggingService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		return mActivityRecognitionPendingIntent;
	}

	private void requestActivityRecognitionUpdates() {

		Logger.l.d("Requesting activity recognition updates");
		ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(this);
		activityRecognitionClient.requestActivityUpdates(TimeUnit.MINUTES.toMillis(1), getActivityRecognitionPendingIntent());

	}

	private void stopActivityRecognitionUpdates() {
		Logger.l.d("Stopping activity recognition updates");
		ActivityRecognition.getClient(this).removeActivityUpdates(getActivityRecognitionPendingIntent());

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		parseIntent(intent);
		return START_STICKY;
	}

	private void parseIntent(Intent intent) {

		if (intent == null || intent.getAction() == null) return;

		Logger.l.d(intent.toString());

		// BroadcastReceivers and IntentServices can go here instead as this is always running

		switch (intent.getAction()) {
			case LOCATION_ACTION:
				if (!LocationResult.hasResult(intent)) return;

				final LocationResult locationResult = LocationResult.extractResult(intent);
				List<Location> locations = locationResult.getLocations();
				for (Location location : locations) {
					OnLocationChanged(location);
				}
				break;
			case GEOFENCE_ACTION:
				final GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

				if (geofencingEvent.hasError()) {
					Logger.l.e("error with geofence", geofencingEvent.getErrorCode());
					return;
				}
				Logger.l.d("geofence transition", geofencingEvent.getGeofenceTransition());
				switch (geofencingEvent.getGeofenceTransition()) {
					case Geofence.GEOFENCE_TRANSITION_ENTER:
						Logger.l.d("Geofence enter intent action");
						onEnterEvent();
						break;

					case Geofence.GEOFENCE_TRANSITION_DWELL:
						Logger.l.d("Geofence dwell intent action");
						onDwellEvent();
						break;

					case Geofence.GEOFENCE_TRANSITION_EXIT:
						Logger.l.d("Geofence exit intent action");
						onExitEvent();
						break;
				}

				break;
			case ACTIVITY_RECOGNITION_ACTION:
				if (!ActivityRecognitionResult.hasResult(intent)) return;

				ActivityRecognitionResult arResult = ActivityRecognitionResult.extractResult(intent);
				DetectedActivity activity = arResult.getMostProbableActivity();

				Logger.l.d("Activity recognition result:", ActivityRecognitionUtils.parseActivity(activity));

				if (activity.getType() != DetectedActivity.TILTING) Session.getInstance().setCurrentDetectedActivity(activity.getType());
				break;
			case NOTIFICATION_DISMISS_ACTION:
				int id = intent.getIntExtra("notificationId", 0);
				if (id != 0) ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);

				// Now insert a cancelled prompt answer
				// cancelled prompts should exist above the maximum number of prompts to avoid collision.
				int count = ItinerumDatabase.getInstance(this).promptDao().getCount() + SharedPreferenceManager.getInstance(this).getMaximumNumberOfPrompts() + 1 + (int) (Math.random() * 1000);

				PromptAnswer promptAnswer = new PromptAnswer()
						.withPrompt("")
						.withAnswer("")
						.withLatitude(Session.getInstance().getLastRecordedLatitude())
						.withLongitude(Session.getInstance().getLastRecordedLongitude())
						.withDisplayedAt(DateUtils.formatDateForBackend(Session.getInstance().getGeofenceTimestamp()))
						.withRecordedAt(DateTime.now().toString(ISODateTimeFormat.dateTime()))
						.withCancelledAt(DateTime.now().toString(ISODateTimeFormat.dateTime()))
						.withUuid(UUID.randomUUID().toString())
						.withUploaded(false)
						.withCancelled(true)
						.withPromptNumber(-1);

				ItinerumDatabase.getInstance(this).promptDao().insert(promptAnswer);

				Session.getInstance().setGeofenceState(Session.GeofenceState.ANSWERED);
				break;
			case WAKEUP_ALARM_ACTION:
				Logger.l.d("Wakeup alarm action triggered");
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10), getWakeupAlarmPendingIntent());
				}
				break;
			default:
				if (BuildConfig.DEBUG) throw new NotImplementedException(intent.getAction() + " not implemented");
		}
	}

	/**
	 * Start low-power point collection as location is made available by system, along with
	 * periodic system wakeup to check location for more accurate geofence monitoring
	 */
	private void startLowPowerLogging() {
		Logger.l.d("Starting low-power location listener");
		startForeground(notification_id, getDefaultNotification());
		mLocationManager.startUpdates(LocationManager.LocationState.LOW_POWER);
	}

	private void startNoPowerLogging() {
		mLocationManager.startUpdates(LocationManager.LocationState.NO_POWER);
	}

	/**
	 * Start active point collection. GPS is ON and app is actively using power
	 */
	private void startActiveLogging() {
		Logger.l.d("Start active logging");

		// (!!) now we need to run in the foreground with a notification so the app doesn't x
		// get killed. This has to be called within 5s of startForegroundService()
		startForeground(notification_id, getDefaultNotification());

//		if (Session.getInstance().isRecording()) {
//			Logger.l.w("Session already started, ignoring");
//			return;
//		}

		Session.getInstance().setRecording(true);
		notifyClientStarted();
		startActiveLocationManager();
		requestActivityRecognitionUpdates();
	}

	/**
	 * Stops logging, removes notification, stops GPS manager, stops email timer
	 */
	private void stopActiveLogging() {
		Logger.l.w("stop logging called");

		if (!Session.getInstance().isRecording()) return;
		Session.getInstance().setRecording(false);

//		stopActivityRecognitionUpdates();

		SetStatus(getResources().getString(R.string.stopped));

	}

	private void cancelGeofences() {
		Logger.l.d("cancel geofences called");
		mGeofenceManager.cancelGeofences();
	}

	/**
	 * Asks the main service client to clear its form.
	 */
	private void notifyClientStarted() {
		EventBus.getDefault().post(new ServiceEvents.LoggingStatus(true));
	}

	private void startActiveLocationManager() {
		Logger.l.d("Starting active location manager");
		mLocationManager.startUpdates(LocationManager.LocationState.HIGH_POWER);
		SetStatus(getResources().getString(R.string.started));
	}


	public void stopService() {

		Logger.l.d("Stop service");

		Session.getInstance().setUserStillSinceTimeStamp(0);

		Session.getInstance().setLastValidLocation(null);

		removeAllNotifications();

		mLocationManager.stopUpdates();

		stopActivityRecognitionUpdates();

		stopForeground(true);
	}

	/**
	 * Returns true if there is already an instance of this service running.
	 *
	 * @param context
	 * @return
	 */
	public static boolean isRunning(Context context) {
		ActivityManager manager = (ActivityManager) context
			.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
			.getRunningServices(Integer.MAX_VALUE)) {
			if (LocationLoggingService.class.getName().equals(
				service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public void initNotificationChannel(Context context) {
		if (Build.VERSION.SDK_INT < 26) {
			return;
		}
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationChannel channelPersistent = new NotificationChannel("foreground", getResources().getString(R.string.notification_group_persistent), NotificationManager.IMPORTANCE_LOW);
		channelPersistent.setDescription(getString(R.string.notification_group_persistent_description));
		notificationManager.createNotificationChannel(channelPersistent);

		NotificationChannel channelPrompt = new NotificationChannel("trip", getResources().getString(R.string.notification_group_trip), NotificationManager.IMPORTANCE_HIGH);
		channelPrompt.setDescription(getString(R.string.notification_group_trip_description));
		channelPrompt.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		channelPrompt.setVibrationPattern(mPattern);
		notificationManager.createNotificationChannel(channelPrompt);
	}

	private Notification getDefaultNotification() {
		Builder builder = new Builder(this, "foreground");
		builder.setLargeIcon(
				BitmapFactory.decodeResource(getResources(),
						R.mipmap.ic_launcher))
				.setSmallIcon(R.drawable.itinerum_notification_icon)
				.setContentIntent(getLaunchMapActivityIntent())
				.setContentTitle(getString(R.string.notification_title))
				.setShowWhen(false)
				.setContentText(getString(R.string.constant_notification_message))
				.setPriority(NotificationCompat.PRIORITY_MIN)
				.setOngoing(true);

		return builder.build();
	}

	private Notification getPausedNotification() {
		Builder builder = new Builder(this, "foreground");
		builder.setLargeIcon(
				BitmapFactory.decodeResource(getResources(),
						R.mipmap.ic_launcher))
				.setSmallIcon(R.drawable.itinerum_notification_icon)
				.setContentIntent(getLaunchMapActivityIntent())
				.setContentTitle(getString(R.string.notification_title))
				.setShowWhen(false)
				.setContentText(getString(R.string.paused_notification_message))
				.setPriority(NotificationCompat.PRIORITY_MIN)
				.setOngoing(true);

		return builder.build();
	}

	private PendingIntent getLaunchMapActivityIntent() {
		Intent intent = new Intent(this, MapActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
			| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		return PendingIntent.getActivity(this, 0, intent, 0);
	}

	private void onEnterEvent() {
		mGeofenceManager.onGeofenceEnterEvent();
	}

	private void onExitEvent() {

		//insert a cancelled prompt without a cancelled_at value into the table
		if (Session.getInstance().isGeofenceDwell() && !Session.getInstance().isGeofencePurposeRecorded()) {

			PromptAnswer promptAnswer = new PromptAnswer()
					.withPrompt("")
					.withAnswer("")
					.withLatitude(Session.getInstance().getLastRecordedLatitude())
					.withLongitude(Session.getInstance().getLastRecordedLongitude())
					.withDisplayedAt(DateUtils.formatDateForBackend(Session.getInstance().getGeofenceTimestamp()))
					.withRecordedAt(DateTime.now().toString(ISODateTimeFormat.dateTime()))
					.withUuid(UUID.randomUUID().toString())
					.withUploaded(false)
					.withCancelled(true)
					.withPromptNumber(-1);

			ItinerumDatabase.getInstance(this).promptDao().insert(promptAnswer);

		}

		Session.getInstance().setGeofenceState(Session.GeofenceState.ANSWERED);

		mGeofenceManager.onGeofenceExitEvent();
	}

	private void onDwellEvent() {
		Logger.l.d("geofence dwell triggered");

		SharedPreferenceManager sp = SharedPreferenceManager.getInstance(this);

		Session.getInstance().setGeofenceState(Session.GeofenceState.ANSWERED); //this is the default state unless we're supposed to prompt

		if (sp.isRecordingPaused()) {
			Logger.l.d("geofence dwell: paused - skipping");
			return;
		}

		EventBus.getDefault().post(new LocationLoggingEvent.StartStop(false));

		// if the current survey has no prompt questions, don't prompt the user at stops.
		if (sp.getNumberOfPrompts() == 0) {
			return;
		}

		// check to make sure we haven't recorded all trips already, and that we aren't continuing recording
		if (!sp.getOngoingPrompts() && (RecordingUtils.hasFinishedAutomaticPrompts(this))) {
			Logger.l.d("all trips recorded");
			return;
		}

		// check the last geofence for proximity so we don't bother people unnecessarily
		PromptAnswer lastPromptAnswer = ItinerumDatabase.getInstance(this).promptDao().getLastPromptAnswer();
		Logger.l.d("last prompt answer null:", lastPromptAnswer == null);
		// get the distance to the last recorded geofence. Minimum displacement is 250m
		if (lastPromptAnswer != null) {
			LatLng lastGeofence = lastPromptAnswer.getLatLng();
			double distance = RecordingUtils.CalculateDistance(Session.getInstance().getLastRecordedLatitude(), Session.getInstance().getLastRecordedLongitude(), lastGeofence.latitude, lastGeofence.longitude);
			if (distance < 250) {
				Logger.l.d("distance < 250m - skipping prompt");
				return;
			}
		}

		// now we're officially dwelling AND should prompt

		Logger.l.d("Valid prompt after dwell");

		Session.getInstance().setGeofenceState(Session.GeofenceState.DWELL);
		Session.getInstance().setGeofenceTimestamp(System.currentTimeMillis());

		showPrompt();
	}

	private void showPrompt() {

		// If the activity is currently in the forefront, prompt the user immediately
		if (((DMApplication) getApplication()).isActivityMapActive()) {
			EventBus.getDefault().post(new LocationLoggingEvent.ShowGeofencePromptInActivity());
			Logger.l.d("MapActivity open - showing prompt in activity");
			return;
		}

		Intent notificationIntent = new Intent(this, MapActivity.class);
		TaskStackBuilder notificationStackBuilder = TaskStackBuilder.create(this);

		// Adds the back stack for the Intent (but not the Intent itself)
		notificationStackBuilder.addParentStack(MapActivity.class);

		// Adds the Intent that starts the Activity to the top of the stack
		notificationStackBuilder.addNextIntent(notificationIntent);
		PendingIntent notificationPendingIntent =
				notificationStackBuilder.getPendingIntent(
						0,
						PendingIntent.FLAG_ONE_SHOT
				);

		// This is the positive button pending intent from the notification
		Intent positiveIntent = new Intent(this, MapActivity.class);
		positiveIntent.putExtra(GEOFENCE_INTENT_EXTRA, true);

		// The stack builder object will contain an artificial back stack for the started Activity.
		// This ensures that navigating backward from the Activity leads out of your application to the Home screen.
		TaskStackBuilder positiveStackBuilder = TaskStackBuilder.create(this);

		// Adds the back stack for the Intent (but not the Intent itself)
		positiveStackBuilder.addParentStack(MapActivity.class);

		// Adds the Intent that starts the Activity to the top of the stack
		positiveStackBuilder.addNextIntent(positiveIntent);
		PendingIntent positivePendingIntent =
				positiveStackBuilder.getPendingIntent(
						0,
						PendingIntent.FLAG_ONE_SHOT
				);

		// This is the negative button pending intent from the notification
		Intent falseIntent = new Intent(this, LocationLoggingService.class);
		falseIntent.setAction(NOTIFICATION_DISMISS_ACTION);
		falseIntent.putExtra("notificationId", GEOFENCE_NOTIFICATION_ID);
		PendingIntent falsePendingIntent = PendingIntent.getService(this, 0, falseIntent, 0);

		NotificationCompat.Action positive = new NotificationCompat.Action(R.drawable.ic_done_white_24dp, getString(R.string.notification_true), positivePendingIntent);
		NotificationCompat.Action negative = new NotificationCompat.Action(R.drawable.ic_clear_white_24dp, getString(R.string.notification_false), falsePendingIntent);

		// Build the notification to show the user

		Builder builder = new Builder(this, "trip")
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
				.setSmallIcon(R.drawable.itinerum_notification_icon)
				.setContentIntent(notificationPendingIntent)
				.setContentTitle(getString(R.string.notification_stopped_title))
				.setContentText(getString(R.string.notification_stopped_message))
				.addAction(negative)
				.addAction(positive)
				.setVibrate(mPattern)
				.setStyle(new NotificationCompat.BigTextStyle()
						.setBigContentTitle(getString(R.string.notification_stopped_title))
						.bigText(getString(R.string.notification_stopped_message)))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(false);

		NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(GEOFENCE_NOTIFICATION_ID, builder.build());

		Logger.l.d("MapActivity closed - prompt notification triggered");
	}

	/**
	 * Gives a status message to the main service client to display
	 *
	 * @param status The status message
	 */
	void SetStatus(String status) {
//		Logger.l.i(status);
		EventBus.getDefault().post(new ServiceEvents.StatusMessage(status));
	}

	/**
	 * Gives an error message to the main service client to display
	 *
	 * @param messageId ID of string to lookup
	 */
	void SetFatalMessage(int messageId) {
		Logger.l.e(getString(messageId));
		EventBus.getDefault().post(new ServiceEvents.FatalMessage(getString(messageId)));
	}

	/**
	 * Stops the location managers
	 */
	private void stopLocationUpdates() {
		SetStatus(getString(R.string.stopped));
        Logger.l.d("stopping Location Updates");
		mLocationManager.stopUpdates();
	}

	/**
	 * Hides the notification icon in the status bar if it's visible.
	 */
	private void removeAllNotifications() {
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
	}

	// need notion of "last point" vs "last recorded point"

	public void OnLocationChanged(@NonNull Location location) {
		// if the user has manually paused recording, do not acquire points.
		if (Session.getInstance().isPaused()) return;

		// point is fishy and invalid. Ignore.
		if (location.getTime() <= 0) {
			Logger.l.d("location invalid: timestamp <= 0");
			return;
		}

		if (!location.hasAccuracy() || location.getAccuracy() == 0) {
			Logger.l.d("location invalid: no accuracy");
			return;
		}

		Logger.l.d("onLocationChanged", location.getLatitude(), location.getLongitude(), location.getAccuracy());

		long currentTimeStamp = location.getTime();
//		long lastTimestamp = Session.getInstance().getLastValidLocation() != null ? Session.getInstance().getLastValidLocation().getTime() : 0;

		// Time filter
//		if ((currentTimeStamp - lastTimestamp) < Constants.RECORDING_INTERVAL) {
//			return;
//		}

		//Check if a ridiculous distance has been travelled since previous point - could be a bad GPS jump
		if (Session.getInstance().getLastValidLocation() != null) {
			double distanceTravelled = RecordingUtils.CalculateDistance(location, Session.getInstance().getLastValidLocation());
			long timeDifference = Math.abs(location.getTime() - Session.getInstance().getLastValidLocation().getTime()) / 1000;
			double speed = distanceTravelled / timeDifference;
			if (speed > Constants.LARGEST_VELOCITY) {
				Logger.l.d(String.format("Very large jump detected - %d meters in %d sec - discarding point", (long) distanceTravelled, timeDifference));
				return;
			}
		}

		// we have a winner! Record and store point if > min displacement
		if (Math.abs(location.getAccuracy()) <= Constants.TARGET_ACCURACY) {

			Session.getInstance().setLastValidLocation(location);

			if (checkDisplacementThreshold(location, Session.getInstance().getLastRecordedLocation())) {
				Logger.l.d("Logging good point " + location.getLatitude(), location.getLongitude(), location.getProvider());
				RecordPoint(location);
			}

			mGeofenceManager.handleGeofenceForGoodLocation(location);

		} else {
		// try and get a better point.
			if (mBestBadPoint == null || location.getAccuracy() < mBestBadPoint.getAccuracy()) {
				mBestBadPoint = location;
			}

			if (mFirstRetryTimestamp == 0) {
				mFirstRetryTimestamp = System.currentTimeMillis();
			}

			if (currentTimeStamp - mFirstRetryTimestamp <= Constants.RETRY_INTERVAL * 1000) {
				Logger.l.d("Only accuracy of " + String.valueOf(Math.floor(location.getAccuracy())) + " m. Point discarded.");
				SetStatus("Point cached - trying for more accurate one");

				//keep trying
			} else {
				//Give up for now
                Session.getInstance().setLastValidLocation(location);

                if (checkDisplacementThreshold(location, Session.getInstance().getLastRecordedLocation())) {
					// Timeout reached: record best bad point and move on
					Logger.l.d("Only accuracy of " + String.valueOf(Math.floor(location.getAccuracy())) + " m and timeout reached");
					Logger.l.d("Logging bad point", mBestBadPoint.getLatitude(), mBestBadPoint.getLongitude(), mBestBadPoint.getProvider());
					RecordPoint(mBestBadPoint);
					mGeofenceManager.handleGeofenceForBadLocation(mBestBadPoint);
					mBestBadPoint = null;
				}
			}
		}
	}

	/**
	 * This checks to see if the phone has moved the minimum displacement amount for recording since
	 * the last recorded point
	 * @param location
	 * @return If the required displacement between recorded points has occurred
	 */

	private boolean checkDisplacementThreshold(@NonNull Location location, Location lastLocation) {

		if (lastLocation == null) return true;

		// Displacement filter: if we haven't displaced a requisite amount, don't do anything
		if (Constants.SMALLEST_DISPLACEMENT <= 0) return true;

		double distanceTraveled = RecordingUtils.CalculateDistance(location, lastLocation);

		int minDistance = Math.min(Math.max(Constants.SMALLEST_DISPLACEMENT, (int) (location.getSpeed() * 4)), Constants.LARGEST_DISPLACEMENT);
		if (minDistance > distanceTraveled) {
			SetStatus("Only " + String.valueOf(Math.floor(distanceTraveled)) + " m travelled. Point discarded. Min " + minDistance + " m");
			Logger.l.d("Only " + String.valueOf(Math.floor(distanceTraveled)) + " m travelled. Point discarded. Min " + minDistance + " m");
			return false;
		}

		return true;
	}

	private void RecordPoint(@NonNull Location location) {
		Session.getInstance().setLastRecordedLocation(location);
		recordLocationInDatabase(location, Session.getInstance().getCurrentDetectedActivity());

		//reset timestamp for next time.
		mFirstRetryTimestamp = 0;
	}

	private void recordLocationInDatabase(@NonNull Location location, int activity) {
		DateTime timeStamp = new DateTime(location.getTime());

		LocationPoint point = new LocationPoint(location.getAltitude(), location.getLatitude(), location.getLongitude(), DateUtils.formatDateForBackend(timeStamp), location.getSpeed(), location.getAccuracy(), 0, activity);
		ItinerumDatabase.getInstance(this).locationDao().insert(point);
	}

	/**
	 * Triggered when the user presses the pause/resume button. This overrides other GPS toggling
	 * @param pauseResume
	 */

	@Subscribe
	public void onEvent(LocationLoggingEvent.PauseResume pauseResume) {
		Logger.l.d("pause resume event - resume:", pauseResume.pause);
		if (pauseResume.pause) {
			Session.getInstance().setPaused(true);
			Session.getInstance().setRecording(false);
			cancelGeofences();
			stopActiveLogging();
			stopLocationUpdates();
			startNoPowerLogging();
			startForeground(notification_id, getPausedNotification());
		} else {
			Session.getInstance().setPaused(false);
			Session.getInstance().setRecording(false);
			startActiveLogging();
		}
	}

	/**
	 * Triggered whenever the GPS should be toggled on or off
	 * @param startStop
	 */

	@Subscribe
	public void onEvent(LocationLoggingEvent.StartStop startStop) {
		if (!SharedPreferenceManager.getInstance(this).isRecordingPaused()) {
			Logger.l.d("start stop event - start:", startStop.start);
			if (startStop.start) {
				startActiveLogging();
			} else {
				stopActiveLogging();
				startLowPowerLogging();
			}
		} else {
			// this is mandatory because we always call start foreground
			startForeground(notification_id, getPausedNotification());
		}
	}

	/**
	 * Triggered when a geofence loitering alarm is issued.
	 * @param entered
	 */

	@Subscribe
	public void onEvent(LocationLoggingEvent.PromptGeofenceLoiter entered) {
		Logger.l.d("geofence loiter triggered");

		Session.getInstance().setGeofenceState(Session.GeofenceState.LOITER);
		mLocationManager.startUpdates(LocationManager.LocationState.MEDIUM_POWER);
		mGeofenceManager.scheduleGeofenceDwellAlarm();

	}

	/**
	 * Triggered when a dwell alarm is issued. This will prompt the user to record their trip purpose
	 * if necessary
	 * @param entered
	 */

	@Subscribe
	public void onEvent(LocationLoggingEvent.PromptGeofenceDwell entered) {
		onDwellEvent();
	}

	@Subscribe
	public void onEvent(LocationLoggingEvent.PromptImmediate immediate) {
		showPrompt();
	}
}
