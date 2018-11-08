package ca.itinerum.android.survey.views;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.sync.retrofit.Survey;
import ca.itinerum.android.utilities.NumberPickerViewGroup;
import ca.itinerum.android.utilities.SystemUtils;

/**
 * Created by stewart on 2018-05-01.
 */

public class NumberPickerEntryView extends BaseSurveyView {

	@BindView(R.id.title) AppCompatTextView mTitle;
	@BindView(R.id.question) AppCompatTextView mQuestion;
	@BindView(R.id.number_picker) NumberPickerViewGroup mNumberPicker;

	public NumberPickerEntryView(Context context, Survey survey) {
		this(context, null, 0);
		mSurvey = survey;
		onFinishInflate();
	}

	public NumberPickerEntryView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NumberPickerEntryView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_number_picker_entry, this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		mTitle.setText(getSurvey().getColName());
		mQuestion.setText(getSurvey().getPrompt());

		mNumberPicker.setMinValue(0);
		mNumberPicker.setIncrement(1);
		mNumberPicker.setLongIncrement(1);

	}

	public void configureNumberPicker(int minValue, int maxValue, int defaultValue, int clickSize, int longClickSize, boolean hideUnit) {
		setMinValue(minValue);
		setMaxValue(maxValue);
		setDefaultValue(defaultValue);
		setIncrementValues(clickSize, longClickSize);
		shouldHideUnit(hideUnit);
	}

	public void setUnits(String units) {
		mNumberPicker.setUnitText(units);
	}

	public void setMinValue(int minValue) {
		mNumberPicker.setMinValue(minValue);
	}

	public void setMaxValue(int maxValue) {
		mNumberPicker.setMaxValue(maxValue);
	}

	public void setDefaultValue(int defaultValue) {
		mNumberPicker.setDefaultValue(defaultValue);
	}

	public void setIncrementValues(int click, int longClick) {
		mNumberPicker.setIncrement(click);
		mNumberPicker.setLongIncrement(longClick);
	}

	public void shouldHideUnit(boolean hide) {
		mNumberPicker.shouldHideUnit(hide);
	}

	@Override
	public String getSurveyResponse() {
		SystemUtils.hideKeyboardFrom(mContext, this);
		return String.valueOf(mNumberPicker.getValue());
	}

	@Override
	public void setResult(Object result) {
		if (result instanceof String)
			  mNumberPicker.setValue(Long.valueOf((String) result), false);
	}

	@Override
	public boolean canAdvance() {
		return (mNumberPicker.getValue() >= mNumberPicker.getMinValue() && mNumberPicker.getValue() <= mNumberPicker.getMaxValue()) ;
	}


}
