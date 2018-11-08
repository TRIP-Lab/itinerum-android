package ca.itinerum.android.utilities;

import android.content.Context;

import ca.itinerum.android.R;
import ca.itinerum.android.preamble.AboutActivity;
import ca.itinerum.android.preamble.LetsGoActivity;
import ca.itinerum.android.preamble.SystemAccessActivity;
import ca.itinerum.android.preamble.TermsOfServiceActivity;
import ca.itinerum.android.preamble.WelcomeActivity;
import ca.itinerum.android.survey.SurveyActivity;

public class PreambleActivitiesHelper {
	PreambleActivitiesHelper(){}

	public static Class<?> getNextActivity(Context context, String currentActivity) {
		String[] activities = context.getResources().getStringArray(R.array.preamble_activities);
		int index = -1;
		for (int i = 0; i < activities.length; i++) {
			if (activities[i].equals(currentActivity)) {
				index = i;
				break;
			}
		}

		if (index > -1 && index < activities.length - 1) {
			return getActivityForString(activities[index + 1]);
		}

		return null;
	}

	public static Class<?> getPreviousActivity(Context context, String currentActivity) {
		String[] activities = context.getResources().getStringArray(R.array.preamble_activities);
		int index = -1;
		for (int i = 0; i < activities.length; i++) {
			if (activities[i].equals(currentActivity)) {
				index = i;
				break;
			}
		}

		if (index > 0 && index < activities.length) {
			return getActivityForString(activities[index - 1]);
		}

		return null;
	}

	private static Class<?> getActivityForString(String activity) {
		switch (activity) {
			case "WelcomeActivity":
				return WelcomeActivity.class;
			case "AboutActivity":
				return AboutActivity.class;
			case "SystemAccessActivity":
				return SystemAccessActivity.class;
			case "TermsOfServiceActivity":
				return TermsOfServiceActivity.class;
			case "LetsGoActivity":
				return LetsGoActivity.class;
			case "SurveyActivity":
				return SurveyActivity.class;
		}
		return null;
	}

}
