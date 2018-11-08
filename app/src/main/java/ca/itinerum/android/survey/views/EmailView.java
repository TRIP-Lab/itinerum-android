package ca.itinerum.android.survey.views;

import android.content.Context;
import android.support.v4.util.PatternsCompat;
import android.text.InputType;

import org.apache.commons.lang3.StringUtils;

import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.sync.retrofit.Survey;

/**
 * Created by stewjacks on 2017-01-19.
 */

public class EmailView extends TextEntryView {

	public EmailView(Context context, Survey survey) {
		super(context, survey);
		init();
	}

	private void init() {
		mTitle.setText(R.string.question_email_title);
		mQuestion.setText(R.string.question_email_subtitle);
		mResponse.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

		if (BuildConfig.DEBUG) //noinspection HardCodedStringLiteral
			mResponse.setText("test@test.com");
	}

	private boolean isValidEmail(CharSequence text) {
//		if (!BuildConfig.EMAIL_MANDATORY) return true;
		return StringUtils.isNotEmpty(text) && PatternsCompat.EMAIL_ADDRESS.matcher(text).matches();
	}

	@Override
	public boolean canAdvance() {
		return super.canAdvance() && isValidEmail(mResponse.getText().toString());
	}
}
