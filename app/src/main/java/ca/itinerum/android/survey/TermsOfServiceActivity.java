package ca.itinerum.android.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.sync.retrofit.Triplab;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.SystemUtils;

public class TermsOfServiceActivity extends Activity {

	@BindView(R.id.banner) SimpleDraweeView mBanner;
	@BindView(R.id.ethics_title) TextView mEthicsTitle;
	@BindView(R.id.ethics_header) TextView mEthicsHeader;
	@BindView(R.id.ethics_body) TextView mEthicsBody;
	@BindView(R.id.ethics_postamble) TextView mEthicsPostamble;
	@BindView(R.id.fragment_back_button) ImageButton mFragmentBackButton;
	@BindView(R.id.fragment_continue_button) Button mFragmentContinueButton;
	@BindView(R.id.bottom_buttons_container) LinearLayout mBottomButtonsContainer;
	@BindView(R.id.container) ScrollView mContainer;

	@BindColor(R.color.base_colour) int CONTINUE_BUTTON_COLOUR;
	@BindColor(android.R.color.darker_gray) int DISABLE_BUTTON_COLOUR;

	@BindDrawable(R.drawable.cancel) Drawable CANCEL_DRAWABLE;

	@BindString(R.string.agree_box) String AGREE_BTN;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_terms_of_service);
		ButterKnife.bind(this);

		SystemUtils.hideKeyboardFrom(this, mContainer);

		mFragmentContinueButton.setText(AGREE_BTN);
		mFragmentBackButton.setImageDrawable(CANCEL_DRAWABLE);

		final SharedPreferenceManager sp = SharedPreferenceManager.getInstance(this);

		mEthicsTitle.setText(R.string.terms_of_service_title);
		mEthicsHeader.setVisibility(View.GONE);
		mEthicsPostamble.setVisibility(View.GONE);

		mEthicsBody.setText(sp.getSurveyResponseObject().getTermsOfService());
		mBanner.setImageURI(Triplab.sDashboardBaseURL + sp.getAvatar());

		mFragmentContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sp.setTermsOfServiceRequired(false);
				startActivity(new Intent(TermsOfServiceActivity.this, SurveyActivity.class));
				TermsOfServiceActivity.this.finish();
			}
		});

		mFragmentBackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDeclinedView();
			}
		});
	}

	private void showDeclinedView() {
		new AlertDialog.Builder(this).setMessage(R.string.message_terms_non_conforming)
				.setPositiveButton(android.R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SystemUtils.leaveCurrentSurvey(TermsOfServiceActivity.this);
						onBackPressed();
					}
				})
				.show();
	}
}
