package ca.itinerum.android.survey;

import ca.itinerum.android.sync.retrofit.Survey;

/**
 * Created by stewjacks on 2017-01-19.
 */

public interface SurveyQuestion {
	Object getSurveyResponse();
	String getSurveyQuestionColumnName();
	Survey getSurvey();
	void setSurvey(Survey survey);
	void setResult(Object result);
	boolean canAdvance();
	boolean returnsResult();
	void setAdvanceListener(SurveyAdvanceListener listener);
}
