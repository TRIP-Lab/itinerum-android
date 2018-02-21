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

public abstract class SingleSelectView extends BaseSurveyView {

	@BindView(R.id.title) TextView mTitle;
	@BindView(R.id.question) TextView mQuestion;
	@BindView(R.id.list_view) ListView mListView;
	private ArrayAdapter<String> mAdapter;

	public SingleSelectView(Context context, Survey survey) {
		this(context, null, 0);
		mSurvey = survey;
		onFinishInflate();
	}

	public SingleSelectView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SingleSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_list, this);
		setOrientation(VERTICAL);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		mTitle.setVisibility(GONE);
		mQuestion.setText(mSurvey.getPrompt());
		if (mSurvey.getFields().getChoices() != null && mSurvey.getFields().getChoices().size() > 0) {
			mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_single_choice, mSurvey.getFields().getChoices());
			mListView.setAdapter(mAdapter);
		}

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mListener.onCanAdvance(true);
			}
		});
	}

	@Override
	public boolean canAdvance() {
		return mListView.getCheckedItemPosition() != -1;
	}
}
