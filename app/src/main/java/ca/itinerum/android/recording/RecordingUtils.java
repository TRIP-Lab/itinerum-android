/*
*    This file is part of GPSLogger for Android.
*
*    GPSLogger for Android is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 2 of the License, or
*    (at your option) any later version.
*
*    GPSLogger for Android is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
*/

package ca.itinerum.android.recording;

import android.app.NotificationManager;
import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;

import com.evernote.android.job.JobManager;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Calendar;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.sync.DataSyncJob;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.db.ItinerumDatabase;

public class RecordingUtils {

	public static double CalculateDistance(Location location1, Location location2) {
		return CalculateDistance(location1.getLatitude(), location1.getLongitude(), location2.getLatitude(), location2.getLongitude());
	}

    public static double CalculateDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        /*
            Haversine formula:
            A = sin²(Δlat/2) + cos(lat1).cos(lat2).sin²(Δlong/2)
            C = 2.atan2(√a, √(1−a))
            D = R.c
            R = radius of earth, 6371 km.
            All angles are in radians
            */

		double deltaLatitude = Math.toRadians(Math.abs(latitude1 - latitude2));
		double deltaLongitude = Math.toRadians(Math.abs(longitude1 - longitude2));
		double latitude1Rad = Math.toRadians(latitude1);
		double latitude2Rad = Math.toRadians(latitude2);

		double a = Math.pow(Math.sin(deltaLatitude / 2), 2) +
				(Math.cos(latitude1Rad) * Math.cos(latitude2Rad) * Math.pow(Math.sin(deltaLongitude / 2), 2));

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return 6371 * c * 1000; //Distance in meters

	}

	public static int getRecordingDays(Context context) {

		if (!StringUtils.isBlank(BuildConfig.ABSOLUTE_CUTOFF)) return -1;
		return SharedPreferenceManager.getInstance(context).getNumberOfRecordingDays();
	}

	/**
	 * Returns the scheduled absolute cutoff time. This can be in the past
	 * or null if the survey is ongoing
	 * @param context
	 * @return the scheduled absolute cutoff time.
	 */

	@Nullable
	public static DateTime getAbsuluteCutoffDateTime(Context context) {
		// if there is a hardcoded end date, it takes precedence
		if (!StringUtils.isBlank(BuildConfig.ABSOLUTE_CUTOFF)) {
			return DateTime.parse(BuildConfig.ABSOLUTE_CUTOFF);
		}

		SharedPreferenceManager sp = SharedPreferenceManager.getInstance(context);

		/* schedule cutoff to some time in the future. This might be a problem with
		the scheduler library?
		 */
		if (RecordingUtils.isOngoing(context)){
			return null;
		}

		// get the number of days to schedule
		final long surveyTime = sp.getQuestionnaireCompleteDate();
		final int numDays = sp.getNumberOfRecordingDays();

		// the end time is midnight of the day the survey was completed plus the number of survey days
		return (new DateTime(surveyTime)).plusDays(1).withMillisOfDay(0).plusDays(numDays);

	}

	/** TODO: convert to joda datetime
	 * Calculates the number of days remaining in the survey
	 * @param context
	 * @return number of days remaining in survey
	 */
	public static long getCutoffDays(Context context) {

		if (!StringUtils.isBlank(BuildConfig.ABSOLUTE_CUTOFF)) {
			DateTime dateTime = DateTime.parse(BuildConfig.ABSOLUTE_CUTOFF);
			return Days.daysBetween(DateTime.now().toLocalDate(), dateTime.toLocalDate()).getDays();
		}

		int numDays = SharedPreferenceManager.getInstance(context).getNumberOfRecordingDays();

		if (numDays <= 0) {
			return TimeUnit.MILLISECONDS.toDays(Math.abs(System.currentTimeMillis() - SharedPreferenceManager.getInstance(context).getQuestionnaireCompleteDate()));
		}

		long endTime = getCutoffTime(context);
		if (endTime == -1) return endTime;

		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());

		return TimeUnit.MILLISECONDS.toDays(
			Math.abs(endTime - now.getTimeInMillis()));

	}

	/**
	 * TODO: convert this to joda datetime re: deprecated
	 * Gets the absolute end time of this survey in epoch milliseconds
	 *
	 * This will return -1 for a finished survey
	 * @param context
	 * @return absolute survey end time
	 */
	@Deprecated
	public static long getCutoffTime(Context context) {
		final long surveyTime = SharedPreferenceManager.getInstance(context).getQuestionnaireCompleteDate();
		final int numDays = SharedPreferenceManager.getInstance(context).getNumberOfRecordingDays();

		// next day at midnight to account for a portion of a day
	    Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(surveyTime);
        startTime.set(Calendar.DATE, startTime.get(Calendar.DATE) + 1);
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);

        Calendar endTime = Calendar.getInstance();
        endTime.setTime(startTime.getTime());
        endTime.add(Calendar.DATE, numDays);

        Calendar now = Calendar.getInstance();
	    now.setTimeInMillis(System.currentTimeMillis());

        if (now.compareTo(endTime) == 1) return -1; //finished

        return endTime.getTimeInMillis();
    }

	/**
	 * Determines if the current survey has an infinite duration
	 * @param context
	 * @return if the survey is indefinite
	 */
	public static boolean isOngoing(Context context) {
		return SharedPreferenceManager.getInstance(context).getNumberOfRecordingDays() == -1;
	}

	/**
	 * Determines if the current survey is complete
	 * @param context
	 * @return if the survey is complete
	 */
    public static boolean isComplete(Context context) {
		// ongoing surveys never complete
    	if (isOngoing(context)) return false;

		SharedPreferenceManager pm = SharedPreferenceManager.getInstance(context);

		// if we're past the cutoff date, we're done
		if (RecordingUtils.getAbsuluteCutoffDateTime(context).isBefore(DateTime.now())) return true;

		// if we haven't completed prompts, check cutoff. Cutoff trumps user request.
		if (!hasFinishedAutomaticPrompts(context)) return false;

		// return user pref - this value is true if the user has requested that prompts continue after the max number
		return !pm.getOngoingPrompts() && pm.getHasRespondedToContinueMessage();

    }

    public static boolean hasFinishedAutomaticPrompts(Context context) {

		SharedPreferenceManager pm = SharedPreferenceManager.getInstance(context);

		int maximumNumberOfPrompts = pm.getMaximumNumberOfPrompts();
		int numberOfPrompts = pm.getNumberOfPrompts();

		if (numberOfPrompts == 0) return false;

		int numberOfRecordedPrompts = ItinerumDatabase.getInstance(context).promptDao().getAllAutomaticPromptAnswersCount() / numberOfPrompts;

		// if we haven't completed prompts, check cutoff. Cutoff trumps user request.
		return numberOfRecordedPrompts >= maximumNumberOfPrompts;
	}

	public static boolean shouldContinuePromptsAfterMax(Context context) {
		SharedPreferenceManager pm = SharedPreferenceManager.getInstance(context);
		return !pm.getOngoingPrompts() && pm.getHasRespondedToContinueMessage();
	}

	/**
	 * Schedule a job to periodically sync database data to backend
	 * @param context
	 */
	public static void schedulePeriodicUpdates(Context context) {
		// Don't schedule periodic recordings if we've already synced since the end of the survey
		if (SharedPreferenceManager.getInstance(context).getLastSyncDate().getMillis() > RecordingUtils.getCutoffTime(context)) {
			return;
		}

		if (!JobManager.instance().getAllJobRequestsForTag(DataSyncJob.PERIODIC_TAG).isEmpty()) return;
		DataSyncJob.schedulePeriodicJob();
	}

	/**
	 * Cancel and remove global notification from the status bar regarding a geofence dwell prompt
	 * @param context
	 */
    public static void cancelGeofenceNotification(Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(LocationLoggingService.GEOFENCE_NOTIFICATION_ID);
    }

    public static Comparator getTimeComparator() {
    	return new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String[] a1 = o1.split(":");
				String[] a2 = o2.split(":");

				int h1 = Integer.parseInt(a1[0]);
				int h2 = Integer.parseInt(a2[0]);

				if (h1 > h2) return 1;
				if (h1 < h2) return -1;

				int m1 = Integer.parseInt(a1[1]);
				int m2 = Integer.parseInt(a2[1]);

				if (m1 > m2) return 1;
				if (m1 < m2) return -1;

				return 0;

			}
		};
	}
}
