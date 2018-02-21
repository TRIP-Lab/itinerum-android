package ca.itinerum.android;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.sync.PromptAnswerGroup;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class PromptsRecyclerView extends FrameLayout {
	@BindView(R.id.recyclerview) RecyclerView mRecyclerview;
	@BindView(R.id.textview_no_data) TextView mTextviewNoData;

	private OnPromptItemClickedListener mOnPromptItemClickListener;

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
	}

	public void setPromptData(List<PromptAnswerGroup> promptData) {
		mTextviewNoData.setVisibility(promptData.size() > 0 ? GONE : VISIBLE);

		Collections.sort(promptData, new Comparator<PromptAnswerGroup>() {
			@Override
			public int compare(PromptAnswerGroup promptAnswerGroup, PromptAnswerGroup t1) {
				return promptAnswerGroup.getSubmitDate().compareTo(t1.getSubmitDate());
			}
		});

		List<List<PromptAnswerGroup>> dateGroupedAnswers = new ArrayList<>();
		List<PromptAnswerGroup> dateGroup = new ArrayList<>();


		//TODO: golf this -> can be done with sublist and equalsDay cleanly
		int i = 0;
		for (PromptAnswerGroup group: promptData) {
			group.setPosition(i);
			i++;

			if (dateGroup.size() == 0) {
				dateGroup.add(group);
			} else if (dateGroup.get(0).equalsDay(group)) {
				dateGroup.add(group);
			} else {
				dateGroupedAnswers.add(dateGroup);
				dateGroup = new ArrayList<>();
				dateGroup.add(group);
			}
		}

		if (dateGroup.size() > 0) dateGroupedAnswers.add(dateGroup);

		final SectionedRecyclerViewAdapter sectionAdapter = new SectionedRecyclerViewAdapter();

		for (List<PromptAnswerGroup> group : dateGroupedAnswers) {
			sectionAdapter.addSection(new Section(group));
		}

		mRecyclerview.setAdapter(sectionAdapter);
		GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);

		gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				switch(sectionAdapter.getSectionItemViewType(position)) {
					case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
						return 3;
					case SectionedRecyclerViewAdapter.VIEW_TYPE_FOOTER:
						return 3;
					default:
						return 1;
				}
			}
		});
		mRecyclerview.setLayoutManager(gridLayoutManager);

	}

	public static int sidebarColor(String seed) {
		if (seed == null) return 0;
		Random random = new Random(seed.hashCode());
		return Color.HSVToColor(new float[] { random.nextInt(128) + 136, 0.75f, 0.75f });
	}

	public void setOnPromptItemClickListener(OnPromptItemClickedListener listener) {
		mOnPromptItemClickListener = listener;
	}

	class Section extends StatelessSection {

		private final List<PromptAnswerGroup> mItemList;

		public Section(List<PromptAnswerGroup> list) {
			super(new SectionParameters.Builder(R.layout.list_item_prompt)
					.headerResourceId(R.layout.list_item_header)
					.footerResourceId(R.layout.view_footer_shadow)
					.build());

			mItemList = list;

		}

		@Override
		public int getContentItemsTotal() {
			return mItemList.size(); // number of items of this section
		}

		@Override
		public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
			return new PromptSectionTitleViewHolder(view);
		}

		@Override
		public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
			PromptSectionTitleViewHolder itemHolder = (PromptSectionTitleViewHolder) holder;

			itemHolder.mTextViewTitle.setText(mItemList.get(0).getSubmitDate().toString(DateTimeFormat.fullDate()));
		}

		@Override
		public RecyclerView.ViewHolder getItemViewHolder(View view) {
			return new PromptViewHolder(view);
		}

		@Override
		public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
			PromptViewHolder itemHolder = (PromptViewHolder) holder;

			itemHolder.getRootView().setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mOnPromptItemClickListener != null)
						mOnPromptItemClickListener.onPromptItemClick(view, mItemList.get(position).getPosition());
				}
			});

			// bind your view here
			itemHolder.setText(mItemList.get(position).getSubmitDate().toString(DateTimeFormat.shortTime()));
			itemHolder.setBackground(sidebarColor(mItemList.get(position).getSubmitDate().toString(DateTimeFormat.fullDateTime())));
		}
	}

	class PromptSectionTitleViewHolder extends RecyclerView.ViewHolder {

		@BindView(R.id.prompt_title) TextView mTextViewTitle;

		public PromptSectionTitleViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}
	}

	class PromptViewHolder extends RecyclerView.ViewHolder {

		private final View mRootView;
		@BindView(R.id.imageview_indicator) ImageView mImageViewIndicator;
		@BindView(R.id.textview_time) TextView mTextViewTime;

		public PromptViewHolder(View itemView) {
			super(itemView);
			mRootView = itemView;
			ButterKnife.bind(this, itemView);
		}

		public void setText(String text) {
			mTextViewTime.setText(text);
		}

		public void setBackground(int colour) {
			mRootView.setBackgroundColor(colour);
		}

		public View getRootView() {
			return mRootView;
		}
	}

	public interface OnPromptItemClickedListener {
		void onPromptItemClick(View view, int position);
	}
}
