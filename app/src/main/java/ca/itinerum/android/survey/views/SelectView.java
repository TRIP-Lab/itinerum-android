package ca.itinerum.android.survey.views;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.common.SelectionRecyclerView;
import ca.itinerum.android.sync.retrofit.Survey;

/**
 * Created by stewjacks on 2017-01-19.
 */

public abstract class SelectView extends BaseSurveyView {

	@BindView(R.id.title) AppCompatTextView mTitle;
	@BindView(R.id.question) AppCompatTextView mQuestion;
	@BindView(R.id.list_view) SelectionRecyclerView mListView;

	@BindDimen(R.dimen.survey_bottom_space) int BOTTOM_SPACE;

	public SelectView(Context context, Survey survey) {
		this(context, null, 0);
		mSurvey = survey;
		onFinishInflate();
	}

	public SelectView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SelectView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_list, this);
		setOrientation(VERTICAL);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		setTitleText(mSurvey.getColName());

		mQuestion.setText(mSurvey.getPrompt());

		mListView.setOnItemClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onCanAdvance(canAdvance());
			}
		});
		mListView.setBottomSpace(BOTTOM_SPACE);
	}

	public void setTitleText(String title) {
		mTitle.setText(title);
		if (title == null) return;

		String trimmed = title.trim();
		int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
		mTitle.setMaxLines(Math.min(words, 2));

	}

	@Override
	public boolean canAdvance() {
		return true;
	}
}
