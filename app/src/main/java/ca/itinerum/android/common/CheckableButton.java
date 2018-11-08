package ca.itinerum.android.common;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.widget.Checkable;

public class CheckableButton extends AppCompatButton implements Checkable {

	private boolean mChecked = false;
	private static final int[] CheckedStateSet = { android.R.attr.state_checked };

	public CheckableButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setChecked(boolean checked) {
		mChecked = checked;
		refreshDrawableState();
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void toggle() {
		setChecked(!isChecked());
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
