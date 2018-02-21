package ca.itinerum.android.survey.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.sync.retrofit.Survey;
import ca.itinerum.android.utilities.SystemUtils;

/**
 * Created by stewjacks on 2017-01-19.
 */

public class TextEntryView extends BaseSurveyView {

	@BindView(R.id.title) TextView mTitle;
	@BindView(R.id.question) TextView mQuestion;
	@BindView(R.id.response) EditText mResponse;

	public TextEntryView(Context context, Survey survey) {
		this(context, null, 0);
		mSurvey = survey;
		onFinishInflate();
	}

	public TextEntryView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TextEntryView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_text_entry, this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		mTitle.setVisibility(GONE);
		mQuestion.setText(getSurvey().getPrompt());

		mResponse.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mListener != null) mListener.onCanAdvance(canAdvance());
			}

			@Override
			public void afterTextChanged(Editable s) {}
		});
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
