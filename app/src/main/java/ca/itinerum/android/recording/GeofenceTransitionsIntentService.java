package ca.itinerum.android.recording;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.greenrobot.eventbus.EventBus;

import ca.itinerum.android.utilities.LocationLoggingEvent;
import ca.itinerum.android.utilities.Logger;

/**
 * Created by stewjacks on 15-09-20.
 */
public class GeofenceTransitionsIntentService extends IntentService {

	public GeofenceTransitionsIntentService() {
		super("GeofenceTransitionsIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

		if (geofencingEvent.hasError()) {
			Logger.l.e("error with geofence", geofencingEvent.getErrorCode());
			return;
		}
		Logger.l.d("geofence transition", geofencingEvent.getGeofenceTransition());
		if (geofencingEvent.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_EXIT) {
			EventBus.getDefault().post(new LocationLoggingEvent.GeofenceExit());
		}
	}
}
