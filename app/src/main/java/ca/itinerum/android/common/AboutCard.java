package ca.itinerum.android.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;

public class AboutCard extends LinearLayout {

	private boolean mSelected;
	private String mParagraphText;
	private String mTitleText;


	@BindView(R.id.container) LinearLayout mContainer;
	@BindView(R.id.title) AppCompatTextView mTitle;
	@BindView(R.id.checkbox) AppCompatImageView mCheckbox;
	@BindView(R.id.paragraph) AppCompatTextView mParagraph;

	public AboutCard(Context context) {
		this(context, null);
		onFinishInflate();
	}

	public AboutCard(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AboutCard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_about_card, this);
		setOrientation(VERTICAL);

		final int[] styleable = {R.attr.title, android.R.attr.text, R.attr.selected};
		Arrays.sort(styleable);

		TypedArray a = context.obtainStyledAttributes(attrs, styleable);
		for (int attrIndex = 0; attrIndex < styleable.length; attrIndex++) {
			int attribute = styleable[attrIndex];
			switch (attribute) {
				case R.attr.title:
					mTitleText = a.getString(attrIndex);
					break;

				case android.R.attr.text:
					mParagraphText = a.getString(attrIndex);
					break;

				case R.attr.selected:
					mSelected = a.getBoolean(attrIndex, false);
					break;
			}
		}
		a.recycle();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		mTitle.setText(mTitleText);
		mParagraph.setText(mParagraphText);
		mParagraph.setScaleY(0);
		mParagraph.setVisibility(GONE);

		mContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggle();
			}
		});
	}

	private void toggle() {
		mSelected = !mSelected;

		mParagraph.setVisibility(VISIBLE);
		mParagraph.animate()
				.withEndAction(new Runnable() {
					@Override
					public void run() {
						if (!mSelected) mParagraph.setVisibility(GONE);
					}
				})
				.alpha(mSelected ? 1f : 0f)
				.scaleY(mSelected ? 1f : 0f)
				.start();

		mCheckbox.animate().rotation(mSelected ? 90 : 270).start();
	}

	public void setParagraphText(String paragraphText) {
		mParagraphText = paragraphText;
		mParagraph.setText(paragraphText);
	}

	public void setTitleText(CharSequence titleText) {
		mTitle.setText(titleText);
	}
}
