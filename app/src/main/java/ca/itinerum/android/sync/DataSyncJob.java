package ca.itinerum.android.sync;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import ca.itinerum.android.R;
import ca.itinerum.android.recording.RecordingUtils;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.ServiceEvents;
import ca.itinerum.android.utilities.SharedPreferenceManager;

/**
 * This is class is in charge of scheduling the next attempts to sync the data
 * points.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class DataSyncJob extends Job {

	public static final String PERIODIC_TAG = "periodic_data_sync_job";
	public static final String IMMEDIATE_TAG = "data_sync_job";

	@NonNull
	@Override
	protected Result onRunJob(Params params) {
		DataSender dataSender = new DataSender(getContext());

		boolean success = true;

		try {
			dataSender.sync();
		} catch (Exception e) {
			success = false;
			Logger.l.e(e.toString());
		}

		postUserMessage(success);

		if (success) {
			if (RecordingUtils.isComplete(getContext())) {
				JobManager.instance().cancelAllForTag(DataSyncJob.PERIODIC_TAG);
			}

			return Result.SUCCESS;
		}

		return Result.FAILURE;
	}

	private void postUserMessage(final boolean successful) {

		Logger.l.i("sync to backend successful:", successful);
		EventBus.getDefault().post(new ServiceEvents.UserMessage(successful ? R.string.snackbar_sync_successful : R.string.snackbar_sync_failed));
	}

	public static int scheduleImmediateJob() {
		return new JobRequest.Builder(DataSyncJob.IMMEDIATE_TAG)
				.startNow()
				.build()
				.schedule();
	}

	public static int schedulePeriodicJob() {
		return new JobRequest.Builder(DataSyncJob.PERIODIC_TAG)
				.setPeriodic(TimeUnit.HOURS.toMillis(6), TimeUnit.HOURS.toMillis(1))
				.setRequiredNetworkType(JobRequest.NetworkType.UNMETERED)
				.setUpdateCurrent(true)
				.build()
				.schedule();
	}
}