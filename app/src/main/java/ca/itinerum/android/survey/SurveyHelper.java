package ca.itinerum.android.survey;

import android.content.Context;
import android.support.compat.BuildConfig;

import org.apache.commons.lang3.NotImplementedException;

import ca.itinerum.android.R;
import ca.itinerum.android.survey.views.BaseSurveyView;
import ca.itinerum.android.survey.views.EmailView;
import ca.itinerum.android.survey.views.EthicsView;
import ca.itinerum.android.survey.views.LocalSingleSelectView;
import ca.itinerum.android.survey.views.LocationPickerView;
import ca.itinerum.android.survey.views.NumberTextEntryView;
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

		BaseSurveyView view = null;
		// most of these need to be constructed
		switch (survey.getId()) {
			case SELECT_ONE:
				view = new RemoteSingleSelectView(context, survey);
				break;
			case SELECT_MANY:
				view = new RemoteMultiSelectView(context, survey);
				break;
			case NUMBER_INPUT:
				view = new NumberTextEntryView(context, survey);
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
				survey.setPrompt(context.getString(R.string.question_sex_title));
				view = new LocalSingleSelectView(context, survey);
				((LocalSingleSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.question_gender));
				break;
			case AGE:
				survey.setPrompt(context.getString(R.string.question_age_title));
				view = new LocalSingleSelectView(context, survey);
				((LocalSingleSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.question_age));
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
				view = new LocalSingleSelectView(context, survey);
				((LocalSingleSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.primary_mode_question));
				break;
			case TRAVEL_MODE_WORK:
				survey.setPrompt(String.format(context.getString(R.string.primary_mode_question), context.getString(R.string.travel_work_component)));
				view = new LocalSingleSelectView(context, survey);
				((LocalSingleSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.primary_mode_question));
				break;
			case TRAVEL_MODE_ALT_STUDY:
				survey.setPrompt(String.format(context.getString(R.string.secondary_mode_question), context.getString(R.string.travel_school_component)));
				view = new LocalSingleSelectView(context, survey);
				((LocalSingleSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.secondary_mode_question));
				break;
			case TRAVEL_MODE_ALT_WORK:
				survey.setPrompt(String.format(context.getString(R.string.secondary_mode_question), context.getString(R.string.travel_work_component)));
				view = new LocalSingleSelectView(context, survey);
				((LocalSingleSelectView) view).setArrayResource(context.getResources().getStringArray(R.array.secondary_mode_question));
				break;
			default:
				// Null check this later and immediately advance if null
				// We don't want the app to crash if the json is malformed.
				if (BuildConfig.DEBUG) throw new NotImplementedException("Survey type " + survey.getId() + " is not defined.");
		}

		return view;

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
