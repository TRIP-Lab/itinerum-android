package ca.itinerum.android.survey.views;

import android.content.Context;
import android.util.SparseBooleanArray;

import java.util.HashSet;

import ca.itinerum.android.sync.retrofit.Survey;

/**
 * Created by stewjacks on 2017-01-19.
 */

public class RemoteMultiSelectView extends MultiSelectView {

	public RemoteMultiSelectView(Context context, Survey survey) {
		super(context, null, 0);
		mSurvey = survey;
		onFinishInflate();
	}


	@Override
	public HashSet<String> getSurveyResponse() {
		HashSet<String> results = new HashSet<>();
		SparseBooleanArray isChecked = mListView.getCheckedItemPositions();

		for (int i = 0; i < mAdapter.getCount(); i++) {
			if (isChecked.get(i)) results.add(mSurvey.getFields().getChoices().get(i));
		}

		return results;
	}

	@Override
	public void setResult(Object result) {
		if (result instanceof HashSet) {
			HashSet<String> set = (HashSet<String>) result;
			for (int i = 0; i < mAdapter.getCount(); i++) {
				mListView.setItemChecked(i, (set.contains(mAdapter.getItem(i))));
			}
		}
	}
}
