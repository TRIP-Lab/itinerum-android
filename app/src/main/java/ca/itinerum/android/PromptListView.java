package ca.itinerum.android;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.sync.PromptAnswerGroup;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.db.ItinerumDatabase;

public class PromptListView extends LinearLayout {

	@BindView(R.id.title) AppCompatTextView mTitle;
	@BindView(R.id.add_trip_container) RelativeLayout mAddTripContainer;
	@BindView(R.id.prompts_recycler_view) PromptsRecyclerView mPromptsRecyclerView;
	@BindDimen(R.dimen.padding_large) int PADDING;
	@BindDimen(R.dimen.one_dp) int ONE_DP;
	@BindColor(R.color.divider) int DIVIDER_COLOUR;
	@BindColor(R.color.background_light) int BACKGROUND_COLOUR;
	private int mBottomPadding;

	public PromptListView(Context context) {
		this(context, null);
		onFinishInflate();
	}

	public PromptListView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PromptListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater.from(context).inflate(R.layout.view_prompt_list, this, true);
		setOrientation(VERTICAL);
		setClickable(true);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		refreshList();

		mPromptsRecyclerView.setBottomPadding(mBottomPadding);
	}

	public void setOnPromptItemClickListener(PromptsRecyclerView.OnPromptItemClickedListener listener) {
		mPromptsRecyclerView.setOnPromptItemClickListener(listener);
	}

	public void setAddTripClickListener(OnClickListener listener) {
		mAddTripContainer.setOnClickListener(listener);
	}

	public void refreshList() {
		List<PromptAnswer> promptAnswers = ItinerumDatabase.getInstance(getContext()).promptDao().getAllRegisteredPromptAnswers();
		List<PromptAnswerGroup> promptAnswerGroups = PromptAnswerGroup.sortPrompts(promptAnswers, SharedPreferenceManager.getInstance(getContext()).getNumberOfPrompts());

		mPromptsRecyclerView.setPromptData(promptAnswerGroups);
	}

	public void setBottomPadding(int padding) {
		mBottomPadding = padding;
		if (mPromptsRecyclerView != null) mPromptsRecyclerView.setBottomPadding(padding);
	}
}
