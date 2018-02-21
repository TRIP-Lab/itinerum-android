package ca.itinerum.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.activityrecognition.ActivityRecognitionUtils;
import ca.itinerum.android.recording.GeofenceManager;
import ca.itinerum.android.recording.LocationLoggingService;
import ca.itinerum.android.recording.RecordingUtils;
import ca.itinerum.android.recording.Session;
import ca.itinerum.android.settings.SettingsActivity;
import ca.itinerum.android.sync.PromptAnswerGroup;
import ca.itinerum.android.sync.retrofit.Prompt;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.sync.retrofit.Triplab;
import ca.itinerum.android.utilities.DateUtils;
import ca.itinerum.android.utilities.HaversineDistance;
import ca.itinerum.android.utilities.LocationLoggingEvent;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.MBSASCluster;
import ca.itinerum.android.utilities.ServiceEvents;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.SystemUtils;
import ca.itinerum.android.utilities.db.LocationDatabase;
import ca.itinerum.android.utilities.db.LocationPoint;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import mehdi.sakout.fancybuttons.FancyButton;

public class MapActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener, GoogleMap.OnCircleClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback, PromptDialog.PromptDialogListener, PromptsRecyclerView.OnPromptItemClickedListener, PromptDetailsView.DetailsViewUpdateListener {

	@BindView(R.id.select_time_button) FancyButton mSelectTimeButton;
	@BindView(R.id.title_days_remaining) TextView mTitleDaysRemaining;
	@BindView(R.id.container_days_remaining) ViewGroup mContainerDaysRemaining;
	@BindView(R.id.content_days_remaining) TextView mContentDaysRemaining;
	@BindView(R.id.container_points) ViewGroup mContainerPoints;
	@BindView(R.id.button_prompts) Button mButtonPrompts;
	@BindView(R.id.title_points) TextView mTitlePoints;
	@BindView(R.id.content_points) TextView mContentPoints;
	@BindView(R.id.title_time) TextView mTitleTime;
	@BindView(R.id.info_button) SimpleDraweeView mInfoButton;
	@BindView(R.id.map_container) FrameLayout mMapContainer;
	@BindView(R.id.coordinator) CoordinatorLayout mCoordinator;

	@BindView(R.id.textview_date) TextView mTextViewPromptDetailsDate;
	@BindView(R.id.bottomsheet_prompt_details) ViewGroup mBottomsheetPromptDetails;

	@BindView(R.id.textview_point_title) TextView mTextviewPointTitle;
	@BindView(R.id.textview_point_date) TextView mTextviewPointDate;
	@BindView(R.id.textview_point_accuracy) TextView mTextviewPointAccuracy;
	@BindView(R.id.textview_point_mode) TextView mTextviewPointMode;
	@BindView(R.id.bottomsheet_point_details) LinearLayout mBottomsheetPointDetails;
	@BindView(R.id.toolbar) Toolbar mToolbar;
	@BindView(R.id.container) LinearLayout mContainer;
	@BindView(R.id.button_more_info_prompt_details) Button mButtonMoreInfoPromptDetails;


	private MapTripView mMapTripView;
	private PromptsRecyclerView mPromptsRecyclerView;

	@BindDimen(R.dimen.padding_large) int LARGE_PADDING;

	@BindColor(R.color.base_colour) int mBaseColour;

	private GoogleMap mMap;

	private Handler mDebugHandler;

	private final int LOCATION_PERMISSION_CODE = 10823;

	private Runnable mDebugStatusChecker = new Runnable() {
		@Override
		public void run() {
			updateDebugView();
			mDebugHandler.postDelayed(mDebugStatusChecker, 1000);
		}
	};

	private Runnable mRecordingChecker = new Runnable() {
		@Override
		public void run() {
			mDebugHandler.postDelayed(mRecordingChecker, 5000);
		}
	};
	private Circle mClickedMarkerRadius;
	private Circle mGeofenceDebugRadius;

	private DialogFragment mDialogFragment = null;
	private AlertDialog mDialog = null;

	private boolean mDatesSet;
	private int mCurrentPrompt;
	private List<Prompt> mPrompts;
	private PromptAnswer[] mPromptResponses;
	private int mMaximumNumberOfPrompts;
	private int mNumberOfPrompts;
	private int mNumberOfRecordedPrompts;
	private int mNumberOfDays;

	private FusedLocationProviderClient mFusedLocationClient;
	private LatLng mLastLocation;
	private SharedPreferenceManager mPreferenceManager;
	private DateTime mFromDate;
	private DateTime mToDate;
	private Disposable mMapUpdate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_map);
		ButterKnife.bind(this);


		mMapTripView = new MapTripView(this);
		mMapTripView.setOnMapReadyCallback(this);

		mPromptsRecyclerView = new PromptsRecyclerView(this);
		mPromptsRecyclerView.setOnPromptItemClickListener(this);

		BottomSheetBehavior.from(mBottomsheetPromptDetails).setState(BottomSheetBehavior.STATE_HIDDEN);
		BottomSheetBehavior.from(mBottomsheetPointDetails).setState(BottomSheetBehavior.STATE_HIDDEN);

		mMapContainer.addView(mMapTripView);
		if (mMapTripView.getMapview() != null) mMapTripView.getMapview().onCreate(savedInstanceState);

		mButtonPrompts.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MapActivity.this, PromptListActivity.class);
				List<Pair<View, String>> pairs = new ArrayList<>();

				pairs.add(Pair.create((View) mToolbar, "toolbar"));

				View statusBar = findViewById(android.R.id.statusBarBackground);
				View navigationBar = findViewById(android.R.id.navigationBarBackground);

				if (statusBar != null) {
					pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
				}
				if (navigationBar != null) {
					pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
				}

				ActivityOptionsCompat options = ActivityOptionsCompat.
						makeSceneTransitionAnimation(MapActivity.this, pairs.toArray(new Pair[pairs.size()]));
				startActivity(intent, options.toBundle());
			}
		});

		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		setSupportActionBar(mToolbar);

		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setCustomView(R.layout.view_actionbar_banner);

		mPreferenceManager = SharedPreferenceManager.getInstance(this);

		mInfoButton.setImageURI(Triplab.sDashboardBaseURL + mPreferenceManager.getAvatar());

		mMaximumNumberOfPrompts = mPreferenceManager.getMaximumNumberOfPrompts();
		mNumberOfPrompts = mPreferenceManager.getNumberOfPrompts();
		mNumberOfDays = mPreferenceManager.getNumberOfRecordingDays();
		mNumberOfRecordedPrompts = mPreferenceManager.getNumberOfRecordedPrompts();

		mDatesSet = false;
		mMapTripView.mProgressBar.setVisibility(View.INVISIBLE);

		mMapTripView.mVersion.setText(BuildConfig.VERSION_NAME);

		mMapTripView.mUuid.setText(mPreferenceManager.getUUID() + " current v: " + mPreferenceManager.getCurrentVersion());

		mSelectTimeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(MapActivity.this, TimeDatePreferenceActivity.class), 30483);
			}
		});


		RegisterEventBus();

		configurePausePlayFab();

		mInfoButton.setVisibility(View.VISIBLE);
		mInfoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				AboutDialog dialog = new AboutDialog();
				Bundle bundle = new Bundle();
				bundle.putString("message", SharedPreferenceManager.getInstance(MapActivity.this).getAboutText());
				bundle.putString("remote_image", Triplab.sDashboardBaseURL + SharedPreferenceManager.getInstance(MapActivity.this).getAvatar());
				bundle.putBoolean("show_brand", false);
				dialog.setArguments(bundle);
				dialog.show(getSupportFragmentManager(), "about");

			}
		});

		mPrompts = mPreferenceManager.getPrompts();

		if (mPrompts != null && mPrompts.size() > 0) {
			mPromptResponses = new PromptAnswer[mPrompts.size()];
			Arrays.fill(mPromptResponses, null);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!mDatesSet) mPreferenceManager.resetDatesToDefault();
		else mDatesSet = false;

		mMapTripView.setPausedState(mPreferenceManager.isRecordingPaused());
		if (mMapTripView.getMapview() != null) mMapTripView.getMapview().onResume();

		updatePointsRecordedUI();
		updateDaysRemainingUI();

		RecordingUtils.cancelGeofenceNotification(this);

		if (getIntent().getBooleanExtra(LocationLoggingService.GEOFENCE_INTENT_EXTRA, false)) {
			showPrompt();
			getIntent().removeExtra(LocationLoggingService.GEOFENCE_INTENT_EXTRA);
		} else if (Session.getInstance().shouldShowDwellDialog()) {
			showStoppedQuestion();
		} else {
			if (mDialog != null) mDialog.dismiss();
		}

		checkGeofenceSurvey();
		checkTimeCutoffAndAlertUser();

		// hard refresh when date is set to today
		if (mPreferenceManager.getDateState() == TimeDatePreferenceActivity.DateState.TODAY) {
			mPreferenceManager.resetDatesToDefault();
		}

		mSelectTimeButton.setText(mPreferenceManager.getDateState().getLabel(this));

		setupMap();

		mMapTripView.mStatusText.setText(getString(Session.getInstance().isRecording() ? R.string.started : R.string.stopped));


		Logger.l.d("ServiceRecordLocations running = "
				+ LocationLoggingService.isRunning(this));

		// This will check if location mode is in high accuracy
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			try {
				int locationMode = Settings.Secure.getInt(
						getContentResolver(),
						Settings.Secure.LOCATION_MODE);

				if (locationMode != Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) {
					new AlertDialog.Builder(this)
							.setTitle(
									getString(android.R.string.dialog_alert_title))
							.setMessage(
									R.string.alert_location_settings)
							.setPositiveButton(R.string.dialog_settings,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											startActivity(new Intent(
													Settings.ACTION_LOCATION_SOURCE_SETTINGS));
										}
									})
							.setIcon(R.drawable.ic_warning_black_36dp).show();
				}
			} catch (SettingNotFoundException e) {
				Logger.l.e(e.toString());
			}
		}

		GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
		int result = googleAPI.isGooglePlayServicesAvailable(this);

		if (result != ConnectionResult.SUCCESS) {
			mMapTripView.mStatusText.setVisibility(View.GONE);
			mMapTripView.mPointsDetailsText.setVisibility(View.GONE);
			mSelectTimeButton.setVisibility(View.GONE);
		} else {
			mMapTripView.mStatusText.setVisibility(View.VISIBLE);
			mMapTripView.mPointsDetailsText.setVisibility(View.VISIBLE);
			mSelectTimeButton.setVisibility(View.VISIBLE);
		}

		mMapTripView.mToggleGps.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new LocationLoggingEvent.StartStop(!Session.getInstance().isRecording()));
			}
		});

		mMapTripView.mModePromptButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Session.getInstance().setGeofencePurposeRecorded(false);
				showStoppedQuestion();
			}
		});
	}

	private void updatePoints() {

		Bitmap img = SystemUtils.ColourBitmap(MapActivity.this, R.drawable.marker, R.color.base_colour);
		final BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(img);

		mMapTripView.mProgressBar.setVisibility(View.VISIBLE);

		// get all prompts from db and group them
		Flowable<List<PromptAnswerGroup>> groupedPrompts = LocationDatabase.getInstance(this).promptDao()
				.getAllRegisteredPromptAnswersFlowable(DateUtils.formatDateForBackend(mFromDate), DateUtils.formatDateForBackend(mToDate)).map(new Function<List<PromptAnswer>, List<PromptAnswerGroup>>() {
					@Override
					public List<PromptAnswerGroup> apply(List<PromptAnswer> promptAnswers) throws Exception {
						return PromptAnswerGroup.sortPrompts(promptAnswers, mNumberOfPrompts);

					}
				});


		// get the points from db, apply Douglas Peucker filter to reduce number of points, cluster them
		Flowable<List<CentroidCluster<LocationPoint>>> pointClusters = LocationDatabase.getInstance(this)
				.locationDao().getAllPointsBetweenDatesFlowable(50, mFromDate.toString(DateUtils.PATTERN), mToDate.toString(DateUtils.PATTERN))
				.map(new Function<List<LocationPoint>, List<CentroidCluster<LocationPoint>>>() {
					@Override
					public List<CentroidCluster<LocationPoint>> apply(List<LocationPoint> locationPoints) throws Exception {
						MBSASCluster clusterer = new MBSASCluster(100, new HaversineDistance());
						return clusterer.cluster(locationPoints);
					}
				});

		// combine latest results into tuple
		Flowable<Pair<List<CentroidCluster<LocationPoint>>, List<PromptAnswerGroup>>> combineLatest = Flowable.combineLatest(pointClusters, groupedPrompts, new BiFunction<List<CentroidCluster<LocationPoint>>, List<PromptAnswerGroup>, Pair<List<CentroidCluster<LocationPoint>>, List<PromptAnswerGroup>>>() {
			@Override
			public Pair<List<CentroidCluster<LocationPoint>>, List<PromptAnswerGroup>> apply(List<CentroidCluster<LocationPoint>> centroidClusters, List<PromptAnswerGroup> promptAnswerGroups) throws Exception {
				return new Pair<>(centroidClusters, promptAnswerGroups);
			}
		});

		if (mMapUpdate != null && !mMapUpdate.isDisposed()) mMapUpdate.dispose();

		mMapUpdate = combineLatest
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Consumer<Pair<List<CentroidCluster<LocationPoint>>, List<PromptAnswerGroup>>>() {
					@Override
					public void accept(Pair<List<CentroidCluster<LocationPoint>>, List<PromptAnswerGroup>> listListPair) throws Exception {

						if (listListPair.first.isEmpty() && listListPair.second.isEmpty()) throw new Exception("empty dataset");

						mMap.clear();

						List<CentroidCluster<LocationPoint>> locationPoints = listListPair.first;
						List<PromptAnswerGroup> groupedPromptsList = listListPair.second;

						final LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();

						boolean boundsBuilderNotEmpty = false;

						List<LatLng> latLngs = new ArrayList<>(locationPoints.size());

						for (CentroidCluster<LocationPoint> point : locationPoints) {

							boundsBuilderNotEmpty = true;

							LatLng latLng = new LatLng(point.getCenter().getPoint()[0], point.getCenter().getPoint()[1]);

							mMap.addMarker(new MarkerOptions()
									.position(latLng)
									.icon(bitmapDescriptor)
									.anchor(0.5f, 0.5f)
									.snippet(Double.toString(point.getPoints().get(0).getHaccuracy()) + "%" + point.getPoints().get(0).getActivity() + "%" + point.getPoints().get(0).getTimestamp()));

							latLngs.add(latLng);

							boundsBuilder.include(latLng);

						}

						int i = 0;
						for (PromptAnswerGroup promptAnswerGroup : groupedPromptsList) {
							Circle circle = mMap.addCircle(new CircleOptions()

									.center(promptAnswerGroup.getLatLng())
									.radius(GeofenceManager.GEOFENCE_RADIUS)
									.strokeWidth(0)
									.clickable(true)
									.fillColor(getResources().getColor(R.color.stop_colour)));

							circle.setTag(i);

							i++;
						}

						if (boundsBuilderNotEmpty) {

							mMap.addPolyline(new PolylineOptions().addAll(latLngs).color(mBaseColour).width(10));

							moveCamera(boundsBuilder.build(), mMap, mMapContainer.getWidth(), mMapContainer.getHeight());
						}

						mMapTripView.mProgressBar.setVisibility(View.GONE);
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
						Logger.l.e(throwable.toString());

						mMapTripView.mProgressBar.setVisibility(View.GONE);

						if (throwable.getMessage().equals("empty dataset")) {

							if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
								String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
								ActivityCompat.requestPermissions(MapActivity.this, permission, LOCATION_PERMISSION_CODE);
								return;
							}

							//TODO: observable
							mFusedLocationClient.getLastLocation().addOnSuccessListener(MapActivity.this, new OnSuccessListener<Location>() {
								@Override
								public void onSuccess(Location location) {
									// Got last known location. In some rare situations this can be null.
									if (location != null) {
										mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
										moveCamera(mLastLocation, mMap, 15);
									}
								}
							});
						}

						if (mPreferenceManager.getHasDwelledOnce()) Toast.makeText(MapActivity.this, R.string.snackbar_no_points, Toast.LENGTH_LONG).show();
					}
				});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mMapTripView.getMapview() != null) mMapTripView.getMapview().onPause();
		if (mDialog != null) mDialog.dismiss();
		mDialog = null;
		stopDebugRefresh();
		if (mMapUpdate != null) mMapUpdate.dispose();
	}

	@Override
	protected void onDestroy() {
		UnregisterEventBus();
		try {
			if (mMapTripView.getMapview() != null) mMapTripView.getMapview().onDestroy();
		} catch (Exception e) {
			Logger.l.e("Error destroying MapView", e.toString());
		}
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (mMapTripView.getMapview() != null) mMapTripView.getMapview().onLowMemory();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mMapTripView.getMapview() != null) mMapTripView.getMapview().onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {

		BottomSheetBehavior promptBehaviour = BottomSheetBehavior.from(mBottomsheetPromptDetails);

		switch (promptBehaviour.getState()) {
			case BottomSheetBehavior.STATE_EXPANDED:
			case BottomSheetBehavior.STATE_SETTLING:
			case BottomSheetBehavior.PEEK_HEIGHT_AUTO:
				promptBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
				return;
			case BottomSheetBehavior.STATE_COLLAPSED:
				promptBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
				return;
		}

		BottomSheetBehavior pointBehaviour = BottomSheetBehavior.from(mBottomsheetPointDetails);

		switch
				(pointBehaviour.getState()) {
			case BottomSheetBehavior.STATE_EXPANDED:
			case BottomSheetBehavior.PEEK_HEIGHT_AUTO:
			case BottomSheetBehavior.STATE_COLLAPSED:
			case BottomSheetBehavior.STATE_SETTLING:
				pointBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
				return;
		}

		super.onBackPressed();
	}

	private void toggle() {
		EventBus.getDefault().post(new LocationLoggingEvent.PauseResume(mPreferenceManager.isRecordingPaused()));
		if (mPreferenceManager.isRecordingPaused())
			mMapTripView.mPauseMaskingView.setVisibility(View.GONE);
		else mMapTripView.mPauseMaskingView.setVisibility(View.VISIBLE);
		mMapTripView.mFab.toggle();
		mPreferenceManager.togglePauseRecording();
	}

	private void configurePausePlayFab() {
		mMapTripView.mFab.setVisibility(View.VISIBLE);

		mMapTripView.mFab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showTogglePrompt();
			}
		});
	}

	private void showTogglePrompt() {
		int title = SharedPreferenceManager.getInstance(MapActivity.this).isRecordingPaused() ?
				R.string.should_resume_title : R.string.should_pause_title;

		int message = SharedPreferenceManager.getInstance(MapActivity.this).isRecordingPaused() ?
				R.string.should_resume_message : R.string.should_pause_message;

		AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				toggle();
			}
		});
		builder.show();
	}

	private void checkTimeCutoffAndAlertUser() {
		if (RecordingUtils.isOngoing(this)) return;

		if (RecordingUtils.isComplete(this)) {
			new AlertDialog.Builder(this)
					.setMessage(getString(R.string.survey_complete_message))
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

						}
					})
					.show();

			((DMApplication) getApplicationContext()).stopLoggingService();
		}
	}

	// ** Debug View stuff starts **
	private void startDebugRefresh() {
		if (mDebugHandler == null) mDebugHandler = new Handler(Looper.getMainLooper());
		mDebugStatusChecker.run();
		mRecordingChecker.run();

	}

	private void stopDebugRefresh() {
		if (mDebugHandler != null) {
			mDebugHandler.removeCallbacks(mDebugStatusChecker);
			mDebugHandler.removeCallbacks(mRecordingChecker);
		}
	}

	private void updateDebugView() {
		mMapTripView.mDebugGeofenceActive.setText("geofence exists: " + Session.getInstance().isGeofenceActive());

		mMapTripView.mDebugGeofenceDwell.setText("geofence dwell: " + Session.getInstance().isGeofenceDwell());

		mMapTripView.mDebugGeofenceLoitering.setText("geofence loitering: " + Session.getInstance().isGeofenceLoitering());

		mMapTripView.mDebugLastSync.setText("last sync: " + new DateTime(mPreferenceManager.getLastSyncTime()).toString());

		mMapTripView.mCurrentModeText.setText("Current mode: " + Session.getInstance().getCurrentDetectedActivity());

		if (Session.getInstance().isGeofenceActive() && mMap != null) {

			if (mGeofenceDebugRadius != null &&
					Session.getInstance().getGeofenceLatLng() != null &&
					!Session.getInstance().getGeofenceLatLng().equals(mGeofenceDebugRadius.getCenter())) {
				Logger.l.d("Geofence not equal - removing", Session.getInstance().getGeofenceLatLng().toString(), mGeofenceDebugRadius.getCenter().toString());
				mGeofenceDebugRadius.remove();
				mGeofenceDebugRadius = null;
			}

			if (mGeofenceDebugRadius == null && Session.getInstance().getGeofenceLatLng() != null)
				mGeofenceDebugRadius = mMap.addCircle(
						new CircleOptions()
								.center(Session.getInstance().getGeofenceLatLng())
								.radius(GeofenceManager.GEOFENCE_RADIUS)
								.strokeWidth(0)
								.fillColor(Color.argb(75, 0, 0, 255)
								)
				);
		} else {
			if (mGeofenceDebugRadius != null) {
				mGeofenceDebugRadius.remove();
				mGeofenceDebugRadius = null;
			}
		}

	}

	// ** Debug View stuff ends **

	private void showPromptDialog(final Prompt prompt) {

		final ArrayList<String> promptPositions = (mPromptResponses[mCurrentPrompt] != null && mPromptResponses[mCurrentPrompt].getAnswer() != null) ?
				mPromptResponses[mCurrentPrompt].getAnswer() :
				new ArrayList<String>();

		// single choice prompt
		if (prompt.getId() != 1 && prompt.getId() != 2) {
			if (BuildConfig.DEBUG)
				throw new NotImplementedException("No prompt id type " + prompt.getId());
			return;
		}

		mDialogFragment = PromptDialog.newInstance(prompt, mCurrentPrompt, promptPositions);
		mDialogFragment.show(getSupportFragmentManager(), "" + prompt.getId());

	}

	private boolean shouldShowPrompt() {
		if (!mPreferenceManager.getOngoingPrompts() && (mNumberOfPrompts < 1 || mPreferenceManager.getNumberOfRecordedPrompts() >= mMaximumNumberOfPrompts))
			return false;

		//TODO: this might be the reason we're losing prompts
		if (mDialog != null || Session.getInstance().isGeofencePurposeRecorded()) return false;

		return !(mPrompts == null || mPrompts.size() < 1);
	}

	private void showPrompt() {

		if (!shouldShowPrompt()) return;

		if (mCurrentPrompt < mPrompts.size()) {
			showPromptDialog(mPrompts.get(mCurrentPrompt));
		} else {
			submitGeofenceSurvey();
			checkGeofenceSurvey();
		}

	}

	private void showStoppedQuestion() {

		if (!shouldShowPrompt()) return;

		mDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.notification_title)
				.setMessage(R.string.notification_stopped_message)
				.setPositiveButton(R.string.notification_true, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mDialog = null;
						mCurrentPrompt = 0;
						showPrompt();
					}
				})
				.setNegativeButton(R.string.notification_false, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						cancelGeofenceSurvey();
					}
				}).create();

		mDialog.setCanceledOnTouchOutside(false);
		mDialog.setCancelable(false);
		mDialog.show();

	}

	private void updatePointsRecordedUI() {

		mNumberOfRecordedPrompts = mPreferenceManager.getNumberOfRecordedPrompts();

		if (mNumberOfPrompts < 1) {
			mContainerPoints.setVisibility(View.GONE);
			return;
		}

		mContainerPoints.setVisibility(View.VISIBLE);
		mContentPoints.setText(String.format(getString(R.string.delimiter), mNumberOfRecordedPrompts, mMaximumNumberOfPrompts));
	}

	private void updateDaysRemainingUI() {
		if (mNumberOfDays <= 0) {
			mTitleDaysRemaining.setText(R.string.title_days_incrementing);
			long days = TimeUnit.MILLISECONDS.toDays(Math.abs(System.currentTimeMillis() - mPreferenceManager.getQuestionnaireCompleteDate()));
			mContentDaysRemaining.setText(String.format(Locale.US, "%d", days));
		} else {
			long daysRemaining = RecordingUtils.getCutoffDays(this);
			if (daysRemaining == -1) mContentDaysRemaining.setText(R.string.days_remaining_done);
			else
				mContentDaysRemaining.setText(String.format(getString(R.string.delimiter), daysRemaining, mNumberOfDays));
		}
	}

	@SuppressLint("StringFormatMatches")
	private void checkGeofenceSurvey() {
		if (mMaximumNumberOfPrompts < 1 || mNumberOfRecordedPrompts < mMaximumNumberOfPrompts)
			return;

		if (mPreferenceManager.getOngoingPrompts()) return; //the user will continue to get prompts

		final long daysRemaining = TimeUnit.DAYS.convert(RecordingUtils.getCutoffTime(this) - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

		String message = String.format(getString(R.string.max_trips_completed_message), mMaximumNumberOfPrompts, daysRemaining);
		mDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.notification_title)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mDialog.dismiss();
						mDialog = null;
					}
				})
				.show();

		mDialog.setCanceledOnTouchOutside(false);

	}

	private void cancelGeofenceSurvey() {
		mDialog = null;
		Arrays.fill(mPromptResponses, null);
		RecordingUtils.cancelGeofenceNotification(getApplicationContext());
		Session.getInstance().setGeofencePurposeRecorded(true);
		Session.getInstance().setShowDwellDialog(false);

		// Now insert a cancelled prompt answer
		// cancelled prompts should exist above the maximum number of prompts to avoid collision.
		int count = LocationDatabase.getInstance(this).promptDao().getCount() + SharedPreferenceManager.getInstance(this).getMaximumNumberOfPrompts() + 1;

		PromptAnswer promptAnswer = new PromptAnswer()
				.withPrompt("")
				.withAnswer("")
				.withLatitude(Session.getInstance().getLastRecordedLatitude())
				.withLongitude(Session.getInstance().getLastRecordedLongitude())
				.withDisplayedAt(DateTime.now().toString(ISODateTimeFormat.dateTime()))
				.withRecordedAt(DateTime.now().toString(ISODateTimeFormat.dateTime()))
				.withUuid(UUID.randomUUID().toString())
				.withUploaded(false)
				.withCancelled(true)
				.withPromptNumber(count);

		LocationDatabase.getInstance(this).promptDao().insert(promptAnswer);

	}

	private void submitGeofenceSurvey() {
		RecordingUtils.cancelGeofenceNotification(getApplicationContext());
		mDialog = null;

		// TODO: clone because this should be done async
		PromptAnswer[] p = mPromptResponses.clone();
		LocationDatabase.getInstance(this).promptDao().insert(p);
		//increment the number of recoded prompts
		mPreferenceManager.setNumberOfRecordedPrompts(mNumberOfRecordedPrompts + 1);

		Arrays.fill(mPromptResponses, null);

		Session.getInstance().setGeofencePurposeRecorded(true);
		Session.getInstance().setShowDwellDialog(false);

		updatePointsRecordedUI();

		drawMap();

	}

	/**
	 * Creates the activity_map
	 */
	private void setupMap() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
			ActivityCompat.requestPermissions(this, permission, LOCATION_PERMISSION_CODE);
			return;
		}

		// This is safe to call whenever, so this isn't a bad idea at this point
		((DMApplication) getApplication()).startLoggingService();

		if (!mPreferenceManager.getHasDwelledOnce()) {

			mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
				@Override
				public void onSuccess(Location location) {
					// Got last known location. In some rare situations this can be null.
					if (location != null) {
						mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
						moveCamera(mLastLocation, mMap, 15);
					}
				}
			});
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == LOCATION_PERMISSION_CODE && grantResults[0] != -1) {
			((DMApplication) getApplication()).startLoggingService();
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	private void drawMap() {
		if (mMap != null) {

			SharedPreferenceManager prefManager = SharedPreferenceManager
					.getInstance(this);

			mFromDate = new DateTime(prefManager.getFromDate());
			mToDate = new DateTime(prefManager.getToDate());
			updatePoints();

		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.refresh_menu_item:
				SharedPreferenceManager.getInstance(MapActivity.this).resetDatesToDefault();
				mSelectTimeButton.setText(SharedPreferenceManager.getInstance(MapActivity.this).getDateState().getLabel(MapActivity.this));
				drawMap();
				if (BuildConfig.DEBUG) updateDebugView();
				break;

			case R.id.debug_view:
				if (mMapTripView.mDebugView.getVisibility() != View.VISIBLE) {
					mMapTripView.mDebugView.setVisibility(View.VISIBLE);
					startDebugRefresh();
					mRecordingChecker.run();
				} else {
					mMapTripView.mDebugView.setVisibility(View.GONE);
					stopDebugRefresh();
				}
				break;

			case R.id.settings_menu_item:

				Intent intent = new Intent(MapActivity.this, SettingsActivity.class);
				List<Pair<View, String>> pairs = new ArrayList<>();

				pairs.add(Pair.create((View) mToolbar, "toolbar"));

				View statusBar = findViewById(android.R.id.statusBarBackground);
				View navigationBar = findViewById(android.R.id.navigationBarBackground);

				if (statusBar != null) {
					pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
				}
				if (navigationBar != null) {
					pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
				}

				ActivityOptionsCompat options = ActivityOptionsCompat.
						makeSceneTransitionAnimation(MapActivity.this, pairs.toArray(new Pair[pairs.size()]));
				startActivity(intent, options.toBundle());

				break;


		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == 30483) {
			if (resultCode == Activity.RESULT_OK) {
				mDatesSet = true;
				drawMap();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.debug_view).setVisible((BuildConfig.DEBUG || BuildConfig.BUILD_TYPE.equals("alpha")));

		return true;
	}

	private static void moveCamera(LatLngBounds bounds, GoogleMap map, int width, int height) {
		if (map == null) return;

		map.moveCamera(CameraUpdateFactory
				.newLatLngBounds(bounds,
						width, height, 2));

	}

	private static void animateCamera(LatLngBounds bounds, GoogleMap map, int width, int height) {
		if (map == null) return;

		map.animateCamera(CameraUpdateFactory
				.newLatLngBounds(bounds,
						width, height, 2));

	}


	private static void moveCamera(LatLng location, GoogleMap map, int zoom) {
		if (map == null || location == null) return;

		map.moveCamera(CameraUpdateFactory
				.newCameraPosition(new CameraPosition.Builder()
						.target(location).zoom(zoom)
						.bearing(0).tilt(0).build()));
	}

	@Override
	public void onPromptItemClick(View v, int position) {

		PromptAnswerGroup answers = PromptAnswerGroup.sortPrompts(LocationDatabase.getInstance(this).promptDao().getAllRegisteredPromptAnswers(), mNumberOfPrompts).get(position);
		PromptDetailsView view = new PromptDetailsView(this);
		view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		view.setPrompts(answers);
		view.setDateTimePickerListener(this);
		mMapContainer.removeAllViews();
		mMapContainer.addView(view);
	}

	@Override
	public void onDateClicked(DatePickerDialog.OnDateSetListener listener, int year, int monthOfYear, int dayOfMonth) {

		Calendar minDate = new DateTime(mPreferenceManager.getQuestionnaireCompleteDate()).toCalendar(Locale.US);
		Calendar maxDate = DateTime.now().toCalendar(Locale.US);

		DatePickerDialog dialog = DatePickerDialog
				.newInstance(listener, year, monthOfYear, dayOfMonth);

		dialog.setMinDate(minDate);
		dialog.setMaxDate(maxDate);

		dialog.setAccentColor(getResources().getColor(R.color.base_colour));
		dialog.show(getFragmentManager(), "datepicker_dialog");
	}

	@Override
	public void onTimeClicked(TimePickerDialog.OnTimeSetListener listener, int hourOfDay, int minute, boolean isToday) {

		TimePickerDialog dialog = TimePickerDialog.newInstance(listener, hourOfDay, minute, android.text.format.DateFormat.is24HourFormat(this));

		if (isToday) dialog.setMaxTime(hourOfDay, minute, 59);
		dialog.setAccentColor(getResources().getColor(R.color.base_colour));
		dialog.show(getFragmentManager(), "timepicker_dialog");
	}

	@Override
	public void onSubmit(boolean successful) {
		BottomSheetBehavior.from(mBottomsheetPromptDetails).setState(BottomSheetBehavior.STATE_HIDDEN);
	}

	@Override
	public void onCancel() {
		BottomSheetBehavior.from(mBottomsheetPromptDetails).setState(BottomSheetBehavior.STATE_HIDDEN);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		if (marker.getSnippet() == null) return true;

		BottomSheetBehavior.from(mBottomsheetPromptDetails).setState(BottomSheetBehavior.STATE_HIDDEN);
		BottomSheetBehavior.from(mBottomsheetPointDetails).setState(BottomSheetBehavior.STATE_EXPANDED);

		String[] snippets = marker.getSnippet().split("%");

		float accuracy = Float.parseFloat(snippets[0]);
		int mode = Integer.parseInt(snippets[1]);
		DateTime dateTime = new DateTime(snippets[2]);

		if (mClickedMarkerRadius != null) {
			mClickedMarkerRadius.remove();
			// a way to remove highlighting
			if (mClickedMarkerRadius.getCenter().equals(marker.getPosition())) return false;
		}

		mClickedMarkerRadius = mMap.addCircle(new CircleOptions()
				.center(marker.getPosition())
				.radius(Float.parseFloat(snippets[0]))
				.strokeWidth(0)
				.fillColor(Color.argb(75, 255, 0, 0)));
		
		mTextviewPointDate.setText(String.format(getString(R.string.recorded_date_time), dateTime.toString(DateTimeFormat.fullDate()), dateTime.toString(DateTimeFormat.shortTime())));
		mTextviewPointAccuracy.setText(String.format(getString(R.string.accuracy_estimate), accuracy));
		mTextviewPointMode.setText(String.format(getString(R.string.mode_estimate), ActivityRecognitionUtils.parseActivityLocalized(mode, this)));

		return true;
	}

	@Override
	public void onCircleClick(Circle circle) {
		if (circle.getTag() != null && circle.getTag() instanceof Integer) { // non null && Integer = int

			BottomSheetBehavior.from(mBottomsheetPointDetails).setState(BottomSheetBehavior.STATE_HIDDEN);

//			mPromptDetailsView.scrollTo(0, 0);

			final int position = (int) circle.getTag();
			PromptAnswerGroup promptAnswer = PromptAnswerGroup.sortPrompts(LocationDatabase.getInstance(this).promptDao().getAllRegisteredPromptAnswers(), mNumberOfPrompts).get(position);

//			mPromptDetailsView.setPrompts(promptAnswer);

			mButtonMoreInfoPromptDetails.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(MapActivity.this, PromptDetailsActivity.class);
					intent.putExtra("position", position);

					List<Pair<View, String>> pairs = new ArrayList<>();

//					pairs.add(Pair.create(view.findViewById(R.id.textview_time), "time"));
//				pairs.add(Pair.create(view.findViewById(R.id.textview_date), "date"));
					pairs.add(Pair.create((View) mToolbar, "toolbar"));

					// These are fixes for flickering nav and status bars
					View statusBar = findViewById(android.R.id.statusBarBackground);
					View navigationBar = findViewById(android.R.id.navigationBarBackground);

					if (statusBar != null) {
						pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
					}
					if (navigationBar != null) {
						pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
					}

					ActivityOptionsCompat options = ActivityOptionsCompat.
							makeSceneTransitionAnimation(MapActivity.this, pairs.toArray(new Pair[pairs.size()]));

					startActivity(intent, options.toBundle());
				}
			});

			mTextViewPromptDetailsDate.setText(String.format(getString(R.string.recorded_date_time), promptAnswer.getSubmitDate().toString(DateTimeFormat.fullDate()), promptAnswer.getSubmitDate().toString(DateTimeFormat.shortTime())));

			BottomSheetBehavior b = BottomSheetBehavior.from(mBottomsheetPromptDetails);
			b.setState(BottomSheetBehavior.STATE_EXPANDED);
			b.setHideable(true);
//			b.setPeekHeight(mPromptDetailsView.getPeekHeight());
			mMap.animateCamera(CameraUpdateFactory.newLatLng(promptAnswer.getLatLng()), 200, null);
		}
	}

	@Override
	public void onMapClick(LatLng latLng) {
		if (mClickedMarkerRadius != null) {
			mClickedMarkerRadius.remove();
		}
	}

	private void RegisterEventBus() {
		EventBus.getDefault().register(this);
	}

	private void UnregisterEventBus() {
		try {
			EventBus.getDefault().unregister(this);
		} catch (Throwable t) {
			//this may crash if registration did not go through. just be safe
		}
	}

	@Subscribe
	public void onEventMainThread(final ServiceEvents.StatusMessage message) {
		if (mMapTripView.mStatusText == null) return; //?? getting thread issues on setting text
		MapActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				mMapTripView.mStatusText.setText(message.status);
			}
		});
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(ServiceEvents.UserMessage message) {
		Toast.makeText(this, message.message, Toast.LENGTH_SHORT).show();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(ServiceEvents.FatalMessage event) {
		if (mMapTripView.mStatusText == null) return;
		mMapTripView.mStatusText.setText(event.message);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(LocationLoggingEvent.ShowGeofencePromptInActivity activity) {
		if (mMaximumNumberOfPrompts < 1) return;
		if (((DMApplication) getApplication()).isActivityMapActive()) showStoppedQuestion();
	}


	@Override
	public void onMapReady(GoogleMap googleMap) {
		// Check if we were successful in obtaining the activity_map.
		mMap = googleMap;
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.getUiSettings().setMapToolbarEnabled(false);
		mMap.getUiSettings().setIndoorLevelPickerEnabled(false);

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			mMap.setMyLocationEnabled(true);
		}
		mMap.setOnMarkerClickListener(this);
		mMap.setOnCircleClickListener(this);

		moveCamera(mLastLocation, mMap, 15);

		drawMap();

	}

	@Override
	public void onPositiveButtonClicked() {
		PromptAnswer answer = ((PromptDialog) mDialogFragment).getPromptAnswer();
		mPromptResponses[mCurrentPrompt] = answer;

		if (mPromptResponses[mCurrentPrompt] != null) {
			mPromptResponses[mCurrentPrompt].setPromptNumber(mNumberOfRecordedPrompts);
			mPromptResponses[mCurrentPrompt].setCancelled(false);
			mDialogFragment.dismiss();
			mDialogFragment = null;
			mCurrentPrompt++;
			showPrompt();

		} else {
			Toast.makeText(MapActivity.this, R.string.toast_provide_answer, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onNeutralButtonClicked() {
		mDialogFragment = null;
		mCurrentPrompt--;
		showPrompt();
	}

	@Override
	public void onNegativeButtonClicked() {
		cancelGeofenceSurvey();
		mDialogFragment = null;
	}
}