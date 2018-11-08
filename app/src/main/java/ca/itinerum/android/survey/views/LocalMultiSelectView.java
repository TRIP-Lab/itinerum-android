package ca.itinerum.android.survey.views;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;

import java.util.Arrays;
import java.util.HashSet;

import ca.itinerum.android.survey.SurveyHelper;
import ca.itinerum.android.sync.retrofit.Survey;

/**
 * Created by stewjacks on 2017-01-19.
 */

public class LocalMultiSelectView extends MultiSelectView {


	public LocalMultiSelectView(Context context, Survey survey) {
		super(context, null, 0);
		mSurvey = survey;
		onFinishInflate();
	}

	public void setArrayResource(String[] options) {
		mListView.setData(Arrays.asList(options), true);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setTitleText(SurveyHelper.getUserVisibleSurveyTitle(getContext(), mSurvey));
	}

	@Override
	public HashSet<Integer> getSurveyResponse() {
		HashSet<Integer> results = new HashSet<>();
		SparseBooleanArray isChecked = mListView.getCheckedItemPositions();

		for (int i = 0; i < mListView.getSize(); i++) {
			if (isChecked.get(i)) results.add(i);
		}

		return results;
	}

	@Override
	public void setResult(Object result) {
		if (result instanceof HashSet) {
			HashSet<String> set = (HashSet<String>) result;
			SparseBooleanArray array = new SparseBooleanArray();
			for (int i = 0; i < mListView.getSize(); i++) {
				array.put(i, set.contains(i));
			}
			mListView.setCheckedItemPositions(array);
		}
	}
}
