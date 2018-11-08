package ca.itinerum.android.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import ca.itinerum.android.ContentOverlayView;
import ca.itinerum.android.sync.retrofit.Prompt;
import ca.itinerum.android.sync.retrofit.Results;
import ca.itinerum.android.sync.retrofit.Survey;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

@SuppressWarnings("HardCodedStringLiteral")
@SuppressLint("ApplySharedPref")
public class SharedPreferenceManager
{
	private static final String CURRENT_VERSION = "CURRENT_VERSION";
	private static final String KEY_DATE_STATE = "DATE_STATE";
	private static final String SURVEY_COMPLETED_DATE = "SURVEY_COMPLETED_DATE";
	private static final String PAUSE_RECORDING = "PAUSE_RECORDING";
	private static final String HAS_SEEN_TUTORIAL = "HAS_SEEN_TUTORIAL";
	private static final String SURVEY_NAME = "SURVEY_NAME";
	private static final String UNIQUE_ID = "UNIQUE_ID";
	private static final String SURVEY_RESPONSES = "SURVEY_RESPONSES";
	private static final String SURVEY_ID = "SURVEY_ID";
	private static final String NUM_PROMPTS = "NUM_PROMPTS";
	private static final String MAX_PROMPTS = "MAX_PROMPTS";
	private static final String NUM_DAYS = "NUM_DAYS";
	private static final String SURVEY_RESPONSE_OBJECT = "SURVEY_RESPONSE_OBJECT";
	private static final String TOS = "TOS";
	private static final String KEY_FROM_DATE = "from_date";
	private static final String KEY_FROM_TIME = "from_time";
	private static final String KEY_TO_DATE = "to_date";
	private static final String KEY_TO_TIME = "to_time";
	private static final String ONGOING_PROMPTS = "ONGOING_PROMPTS";
	private static final String HAS_DWELLED_ONCE = "HAS_DWELLED_ONCE";
	private static final String KEY_LAST_SYNC_DATE = "KEY_LAST_SYNC_DATE";
	private static final String RESEARCH_ETHICS = "RESEARCH_ETHICS";
	private static final String SEEN_CONTINUE = "SEEN_CONTINUE";

	private Context mContext;
    private final DateFormat mDateFormat;

    private static SharedPreferenceManager instance = null;
	private Subject<DateTime> mLastSyncDateObservable;

	private SharedPreferenceManager() {
        mDateFormat = new SimpleDateFormat("HH:mm", Locale.US);
    }
    
    public static synchronized SharedPreferenceManager getInstance(Context context) {
        if (instance == null) instance = new SharedPreferenceManager();

        instance.mContext = context;  
        
        return instance;
    }
	
	private SharedPreferences getSharedPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(mContext);
	}
    
    public void resetDatesToDefault() {
        Calendar endOfDay = new GregorianCalendar();
		endOfDay.set(Calendar.HOUR_OF_DAY, 23);
		endOfDay.set(Calendar.MINUTE, 59);
		endOfDay.set(Calendar.SECOND, 59);
		endOfDay.set(Calendar.MILLISECOND, 999);

		Calendar startOfDay = new GregorianCalendar();
// reset hour, minutes, seconds and millis
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Editor editor = getSharedPrefs().edit();
        
        editor.putString(KEY_FROM_DATE, DatePreference.formatter().format(startOfDay.getTime()));
        editor.putString(KEY_TO_DATE, DatePreference.formatter().format(endOfDay.getTime()));
        editor.putString(KEY_FROM_TIME, mDateFormat.format(startOfDay.getTime()));
        editor.putString(KEY_TO_TIME, mDateFormat.format(endOfDay.getTime()));
	    setDateState(ContentOverlayView.DateState.TODAY);
        editor.commit();
    }

	public int getCurrentVersion() {
		return getSharedPrefs().getInt(CURRENT_VERSION, 0);
	}

	public void setCurrentVersion(final int version) {
		getSharedPrefs().edit().putInt(CURRENT_VERSION, version).commit();
	}

    public Date getFromDate()
    {
        return this.getDateFromKeys(KEY_FROM_DATE, KEY_FROM_TIME);
    }

    public Date getToDate() {
        return this.getDateFromKeys(KEY_TO_DATE, KEY_TO_TIME);
    }

	public void setToDate(final Date date) {
		Editor editor = getSharedPrefs().edit();

		editor.putString(KEY_TO_DATE, DatePreference.formatter().format(date));
		editor.putString(KEY_TO_TIME, mDateFormat.format(date));
		editor.commit();
	}

	public void setFromDate(final Date date) {
		Editor editor = getSharedPrefs().edit();

		editor.putString(KEY_FROM_DATE, DatePreference.formatter().format(date));
		editor.putString(KEY_FROM_TIME, mDateFormat.format(date));
		editor.commit();
	}

	public void setDateState(final ContentOverlayView.DateState state) {
		Editor editor = getSharedPrefs().edit();
		editor.putString(KEY_DATE_STATE, state.name());
		editor.commit();
	}

	public ContentOverlayView.DateState getDateState() {
		String savedDateStateName = getSharedPrefs().getString(KEY_DATE_STATE, null);
		if (savedDateStateName == null) {
			setDateState(ContentOverlayView.DateState.TODAY);
			savedDateStateName = getSharedPrefs().getString(KEY_DATE_STATE, "TODAY");
		}

		return ContentOverlayView.DateState.valueOf(savedDateStateName);

	}
    
    private Date getDateFromKeys(final String dateKey, final String timeKey)
    {
        String dateStringValue = getSharedPrefs().getString(dateKey, DatePreference.defaultCalendar().toString());
        
        Date returnDate;
        try
        {
            returnDate = DatePreference.formatter().parse(dateStringValue);
            String timeStringValue = getSharedPrefs().getString(timeKey, "00:00");
            String[] times = timeStringValue.split(":");
            int hours = Integer.parseInt(times[0]);
            int minutes = Integer.parseInt(times[1]);
            
            // Adding the hours and minutes
            returnDate.setTime(returnDate.getTime() + hours * 60 * 60 * 1000 + minutes * 60 * 1000);
            
            return returnDate;
        }
        catch (ParseException e)
        {
            return null;
        }       
    }

	public String getUUID() {
		return getSharedPrefs().getString(UNIQUE_ID, null);
	}

	public void setUUID(final String uuid) {
		getSharedPrefs().edit().putString(UNIQUE_ID, uuid).commit();
	}

	private void setQuestionnaireCompleteDate(final long time) {
		getSharedPrefs().edit().putLong(SURVEY_COMPLETED_DATE, time).commit();
	}

	// helper function
	public void setQuestionnaireCompleteDateToNow() {
		setQuestionnaireCompleteDate(Calendar.getInstance().getTimeInMillis());
	}

	/* This is the date the initial survey was complete and the app started recording
	 */
	public long getQuestionnaireCompleteDate() {
		return getSharedPrefs().getLong(SURVEY_COMPLETED_DATE, 0);
	}

	public boolean hasCompletedQuestionnaire() {
		return getQuestionnaireCompleteDate() != 0;
	}

	public long getLastSyncTime() {
		return getLastSyncDate().getMillis();
	}

	@NonNull
	public DateTime getLastSyncDate() {

		return new DateTime(getSharedPrefs().getString(KEY_LAST_SYNC_DATE, "0"));
	}

	public Subject<DateTime> getLastSyncDateObservable() {
		if (mLastSyncDateObservable == null) mLastSyncDateObservable = BehaviorSubject.create();
		mLastSyncDateObservable.onNext(getLastSyncDate());
		return mLastSyncDateObservable;

	}

	public void setLastSyncDate(@NonNull final String date) {
		getLastSyncDateObservable();
		getSharedPrefs().edit().putString(KEY_LAST_SYNC_DATE, date).commit();
		mLastSyncDateObservable.onNext(getLastSyncDate());
	}


	public boolean isRecordingPaused() {
		return getSharedPrefs().getBoolean(PAUSE_RECORDING, false);
	}

	public void setRecordingPaused(boolean paused) {
		getSharedPrefs().edit().putBoolean(PAUSE_RECORDING, paused).commit();
	}

	public boolean hasSeenTutorial() {
		return getSharedPrefs().getBoolean(HAS_SEEN_TUTORIAL, false);
	}

	public void setSeenTutorial(final boolean seenTutorial) {
		getSharedPrefs().edit().putBoolean(HAS_SEEN_TUTORIAL, seenTutorial).commit();
	}

	public Results getSurveyResponseObject() {
		Gson gson = new Gson();
		String json = getSharedPrefs().getString(SURVEY_RESPONSE_OBJECT, null);
		if (json == null) return null;

		return gson.fromJson(json, new TypeToken<Results>() {}.getType());
	}

	public void setSurveyResponseObject(final Results response) {
		Gson gson = new Gson();
		String s = gson.toJson(response);
		getSharedPrefs().edit().putString(SURVEY_RESPONSE_OBJECT, s).commit();
	}

	public String getSurveyName() {
		return getSharedPrefs().getString(SURVEY_NAME, null);
	}

	public void setSurveyName(final String surveyName) {
		getSharedPrefs().edit().putString(SURVEY_NAME, surveyName).commit();
	}

	public String getAboutText() {
		return getSurveyResponseObject().getAboutText();
	}

	public String getAvatar() {
		String avatar = getSurveyResponseObject().getAvatar();
		return avatar != null ? avatar : "/assets/static/defaultAvatar.png";
	}

	public long getSurveyId() {
		return getSharedPrefs().getLong(SURVEY_ID, -1);
	}

	public void setSurveyId(final long surveyId) {
		getSharedPrefs().edit().putLong(SURVEY_ID, surveyId).commit();
	}

	// helper
	public List<Survey> getSurvey() {
		return getSurveyResponseObject().getSurvey();
	}

	public List<Prompt> getPrompts() {
		return getSurveyResponseObject().getPrompt().getPrompts();
	}

	public ArrayMap<String, Object> getCompletedQuestionnaire() {
		Gson gson = new Gson();
		String json = getSharedPrefs().getString(SURVEY_RESPONSES, null);
		if (json == null) return null;

		return gson.fromJson(json, new TypeToken<ArrayMap<String, Object>>() {}.getType());
	}

	public void setCompletedQuestionnaire(final ArrayMap<String, Object> completedSurvey) {
		Gson gson = new Gson();
		getSharedPrefs().edit().putString(SURVEY_RESPONSES, gson.toJson(completedSurvey)).commit();
	}


	public int getNumberOfPrompts() {
		return getSharedPrefs().getInt(NUM_PROMPTS, -1);
	}

	public void setNumberOfPrompts(final int numberOfPrompts) {
		getSharedPrefs().edit().putInt(NUM_PROMPTS, numberOfPrompts).commit();
	}

	public int getMaximumNumberOfPrompts() {
		return getSharedPrefs().getInt(MAX_PROMPTS, -1);
	}

	public void setMaximumNumberOfPrompts(final int maxPrompts) {
		getSharedPrefs().edit().putInt(MAX_PROMPTS, maxPrompts).commit();
	}

	public int getNumberOfRecordingDays() {
		return getSharedPrefs().getInt(NUM_DAYS, -1);
	}

	public void setNumberOfRecordingDays(final int numberOfRecordingDays) {
		getSharedPrefs().edit().putInt(NUM_DAYS, numberOfRecordingDays).commit();
	}

	public boolean getTermsOfServiceRequired() {
		return getSharedPrefs().getBoolean(TOS, false);
	}

	public void setTermsOfServiceRequired(final boolean termsOfServiceRequired) {
		getSharedPrefs().edit().putBoolean(TOS, termsOfServiceRequired).commit();
	}

	public void setOngoingPrompts(boolean ongoingPrompts) {
		getSharedPrefs().edit().putBoolean(ONGOING_PROMPTS, ongoingPrompts).commit();
	}

	public boolean getOngoingPrompts() {
		return getSharedPrefs().getBoolean(ONGOING_PROMPTS, false);
	}

	public boolean getHasDwelledOnce() {
		return getSharedPrefs().getBoolean(HAS_DWELLED_ONCE, false);
	}

	public void setHasDwelledOnce(boolean hasDwelledOnce) {
		getSharedPrefs().edit().putBoolean(HAS_DWELLED_ONCE, hasDwelledOnce).commit();
	}

	public boolean getHasRespondedToContinueMessage() {
		return getSharedPrefs().getBoolean(SEEN_CONTINUE, false);
	}

	public void setHasRespondedToContinueMessage(boolean seenContinueMessage) {
		getSharedPrefs().edit().putBoolean(SEEN_CONTINUE, seenContinueMessage).commit();
	}

	/**
	 * This is danger and should not be used without understanding the consequences!
	 */
	public void deleteAllSettings() {
		getSharedPrefs().edit().clear().commit();
	}

	public boolean getResearchEthicsAgreement() {
		return getSharedPrefs().getBoolean(RESEARCH_ETHICS, false);
	}

	public void setResearchEthicsAgreement(boolean researchEthicsAgreement) {
		getSharedPrefs().edit().putBoolean(RESEARCH_ETHICS, researchEthicsAgreement).commit();
	}
}
