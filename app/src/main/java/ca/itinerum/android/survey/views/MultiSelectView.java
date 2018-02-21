package ca.itinerum.android.survey.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.sync.retrofit.Survey;

/**
 * Created by stewjacks on 2017-01-19.
 */

public abstract class MultiSelectView extends BaseSurveyView {

	@BindView(R.id.question) TextView mQuestion;
	@BindView(R.id.title) TextView mTitle;
	@BindView(R.id.list_view) ListView mListView;
	protected ArrayAdapter<String> mAdapter;

	public MultiSelectView(Context context, Survey survey) {
		this(context, null, 0);
		mSurvey = survey;
		onFinishInflate();
	}

	public MultiSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_list, this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		mTitle.setVisibility(GONE);
		mQuestion.setText(mSurvey.getPrompt());

		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, mSurvey.getFields().getChoices());
		mListView.setAdapter(mAdapter);


		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mListener.onCanAdvance(mListView.getCheckedItemPositions().size() > 0);
			}
		});
	}

	@Override
	public boolean canAdvance() {
		// presumably they can select none from a multiselect list
		return true;
	}

}
