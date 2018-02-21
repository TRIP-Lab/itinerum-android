package ca.itinerum.android.activityrecognition;

import android.content.Context;

import com.google.android.gms.location.DetectedActivity;

import ca.itinerum.android.R;

public class ActivityRecognitionUtils{
	
	private static int previousActivity = DetectedActivity.UNKNOWN;
	
	/**
	 * Parses a detected activity into a human readable string
	 * @param activity
	 * @return
	 */

	public static String parseActivityLocalized(DetectedActivity activity, Context context) {
		if (activity == null) return context.getString(R.string.ar_unknown);

		return parseActivityLocalized(activity.getType(), context);

	}

	public static String parseActivityLocalized(int activity, Context context) {
		switch (activity) {
			case DetectedActivity.TILTING:
				return context.getString(R.string.ar_tilting);
			case DetectedActivity.STILL:
				return context.getString(R.string.ar_still);
			case DetectedActivity.IN_VEHICLE:
				return context.getString(R.string.ar_vehicle);
			case DetectedActivity.ON_BICYCLE:
				return context.getString(R.string.ar_bicycle);
			case DetectedActivity.ON_FOOT:
				return context.getString(R.string.ar_on_foot);
			case DetectedActivity.WALKING:
				return context.getString(R.string.ar_walking);
			case DetectedActivity.RUNNING:
				return context.getString(R.string.ar_running);
			default:
				return context.getString(R.string.ar_unknown);
		}
	}

	public static String parseActivity(DetectedActivity activity) {
		if (activity == null) return "Unknown";

		return parseActivity(activity.getType());

	}

	@SuppressWarnings("HardCodedStringLiteral")
	private static String parseActivity(int activity) {
		switch (activity) {
			case DetectedActivity.TILTING:
				return "Tilting";
			case DetectedActivity.STILL:
				return "Still";
			case DetectedActivity.IN_VEHICLE:
				return "In Vehicle";
			case DetectedActivity.ON_BICYCLE:
				return "Bicycle";
			case DetectedActivity.ON_FOOT:
				return "On Foot";
			case DetectedActivity.WALKING:
				return "Walking";
			case DetectedActivity.RUNNING:
				return "Running";
			default:
				return "Unknown";
		}
	}
    
	/**
	 * Shows a notification displaying a detected activity
	 * @param context
	 * @param activity
	 */
    @SuppressWarnings("HardCodedStringLiteral")
	public static void showActivityNotification(Context context, DetectedActivity activity){
    	String msg;
    	
    	switch(activity.getType()){
    	case DetectedActivity.IN_VEHICLE:
    		msg = "driving";
    		break;
    	case DetectedActivity.ON_BICYCLE:
    		msg = "biking";
    		break;
    	case DetectedActivity.ON_FOOT:
    		msg = "on foot";
    		break;
    	case DetectedActivity.STILL:
    		msg = "standing";
    		break;
    	case DetectedActivity.TILTING:
    		msg = "tilting";
    		break;
		default:
			msg = "default";
    	}
    	
    	if(previousActivity != activity.getType() && activity.getConfidence() > 50){
    		previousActivity = activity.getType();
    	}
    }
}
