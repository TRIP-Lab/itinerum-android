package ca.itinerum.android.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;

public class RoundedCheckboxView extends LinearLayout implements Checkable {

	@BindView(R.id.textview) CheckableTextView mTextview;
	@BindView(R.id.checkbox) CheckBox mCheckbox;

	private static final int[] CheckedStateSet = { android.R.attr.state_checked };

	private int mTextColorResource;
	private boolean mIsChecked = false;
	private CharSequence mInitialText = "";

	public RoundedCheckboxView(Context context) {
		this(context, null);
		onFinishInflate();
	}

	public RoundedCheckboxView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundedCheckboxView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_rounded_checkbox, this);

		setBackgroundResource(R.drawable.background_rounded_checkbox);

		final int[] styleable = {android.R.attr.checked, android.R.attr.text, android.R.attr.textColor};
		Arrays.sort(styleable);

		TypedArray a = context.obtainStyledAttributes(attrs, styleable);
		for (int attrIndex = 0; attrIndex < styleable.length; attrIndex++) {
			int attribute = styleable[attrIndex];
			switch (attribute) {
				case android.R.attr.text:
					mInitialText = a.getText(attrIndex);
					break;
				case android.R.attr.textSize:
					mIsChecked = a.getBoolean(attrIndex, false);
					break;
				case android.R.attr.textColor:
					mTextColorResource = a.getResourceId(attrIndex, 0);
					break;
			}
		}
		a.recycle();

		//TODO: xml possible tint?

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);
		setOrientation(HORIZONTAL);
		mTextview.setText(mInitialText);
		if (mTextColorResource != 0) mTextview.setTextColor(mTextColorResource);
		setChecked(mIsChecked);

		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggle();
			}
		});
	}

	/*
	 * Has "selected" notion which also changes font from black -> white and regular -> semibold
	 * */

	public void toggle() {
		setChecked(!mIsChecked);
	}

	public void setChecked(boolean checked) {
		mIsChecked = checked;
		mTextview.setChecked(checked);
		mCheckbox.setChecked(checked);
		refreshDrawableState();

	}

	@Override
	public boolean isChecked() {
		return mIsChecked;
	}

	public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
		mCheckbox.setOnCheckedChangeListener(listener);
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CheckedStateSet);
		}
		return drawableState;
	}

}
