package ca.itinerum.android.recording;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.evernote.android.job.JobManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.utilities.LocationLoggingEvent;
import ca.itinerum.android.utilities.Logger;

@SuppressWarnings("HardCodedStringLiteral")
public class GeofenceManager {
	public static final int GEOFENCE_RADIUS = 100;

	private final Context mContext;
	private final GoogleApiClient mLocationApiClient;

	private Location mCurrentLocation;

	private boolean mGeofenceIsActive;

	private static final String GEOFENCE_NAME = "rolling-geofence";
	private final int LOITERING_DELAY = 60*1000;
	private final int DWELL_DELAY = 3*60*1000; //BuildConfig.DEBUG ? 10*1000 : 5*60*1000;

	private int mLoiteringId = -1;
	private int mDwellId = -1;

	private GeofencingClient mGeofencingClient;
	private Geofence mCurrentGeofence;
	private List<Geofence> mCurrentGeofences = new ArrayList<>();
	private PendingIntent mGeofencePendingIntent;

	/**
	 * Geofence logic
	 */

	public GeofenceManager(Context context) {
		mGeofencingClient = LocationServices.getGeofencingClient(context);
		mContext = context;

		GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
				.addApi(ActivityRecognition.API)
				.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

					@Override
					public void onConnectionSuspended(int arg) {}

					@Override
					public void onConnected(Bundle arg0) {
						try {
							Logger.l.d("Google Location.API connected");

						}
						catch(Throwable t) {
							Logger.l.e("Can't connect to Google Location.API", t);
						}
					}

				})
				.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(ConnectionResult arg0) {
						Logger.l.w("Google Location.API connection failed", arg0.toString());
					}
				});

		mLocationApiClient = builder.build();
		mLocationApiClient.connect();
	}

	/**
	 * Check high accuracy location point against current geofence
	 * @param location
	 * @return boolean point is outside acceptable geofence bounds
	 */

	private boolean highAccuracyPointOutsideBounds(@NonNull Location location) {
		boolean outside = mCurrentLocation != null
				&& location.distanceTo(mCurrentLocation) > GEOFENCE_RADIUS;

		return outside;
	}

	/**
	 * Check low accuracy location point against current geofence
	 * @param location
	 * @return boolean point is outside acceptable geofence bounds
	 */

	private boolean lowAccuracyPointOutsideBounds(@NonNull Location location) {
		return mCurrentLocation != null
				&& location.distanceTo(mCurrentLocation) > mCurrentLocation.getAccuracy(); //larger
	}

	/**
	 * Check a "bad" location point (inaccurate, wifi/gsm, etc.) against the current geofence to
	 * decide if it should be dismissed in favour of a new point. If yes, replace the current geofence
	 * @param location
	 */

	public void handleGeofenceForBadLocation(Location location) {

//		Logger.l.d("handle active geofence exists for low accuracy point outside bounds", lowAccuracyPointOutsideBounds(location),  mGeofenceIsActive.toString());

		// don't remove geofences for bad locations unless we're really far off.
		if (!mGeofenceIsActive || (location.getAccuracy() < 500 && lowAccuracyPointOutsideBounds(location))) {
//			removeGeofenceForCurrentLocation();
			mCurrentLocation = location;
			addGeofenceForCurrentLocation();
		}
	}

	/**
	 * Check if a "good" location point (high accuracy, GPS) against the current geofence to decide
	 * if the geofence should be replaced in favour of a new point. If yes, replace the current geofence
	 * @param location
	 */

	public void handleGeofenceForGoodLocation(Location location) {
		Logger.l.d("handleGeofenceForGoodLocation mGeofenceIsActive", mGeofenceIsActive, "outside bounds", highAccuracyPointOutsideBounds(location));

		if (!mGeofenceIsActive || highAccuracyPointOutsideBounds(location)) {

//			removeGeofenceForCurrentLocation();
			mCurrentLocation = location;
			addGeofenceForCurrentLocation();
		}
	}

	/**
	 * add the current location as a geofence
	 */

	// permissions checked elsewhere
	@SuppressLint("MissingPermission")
	private void addGeofenceForCurrentLocation() {
		Logger.l.d("adding geofence for current location");

		Geofence geofence = new Geofence.Builder()
				.setRequestId(GEOFENCE_NAME)
				.setCircularRegion(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), GEOFENCE_RADIUS)
				.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
				.setNotificationResponsiveness(0)
				.setExpirationDuration(Geofence.NEVER_EXPIRE)
				.build();

		mCurrentGeofences.add(geofence);

		GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
		builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
		builder.addGeofence(geofence);
		GeofencingRequest geofenceRequest = builder.build();

		EventBus.getDefault().post(new LocationLoggingEvent.GeofenceEnter());
		Session.getInstance().setGeofenceState(Session.GeofenceState.ACTIVE);

		if (BuildConfig.SHOW_DEBUG) {
			Session.getInstance().setGeofenceLatLng(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
		}

		mGeofenceIsActive = true;

		mGeofencingClient.addGeofences(geofenceRequest, getGeofencePendingIntent()).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if (task.isSuccessful()) {
					Logger.l.d("geofence successfully added");
				} else Logger.l.e("failed to add geofence", task.getException().getMessage());
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
//				((ApiException) e).getMessage();
				e.getMessage();
				Logger.l.e("failed to add geofence", e.getMessage());
				mGeofenceIsActive = false;
			}
		});
	}

	/**
	 * Remove the current geofence from Pathsense, and cancel all associated alarms
	 */

	public void removeGeofenceForCurrentLocation() {
		// remove by name doesn't seem to be threadsafe
		Logger.l.d("removeGeofenceForCurrentLocation");
		mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				Logger.l.d("Geofence successfully removed");
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Logger.l.e("failed to remove geofence", e.toString());
			}
		});
		mCurrentGeofences = new ArrayList<>();

		Session.getInstance().setGeofenceState(Session.GeofenceState.NONE);
		mGeofenceIsActive = false;
		cancelAllGeofenceAlarms();
	}

	private PendingIntent getGeofencePendingIntent() {
		if (mGeofencePendingIntent != null) return mGeofencePendingIntent;

		Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
		mGeofencePendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return mGeofencePendingIntent;
	}

	/**
	 * Geofence Loitering Alarm
	 * Primary alarm for disabling location services if the device has been stationary for LOITERING_DELAY
	 */

	public void scheduleGeofenceLoiteringAlarm() {
		cancelAllGeofenceAlarms();
		mLoiteringId = GeofenceLoiterJob.scheduleJob(LOITERING_DELAY);
	}

	/**
	 * Geofence Dwell Alarm
	 * Secondary alarm to prompt the user if they have been stationary for DWELL_DELAY after LOITERING_DELAY
	 */

	public void scheduleGeofenceDwellAlarm() {
		cancelAllGeofenceAlarms();
		mDwellId = GeofenceDwellJob.scheduleJob(DWELL_DELAY);
	}

	/**
	 * Alarm cancellation methods
	 */

	private void cancelGeofenceLoiteringAlarm() {
		JobManager.instance().cancelAllForTag(GeofenceLoiterJob.TAG);
	}

	private void cancelGeofenceDwellAlarm() {
		JobManager.instance().cancelAllForTag(GeofenceDwellJob.TAG);
	}

	void cancelAllGeofenceAlarms() {
		cancelGeofenceDwellAlarm();
		cancelGeofenceLoiteringAlarm();
	}

	void cancelGeofences() {
		removeGeofenceForCurrentLocation();
		cancelAllGeofenceAlarms();
	}

}
