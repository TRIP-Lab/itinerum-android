package ca.itinerum.android.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import ca.itinerum.android.R;

public class ContinueBackFrameLayout extends RelativeLayout {

	@NonNull private AppCompatButton mContinueButton;
	@NonNull private AppCompatImageButton mBackButton;

	@Nullable private OnClickListener mBackButtonListener;
	@Nullable private OnClickListener mContinueButtonListener;

	//TODO: make this a param for xml
	private String mContinueButtonText;

	public ContinueBackFrameLayout(@NonNull Context context) {
		super(context);
	}

	public ContinueBackFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public ContinueBackFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mContinueButton = new AppCompatButton(getContext(), null, R.style.StockButton_DarkTheme_Bottom);

		mContinueButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mContinueButtonListener != null) mContinueButtonListener.onClick(v);
			}
		});

		addView(mContinueButton);

		mBackButton = new AppCompatImageButton(getContext(), null, R.style.BackButton);
		mBackButton.setImageResource(R.drawable.ic_back_vector);


		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBackButtonListener != null) mBackButtonListener.onClick(v);
			}
		});

		addView(mBackButton);

	}

	public void setBackButtonListener(OnClickListener listener) {
		mBackButtonListener = listener;
	}

	public void setContinueButtonListener(OnClickListener listener) {
		mContinueButtonListener = listener;

	}

	public void setBackButtonVisible(boolean backButtonVisible) {
		mBackButton.setVisibility(backButtonVisible ? VISIBLE : GONE);
	}

	public void setContinueButtonText(String continueButtonText) {
		mContinueButtonText = continueButtonText;
		if (mContinueButton != null) mContinueButton.setText(continueButtonText);
	}
}
