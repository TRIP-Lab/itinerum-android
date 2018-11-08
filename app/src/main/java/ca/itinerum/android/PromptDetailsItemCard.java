package ca.itinerum.android;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.LinearLayout;

import org.apache.commons.lang3.NotImplementedException;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.sync.retrofit.Prompt;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.Logger;

public class PromptDetailsItemCard extends LinearLayout {

	@BindDimen(R.dimen.padding_large) int PADDING;

	@BindView(R.id.textview_prompt_title) AppCompatTextView mTextviewPromptTitle;
	@BindView(R.id.view_prompt_dialog_list) PromptDialogSelectableRecyclerView mViewPromptDialogList;

	public PromptDetailsItemCard(Context context) {
		this(context, null);
		onFinishInflate();
	}

	public PromptDetailsItemCard(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PromptDetailsItemCard(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_prompt_details_card, this);

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		setOrientation(VERTICAL);

		mViewPromptDialogList.setScrollable(false);
	}

	public PromptDialogSelectableRecyclerView getViewPromptDialogList() {
		return mViewPromptDialogList;
	}

	public void setPrompts(@NonNull Prompt prompt, PromptAnswer promptAnswer) {

		mTextviewPromptTitle.setText(prompt.getPrompt());

		if (promptAnswer != null) {
			// now we have the corresponding Prompt to PromptAnswer
			// single choice mode
			switch (prompt.getId()) {
				case 1:
					for (int i = 0; i < prompt.getChoices().size(); i++) {
						if (prompt.getChoices().get(i).equals(promptAnswer.getAnswer().get(0))) {
							mViewPromptDialogList.setPromptContent(prompt, i);
							break;
						}
					}
					break;

				case 2:
					SparseBooleanArray checked = new SparseBooleanArray(prompt.getChoices().size());
					for (int i = 0; i < prompt.getChoices().size(); i++) {
						for (int j = 0; j < promptAnswer.getAnswer().size(); j++) {
							if (prompt.getChoices().get(i).equals(promptAnswer.getAnswer().get(j))) {
								checked.put(i, true);
							}
						}
					}
					mViewPromptDialogList.setPromptContent(prompt, checked);
					break;

				default:
					if (BuildConfig.DEBUG)
						throw new NotImplementedException("No prompt id type " + prompt.getId());
					mViewPromptDialogList.setPromptContent(prompt);
					break;
			}


		} else {
			mViewPromptDialogList.setPromptContent(prompt);
		}

		measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

		int height = mViewPromptDialogList.getAdapterMeasuredHeight(getMeasuredWidth()); // have to predefine and pre-measure listview or it won't nest in scrollview.
		mViewPromptDialogList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));

		// recyclerview cell height isn't always right on the first pass when larger text takes up multiline. This performs a measure after layout in a second pass
		mViewPromptDialogList.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				final int height = mViewPromptDialogList.getAdapterMeasuredHeight(getMeasuredWidth());

				if (bottom - top != height) {
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							mViewPromptDialogList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
						}
					});
				}

				mViewPromptDialogList.removeOnLayoutChangeListener(this);
			}
		});

		//Doing this here because it uses an <include> tag in XML instead of stubbing out Java object
		mTextviewPromptTitle.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.list_item_background_light));
		mViewPromptDialogList.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.list_item_background_light));

	}
}
