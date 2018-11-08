package ca.itinerum.android.utilities;

import ca.itinerum.android.BuildConfig;

/**
 * Created by stewart on 2018-04-27.
 */

public class PercentObject {

	private int mPercent;
	private String mLabel;
	private int mMaxVal;
	private int mTotal;

	public PercentObject(String label) {
		this(0, label, 100);
	}

	public PercentObject(int percent, String label, int maxVal) {
		mPercent = percent;
		mLabel = label;
		mMaxVal = maxVal;
	}

	public int getPercent() {
		return mPercent;
	}

	public void setPercent(int percent) {
		if (percent > 100 || percent < 0) {
			if (BuildConfig.DEBUG) throw new Error("percent is a number between 0 and 100");
			return;
		}
		mPercent = percent;
	}

	public String getLabel() {
		return mLabel;
	}

	public void setLabel(String label) {
		mLabel = label;
	}

	public void setMaxValue(int maxVal) {
		mMaxVal = maxVal;
	}

	public int getMaxValue() { // exclusive max value
		return 100 - mTotal + getPercent();
	}

	public void setTotal(int total) {
		mTotal = total;
	}

	public int getTotal() {
		return mTotal;
	}

}
