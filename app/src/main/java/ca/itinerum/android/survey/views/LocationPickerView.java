package ca.itinerum.android.survey.views;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.CrosshairMapView;
import ca.itinerum.android.R;
import ca.itinerum.android.sync.retrofit.LocationUserSurveyAnswer;
import ca.itinerum.android.sync.retrofit.Survey;
import ca.itinerum.android.utilities.Logger;
import ca.itinerum.android.utilities.SystemUtils;

/**
 * Created by stewjacks on 2017-01-19.
 */

//TODO: onLowMemoryState for MapView isn't ever called. This has to be done at the activity level via some sort of broadcast, so the mapview needs to be registered with the activity.

public class LocationPickerView extends BaseSurveyView implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private GoogleMap mMap;
	private LatLng mLastLocation;

	private GoogleApiClient mGoogleApiClient;
	private float mLastZoom = 0f;

	private Geocoder mGeocoder;
	private AsyncTask<String, Void, List<Address>> mTask;

	@BindView(R.id.location_title) TextView mLocationTitle;
	@BindView(R.id.location_paragraph) TextView mLocationParagraph;
	@BindView(R.id.location_field) EditText mLocationField;
	@BindView(R.id.location_search_button) Button mLocationSearchButton;
	//TODO: this view is tightly coupled to the child crosshairmapview. This logic should be decoupled when someone has the time
	@BindView(R.id.crosshair_mapview) CrosshairMapView mCrosshairMapview;
	@BindView(R.id.mapview) MapView mMapview;
	@BindView(R.id.crosshair_view) ImageView mCrosshairView;

	@BindString(R.string.location_title) String mLocationTitleString;
	@BindString(R.string.location_paragraph) String mLocationParagraphString;
	@BindString(R.string.location_home_component) String HOME_COMPONENT;
	@BindString(R.string.location_work_component) String WORK_COMPONENT;
	@BindString(R.string.location_school_component) String SCHOOL_COMPONENT;

	public LocationPickerView(Context context, Survey survey) {
		this(context, null, 0);
		mSurvey = survey;
		onFinishInflate();
	}

	public LocationPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_location_picker, this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);

		mCrosshairMapview.setMapReadyCallback(this);

		// Create an instance of GoogleAPIClient.
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(mContext)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
		}


		mGeocoder = new Geocoder(mContext);

		mLocationField.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				mLocationSearchButton.setEnabled(!StringUtils.isBlank(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		mCrosshairView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mListener.onCanAdvance(canAdvance());
					SystemUtils.hideKeyboardFrom(mContext, LocationPickerView.this);
				}
				return false;
			}
		});

		mLocationField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if (i == EditorInfo.IME_ACTION_SEARCH) {
					getCurrentLocation(textView.getText().toString());
					return true;
				}
				return false;
			}
		});

		mLocationSearchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				getCurrentLocation(mLocationField.getText().toString());
			}
		});

		configureViewForSurveyType();
	}

	private void getCurrentLocation(String search) {

		if (mTask != null) mTask.cancel(true);

		mTask = new AsyncTask<String, Void, List<Address>>() {

			LatLng target;

			@Override
			protected void onPreExecute() {
				if (mMap != null) target = mMap.getCameraPosition().target;
				super.onPreExecute();
			}

			@Override
			protected List<Address> doInBackground(String... searchFields) {
				Geocoder geo = new Geocoder(mContext);

				if (mMap == null || searchFields[0] == null) return null;

				List<Address> location = new ArrayList<>();

				try {
					if (target == null) {
						location = geo.getFromLocationName(searchFields[0], 20);
					} else {

						double latRadian = Math.toRadians(target.latitude);
						double dx = 170000;
						double dy = 170000;

						double swlat = target.latitude + (180 / Math.PI) * (-dy / 6378137);
						double swlon = target.longitude + (180 / Math.PI) * (-dx / 6378137) / Math.cos(latRadian);

						double nelat = target.latitude + (180 / Math.PI) * (dy / 6378137);
						double nelon = target.longitude + (180 / Math.PI) * (dx / 6378137) / Math.cos(latRadian);

						location = geo.getFromLocationName(searchFields[0], 5, swlat, swlon, nelat, nelon);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return location;
			}

			public void onPostExecute(List<Address> location) {
				if (location != null) {

					if (location.size() == 0) {
						Toast.makeText(mContext, mContext.getString(R.string.location_not_found), Toast.LENGTH_LONG).show();
						return;
					}

					try {
						SystemUtils.hideKeyboardFrom(mContext, LocationPickerView.this);
					} catch (Exception e) {
						Logger.l.e(e.toString());
					}

					Address address = location.get(0);

					float lowestDistance = Float.MAX_VALUE;
					for (Address addr : location) {
						float[] results = new float[3];
						LatLng centrepoint = mCrosshairMapview.getMapCentrePoint();
						Location.distanceBetween(centrepoint.latitude, centrepoint.longitude, addr.getLatitude(), addr.getLongitude(), results);
						if (results[0] < lowestDistance) {
							address = addr;
							lowestDistance = results[0];
						}
					}

					if (mMap != null) {
						LatLng loc = new LatLng(address.getLatitude(), address.getLongitude());
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, Math.max(mMap.getCameraPosition().zoom, 15)));
						if (mListener != null) mListener.onCanAdvance(canAdvance());
					}
				} else {
					Toast.makeText(mContext, R.string.location_search_error, Toast.LENGTH_SHORT).show();
				}

			}

		}.execute(search);
	}

	private void configureViewForSurveyType() {

		switch (mSurvey.getId()) {
			case 105:
				mLocationTitleString = String.format(mLocationTitleString, HOME_COMPONENT);
				mLocationParagraphString = String.format(mLocationParagraphString, HOME_COMPONENT.toLowerCase());
				break;
			case 106:
				mLocationTitleString = String.format(mLocationTitleString, SCHOOL_COMPONENT);
				mLocationParagraphString = String.format(mLocationParagraphString, SCHOOL_COMPONENT.toLowerCase());
				break;
			case 107:
				mLocationTitleString = String.format(mLocationTitleString, WORK_COMPONENT);
				mLocationParagraphString = String.format(mLocationParagraphString, WORK_COMPONENT.toLowerCase());
				break;
			default:

				mLocationTitleString = mSurvey.getPrompt();
				mLocationParagraphString = null;

		}
		mLocationTitle.setText(mLocationTitleString);
		mLocationParagraph.setText(mLocationParagraphString);
	}

	private void setCurrentLatLng(LatLng latLng) {
		mLastLocation = latLng;
		if (mMap != null) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation, 15));
	}


	@Override
	public void setResult(Object result) {
		if (result instanceof LocationUserSurveyAnswer) {
			LocationUserSurveyAnswer response = (LocationUserSurveyAnswer) result;
			response.getLatitude();
			setCurrentLatLng(response.toLatLng());
		}
	}

	//TODO: this should return an object that can be cast later.
	@Override
	public Object getSurveyResponse() {
		LatLng centre = mCrosshairMapview.getMapCentrePoint();
		return new LocationUserSurveyAnswer(centre.latitude, centre.longitude);
	}

	@Override
	public boolean canAdvance() {
		return mCrosshairMapview.getMapCentrePoint() != null;
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (lastLocation != null && mLastLocation == null) {
			mLastLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation, 15));
		}
	}

	@SuppressWarnings("HardCodedStringLiteral")
	@Override
	public void onConnectionSuspended(int i) {
		Logger.l.e("onConnectionSuspended", i);

	}

	@SuppressWarnings("HardCodedStringLiteral")
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Logger.l.e("onConnectionFailed", connectionResult.toString());
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}

		mMap.setMyLocationEnabled(true);
		if (mLastLocation != null) {
			// This signifies there is a last position that isn't the last known location of the device
			// i.e. a cached location from setResult(). Therefore the user can advance.

			mListener.onCanAdvance(canAdvance());
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation, 15));
		}
		mGoogleApiClient.connect();
	}


	@Override
	public void onMyLocationChange(Location location) {
		boolean shouldCentre = (mLastLocation == null);
		mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
		if (shouldCentre && mMap != null) {
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation, 15));
		}
	}
}
