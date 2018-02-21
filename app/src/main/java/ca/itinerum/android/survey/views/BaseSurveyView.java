package ca.itinerum.android.survey.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import ca.itinerum.android.survey.SurveyAdvanceListener;
import ca.itinerum.android.survey.SurveyQuestion;
import ca.itinerum.android.sync.retrofit.Survey;

/**
 * Created by stewjacks on 2017-01-19.
 */

public abstract class BaseSurveyView extends LinearLayout implements SurveyQuestion {

	Survey mSurvey;
	Context mContext;
	SurveyAdvanceListener mListener;

	public BaseSurveyView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		setOrientation(VERTICAL);
	}


	@Override
	public String getSurveyQuestionColumnName() {
		return mSurvey.getColName();
	}

	@Override
	public Survey getSurvey() {
		return mSurvey;
	}

	@Override
	public void setSurvey(Survey survey) {
		mSurvey = survey;
	}

	@Override
	public boolean returnsResult() {
		return true;
	}

	@Override
	public void setAdvanceListener(SurveyAdvanceListener listener) {
		mListener = listener;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mListener = null;
	}
}
