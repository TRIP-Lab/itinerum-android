package ca.itinerum.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import ca.itinerum.android.onboarding.OnboardActivity;
import ca.itinerum.android.preamble.SystemAccessActivity;
import ca.itinerum.android.preamble.WelcomeActivity;
import ca.itinerum.android.survey.SurveyActivity;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.SystemUtils;

public class MainActivity extends Activity
{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	  }
	
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferenceManager sp = SharedPreferenceManager.getInstance(this);

		if (sp.getSurveyName() == null) startActivity(new Intent(this, WelcomeActivity.class));
		else if (!sp.getResearchEthicsAgreement() || !SystemUtils.locationEnabled(this)) startActivity(new Intent(this, SystemAccessActivity.class));
//		else if (sp.getTermsOfServiceRequired()) startActivity(new Intent(this, TermsOfServiceActivity.class));
		else if (!sp.hasCompletedQuestionnaire()) startActivity(new Intent(this, SurveyActivity.class));
		else if (!sp.hasSeenTutorial()) startActivity(new Intent(this, OnboardActivity.class));

        else {
			((DMApplication) getApplication()).startLoggingService();
			startActivity(new Intent(this, MapActivity.class));
		}

		finish();

	}

}
