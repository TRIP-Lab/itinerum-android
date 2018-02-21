package ca.itinerum.android.survey.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.sync.retrofit.Survey;

/**
 * Created by stewjacks on 2017-01-19.
 */

public class OccupationView extends BaseSurveyView {

	@BindView(R.id.title) TextView mTitle;
	@BindView(R.id.question) TextView mQuestion;
	@BindView(R.id.list_view) ListView mListView;

	@BindArray(R.array.question_occupation) String[] OCCUPATION;

	private ArrayAdapter<String> mAdapter;

	public OccupationView(Context context, Survey survey) {
		this(context, null, 0);
		mSurvey = survey;
		onFinishInflate();
	}

	public OccupationView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public OccupationView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_list, this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		mTitle.setVisibility(GONE);
		mQuestion.setText(R.string.occupation_title);

		mAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_single_choice, OCCUPATION);
		mListView.setAdapter(mAdapter);

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
			mListView.setItemChecked((int) result, true);
		}
	}

	@Override
	public boolean canAdvance() {
		return (mListView.getCheckedItemPosition() != -1);
	}
}
