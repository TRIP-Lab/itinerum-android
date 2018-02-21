package ca.itinerum.android.survey;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;

/**
 * Created by stewjacks on 2017-01-22.
 */

public class WelcomeFragment extends NamedFragment{

	@BindView(R.id.fragment_continue_button) Button mContinueButton;

	private OnFragmentInteractionListener mListener;

	public static WelcomeFragment newInstance(String name) {
		WelcomeFragment fragment = new WelcomeFragment();
		Bundle args = new Bundle();
		args.putString(NAME, name);
		fragment.setArguments(args);
		return fragment;
	}

	public WelcomeFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_welcome, container, false);
		ButterKnife.bind(this, v);

		mContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onButtonPressed();
			}
		});
		checkButtonLock();

		return v;
	}

	private void checkButtonLock() {
		toggleContinueButton(true);
	}

	private void toggleContinueButton(boolean enabled) {
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

	public void onButtonPressed() {
		if (mListener != null) {

			Map<String, Object> results = new HashMap<>();
			mListener.onFragmentInteraction(getName(), results);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

}
