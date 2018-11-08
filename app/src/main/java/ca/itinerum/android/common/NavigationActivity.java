package ca.itinerum.android.common;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.utilities.PreambleActivitiesHelper;

public class NavigationActivity extends FragmentActivity {

	@BindView(R.id.back_button) protected AppCompatImageButton mBackButton;
	@BindView(R.id.continue_button) protected AppCompatButton mContinueButton;

	private boolean mContinueClicked = false;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ButterKnife.bind(this);

		mBackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		mContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mContinueClicked) return;
				mContinueClicked = true;
				finishAndAdvance();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mContinueClicked = false;
	}

	@Override
	public void onBackPressed() {
		if (getParentActivityIntent() != null) {
			overridePendingTransition(0, 0);
			TaskStackBuilder.create(this).addNextIntent(getParentActivityIntent()).startActivities();
		}
		super.onBackPressed();
	}

	@Nullable
	@Override
	public Intent getParentActivityIntent() {
		Class<?> previousClass = PreambleActivitiesHelper.getPreviousActivity(this, getClass().getSimpleName());
		if (previousClass != null) return new Intent(this, previousClass);
		return super.getParentActivityIntent();
	}

	private Intent getChildActivityIntent() {
		Class<?> nextClass = PreambleActivitiesHelper.getNextActivity(this, getClass().getSimpleName());
		if (nextClass != null) return new Intent(this, nextClass);
		throw new Error(getClass().getSimpleName() + " does not have a child activity defined for navigation");
	}

	protected void finishAndAdvance() {
		Intent intent = getChildActivityIntent();
		if (intent != null) {
			overridePendingTransition(0,0);
			startActivity(intent);
		}
		else if (BuildConfig.DEBUG) throw new NotImplementedException("mNextIntent cannot be null");
	}

	protected void toggleContinueButton(boolean enabled) {
		if (enabled) {
			mContinueButton.setEnabled(true);
			mContinueButton.setClickable(true);
			mContinueButton.setAlpha(1f);
		} else {
			mContinueButton.setEnabled(false);
			mContinueButton.setClickable(false);
			mContinueButton.setAlpha(0.5f);
		}
	}
}
