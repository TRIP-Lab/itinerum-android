package ca.itinerum.android.survey;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;

public class PermissionsFragment extends NamedFragment implements ActivityCompat.OnRequestPermissionsResultCallback {
	@BindView(R.id.fragment_continue_button) Button mContinueButton;
	@BindView(R.id.location_button) Button mLocationButton;
	@BindView(R.id.permission_warning) TextView mPermissionWarning;

	private OnFragmentInteractionListener mListener;

	private final int LOCATION_PERMISSION_CODE = 59662;

	public static PermissionsFragment newInstance(String name) {
		PermissionsFragment fragment = new PermissionsFragment();
		Bundle args = new Bundle();
		args.putString(NAME, name);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_permissions, container, false);
		ButterKnife.bind(this, v);

		mLocationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLocationButtonPressed();
			}
		});

		mContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNextButtonPressed();
			}
		});

		enableNextButton(locationEnabled());

		return v;
	}

	private boolean locationEnabled() {
		return !(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
	}

	private void onLocationButtonPressed() {
		if (!locationEnabled()) {
			String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
			ActivityCompat.requestPermissions(getActivity(), permission, LOCATION_PERMISSION_CODE);
		}
	}

	public void onNextButtonPressed() {
		if (mListener != null) {
			mListener.onFragmentInteraction(getName(), null);
		}
	}

	private void enableNextButton(boolean enabled) {
		if (enabled) {
			mContinueButton.setEnabled(true);
			mContinueButton.setClickable(true);
			mContinueButton.setAlpha(1f);
		} else {
			mContinueButton.setEnabled(false);
			mContinueButton.setClickable(false);
			mContinueButton.setAlpha(0.5f);
		}
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) activity;
		} else {
			throw new RuntimeException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	private void showWarning() {
		mPermissionWarning.setVisibility(View.VISIBLE);
	}

	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == LOCATION_PERMISSION_CODE) {
			enableNextButton(true);
			if (grantResults[0] == -1) {
				showWarning();
			} else {
				onNextButtonPressed();
			}
		}

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}
