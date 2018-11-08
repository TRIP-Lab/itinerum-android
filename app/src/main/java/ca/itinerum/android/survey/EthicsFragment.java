package ca.itinerum.android.survey;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.itinerum.android.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EthicsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EthicsFragment extends NamedFragment {
	private OnFragmentInteractionListener mListener;

	@BindView(R.id.continue_button) AppCompatButton mFragmentContinueButton;
	@BindView(R.id.fragment_decline_button) AppCompatButton mFragmentDeclineButton;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment EthicsFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static EthicsFragment newInstance(String name) {
		EthicsFragment fragment = new EthicsFragment();
		Bundle args = new Bundle();
		args.putString(NAME, name);
		fragment.setArguments(args);
		return fragment;
	}

	public EthicsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_research_ethics, container, false);
		ButterKnife.bind(this, v);

		mFragmentContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onButtonPressed(true);
			}
		});
		mFragmentDeclineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onButtonPressed(false);
			}
		});

		return v;
	}

	private void onButtonPressed(boolean result) {
		if (mListener != null) {
			Map<String, Object> results = new HashMap<>();
			results.put(getName(), result);
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

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
}
