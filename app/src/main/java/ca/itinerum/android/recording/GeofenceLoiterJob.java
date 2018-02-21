package ca.itinerum.android.recording;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import org.greenrobot.eventbus.EventBus;

import ca.itinerum.android.utilities.LocationLoggingEvent;

/**
 * Created by stewjacks on 2017-02-17.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class GeofenceLoiterJob extends Job {
	public static final String TAG = "geofence_loiter_job";

	@NonNull
	@Override
	protected Result onRunJob(Params params) {
		EventBus.getDefault().post(new LocationLoggingEvent.PromptGeofenceLoiter());
		return Result.SUCCESS;
	}

	public static int scheduleJob(int delay) {
		return new JobRequest.Builder(GeofenceLoiterJob.TAG)
				.setExact(delay)
				.build()
				.schedule();
	}
}
