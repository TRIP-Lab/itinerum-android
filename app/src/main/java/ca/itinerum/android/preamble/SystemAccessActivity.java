package ca.itinerum.android.preamble;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.common.NavigationActivity;
import ca.itinerum.android.common.RoundedCheckboxView;
import ca.itinerum.android.survey.EthicsFragment;
import ca.itinerum.android.survey.OnFragmentInteractionListener;
import ca.itinerum.android.survey.SurveySelectionDialogFragment;
import ca.itinerum.android.sync.retrofit.CreateResponse;
import ca.itinerum.android.sync.retrofit.ErrorMessage;
import ca.itinerum.android.sync.retrofit.Triplab;
import ca.itinerum.android.sync.retrofit.User;
import ca.itinerum.android.sync.retrofit.UserParams;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.SystemUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SystemAccessActivity extends NavigationActivity implements ActivityCompat.OnRequestPermissionsResultCallback, OnFragmentInteractionListener {

	private final int LOCATION_PERMISSION_CODE = 59662;
	private final String CODE_FRAGMENT = "CODE_FRAGMENT";
	private final String CODE_ETHICS = "CODE_ETHICS";

	private boolean mAcceptResearchEthics = false;
	private boolean mValidSurvey = false;
	private boolean mHasHardcodedSurvey = false;

	@BindView(R.id.progress_bar) FrameLayout mProgressBar;
	@BindView(R.id.container) FrameLayout mContainer;
	@BindView(R.id.card_research_ethics) RoundedCheckboxView mCardResearchEthics;
	@BindView(R.id.card_location_permission) RoundedCheckboxView mCardLocationPermission;
	@BindView(R.id.card_survey_code) RoundedCheckboxView mCardSurveyCode;

	private String mSurveyName;
	private SurveySelectionDialogFragment mSurveySelectFragment;
	private EthicsFragment mEthicsFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.view_preamble_system_access);
		super.onCreate(savedInstanceState);
		ButterKnife.bind(this);

		if (BuildConfig.DEBUG) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4569);
			}
		}

		// set up the ethics fragment in this activity
		mEthicsFragment = EthicsFragment.newInstance(CODE_ETHICS);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_container, mEthicsFragment);
		ft.hide(mEthicsFragment);
		ft.commit();

		mCardResearchEthics.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mAcceptResearchEthics = isChecked;
				SharedPreferenceManager.getInstance(SystemAccessActivity.this).setResearchEthicsAgreement(isChecked);
				checkAdvance();
			}
		});

		mCardResearchEthics.setChecked(SharedPreferenceManager.getInstance(this).getResearchEthicsAgreement());

		mCardResearchEthics.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEthicsFragment == null || !mEthicsFragment.isVisible()) {
					showResearchEthicsFragment();
				}
			}
		});

		// TODO: show research ethics as dialog fragment or full fragment

		// Location card appears in API 23+ because of runtime permissions

		if (Build.VERSION.SDK_INT < 23) {
			mCardLocationPermission.setVisibility(View.GONE);
		}

		mCardLocationPermission.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestRuntimeLocationPermission();
			}
		});

		mCardLocationPermission.setChecked(locationEnabled());

		mCardLocationPermission.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				buttonView.setChecked(locationEnabled());
			}
		});

		// Survey code will appear in apps that don't have a hardcoded code

		mSurveyName = getResources().getString(R.string.default_code);
		mValidSurvey = !StringUtils.isBlank(mSurveyName);
		mHasHardcodedSurvey = mValidSurvey;

		if (!mValidSurvey) {
			mSurveyName = SharedPreferenceManager.getInstance(this).getSurveyName();
			mValidSurvey = mSurveyName != null;
		}

		mCardSurveyCode.setVisibility(mValidSurvey ? View.GONE : View.VISIBLE);

		mCardSurveyCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mValidSurvey) return;

				showSurveySelectFragment();
			}
		});

		mCardSurveyCode.setChecked(mValidSurvey);

		// Housekeeping

		mContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onButtonPressed();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkAdvance();
	}

	private void onButtonPressed() {

		if (mHasHardcodedSurvey) {
			checkSurveyCode(true);
		} else {
			finishAndAdvance();
		}

	}

	protected void finishAndAdvance() {
		Intent intent;

		if (SharedPreferenceManager.getInstance(this).getTermsOfServiceRequired()) {
			intent = new Intent(SystemAccessActivity.this, TermsOfServiceActivity.class);
		} else {
			intent = new Intent(SystemAccessActivity.this, LetsGoActivity.class);
		}

		startActivity(intent);
		SystemAccessActivity.this.finish();
	}

	private void showSurveySelectFragment() {
		mSurveySelectFragment = SurveySelectionDialogFragment.newInstance(CODE_FRAGMENT);
		mSurveySelectFragment.show(getSupportFragmentManager(), mSurveySelectFragment.getName());
	}

	private void showResearchEthicsFragment() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations( R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
		ft.show(mEthicsFragment);
		ft.commit();
	}

	private void hideResearchEthicsFragment() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations( R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
		ft.hide(mEthicsFragment);
		ft.commit();
	}

	private void requestRuntimeLocationPermission() {
		if (!locationEnabled()) {
			String[] permission = {android.Manifest.permission.ACCESS_FINE_LOCATION};
			ActivityCompat.requestPermissions(this, permission, LOCATION_PERMISSION_CODE);
		}
	}

	private boolean locationEnabled() {
		return !(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
	}

	private boolean checkAdvance() {
		mCardLocationPermission.setChecked(locationEnabled());
		mCardResearchEthics.setChecked(mAcceptResearchEthics);
		mCardSurveyCode.setChecked(mValidSurvey);

		if (mAcceptResearchEthics && mValidSurvey && locationEnabled()) {
			mContinueButton.setEnabled(true);
			mContinueButton.setClickable(true);
			mContinueButton.setAlpha(1f);
		} else {
			mContinueButton.setEnabled(false);
			mContinueButton.setClickable(false);
			mContinueButton.setAlpha(0.5f);
		}

		return mAcceptResearchEthics && mValidSurvey && locationEnabled();

	}

	@TargetApi(Build.VERSION_CODES.M)
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == LOCATION_PERMISSION_CODE) {
			if (grantResults[0] == -1) {
				Toast.makeText(this, R.string.permissions_warning, Toast.LENGTH_SHORT).show();
				mCardLocationPermission.setChecked(false);
			} else {
				mCardLocationPermission.setChecked(true);
			}

			checkAdvance();
		}

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	private void checkSurveyCode(final boolean advance) {
		// TODO: get code value from fragment
		// TODO: check it via retrofit

		SystemUtils.hideKeyboardFrom(this, mContainer);

		if (StringUtils.isBlank(mSurveyName)) {
			Toast.makeText(this, R.string.invalid_survey_code, Toast.LENGTH_SHORT).show();
			return;
		}

		if (mSurveySelectFragment != null) mSurveySelectFragment.dismiss();

		mProgressBar.setVisibility(View.VISIBLE);
		mProgressBar.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		String uuid = SharedPreferenceManager.getInstance(this).getUUID();

		UserParams params = new UserParams()
				.withItinerumVersion(BuildConfig.VERSION_NAME)
				.withModel(Build.MODEL)
				.withOs("Android")
				.withOsVersion(Build.VERSION.RELEASE)
				.withUuid(uuid);

		User user = new User()
				.withUser(params)
				.withLang(Locale.getDefault().getLanguage())
				.withSurveyName(mSurveyName);

		Triplab triplab = new Triplab();
		triplab.getApi().createUser(user).enqueue(new Callback<CreateResponse>() {
			@Override
			public void onResponse(Call<CreateResponse> call, Response<CreateResponse> response) {

				if (!response.isSuccessful()) {
					mValidSurvey = mHasHardcodedSurvey;
					checkAdvance();
					mProgressBar.setVisibility(View.GONE);
					mSurveySelectFragment.show(getSupportFragmentManager(), mSurveySelectFragment.getName());
					try {
						/* here we parse the error, however at the moment there's only one 400 possible */
						Logger.l.e(response.errorBody());
						final String responseStream = response.errorBody().string();
						Logger.l.e(responseStream);
						ErrorMessage error = (new Gson()).fromJson(responseStream, ErrorMessage.class);
						if (error.getErrors() != null && error.getErrors().length > 0) Logger.l.e(error.getErrors()[0]);
						/* error parsing done */
						final String message = getString(R.string.survey_not_found);
						SystemAccessActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(SystemAccessActivity.this, message, Toast.LENGTH_LONG).show();
							}
						});
					} catch (IOException e) {
						Logger.l.e(e.toString());
					} catch (JsonSyntaxException exception) {
						Logger.l.e(exception.toString());
						SystemAccessActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(SystemAccessActivity.this, getString(R.string.unable_to_join_message), Toast.LENGTH_LONG).show();
							}
						});
					}
					return;
				}

				mValidSurvey = true;

				SharedPreferenceManager sp = SharedPreferenceManager.getInstance(SystemAccessActivity.this);

				sp.setSurveyId(response.body().getResults().getSurveyId());
				sp.setSurveyResponseObject(response.body().getResults());
				sp.setNumberOfRecordingDays(response.body().getResults().getPrompt().getMaxDays());
				sp.setNumberOfPrompts(response.body().getResults().getPrompt().getPrompts().size());
				sp.setMaximumNumberOfPrompts(response.body().getResults().getPrompt().getMaxPrompts());
				sp.setSurveyName(mSurveyName);
				sp.setTermsOfServiceRequired(response.body().getResults().getTermsOfService() != null && !BuildConfig.FLAVOR.equals("montreal"));

				checkAdvance();

				if (advance) finishAndAdvance();

				mProgressBar.setVisibility(View.GONE);

			}

			@Override
			public void onFailure(Call<CreateResponse> call, Throwable t) {
				Logger.l.e("onFailure", t.toString());
				mProgressBar.setVisibility(View.GONE);
				SystemAccessActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(SystemAccessActivity.this, getString(R.string.unable_to_join_message), Toast.LENGTH_LONG).show();
					}
				});

				final SurveySelectionDialogFragment fragment = (SurveySelectionDialogFragment) SystemAccessActivity.this.getSupportFragmentManager().findFragmentByTag(CODE_FRAGMENT);
				if (fragment != null) {
					SystemAccessActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							fragment.mContinueButton.setEnabled(true);
						}
					});
				}
			}
		});

	}

	@Override
	public void onBackPressed() {
		if (mEthicsFragment.isVisible()) {
			hideResearchEthicsFragment();
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onFragmentInteraction(String fragmentName, Map<String, Object> results) {
		if (results != null) parseResults(results);
	}

	private void parseResults(Map<String, Object> results) {
		if (results.containsKey(CODE_FRAGMENT)) {
			mSurveyName = (String) results.get(CODE_FRAGMENT);
			checkSurveyCode(false)	;
		} else if (results.containsKey(CODE_ETHICS)) {
			mAcceptResearchEthics = (boolean) results.get(CODE_ETHICS);
			hideResearchEthicsFragment();
			checkAdvance();
		}
	}

}
