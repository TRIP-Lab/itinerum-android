package ca.itinerum.android.preamble;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.common.NavigationActivity;
import ca.itinerum.android.survey.SurveyActivity;
import ca.itinerum.android.utilities.PreambleActivitiesHelper;

public class LetsGoActivity extends NavigationActivity {

	@BindView(R.id.icon) AppCompatImageView mIcon;
	@BindView(R.id.title) AppCompatTextView mTitle;
	@BindView(R.id.paragraph) AppCompatTextView mParagraph;
	@BindView(R.id.content_image) AppCompatImageView mContentImage;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		setContentView(R.layout.view_preamble_welcome);
		super.onCreate(savedInstanceState);
		ButterKnife.bind(this);

		mTitle.setText(R.string.letsgo_title);
		mParagraph.setText(R.string.letsgo_paragraph);
	}
}
