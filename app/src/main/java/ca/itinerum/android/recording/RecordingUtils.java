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

import com.evernote.android.job.JobManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import ca.itinerum.android.sync.DataSyncJob;
import ca.itinerum.android.utilities.SharedPreferenceManager;

public class RecordingUtils {

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

	/**
	 * Calculates the number of days remaining in the survey
	 * @param context
	 * @return number of days remaining in survey
	 */
	public static long getCutoffDays(Context context) {
		long endTime = getCutoffTime(context);
		if (endTime == -1) return endTime;

		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());

		return TimeUnit.MILLISECONDS.toDays(
			Math.abs(endTime - now.getTimeInMillis()));

	}

	/**
	 * Gets the absolute end time of this survey in epoch milliseconds
	 *
	 * This will return -1 for a finished survey
	 * @param context
	 * @return absolute survey end time
	 */
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
		if (isOngoing(context)) return false;

		return (RecordingUtils.getCutoffTime(context) <= 0);
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
	 * Schedule a job to stop recording when the survey should complete
	 * @param context
	 */
	public static void scheduleSurveyCutoffJob(Context context) {
		CutoffJob.scheduleJob(RecordingUtils.getCutoffTime(context) - System.currentTimeMillis());
	}

	/**
	 * Cancel and remove global notification from the status bar regarding a geofence dwell prompt
	 * @param context
	 */
    public static void cancelGeofenceNotification(Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(LocationLoggingService.GEOFENCE_NOTIFICATION_ID);
    }
}
