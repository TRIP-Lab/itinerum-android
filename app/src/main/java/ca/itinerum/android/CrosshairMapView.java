package ca.itinerum.android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by stewjacks on 2018-01-26.
 */

public class CrosshairMapView extends FrameLayout implements OnMapReadyCallback {
	@BindView(R.id.mapview) MapView mMapview;
	@BindView(R.id.crosshair_view) ImageView mCrosshairView;
	private GoogleMap mMap;
	private OnMapReadyCallback mMapReadyCallback;
	private boolean mMapGesturesEnabled = true;

	public CrosshairMapView(@NonNull Context context) {
		this(context, null);
		onFinishInflate();
	}

	public CrosshairMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CrosshairMapView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_crosshair_map, this);

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mMapview != null) {
			mMapview.onCreate(null);
			mMapview.onStart();
			mMapview.onResume();
			mMapview.getMapAsync(this);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mMapview != null) {
			mMapview.onPause();
			mMapview.onStop();
			mMapview.onDestroy();
		}
		super.onDetachedFromWindow();
	}

	public LatLng getMapCentrePoint() {
		if (mMap != null) {
			return mMap.getCameraPosition().target;
		}
		return null;
	}

	public void setMapReadyCallback(OnMapReadyCallback callback) {
		mMapReadyCallback = callback;
	}

	public void setCrosshairVisible(boolean visible) {
		mCrosshairView.setVisibility(visible ? VISIBLE : GONE);
	}

	public void setMapGesturesEnabled(boolean enabled) {
		// saved because map may not be ready when this is set.
		mMapGesturesEnabled = enabled;
		if (mMap != null) mMap.getUiSettings().setAllGesturesEnabled(enabled);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		mMap.getUiSettings().setAllGesturesEnabled(mMapGesturesEnabled);
		if (mMapReadyCallback != null) mMapReadyCallback.onMapReady(googleMap);
	}
}
