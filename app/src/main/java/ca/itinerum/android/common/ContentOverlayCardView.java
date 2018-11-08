package ca.itinerum.android.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.Arrays;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;

public class ContentOverlayCardView extends LinearLayout {

	@BindView(R.id.content_card_title) AppCompatTextView mContentCardTitle;
	@BindView(R.id.content_card_content) AppCompatTextView mContentCardContent;
	@BindView(R.id.content_card_total) AppCompatTextView mContentCardTotal;

	@BindString(R.string.content_totals) String TOTALS;
	@BindString(R.string.content_totals_fixed) String TOTALS_FIXED;

	private CharSequence mTitleText;

	public ContentOverlayCardView(Context context) {
		this(context, null);
		onFinishInflate();
	}

	public ContentOverlayCardView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ContentOverlayCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_content_overlay_card, this);
		setOrientation(VERTICAL);

		final int[] styleable = {android.R.attr.text, R.attr.totals};
		Arrays.sort(styleable);

		TypedArray a = context.obtainStyledAttributes(attrs, styleable);
		for (int attrIndex = 0; attrIndex < styleable.length; attrIndex++) {
			int attribute = styleable[attrIndex];
			switch (attribute) {
				case android.R.attr.text:
					mTitleText = a.getText(attrIndex);
					break;
			}
		}
		a.recycle();


	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);
		if (mTitleText != null) mContentCardTitle.setText(mTitleText);
	}

	public void setTitleText(String text) {
		mContentCardTitle.setText(text);
	}

	public void setLargeContentValue(int value) {
		mContentCardContent.setText("" + value);
	}

	public void setTotals(int totals) {
		mContentCardTotal.setText(String.format(TOTALS, totals));
		if (totals == -1) mContentCardTotal.setVisibility(INVISIBLE);
	}

	public void setTotals(String totals) {
		mContentCardTotal.setText(String.format(TOTALS_FIXED, totals));
		mContentCardTotal.setVisibility(VISIBLE);
	}

	public void setDone() {
		mContentCardContent.setText(getResources().getString(R.string.days_remaining_done));
		mContentCardTotal.setVisibility(GONE);
	}
}
