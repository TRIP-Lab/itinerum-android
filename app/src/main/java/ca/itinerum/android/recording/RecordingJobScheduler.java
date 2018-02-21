package ca.itinerum.android.recording;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import ca.itinerum.android.sync.DataSyncJob;

/**
 * Created by stewjacks on 2017-02-16.
 */

public class RecordingJobScheduler implements JobCreator {

	@Override
	public Job create(String tag) {
		switch (tag) {
			case GeofenceDwellJob.TAG:
				return new GeofenceDwellJob();
			case GeofenceLoiterJob.TAG:
				return new GeofenceLoiterJob();
			case CutoffJob.TAG:
				return new CutoffJob();
			case DataSyncJob.IMMEDIATE_TAG:
				return new DataSyncJob();
			case DataSyncJob.PERIODIC_TAG:
				return new DataSyncJob();
			default:
				return null;
		}
	}
}
