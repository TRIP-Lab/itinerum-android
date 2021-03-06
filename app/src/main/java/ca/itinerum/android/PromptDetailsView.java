package ca.itinerum.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;
import ca.itinerum.android.sync.PromptAnswerGroup;
import ca.itinerum.android.sync.retrofit.Prompt;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.DateUtils;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.SystemUtils;

public class PromptDetailsView extends ScrollView implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, OnMapReadyCallback {

	@BindView(R.id.container) LinearLayout mContainer;
	@BindView(R.id.crosshair_mapview) CrosshairMapView mCrosshairMapView;
	@BindView(R.id.map_container) FrameLayout mMapContainer;
	@BindView(R.id.prompts_container) LinearLayout mPromptsContainer;
	@BindView(R.id.button_date) AppCompatButton mButtonDate;
	@BindView(R.id.button_time) AppCompatButton mButtonTime;
	@BindView(R.id.button_submit) AppCompatButton mButtonSubmit;
	@BindView(R.id.button_cancel) AppCompatButton mButtonCancel;
	@BindView(R.id.map_masking_view) FrameLayout mMapMaskingView;
	@BindView(R.id.button_map_cancel) AppCompatButton mButtonMapCancel;
	@BindView(R.id.button_map_save) AppCompatButton mButtonMapSave;
	@BindView(R.id.textview_map_instruction) AppCompatTextView mTextviewMapInstruction;
	@BindView(R.id.textview_date) AppCompatTextView mTextviewDate;
	@BindView(R.id.textview_time) AppCompatTextView mTextviewTime;
	@BindView(R.id.masking_view) View mMaskingView;

	@BindDimen(R.dimen.default_prompt_details_map_height) int DEFAULT_MAP_HEIGHT;
	@BindDimen(R.dimen.padding_large) int PADDING;
	@BindDimen(R.dimen.shadow_size) int SHADOW_HEIGHT;

	private DetailsViewUpdateListener mListener;
	private LatLng mLocation = new LatLng(0, 0);
	private List<PromptDialogSelectableRecyclerView> mGeneratedPromptLists = new ArrayList<>();
	private PromptAnswerGroup mPromptAnswers;
	private GoogleMap mMap;
	private boolean mScrollable = true;

	private int mPeekTopPadding;

	private boolean mNewEntry;

	private DateTime mRecordedDate;
	private List<Prompt> mPrompts;
	private int mMapScroll = 0;

	private boolean mEditable = false;

	public PromptDetailsView(Context context) {
		this(context, null);
		onFinishInflate();
	}

	public PromptDetailsView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PromptDetailsView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_prompt_details, this);

		mScrollable = attrs.getAttributeBooleanValue(R.styleable.PromptDetailsView_scrollable, true);
		mPeekTopPadding = attrs.getAttributeIntValue(R.styleable.PromptDetailsView_peek_top_padding, 0);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		setFillViewport(true);

		setEditable(mEditable);

		mCrosshairMapView.setMapGesturesEnabled(false);
		mCrosshairMapView.setCrosshairVisible(false);
		mCrosshairMapView.setMapReadyCallback(this);

		mButtonDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mEditable) return;
				if (mListener != null) mListener.onDateClicked(PromptDetailsView.this,
						mRecordedDate.getYear(),
						mRecordedDate.getMonthOfYear() - 1,
						mRecordedDate.getDayOfMonth(),
						null, null);
			}
		});

		mButtonTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mEditable) return;
				boolean isToday = (mRecordedDate.dayOfYear().equals(DateTime.now().dayOfYear()) && mRecordedDate.year().equals(DateTime.now().year()));

				if (mListener != null) mListener.onTimeClicked(PromptDetailsView.this,
						mRecordedDate.getHourOfDay(),
						mRecordedDate.getMinuteOfHour(),
						isToday, null);
			}
		});

		mButtonMapCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				minimizeMap();

			}
		});

		mButtonMapSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				mLocation = mCrosshairMapView.getMapCentrePoint();

				mMap.clear();

				Bitmap img1 = SystemUtils.ColourBitmap(getContext(), R.drawable.marker, R.color.mdtp_accent_color);
				BitmapDescriptor bitmapDescriptor1 = BitmapDescriptorFactory.fromBitmap(img1);

				mMap.addMarker(new MarkerOptions()
						.position(mLocation)
						.icon(bitmapDescriptor1)
						.anchor(0.5f, 0.5f));


				minimizeMap();
			}
		});

		mButtonSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				getAnswers();
			}
		});

		mButtonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mListener != null) {
					mListener.onCancel();
				}
			}
		});

		mMapMaskingView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mCrosshairMapView.setMapGesturesEnabled(true);
				mCrosshairMapView.setCrosshairVisible(true);
				mMapMaskingView.setVisibility(GONE);
				mScrollable = false;

				mButtonMapCancel.setVisibility(VISIBLE);
				mButtonMapSave.setVisibility(VISIBLE);

				mMapContainer.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, PromptDetailsView.this.getHeight()));
				mMapContainer.addOnLayoutChangeListener(new OnLayoutChangeListener() {
					@Override
					public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
						mMapScroll = getScrollY();
						scrollTo(0, mMapContainer.getTop());
						mMapContainer.removeOnLayoutChangeListener(this);
					}
				});

			}
		});

		mMaskingView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			}
		});
	}

	public void minimizeMap() {
		mCrosshairMapView.setMapGesturesEnabled(false);
		mCrosshairMapView.setCrosshairVisible(false);
		mScrollable = true;
		mMapMaskingView.setVisibility(VISIBLE);

		mButtonMapCancel.setVisibility(GONE);
		mButtonMapSave.setVisibility(GONE);

		mMapContainer.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, DEFAULT_MAP_HEIGHT));

		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 15f));

		scrollTo(0, mMapScroll);
	}

	public boolean isInMapMode() {
		return mMapMaskingView.getVisibility() != VISIBLE;
	}

	protected List<PromptAnswer> getAnswers() {
		int i = 0;

		List<PromptAnswer> answers = new ArrayList<>();

		String timestamp = DateUtils.getCurrentFormattedTime();
		for (PromptDialogSelectableRecyclerView promptList : mGeneratedPromptLists) {

			// invalid response -> trigger alert

			PromptAnswer promptAnswer = new PromptAnswer()
					.withAnswer(promptList.getPromptAnswer().getAnswer())
					.withPrompt(mPrompts.get(i).getPrompt())
					.withLatitude(mLocation.latitude)
					.withLongitude(mLocation.longitude)
					.withUploaded(false)
					.withUuid(mPromptAnswers.getUUID())
					.withRecordedAt(mRecordedDate.toString(DateUtils.PATTERN))
					.withPromptNumber(i);

			if (mNewEntry) {
				promptAnswer.setDisplayedAt(timestamp);
				promptAnswer.setUserDefined(true);
			} else {
				promptAnswer.setId(mPromptAnswers.getPromptAnswers().get(i).getId());
				promptAnswer.setDisplayedAt(mPromptAnswers.getPromptAnswers().get(i).getDisplayedAt());
			}

			i++;

			answers.add(promptAnswer);
		}

		if (mListener != null) mListener.onSubmit(true);

		return answers;
	}

	public void setEditable(boolean editable) {
		mEditable = editable;

		mButtonDate.setVisibility(editable ? VISIBLE : INVISIBLE);
		mButtonTime.setVisibility(editable ? VISIBLE : INVISIBLE);
		mMaskingView.setVisibility(editable ? GONE : VISIBLE);
		mTextviewMapInstruction.setVisibility(editable ? VISIBLE : GONE);

	}

	boolean promptsAreValid() {

		boolean isValid = true;

		if (mLocation.equals(new LatLng(0, 0))) {
			scrollTo(0, mMapContainer.getTop());
			Toast.makeText(getContext(), R.string.toast_no_location_set, Toast.LENGTH_SHORT).show();
			isValid = false;
		}

		int[] firstInvalidLocation = {};

		for (PromptDialogSelectableRecyclerView promptList : mGeneratedPromptLists) {
			if (promptList.getPromptAnswer() == null || promptList.getPromptAnswer().getAnswer() == null) {
				isValid = false;
                promptList.setIncomplete(true);
				if (firstInvalidLocation.length == 0) {
					firstInvalidLocation = new int[2];
					((View)promptList.getParent()).getLocationInWindow(firstInvalidLocation);
				}
			} else {
				promptList.setIncomplete(false);
			}
		}

		if (firstInvalidLocation.length > 0) {
			int[] location = new int[2];
			getLocationOnScreen(location);
			smoothScrollBy(0, firstInvalidLocation[1] - location[1]);
		}

		return isValid;
	}

	public void setNewPrompt(boolean newEntry) {
		mNewEntry = newEntry;
		setEditable(true);
	}

	public void setDateTimePickerListener(DetailsViewUpdateListener listener) {
		mListener = listener;
	}

	public void setPrompts(PromptAnswerGroup promptAnswers) {

		mPromptAnswers = promptAnswers;

		mLocation = promptAnswers.getLatLng();

		updateMapMarker();

		mRecordedDate = promptAnswers.getSubmitDate();

		updateDateTimeUI();

		mPromptsContainer.removeAllViews();
		mGeneratedPromptLists.clear();

		mPrompts = SharedPreferenceManager.getInstance(getContext()).getPrompts();

		for (Prompt prompt : mPrompts) {

			final PromptDetailsItemCard itemView = new PromptDetailsItemCard(getContext());

			mGeneratedPromptLists.add(itemView.getViewPromptDialogList());

			if (promptAnswers.size() > 0) {
				for (PromptAnswer promptAnswer : promptAnswers.getPromptAnswers()) {
					if (promptAnswer.getPrompt().equals(prompt.getPrompt())) {
						// now we have the corresponding Prompt to PromptAnswer
						itemView.setPrompts(prompt, promptAnswer);
					}
				}
			} else {
				itemView.setPrompts(prompt, null);
			}

			mPromptsContainer.addView(itemView);

		}
	}

	private void updateDateTimeUI() {
		mTextviewDate.setText(mRecordedDate.toString(DateTimeFormat.fullDate()));
		mTextviewTime.setText(mRecordedDate.toString(DateTimeFormat.shortTime()));
	}

	private void updateMapMarker() {
		if (mMap == null) return;
		if (mLocation.equals(new LatLng(0, 0))) return;

		mMap.clear();

		Bitmap img1 = SystemUtils.ColourBitmap(getContext(), R.drawable.marker, R.color.base);
		BitmapDescriptor bitmapDescriptor1 = BitmapDescriptorFactory.fromBitmap(img1);

		mMap.addMarker(new MarkerOptions()
				.position(mLocation)
				.icon(bitmapDescriptor1)
				.anchor(0.5f, 0.5f));
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 15f));

	}

	// https://github.com/wdullaer/MaterialDateTimePicker

	@Override
	public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
		mRecordedDate = mRecordedDate.withYear(year).withMonthOfYear(monthOfYear + 1).withDayOfMonth(dayOfMonth);
		updateDateTimeUI();
	}

	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
		mRecordedDate = mRecordedDate.withHourOfDay(hourOfDay).withMinuteOfHour(minute).withSecondOfMinute(second);
		updateDateTimeUI();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// if we can scroll pass the event to the superclass
				if (mScrollable) return super.onTouchEvent(ev);
				// only continue to handle the touch event if scrolling enabled
				return mScrollable; // mScrollable is always false at this point
			default:
				return super.onTouchEvent(ev);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// Don't do anything with intercepted touch events if
		// we are not scrollable
		if (!mScrollable) return false;
		else return super.onInterceptTouchEvent(ev);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		mMap.getUiSettings().setAllGesturesEnabled(false);

		updateMapMarker();

		scrollTo(0, 0);
	}

	public interface DetailsViewUpdateListener {
		void onDateClicked(DatePickerDialog.OnDateSetListener listener, int year, int monthOfYear, int dayOfMonth, DateTime minDateTime, String title);

		void onTimeClicked(TimePickerDialog.OnTimeSetListener listener, int hourOfDay, int minute, boolean isToday, String title);

		void onSubmit(boolean successful);

		void onCancel();
	}
}
