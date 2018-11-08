package ca.itinerum.android.preamble;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.common.NavigationActivity;
import ca.itinerum.android.common.RoundedCheckboxView;

public class WelcomeActivity extends NavigationActivity {

	@BindView(R.id.icon) AppCompatImageView mIcon;
	@BindView(R.id.title) AppCompatTextView mTitle;
	@BindView(R.id.paragraph) AppCompatTextView mParagraph;
	@BindView(R.id.age_constraint) RoundedCheckboxView mAgeConstraint;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		setContentView(R.layout.view_preamble_welcome);
		super.onCreate(savedInstanceState);
		ButterKnife.bind(this);
		mTitle.setText(R.string.welcome_title);
		mParagraph.setText(R.string.welcome_paragraph);
		mBackButton.setVisibility(View.INVISIBLE);

		if (BuildConfig.AGE_CONSTRAINT) {
			toggleContinueButton(false);
			mAgeConstraint.setVisibility(View.VISIBLE);
			mAgeConstraint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					toggleContinueButton(isChecked);
				}
			});
		}
	}
}
