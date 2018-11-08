package ca.itinerum.android.survey;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.onboarding.OnboardActivity;
import ca.itinerum.android.survey.views.BaseSurveyView;
import ca.itinerum.android.sync.retrofit.ErrorMessage;
import ca.itinerum.android.sync.retrofit.Survey;
import ca.itinerum.android.sync.retrofit.Triplab;
import ca.itinerum.android.sync.retrofit.Update;
import ca.itinerum.android.sync.retrofit.UpdateResponse;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.SystemUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SurveyActivity extends AppCompatActivity implements SurveyAdvanceListener {

	//NOTE: These are annoying requirements b/c we only show the school and work locations maps if the user is a worker and/or student.
	public boolean mIsEmployed;
	public boolean mIsStudent;

	@BindView(R.id.progress_bar) FrameLayout mProgressBar;
	@BindView(R.id.back_button) AppCompatImageButton mBackButton;
	@BindView(R.id.continue_button) AppCompatButton mContinueButton;
	@BindView(R.id.container) FrameLayout mContainer;
	@BindView(R.id.container_fullframe) FrameLayout mContainerFullframe;
	@BindView(R.id.survey_progress_bar) ProgressBar mSurveyProgressBar;
	@BindView(R.id.list_mask) FrameLayout mListMask;

	@BindString(R.string.continue_button) String CONTINUE_BTN;
	@BindString(R.string.submit_button) String SUBMIT_BTN;

	private List<Survey> mSurvey;
	public ArrayMap<String, Object> mSurveyResults;
	private int mCurrentQuestion = 0;
	private BaseSurveyView mCurrentView;
	private boolean mCanAdvance = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_container);
		ButterKnife.bind(this);

		SystemUtils.hideKeyboardFrom(this, mContainer);

		SharedPreferenceManager sp = SharedPreferenceManager.getInstance(this);

		mSurveyResults = sp.getCompletedQuestionnaire();
		if (mSurveyResults == null) mSurveyResults = new ArrayMap<>();
		mSurvey = sp.getSurvey();
		if (mSurvey == null) finishSurvey();

		mSurveyProgressBar.setMax(mSurvey.size());
		mSurveyProgressBar.setProgress(mCurrentQuestion + 1);

		mContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getQuestionResultAndAdvance();
			}
		});

		mBackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		mProgressBar.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// these two lines are sanity checks for onBackButtonPressed()
		if (mCurrentQuestion >= mSurvey.size()) mCurrentQuestion = mSurvey.size() - 1;
		mProgressBar.setVisibility(View.GONE);

		showQuestion(mCurrentQuestion);
	}

	private void showQuestion(int currentQuestion) {
		if (mSurveyProgressBar != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
				mSurveyProgressBar.setProgress(currentQuestion + 1, true);
			else mSurveyProgressBar.setProgress(currentQuestion + 1);
		}
		mCanAdvance = false;
//		mContainerInline.removeAllViews();
		mContainerFullframe.removeAllViews();
		mCurrentView = SurveyHelper.getSurveyView(this, mSurvey.get(currentQuestion));

		if (mSurveyResults.containsKey(mSurvey.get(currentQuestion).getColName())) {
			mCurrentView.setResult(mSurveyResults.get(mSurvey.get(currentQuestion).getColName()));
		} else if (BuildConfig.DEBUG) { //this is to speed up debug testing
			mCurrentView.setResult(null);
		}

		FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		mListMask.setVisibility(mCurrentView.isFullframe() ? View.INVISIBLE : View.VISIBLE);

		mContainerFullframe.addView(mCurrentView, p);

		mCurrentQuestion = currentQuestion;
		mCurrentView.setAdvanceListener(this);

		mCanAdvance = mCurrentView.canAdvance();
		updateContinueButton();
	}

	private void getQuestionResultAndAdvance() {

		if (mCurrentView != null && mCurrentView.returnsResult()) {
			mSurveyResults.put(mCurrentView.getSurveyQuestionColumnName(), mCurrentView.getSurveyResponse());

			// Occupation survey special case: This dictates what location surveys are shown later
			if (mCurrentView.getSurvey().getId() == SurveyHelper.OCCUPATION) {
				int response = (int) mCurrentView.getSurveyResponse();

				mIsEmployed = false;
				mIsStudent = false;

				switch (response) {
					case 0:
					case 1:
						mIsEmployed = true;
						break;
					case 2:
						mIsStudent = true;
						break;
					case 3:
						if (BuildConfig.FLAVOR.equals("montreal")) break; //this is a hack so the reordered questions work as this option was removed
						mIsEmployed = true;
						mIsStudent = true;
						break;
					default:
						break;
				}
			}
		}

		mCurrentQuestion++;
		if (mCurrentQuestion < mSurvey.size()) {
			//TODO: this has the potential for an infinite loop?
			while (mCurrentQuestion < mSurvey.size()) {
				if (SurveyHelper.shouldShowQuestion(mSurvey.get(mCurrentQuestion), mIsEmployed, mIsStudent)) {
					showQuestion(mCurrentQuestion);
					return;
				}
				mCurrentQuestion++;
			}
		}

		mCurrentQuestion = Math.min(mCurrentQuestion, mSurvey.size());

		finishSurvey();
	}

	private void finishSurvey() {

		mProgressBar.setVisibility(View.VISIBLE);

		Update update = new Update();

		SharedPreferenceManager sp = SharedPreferenceManager.getInstance(this);
		update.withSurvey(mSurveyResults).withUuid(sp.getUUID()).withSurveyId(sp.getSurveyId());

		Triplab triplab = new Triplab();
		triplab.getApi().updateReport(update).enqueue(new Callback<UpdateResponse>() {
			@Override
			public void onResponse(Call<UpdateResponse> call, Response<UpdateResponse> response) {

				if (!response.isSuccessful()) {

					mProgressBar.setVisibility(View.GONE);

					try {
						Logger.l.e("error response code", response.code());
						Gson gson = new Gson();
						if (response.errorBody() != null) {
							//NOTE: for some reason deriving the response and logging it produces a null result later.
							final String responseStream = response.errorBody().string();
							Logger.l.e(responseStream);
							ErrorMessage error = gson.fromJson(responseStream, ErrorMessage.class);
							if (error != null) {
								//TODO: localize and make sure this actually works
								String errorMessage = "Error submitting survey. Please try again";
								if (error.getErrors() != null && error.getErrors().length > 0)
									errorMessage = error.getErrors()[0];
//								else if (error.getMessage() != null) errorMessage = error.getMessage();
								Logger.l.e(errorMessage);
								final String message = errorMessage;
								SurveyActivity.this.runOnUiThread(new Runnable() {
									public void run() {
										Toast.makeText(SurveyActivity.this, message, Toast.LENGTH_LONG).show();
									}
								});
							}
						}

					} catch (IOException e) {
						Logger.l.e(e.toString());
					}
					return;
				}

				SharedPreferenceManager.getInstance(SurveyActivity.this).setQuestionnaireCompleteDateToNow();
				SharedPreferenceManager.getInstance(SurveyActivity.this).setCompletedQuestionnaire(mSurveyResults); //doubt I'll ever come back for this.

				startActivity(new Intent(SurveyActivity.this, OnboardActivity.class));


				// Montreal-specific check for survey response from user
				if (BuildConfig.FLAVOR.equals("montreal")) {
					Object survey = null;
					if (mSurveyResults.containsKey("Newsletter")) {
						survey = mSurveyResults.get("Newsletter");
					} else if (mSurveyResults.containsKey("Infolettre")) {
						survey = mSurveyResults.get("Infolettre");
					}

					if (survey instanceof String) {
						if (((String) survey).contains("Yes") || ((String) survey).contains("Oui")) {
							String url = getString(R.string.tester_site);
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse(url));
							startActivity(i);
						}
					}
				}

				SurveyActivity.this.finish();

			}

			@Override
			public void onFailure(Call<UpdateResponse> call, Throwable t) {
				SurveyActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						mProgressBar.setVisibility(View.GONE);
						Toast.makeText(SurveyActivity.this, R.string.message_unable_to_sync, Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}


	@Override
	public void onBackPressed() {
		mCurrentQuestion--;
		while (mCurrentQuestion >= 0) {
			if (SurveyHelper.shouldShowQuestion(mSurvey.get(mCurrentQuestion), mIsEmployed, mIsStudent)) {
				showQuestion(mCurrentQuestion);
				return;
			}
			mCurrentQuestion--;
		}
		super.onBackPressed();
	}

	private void lockButton() {
		mContinueButton.setEnabled(false);
		mContinueButton.setClickable(false);
		mContinueButton.setAlpha(0.5f);
	}

	private void unlockButton() {
		mContinueButton.setEnabled(true);
		mContinueButton.setClickable(true);
		mContinueButton.setAlpha(1f);
	}

	private void updateContinueButton() {
		mBackButton.setVisibility(mCurrentQuestion == 0 ? View.INVISIBLE : View.VISIBLE);
		mContinueButton.setText(mSurvey.size() - 1 == mCurrentQuestion ? SUBMIT_BTN : CONTINUE_BTN);
//		if (SurveyHelper.isMapView(mSurvey.get(mCurrentQuestion))) {
//
//			mContinueButton.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.base), PorterDuff.Mode.SRC_IN);
//			mContinueButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
//		} else {
//		mContinueButton.getBackground().setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_IN);
//		mContinueButton.setTextColor(ContextCompat.getColor(this, R.color.base));
//		}

		if (mCanAdvance) unlockButton();
		else lockButton();
	}

	@Override
	public void onCanAdvance(boolean canAdvance) {
		mCanAdvance = canAdvance;
		updateContinueButton();
	}
}
