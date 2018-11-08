package ca.itinerum.android.recording;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Created by stewart on 2018-03-21.
 */

public class LocationManager {

	public enum LocationState {
		HIGH_POWER,
		MEDIUM_POWER,
		LOW_POWER,
		NO_POWER,
		FRESH
	}

	private final Context mContext;

	private final FusedLocationProviderClient mLocationProviderClient;
	private PendingIntent mLocationPendingIntent;

	/**
	 * High power location request that has highest accuracy
	 */
	private final LocationRequest mActivePriorityRequest = new LocationRequest()
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
			.setInterval(1_000L)
			.setFastestInterval(1_000L);

	/**
	 * Medium priority location request with block-level accuracy or better
	 */
	private final LocationRequest mMediumPriorityRequest = new LocationRequest()
			.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
			.setFastestInterval(1_000L)
			.setInterval(10_000L);

	/**
	 * Low priority location request with low accuracy and low power consumption
	 */
	private final LocationRequest mLowPriorityRequest = new LocationRequest()
			.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
			.setInterval(60_000L)
			.setFastestInterval(10_000L);
	/**
	 * Passive location request that is responsible for no power consumption via location
	 */
	private final LocationRequest mPassivePriorityRequest = new LocationRequest()
			.setPriority(LocationRequest.PRIORITY_NO_POWER)
			.setSmallestDisplacement(25)
			.setFastestInterval(10_000L);

	/**
	 * Location request that attempts to receive three (3) updates in a 10 second period before expiring
	 * Useful for periodic tasks as it will time out automatically. Scheduled with a wait time so these may
	 * come grouped any time in a 10s window
	 */
	private final LocationRequest mFreshUpdatesRequest = new LocationRequest()
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
			.setExpirationDuration(10_000L)
			.setNumUpdates(3)
			.setInterval(1_000L)
			.setFastestInterval(1_000L);

	/**
	 * Location logic
	 */

	public LocationManager(@NonNull Context context) {
		mLocationProviderClient = new FusedLocationProviderClient(context);
		mContext = context;
	}

	public void startUpdates(@NonNull LocationState state) {

		if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}

		LocationRequest request;

		switch (state) {
			case NO_POWER:
				request = mPassivePriorityRequest;
				break;
			case LOW_POWER:
				request = mLowPriorityRequest;
				break;
			case MEDIUM_POWER:
				request = mMediumPriorityRequest;
				break;
			case HIGH_POWER:
				request = mActivePriorityRequest;
				break;
			case FRESH:
				request = mFreshUpdatesRequest;
				break;
			default:
				throw new NotImplementedException("LocationState " + state.name() + " not implemented");
		}

		mLocationProviderClient.requestLocationUpdates(request, getLocationPendingIntent());
	}

	/**
	 * !!This happens asynchronously bc it's a Task so can't be called in rapid succession with startUpdates w/o more thought
	 */
	public void stopUpdates() {
		mLocationProviderClient.removeLocationUpdates(getLocationPendingIntent());
	}

	private PendingIntent getLocationPendingIntent() {
		if (mLocationPendingIntent == null) {
			Intent intent = new Intent(mContext, LocationLoggingService.class);
			intent.setAction(LocationLoggingService.LOCATION_ACTION);
			mLocationPendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		return mLocationPendingIntent;
	}

}
