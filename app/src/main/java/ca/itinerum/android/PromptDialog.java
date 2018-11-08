package ca.itinerum.android;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;

import java.util.ArrayList;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.sync.retrofit.Prompt;
import ca.itinerum.android.sync.retrofit.PromptAnswer;

public class PromptDialog extends DialogFragment {

	@BindView(R.id.title) AppCompatTextView mTitle;
	@BindView(R.id.recyclerview) PromptDialogSelectableRecyclerView mRecyclerview;
	@BindView(R.id.continue_button) AppCompatButton mFragmentContinueButton;
	@BindView(R.id.back_button) AppCompatImageButton mBackButton;
	@BindView(R.id.dismiss_button) AppCompatImageButton mDismissButton;
	@BindView(R.id.list_mask) FrameLayout mListMask;

	@BindDimen(R.dimen.list_bottom_padding) int PADDING;
	private ViewPropertyAnimator mAnimation;

	private int mLastPositionVisible = -1;

	final int VISIBLE = 1;
	final int INVISIBLE = 0;

	public static PromptDialog newInstance(Prompt prompt, int currentPrompt, ArrayList<String> selected) {
		PromptDialog fragment = new PromptDialog();
		Bundle args = new Bundle();
		args.putInt("currentPrompt", currentPrompt);
		args.putStringArrayList("selected", selected);
		args.putParcelable("prompt", prompt);
		args.putString("type", "PromptDialog");
		fragment.setArguments(args);
		return fragment;
	}

	public PromptAnswer getPromptAnswer() {
		return mRecyclerview.getPromptAnswer();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_prompt_dialog, container, false);
		ButterKnife.bind(this, v);

		mRecyclerview.setIncomplete(false);

		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		int currentPrompt = getArguments().getInt("currentPrompt");

		if (currentPrompt == 0) mBackButton.setVisibility(View.INVISIBLE);

		ArrayList<String> promptPositions = getArguments().getStringArrayList("selected");
		Prompt prompt = getArguments().getParcelable("prompt");

		mTitle.setText(prompt.getPrompt());

		SparseBooleanArray selected = new SparseBooleanArray();
		for (int i = 0; i < prompt.getChoices().size(); i++) {
			selected.put(i, promptPositions.contains(prompt.getChoices().get(i)));
		}

		mRecyclerview.setPromptContent(prompt, selected);
		mRecyclerview.setBottomSpace(PADDING);

		mListMask.setVisibility(mRecyclerview.isLastVisible() ? View.GONE : View.VISIBLE);

		mFragmentContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((PromptDialogListener) getActivity()).onPositiveButtonClicked(PromptDialog.this);
			}
		});

		mBackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((PromptDialogListener) getActivity()).onNeutralButtonClicked(PromptDialog.this);
			}
		});

		mDismissButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((PromptDialogListener) getActivity()).onNegativeButtonClicked(PromptDialog.this);
			}
		});

		// Add a layout change listener to detect when the view has properly rendered
		mRecyclerview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerview.getLayoutManager();
				if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() != RecyclerView.NO_POSITION) {
					updateRecyclerviewMask();
					// check if the first and last views are completely visible to remove scrolling effects
					if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0 && linearLayoutManager.findLastCompletelyVisibleItemPosition() == mRecyclerview.getAdapter().getItemCount() - 1) {
						mRecyclerview.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
					}
					// remove the listener for efficiency
					mRecyclerview.removeOnLayoutChangeListener(this);
				}
			}
		});

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

		getDialog().setCanceledOnTouchOutside(false);
		getDialog().setCancelable(false);

		return v;
	}

//	@Override
//	public void onStart() {
//		super.onStart();
//		Dialog dialog = getDialog();
//		if (dialog != null) {
//			DisplayMetrics displaymetrics = new DisplayMetrics();
//			getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//			int width = (int) (displaymetrics.widthPixels * 0.9);
//
//			dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
//			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//		}
//
//	}

	private void updateRecyclerviewMask() {
		int lastPositionVisible = mRecyclerview.isLastVisible() ? VISIBLE : INVISIBLE;
		if (lastPositionVisible != mLastPositionVisible) {
			mListMask.animate().alpha(mRecyclerview.isLastVisible() ? 0f : 1f).start();
		}
		mLastPositionVisible = lastPositionVisible;
	}
}