package ca.itinerum.android.preamble;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.common.NavigationActivity;
import ca.itinerum.android.sync.retrofit.Triplab;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.SystemUtils;

public class TermsOfServiceActivity extends NavigationActivity {

	@BindView(R.id.banner) SimpleDraweeView mBanner;
	@BindView(R.id.ethics_title) AppCompatTextView mEthicsTitle;
	@BindView(R.id.ethics_header) AppCompatTextView mEthicsHeader;
	@BindView(R.id.ethics_body) AppCompatTextView mEthicsBody;
	@BindView(R.id.ethics_postamble) AppCompatTextView mEthicsPostamble;
	@BindView(R.id.decline_button) AppCompatButton mDeclineButton;
	@BindView(R.id.container) FrameLayout mContainer;

	@BindString(R.string.agree) String AGREE_BTN;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_terms_of_service);
		super.onCreate(savedInstanceState);
		ButterKnife.bind(this);

		SystemUtils.hideKeyboardFrom(this, mContainer);

		mContinueButton.setText(AGREE_BTN);

		final SharedPreferenceManager sp = SharedPreferenceManager.getInstance(this);

		if (StringUtils.isEmpty(sp.getSurveyResponseObject().getTermsOfService())) finishAndAdvance();

		mEthicsTitle.setText(R.string.terms_of_service_title);
		mEthicsHeader.setVisibility(View.GONE);
		mEthicsPostamble.setVisibility(View.GONE);

		mEthicsBody.setText(sp.getSurveyResponseObject().getTermsOfService());
		mBanner.setImageURI(Triplab.sDashboardBaseURL + sp.getAvatar());

		mContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				sp.setTermsOfServiceRequired(false);
				finishAndAdvance();
			}
		});

		mDeclineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDeclinedView();
			}
		});
	}

	private void showDeclinedView() {
		new AlertDialog.Builder(this, R.style.AppTheme_NoActionBar_Survey_Alert).setMessage(R.string.message_terms_non_conforming)
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
