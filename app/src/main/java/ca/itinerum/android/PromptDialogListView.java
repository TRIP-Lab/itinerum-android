package ca.itinerum.android;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;

import ca.itinerum.android.recording.Session;
import ca.itinerum.android.sync.retrofit.Prompt;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.DateUtils;

/**
 * Created by stewjacks on 2018-01-12.
 */

public class PromptDialogListView extends ListView {


	private Prompt mPrompt;
	private ArrayAdapter<String> mAdapter;

	public PromptDialogListView(Context context) {
		super(context);
	}

	public PromptDialogListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PromptDialogListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	public void setPromptContent(Prompt prompt, int selected) {
		setPromptContent(prompt);
		setItemChecked(selected, true);
	}

	public void setPromptContent(Prompt prompt, boolean[] selected) {
		setPromptContent(prompt);

		for (int i = 0; i < selected.length; i++) {
			setItemChecked(i, selected[i]);
		}
	}

	// configure onclick listener for content change w/ callback to
	public void setPromptContent(final Prompt prompt) {
		mPrompt = prompt;

		if (prompt.getId() == 1) {
			setChoiceMode(CHOICE_MODE_SINGLE);
			mAdapter = new ArrayAdapter<>(getContext(), R.layout.simple_list_item_single_choice, prompt.getChoices());
		} else if (prompt.getId() == 2) {
			setChoiceMode(CHOICE_MODE_MULTIPLE);
			mAdapter = new ArrayAdapter<>(getContext(), R.layout.simple_list_item_multiple_choice, prompt.getChoices());
		} else {
			if (BuildConfig.DEBUG) throw new NotImplementedException("No prompt id type " + prompt.getId());
			return;
		}

		this.setAdapter(mAdapter);

	}

	public PromptAnswer getPromptAnswer() {
		if (getChoiceMode() == CHOICE_MODE_SINGLE) {
			if (getCheckedItemCount() == 0) return null;
			return new PromptAnswer()
					.withAnswer(mPrompt.getChoices().get(getCheckedItemPosition()))
					.withPrompt(mPrompt.getPrompt())
					.withLatitude(Session.getInstance().getLastRecordedLatitude())
					.withLongitude(Session.getInstance().getLastRecordedLongitude())
					.withRecordedAt(DateUtils.getCurrentFormattedTime())
					.withDisplayedAt(DateUtils.formatDateForBackend(Session.getInstance().getGeofenceTimestamp()));
		} else {
			final ArrayList<String> promptPositions = new ArrayList<>();
			int len = getCount();
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
	}

	public int getAdapterMeasuredHeight() {
		int count = getAdapter().getCount();
		int dividerHeight = getDividerHeight();

		int totalHeight = 0;

		for (int i = 0; i < count; i++) {
			View listItem = getAdapter().getView(i, null, this);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		totalHeight += dividerHeight * (count - 1) + getPaddingTop() + getPaddingBottom();

		return totalHeight;
	}
}
