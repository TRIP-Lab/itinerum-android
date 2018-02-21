package ca.itinerum.android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.sync.retrofit.Prompt;
import ca.itinerum.android.sync.retrofit.PromptAnswer;

public class PromptDetailsItemCard extends LinearLayout {

	@BindDimen(R.dimen.padding_large) int PADDING;

	@BindView(R.id.textview_prompt_title) TextView mTextviewPromptTitle;
	@BindView(R.id.view_prompt_dialog_list) PromptDialogListView mViewPromptDialogList;

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
	}

	public PromptDialogListView getViewPromptDialogList() {
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
					boolean[] checked = new boolean[prompt.getChoices().size()];
					Arrays.fill(checked, false);
					for (int i = 0; i < prompt.getChoices().size(); i++) {
						for (int j = 0; j < promptAnswer.getAnswer().size(); j++) {
							if (prompt.getChoices().get(i).equals(promptAnswer.getAnswer().get(j))) {
								checked[i] = true;
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

		int height = mViewPromptDialogList.getAdapterMeasuredHeight(); // have to predefine and pre-measure listview or it won't nest in scrollview.
		mViewPromptDialogList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));

		//Doing this here because it uses an <include> tag in XML instead of stubbing out Java object
		mTextviewPromptTitle.setBackgroundColor(getResources().getColor(R.color.cardview_light_background));

	}
}
