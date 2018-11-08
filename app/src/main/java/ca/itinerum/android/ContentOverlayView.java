package ca.itinerum.android;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.joda.time.DateTime;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.common.CheckableButton;
import ca.itinerum.android.common.ContentOverlayCardView;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.SharedPreferenceManager;

public class ContentOverlayView extends LinearLayout implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
	@BindView(R.id.validated_trips_card) ContentOverlayCardView mValidatedTripsCard;
	@BindView(R.id.days_card) ContentOverlayCardView mDaysCard;
	@BindView(R.id.past_trips_card) LinearLayout mPastTripsCard;
	@BindView(R.id.past_trips_card_title) AppCompatTextView mPastTripsCardTitle;
	@BindView(R.id.past_trips_card_button_today) CheckableButton mPastTripsCardButtonToday;
	@BindView(R.id.past_trips_card_button_yesterday) CheckableButton mPastTripsCardButtonYesterday;
	@BindView(R.id.past_trips_card_button_custom) CheckableButton mPastTripsCardButtonCustom;
	@BindView(R.id.past_trips_card_button_all) CheckableButton mPastTripsCardButtonAll;
	@BindView(R.id.drawer_image) AppCompatImageView mDrawerImage;

	private OnDateButtonListener mListener;
	private CheckableButton[] mButtons;
	private PromptDetailsView.DetailsViewUpdateListener mDetaisUpdateListener;

	@BindString(R.string.start_date_title) String START_DATE_TITLE;
	@BindString(R.string.end_date_title) String END_DATE_TITLE;
	@BindString(R.string.start_time_title) String START_TIME_TITLE;
	@BindString(R.string.end_time_title) String END_TIME_TITLE;

	private DateTimeMode mStartDateSet = DateTimeMode.START_DATE;

	private enum DateTimeMode {
			START_DATE, END_DATE, START_TIME, END_TIME
	}

	public ContentOverlayView(Context context) {
		this(context, null);
		onFinishInflate();
	}

	public ContentOverlayView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ContentOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_content_overlay, this);
		setOrientation(VERTICAL);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		// we're starting expanded as a design decision
		setDrawerImageProgress(0f);

		mButtons = new CheckableButton[]{mPastTripsCardButtonToday, mPastTripsCardButtonYesterday, mPastTripsCardButtonCustom, mPastTripsCardButtonAll};

		setSelectedDateState(SharedPreferenceManager.getInstance(getContext()).getDateState());

		mPastTripsCardButtonToday.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) mListener.onDateButtonClicked(DateState.TODAY);
			}
		});

		mPastTripsCardButtonYesterday.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) mListener.onDateButtonClicked(DateState.YESTERDAY);
			}
		});

		mPastTripsCardButtonAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) mListener.onDateButtonClicked(DateState.ALL_TIME);
			}
		});

		mPastTripsCardButtonCustom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDetaisUpdateListener == null) return;
				mStartDateSet = DateTimeMode.START_DATE;
				DateTime dt = DateTime.now();
				mDetaisUpdateListener.onDateClicked(ContentOverlayView.this,
						dt.getYear(),
						dt.getMonthOfYear(),
						dt.getDayOfMonth(),
						null,
						START_DATE_TITLE);

//				if (mListener != null) mListener.onDateButtonClicked(DateState.CUSTOM);
			}
		});
	}

	public void setSelectedDateState(DateState dateState) {

		for (CheckableButton button : mButtons) {
			button.setChecked(false);
		}

		switch (dateState) {
			case TODAY:
				mPastTripsCardButtonToday.setChecked(true);
				break;
			case YESTERDAY:
				mPastTripsCardButtonYesterday.setChecked(true);
				break;
			case CUSTOM:
				mPastTripsCardButtonCustom.setChecked(true);
				break;
			case ALL_TIME:
				mPastTripsCardButtonAll.setChecked(true);
				break;
			default:
				break;

		}
	}

	public void setDrawerImageProgress(float progress) {

		//progress: 0 = 90deg, 1 = 270deg
		progress = Math.max(Math.min(progress, 1f), 0f);
		mDrawerImage.setRotation(90f + 180f*progress);

	}

	public void setDetailsViewUpdatePickerListener(PromptDetailsView.DetailsViewUpdateListener listener) {
		mDetaisUpdateListener = listener;
	}

	public void setDateSetListener(OnDateButtonListener listener) {
		mListener = listener;
	}

	public void setValidatedTrips(int value) {
		mValidatedTripsCard.setLargeContentValue(value);
	}

	public void setTotalValidatedTrips(int value) {
		mValidatedTripsCard.setTotals(value);
	}

	public void setCurrentDay(int value) {
		mDaysCard.setLargeContentValue(value);
	}

	public void setTotalDays(String value) {
		mDaysCard.setTotals(value);
	}

	public void setTotalDays(int value) {
		mDaysCard.setTotals(value);
	}

	public void setDone() {
		mDaysCard.setDone();
	}

	public AppCompatImageView getDrawerImage() {
		return mDrawerImage;
	}

	@Override
	public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
		DateTime dateTime = DateTime.now().withYear(year).withMonthOfYear(monthOfYear + 1).withDayOfMonth(dayOfMonth); // dialogfragment returns months starting at 0 instead of 1
		switch(mStartDateSet) {
			case START_DATE:
				mStartDateSet = DateTimeMode.START_TIME;
				SharedPreferenceManager.getInstance(getContext()).setFromDate(dateTime.withMillisOfDay(0).toDate());

				mDetaisUpdateListener.onTimeClicked(ContentOverlayView.this,
						DateTime.now().getHourOfDay(),
						DateTime.now().getMinuteOfHour(),
						(dateTime.getDayOfYear() == DateTime.now().getDayOfYear() && dateTime.getYear() == DateTime.now().getYear()),
						START_TIME_TITLE);
				break;

			case END_DATE:
				mStartDateSet = DateTimeMode.END_TIME;
				SharedPreferenceManager.getInstance(getContext()).setToDate(dateTime.withMillisOfDay(1000*60*60*24 - 1).toDate());
				mDetaisUpdateListener.onTimeClicked(ContentOverlayView.this,
						DateTime.now().getHourOfDay(),
						DateTime.now().getMinuteOfHour(),
						(dateTime.getDayOfYear() == DateTime.now().getDayOfYear() && dateTime.getYear() == DateTime.now().getYear()),
						END_TIME_TITLE);
				break;
		}

	}

	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
		DateTime dateTime;
		switch(mStartDateSet) {
			case START_TIME:
				mStartDateSet = DateTimeMode.END_DATE;

				 dateTime = new DateTime(SharedPreferenceManager.getInstance(getContext()).getFromDate()).withHourOfDay(hourOfDay).withMinuteOfHour(minute);

				SharedPreferenceManager.getInstance(getContext()).setFromDate(dateTime.toDate());

				mDetaisUpdateListener.onDateClicked(ContentOverlayView.this,
						DateTime.now().getYear(),
						DateTime.now().getMonthOfYear(),
						DateTime.now().getDayOfMonth(),
						dateTime,
						END_DATE_TITLE);
				break;

			case END_TIME:
				dateTime = new DateTime(SharedPreferenceManager.getInstance(getContext()).getToDate()).withHourOfDay(hourOfDay).withMinuteOfHour(minute);
				SharedPreferenceManager.getInstance(getContext()).setToDate(dateTime.toDate());
				if (mListener != null) mListener.onDateButtonClicked(DateState.CUSTOM);



		}
	}

	public enum DateState {
		TODAY(R.string.datetime_enum_today),
		YESTERDAY(R.string.datetime_enum_yesterday),
		LAST_SEVEN(R.string.datetime_enum_last_seven),
		ALL_TIME(R.string.datetime_enum_all),
		CUSTOM(R.string.datetime_enum_custom);

		private int mResId = -1;

		DateState(int res) {
			mResId = res;
		}

		public String getLabel(Context context) {
			Resources res = context.getResources();
			if (mResId > 0) {
				return (res.getString(mResId));
			}
			return (name());
		}

		public static String[] values(Context context) {
			String[] values = new String[DateState.values().length];
			for (int i = 0; i < DateState.values().length; i++) {
				values[i] = DateState.values()[i].getLabel(context);
			}

			return values;
		}
	}

	public interface OnDateButtonListener {
		void onDateButtonClicked(DateState dateState);
	}
}
