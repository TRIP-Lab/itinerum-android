package ca.itinerum.android.utilities;

import android.content.Context;

import org.joda.time.DateTime;

import ca.itinerum.android.recording.CutoffJob;
import ca.itinerum.android.recording.RecordingUtils;

public class CutoffScheduler {

	public enum CutoffResponse{
		ONGOING, COMPLETE, SCHEDULED
	}

	private CutoffScheduler() {}

	public static CutoffResponse Schedule(Context context) {
		DateTime cutoff = RecordingUtils.getAbsuluteCutoffDateTime(context);
		if (cutoff == null) return CutoffResponse.ONGOING; // Do not schedule cutoff for ongoing surveys

		if (cutoff.isBefore(DateTime.now())) return CutoffResponse.COMPLETE; // the survey is complete

		CutoffJob.scheduleJob(cutoff);
		return CutoffResponse.SCHEDULED;
	}

}
