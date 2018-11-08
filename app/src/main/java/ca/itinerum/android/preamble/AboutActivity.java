package ca.itinerum.android.preamble;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.common.NavigationActivity;

public class AboutActivity extends NavigationActivity {
	@BindView(R.id.title) AppCompatTextView mTitle;
	@BindView(R.id.paragraph) AppCompatTextView mParagraph;
	@BindView(R.id.content_image) AppCompatImageView mContentImage;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		setContentView(R.layout.view_preamble_about);
		super.onCreate(savedInstanceState);
		ButterKnife.bind(this);

		mTitle.setText(R.string.about_title);
		mParagraph.setText(R.string.about_paragraph);
		mContentImage.setVisibility(View.VISIBLE);

	}
}
