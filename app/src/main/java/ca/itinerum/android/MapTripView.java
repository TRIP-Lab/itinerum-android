package ca.itinerum.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.utilities.PausePlayFab;

/**
 * Created by stewjacks on 2018-01-23.
 */

public class MapTripView extends FrameLayout implements OnMapReadyCallback {
	@BindView(R.id.mapview) MapView mMapview;
	@BindView(R.id.pause_masking_view) TextView mPauseMaskingView;
	@BindView(R.id.progress_bar) ProgressBar mProgressBar;
	@BindView(R.id.powered_by) TextView mPoweredBy;
	@BindView(R.id.points_details_text) TextView mPointsDetailsText;
	@BindView(R.id.version) TextView mVersion;
	@BindView(R.id.uuid) TextView mUuid;
	@BindView(R.id.toggle_gps) Button mToggleGps;
	@BindView(R.id.mode_prompt_button) Button mModePromptButton;
	@BindView(R.id.fab) PausePlayFab mFab;

	@BindView(R.id.status_text) TextView mStatusText;

	@BindView(R.id.debug_view) LinearLayout mDebugView;
	@BindView(R.id.debug_geofence_active) TextView mDebugGeofenceActive;
	@BindView(R.id.debug_geofence_loitering) TextView mDebugGeofenceLoitering;
	@BindView(R.id.debug_geofence_dwell) TextView mDebugGeofenceDwell;
	@BindView(R.id.debug_last_sync) TextView mDebugLastSync;
	@BindView(R.id.debug_next_sync) TextView mDebugNextSync;
	@BindView(R.id.current_mode_text) TextView mCurrentModeText;

	private GoogleMap mMap;
	private OnMapReadyCallback mOnMapReadyCallback;

	public MapTripView(Context context) {
		this(context, null);
		onFinishInflate();
	}

	public MapTripView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MapTripView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(getContext(), R.layout.view_map, this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		mProgressBar.setVisibility(View.INVISIBLE);

		mVersion.setText(BuildConfig.VERSION_NAME);

		mPoweredBy.setVisibility(View.VISIBLE);

		mPoweredBy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(getContext().getString(R.string.itinerum_url)));
				getContext().startActivity(intent);
			}
		});
	}

	public MapView getMapview() {
		return mMapview;
	}

	//TODO: Do this in activity
	public void setPausedState(boolean paused) {
		if (paused) {
			mFab.setPlay();
			mPauseMaskingView.setVisibility(View.VISIBLE);
		} else {
			mFab.setPaused();
			mPauseMaskingView.setVisibility(View.GONE);
		}
	}

	//TODO: Do this in activity
	public void setUuid(String uuid) {
		mUuid.setText(uuid);
	}

	public void setOnMapReadyCallback(OnMapReadyCallback callback) {
		mOnMapReadyCallback = callback;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		onStart();
	}

	public void onStart() {
		if (mMapview != null) {
			mMapview.getMapAsync(this);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		if (mOnMapReadyCallback != null) mOnMapReadyCallback.onMapReady(googleMap);
	}
}
