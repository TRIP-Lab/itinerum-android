package ca.itinerum.android;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.BuildConfig;
import ca.itinerum.android.R;
import ca.itinerum.android.activityrecognition.ActivityRecognitionUtils;
import ca.itinerum.android.recording.GeofenceManager;
import ca.itinerum.android.recording.LocationLoggingService;
import ca.itinerum.android.recording.RecordingUtils;
import ca.itinerum.android.recording.Session;
import ca.itinerum.android.settings.SettingsFragment;
import ca.itinerum.android.sync.PromptAnswerGroup;
import ca.itinerum.android.sync.retrofit.Prompt;
import ca.itinerum.android.sync.retrofit.PromptAnswer;
import ca.itinerum.android.utilities.DateUtils;
import ca.itinerum.android.utilities.DouglasPeucker;
import ca.itinerum.android.utilities.HaversineDistance;
import ca.itinerum.android.utilities.LocationLoggingEvent;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.MBSASCluster;
import ca.itinerum.android.utilities.ServiceEvents;
import ca.itinerum.android.utilities.SharedPreferenceManager;
import ca.itinerum.android.utilities.SystemUtils;
import ca.itinerum.android.utilities.db.ItinerumDatabase;
import ca.itinerum.android.utilities.db.LocationPoint;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MapActivity extends AppCompatActivity implements GoogleMap.OnCameraMoveListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCircleClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback, PromptsRecyclerView.OnPromptItemClickedListener, PromptDetailsView.DetailsViewUpdateListener, ContentOverlayView.OnDateButtonListener, PromptDialogListener {

	private final int LOCATION_PERMISSION_CODE = 10823;

	@BindDimen(R.dimen.padding_large) int PADDING_LARGE;
	@BindColor(R.color.base) int BASE_COLOUR;

	@BindView(R.id.map_trip_view) MapTripView mMapTripView;
	@BindView(R.id.coordinator) CoordinatorLayout mCoordinator;
	@BindView(R.id.textview_date) AppCompatTextView mTextViewPromptDetailsDate;
	@BindView(R.id.bottomsheet_prompt_details) ViewGroup mBottomsheetPromptDetails;
	@BindView(R.id.textview_point_title) AppCompatTextView mTextviewPointTitle;
	@BindView(R.id.textview_point_date) AppCompatTextView mTextviewPointDate;
	@BindView(R.id.textview_point_accuracy) AppCompatTextView mTextviewPointAccuracy;
	@BindView(R.id.textview_point_mode) AppCompatTextView mTextviewPointMode;
	@BindView(R.id.bottomsheet_point_details) LinearLayout mBottomsheetPointDetails;
	@BindView(R.id.container) LinearLayout mContainer;
	@BindView(R.id.button_more_info_prompt_details) AppCompatButton mButtonMoreInfoPromptDetails;
	@BindView(R.id.content_card) ContentOverlayView mContentCard;
	@BindView(R.id.trip_list_button) FloatingActionButton mTripListButton;
	@BindView(R.id.app_settings_button) FloatingActionButton mAppSettingsButton;
	@BindView(R.id.app_info_button) FloatingActionButton mAppInfoButton;
	@BindView(R.id.map_masking_view) View mMapMaskingView;
	@BindView(R.id.map_touch_view) View mMapTouchView;
	@BindView(R.id.info_view) AboutView mAboutView;
	@BindView(R.id.refresh_button) AppCompatImageButton mRefreshButton;
	@BindView(R.id.debug_button) AppCompatImageButton mDebugButton;
	@BindView(R.id.view_container) FrameLayout mViewContainer;
	@BindView(R.id.settings_fragment_container) FrameLayout mSettingsFragmentContainer; // this is where the fragment goes
	@BindView(R.id.settings_container) LinearLayout mSettingsContainer; // this is the titled box to show and hide
	@BindView(R.id.buttons_container) FrameLayout mButtonsContainer;
	@BindView(R.id.finished) FrameLayout mFinished;

	private SpringAnimation mContentCardSpringAnimation;

	private PromptsRecyclerView mPromptsRecyclerView;
	private GoogleMap mMap;
	private Handler mDebugHandler;

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
	private List<Prompt> mPrompts;
	private PromptAnswerGroup mPromptResponses;
	private int mMaximumNumberOfPrompts;
	private int mNumberOfPrompts;
	private int mNumberOfDays;

	private FusedLocationProviderClient mFusedLocationClient;
	private LatLng mLastLocation;
	private SharedPreferenceManager mPreferenceManager;
	private Disposable mMapUpdateDisposible;
	private int mContentCardHeight;
	private PromptListView mPromptListView;
	private SettingsFragment mPreferenceFragment;
	private Disposable mPromptAnswersDisposable;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// this prevents pretty transitions but is necessary for these versions
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			getWindow().setSharedElementsUseOverlay(false);
		}

		setContentView(R.layout.activity_map);
		ButterKnife.bind(this);

		mMapTripView.setOnMapReadyCallback(this);

		mPromptsRecyclerView = new PromptsRecyclerView(this);
		mPromptsRecyclerView.setOnPromptItemClickListener(this);

		BottomSheetBehavior.from(mBottomsheetPromptDetails).setState(BottomSheetBehavior.STATE_HIDDEN);
		BottomSheetBehavior.from(mBottomsheetPointDetails).setState(BottomSheetBehavior.STATE_HIDDEN);

		if (mMapTripView.getMapview() != null)
			mMapTripView.getMapview().onCreate(savedInstanceState);

		mContentCard.setDateSetListener(this);
		mContentCard.setDetailsViewUpdatePickerListener(this);

		mTripListButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleInfoView(true);
				toggleAppContentView(true);
				toggleSettingsFragment(true);
				toggleTripListView(false);
			}
		});

		mAppSettingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleInfoView(true);
				toggleAppContentView(true);
				toggleTripListView(true);
				toggleSettingsFragment(false);
			}
		});

		mAppInfoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleAppContentView(true);
				toggleTripListView(true);
				toggleSettingsFragment(true);
				toggleInfoView(false);
			}
		});

		mMapMaskingView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//do nothing
			}
		});

		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		mPreferenceManager = SharedPreferenceManager.getInstance(this);

		if (BuildConfig.DEBUG) mPreferenceManager.setHasDwelledOnce(true);

		mMaximumNumberOfPrompts = mPreferenceManager.getMaximumNumberOfPrompts();
		mNumberOfPrompts = mPreferenceManager.getNumberOfPrompts();
		mNumberOfDays = RecordingUtils.getRecordingDays(this);

		if (mMaximumNumberOfPrompts > 0) {

			mPromptAnswersDisposable = ItinerumDatabase.getInstance(this).promptDao().getAllAutomaticPromptAnswersFlowable()
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(
							new Consumer<List<PromptAnswer>>() {
								@Override
								public void accept(List<PromptAnswer> promptAnswers) throws Exception {
									if (mContentCard == null || mNumberOfPrompts <= 0) return;
									mContentCard.setValidatedTrips(promptAnswers.size() / mNumberOfPrompts);
									mContentCard.setTotalValidatedTrips(mMaximumNumberOfPrompts);
								}
							});
		} else {
			mContentCard.mValidatedTripsCard.setVisibility(View.GONE);
		}


		mDatesSet = false;
		mMapTripView.mProgressBar.setVisibility(View.INVISIBLE);

		mMapTripView.mVersion.setText(BuildConfig.VERSION_NAME);

		mMapTripView.mUuid.setText(mPreferenceManager.getUUID() + " current v: " + mPreferenceManager.getCurrentVersion());

		RegisterEventBus();

		mRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				drawMap();
			}
		});

		mDebugButton.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

		mDebugButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMapTripView.mDebugView.getVisibility() != View.VISIBLE) {
					mMapTripView.mDebugView.setVisibility(View.VISIBLE);
					startDebugRefresh();
					mRecordingChecker.run();
				} else {
					mMapTripView.mDebugView.setVisibility(View.GONE);
					stopDebugRefresh();
				}
			}
		});

		mPrompts = mPreferenceManager.getPrompts();

		mContentCard.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				if (bottom - top > 0) {
					mContentCardHeight = bottom - top;
					mContentCard.removeOnLayoutChangeListener(this);
				}

			}
		});

		// Tapping the drawer icon should trigger a toggle outside of fling and drag logic
		mContentCard.getDrawerImage().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleAppContentView(false);
			}
		});

		/**
		 * This handles drag, fling, and tap logic for the content card
		 */
		mContentCard.setOnTouchListener(new View.OnTouchListener() {
			private float mInitialdY;
			private float mInitialRawY;
			final VelocityTracker mVelocityTracker = VelocityTracker.obtain();

			@Override
			public boolean onTouch(View v, final MotionEvent event) {
				int pointerId = event.getPointerId(event.getActionIndex());

				// The view is translating, so velocity tracking gets a little messed up
				event.setLocation(event.getRawX(), event.getRawY());

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN: {
						mVelocityTracker.clear();
						mVelocityTracker.addMovement(event);
						if (mContentCardSpringAnimation != null && mContentCardSpringAnimation.isRunning())
							mContentCardSpringAnimation.cancel();
						mInitialRawY = event.getRawY();
						mInitialdY = mContentCard.getTranslationY();
						return true;
					}
					case MotionEvent.ACTION_MOVE: {
						mVelocityTracker.addMovement(event);
						float dY = Math.min(0, Math.max(-mContentCardHeight + 100, event.getRawY() - mInitialRawY + mInitialdY));
						mContentCard.setTranslationY(dY);
						float progress = Math.abs(dY / (-mContentCardHeight + 100));
						mContentCard.setDrawerImageProgress(progress);
						return true;
					}
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP: {
						if (mContentCardSpringAnimation != null)
							mContentCardSpringAnimation.cancel();

						float displacementY = Math.abs(mInitialRawY - event.getRawY());
						mVelocityTracker.computeCurrentVelocity(1000);
						if (displacementY < 100f) {
							// small taps will expand
							mContentCardSpringAnimation = getContentCardSpringAnimationFactory(mVelocityTracker.getYVelocity(pointerId), 0);
						} else {
							// otherwise perform a fling
							final float restPositon = mVelocityTracker.getYVelocity(pointerId) >= 0 ? 0 : -mContentCardHeight + 100;
							mContentCardSpringAnimation = getContentCardSpringAnimationFactory(mVelocityTracker.getYVelocity(pointerId), restPositon);
						}
						mContentCardSpringAnimation.start();
						return true;
					}
				}
				return false;
			}
		});

	}


	@Override
	protected void onResume() {
		super.onResume();

		if (!mDatesSet) mPreferenceManager.resetDatesToDefault();
		else mDatesSet = false;

		mMapTripView.setPausedState(mPreferenceManager.isRecordingPaused());
		if (mMapTripView.getMapview() != null) mMapTripView.getMapview().onResume();

		updateDaysRemainingUI();

		if (mPromptListView != null) mPromptListView.refreshList();

		RecordingUtils.cancelGeofenceNotification(this);

		if (getIntent().getBooleanExtra(LocationLoggingService.GEOFENCE_INTENT_EXTRA, false)) {
			mPromptResponses = new PromptAnswerGroup(this);
			showPrompt();
			getIntent().removeExtra(LocationLoggingService.GEOFENCE_INTENT_EXTRA);
		} else if (Session.getInstance().shouldShowDwellDialog() && mDialogFragment == null) { // don't show stopped question if dialogfragment is currently showing
			showStoppedQuestion();
		} else {
			if (mDialog != null) mDialog.dismiss();
		}

		checkGeofenceSurvey();
		checkTimeCutoffAndAlertUser();

		// hard refresh when date is set to today
		if (mPreferenceManager.getDateState() == ContentOverlayView.DateState.TODAY) {
			mPreferenceManager.resetDatesToDefault();
		}

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
		} else {
			mMapTripView.mStatusText.setVisibility(View.VISIBLE);
			mMapTripView.mPointsDetailsText.setVisibility(View.VISIBLE);
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

	private void updatePoints(final DateTime fromDate, final DateTime toDate) {

		Bitmap img = SystemUtils.ColourBitmap(MapActivity.this, R.drawable.marker, R.color.base);
		final BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(img);

		mMapTripView.mProgressBar.setVisibility(View.VISIBLE);

		// get all prompts from db and group them
		Flowable<List<PromptAnswerGroup>> groupedPrompts = ItinerumDatabase.getInstance(this).promptDao()
				.getAllRegisteredPromptAnswersFlowable(DateUtils.formatDateForBackend(fromDate), DateUtils.formatDateForBackend(toDate)).map(new Function<List<PromptAnswer>, List<PromptAnswerGroup>>() {
					@Override
					public List<PromptAnswerGroup> apply(List<PromptAnswer> promptAnswers) throws Exception {
						return PromptAnswerGroup.sortPrompts(promptAnswers, mNumberOfPrompts);

					}
				});


		Flowable<List<LocationPoint>> points = ItinerumDatabase.getInstance(this).locationDao().getAllPointsBetweenDatesFlowable(50, fromDate.toString(DateUtils.PATTERN), toDate.toString(DateUtils.PATTERN)).map(new Function<List<LocationPoint>, List<LocationPoint>>() {
			@Override
			public List<LocationPoint> apply(List<LocationPoint> locationPoints) throws Exception {
				return DouglasPeucker.filter(locationPoints, 0.0002);
			}
		});

		// get the points from db, apply Douglas Peucker filter to reduce number of points, cluster them
		Flowable<List<CentroidCluster<LocationPoint>>> pointClusters = points
				.map(new Function<List<LocationPoint>, List<CentroidCluster<LocationPoint>>>() {
					@Override
					public List<CentroidCluster<LocationPoint>> apply(List<LocationPoint> locationPoints) throws Exception {
						MBSASCluster clusterer = new MBSASCluster(50, new HaversineDistance());
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

		if (mMapUpdateDisposible != null && !mMapUpdateDisposible.isDisposed()) mMapUpdateDisposible.dispose();

		mMapUpdateDisposible = combineLatest
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Consumer<Pair<List<CentroidCluster<LocationPoint>>, List<PromptAnswerGroup>>>() {
					@Override
					public void accept(Pair<List<CentroidCluster<LocationPoint>>, List<PromptAnswerGroup>> listListPair) throws Exception {

						if (listListPair.first.isEmpty() && listListPair.second.isEmpty())
							throw new Exception("empty dataset");

						mMap.clear();

						List<CentroidCluster<LocationPoint>> locationPoints = listListPair.first;
						List<PromptAnswerGroup> groupedPromptsList = listListPair.second;

						final LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();

						boolean boundsBuilderNotEmpty = false;

						List<LatLng> latLngs = new ArrayList<>(locationPoints.size());

						for (CentroidCluster<LocationPoint> point : locationPoints) {

							boundsBuilderNotEmpty = true;

							LatLng latLng = new LatLng(point.getCenter().getPoint()[0], point.getCenter().getPoint()[1]);

							//TODO: eventually only new points should be added to the map to prevent overlap when mMap.clear() is removed
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
                            //TODO: polyline is returned here, and mPolyline.updatePoints() can be used to be more memory efficient (instead of clearing map)
							mMap.addPolyline(new PolylineOptions().addAll(latLngs).color(BASE_COLOUR).width(10));

							moveCamera(boundsBuilder.build(), mMap, mMapTripView.getMapview().getWidth(), mMapTripView.getMapview().getHeight());
						}

						mMapTripView.mProgressBar.setVisibility(View.GONE);
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable throwable) throws Exception {
						Logger.l.e(throwable.toString());

						mMapTripView.mProgressBar.setVisibility(View.GONE);

						mMap.clear();

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

						if (mPreferenceManager.getHasDwelledOnce())
							Toast.makeText(MapActivity.this, R.string.snackbar_no_points, Toast.LENGTH_LONG).show();
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
		if (mMapUpdateDisposible != null) mMapUpdateDisposible.dispose();
	}

	@Override
	protected void onDestroy() {

		if (mPromptAnswersDisposable != null) mPromptAnswersDisposable.dispose();

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
		if (mMapTripView.getMapview() != null)
			mMapTripView.getMapview().onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {

		BottomSheetBehavior promptBehaviour = BottomSheetBehavior.from(mBottomsheetPromptDetails);

		switch (promptBehaviour.getState()) {
			case BottomSheetBehavior.STATE_EXPANDED:
			case BottomSheetBehavior.STATE_SETTLING:
			case BottomSheetBehavior.PEEK_HEIGHT_AUTO:
			case BottomSheetBehavior.STATE_COLLAPSED:
				promptBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
				return;
		}

		BottomSheetBehavior pointBehaviour = BottomSheetBehavior.from(mBottomsheetPointDetails);

		switch (pointBehaviour.getState()) {
			case BottomSheetBehavior.STATE_EXPANDED:
			case BottomSheetBehavior.PEEK_HEIGHT_AUTO:
			case BottomSheetBehavior.STATE_COLLAPSED:
			case BottomSheetBehavior.STATE_SETTLING:
				pointBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
				return;
		}

		if (mViewContainer.getVisibility() == View.VISIBLE) {
			toggleTripListView(true);
			return;
		}

		if (mContentCard.getTranslationY() == 0) {
			toggleAppContentView(true);
			return;
		}

		if (mAboutView.getVisibility() == View.VISIBLE) {
			toggleInfoView(true);
			return;
		}

		if (mSettingsContainer.getVisibility() == View.VISIBLE) {
			toggleSettingsFragment(true);
			return;
		}

		super.onBackPressed();
	}

	private SpringAnimation getContentCardSpringAnimationFactory(float velocity, float restPosition) {
		return new SpringAnimation(mContentCard, DynamicAnimation.TRANSLATION_Y).setStartVelocity(velocity)
				.setSpring(new SpringForce(restPosition).setStiffness(SpringForce.STIFFNESS_LOW).setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY))
				.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
					@Override
					public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
						float progress = value / (-mContentCardHeight + 100);
						mContentCard.setDrawerImageProgress(progress);
						mMapMaskingView.setVisibility(progress < 0.5f ? View.VISIBLE : View.INVISIBLE);
					}
				});
	}

	private void toggleInfoView(boolean forced) {
		if (mAboutView.getVisibility() == View.VISIBLE || forced) {
			mAboutView.animate().alpha(0f).withEndAction(new Runnable() {
				@Override
				public void run() {
					mAboutView.setVisibility(View.GONE);
					mMapMaskingView.setVisibility(View.GONE);
				}
			}).start();

			mAppInfoButton.setImageResource(R.drawable.ic_info_vector);
			return;
		}

		mAboutView.setBottomPadding(mButtonsContainer.getHeight() + PADDING_LARGE);

		mAppInfoButton.setImageResource(R.drawable.ic_close_vector);
		mAboutView.setVisibility(View.VISIBLE);
		mMapMaskingView.setVisibility(View.VISIBLE);
		mAboutView.animate().alpha(1f).start();

	}

	private void toggleAppContentView(boolean forced) {
		if (mContentCardSpringAnimation != null) mContentCardSpringAnimation.cancel();
		final float restPositon = !forced && mContentCard.getTranslationY() < 0 ? 0 : -mContentCardHeight + 100;
		mContentCardSpringAnimation = getContentCardSpringAnimationFactory(0, restPositon);
		mContentCardSpringAnimation.start();

	}

	private void toggleTripListView(boolean forced) {
		mViewContainer.setVisibility(View.VISIBLE);
		if (mPromptListView == null && !forced) {
			mPromptListView = new PromptListView(MapActivity.this);
			mPromptListView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			mPromptListView.setOnPromptItemClickListener(new PromptsRecyclerView.OnPromptItemClickedListener() {
				@Override
				public void onPromptItemClick(View view, int position) {
					Intent intent = new Intent(MapActivity.this, PromptDetailsActivity.class);
					intent.putExtra("position", position);

					MapActivity.this.startActivity(intent);
				}
			});

			mPromptListView.setAddTripClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					mDialogFragment = TwoButtonDialog.newInstance("new_prompt", getString(R.string.dialog_add_destination_warning_message), getString(android.R.string.ok), getString(android.R.string.cancel));
					mDialogFragment.show(getSupportFragmentManager(), "new_prompt");
				}
			});

			mTripListButton.setImageResource(R.drawable.ic_close_vector);
//					mPromptListView.refreshList();

			mPromptListView.setBottomPadding(mButtonsContainer.getHeight() + PADDING_LARGE);

			//TODO: add OnPromptItemClickListener
			mViewContainer.removeAllViews();
			mViewContainer.addView(mPromptListView);
			mViewContainer.animate().alpha(1).withEndAction(new Runnable() {
				@Override
				public void run() {
					mMapMaskingView.setVisibility(View.VISIBLE);
				}
			}).start();
		} else {
			mViewContainer.animate().alpha(0).withEndAction(new Runnable() {
				@Override
				public void run() {
					mViewContainer.setVisibility(View.GONE);
					mViewContainer.removeAllViews();
					mPromptListView = null;
					mMapMaskingView.setVisibility(View.GONE);
				}
			}).start();

			mTripListButton.setImageResource(R.drawable.ic_logo_vector);
		}
	}

	private void toggleSettingsFragment(boolean forced) {

		mSettingsContainer.setVisibility(View.VISIBLE);

		mMapTripView.setPausedState(mPreferenceManager.isRecordingPaused());

		// this is happening on demand instead of onCreate because it requires layout element heights
		if (mPreferenceFragment == null && !forced) {
			// add the settings fragment
			mPreferenceFragment = SettingsFragment.newInstance(mButtonsContainer.getHeight() + PADDING_LARGE);
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.settings_fragment_container, mPreferenceFragment, "settings");
			ft.commit();
			mAppSettingsButton.setImageResource(R.drawable.ic_close_vector);
			mMapMaskingView.setVisibility(View.VISIBLE);
			mSettingsContainer.animate().alpha(1).start();
		} else {
			if (mPreferenceFragment == null) return;

			mSettingsContainer.animate().alpha(0).withEndAction(new Runnable() {
				@Override
				public void run() {
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					ft.remove(mPreferenceFragment);
					ft.commit();
					mSettingsContainer.setVisibility(View.GONE);
					mPreferenceFragment = null;
					mMapMaskingView.setVisibility(View.GONE);
				}
			}).start();

			mAppSettingsButton.setImageResource(R.drawable.ic_settings_vector);
		}
	}

//	private void toggle() {
//		EventBus.getDefault().post(new LocationLoggingEvent.PauseResume(mPreferenceManager.isRecordingPaused()));
//		if (mPreferenceManager.isRecordingPaused())
//			mMapTripView.mPauseMaskingView.setVisibility(View.GONE);
//		else mMapTripView.mPauseMaskingView.setVisibility(View.VISIBLE);
//		mPreferenceManager.togglePauseRecording();
//	}
//
//	private void showTogglePrompt() {
//		int title = SharedPreferenceManager.getInstance(MapActivity.this).isRecordingPaused() ?
//				R.string.should_resume_title : R.string.should_pause_title;
//
//		int message = SharedPreferenceManager.getInstance(MapActivity.this).isRecordingPaused() ?
//				R.string.should_resume_message : R.string.should_pause_message;
//
//		AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
//		builder.setTitle(title);
//		builder.setMessage(message);
//		builder.setNegativeButton(android.R.string.cancel, null);
//		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialogInterface, int i) {
//				toggle();
//			}
//		});
//		builder.show();
//	}

	private void checkTimeCutoffAndAlertUser() {
		if (RecordingUtils.isOngoing(this)) return;

		if (RecordingUtils.isComplete(this)) {
			mFinished.setVisibility(View.VISIBLE);
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

	@SuppressLint("SetTextI18n")
	private void updateDebugView() {
		mMapTripView.mDebugGeofenceActive.setText("geofence exists: " + Session.getInstance().isGeofenceActive());

		mMapTripView.mDebugGeofenceDwell.setText("geofence dwell: " + Session.getInstance().isGeofenceDwell());

		mMapTripView.mDebugGeofenceLoitering.setText("geofence loitering: " + Session.getInstance().isGeofenceLoitering());

		mMapTripView.mDebugGeofencePurpose.setText("geofence purpose recorded: " + Session.getInstance().isGeofencePurposeRecorded());

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

		if (mDialogFragment != null) mDialogFragment.dismiss();

		final ArrayList<String> promptPositions = (mPromptResponses.getCurrentPromptAnswer() != null) ?
				mPromptResponses.getCurrentPromptAnswer().getAnswer() :
				new ArrayList<String>();

		mDialogFragment = PromptDialog.newInstance(prompt, mPromptResponses.getPosition(), promptPositions);
		mDialogFragment.show(getSupportFragmentManager(), "" + prompt.getId());

	}

	private boolean shouldShowPrompt() {
		if (RecordingUtils.isComplete(this)) return false;
        if (mNumberOfPrompts < 1) return false;

		return !(mPrompts == null || mPrompts.size() < 1);
	}

	private void showPrompt() {

		if (!shouldShowPrompt()) return;

		if (mPromptResponses.hasNext()) {
			showPromptDialog(mPrompts.get(mPromptResponses.getPosition()));
		} else {
			submitGeofenceSurvey();
			checkGeofenceSurvey();
		}

	}

	private void showStoppedQuestion() {

		if (!shouldShowPrompt()) return;
		mDialogFragment = TwoButtonDialog.newInstance("stopped_prompt", getString(R.string.notification_stopped_message), getString(R.string.correct), getString(R.string.dismiss));

		mDialogFragment.show(getSupportFragmentManager(), "prompt");

	}

	private void updateDaysRemainingUI() {
		long daysRemaining = RecordingUtils.getCutoffDays(this);
		if (daysRemaining == -1) mContentCard.setDone();
		else {
			mContentCard.setCurrentDay((int) daysRemaining);
			if (mNumberOfDays == -1) {
				mContentCard.setTotalDays(DateTime.parse(BuildConfig.ABSOLUTE_CUTOFF).toString(DateTimeFormat.shortDate()));
			} else {
				mContentCard.setTotalDays(mNumberOfDays);
			}
		}
	}

	@SuppressLint("StringFormatMatches")
	private void checkGeofenceSurvey() {
		if (RecordingUtils.isComplete(this)) return;
		if (!RecordingUtils.hasFinishedAutomaticPrompts(this)) return;

		if (mPreferenceManager.getHasRespondedToContinueMessage()) return;

		final long daysRemaining = TimeUnit.DAYS.convert(RecordingUtils.getCutoffTime(this) - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		if (BuildConfig.FLAVOR.equals("montreal")) {
			String message = String.format(getString(R.string.max_trips_completed_message), mMaximumNumberOfPrompts);
			mDialogFragment = TwoButtonDialog.newInstance("complete", message, getString(R.string.yes), getString(R.string.no));
			mDialogFragment.show(getSupportFragmentManager(), "prompt");
		} else {
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


	}

	private void cancelGeofenceSurvey() {
		if (mDialogFragment == null) return;
		mDialogFragment.dismiss();
		mDialogFragment = null;
		mDialog = null;
		mPromptResponses = null;
		RecordingUtils.cancelGeofenceNotification(getApplicationContext());
		Session.getInstance().setGeofencePurposeRecorded(true);
		Session.getInstance().setShowDwellDialog(false);

		// Now insert a cancelled prompt answer
		PromptAnswer promptAnswer = new PromptAnswer()
				.withPrompt("")
				.withAnswer("")
				.withLatitude(Session.getInstance().getLastRecordedLatitude())
				.withLongitude(Session.getInstance().getLastRecordedLongitude())
				.withDisplayedAt(DateUtils.formatDateForBackend(Session.getInstance().getGeofenceTimestamp()))
				.withRecordedAt(DateTime.now().toString(ISODateTimeFormat.dateTime()))
				.withUuid(UUID.randomUUID().toString())
				.withUploaded(false)
				.withCancelled(true)
				.withCancelledAt(DateTime.now().toString(ISODateTimeFormat.dateTime()))
				.withPromptNumber(-1);

		ItinerumDatabase.getInstance(this).promptDao().insert(promptAnswer);

		Session.getInstance().setGeofenceState(Session.GeofenceState.ANSWERED);

	}

	private void submitGeofenceSurvey() {
		RecordingUtils.cancelGeofenceNotification(getApplicationContext());
		mDialog = null;

		// TODO: clone because this should be done async
		ItinerumDatabase.getInstance(this).promptDao().insert(mPromptResponses.getPromptAnswers().toArray(new PromptAnswer[mPromptResponses.getPromptAnswers().size()]));
		//increment the number of recoded prompts

		mPromptResponses = null;

		Session.getInstance().setGeofenceState(Session.GeofenceState.ANSWERED);

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
			updatePoints(new DateTime(mPreferenceManager.getFromDate()), new DateTime(mPreferenceManager.getToDate()));
		}
	}

	@Override
	public void onDateButtonClicked(ContentOverlayView.DateState dateState) {
		mPreferenceManager.setDateState(dateState);

		Calendar c = Calendar.getInstance();

		switch (dateState) {
			case TODAY:
				mPreferenceManager.resetDatesToDefault();
				break;
			case YESTERDAY:
				mPreferenceManager.resetDatesToDefault();
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
				c.add(Calendar.MILLISECOND, -1);
				mPreferenceManager.setToDate(c.getTime());

				c.add(Calendar.MILLISECOND, 1);
				c.add(Calendar.DATE, -1);
				mPreferenceManager.setFromDate(c.getTime());
				break;
			case LAST_SEVEN:
				mPreferenceManager.resetDatesToDefault();
				c.add(Calendar.DATE, -6);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);

				mPreferenceManager.setFromDate(c.getTime());

				break;
			case ALL_TIME:
				mPreferenceManager.resetDatesToDefault();
				c.setTimeInMillis(0);
				mPreferenceManager.setFromDate(c.getTime());
				break;
			case CUSTOM:
				break;
		}

		mContentCard.setSelectedDateState(dateState);

		mDatesSet = true;
		toggleAppContentView(false);
		drawMap();
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

		PromptAnswerGroup answers = PromptAnswerGroup.sortPrompts(ItinerumDatabase.getInstance(this).promptDao().getAllRegisteredPromptAnswers(), mNumberOfPrompts).get(position);
		PromptDetailsView view = new PromptDetailsView(this);
		view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		view.setPrompts(answers);
		view.setDateTimePickerListener(this);
	}

	@Override
	public void onDateClicked(DatePickerDialog.OnDateSetListener listener, int year, int monthOfYear, int dayOfMonth, DateTime minDateTime, String title) {

		Calendar minDate;

		if (minDateTime == null) minDate = new DateTime(mPreferenceManager.getQuestionnaireCompleteDate()).toCalendar(Locale.US);
		else minDate = minDateTime.toCalendar(Locale.US);


		Calendar maxDate = DateTime.now().toCalendar(Locale.US);

		DatePickerDialog dialog = DatePickerDialog
				.newInstance(listener, year, monthOfYear, dayOfMonth);

		dialog.setTitle(title);

		dialog.setMinDate(minDate);
		dialog.setMaxDate(maxDate);

		dialog.setAccentColor(getResources().getColor(R.color.base));
		dialog.show(getFragmentManager(), "datepicker_dialog");
	}

	@Override
	public void onTimeClicked(TimePickerDialog.OnTimeSetListener listener, int hourOfDay, int minute, boolean isToday, String title) {

		TimePickerDialog dialog = TimePickerDialog.newInstance(listener, hourOfDay, minute, DateFormat.is24HourFormat(this));

		if (isToday) dialog.setMaxTime(hourOfDay, minute, 59);

		dialog.setTitle(title);
		dialog.setAccentColor(getResources().getColor(R.color.base));
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

		if (mMapUpdateDisposible != null) mMapUpdateDisposible.dispose();

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

			if (mMapUpdateDisposible != null) mMapUpdateDisposible.dispose();

			final int position = (int) circle.getTag();
			PromptAnswerGroup promptAnswer = PromptAnswerGroup.sortPrompts(ItinerumDatabase.getInstance(this).promptDao().getAllRegisteredPromptAnswers(), mNumberOfPrompts).get(position);

			mButtonMoreInfoPromptDetails.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(MapActivity.this, PromptDetailsActivity.class);
					intent.putExtra("position", position);

					List<Pair<View, String>> pairs = new ArrayList<>();

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
			mMap.animateCamera(CameraUpdateFactory.newLatLng(promptAnswer.getLatLng()), 200, null);
		}
	}

	@Override
	public void onCameraMove() {
		if (mMapUpdateDisposible != null) mMapUpdateDisposible.dispose();
	}

	@Override
	public void onMapClick(LatLng latLng) {
		if (mClickedMarkerRadius != null) {
			mClickedMarkerRadius.remove();
		}

		if (mMapUpdateDisposible != null) mMapUpdateDisposible.dispose();

		BottomSheetBehavior.from(mBottomsheetPointDetails).setState(BottomSheetBehavior.STATE_HIDDEN);
		BottomSheetBehavior.from(mBottomsheetPromptDetails).setState(BottomSheetBehavior.STATE_HIDDEN);
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
		mMap.setOnMapClickListener(this);
		mMap.setOnMarkerClickListener(this);
		mMap.setOnCircleClickListener(this);
		mMap.setOnCameraMoveListener(this);

		moveCamera(mLastLocation, mMap, 15);

		drawMap();

	}

	@Override
	public void onPositiveButtonClicked(DialogFragment fragment) {

		if (fragment instanceof TwoButtonDialog) {

			switch (((TwoButtonDialog) fragment).getType()) {
				case "pause_recording":
					((SwitchPreference) mPreferenceFragment.findPreference("PAUSE_RECORDING")).setChecked(true);
					mPreferenceManager.setRecordingPaused(true);
					EventBus.getDefault().post(new LocationLoggingEvent.PauseResume(true));
					break;
				case "stopped_prompt":
					mPromptResponses = new PromptAnswerGroup(MapActivity.this);
					showPrompt();
					break;
				case "complete":
					mPreferenceManager.setOngoingPrompts(true);
					mPreferenceManager.setHasRespondedToContinueMessage(true);
					fragment.dismiss();
					//Todo: make this a snackbar or toast
					mDialog = new AlertDialog.Builder(MapActivity.this)
							.setTitle(R.string.notification_title)
							.setMessage(R.string.thanks)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									mDialog.dismiss();
									mDialog = null;
								}
							})
							.show();
					break;
				case "new_prompt":
					Intent intent = new Intent(MapActivity.this, PromptDetailsActivity.class);
					intent.putExtra("new_prompt", true);
					MapActivity.this.startActivity(intent);
					break;
				default:
					if (BuildConfig.DEBUG) throw new Error(((TwoButtonDialog) fragment).getType() + " is not implemented");
			}

			fragment.dismiss();
			return;
		}

		PromptAnswer answer = ((PromptDialog) mDialogFragment).getPromptAnswer();

		if (answer != null) {
			mPromptResponses.setCurrentPromptAndIncrement(answer);

			mDialogFragment.dismiss();
			mDialogFragment = null;

			showPrompt();

		} else {
			Toast.makeText(MapActivity.this, R.string.toast_provide_answer, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onNeutralButtonClicked(DialogFragment fragment) {
		if (fragment instanceof TwoButtonDialog) return;

		//TODO: save response here before going back?
		mPromptResponses.setPosition(mPromptResponses.getPosition() - 1);
		showPrompt();
	}

	@Override
	public void onNegativeButtonClicked(DialogFragment fragment) {
		if (fragment instanceof TwoButtonDialog) {
			if (((TwoButtonDialog) fragment).getType().equals("complete")) {
				mPreferenceManager.setHasRespondedToContinueMessage(true);
				mPreferenceManager.setOngoingPrompts(false);
				((DMApplication) getApplication()).stopLoggingService();
				final long daysRemaining = TimeUnit.DAYS.convert(RecordingUtils.getCutoffTime(this) - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
				fragment.dismiss();
				mDialog = new AlertDialog.Builder(MapActivity.this)
						.setTitle(R.string.notification_title)
						.setMessage(String.format(getString(R.string.no_ongoing_message), daysRemaining))
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								mDialog.dismiss();
								mDialog = null;
								checkTimeCutoffAndAlertUser();
							}
						})
						.show();
				return;
			} else if (((TwoButtonDialog) fragment).getType().equals("pause_recording")) {
				fragment.dismiss();
				return; //calling cancelGeofenceSurvey() causes a crash
			} else {
				fragment.dismiss();
			}
		}
		cancelGeofenceSurvey();
	}
}