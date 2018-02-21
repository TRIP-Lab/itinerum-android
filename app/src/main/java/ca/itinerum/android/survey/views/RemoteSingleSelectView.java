package ca.itinerum.android.survey.views;

import android.content.Context;

import ca.itinerum.android.sync.retrofit.Survey;

/**
 * Created by stewjacks on 2017-01-19.
 */

public class RemoteSingleSelectView extends SingleSelectView {

	public RemoteSingleSelectView(Context context, Survey survey) {
		super(context, null, 0);
		mSurvey = survey;
		onFinishInflate();
	}

	@Override
	public String getSurveyResponse() {
		return mSurvey.getFields().getChoices().get(mListView.getCheckedItemPosition());
	}

	@Override
	public void setResult(Object result) {
		if (result != null && result instanceof String) {
			if (mSurvey.getFields().getChoices().contains(result)) {
				mListView.setItemChecked(mSurvey.getFields().getChoices().indexOf(result), true);
			}
		}
	}
}
