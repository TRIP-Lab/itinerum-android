package ca.itinerum.android.survey.views;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
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

public class TextEntryView extends BaseSurveyView {

	@BindView(R.id.title) AppCompatTextView mTitle;
	@BindView(R.id.question) AppCompatTextView mQuestion;
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

		mTitle.setText(getSurvey().getColName());
		mQuestion.setText(getSurvey().getPrompt());

		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SystemUtils.hideKeyboardFrom(mContext, TextEntryView.this);
			}
		});

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

		if (BuildConfig.DEBUG) mResponse.setText("test");

		mResponse.setInputType(InputType.TYPE_CLASS_TEXT);
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
		return true;
	}

}
