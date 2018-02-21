package ca.itinerum.android.survey;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.DMApplication;
import ca.itinerum.android.MapActivity;
import ca.itinerum.android.R;
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

public class SurveyActivity extends Activity implements SurveyAdvanceListener {

	//NOTE: These are annoying requirements b/c we only show the school and work locations maps if the user is a worker and/or student.
	public boolean mIsEmployed;
	public boolean mIsStudent;

	@BindView(R.id.progress_bar) ProgressBar mProgressBar;
	@BindView(R.id.fragment_container) FrameLayout mContainer;
	@BindView(R.id.fragment_back_button) ImageButton mFragmentBackButton;
	@BindView(R.id.fragment_continue_button) Button mFragmentContinueButton;

	@BindColor(R.color.base_colour) int CONTINUE_BUTTON_COLOUR;
	@BindColor(android.R.color.darker_gray) int DISABLE_BUTTON_COLOUR;

	@BindString(R.string.continue_button) String CONTINUE_BTN;
	@BindString(R.string.submit_button) String SUBMIT_BTN;

	@BindDimen(R.dimen.padding_medium) int PADDING;

	private List<Survey> mSurvey;
	private ArrayMap<String, Object> mSurveyResults;
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

		mFragmentContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getQuestionResultAndAdvance();
			}
		});

		mFragmentBackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		showQuestion(mCurrentQuestion);
	}

	private void showQuestion(int currentQuestion) {
		mCanAdvance = false;
		mContainer.removeAllViews();
		mCurrentView = SurveyHelper.getSurveyView(this, mSurvey.get(currentQuestion));

		if (mSurveyResults.containsKey(mSurvey.get(currentQuestion).getColName())) {
			mCurrentView.setResult(mSurveyResults.get(mSurvey.get(currentQuestion).getColName()));
		}

		FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		mContainer.addView(mCurrentView, p);
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
						mIsEmployed = true;
						mIsStudent = true;
						break;
					default:
						break;
				}
			}
		}

		mCurrentQuestion++;
		while (mCurrentQuestion < mSurvey.size()) {
			if (SurveyHelper.shouldShowQuestion(mSurvey.get(mCurrentQuestion), mIsEmployed, mIsStudent)) {
				showQuestion(mCurrentQuestion);
				return;
			}
			mCurrentQuestion = Math.min(mCurrentQuestion + 1, mSurvey.size() - 1);
		}

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
								if (error.getError() != null) errorMessage = error.getError();
								else if (error.getMessage() != null) errorMessage = error.getMessage();
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

				((DMApplication) getApplication()).startLoggingService();

				startActivity(new Intent(SurveyActivity.this, MapActivity.class));
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
		mFragmentContinueButton.setBackgroundColor(DISABLE_BUTTON_COLOUR);
		mFragmentContinueButton.setEnabled(false);
		mFragmentContinueButton.setClickable(false);
		mFragmentContinueButton.setAlpha(0.5f);
	}

	private void unlockButton() {
		mFragmentContinueButton.setEnabled(true);
		mFragmentContinueButton.setClickable(true);
		mFragmentContinueButton.setBackgroundColor(CONTINUE_BUTTON_COLOUR);
		mFragmentContinueButton.setAlpha(1f);
	}

	private void updateContinueButton() {
		mFragmentBackButton.setVisibility(mCurrentQuestion == 0 ? View.GONE : View.VISIBLE);
		mFragmentContinueButton.setText(mSurvey.size() - 1 == mCurrentQuestion ? SUBMIT_BTN : CONTINUE_BTN);

		if (mCanAdvance) unlockButton();
		else lockButton();
	}

	@Override
	public void onCanAdvance(boolean canAdvance) {
		mCanAdvance = canAdvance;
		updateContinueButton();
	}
}
