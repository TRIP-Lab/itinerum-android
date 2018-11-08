package ca.itinerum.android;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.system.ErrnoException;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;

public class TwoButtonDialog extends DialogFragment {

	@BindView(R.id.positive_button) AppCompatButton mPositiveButton;
	@BindView(R.id.negative_button) AppCompatButton mNegativeButton;
	@BindView(R.id.message) AppCompatTextView mMessage;

	public static TwoButtonDialog newInstance(@NonNull String type, String message, String positive, String negative) {
		TwoButtonDialog fragment = new TwoButtonDialog();
		Bundle args = new Bundle();
		args.putString("type", type);
		args.putString("message", message);
		args.putString("positive", positive);
		args.putString("negative", negative);
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.view_dialog_two_button, container, false);
		ButterKnife.bind(this, v);

		mMessage.setText(getArguments().getString("message"));
		String positive = getArguments().getString("positive");
		String negative = getArguments().getString("negative");
		mPositiveButton.setText(positive);
		mNegativeButton.setText(negative);
		if (positive == null) mPositiveButton.setVisibility(View.GONE);
		if (negative == null) mNegativeButton.setVisibility(View.GONE);

		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		mPositiveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((PromptDialogListener) getActivity()).onPositiveButtonClicked(TwoButtonDialog.this);
			}
		});

		mNegativeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((PromptDialogListener) getActivity()).onNegativeButtonClicked(TwoButtonDialog.this);
			}
		});

		getDialog().setCanceledOnTouchOutside(false);
		getDialog().setCancelable(false);

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog != null) {
			DisplayMetrics displaymetrics = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			int width = (int) (displaymetrics.widthPixels * 0.9);

			dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

	}

	public String getType() {
		if (getArguments().getString("type") == null) throw new Error("TwoButtonDialog requires a type");
		return getArguments().getString("type");
	}

}