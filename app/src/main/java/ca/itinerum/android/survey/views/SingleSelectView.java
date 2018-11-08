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

public abstract class SingleSelectView extends SelectView {

	public SingleSelectView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SingleSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		if (mSurvey.getFields().getChoices() != null && mSurvey.getFields().getChoices().size() > 0) {
			mListView.setData(mSurvey.getFields().getChoices(), false);
		}

	}

	@Override
	public boolean canAdvance() {
		return mListView.getCheckedItemPosition() != -1;
	}
}
