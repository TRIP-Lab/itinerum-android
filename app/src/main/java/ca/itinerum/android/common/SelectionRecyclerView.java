package ca.itinerum.android.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;

public class SelectionRecyclerView extends FrameLayout {
	public final int CHOICE_MODE_SINGLE = 1;
	public final int CHOICE_MODE_MULTIPLE = 2;
	private boolean mIsDark;

	private List<ListItem> mDataset;
	private int mLastSelectedIndex = -1;
	private OnClickListener mItemClickListener;
	private int mChoiceMode = CHOICE_MODE_SINGLE;

	private boolean mScrollable = true;

	@BindDrawable(R.drawable.background_rounded_list_item_dark) Drawable BACKGROUND_DARK;
	@BindDrawable(R.drawable.background_rounded_list_item_light) Drawable BACKGROUND;

	@BindView(R.id.rv) RecyclerView mRecyclerview;
	@BindView(R.id.incomplete_indicator) View mIncompleteIndicator;

	public SelectionRecyclerView(Context context) {
		this(context, null);
		onFinishInflate();
	}

	public SelectionRecyclerView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, R.attr.customSelectionRecyclerView);
	}

	public SelectionRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		inflate(context, R.layout.selection_recyclerview, this);

		ButterKnife.bind(this);

		setHasFixedSize(true);
		setIncomplete(false);

		final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SelectionRecyclerView, defStyle, 0);
		mIsDark = array.getBoolean(R.styleable.SelectionRecyclerView_dark_mode, true);
		array.recycle();

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
//		ButterKnife.bind(this);

	}

	public boolean isLastVisible() {
		return (((LinearLayoutManager) getLayoutManager()).findLastCompletelyVisibleItemPosition() + 1 >= getAdapter().getItemCount());
	}

	public void setData(List<String> data) {
		mDataset = new ArrayList<>();

		for (String string : data) {
			mDataset.add(new ListItem(string));
		}

		setAdapter(new SelectionAdapter(mDataset, mClickListener));

		ScrollToggleLinearLayoutManager ll = new ScrollToggleLinearLayoutManager(getContext());
		ll.setScrollable(mScrollable);
		setLayoutManager(ll);

	}

	public void setHasFixedSize(boolean hasFixedSize) {
		mRecyclerview.setHasFixedSize(hasFixedSize);
	}

	public RecyclerView.LayoutManager getLayoutManager() {
		return mRecyclerview.getLayoutManager();
	}

	public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
		mRecyclerview.setLayoutManager(layoutManager);
	}

	public RecyclerView.Adapter getAdapter() {
		return mRecyclerview.getAdapter();
	}

	public void setAdapter(RecyclerView.Adapter adapter) {
		mRecyclerview.setAdapter(adapter);
	}

	public void swapAdapter(RecyclerView.Adapter adapter, boolean removeAndRecycleExistingViews) {
		mRecyclerview.swapAdapter(adapter, removeAndRecycleExistingViews);
	}

	public void addItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
		mRecyclerview.addItemDecoration(itemDecoration);
	}

	public int getChildLayoutPosition(View child) {
		return mRecyclerview.getChildLayoutPosition(child);
	}

	public void setIncomplete(boolean incomplete) {
		mIncompleteIndicator.setVisibility(incomplete ? VISIBLE : GONE);
	}

	public void addOnScrollListener(RecyclerView.OnScrollListener listener) {
		mRecyclerview.addOnScrollListener(listener);
	}

	/**
	 * todo: remove this for above
	 **/
	@Deprecated
	public void setData(List<String> data, boolean multiSelect) {
		setChoiceMode(multiSelect ? CHOICE_MODE_MULTIPLE : CHOICE_MODE_SINGLE);

		mDataset = new ArrayList<>();

		for (String string : data) {
			mDataset.add(new ListItem(string));
		}

		setAdapter(new SelectionAdapter(mDataset, mClickListener));

		ScrollToggleLinearLayoutManager ll = new ScrollToggleLinearLayoutManager(getContext());
		ll.setScrollable(mScrollable);
		setLayoutManager(ll);

	}

	public void setScrollable(boolean scrollable) {
		mScrollable = scrollable;
		ScrollToggleLinearLayoutManager scrollToggleLinearLayoutManager;
		if (getLayoutManager() instanceof ScrollToggleLinearLayoutManager) {
			scrollToggleLinearLayoutManager = (ScrollToggleLinearLayoutManager) getLayoutManager();
		} else {
			scrollToggleLinearLayoutManager = new ScrollToggleLinearLayoutManager(getContext());
		}

		scrollToggleLinearLayoutManager.setScrollable(scrollable);
		setLayoutManager(scrollToggleLinearLayoutManager);
	}

	public void setBottomSpace(int space) {
		addItemDecoration(new BottomSpaceItemDecoration(space));
	}

	public void setOnItemClickListener(OnClickListener onItemClickListener) {
		mItemClickListener = onItemClickListener;
	}

	public void setChoiceMode(int mode) {
		mChoiceMode = mode;
	}

	public SparseBooleanArray getCheckedItemPositions() {
		SparseBooleanArray array = new SparseBooleanArray();
		int i = 0;
		for (ListItem listItem : mDataset) {
			array.append(i, listItem.isSelected());
			i++;
		}

		return array;
	}

	public int getCheckedItemPosition() {
		return mLastSelectedIndex;
	}

	public void setCheckedItemPosition(int position, boolean checked) {

		if (mChoiceMode == CHOICE_MODE_SINGLE) {
			for (ListItem item : mDataset) {
				item.setSelected(false);
			}
		}

		setItemChecked(position, checked);
	}

	public void setCheckedItemPositions(SparseBooleanArray array) {
		if (mDataset == null) return;
		for (int i = 0; i < mDataset.size(); i++) {
			mDataset.get(i).setSelected(array.get(i));
			if (array.get(i)) mLastSelectedIndex = i;
		}
		swapAdapter(new SelectionAdapter(mDataset, mClickListener), false);
	}

	private void setItemChecked(int position, boolean checked) {
		mLastSelectedIndex = position;
		ListItem item = mDataset.get(mLastSelectedIndex);
		item.setSelected(checked);
		mDataset.set(position, item);
		swapAdapter(new SelectionAdapter(mDataset, mClickListener), false);
	}

	public int getSize() {
		return mDataset.size();
	}

	public List<ListItem> getDataset() {
		return mDataset;
	}

	private final OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			int pos = getChildLayoutPosition(v);

			if (pos == RecyclerView.NO_POSITION) return;
			ListItem item = mDataset.get(pos);
			setCheckedItemPosition(pos, mChoiceMode == CHOICE_MODE_MULTIPLE ? !item.isSelected() : true);

			if (mItemClickListener != null) mItemClickListener.onClick(v);

		}
	};

	class SelectionAdapter extends RecyclerView.Adapter<ViewHolder> {

		private final List<ListItem> mDataset;
		private final OnClickListener mOnClickListener;

		public SelectionAdapter(List<ListItem> dataset, OnClickListener onClickListener) {
			mDataset = dataset;
			mOnClickListener = onClickListener;
			super.setHasStableIds(true);
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

			int resource;
			switch (mChoiceMode) {
				case CHOICE_MODE_SINGLE:
					resource = mIsDark ? R.layout.list_item_single_choice_dark : R.layout.list_item_single_choice_light;
					break;
				case CHOICE_MODE_MULTIPLE:
					resource = mIsDark ? R.layout.list_item_multiple_choice_dark : R.layout.list_item_multiple_choice_light;
					break;
				default:
					throw new NotImplementedException(mChoiceMode + " is not a valid option for a choice mode");
			}

			View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					// do animations

					if (mOnClickListener != null) mOnClickListener.onClick(v);
				}
			});

			ViewHolder viewHolder = new ViewHolder(v);
			return viewHolder;
		}

		@Override
		public long getItemId(int position) {
			return mDataset.get(position).getValue().hashCode();
		}

		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			holder.mTextView.setText(mDataset.get(position).getValue());
			holder.mTextView.setChecked(mDataset.get(position).isSelected());
			holder.mCheckbox.setChecked(mDataset.get(position).isSelected());
			holder.mContainer.setChecked(mDataset.get(position).isSelected());
		}

		@Override
		public int getItemCount() {
			return mDataset.size();
		}
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		@BindView(R.id.container) CheckableLinearLayout mContainer;
		@BindView(R.id.textview) CheckableTextView mTextView;
		@BindView(R.id.checkbox) AppCompatCheckBox mCheckbox;

		public ViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
			mContainer.setBackgroundResource(mIsDark ? R.drawable.background_rounded_list_item_dark : R.drawable.background_rounded_list_item_light);
			mTextView.setTextColor(mIsDark ? getResources().getColorStateList(R.color.button_state_list_dark) : getResources().getColorStateList(R.color.button_state_list_light));
		}
	}

	public class ListItem {
		private String mValue;
		private boolean mIsSelected = false;

		public ListItem(String value) {
			this(value, false);
		}

		public ListItem(String value, boolean isSelected) {
			mValue = value;
			mIsSelected = isSelected;
		}

		public String getValue() {
			return mValue;
		}

		public void setValue(String value) {
			mValue = value;
		}

		public boolean isSelected() {
			return mIsSelected;
		}

		public void setSelected(boolean selected) {
			mIsSelected = selected;
		}
	}

	private class ScrollToggleLinearLayoutManager extends LinearLayoutManager {

		private boolean mScrollable;

		public ScrollToggleLinearLayoutManager(Context context) {
			super(context);
		}

		public void setScrollable(boolean scrollable) {
			mScrollable = scrollable;
		}

		@Override
		public boolean canScrollVertically() {
			return mScrollable && super.canScrollVertically();
		}
	}
}