package ca.itinerum.android;

import android.support.v4.app.DialogFragment;

interface PromptDialogListener {
	void onPositiveButtonClicked(DialogFragment fragment);

	void onNeutralButtonClicked(DialogFragment fragment);

	void onNegativeButtonClicked(DialogFragment fragment);
}
