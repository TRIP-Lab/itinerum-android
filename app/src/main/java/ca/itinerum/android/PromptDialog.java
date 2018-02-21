package ca.itinerum.android;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;

import ca.itinerum.android.sync.retrofit.Prompt;
import ca.itinerum.android.sync.retrofit.PromptAnswer;

public class PromptDialog extends DialogFragment {

	private PromptDialogListView mPromptDialogListView;

	public static PromptDialog newInstance(Prompt prompt, int currentPrompt, ArrayList<String> selected) {
		PromptDialog frag = new PromptDialog();
		Bundle args = new Bundle();
		args.putInt("currentPrompt", currentPrompt);
		args.putStringArrayList("selected", selected);
		args.putParcelable("prompt", prompt);
		frag.setArguments(args);
		return frag;
	}

	public PromptAnswer getPromptAnswer() {
		return mPromptDialogListView.getPromptAnswer();
	}

	@Override
	public void onStart() {
		super.onStart();
        // this overrides the default onclick dismiss action
		((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((PromptDialogListener) getActivity()).onPositiveButtonClicked();
			}
		});
	}

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int currentPrompt = getArguments().getInt("currentPrompt");
		ArrayList<String> promptPositions = getArguments().getStringArrayList("selected");
		Prompt prompt = getArguments().getParcelable("prompt");

		TextView titleView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.textview_prompt_title, null);
		titleView.setText(prompt.getPrompt());

		AlertDialog.Builder dialogBuilder;
		mPromptDialogListView = (PromptDialogListView) LayoutInflater.from(getContext()).inflate(R.layout.listview_dialog_prompt, null);

		boolean[] selected = new boolean[prompt.getChoices().size()];

		for (int i = 0; i < prompt.getChoices().size(); i++) {
			selected[i] = promptPositions.contains(prompt.getChoices().get(i));
		}

		mPromptDialogListView.setPromptContent(prompt, selected);

		// single choice prompt
		if (prompt.getId() != 1 && prompt.getId() != 2) {
			if (BuildConfig.DEBUG) throw new NotImplementedException("No prompt id type " + prompt.getId());
			return null;
		}

		dialogBuilder = new AlertDialog.Builder(getContext())
				.setCustomTitle(titleView)
				.setView(mPromptDialogListView)
				.setPositiveButton(R.string.continue_button, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});

		if (currentPrompt != 0) {
			dialogBuilder.setNeutralButton(R.string.back_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialogBuilder, int which) {
					((PromptDialogListener) getActivity()).onNeutralButtonClicked();
				}
			});
		} else {
			dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogBuilder, int which) {
					((PromptDialogListener) getActivity()).onNegativeButtonClicked();
				}
			});
		}

		AlertDialog dialog = dialogBuilder.create();

		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);

		return dialog;
	}

	public interface PromptDialogListener {
		void onPositiveButtonClicked();
		void onNeutralButtonClicked();
		void onNegativeButtonClicked();
	}

}