package ca.itinerum.android.survey.views;

import android.content.Context;
import android.util.AttributeSet;

import ca.itinerum.android.R;
import ca.itinerum.android.sync.retrofit.Survey;

/**
 * Created by stewjacks on 2017-01-19.
 */

public class EthicsView extends BaseSurveyView {

	public EthicsView(Context context, Survey survey) {
		this(context, null, 0);
		mSurvey = survey;
		onFinishInflate();
	}

	public EthicsView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_ethics, this);
	}

	@Override
	public void setResult(Object result) {}

	@Override
	public String getSurveyResponse() {
		return null;
	}

	@Override
	public boolean returnsResult() {
		return false;
	}

	@Override
	public boolean canAdvance() {
		return true;
	}

}
