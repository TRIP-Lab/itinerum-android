package ca.itinerum.android.survey.views;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputType;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.sync.retrofit.Survey;
import ca.itinerum.android.utilities.SystemUtils;

/**
 * Created by stewjacks on 2017-01-19.
 */

public class NumberTextEntryView extends TextEntryView {

	@BindView(R.id.question) AppCompatTextView mQuestion;
	@BindView(R.id.response) EditText mResponse;

	public NumberTextEntryView(Context context, Survey survey) {
		super(context, survey);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		mResponse.setInputType(InputType.TYPE_CLASS_NUMBER);

		if (BuildConfig.DEBUG) mResponse.setText("5");
	}

	@Override
	public String getSurveyResponse() {
		SystemUtils.hideKeyboardFrom(mContext, this);
		return mResponse.getText().toString();
	}

	@Override
	public void setResult(Object result) {
		if (result instanceof String)
			mResponse.setText((String) result);
	}

	@Override
	public boolean canAdvance() {
		return !StringUtils.isBlank(mResponse.getText().toString());
	}

}
