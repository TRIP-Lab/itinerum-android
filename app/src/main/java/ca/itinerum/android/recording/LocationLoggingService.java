/*
 * LocationLoggingService is an unbound service that manages the LocationManager and
 * ActivityRecognitionManager interactions. A single instance of both should be running
 * at all times. This is created onBoot or onCreate of the MapActivity, whichever happens 
 * first
 *
 * This is influenced by the structure of GPSLogger project: https://github.com/mendhak/gpslogger/
 */

package ca.itinerum.android.recording;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.NotImplementedException;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;

import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.DMApplication;
import ca.itinerum.android.MapActivity;
import ca.itinerum.android.R;
import ca.itinerum.android.activityrecognition.ActivityRecognitionIntentService;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.Constants;
import ca.itinerum.android.utilities.DateUtils;
import ca.itinerum.android.utilities.LocationLoggingEvent;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.ServiceEvents;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.db.LocationDatabase;
import ca.itinerum.android.utilities.db.LocationPoint;

@SuppressWarnings("HardCodedStringLiteral")
public class LocationLoggingService extends Service {
	public static final int GEOFENCE_NOTIFICATION_ID = 920323;
	public static final String GEOFENCE_INTENT_EXTRA = "geofence_notification";
	private static final int notification_id = 28535;

	private GpsLoggingBinder mBinder;

	private PendingIntent mActivityRecognitionPendingIntent;

	private LocationManager mGpsLocationManager;
	private LocationManager mPassiveLocationManager;
	private LocationManager mTowerLocationManager;

	private NamedLocationListener mGpsLocationListener;
	private NamedLocationListener mTowerLocationListener;
	private NamedLocationListener mPassiveLocationListener;

	private long mFirstRetryTimestamp;
	private GeofenceManager mGeofenceManager;
	private Location mBestBadPoint;

	private static final long[] mPattern = {0, 100, 100, 100, 100, 100, 100, 1000, 500};

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

		Logger.l.d("ServiceRecordLocations", "onCreate()");

		initNotificationChannel(this);

		EventBus.getDefault().register(this);

		mBinder = new GpsLoggingBinder();

		Session.getInstance().setRecording(false);

		getGeofenceManager().removeGeofenceForCurrentLocation();

		EventBus.getDefault().post(new LocationLoggingEvent.StartStop(true));

	}

	@Override
	public void onDestroy() {
		Logger.l.d("ServiceRecordLocations", "onDestroy()");

		// unregister eventbus
		try {
			EventBus.getDefault().unregister(this);
		} catch (Throwable t) {
			Logger.l.e(t.toString());
			//this may crash if registration did not go through. just be safe
		}

		try {
			stopLogging();
			stopService();
		} catch (java.lang.IllegalArgumentException e) {
			Logger.l.e(e.toString());
		}

		stopForeground(true);

		super.onDestroy();

	}

	private PendingIntent getActivityRecognitionPendingIntent() {
		if (mActivityRecognitionPendingIntent == null) {
			Intent intent = new Intent(LocationLoggingService.this, ActivityRecognitionIntentService.class);
			mActivityRecognitionPendingIntent = PendingIntent.getService(LocationLoggingService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		return mActivityRecognitionPendingIntent;
	}

	private void requestActivityRecognitionUpdates() {

		Logger.l.d("Requesting activity recognition updates");
		ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(this);
		activityRecognitionClient.requestActivityUpdates(60 * 1000l, getActivityRecognitionPendingIntent());

	}

	private void stopActivityRecognitionUpdates() {
		Logger.l.d("Stopping activity recognition updates");
		ActivityRecognition.getClient(this).removeActivityUpdates(getActivityRecognitionPendingIntent());

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.l.d("onStartCommand flags, startId", flags, startId);
		return START_STICKY;
	}

	private GeofenceManager getGeofenceManager() {
		if (mGeofenceManager == null) mGeofenceManager = new GeofenceManager(this);
		return mGeofenceManager;
	}

	private void startPassiveManager() {
		Logger.l.d("Starting passive location listener");
		if (mPassiveLocationListener == null) {
			mPassiveLocationListener = new NamedLocationListener(this, "PASSIVE");
		}
		mPassiveLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Logger.l.e("no location permission granted");
			return;
		}
		mPassiveLocationManager.requestLocationUpdates(android.location.LocationManager.PASSIVE_PROVIDER, 1000, 0, mPassiveLocationListener, Looper.getMainLooper());
	}


	private void stopPassiveManager() {
		if (mPassiveLocationManager != null) {
			Logger.l.d("Removing passiveLocationManager updates");
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				Logger.l.e("no location permission granted");
				return;
			}
			mPassiveLocationManager.removeUpdates(mPassiveLocationListener);
		}
	}

	protected void startLogging() {
		Logger.l.d("Start Logging");

		if (Session.getInstance().isRecording()) {
			Logger.l.w("Session already started, ignoring");
			return;
		}

		try {
			// (!!) now we need to run in the foreground with a notification so the
			// app doesn't get killed.
			startForeground(notification_id, getDefaultNotification());
		} catch (Exception ex) {
			Logger.l.e("Could not start GPSLoggingService in foreground. ", ex);
		}

		Session.getInstance().setRecording(true);
		notifyClientStarted();
		startGpsManager();
		stopPassiveManager();
		requestActivityRecognitionUpdates();
	}

	private void cancelGeofences() {
		getGeofenceManager().cancelGeofences();
	}

	/**
	 * Asks the main service client to clear its form.
	 */
	private void notifyClientStarted() {
		EventBus.getDefault().post(new ServiceEvents.LoggingStatus(true));
	}

	private void startGpsManager() {

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Logger.l.e("no location permission");
			return;
		}

		if (mGpsLocationListener == null) {
			mGpsLocationListener = new NamedLocationListener(this, "GPS");
		}

		if (mTowerLocationListener == null) {
			mTowerLocationListener = new NamedLocationListener(this, "CELL");
		}

		mGpsLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mTowerLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);

		checkTowerAndGpsStatus();

		Logger.l.i("Requesting GPS location updates");
		// gps satellite based
		mGpsLocationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 1000, 0, mGpsLocationListener, Looper.getMainLooper());

		Session.getInstance().setUsingGps(true);
//		startAbsoluteTimer();

		Logger.l.i("Requesting tower location updates");
		Session.getInstance().setUsingGps(false);
		// Cell tower and wifi based
		mTowerLocationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 1000, 0, mTowerLocationListener);

//		startAbsoluteTimer();


		if (!Session.getInstance().isTowerEnabled() && !Session.getInstance().isGpsEnabled()) {
			Logger.l.e("No provider available!");
			Session.getInstance().setUsingGps(false);
			SetStatus(getResources().getString(R.string.gpsprovider_unavailable));
			SetFatalMessage(R.string.gpsprovider_unavailable);
			stopLogging();
			setLocationServiceUnavailable();
			return;
		}

		Session.getInstance().setWaitingForLocation(true);
		SetStatus(getResources().getString(R.string.started));
	}

	/**
	 * Stops logging, removes notification, stops GPS manager, stops email timer
	 */
	public synchronized void stopLogging() {
		Logger.l.w("stop logging called");

		if (!Session.getInstance().isRecording()) return;
		Session.getInstance().setRecording(false);

		stopGpsManager();
		startPassiveManager();
		stopActivityRecognitionUpdates();

		notifyClientStopped();

	}

	public void stopService() {

		Session.getInstance().setUserStillSinceTimeStamp(0);

		Session.getInstance().setLastValidLocation(null);
		Session.getInstance().setSinglePointMode(false);

		removeAllNotifications();

		stopPassiveManager();

		stopForeground(true);
	}

	/**
	 * This method is called periodically to determine whether the cell tower /
	 * gps providers have been enabled, and sets class level variables to those
	 * values.
	 */
	private void checkTowerAndGpsStatus() {
		Session.getInstance().setTowerEnabled(mTowerLocationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER));
		Session.getInstance().setGpsEnabled(mGpsLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER));
	}

	/**
	 * Notifies main form that logging has stopped
	 */
	void notifyClientStopped() {
		EventBus.getDefault().post(new ServiceEvents.LoggingStatus(false));
	}

	// 30 minute check on user position
	private boolean userHasBeenStillForTooLong() {
		return !Session.getInstance().hasDescription() && !Session.getInstance().isSinglePointMode() &&
			(Session.getInstance().getUserStillSinceTimeStamp() > 0 && (System.currentTimeMillis() - Session.getInstance().getUserStillSinceTimeStamp()) > (30 * 60 * 1000));
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
		NotificationChannel channelPersistent = new NotificationChannel("foreground", getResources().getString(R.string.notification_group_persistent), NotificationManager.IMPORTANCE_NONE);
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

	private PendingIntent getLaunchMapActivityIntent() {
		Intent intent = new Intent(this, MapActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
			| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		return PendingIntent.getActivity(this, 0, intent, 0);
	}

	/**
	 * Stops location manager, then starts it.
	 */
	void RestartGpsManagers() {
		stopGpsManager();
		startGpsManager();
	}

	void setLocationServiceUnavailable() {
		EventBus.getDefault().post(new ServiceEvents.LocationServicesUnavailable());
	}

	/**
	 * Gives a status message to the main service client to display
	 *
	 * @param status The status message
	 */
	void SetStatus(String status) {
		Logger.l.i(status);
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
	private void stopGpsManager() {

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Logger.l.e("no location permission granted");
		}

        if (mTowerLocationListener != null) {
			Logger.l.d("Removing towerLocationManager updates");
			mTowerLocationManager.removeUpdates(mTowerLocationListener);
		}

		if (mGpsLocationListener != null) {
			Logger.l.d("Removing gpsLocationManager updates");
			mGpsLocationManager.removeUpdates(mGpsLocationListener);
		}

		Session.getInstance().setWaitingForLocation(false);
//		EventBus.getDefault().post(new ServiceEvents.WaitingForLocation(false));

		SetStatus(getString(R.string.stopped));
	}

	/**
	 * Hides the notification icon in the status bar if it's visible.
	 */
	private void removeAllNotifications() {
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
	}

	// need notion of "last point" vs "last recorded point"

	public void OnLocationChanged(@NonNull Location location) {
		if (Session.getInstance().isPaused()) return;
		Logger.l.d("location changed", location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.getProvider());

		long currentTimeStamp = System.currentTimeMillis();
		// Time filter
		if ((currentTimeStamp - Session.getInstance().getLastRecordedTimestamp()) < (Constants.RECORDING_INTERVAL * 1000)) {
			return;
		}

		//Check if a ridiculous distance has been travelled since previous point - could be a bad GPS jump
		if (Session.getInstance().getLastValidLocation() != null) {
			double distanceTravelled = RecordingUtils.CalculateDistance(location.getLatitude(), location.getLongitude(), Session.getInstance().getLastValidLocation().getLatitude(), Session.getInstance().getLastValidLocation().getLongitude());
			long timeDifference = Math.abs(location.getTime() - Session.getInstance().getLastValidLocation().getTime()) / 1000;
			double speed = distanceTravelled / timeDifference;
			if (speed > Constants.LARGEST_VELOCITY) { //357 m/s ~=  1285 km/h
				Logger.l.d(String.format("Very large jump detected - %d meters in %d sec - discarding point", (long) distanceTravelled, timeDifference));
				SetStatus(String.format("Very large jump detected - %d meters in %d sec - discarding point", (long) distanceTravelled, timeDifference));
				return;
			}
		}

		// point is fishy and invalid. Ignore.
		if (!location.hasAccuracy() || location.getAccuracy() == 0) return;

		// we have a winner! Record and store point if > min displacement
		if (Math.abs(location.getAccuracy()) <= Constants.TARGET_ACCURACY) {
			Session.getInstance().setLastValidLocation(location);
			if (checkDisplacementThreshold(location, Session.getInstance().getLastRecordedLocation())) {
				RecordPoint(location);
			}

			getGeofenceManager().handleGeofenceForGoodLocation(location);

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
				if (checkDisplacementThreshold(location, Session.getInstance().getLastRecordedLocation())) {
					// Timeout reached: record best bad point and move on
					Logger.l.d("Only accuracy of " + String.valueOf(Math.floor(location.getAccuracy())) + " m and timeout reached");
					Logger.l.d("Inaccurate point logged because retries timed out.");
					RecordPoint(mBestBadPoint);
					getGeofenceManager().handleGeofenceForBadLocation(mBestBadPoint);
					mBestBadPoint = null;
				}

                Session.getInstance().setLastValidLocation(location);

			}
		}
	}

	/**
	 * This checks to see if the phone has moved the minimum displacement amount for recording since
	 * the last recorded point
	 * @param location
	 * @return If the required displacement between recorded points has occurred
	 */

	private boolean checkDisplacementThreshold(Location location, Location lastLocation) {
		// Displacement filter: if we haven't displaced a requisite amount, don't do anything

		if (location == null && BuildConfig.DEBUG) throw new NotImplementedException("location cannot be null");

		if (Constants.SMALLEST_DISPLACEMENT <= 0) return true;

		if (lastLocation == null) return true;

		double distanceTraveled = RecordingUtils.CalculateDistance(location.getLatitude(), location.getLongitude(),
				lastLocation.getLatitude(), lastLocation.getLongitude());

		int minDistance = Math.max(Constants.SMALLEST_DISPLACEMENT, (int) (location.getSpeed() * 2));
		if (minDistance > distanceTraveled) {
			SetStatus("Only " + String.valueOf(Math.floor(distanceTraveled)) + " m travelled. Point discarded. Min " + minDistance + " m");
			Logger.l.d("Only " + String.valueOf(Math.floor(distanceTraveled)) + " m travelled. Point discarded. Min " + minDistance + " m");
			return false;
		}

		return true;
	}

	private void RecordPoint(Location location) {
//		SetDistanceTraveled(location);
		Session.getInstance().setLastRecordedLocation(location);
		recordLocationInDatabase(location, Session.getInstance().getCurrentDetectedActivity());
//		stopManagerAndResetAlarm();

		//reset timestamp for next time.
		mFirstRetryTimestamp = 0;
	}

	private void recordLocationInDatabase(Location location, int activity) {
		DateTime timeStamp = new DateTime(location.getTime());

		LocationPoint point = new LocationPoint(location.getAltitude(), location.getLatitude(), location.getLongitude(), DateUtils.formatDateForBackend(timeStamp), location.getSpeed(), location.getAccuracy(), 0, activity);
		LocationDatabase.getInstance(this).locationDao().insert(point);
	}

	/**
	 * Triggered when the user presses the pause/resume button. This overrides other GPS toggling
	 * @param pauseResume
	 */

	@Subscribe
	public void onEvent(LocationLoggingEvent.PauseResume pauseResume) {
		Logger.l.d("pause resume event - resume:", pauseResume.resume);
		if (pauseResume.resume) {
			//TODO: need a value here to prevent points being recorded
			Session.getInstance().setPaused(false);
			startLogging();
		} else {
			Session.getInstance().setPaused(true);
			cancelGeofences();
			stopLogging();
		}
	}

	/**
	 * Triggered whenever the GPS should be toggled on or off
	 * @param startStop
	 */

	@Subscribe
	public void onEvent(LocationLoggingEvent.StartStop startStop) {
		Logger.l.d("start stop event - start:", startStop.start);
		if (!SharedPreferenceManager.getInstance(this).isRecordingPaused()) {
			if (startStop.start) {
				startLogging();
			} else {
				stopLogging();
			}
		}
	}

	/**
	 * Triggered when a geofence is exited
	 * @param geofenceExit
	 */

	@Subscribe
	public void onEvent(LocationLoggingEvent.GeofenceExit geofenceExit) {
		Logger.l.d("geofence exit event");
		RecordingUtils.cancelGeofenceNotification(getApplicationContext());
		Session.getInstance().setGeofenceState(Session.GeofenceState.NONE);
		EventBus.getDefault().post(new LocationLoggingEvent.StartStop(true));
	}

	/**
	 * Triggered when a geofence is entered
	 * @param geofenceEnter
	 */

	@Subscribe
	public void onEvent(LocationLoggingEvent.GeofenceEnter geofenceEnter) {
		Logger.l.d("geofence entered event");
		Session.getInstance().setGeofenceState(Session.GeofenceState.ACTIVE);
		getGeofenceManager().scheduleGeofenceLoiteringAlarm();
		RecordingUtils.cancelGeofenceNotification(getApplicationContext());
	}

	/**
	 * Triggered when a geofence loitering alarm is issued.
	 * @param entered
	 */

	@Subscribe
	public void onEvent(LocationLoggingEvent.PromptGeofenceLoiter entered) {
		Logger.l.d("geofence loiter triggered");

		Session.getInstance().setGeofenceState(Session.GeofenceState.LOITER);
		EventBus.getDefault().post(new LocationLoggingEvent.StartStop(false));
		getGeofenceManager().scheduleGeofenceDwellAlarm();

	}

	/**
	 * Triggered when a dwell alarm is issued. This will prompt the user to record their trip purpose
	 * if necessary
	 * @param entered
	 */

	@Subscribe
	public void onEvent(LocationLoggingEvent.PromptGeofenceDwell entered) {
		Logger.l.d("geofence dwell triggered");
		Session.getInstance().setGeofenceState(Session.GeofenceState.DWELL_NO_PROMPT);

		SharedPreferenceManager sp = SharedPreferenceManager.getInstance(this);

		// check to make sure we haven't recorded all trips already, and that we aren't continuing recording
		if (!sp.getOngoingPrompts() && (SharedPreferenceManager.getInstance(this).getNumberOfRecordedPrompts() >= sp.getMaximumNumberOfPrompts()))
			return;

		if (!sp.getHasDwelledOnce()) {
			// here we record the very first stop without notification as a reference. It does not count toward their recorded trip count
			sp.setHasDwelledOnce(true);
			return;
		}

		// check the last geofence for proximity so we don't bother people unnecessarily
		PromptAnswer lastPromptAnswer = LocationDatabase.getInstance(this).promptDao().getLastPromptAnswer();

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

		// This is the positive button pending intent from the notification
		Intent positiveIntent = new Intent(this, MapActivity.class);
		positiveIntent.putExtra(GEOFENCE_INTENT_EXTRA, true);

		// The stack builder object will contain an artificial back stack for the started Activity.
		// This ensures that navigating backward from the Activity leads out of your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MapActivity.class);

		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(positiveIntent);
		PendingIntent positivePendingIntent =
			stackBuilder.getPendingIntent(
				0,
				PendingIntent.FLAG_ONE_SHOT
			);

		// This is the negative button pending intent from the notification
		Intent falseIntent = new Intent(this, NotificationDismissReceiver.class);
		falseIntent.putExtra("notificationId", GEOFENCE_NOTIFICATION_ID);
		PendingIntent falsePendingIntent = PendingIntent.getBroadcast(this, 0, falseIntent, 0);

		NotificationCompat.Action positive = new NotificationCompat.Action(R.drawable.ic_done_white_24dp, getString(R.string.notification_true), positivePendingIntent);
		NotificationCompat.Action negative = new NotificationCompat.Action(R.drawable.ic_clear_white_24dp, getString(R.string.notification_false), falsePendingIntent);

		// If the activity is currently in the forefront, prompt the user immediately
		if (((DMApplication) getApplication()).isActivityMapActive()) {
			EventBus.getDefault().post(new LocationLoggingEvent.ShowGeofencePromptInActivity());
			Logger.l.d("MapActivity open - showing prompt in activity");
			return;
		}

		// Build the notification to show the user

		Builder builder = new Builder(this, "trip")
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
				.setSmallIcon(R.drawable.itinerum_notification_icon)
				.setContentIntent(positivePendingIntent)
				.setContentTitle(getString(R.string.notification_stopped_title))
				.setContentText(getString(R.string.notification_stopped_message))
				.addAction(negative)
				.addAction(positive)
				.setVibrate(mPattern)
				.setUsesChronometer(true)
				.setStyle(new NotificationCompat.BigTextStyle()
				.setBigContentTitle(getString(R.string.notification_stopped_title))
				.bigText(getString(R.string.notification_stopped_message)))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(false);

		NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(GEOFENCE_NOTIFICATION_ID, builder.build());

		Logger.l.d("MapActivity closed - prompt notification triggered");
	}
}
