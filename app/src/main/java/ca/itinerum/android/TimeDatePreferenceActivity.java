package ca.itinerum.android;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.utilities.SharedPreferenceManager;

public class TimeDatePreferenceActivity extends AppCompatActivity {

	@BindView(R.id.list_quick_dates) ListView mListQuickDates;
	@BindView(R.id.start_date) TextView mStartDateField;
	@BindView(R.id.start_time) TextView mStartTimeField;
	@BindView(R.id.end_date) TextView mEndDateField;
	@BindView(R.id.end_time) TextView mEndTimeField;
	@BindView(R.id.date_container) LinearLayout mDateContainer;
	@BindView(R.id.toolbar) Toolbar mToolbar;

	private ArrayAdapter mListQuickDatesAdapter;

	private Calendar mEndCalendar, mStartCalendar;
	private SharedPreferenceManager mSharedPrefs;
	private DateState mCurrentState;
	private TimePickerDialog mTimeDialog;
	private DatePickerDialog mDateDialog;


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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_time_date_preference);
		ButterKnife.bind(this);

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mSharedPrefs = SharedPreferenceManager.getInstance(this);

		mCurrentState = mSharedPrefs.getDateState();

		mSharedPrefs.resetDatesToDefault();

		updateDatesFromEnum();
		updateDateAndTimeUI();

		mStartDateField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDatePickerDialog(true);
			}
		});

		mStartTimeField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePickerDialog(true);
			}
		});

		mEndDateField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDatePickerDialog(false);
			}
		});

		mEndTimeField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePickerDialog(false);
			}
		});
		mListQuickDatesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, DateState.values(this));
		mListQuickDates.setAdapter(mListQuickDatesAdapter);
		mListQuickDates.setItemChecked(mCurrentState.ordinal(), true);
		mListQuickDates.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mCurrentState = DateState.values()[position];
				mSharedPrefs.setDateState(mCurrentState);
				updateDatesFromEnum();
				setResult(RESULT_OK);
			}
		});

		mListQuickDatesAdapter.notifyDataSetChanged();
	}

	private void showDatePickerDialog(final boolean start) {

		final Calendar calendar = Calendar.getInstance();

		if (start) calendar.setTime(mSharedPrefs.getFromDate());
		else calendar.setTime(mSharedPrefs.getToDate());

		if (mTimeDialog != null) mTimeDialog.hide();
		if (mDateDialog != null) mDateDialog.hide();

		mDateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				calendar.set(Calendar.YEAR, year);
				calendar.set(Calendar.MONTH, monthOfYear);
				calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				if (start) mSharedPrefs.setFromDate(calendar.getTime());
				else mSharedPrefs.setToDate(calendar.getTime());

				if (mSharedPrefs.getFromDate().after(mSharedPrefs.getToDate())) {
					mSharedPrefs.setToDate(mSharedPrefs.getFromDate());
				}

				updateDateAndTimeUI();
			}
		}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

		mDateDialog.show();
	}

	private void showTimePickerDialog(final boolean start) {

		final Calendar calendar = Calendar.getInstance();

		if (start) calendar.setTime(mSharedPrefs.getFromDate());
		else calendar.setTime(mSharedPrefs.getToDate());

		if (mTimeDialog != null) mTimeDialog.hide();

		mTimeDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				calendar.set(Calendar.MINUTE, minute);
				if (start) mSharedPrefs.setFromDate(calendar.getTime());
				else mSharedPrefs.setToDate(calendar.getTime());

				if (mSharedPrefs.getFromDate().after(mSharedPrefs.getToDate())) {
					mSharedPrefs.setToDate(mSharedPrefs.getFromDate());
				}

				updateDateAndTimeUI();
			}
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(this));

		mTimeDialog.show();
	}

	private void updateDatesFromEnum() {
		Calendar c = Calendar.getInstance();
		mSharedPrefs.resetDatesToDefault();
		mSharedPrefs.setDateState(mCurrentState);

		switch (mCurrentState) {
			case TODAY:
				break;
			case YESTERDAY:
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
				c.add(Calendar.MILLISECOND, -1);
				mSharedPrefs.setToDate(c.getTime());

				c.add(Calendar.MILLISECOND, 1);
				c.add(Calendar.DATE, -1);
				mSharedPrefs.setFromDate(c.getTime());
				break;
			case LAST_SEVEN:
				c.add(Calendar.DATE, -6);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);

				mSharedPrefs.setFromDate(c.getTime());

				break;
			case ALL_TIME:
				c.setTimeInMillis(0);
				mSharedPrefs.setFromDate(c.getTime());

				break;
			case CUSTOM:
				break;
		}

		updateDateAndTimeUI();
	}

	private void updateDateAndTimeUI() {

		boolean isCustom = mCurrentState == DateState.CUSTOM;
		mDateContainer.setVisibility(isCustom ? View.VISIBLE : View.GONE);

		mStartCalendar = Calendar.getInstance();
		mStartCalendar.setTime(mSharedPrefs.getFromDate());
		mEndCalendar = Calendar.getInstance();
		mEndCalendar.setTime(mSharedPrefs.getToDate());

		mStartDateField.setText(DateUtils.formatDateTime(this, mStartCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR));
		mStartTimeField.setText(DateUtils.formatDateTime(this, mStartCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
		mEndDateField.setText(DateUtils.formatDateTime(this, mEndCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR));
		mEndTimeField.setText(DateUtils.formatDateTime(this, mEndCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onBackPressed() {
		setResult(RESULT_OK);
		finish();
	}
}
