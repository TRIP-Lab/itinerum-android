package ca.itinerum.android;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.recording.Session;
import ca.itinerum.android.sync.PromptAnswerGroup;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.db.LocationDatabase;

public class PromptDetailsActivity extends AppCompatActivity implements PromptDetailsView.DetailsViewUpdateListener {

	@BindView(R.id.toolbar) Toolbar mToolbar;
	@BindView(R.id.prompt_details_view) PromptDetailsView mPromptDetailsView;
	@BindView(R.id.container) CoordinatorLayout mContainer;

	private boolean mNewPrompt;
	private boolean mLocationSet = true;

	private boolean mEdited = true;
	private boolean mIsEditing = false;
	private boolean mHasIncremented = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prompt_details);
		ButterKnife.bind(this);

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mNewPrompt = getIntent().getBooleanExtra("new_prompt", false);

		if (mNewPrompt) {
			mLocationSet = false;
			mIsEditing = true;
			mEdited =  true;
			getSupportActionBar().setTitle(R.string.title_create_trip);
			//TODO ... need a nicer way of creating a new PromptAnswerGroup
			int promptLength = SharedPreferenceManager.getInstance(this).getPrompts().size();
			LatLng lastPoint = Session.getInstance().getLastValidLocation() == null ? new LatLng(0, 0) : new LatLng(Session.getInstance().getLastValidLocation().getLatitude(), Session.getInstance().getLastValidLocation().getLongitude());
			mPromptDetailsView.setNewPrompt(mNewPrompt);
			mPromptDetailsView.setPrompts(new PromptAnswerGroup(promptLength, lastPoint, new DateTime()));

		} else {
			int position = getIntent().getIntExtra("position", -1);

			if (BuildConfig.DEBUG && position == -1)
				throw new Error("position int required in bundle for editing prompt");

			PromptAnswerGroup data = PromptAnswerGroup
					.sortPrompts(
							LocationDatabase.getInstance(this).promptDao().getAllRegisteredPromptAnswers(),
							SharedPreferenceManager.getInstance(this).getNumberOfPrompts())
					.get(position);

			mPromptDetailsView.setPrompts(data);

			getSupportActionBar().setTitle(mIsEditing ? getString(R.string.title_edit_existing_trip) : getString(R.string.title_existing_trip));

		}

		mPromptDetailsView.setDateTimePickerListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_prompt_details, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		menu.findItem(R.id.edit_item).setIcon(mIsEditing ? R.drawable.ic_save_white_24dp : R.drawable.ic_edit_white_24dp);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				if (mIsEditing) showOnBackWarning();
				else supportFinishAfterTransition();
				return true;
			case R.id.edit_item:
				if (mIsEditing) {
					if (mEdited) {
						// save

						if (mPromptDetailsView.promptsAreValid()) {
							List<PromptAnswer> answers = mPromptDetailsView.getAnswers();
							int numberOfPrompts = SharedPreferenceManager.getInstance(this).getNumberOfRecordedPrompts();
							if (mNewPrompt && !mHasIncremented) {
								if (!mHasIncremented) {
									numberOfPrompts++;
									SharedPreferenceManager.getInstance(this).setNumberOfRecordedPrompts(numberOfPrompts);
									mHasIncremented = true;
								}

								for (PromptAnswer answer: answers) {
									answer.setPromptNumber(numberOfPrompts);
								}
							}

							PromptAnswer[] spread = new PromptAnswer[answers.size()];
							spread = answers.toArray(spread);
							LocationDatabase.getInstance(this).promptDao().insert(spread);

							Snackbar.make(mContainer, R.string.snackbar_saved_trip, Snackbar.LENGTH_SHORT).show();
							item.setIcon(R.drawable.ic_edit_white_24dp);

						}
						else return true;
					}

				} else {
					item.setIcon(R.drawable.ic_save_white_24dp);
				}

				mIsEditing = !mIsEditing;
				mPromptDetailsView.setEditable(mIsEditing);
				if (mNewPrompt) getSupportActionBar().setTitle(getString(R.string.title_create_trip));
				else getSupportActionBar().setTitle(mIsEditing ? getString(R.string.title_edit_existing_trip) : getString(R.string.title_existing_trip));

				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (mIsEditing) showOnBackWarning();
		else super.onBackPressed();
	}

	private void showOnBackWarning() {
		new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_warning_black_36dp)
				.setTitle(R.string.title_warning_generic)
				.setMessage(mNewPrompt ? getString(R.string.message_unsaved_new_trip) : getString(R.string.message_unsaved_existing_trip))
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						PromptDetailsActivity.this.supportFinishAfterTransition();
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	@Override
	public void onDateClicked(DatePickerDialog.OnDateSetListener listener, int year, int monthOfYear, int dayOfMonth) {
		Calendar minDate = new DateTime(SharedPreferenceManager.getInstance(this).getQuestionnaireCompleteDate()).toCalendar(Locale.US);
		Calendar maxDate = DateTime.now().toCalendar(Locale.US);

		DatePickerDialog dialog = DatePickerDialog.newInstance(listener, year, monthOfYear, dayOfMonth);

		dialog.setMinDate(minDate);
		dialog.setMaxDate(maxDate);
		dialog.setAccentColor(getResources().getColor(R.color.base_colour));

		dialog.show(getFragmentManager(), "datepicker_dialog");
	}

	@Override
	public void onTimeClicked(TimePickerDialog.OnTimeSetListener listener, int hourOfDay, int minute, boolean isToday) {

		TimePickerDialog dialog = TimePickerDialog.newInstance(listener, hourOfDay, minute, DateFormat.is24HourFormat(this));

		if (isToday) dialog.setMaxTime(hourOfDay, minute, 59);
		dialog.setAccentColor(getResources().getColor(R.color.base_colour));

		dialog.show(getFragmentManager(), "timepicker_dialog");
	}

	@Override
	public void onSubmit(boolean successful) {

		if (!successful) {
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_warning_black_36dp)
					.setTitle(R.string.title_incomplete_new_trip)
					.setMessage(R.string.message_incomplete_new_trip)
					.setPositiveButton(android.R.string.ok, null)
					.show();
		}
	}

	@Override
	public void onCancel() {
		if (mNewPrompt) {
			showOnBackWarning();
		} else {
			onBackPressed();
		}
	}
}
