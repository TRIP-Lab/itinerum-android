package ca.itinerum.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import ca.itinerum.android.survey.PreambleActivity;
import ca.itinerum.android.survey.SurveyActivity;
import ca.itinerum.android.survey.TermsOfServiceActivity;
import ca.itinerum.android.utilities.SharedPreferenceManager;

public class MainActivity extends Activity
{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	  }
	
    @Override
    protected void onResume()
    {
        super.onResume();

		if (SharedPreferenceManager.getInstance(this).getTermsOfServiceRequired()) startActivity(new Intent(this, TermsOfServiceActivity.class));

		else if (SharedPreferenceManager.getInstance(this).getSurveyName() == null) startActivity(new Intent(this, PreambleActivity.class));

		else if (!SharedPreferenceManager.getInstance(this).hasCompletedQuestionnaire()) startActivity(new Intent(this, SurveyActivity.class));

        else startActivity(new Intent(this, MapActivity.class));

		finish();

	}

}
