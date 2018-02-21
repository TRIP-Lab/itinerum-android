package ca.itinerum.android.survey.views;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

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
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_single_choice, options);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mListener.onCanAdvance(true);
			}
		});
	}

	@Override
	public Integer getSurveyResponse() {
		return mListView.getCheckedItemPosition();
	}

	@Override
	public void setResult(Object result) {
		if (result != null && result instanceof Integer) {
			mListView.setItemChecked((int)result, true);
		}
	}
}
