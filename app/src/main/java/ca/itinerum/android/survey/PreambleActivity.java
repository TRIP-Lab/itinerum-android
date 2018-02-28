package ca.itinerum.android.survey;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
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

public class PreambleActivity extends FragmentActivity implements OnFragmentInteractionListener, ActivityCompat.OnRequestPermissionsResultCallback {

	@BindView(R.id.progress_bar) ProgressBar mProgressBar;
	@BindView(R.id.fragment_container) FrameLayout mFragmentContainer;
	@BindView(R.id.container) RelativeLayout mContainer;
	@BindView(R.id.bottom_buttons_container) LinearLayout mBottomButtonsContainer;

	private NamedFragment mCurrentFragment;
	private String mSurveyName;

	public enum FragmentType {
		ETHICS,
		PERMISSIONS,
		WELCOME,
		SURVEY;

		public static FragmentType getFragmentTypeForTag(String tag) {
			for (FragmentType type : FragmentType.values()) {
				if (type.name().equals(tag)) return type;
			}
			if (BuildConfig.DEBUG) throw new NotImplementedException("tag " + tag + " not implemented");

			return ETHICS;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_container);
		ButterKnife.bind(this);

		if (BuildConfig.DEBUG) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4569);
			}
		}

		mBottomButtonsContainer.setVisibility(View.GONE);

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mCurrentFragment != null)
			showFragment(mCurrentFragment, FragmentType.getFragmentTypeForTag(mCurrentFragment.getTag()));
		else showResearchEthicsView();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void showFragment(NamedFragment fragment, FragmentType type) {
		showFragment(fragment, type, true);
	}

	private void showFragment(NamedFragment fragment, FragmentType type, boolean shouldBackstack) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, fragment, type.name());
		// we won't add the initial transition
		if (shouldBackstack) fragmentTransaction.addToBackStack(type.name());
		mCurrentFragment = fragment;
		fragmentTransaction.commitAllowingStateLoss();
	}

	private void showResearchEthicsView() {
		showFragment(EthicsFragment.newInstance(FragmentType.ETHICS.name()), FragmentType.ETHICS, false);
	}

	private void showPermissionsQuestion() {
		showFragment(PermissionsFragment.newInstance(FragmentType.PERMISSIONS.name()), FragmentType.PERMISSIONS);
	}

	private void showWelcomeFragment() {
		showFragment(WelcomeFragment.newInstance(FragmentType.WELCOME.name()), FragmentType.WELCOME);
	}

	private void showSurveySelectFragment() {
		FragmentManager manager = getSupportFragmentManager();
		android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();

		SurveySelectionDialogFragment d = SurveySelectionDialogFragment.newInstance(FragmentType.SURVEY.name());
		d.show(transaction, d.getName());
	}

	private void checkSurveyCode() {
		// TODO: get code value from fragment
		// TODO: check it via retrofit

		SystemUtils.hideKeyboardFrom(this, mContainer);

		if (StringUtils.isBlank(mSurveyName)) {
			Snackbar.make(mContainer, R.string.invalid_survey_code, Snackbar.LENGTH_LONG).show();
			return;
		}

		mProgressBar.setVisibility(View.VISIBLE);

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
				mProgressBar.setVisibility(View.GONE);

				if (!response.isSuccessful()) {
					try {
						Logger.l.e(response.errorBody());
						final String responseStream = response.errorBody().string();
						Logger.l.e(responseStream);
						ErrorMessage error = (new Gson()).fromJson(responseStream, ErrorMessage.class);
						String m = error.getMessage();
						if (m == null) m = error.getError();
						if (m == null) m = getString(R.string.unable_to_join_message);
						final String message = m;
						PreambleActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(PreambleActivity.this, message, Toast.LENGTH_LONG).show();
							}
						});
					} catch (IOException e) {
						Logger.l.e(e.toString());
					} catch (JsonSyntaxException exception) {
						Logger.l.e(exception.toString());
						PreambleActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(PreambleActivity.this, getString(R.string.unable_to_join_message), Toast.LENGTH_LONG).show();
							}
						});
					}
					return;
				}

				SharedPreferenceManager sp = SharedPreferenceManager.getInstance(PreambleActivity.this);

				//Logger.l.d(response.code());

				sp.setSurveyId(response.body().getResults().getSurveyId());
				sp.setSurveyResponseObject(response.body().getResults());
				sp.setNumberOfRecordingDays(response.body().getResults().getPrompt().getMaxDays());
				sp.setNumberOfPrompts(response.body().getResults().getPrompt().getPrompts().size());
				sp.setMaximumNumberOfPrompts(response.body().getResults().getPrompt().getMaxPrompts());
				sp.setSurveyName(mSurveyName);
				if (response.body().getResults().getTermsOfService() != null) {
					SharedPreferenceManager.getInstance(PreambleActivity.this).setTermsOfServiceRequired(true);
					Intent intent = new Intent(PreambleActivity.this, TermsOfServiceActivity.class);
					startActivity(intent);
					PreambleActivity.this.finish();
				} else {
					SharedPreferenceManager.getInstance(PreambleActivity.this).setTermsOfServiceRequired(false);
					Intent intent = new Intent(PreambleActivity.this, SurveyActivity.class);
					startActivity(intent);
					PreambleActivity.this.finish();
				}

				final SurveySelectionDialogFragment fragment = (SurveySelectionDialogFragment) PreambleActivity.this.getSupportFragmentManager().findFragmentByTag(FragmentType.SURVEY.name());
				if (fragment != null) {
					PreambleActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							fragment.mContinueButton.setEnabled(true);
						}
					});
				}

			}

			@Override
			public void onFailure(Call<CreateResponse> call, Throwable t) {
				Logger.l.e("onFailure", t.toString());
				mProgressBar.setVisibility(View.GONE);
				PreambleActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(PreambleActivity.this, getString(R.string.unable_to_join_message), Toast.LENGTH_LONG).show();
					}
				});

				final SurveySelectionDialogFragment fragment = (SurveySelectionDialogFragment) PreambleActivity.this.getSupportFragmentManager().findFragmentByTag(FragmentType.SURVEY.name());
				if (fragment != null) {
					PreambleActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							fragment.mContinueButton.setEnabled(true);
						}
					});
				}
			}
		});

	}

	private void moveToNextFragment(String fragmentName) {
		switch (FragmentType.getFragmentTypeForTag(fragmentName)) {
			case ETHICS:
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					showPermissionsQuestion();
				} else {
					showWelcomeFragment();
				}
				break;
			case PERMISSIONS:
				showWelcomeFragment();
				break;
			case WELCOME:
				String code = getString(R.string.default_code);
				if (!StringUtils.isEmpty(code)) {
					mSurveyName = code;
					checkSurveyCode();
				}
				else showSurveySelectFragment();
				break;
			case SURVEY:
				checkSurveyCode();
				break;
		}
	}

	private NamedFragment getBackstackFragment() {
		return (NamedFragment) getSupportFragmentManager().findFragmentByTag(getBackstackFragmentName());
	}

	private String getBackstackFragmentName() {
		FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
		return backEntry.getName();
	}

	@Override
	public void onBackPressed() {
		if (getFragmentManager().getBackStackEntryCount() > 0) {
			mCurrentFragment = getBackstackFragment();
			getFragmentManager().popBackStack();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onFragmentInteraction(String fragmentName, Map<String, Object> results) {
		if (results != null) parseResults(results);
		moveToNextFragment(fragmentName);
	}

	private void parseResults(Map<String, Object> results) {
		if (results.containsKey(FragmentType.SURVEY.name())) {
			mSurveyName = (String) results.get(FragmentType.SURVEY.name());
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

		if (mCurrentFragment != null && mCurrentFragment instanceof PermissionsFragment) {
			mCurrentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

}
