package ca.itinerum.android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.common.BottomSpaceItemDecoration;
import ca.itinerum.android.sync.PromptAnswerGroup;

public class PromptsRecyclerView extends FrameLayout {
	@BindView(R.id.recyclerview) RecyclerView mRecyclerview;
	@BindView(R.id.textview_no_data) AppCompatTextView mTextviewNoData;
	@BindView(R.id.list_mask) View mListMask;

	private OnPromptItemClickedListener mOnPromptItemClickListener;
	private int mLastPositionVisible = -1;

	public PromptsRecyclerView(@NonNull Context context) {
		this(context, null);
		onFinishInflate();
	}

	public PromptsRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public PromptsRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_prompts, this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);
		this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mRecyclerview.setHasFixedSize(true);

		mRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				updateRecyclerviewMask();
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				updateRecyclerviewMask();
			}
		});
	}

	private void updateRecyclerviewMask() {
		int lastPositionVisible = isLastVisible() ? VISIBLE : INVISIBLE;
		if (lastPositionVisible != mLastPositionVisible) {
			mListMask.animate().alpha(isLastVisible() ? 0f : 1f).start();
		}
		mLastPositionVisible = lastPositionVisible;
	}

	public void setPromptData(List<PromptAnswerGroup> promptData) {
		mTextviewNoData.setVisibility(promptData.size() > 0 ? GONE : VISIBLE);
		mRecyclerview.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
		mRecyclerview.setAdapter(new Adapter(promptData));
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

		mRecyclerview.setLayoutManager(linearLayoutManager);

		updateRecyclerviewMask();

	}

	public void setOnPromptItemClickListener(OnPromptItemClickedListener listener) {
		mOnPromptItemClickListener = listener;
	}

	class Adapter extends RecyclerView.Adapter<PromptViewHolder> {

		private final List<PromptAnswerGroup> mPromptData;

		public Adapter(List<PromptAnswerGroup> promptData) {
			Collections.sort(promptData, new Comparator<PromptAnswerGroup>() {
				@Override
				public int compare(PromptAnswerGroup promptAnswerGroup, PromptAnswerGroup t1) {
					return promptAnswerGroup.getSubmitDate().compareTo(t1.getSubmitDate());
				}
			});

			mPromptData = promptData;
		}

		@NonNull
		@Override
		public PromptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_prompt, parent, false);

			return new PromptViewHolder(v);
		}

		@Override
		public void onBindViewHolder(@NonNull PromptViewHolder holder, final int position) {
			DateTime dt = mPromptData.get(position).getSubmitDate();
			holder.setDateTime(dt.toString(DateTimeFormat.forPattern("M - dd - YYYY")), dt.toString(DateTimeFormat.shortTime()));
			holder.setPosition(position + 1);
			if (mOnPromptItemClickListener != null)
				holder.getRootView().setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mOnPromptItemClickListener != null)
							mOnPromptItemClickListener.onPromptItemClick(v, position);
					}
				});
		}

		@Override
		public int getItemCount() {
			return mPromptData.size();
		}
	}

	public void setBottomPadding(int padding) {
		LayoutParams lp = (LayoutParams) mTextviewNoData.getLayoutParams();
		lp.setMargins(0, -padding/2, 0, 0);
		mTextviewNoData.setLayoutParams(lp);

		mRecyclerview.addItemDecoration(new BottomSpaceItemDecoration(padding));
	}

	class PromptViewHolder extends RecyclerView.ViewHolder {

		private final View mRootView;
		@BindView(R.id.textview_position) AppCompatTextView mTextviewPosition;
		@BindView(R.id.textview_date) AppCompatTextView mTextviewDate;
		@BindView(R.id.textview_time) AppCompatTextView mTextviewTime;
		@BindView(R.id.button_edit) ImageView mButtonEdit;

		public PromptViewHolder(View itemView) {
			super(itemView);
			mRootView = itemView;
			ButterKnife.bind(this, itemView);
		}

		public void setDateTime(String date, String time) {
			mTextviewDate.setText(date);
			mTextviewTime.setText(time);
		}

		public void setPosition(int position) {
			mTextviewPosition.setText("" + position);
		}

		public void setBackground(int colour) {
			mRootView.setBackgroundColor(colour);
		}

		public View getRootView() {
			return mRootView;
		}
	}

	public boolean isLastVisible() {
		if (mRecyclerview.getAdapter().getItemCount() < 1) return true;

		return (((LinearLayoutManager) mRecyclerview.getLayoutManager()).findLastCompletelyVisibleItemPosition() + 1 >= mRecyclerview.getAdapter().getItemCount());
	}

	public interface OnPromptItemClickedListener {
		void onPromptItemClick(View view, int position);
	}
}
