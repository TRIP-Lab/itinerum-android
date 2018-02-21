package ca.itinerum.android.recording;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import ca.itinerum.android.DMApplication;


@SuppressWarnings("HardCodedStringLiteral")
public class CutoffJob extends Job {
	public static final String TAG = "absolute_cutoff_job";

	@NonNull
	@Override
	protected Result onRunJob(Params params) {

		((DMApplication) getContext().getApplicationContext()).stopLoggingService();
		return Result.SUCCESS;
	}

	public static int scheduleJob(final long delay) {
		return new JobRequest.Builder(CutoffJob.TAG)
				.setExecutionWindow(delay, delay + 60*60*1000)
				.setUpdateCurrent(true)
				.build()
				.schedule();
	}
}
