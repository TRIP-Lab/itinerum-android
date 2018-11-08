package ca.itinerum.android.survey.views;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.common.SelectionRecyclerView;
import ca.itinerum.android.sync.retrofit.Survey;

/**
 * Created by stewjacks on 2017-01-19.
 */

public class OccupationView extends BaseSurveyView {

	@BindView(R.id.title) AppCompatTextView mTitle;
	@BindView(R.id.question) AppCompatTextView mQuestion;
	@BindView(R.id.list_view) SelectionRecyclerView mListView;

	@BindArray(R.array.question_occupation) String[] OCCUPATION;

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

		mTitle.setText(R.string.occupation_title);
		mQuestion.setText(R.string.occupation_prompt);

		mListView.setData(Arrays.asList(OCCUPATION), false);

		mListView.setOnItemClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
			mListView.setCheckedItemPosition((int) result, true);
		} else if (BuildConfig.DEBUG) {
			mListView.setCheckedItemPosition(0, true);
		}
	}

	@Override
	public boolean canAdvance() {
		return (mListView.getCheckedItemPosition() != -1);
	}


}
