package ca.itinerum.android.onboarding;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.method.LinkMovementMethod;
import android.view.View;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.DMApplication;
import ca.itinerum.android.MainActivity;
import ca.itinerum.android.utilities.SharedPreferenceManager;

public class OnboardActivity extends Activity {

	@BindView(R.id.title) AppCompatTextView mTitle;
	@BindView(R.id.paragraph) AppCompatTextView mParagraph;
	@BindView(R.id.back_button) AppCompatImageButton mBackButton;
	@BindView(R.id.continue_button) AppCompatButton mContinueButton;
	@BindView(R.id.content_image) AppCompatImageView mContentImage;
	@BindView(R.id.focus_button_image) FloatingActionButton mFocusButtonImage;
	@BindView(R.id.trip_list_button) FloatingActionButton mAppContentButton;
	@BindView(R.id.app_settings_button) FloatingActionButton mAppSettingsButton;
	@BindView(R.id.app_info_button) FloatingActionButton mAppInfoButton;

	private OnboardView mCurrentView = OnboardView.WELCOME_ONBOARD;

	private enum OnboardView {
		WELCOME_ONBOARD,
		HELP_1,
		HELP_2,
		HELP_3,
		HELP_4,
		USING_APP_1,
		USING_APP_2,
		USING_APP_3,
		USING_APP_4,
		THANKS;

		public OnboardView nextView(Context context) {
			int value = this.ordinal();
			while(true) {
				value++;
				if (value > OnboardView.values().length - 1) return null;
				if (OnboardView.values()[value].isValidView(context)) return OnboardView.values()[value];
			}
		}

		public OnboardView previousView(Context context) {
			int value = this.ordinal();
			while(true) {
				value--;
				if (value < 0) return null;
				if (OnboardView.values()[value].isValidView(context)) return OnboardView.values()[value];
			}
		}

		/**
		 * The title for each of these cards will determine the validity of the view for flavours
		 * @param context
		 * @return
		 */
		public boolean isValidView(Context context) {
			return !StringUtils.isBlank(context.getString(getTitleResource()));
		}

		public int getTitleResource() {
			switch (this) {
				case WELCOME_ONBOARD:
					return R.string.welcome_onboard_title;
				case HELP_1:
					return R.string.help_1_title;
				case HELP_2:
					return R.string.help_2_title;
				case HELP_3:
					return R.string.help_3_title;
				case HELP_4:
					return R.string.help_4_title;
				case USING_APP_1:
					return R.string.using_itinerum_1_title;
				case USING_APP_2:
					return R.string.using_itinerum_2_title;
				case USING_APP_3:
					return R.string.using_itinerum_3_title;
				case USING_APP_4:
					return R.string.using_itinerum_4_title;
				case THANKS:
					return R.string.thanks_title;
			}
			return 0;
		}
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_onboarding);
		ButterKnife.bind(this);

		mParagraph.setMovementMethod(LinkMovementMethod.getInstance());

		mBackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCurrentView != null) mCurrentView = mCurrentView.previousView(OnboardActivity.this);
				refreshView();
			}
		});

		mContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCurrentView != null) mCurrentView = mCurrentView.nextView(OnboardActivity.this);
				refreshView();
			}
		});

		refreshView();

	}

	private void refreshView() {

		if (mCurrentView == null) {

			if (!SharedPreferenceManager.getInstance(this).hasSeenTutorial()) {
				SharedPreferenceManager.getInstance(this).setSeenTutorial(true);
				Intent intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}

			finish();
			return;
		}

		// reset all views
		mBackButton.setVisibility(View.VISIBLE);
		mParagraph.setVisibility(View.VISIBLE);
		mContentImage.setVisibility(View.VISIBLE);
		mFocusButtonImage.setVisibility(View.VISIBLE);
		mAppContentButton.setVisibility(View.VISIBLE);
		mAppSettingsButton.setVisibility(View.VISIBLE);
		mAppInfoButton.setVisibility(View.VISIBLE);

		mTitle.setText(mCurrentView.getTitleResource());

		// configure views for individual reqs
		switch (mCurrentView) {
			case WELCOME_ONBOARD:
				mFocusButtonImage.setVisibility(View.GONE);
				mAppContentButton.setVisibility(View.GONE);
				mAppSettingsButton.setVisibility(View.GONE);
				mAppInfoButton.setVisibility(View.GONE);
				mBackButton.setVisibility(View.INVISIBLE);
				mParagraph.setText(R.string.welcome_onboard_paragraph);
				mContentImage.setImageResource(R.drawable.onboard_welcome);
				mContinueButton.setText(R.string.next);
				break;
			case HELP_1:
				mFocusButtonImage.setVisibility(View.GONE);
				mAppContentButton.setVisibility(View.GONE);
				mAppSettingsButton.setVisibility(View.GONE);
				mAppInfoButton.setVisibility(View.GONE);
				mParagraph.setText(R.string.help_1_paragraph);
				mContentImage.setImageResource(R.drawable.onboard_help_1);
				mContinueButton.setText(R.string.next);
				break;
			case HELP_2:
				mFocusButtonImage.setVisibility(View.GONE);
				mAppContentButton.setVisibility(View.GONE);
				mAppSettingsButton.setVisibility(View.GONE);
				mAppInfoButton.setVisibility(View.GONE);
				mParagraph.setText(R.string.help_2_paragraph);
				mContentImage.setImageResource(R.drawable.onboard_help_2);
				mContinueButton.setText(R.string.next);
				break;
			case HELP_3:
				mFocusButtonImage.setVisibility(View.GONE);
				mAppContentButton.setVisibility(View.GONE);
				mAppSettingsButton.setVisibility(View.GONE);
				mAppInfoButton.setVisibility(View.GONE);
				mParagraph.setText(R.string.help_3_paragraph);
				mContentImage.setImageResource(R.drawable.onboard_help_3);
				mContinueButton.setText(R.string.next);
				break;
			case HELP_4:
				mFocusButtonImage.setVisibility(View.GONE);
				mAppContentButton.setVisibility(View.GONE);
				mAppSettingsButton.setVisibility(View.GONE);
				mAppInfoButton.setVisibility(View.GONE);
				mParagraph.setText(R.string.help_1_paragraph);
				mContentImage.setImageResource(0);
				mContinueButton.setText(R.string.next);
				break;
			case USING_APP_1:
				mFocusButtonImage.setVisibility(View.GONE);
				mAppContentButton.setVisibility(View.GONE);
				mAppSettingsButton.setVisibility(View.GONE);
				mAppInfoButton.setVisibility(View.GONE);
				mParagraph.setText(R.string.using_itinerum_1_paragraph);
				mContentImage.setImageResource(R.drawable.onboard_help_1);
				mContinueButton.setText(R.string.next);
				break;
			case USING_APP_2:
				mAppContentButton.setVisibility(View.GONE);
				mAppSettingsButton.setVisibility(View.GONE);
				mAppInfoButton.setVisibility(View.GONE);
				mParagraph.setText(R.string.using_itinerum_2_paragraph);
				mContentImage.setVisibility(View.GONE);
				mFocusButtonImage.setImageResource(R.drawable.ic_logo_vector);
				mContinueButton.setText(R.string.next);
				break;
			case USING_APP_3:
				mContentImage.setVisibility(View.GONE);
				mAppSettingsButton.setVisibility(View.GONE);
				mAppInfoButton.setVisibility(View.GONE);
				mParagraph.setText(R.string.using_itinerum_3_paragraph);
				mFocusButtonImage.setImageResource(R.drawable.ic_settings_vector);
				mContinueButton.setText(R.string.next);
				break;
			case USING_APP_4:
				mParagraph.setText(R.string.using_itinerum_4_paragraph);
				mFocusButtonImage.setImageResource(R.drawable.ic_info_vector);
				mContentImage.setVisibility(View.GONE);
				mContinueButton.setText(R.string.next);
				mAppInfoButton.setVisibility(View.GONE);
				break;
			case THANKS:
				mContentImage.setVisibility(View.GONE);
				mFocusButtonImage.setVisibility(View.GONE);
				mParagraph.setText(R.string.thanks_paragraph);
				mContinueButton.setText(R.string.lets_start);
				break;
			default:
				if (BuildConfig.DEBUG) throw new NotImplementedException(mCurrentView.name() + " not implemented");
		}
	}

	@Override
	public void onBackPressed() {
		if (mCurrentView.previousView(OnboardActivity.this) == null) {
			super.onBackPressed();
			return;
		}

		mCurrentView = mCurrentView.previousView(OnboardActivity.this);
		refreshView();

	}
}
