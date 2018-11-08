package ca.itinerum.android.utilities;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;

/**
 * Created by stewart on 2018-04-26.
 */

public class NumberPickerViewGroup extends LinearLayout {

	@BindView(R.id.textview_content) AppCompatTextView mTextviewContent;
	@BindView(R.id.button_decrement) RepeatButton mButtonDecrement;
	@BindView(R.id.button_increment) RepeatButton mButtonIncrement;
	@BindView(R.id.button_container) ViewGroup mButtonContainer;

	@BindDimen(R.dimen.padding_small) int PADDING;


	private long mValue = -1;

	private long mDefaultValue = 0;
	private long mMaxValue = Long.MAX_VALUE;
	private long mMinValue = Long.MIN_VALUE;
	private int mIncrement = 1;
	private int mLongIncrement = 5;

	private String mUnitText = "";

	private onValueChangedListener mListener;
	private boolean mHideUnit;

	public NumberPickerViewGroup(Context context) {
		this(context, null);
		onFinishInflate();
	}

	public NumberPickerViewGroup(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NumberPickerViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_number_picker_viewgroup, this);

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		setOrientation(HORIZONTAL);

		setValue(mDefaultValue, false);

		setPadding(PADDING, PADDING, PADDING, PADDING);

		mButtonDecrement.setOnRepeatListener(new RepeatButton.OnRepeatButtonListener() {
			@Override
			public boolean onClick() {
				return setValue(calculateValue(getValue(), getIncrement(), false), true);
			}

			@Override
			public boolean onButtonRepeat() {
				return setValue(calculateValue(getValue(), getLongIncrement(), false), true);
			}

			@Override
			public void onTouchUp() {
				if (mListener != null) mListener.touchUp();
			}
		});

		mButtonIncrement.setOnRepeatListener(new RepeatButton.OnRepeatButtonListener() {
			@Override
			public boolean onClick() {
				return setValue(calculateValue(getValue(), getIncrement(), true), true);
			}

			@Override
			public boolean onButtonRepeat() {
				return setValue(calculateValue(getValue(), getLongIncrement(), true), true);
			}

			@Override
			public void onTouchUp() {
				if (mListener != null) mListener.touchUp();
			}
		});

	}

	private long calculateValue(long value, long addition, boolean isIncrement) {
		if (isIncrement) {
			if (value + addition < getMaxValue()) return value + addition;
			return getMaxValue();
		} else {
			if (value - addition > getMinValue()) return value - addition;
			return getMinValue();
		}
	}

	public long getValue() {
		return mValue;
	}

	public boolean setValue(long value, boolean shouldNotify) {
		if (value == getValue()) return false;
		if (value > getMaxValue() || value < getMinValue()) return false;
		StringBuffer b = new StringBuffer().append(value);
		if (!mHideUnit) b.append(" ").append(mUnitText);

		mTextviewContent.setText(b.toString());

		mValue = value;

		if (mListener != null && shouldNotify) mListener.valueChanged(getValue());

		return true;
	}

	public long getDefaultValue() {
		return mDefaultValue;
	}

	public void setDefaultValue(long defaultValue) {
		if (getValue() == mDefaultValue) setValue(defaultValue, false);
		mDefaultValue = defaultValue;
	}

	public long getMaxValue() {
		return mMaxValue;
	}

	public void setMaxValue(long maxValue) {
		mMaxValue = maxValue;
	}

	public long getMinValue() {
		return mMinValue;
	}

	public void setMinValue(long minValue) {
		mMinValue = minValue;
	}

	public int getIncrement() {
		return mIncrement;
	}

	public void setIncrement(int increment) {
		mIncrement = increment;
	}

	public int getLongIncrement() {
		return mLongIncrement;
	}

	public void setLongIncrement(int longIncrement) {
		mLongIncrement = longIncrement;
	}

	public void setUnitText(String text) {
		mUnitText = text;
		setValue(mValue, false);
	}

	public void shouldHideUnit(boolean hide) {
		mHideUnit = hide;
	}

	public void shouldHideIncrementalButtons(boolean hide) {
		mButtonContainer.setVisibility(hide ? INVISIBLE : VISIBLE);
	}

	public onValueChangedListener getListener() {
		return mListener;
	}

	public void setListener(onValueChangedListener listener) {
		mListener = listener;
	}

	public interface onValueChangedListener {
		void valueChanged(long value);
		void touchUp();
	}
}
