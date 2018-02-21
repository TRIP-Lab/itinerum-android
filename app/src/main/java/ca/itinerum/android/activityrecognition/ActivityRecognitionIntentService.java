/**
 * ActivityRecognitionIntentService receives Activity Recognition results when available and parses them appropriately.
 * This service sends a broadcast to toggle location services on and off. Data is logged in a database, which is synced.
 * It also takes the most recent Wifi scan result as evidence as to whether or not to start/stop recording points.
 *
 * @author stewjacks
 */

package ca.itinerum.android.activityrecognition;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import ca.itinerum.android.recording.Session;

public class ActivityRecognitionIntentService extends IntentService {

    public ActivityRecognitionIntentService() {
		super(ActivityRecognitionIntentService.class.getSimpleName());
	}

    /** Called when a new activity detection update is available.
     *
     */

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {

		if (!ActivityRecognitionResult.hasResult(intent)) return;

		ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
		DetectedActivity activity = result.getMostProbableActivity();
		if (activity.getType() != DetectedActivity.TILTING) Session.getInstance().setCurrentDetectedActivity(activity.getType());
	}

}
