package ca.itinerum.android.survey.views;

import android.content.Context;
import android.view.View;

import java.util.Arrays;

import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.survey.SurveyHelper;
import ca.itinerum.android.sync.retrofit.Survey;

/**
 * Created by stewjacks on 2017-01-19.
 */

public class LocalSingleSelectView extends SingleSelectView {

	public LocalSingleSelectView(Context context, Survey survey) {
		super(context, null, 0);
		mSurvey = survey;
		onFinishInflate();
	}

	public void setArrayResource(String[] options) {
		mListView.setData(Arrays.asList(options), false);
		mListView.setOnItemClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onCanAdvance(true);
			}
		});


	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setTitleText(SurveyHelper.getUserVisibleSurveyTitle(getContext(), mSurvey));
	}

	@Override
	public Integer getSurveyResponse() {
		return mListView.getCheckedItemPosition();
	}

	@Override
	public void setResult(Object result) {
		if (result != null && result instanceof Integer) {
			mListView.setCheckedItemPosition((int)result, true);
		} else if (BuildConfig.DEBUG) {
			mListView.setCheckedItemPosition(0, true);
		}
	}

	public void hideQuestion() {
		mQuestion.setVisibility(GONE);
	}
}
