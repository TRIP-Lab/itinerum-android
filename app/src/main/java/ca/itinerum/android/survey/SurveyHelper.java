package ca.itinerum.android.survey;

import android.content.Context;

import org.apache.commons.lang3.NotImplementedException;

import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.survey.views.BaseSurveyView;
import ca.itinerum.android.survey.views.EmailView;
import ca.itinerum.android.survey.views.EthicsView;
import ca.itinerum.android.survey.views.LocalMultiSelectView;
import ca.itinerum.android.survey.views.LocalSingleSelectView;
import ca.itinerum.android.survey.views.LocationPickerView;
import ca.itinerum.android.survey.views.NumberPickerEntryView;
import ca.itinerum.android.survey.views.OccupationView;
import ca.itinerum.android.survey.views.RemoteMultiSelectView;
import ca.itinerum.android.survey.views.RemoteSingleSelectView;
import ca.itinerum.android.survey.views.TextEntryView;
import ca.itinerum.android.sync.retrofit.Survey;

/**
 * Created by stewjacks on 2017-01-18.
 */

public class SurveyHelper {

	public static final int SELECT_ONE = 1;
	public static final int SELECT_MANY = 2;
	public static final int NUMBER_INPUT = 3;
	public static final int LOCATION_GENERIC = 4;
	public static final int TEXTBOX = 5;
	public static final int TOS = 98;
	public static final int PAGE_BREAK = 99;
	public static final int GENDER = 100;
	public static final int AGE = 101;
	public static final int PRIMARY_MODE = 102;
	public static final int EMAIL = 103;
	public static final int OCCUPATION = 104;
	public static final int LOCATION_HOME = 105;
	public static final int LOCATION_STUDY = 106;
	public static final int LOCATION_WORK = 107;
	public static final int TRAVEL_MODE_STUDY = 108;
	public static final int TRAVEL_MODE_ALT_STUDY = 109;
	public static final int TRAVEL_MODE_WORK = 110;
	public static final int TRAVEL_MODE_ALT_WORK = 111;

	public static BaseSurveyView getSurveyView(Context context, Survey survey) {

		BaseSurveyView view = getSurveyViewForSpecialConditions(context, survey);
		if (view != null) return view;

		// most of these need to be constructed
		switch (survey.getId()) {
			case SELECT_ONE:
				view = new RemoteSingleSelectView(context, survey);
				break;
			case SELECT_MANY:
				view = new RemoteMultiSelectView(context, survey);
				break;
			case NUMBER_INPUT:
				view = new NumberPickerEntryView(context, survey);
				break;
			case TEXTBOX:
				view = new TextEntryView(context, survey);
				break;
			case TOS:
				view = new EthicsView(context, survey);
				break;
			case PAGE_BREAK:
				//TODO: this needs to be hooked up, but I'm not quite sure what it does as of right now
				if (BuildConfig.DEBUG) throw new NotImplementedException("Survey type " + survey.getId() + " is not defined.");
				break;
			case GENDER:
				view = new LocalSingleSelectView(context, survey);
				((LocalSingleSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.question_gender));
				((LocalSingleSelectView) view).hideQuestion();
				break;
			case AGE:
				view = new LocalSingleSelectView(context, survey);
				((LocalSingleSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.question_age));
				((LocalSingleSelectView) view).hideQuestion();
				break;
			case PRIMARY_MODE:
				view = new LocalSingleSelectView(context, survey);
				((LocalSingleSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.primary_mode_question));
				break;
			case EMAIL:
				view = new EmailView(context, survey);
				break;
			case OCCUPATION:
				view = new OccupationView(context, survey);
				break;
			case LOCATION_GENERIC:
			case LOCATION_HOME:
			case LOCATION_STUDY:
			case LOCATION_WORK:
				view = new LocationPickerView(context, survey);
				break;
			case TRAVEL_MODE_STUDY:
				survey.setPrompt(String.format(context.getString(R.string.primary_mode_question), context.getString(R.string.travel_school_component)));
				if (BuildConfig.FLAVOR.equals("montreal")) {
					view = new LocalMultiSelectView(context, survey);
					((LocalMultiSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.primary_mode_question));
					((LocalMultiSelectView) view).setMinResponses(1);
				} else {
					view = new LocalSingleSelectView(context, survey);
					((LocalSingleSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.primary_mode_question));
				}
				break;
			case TRAVEL_MODE_WORK:
				survey.setPrompt(String.format(context.getString(R.string.primary_mode_question), context.getString(R.string.travel_work_component)));
				if (BuildConfig.FLAVOR.equals("montreal")) {
					view = new LocalMultiSelectView(context, survey);
					((LocalMultiSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.primary_mode_question));
					((LocalMultiSelectView) view).setMinResponses(1);
				} else {
					view = new LocalSingleSelectView(context, survey);
					((LocalSingleSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.primary_mode_question));
				}
				break;
			case TRAVEL_MODE_ALT_STUDY:
				survey.setPrompt(String.format(context.getString(R.string.secondary_mode_question), context.getString(R.string.travel_school_component)));
				if (BuildConfig.FLAVOR.equals("montreal")) {
					view = new LocalMultiSelectView(context, survey);
					((LocalMultiSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.secondary_mode_question));
					((LocalMultiSelectView) view).setMinResponses(1);
				} else {
					view = new LocalSingleSelectView(context, survey);
					((LocalSingleSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.secondary_mode_question));
				}
				break;
			case TRAVEL_MODE_ALT_WORK:
				survey.setPrompt(String.format(context.getString(R.string.secondary_mode_question), context.getString(R.string.travel_work_component)));
				if (BuildConfig.FLAVOR.equals("montreal")) {
					view = new LocalMultiSelectView(context, survey);
					((LocalMultiSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.secondary_mode_question));
					((LocalMultiSelectView) view).setMinResponses(1);
				} else {
					view = new LocalSingleSelectView(context, survey);
					((LocalSingleSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.secondary_mode_question));
				}
				break;
			default:
				// Null check this later and immediately advance if null
				// We don't want the app to crash if the json is malformed.
				if (BuildConfig.DEBUG) throw new NotImplementedException("Survey type " + survey.getId() + " is not defined.");
		}

		return view;

	}

	private static BaseSurveyView getSurveyViewForSpecialConditions(Context context, Survey survey) {
		return null;
	}

	public static String getUserVisibleSurveyTitle(Context context, Survey survey) {

		String title = survey.getColName();

		switch (survey.getId()) {
			case LOCATION_HOME:
				title = String.format(context.getString(R.string.location_title), context.getString(R.string.location_home_component));
				break;
			case LOCATION_STUDY:
				title = String.format(context.getString(R.string.location_title), context.getString(R.string.location_school_component));
				break;
			case LOCATION_WORK:
				title = String.format(context.getString(R.string.location_title), context.getString(R.string.location_work_component));
				break;
			case TRAVEL_MODE_WORK:
			case TRAVEL_MODE_ALT_WORK:
				title = context.getString(R.string.travel_work_title);
				break;
			case TRAVEL_MODE_STUDY:
			case TRAVEL_MODE_ALT_STUDY:
				title = context.getString(R.string.travel_school_title);
				break;
			case OCCUPATION:
				title = context.getString(R.string.occupation_title);
				break;
			case AGE:
				title = context.getString(R.string.question_age_title);
				break;
			case GENDER:
				title = context.getString(R.string.question_gender_title);
				break;
			default:
				break;

		}

		return title;
	}

	public static boolean isMapView(Survey survey) {
		return (survey.getId() == LOCATION_GENERIC ||
				survey.getId() == LOCATION_HOME ||
				survey.getId() == LOCATION_WORK ||
				survey.getId() == LOCATION_STUDY);
	}

	public static boolean shouldShowQuestion(Survey survey, boolean isEmployed, boolean isStudent) {
		switch (survey.getId()) {
				// study
			case LOCATION_STUDY:
			case TRAVEL_MODE_STUDY:
			case TRAVEL_MODE_ALT_STUDY:
				return isStudent;
				//work
			case LOCATION_WORK:
			case TRAVEL_MODE_WORK:
			case TRAVEL_MODE_ALT_WORK:
				return isEmployed;
			default:
				return true;

		}
	}
}
