package ca.itinerum.android;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.common.AboutCard;
import ca.itinerum.android.utilities.SharedPreferenceManager;

public class AboutView extends ScrollView {
	@BindDimen(R.dimen.padding_large) int PADDING;
	@BindDimen(R.dimen.one_dp) int ONE_DP;
	@BindColor(R.color.divider) int DIVIDER_COLOUR;
	private LinearLayout mLinearLayout;
	private int mBottomPadding;

	public AboutView(Context context) {
		this(context, null);
		onFinishInflate();
	}

	public AboutView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AboutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setClickable(true);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		mLinearLayout = new LinearLayout(getContext());
		mLinearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mLinearLayout.setOrientation(LinearLayout.VERTICAL);
		addView(mLinearLayout);

		AppCompatTextView title = new AppCompatTextView(getContext());
		title.setTextAppearance(getContext(), R.style.Title_Card);
		title.setBackgroundResource(R.color.card_title_background);
		title.setPadding(PADDING, PADDING, PADDING, PADDING);
		title.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		title.setText(R.string.about);
		mLinearLayout.addView(title);

		SharedPreferenceManager sp = SharedPreferenceManager.getInstance(getContext());
		if (!StringUtils.isBlank(sp.getAboutText())) {
			addCard(R.string.about_survey_title, sp.getAboutText());
		}

		addCard(R.string.consent_title, R.string.ethics_body);

		if (!StringUtils.isBlank(sp.getSurveyResponseObject().getTermsOfService())) {
			addCard(R.string.survey_consent_title, sp.getSurveyResponseObject().getTermsOfService());
		}

		addCard(R.string.about_app_title, R.string.about_details);

		addOrUpdateBottomPadding(mBottomPadding);

	}

	public void setBottomPadding(int padding) {
		mBottomPadding = padding;
		addOrUpdateBottomPadding(padding);
	}

	private void addOrUpdateBottomPadding(int bottomPadding) {
		if (mLinearLayout != null) {
			View v = mLinearLayout.findViewWithTag("padding");
			if (v == null) {
				v = new View(getContext());
				v.setTag("padding");
				mLinearLayout.addView(v);
			}

			v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, bottomPadding));
		}
	}

	private void addCard(int titleRes, int paragraphRes) {
		addCard(getContext().getString(titleRes), getContext().getString(paragraphRes));
	}

	private void addCard(int titleRes, String paragraph) {
		addCard(getContext().getString(titleRes), paragraph);
	}

	private void addCard(String title, String paragraph) {
		AboutCard aboutCard = new AboutCard(getContext());
		aboutCard.setTitleText(title);
		aboutCard.setParagraphText(paragraph);
		mLinearLayout.addView(aboutCard);
	}
}
