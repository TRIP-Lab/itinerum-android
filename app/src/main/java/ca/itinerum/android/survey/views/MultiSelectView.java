package ca.itinerum.android.survey.views;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
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

public abstract class MultiSelectView extends SelectView {

	private int mMinResponses = 0;

	public MultiSelectView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}


	public MultiSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		if (mSurvey.getFields().getChoices() != null && mSurvey.getFields().getChoices().size() > 0) {
			mListView.setData(mSurvey.getFields().getChoices(), true);
		}

	}

	@Override
	public boolean canAdvance() {
		// presumably they can select none from a multisele;ct list, if applicable
		if (mMinResponses == 0) return true;

		int size = 0;
		SparseBooleanArray results = mListView.getCheckedItemPositions();
		for (int i = 0; i < results.size(); i++) {
			if (results.get(i)) size++;
		}

		return size >= mMinResponses;
	}

	public void setMinResponses(int minResponses) {
		mMinResponses = minResponses;
	}

	public int getMinResponses() {
		return mMinResponses;
	}
}
