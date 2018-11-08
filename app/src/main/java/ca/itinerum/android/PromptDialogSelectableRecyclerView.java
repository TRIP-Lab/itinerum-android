package ca.itinerum.android;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.widget.Toast;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;

import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.common.SelectionRecyclerView;
import ca.itinerum.android.recording.Session;
import ca.itinerum.android.sync.retrofit.Prompt;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.DateUtils;

/**
 * Created by stewjacks on 2018-01-12.
 */

public class PromptDialogSelectableRecyclerView extends SelectionRecyclerView {

	private Prompt mPrompt;

	public PromptDialogSelectableRecyclerView(Context context) {
		super(context);
	}

	public PromptDialogSelectableRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PromptDialogSelectableRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	public void setPromptContent(Prompt prompt, int selected) {
		setPromptContent(prompt);
		setCheckedItemPosition(selected, true);
	}

	public void setPromptContent(Prompt prompt, SparseBooleanArray selected) {
		setPromptContent(prompt);
		setCheckedItemPositions(selected);
	}

	// configure onclick listener for content change w/ callback to
	public void setPromptContent(final Prompt prompt) {
		mPrompt = prompt;

		switch(prompt.getId()) {
			case 1:
				setChoiceMode(CHOICE_MODE_SINGLE);
				break;
			case 2:
				setChoiceMode(CHOICE_MODE_MULTIPLE);
				break;
			default:
				if (BuildConfig.DEBUG) throw new NotImplementedException("No prompt id type " + prompt.getId());
				return;

		}

		setData(prompt.getChoices());

	}

	public PromptAnswer getPromptAnswer() {
		switch (mPrompt.getId()) {
			case 1: {
				if (getCheckedItemPosition() < 0) {
					Toast.makeText(getContext(), R.string.toast_provide_answer, Toast.LENGTH_SHORT).show();
					return null;
				}
				return new PromptAnswer()
						.withAnswer(mPrompt.getChoices().get(getCheckedItemPosition()))
						.withPrompt(mPrompt.getPrompt())
						.withLatitude(Session.getInstance().getLastRecordedLatitude())
						.withLongitude(Session.getInstance().getLastRecordedLongitude())
						.withRecordedAt(DateUtils.getCurrentFormattedTime())
						.withDisplayedAt(DateUtils.formatDateForBackend(Session.getInstance().getGeofenceTimestamp()));
			}

			case 2: {
				final ArrayList<String> promptPositions = new ArrayList<>();
				int len = getSize();
				SparseBooleanArray checked = getCheckedItemPositions();
				for (int i = 0; i < len; i++) {
					if (checked.get(i)) {
						promptPositions.add(mPrompt.getChoices().get(i));
					}
				}

				//this is for the no response case
				if (promptPositions.size() == 0) promptPositions.add("");

				return new PromptAnswer()
						.withAnswer(promptPositions)
						.withPrompt(mPrompt.getPrompt())
						.withLatitude(Session.getInstance().getLastRecordedLatitude())
						.withLongitude(Session.getInstance().getLastRecordedLongitude())
						.withRecordedAt(DateUtils.getCurrentFormattedTime())
						.withDisplayedAt(DateUtils.formatDateForBackend(Session.getInstance().getGeofenceTimestamp()));
			}


			default:
				if (BuildConfig.DEBUG)
					throw new NotImplementedException("No prompt id type " + mPrompt.getId());
				return null;
		}
	}


//	@TODO: verify
	public int getAdapterMeasuredHeight(int width) {
		measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		return getMeasuredHeight();
	}
}
